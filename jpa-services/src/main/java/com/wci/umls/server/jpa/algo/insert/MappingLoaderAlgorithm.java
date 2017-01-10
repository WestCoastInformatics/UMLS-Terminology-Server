/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractSourceInsertionAlgorithm;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Implementation of an algorithm to import mappings.
 */
public class MappingLoaderAlgorithm extends AbstractSourceInsertionAlgorithm {

  /** The xmap from map. */
  private Map<String, String> xmapFromMap = new HashMap<>();

  /** The xmap to map. */
  private Map<String, String> xmapToMap = new HashMap<>();

  /** The xmap from used set. */
  private Set<String> xmapFromUsedSet = new HashSet<>();

  /** The xmap to used set. */
  private Set<String> xmapToUsedSet = new HashSet<>();

  /** The xmap entries. */
  private Set<String> xmapEntries = new HashSet<>();

  /** The mapping add count. */
  private int mappingAddCount = 0;

  /** The mapping attribute add count. */
  private int mappingAttributeAddCount = 0;

  /** The mapset update count. */
  private int mapsetUpdateCount = 0;

  /**
   * Instantiates an empty {@link MappingLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public MappingLoaderAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("MAPPINGLOADER");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    ValidationResult validationResult = new ValidationResultJpa();

    if (getProject() == null) {
      throw new Exception("Mapping Loading requires a project to be set");
    }

    // Check the input directories

    String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    setSrcDirFile(new File(srcFullPath));
    if (!getSrcDirFile().exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    return validationResult;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting MAPPINGLOADING");

    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    // Set up the handler for identifier assignment
    final IdentifierAssignmentHandler handler =
        newIdentifierAssignmentHandler(getProject().getTerminology());
    handler.setTransactionPerOperation(false);
    handler.beginTransaction();

    try {

      logInfo("[MappingLoader] Checking for new/updated Mappings");
      commitClearBegin();

      //
      // Load the attributes.src file, keeping only mapping lines
      //
      List<String> lines =
          loadFileIntoStringList(getSrcDirFile(), "attributes.src",
              "(.*)(MAPSETNAME|MAPSETVERSION|TOVSAB|TORSAB|FROMRSAB|FROMVSAB|MAPSETGRAMMAR|MAPSETRSAB|MAPSETTYPE|MAPSETVSAB|MTH_MAPFROMEXHAUSTIVE|MTH_MAPTOEXHAUSTIVE|MTH_MAPSETCOMPLEXITY|MTH_MAPFROMCOMPLEXITY|MTH_MAPTOCOMPLEXITY|MAPSETXRTARGETID|MAPSETSID|XMAP|XMAPTO|XMAPFROM)(.*)",
              null);

      // Set the number of steps to the number of lines to be processed
      setSteps(lines.size());

      String fields[] = new String[14];

      for (String line : lines) {

        // Check for a cancelled call once every 100 lines
        if (getStepsCompleted() % 100 == 0) {
          checkCancel();
        }

        FieldedStringTokenizer.split(line, "|", 14, fields);

        // Fields:
        // 0 source_attribute_id
        // 1 sg_id
        // 2 attribute_level
        // 3 attribute_name
        // 4 attribute_value
        // 5 source
        // 6 status
        // 7 tobereleased
        // 8 released
        // 9 suppressible
        // 10 sg_type_1
        // 11 sg_qualifier_1
        // 12 source_atui
        // 13 hashcode

        // e.g.
        // 13370181|381548367|S|XMAP|1~1~9074007~RT~mapped_to~H55~TRUE~447637006~ACTIVE~1~db8b21b9-849e-53de-82ba-8fd4956b8e64~ALWAYS
        // H55|
        // SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||4cd197e2870636a62fcd3c706261471f|
        //

        // MAPPING attributes. Handle them within the loop.
        if (isMapSetAttribute(fields[3])) {
          processMapSetAttribute(line, fields);

          // Update the progress
          updateProgress();
        }

        // XMAP attributes. Save their information to maps
        else if (isXmapAttribute(fields[3])) {
          populateXmapMaps(line, fields);

          // Don't updateProgress for XMAP lines - they need to be process
          // outside of the loop
          if (!fields[3].equals("XMAP")) {
            // Update the progress
            updateProgress();
          }
        }
      }

      // Once all of the XmapMaps are populated, go through and process them
      for (final String xmapEntry : xmapEntries) {
        processXmapEntry(xmapEntry, handler);
        // Update the progress
        updateProgress();
        handler.silentIntervalCommit(getStepsCompleted(), RootService.logCt,
            RootService.commitCt);
      }

      commitClearBegin();
      handler.commitClearBegin();
      handler.close();

      // Finally, update all xmapSets
      for (MapSet mapSet : getCachedMapSets().values()) {
        updateMapSet(mapSet);
        mapsetUpdateCount++;
      }

      commitClearBegin();

      // If any mapTo or MapFrom entries were unused, log them
      final Set<String> xmapFromUnusuedSet = xmapFromMap.keySet();
      xmapFromUnusuedSet.removeAll(xmapFromUsedSet);
      for (String xmapFromUnusued : xmapFromUnusuedSet) {
        logWarn("Warning - XMAPFROM line never used: " + xmapFromUnusued);
      }

      final Set<String> xmapToUnusuedSet = xmapToMap.keySet();
      xmapToUnusuedSet.removeAll(xmapToUsedSet);
      for (String xmapToUnusued : xmapToUnusuedSet) {
        logWarn("Warning - XMAPTO line never used: " + xmapToUnusued);
      }

      logInfo("[MappingLoader] Added " + mappingAddCount + " new Mappings.");
      logInfo("[MappingLoader] Added " + mappingAttributeAddCount
          + " new Mapping Attributes.");
      logInfo("[MappingLoader] " + mapsetUpdateCount + " MapSets updated.");

      logInfo("  project = " + getProject().getId());
      logInfo("  workId = " + getWorkId());
      logInfo("  activityId = " + getActivityId());
      logInfo("  user  = " + getLastModifiedBy());
      logInfo("Finished MAPPINGLOADING");

    } catch (Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /**
   * Populate xmap maps.
   *
   * @param line the line
   * @param fields the fields
   * @throws Exception the exception
   */
  private void populateXmapMaps(String line, String fields[]) throws Exception {

    final String atn = fields[3];
    final String atv = fields[4];

    if (atn.equals("XMAP")) {
      xmapEntries.add(line);
    }
    if (atn.equals("XMAPFROM")) {
      final String fromId = atv.substring(0, atv.indexOf('~'));
      xmapFromMap.put(fromId, atv);
    }
    if (atn.equals("XMAPTO")) {
      final String toId = atv.substring(0, atv.indexOf('~'));
      xmapToMap.put(toId, atv);
    }

  }

  /**
   * Checks if is map set attribute.
   *
   * @param atn the atn
   * @return true, if is map set attribute
   */
  @SuppressWarnings("static-method")
  private boolean isMapSetAttribute(String atn) {
    if (atn.equals("MAPSETNAME") || atn.equals("MAPSETVERSION")
        || atn.equals("TOVSAB") || atn.equals("TORSAB")
        || atn.equals("FROMRSAB") || atn.equals("FROMVSAB")
        || atn.equals("MAPSETGRAMMAR") || atn.equals("MAPSETRSAB")
        || atn.equals("MAPSETTYPE") || atn.equals("MAPSETVSAB")
        || atn.equals("MTH_MAPFROMEXHAUSTIVE")
        || atn.equals("MTH_MAPTOEXHAUSTIVE")
        || atn.equals("MTH_MAPSETCOMPLEXITY")
        || atn.equals("MTH_MAPFROMCOMPLEXITY")
        || atn.equals("MTH_MAPTOCOMPLEXITY") || atn.equals("MAPSETXRTARGETID")
        || atn.equals("MAPSETSID")) {
      return true;
    }
    return false;
  }

  /**
   * Indicates whether or not xmap attribute is the case.
   *
   * @param atn the atn
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @SuppressWarnings("static-method")
  private boolean isXmapAttribute(String atn) {
    if (atn.equals("XMAP") || atn.equals("XMAPFROM") || atn.equals("XMAPTO")) {
      return true;
    }
    return false;
  }

  /**
   * Process map set attribute.
   *
   * @param line the line
   * @param fields the fields
   * @throws Exception the exception
   */
  private void processMapSetAttribute(String line, String fields[])
    throws Exception {

    final String srcAtomAltId = fields[1];
    final String atn = fields[3];
    final String atv = fields[4];

    final MapSet mapset = getCachedMapSet(srcAtomAltId);
    if (mapset == null) {
      logWarn(
          "Warning - mapSet not found with alt terminology id: " + srcAtomAltId
              + "." + " Could not process the following line:\n\t" + line);
      return;
    }
    if (atn.equals("MAPSETNAME")) {
      mapset.setName(atv);
    } else if (atn.equals("MAPSETVERSION")) {
      // n/a - version is picked up from the SAB
      // mapSet.setMapVersion(atv);
    } else if (atn.equals("TOVSAB")) {
      if (!ConfigUtility.isEmpty(mapset.getToTerminology())) {
        String version = atv.substring(mapset.getToTerminology().length());
        mapset.setToVersion(
            version.startsWith("_") ? version.substring(1) : version);
      } else {
        mapset.setToVersion(atv);
      }
    } else if (atn.equals("TORSAB")) {
      mapset.setToTerminology(atv);
      if (!ConfigUtility.isEmpty(mapset.getToVersion())) {
        String version = mapset.getToVersion().substring(atv.length());
        mapset.setToVersion(
            version.startsWith("_") ? version.substring(1) : version);
      }
    } else if (atn.equals("FROMRSAB")) {
      mapset.setFromTerminology(atv);
      if (!ConfigUtility.isEmpty(mapset.getFromVersion())) {
        String version = mapset.getFromVersion().substring(atv.length());
        mapset.setFromVersion(
            version.startsWith("_") ? version.substring(1) : version);
      }
    } else if (atn.equals("FROMVSAB")) {
      if (!ConfigUtility.isEmpty(mapset.getFromTerminology())) {
        String version = atv.substring(mapset.getFromTerminology().length());
        mapset.setFromVersion(
            version.startsWith("_") ? version.substring(1) : version);
      } else {
        mapset.setFromVersion(atv);
      }
    } else if (atn.equals("MAPSETGRAMMAR")) {
      // n/a - leave this as an attribute of the XR atom and don't render in map
      // set
    } else if (atn.equals("MAPSETXRTARGETID")) {
      // n/a - no need for this anymore - inverters should stop making it
    } else if (atn.equals("MAPSETRSAB")) {
      mapset.setTerminology(atv);
      // In case MAPSETVSAB was set first, strip off the RSAB part and use the
      // rest as the version
      if (!ConfigUtility.isEmpty(mapset.getVersion())) {
        final String version = mapset.getVersion().substring(atv.length());
        mapset.setVersion(
            version.startsWith("_") ? version.substring(1) : version);
      }
    } else if (atn.equals("MAPSETTYPE")) {
      mapset.setMapType(atv);
    } else if (atn.equals("MAPSETVSAB")) {
      mapset.setVersion(atv);
      // In case MAPSETRSAB was set first, strip off the RSAB part and use the
      // rest as the version

      if (!ConfigUtility.isEmpty(mapset.getTerminology())) {
        final String version =
            mapset.getVersion().substring(mapset.getTerminology().length());
        mapset.setVersion(
            version.startsWith("_") ? version.substring(1) : version);
      }

    } else if (atn.equals("MTH_MAPFROMEXHAUSTIVE")) {
      mapset.setFromExhaustive(atv);
    } else if (atn.equals("MTH_MAPTOEXHAUSTIVE")) {
      mapset.setToExhaustive(atv);
    } else if (atn.equals("MTH_MAPSETCOMPLEXITY")) {
      mapset.setComplexity(atv);
    } else if (atn.equals("MTH_MAPFROMCOMPLEXITY")) {
      mapset.setFromComplexity(atv);
    } else if (atn.equals("MTH_MAPTOCOMPLEXITY")) {
      mapset.setToComplexity(atv);
    } else if (atn.equals("MAPSETSID")) {
      mapset.setTerminologyId(atv);
      // Set the srcAtomAltId as an alternate terminology id
      mapset.getAlternateTerminologyIds().put(getProject().getTerminology(),
          srcAtomAltId);
    }
  }

  /**
   * Process xmap entry.
   *
   * @param xmapEntry the xmap entry
   * @param handler the handler
   * @throws Exception the exception
   */
  private void processXmapEntry(String xmapEntry,
    IdentifierAssignmentHandler handler) throws Exception {

    final String fields[] = new String[14];
    FieldedStringTokenizer.split(xmapEntry, "|", 14, fields);

    final String xmapFields[] = new String[12];
    FieldedStringTokenizer.split(fields[4], "~", 12, xmapFields);

    //
    // Load associated from entry from maps.
    //
    final String xmapFromEntry = xmapFromMap.get(xmapFields[2]);

    // If not found, fire warning. If found, mark entry as having been used.
    if (xmapFromEntry == null) {
      logWarn("Warning - from-entry not found: " + xmapFields[2] + "."
          + " Could not process the following line:\n\t" + xmapEntry);
      return;
    } else {
      xmapFromUsedSet.add(xmapFields[2]);
    }

    final String xmapFromFields[] = new String[6];
    FieldedStringTokenizer.split(xmapFromEntry, "~", 6, xmapFromFields);

    //
    // Load associated to entry from maps.
    //
    final String xmapToEntry = xmapToMap.get(xmapFields[5]);

    // If not found, fire warning. If found, mark entry as having been used.
    if (xmapToEntry == null) {
      logWarn("Warning - to-entry not found: " + xmapFields[5] + "."
          + " Could not process the following line:\n\t" + xmapEntry);
      return;
    } else {
      xmapToUsedSet.add(xmapFields[5]);
    }

    final String xmapToFields[] = new String[6];
    FieldedStringTokenizer.split(xmapToEntry, "~", 6, xmapToFields);

    //
    // Create and populate the new mapping
    //
    final Mapping mapping = new MappingJpa();
    // look up mapSet for this srcAtomId
    MapSet mapSet = getCachedMapSet(fields[1]);
    if (mapSet == null) {
      logWarn("Warning - mapSet not found: " + fields[1] + "."
          + " Could not process the following line:\n\t" + xmapEntry);
      return;
    }
    mapping.setMapSet(mapSet);
    mapping.setLastModifiedBy(getLastModifiedBy());

    mapping.setGroup(xmapFields[0]);
    mapping.setRank(xmapFields[1]);
    mapping.setFromTerminologyId(xmapFields[2]);
    mapping.setFromIdType(IdType.getIdType(xmapFromFields[3]));
    mapping.setRelationshipType(xmapFields[3]);
    mapping.setAdditionalRelationshipType(xmapFields[4]);
    mapping.setToTerminologyId(xmapFields[5]);
    mapping.setToIdType(IdType.getIdType(xmapToFields[3]));

    mapping.setRule(xmapFields[6]);
    mapping.setAdvice(xmapFields[11]);

    // if MAPATN is "ACTIVE" with nothing -> inactive, with 1 -> active
    mapping.setObsolete(false);
    mapping.setSuppressible(false);
    if (xmapFields[8].equals("ACTIVE")) {
      mapping.setObsolete(!xmapFields[9].equals("1"));
      mapping.setSuppressible(!xmapFields[9].equals("1"));
    }
    mapping.setPublished(false);
    mapping.setPublishable(true);

    final Terminology terminology = getCachedTerminology(fields[5]);
    if (terminology == null) {
      logWarn("Warning - terminology not found: " + fields[5] + "."
          + " Could not process the following line:\n\t" + xmapEntry);
      return;
    } else {
      mapping.setTerminology(terminology.getTerminology());
      mapping.setVersion(terminology.getVersion());
    }

    // Set terminology ids
    mapping.setTerminologyId(xmapFields[10]);

    // Calculate an identity for the xmap entry line as if it were an attribute
    // Note: do NOT persist the attribute - just use returned ATUI

    // Load the terminology that will be assigned to the new attribute
    Terminology setTerminology = getCachedTerminology(fields[5]);
    if (setTerminology == null) {
      logWarn("Warning - terminology not found: " + fields[5]
          + ".  Could not process line: " + xmapEntry);
      return;
    }

    // Create the fake attribute
    Attribute newAttribute = new AttributeJpa();
    newAttribute.setName(fields[3]);
    newAttribute.setValue(fields[4]);
    newAttribute.setTerminology(setTerminology.getTerminology());
    newAttribute.setTerminologyId("");

    // Load the containing object
    ComponentHasAttributes containerComponent =
        (ComponentHasAttributes) getComponent(fields[10], fields[1],
            getCachedTerminologyName(fields[11]), null);
    if (containerComponent == null) {
      logWarn("Warning - could not find Component for type: " + fields[10]
          + ", terminologyId: " + fields[1] + ", and terminology:" + fields[11]
          + ". Could not process line" + xmapEntry);
      return;
    }

    // Compute attribute identity
    String mappingAtui =
        handler.getTerminologyId(newAttribute, containerComponent);

    // Assign the ATUI to the mapping.
    mapping.getAlternateTerminologyIds().put(getProject().getTerminology(),
        mappingAtui);

    // Assign other terminology ids
    if (xmapFromFields[0] != null && !xmapFromFields[0].equals("")) {
      mapping.getAlternateTerminologyIds()
          .put(getProject().getTerminology() + "-FROMID", xmapFromFields[0]);
    }
    if (xmapFromFields[1] != null && !xmapFromFields[1].equals("")) {
      mapping.getAlternateTerminologyIds()
          .put(getProject().getTerminology() + "-FROMSID", xmapFromFields[1]);
    }
    if (xmapToFields[0] != null && !xmapToFields[0].equals("")) {
      mapping.getAlternateTerminologyIds()
          .put(getProject().getTerminology() + "-TOID", xmapToFields[0]);
    }
    if (xmapToFields[1] != null && !xmapToFields[1].equals("")) {
      mapping.getAlternateTerminologyIds()
          .put(getProject().getTerminology() + "-TOSID", xmapToFields[1]);
    }

    // Make mapping attributes
    if (xmapFromFields[4] != null && !xmapFromFields[4].equals("")) {
      mapping.getAttributes()
          .add(makeAttribute(mapping, "FROMRULE", xmapFromFields[4]));
    }
    if (xmapFromFields[5] != null && !xmapFromFields[5].equals("")) {
      mapping.getAttributes()
          .add(makeAttribute(mapping, "FROMRES", xmapFromFields[5]));
    }
    if (xmapToFields[4] != null && !xmapToFields[4].equals("")) {
      mapping.getAttributes()
          .add(makeAttribute(mapping, "TORULE", xmapToFields[4]));
    }
    if (xmapToFields[5] != null && !xmapToFields[5].equals("")) {
      mapping.getAttributes()
          .add(makeAttribute(mapping, "TORES", xmapToFields[5]));
    }

    addMapping(mapping);
    mappingAddCount++;

    mapSet.getMappings().add(mapping);

  }

  /**
   * Make attribute.
   *
   * @param mapping the mapping
   * @param name the name
   * @param value the value
   * @return the attribute
   * @throws Exception the exception
   */
  private Attribute makeAttribute(Mapping mapping, String name, String value)
    throws Exception {
    Attribute att = new AttributeJpa();
    att.setName(name);
    att.setValue(value);
    att.setLastModifiedBy(getLastModifiedBy());
    att.setObsolete(false);
    att.setSuppressible(false);
    att.setPublished(true);
    att.setPublishable(true);
    att.setTerminology(mapping.getTerminology());
    att.setVersion(mapping.getVersion());
    att.setTerminologyId("");

    addAttribute(att, mapping);
    mappingAttributeAddCount++;
    return att;
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();

    return params;
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return "Loads and processes an attributes.src file to load Mapping objects.";
  }

}