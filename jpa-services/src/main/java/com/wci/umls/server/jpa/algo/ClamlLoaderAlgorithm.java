/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.NameVariantType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.TermTypeStyle;
import com.wci.umls.server.model.meta.UsageType;
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to import RF2 snapshot data.
 */
public class ClamlLoaderAlgorithm extends HistoryServiceJpa implements
    Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The terminology. */
  String terminology;

  /** The terminology version. */
  String version;
  
  /** The terminology language. */
  String terminologyLanguage;

  /** hash set for storing default preferred names. */
  Map<Long, String> defaultPreferredNames = new HashMap<>();

  /** counter for objects created, reset in each load section. */
  int objectCt;

  /** The input file. */
  private String inputFile;

  /** The effective time. */
  String effectiveTime;

  /** The concept map. */
  Map<String, Concept> conceptMap = new HashMap<>();

  /** The roots. */
  List<String> roots = null;

  /** child to parent code map NOTE: this assumes a single superclass. */
  Map<String, String> childToParentCodeMap;

  /** Indicates subclass relationships NOTE: this assumes a single superclass. */
  Map<String, Boolean> parentCodeHasChildrenMap;

  /**
   * Instantiates an empty {@link ClamlLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public ClamlLoaderAlgorithm() throws Exception {
    super();
  }

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /**
   * Sets the terminology version.
   *
   * @param version the terminology version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Returns the input file.
   *
   * @return the input file
   */
  public String getInputFile() {
    return inputFile;
  }

  /**
   * Sets the input file.
   *
   * @param inputFile the input file
   */
  public void setInputFile(String inputFile) {
    this.inputFile = inputFile;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#compute()
   */
  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @Override
  public void compute() throws Exception {
    Logger.getLogger(getClass()).info("Starting loading Claml terminology");
    Logger.getLogger(getClass()).info("  inputFile = inputFile");
    Logger.getLogger(getClass()).info("  terminology = " + terminology);
    Logger.getLogger(getClass()).info("  version = " + version);

    FileInputStream fis = null;
    InputStream inputStream = null;
    Reader reader = null;
    try {

      setTransactionPerOperation(false);
      beginTransaction();

      if (!new File(inputFile).exists()) {
        throw new Exception("Specified input file does not exist");
      }

      // open input file and get effective time and version and language
      findVersion(inputFile);
      findLanguage(inputFile);

      // create Metadata
     /* Logger.getLogger(getClass()).info("  Create metadata classes");
      helper =
          new ClamlMetadataHelper(terminology, terminologyVersion,
              effectiveTime, contentService);
      conceptMap = helper.createMetadata();*/

      childToParentCodeMap = new HashMap<>();
      parentCodeHasChildrenMap = new HashMap<>();

      // Prep SAX parser
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      SAXParser saxParser = factory.newSAXParser();
      DefaultHandler handler = new LocalHandler();

      // Open XML and begin parsing
      File file = new File(inputFile);
      fis = new FileInputStream(file);
      inputStream = checkForUtf8BOM(fis);
      reader = new InputStreamReader(inputStream, "UTF-8");
      InputSource is = new InputSource(reader);
      is.setEncoding("UTF-8");
      saxParser.parse(is, handler);

      commit();

      // creating tree positions
      // first get isaRelType from metadata
/*      MetadataService metadataService = new MetadataServiceJpa();
      Map<String, String> hierRelTypeMap =
          metadataService.getHierarchicalRelationshipTypes(terminology,
              terminologyVersion);
      String isaRelType = hierRelTypeMap.keySet().iterator().next().toString();
      metadataService.close();

      // Let the service create its own transaction.
      Logger.getLogger(getClass()).info("Start creating tree positions.");
      for (String root : roots) {
        computeTreePositions(terminology, terminologyVersion,
            isaRelType, root);
      }*/
      close();

      Logger.getLogger(getClass()).info("Done ...");

    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception(
          "Conversion of Claml to RF2 objects failed", e);
    } finally {
      try {
        if (fis != null) {
          fis.close();
        }
      } catch (IOException e) {
        // do nothing
      }
      try {
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (IOException e) {
        // do nothing
      }
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        // do nothing
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#reset()
   */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

  /**
   * Fires a {@link ProgressEvent}.
   * @param pct percent done
   * @param note progress note
   */
  public void fireProgressEvent(int pct, String note) {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    Logger.getLogger(getClass()).info("    " + pct + "% " + note);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.jpa.services.helper.ProgressReporter#addProgressListener
   * (org.ihtsdo.otf.ts.jpa.services.helper.ProgressListener)
   */
  /**
   * Adds the progress listener.
   *
   * @param l the l
   */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.jpa.services.helper.ProgressReporter#removeProgressListener
   * (org.ihtsdo.otf.ts.jpa.services.helper.ProgressListener)
   */
  /**
   * Removes the progress listener.
   *
   * @param l the l
   */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.jpa.algo.Algorithm#cancel()
   */
  /**
   * Cancel.
   */
  @Override
  public void cancel() {
    throw new UnsupportedOperationException("cannot cancel.");
  }

  /**
   * Check for utf8 bom.
   * 
   * @param inputStream the input stream
   * @return the input stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static InputStream checkForUtf8BOM(InputStream inputStream)
    throws IOException {
    PushbackInputStream pushbackInputStream =
        new PushbackInputStream(new BufferedInputStream(inputStream), 3);
    byte[] bom = new byte[3];
    if (pushbackInputStream.read(bom) != -1) {
      if (!(bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF)) {
        pushbackInputStream.unread(bom);
      }
    }
    return pushbackInputStream;
  }

  /**
   * The SAX parser handler.
   * 
   * @author ${author}
   */
  class LocalHandler extends DefaultHandler {

    /** The chars. */
    StringBuilder chars = new StringBuilder();

    /** The label chars - used for description text. */
    StringBuilder labelChars = new StringBuilder();

    /** The rubric kind. */
    String rubricKind = null;

    /** The rubric id. */
    String rubricId = null;

    /** The code. */
    String code = null;

    /** The parent code. */
    String parentCode = null;

    /** The modifier code. */
    String modifierCode = null;

    /** The modifier. */
    String modifier = null;

    /** The class usage. */
    String classUsage = null;

    /** The reference usage. */
    String referenceUsage = null;

    /** The reference code. */
    String referenceCode = null;

    /** The ref set member counter. */
    int refSetMemberCounter = 1;

    /** The reference indicating a non-isa relationship. */
    String reference = null;

    /** The current sub classes. */
    Set<String> currentSubClasses = new HashSet<>();
    
    /** The additional relationship types. */
    Set<String> additionalRelationshipTypes = new HashSet<>();
    
    /** The term types. */
    Set<String> termTypes = new HashSet<>();

    /**
     * This is a code => modifier map. The modifier must then be looked up in
     * modifier map to determine the code extensions and template concepts
     * associated with it.
     */
    Map<String, List<String>> classToModifierMap = new HashMap<>();

    /**
     * This is a code => modifier map. If a code is modified but also blocked by
     * an entry in here, do not make children from the template classes.
     */
    Map<String, List<String>> classToExcludedModifierMap = new HashMap<>();

    /**
     * The rels map for holding data for relationships that will be built after
     * all concepts are created.
     */
    Map<String, Set<Concept>> relsMap = new HashMap<>();

    /** Indicates rels are needed as a result of the SuperClass tag. */
    boolean isaRelNeeded = false;

    /**
     * The concept that is currently being built from the contents of a Class
     * tag.
     */
    Concept concept = new ConceptJpa();

    /** The rel id counter. */
    int relIdCounter = 100;

    /** The modifier map. */
    Map<String, Map<String, Concept>> modifierMap = new HashMap<>();

    /**
     * Tag stack.
     */
    Stack<String> tagStack = new Stack<>();

    /**
     * Instantiates a new local handler.
     */
    public LocalHandler() {
      super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     * java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {

      // add current tag to stack
      tagStack.push(qName.toLowerCase());

      if (qName.equalsIgnoreCase("meta")) {
        // e.g. <Meta name="TopLevelSort"
        // value="- A B D F H K L N P R S T U W X Y Z"/>
        String name = attributes.getValue("name");
        if (name != null && name.equalsIgnoreCase("toplevelsort")) {
          String value = attributes.getValue("value");
          roots = new ArrayList<>();
          for (String code : value.split(" ")) {
            Logger.getLogger(getClass()).info("  Adding root: " + code.trim());
            roots.add(code.trim());
          }
        }
        if (roots.size() == 0)
          throw new IllegalStateException("No roots found");
      }
      // Encountered Class tag, save code and class usage
      if (qName.equalsIgnoreCase("class")) {
        code = attributes.getValue("code");
        classUsage = attributes.getValue("usage");
        Logger.getLogger(getClass()).info(
            "  Encountered class " + code + " "
                + (classUsage == null ? "" : "(" + classUsage + ")"));
      }

      // Encountered Modifier tag, save code and class usage
      if (qName.equalsIgnoreCase("modifier")) {
        code = attributes.getValue("code");
        classUsage = attributes.getValue("usage");
        Logger.getLogger(getClass()).info(
            "  Encountered modifier " + code + " "
                + (classUsage == null ? "" : "(" + classUsage + ")"));
      }

      // Encountered ModifierClass, save modifier, modifierCode and class usage
      if (qName.equalsIgnoreCase("modifierclass")) {
        modifier = attributes.getValue("modifier");
        modifierCode = attributes.getValue("code");

        //
        // CLAML FIXER - ICD10 is broken, fix it here
        //
        if (modifier.endsWith("_4") && !modifierCode.startsWith(".")) {
          Logger.getLogger(getClass()).info("  FIXING broken code, adding . to _4 code");
          modifierCode = "." + modifierCode;
        }
        if (modifier.endsWith("_5") && modifierCode.startsWith(".")) {
          Logger.getLogger(getClass()).info("  FIXING broken code, removing . from _5 code");
          modifierCode = modifierCode.substring(1);
        }
        classUsage = attributes.getValue("usage");
        Logger.getLogger(getClass()).info(
            "  Encountered modifierClass " + modifierCode + " for " + modifier
                + " " + (classUsage == null ? "" : "(" + classUsage + ")"));
      }

      // Encountered Superclass, add parent information
      // ASSUMPTION (tested): single inheritance
      if (qName.equalsIgnoreCase("superclass")) {
        if (parentCode != null)
          throw new IllegalStateException("Multiple SuperClass entries for "
              + code + " = " + parentCode + ", " + attributes.getValue("code"));
        parentCode = attributes.getValue("code");
        isaRelNeeded = true;
        Logger.getLogger(getClass()).info(
            "  Class "
                + (code != null ? code : (modifier + ":" + modifierCode))
                + " has parent " + parentCode);
        parentCodeHasChildrenMap.put(parentCode, true);
      }

      // Encountered "Subclass", save child information
      if (qName.equalsIgnoreCase("subclass")) {
        String childCode = attributes.getValue("code");
        currentSubClasses.add(childCode);
        Logger.getLogger(getClass()).info(
            "  Class "
                + (code != null ? code : (modifier + ":" + modifierCode))
                + " has child " + childCode);
        parentCodeHasChildrenMap.put(code, true);
      }

      // Encountered ModifiedBy, save modifier code information
      if (qName.equalsIgnoreCase("modifiedby")) {
        String modifiedByCode = attributes.getValue("code");
        Logger.getLogger(getClass()).info("  Class " + code + " modified by " + modifiedByCode);
        List<String> currentModifiers = new ArrayList<>();
        if (classToModifierMap.containsKey(code)) {
          currentModifiers = classToModifierMap.get(code);
        }
        currentModifiers.add(modifiedByCode);
        classToModifierMap.put(code, currentModifiers);
      }

      // Encountered ExcludeModifier, save excluded modifier code information
      if (qName.equalsIgnoreCase("excludemodifier")) {
        String excludeModifierCode = attributes.getValue("code");
        Logger.getLogger(getClass()).info(
            "  Class and subclasses of " + code + " exclude modifier "
                + excludeModifierCode);
        List<String> currentModifiers = new ArrayList<>();
        if (classToExcludedModifierMap.containsKey(code)) {
          currentModifiers = classToExcludedModifierMap.get(code);
        }
        currentModifiers.add(excludeModifierCode);
        classToExcludedModifierMap.put(code, currentModifiers);

        // If the code contains a dash (-) we need to generate
        // all of the codes in the range
        if (code.indexOf("-") != -1) {
          String[] startEnd = code.split("-");
          char letterStart = startEnd[0].charAt(0);
          char letterEnd = startEnd[1].charAt(0);
          int start = Integer.parseInt(startEnd[0].substring(1));
          int end = Integer.parseInt(startEnd[1].substring(1));
          for (char c = letterStart; c <= letterEnd; c++) {
            for (int i = start; i <= end; i++) {
              String padI = "0000000000" + i;
              String code =
                  c
                      + padI.substring(
                          padI.length() - startEnd[0].length() + 1,
                          padI.length());
              Logger.getLogger(getClass()).info(
                  "  Class and subclasses of " + code + " exclude modifier "
                      + excludeModifierCode);
              currentModifiers = new ArrayList<>();
              if (classToExcludedModifierMap.containsKey(code)) {
                currentModifiers = classToExcludedModifierMap.get(code);
              }
              currentModifiers.add(excludeModifierCode);
              classToExcludedModifierMap.put(code, currentModifiers);
            }
          }
        }

      }

      // Encountered Rubric, save kind (for description type) and the id
      if (qName.equalsIgnoreCase("rubric")) {
        rubricKind = attributes.getValue("kind");
        rubricId = attributes.getValue("id");
        Logger.getLogger(getClass()).info(
            "  Class " + code + " has rubric " + rubricKind + ", " + rubricId);
      }

      // Encountered Reference, append label chars and save usage
      if (qName.equalsIgnoreCase("reference")) {

        // add label chars if within a label tag
        if (tagStack.contains("label")) {
          // Append a space if we've already seen earlier fragments
          if (labelChars.length() != 0 && chars.toString().trim().length() > 0) {
            labelChars.append(" ");
          }
          labelChars.append(chars.toString().trim());
        } else {
          throw new SAXException(
              "Unexpected place to find reference -- not in label tag");
        }
        // Clear "characters"
        chars = new StringBuilder();

        // Save reference usage
        referenceUsage = attributes.getValue("usage");
        // the referenceCode is used when the value in the Reference tag
        // doesn't actually resolve to a code. We need this because it is
        // what we will ACTUALLY connect the relationship to
        referenceCode = attributes.getValue("code");
      }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
      throws SAXException {
      try {

        // Encountered </Fragment>, append label characters
        if (qName.equalsIgnoreCase("fragment")) {
          // Append a space if we've already seen earlier fragments
          if (labelChars.length() != 0) {
            labelChars.append(" ");
          }
          labelChars.append(chars.toString().trim());
        }

        // Encountered </Para>, append label characters
        if (qName.equalsIgnoreCase("para")) {
          // Append a space if we've already seen earlier fragments
          if (labelChars.length() != 0 && chars.toString().trim().length() > 0) {
            labelChars.append(" ");
          }
          labelChars.append(chars.toString().trim());
        }

        // Encountered </Label> while in a Modifier
        // adding concept/preferred description for modifier class
        // NOTE: non-preferred descriptions still need to be added
        // to modified classes
        if (qName.equalsIgnoreCase("label") && modifierCode != null) {
          // Append a space if we've already seen earlier fragments
          if (labelChars.length() != 0 && chars.toString().trim().length() > 0) {
            labelChars.append(" ");
          }
          // Pick up any characters in the label tag
          labelChars.append(chars.toString().trim());
          addModifierClass();
          // reset label characters
          labelChars = new StringBuilder();
        }

        // Encountered </Label> while in a Class, add concept/description
        if (qName.equalsIgnoreCase("label") && tagStack.contains("class")) {

          // Append a space if we've already seen earlier fragments
          if (labelChars.length() != 0 && chars.toString().trim().length() > 0) {
            labelChars.append(" ");
          }
          // Pick up any characters in the label tag
          labelChars.append(chars.toString().trim());

          // For the first label in the code, create the concept
          if (!conceptMap.containsKey(code)) {
            concept.setTerminologyId(code);
            
            concept.setTerminology(terminology);
            concept.setVersion(version);
            concept.setName(labelChars.toString());
            concept.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version)); 
            concept.setTimestamp(ConfigUtility.DATE_FORMAT3.parse(version));
            concept.setLastModifiedBy("loader");
            concept.setObsolete(false);
            concept.setSuppressible(false);
            concept.setPublishable(true);
            concept.setPublished(true);
            concept.setFullyDefined(false);
            concept.setAnonymous(false);
            concept.setUsesRelationshipIntersection(true);
            concept.setUsesRelationshipUnion(false);
            Logger.getLogger(getClass()).debug(
                "  Add concept " + concept.getTerminologyId() + " "
                    + concept.getName());
            // Persist now, but commit at the end after all descriptions are
            // added
            addConcept(concept);
            conceptMap.put(code, concept);
          }

          // Add atom to concept for this rubric
          Atom atom = new AtomJpa();
          atom.setTerminologyId(rubricId);
          atom.setTerminology(terminology);
          atom.setVersion(version);
          atom.setName(labelChars.toString());
          atom.setCodeId(concept.getTerminologyId());
          atom.setConceptId(concept.getTerminologyId());
          atom.setDescriptorId("");
          atom.setLexicalClassId("");
          atom.setStringClassId("");
          atom.setLanguage(terminologyLanguage);
          atom.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version)); 
          atom.setTimestamp(ConfigUtility.DATE_FORMAT3.parse(version));
          atom.setLastModifiedBy("loader");
          atom.setObsolete(false);
          atom.setSuppressible(false);
          atom.setPublishable(true);
          atom.setPublished(true);
          atom.setTermType(rubricKind);
          termTypes.add(rubricKind);
          atom.setWorkflowStatus("PUBLISHED");

          Logger.getLogger(getClass()).info(
              "  Add Atom for class " + code + " - " + rubricKind
                  + " - "
                  + (atom.getName().replaceAll("\r", "").replaceAll("\n", "")));

          addAtom(atom);
          concept.addAtom(atom);

          // reset label characters
          labelChars = new StringBuilder();
        }

        // Encountered </Label> while in a Class, add concept/description
        if (qName.equalsIgnoreCase("label") && tagStack.contains("modifier")) {
          // reset label characters
          labelChars = new StringBuilder();
        }

        // Encountered </Reference>, create info for later relationship creation
        if (qName.equalsIgnoreCase("reference")) {
          // relationships for this concept will be added at endDocument(),
          // save relevant data now in relsMap
          reference = chars.toString();
          Logger.getLogger(getClass()).info(
              "  Class " + code + " has reference to " + reference + " "
                  + (referenceUsage == null ? "" : "(" + referenceUsage + ")"));

          // If not "dagger" or "aster", it's just a normal reference
          if (referenceUsage == null) {
            referenceUsage = "reference";
          }

          // IF the reference tag didn't have a code attribute, use the text
          // instead
          if (referenceCode == null) {
            referenceCode = reference;
          }
          String key =
              referenceCode + ":" + rubricId + ":" + referenceUsage + ":"
                  + reference;
          // check assumption: key is unique
          if (relsMap.containsKey(key)) {
            throw new Exception("Rels key already exists: " + key);
          }
          // because of checking assumption, will never have >1 in the set
          Set<Concept> concepts = new HashSet<>();
          concepts.add(concept);
          relsMap.put(key, concepts);
        }

        // Encountered </ModifierClass>
        // Add the template concept to the map for this
        // ModifierClass's code (e.g. ".1" => template concept)
        // Add that to the overall map for the corresponding modifier
        if (qName.equalsIgnoreCase("modifierclass")) {
          Map<String, Concept> modifierCodeToClassMap = new HashMap<>();
          if (modifierMap.containsKey(modifier)) {
            modifierCodeToClassMap = modifierMap.get(modifier);
          }
          modifierCodeToClassMap.put(modifierCode, concept);
          modifierMap.put(modifier, modifierCodeToClassMap);
          Logger.getLogger(getClass()).info(
              "  Modifier " + modifier + " needs template class for "
                  + modifierCode);
        }

        // Encountered </Class> or </Modifier> or </ModifierClass>
        // Save info for parents/children
        if (qName.equalsIgnoreCase("class")
            || qName.equalsIgnoreCase("modifier")
            || qName.equalsIgnoreCase("modifierclass")) {

          // if relationships for this concept will be added at endDocument(),
          // save relevant data now in relsMap
          if (isaRelNeeded && concept.getTerminologyId() != null) {
            Logger.getLogger(getClass()).info("  Class " + code + " has parent " + parentCode);
            Set<Concept> children = new HashSet<>();
            // check if this parentCode already has children
            if (relsMap.containsKey(parentCode + ":" + "isa")) {
              children = relsMap.get(parentCode + ":" + "isa");
            }
            children.add(concept);
            relsMap.put(parentCode + ":" + "isa", children);
            for (Concept child : children) {
              childToParentCodeMap.put(child.getTerminologyId(), parentCode);
            }
            parentCodeHasChildrenMap.put(parentCode, true);

          }

          // If concept indicates modifiedby tag, add related children
          // also check subClassToModifierMap to see if
          // modifiers need to be created for this concept
          if (qName.equalsIgnoreCase("class") && code.indexOf("-") == -1) {
            modifierHelper(code);
          }

          // Record class level dagger/asterisk info as refset member
          if (classUsage != null) {
            // TODO:
            // create and populate attribute object with name = "USAGE"
            // add attribute to the concept
            // addAttribute()
            // make metadata object for attributeName
            Attribute att = new AttributeJpa();
            att.setName("USAGE");
            // TODO: value whatever class usage is set to
            att.setValue("USAGE");
            att.setTimestamp(ConfigUtility.DATE_FORMAT3.parse(version));
            att.setTerminologyId("");
            att.setTerminology(terminology);
            att.setVersion(version);
            att.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version));
            att.setLastModifiedBy("loader");
            att.setObsolete(false);
            att.setPublishable(true);
            att.setPublished(true);
            att.setSuppressible(false);
            
            concept.addAttribute(att);
            addAttribute(att, concept);
            
          }

          // reset variables at the end of each
          // Class, Modifier, or ModifierClass
          code = null;
          parentCode = null;
          modifierCode = null;
          modifier = null;
          rubricKind = null;
          rubricId = null;
          concept = new ConceptJpa();
          currentSubClasses = new HashSet<>();
          classUsage = null;
          referenceUsage = null;
          isaRelNeeded = false;
        }

      } catch (Exception e) {
        throw new SAXException(e);
      }

      // pop tag stack and clear characters
      tagStack.pop();
      chars = new StringBuilder();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char ch[], int start, int length) {
      chars.append(new String(ch, start, length));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
      // Add relationships now that all concepts have been created
      Map<String, Integer> relDisambiguation = new HashMap<>();

      try {
        for (Map.Entry<String, Set<Concept>> mapEntry : relsMap.entrySet()) {

          String key = mapEntry.getKey();
          String tokens[] = key.split(":");
          String parentCode = null;
          String id = null;
          String type = null;
          String label = null;

          // handle reference case
          if (tokens.length == 4) {
            parentCode = tokens[0];
            type = tokens[2];
            id = tokens[1];
            label = tokens[3];
            if (relDisambiguation.containsKey(id)) {
              int ct = relDisambiguation.get(id);
              ct++;
              relDisambiguation.put(id, ct);
              id = id + "~" + ct;
            } else {
              relDisambiguation.put(id, 1);
              id = id + "~1";
            }
            // tokens[3]; -- nothing to do with tokens[3] at this point
          }

          // handle isa case
          else if (tokens.length == 2) {
            parentCode = tokens[0];
            type = tokens[1];
          }

          // fail otherwise
          else {
            throw new SAXException(
                "Unexpected number of tokens for relsMap entry "
                    + tokens.length);
          }
          if (type.equals("aster"))
            type = "dagger-to-asterisk";
          if (type.equals("dagger"))
            type = "asterisk-to-dagger";
          for (Concept childConcept : mapEntry.getValue()) {
            Logger.getLogger(getClass()).info(
                "  Create Relationship " + childConcept.getTerminologyId()
                    + " " + type + " " + parentCode + " " + id);
            if (conceptMap.containsKey(parentCode)) {
              ConceptRelationship relationship = new ConceptRelationshipJpa();
              // For reference, use the provided id
              if (id != null) {
                relationship.setTerminologyId(id);
              }
              // otherwise, make a new id
              else {
                relationship.setTerminologyId(new Integer(relIdCounter++)
                    .toString());
              }
              
              relationship.setTerminology(terminology);
              relationship.setVersion(version);
              
              relationship.setTo(conceptMap.get(parentCode));
              relationship.setFrom(childConcept);
              relationship.setRelationshipType(type.toLowerCase().equals("isa") ? "CHD" : "RO");
              relationship.setAdditionalRelationshipType(type);
              additionalRelationshipTypes.add(type); 
              relationship.setGroup(null);
              relationship.setAssertedDirection(true);
              relationship.setInferred(true);
              relationship.setStated(true);
              relationship.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version)); 
              relationship.setTimestamp(ConfigUtility.DATE_FORMAT3.parse(version));
              relationship.setLastModifiedBy("loader");
              relationship.setObsolete(false);
              relationship.setSuppressible(false);
              relationship.setPublishable(true);
              relationship.setPublished(true);
              addRelationship(relationship);
              List<ConceptRelationship> rels = new ArrayList<>();
              
              rels.add(relationship);
              childConcept.setRelationships(rels);

            } else if (modifierMap.containsKey(parentCode)) {
              Logger.getLogger(getClass()).info("    IGNORE rel to modifier");
            } else {
              // throw new SAXException("Problem inserting relationship, code "
              // + parentCode + " does not exist.");
              Logger.getLogger(getClass()).info("    WARNING rel to illegal concept");
            }
          }
        }
        
        // create metadata from sets
        MetadataService metadataService = new MetadataServiceJpa();

        metadataService.setTransactionPerOperation(false);
        metadataService.beginTransaction();

        // relationship types - CHD, PAR, and RO
        String[] relTypes = new String[] {
            "RO", "CHD", "PAR"
        };
        RelationshipType chd = null;
        RelationshipType par = null;
        RelationshipType ro = null;
        for (String rel : relTypes) {
          RelationshipType type = new RelationshipTypeJpa();
          type.setTerminology(terminology);
          type.setVersion(version);
          type.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version));
          type.setLastModifiedBy("loader");
          type.setPublishable(true);
          type.setPublished(true);
          type.setAbbreviation(rel);
          if (rel.equals("CHD")) {
            chd = type;
            type.setExpandedForm("Child of");
          } else if (rel.equals("PAR")) {
            par = type;
            type.setExpandedForm("Parent of");
          } else if (rel.equals("RO")) {
            ro = type;
            type.setExpandedForm("Other");
          } else {
            throw new Exception("Unhandled type");
          }
          metadataService.addRelationshipType(type);
        }
        chd.setInverse(par);
        par.setInverse(chd);
        ro.setInverse(ro);
        metadataService.updateRelationshipType(chd);
        metadataService.updateRelationshipType(par);
        metadataService.updateRelationshipType(ro);

        for (String art : additionalRelationshipTypes) {

            AdditionalRelationshipType relType =
                new AdditionalRelationshipTypeJpa();
            relType.setAbbreviation(art);
            relType.setExpandedForm(art);
            relType.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version));
            relType.setLastModifiedBy("loader");
            relType.setPublishable(true);
            relType.setPublished(true);
            relType.setTerminology(terminology);
            relType.setTimestamp(ConfigUtility.DATE_FORMAT3.parse(version));
            relType.setVersion(version);

            metadataService.addAdditionalRelationshipType(relType);
       

        }
        metadataService.commit();
        metadataService.clear();
        metadataService.beginTransaction();


        for (String tty : termTypes) {

            TermType termType = new TermTypeJpa();
            termType.setAbbreviation(tty);
            termType.setCodeVariantType(CodeVariantType.UNDEFINED);
            termType.setExpandedForm(tty);
            termType.setHierarchicalType(false);
            termType.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version));
            termType.setLastModifiedBy("loader");
            termType.setNameVariantType(NameVariantType.UNDEFINED);
            termType.setObsolete(false);
            termType.setPublishable(true);
            termType.setPublished(true);
            termType.setStyle(TermTypeStyle.STRUCTURAL);
            termType.setSuppressible(false);
            termType.setTerminology(terminology);
            termType.setTimestamp(ConfigUtility.DATE_FORMAT3.parse(version));
            termType.setUsageType(UsageType.UNDEFINED);
            termType.setVersion(version);

            metadataService.addTermType(termType);
          
        }
        metadataService.commit();
        metadataService.clear();
        metadataService.beginTransaction();


          Language language = new LanguageJpa();
          language.setAbbreviation(terminologyLanguage);
          language.setExpandedForm(terminologyLanguage);
          language.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version));
          language.setLastModifiedBy("loader");
          language.setPublishable(true);
          language.setPublished(true);
          language.setTerminology(terminology);
          language.setTimestamp(ConfigUtility.DATE_FORMAT3.parse(version));
          language.setISO3Code("???");
          language.setISOCode(terminologyLanguage);
          language.setVersion(version);
          metadataService.addLanguage(language);
        
          AttributeName name = new AttributeNameJpa();
          name.setTerminology(terminology);
          name.setVersion(version);
          name.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version));
          name.setLastModifiedBy("loader");
          name.setPublishable(true);
          name.setPublished(true);
          name.setExpandedForm("USAGE");
          name.setAbbreviation("USAGE");
          metadataService.addAttributeName(name);

        metadataService.commit();
        metadataService.clear();
        metadataService.beginTransaction();
        
        PrecedenceList list = new PrecedenceListJpa();
        list.setDefaultList(true);

        List<KeyValuePair> lkvp = new ArrayList<>();

        KeyValuePair pr = new KeyValuePair();
        pr.setKey(terminology);
        pr.setValue("preferred");
        lkvp.add(pr);
        for (String tty : termTypes) {
          if (!tty.equals("preferred")) {
            KeyValuePair pair = new KeyValuePair();
            pair.setKey(terminology);
            pair.setValue(tty);
            lkvp.add(pair);
          }
        }
        
        KeyValuePairList kvpl = new KeyValuePairList();
        kvpl.setKeyValuePairList(lkvp);
        list.setPrecedence(kvpl);
        list.setTimestamp(ConfigUtility.DATE_FORMAT3.parse(version));
        list.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version));
        list.setLastModifiedBy("loader");
        list.setName("DEFAULT");  
        metadataService.addPrecedenceList(list);
        metadataService.commit();
        
        
        
        metadataService.close();
      } catch (Exception e) {
        throw new SAXException(e);
      }
      
    }

    /**
     * Adds the modifier class.
     * 
     * @throws Exception the exception
     */
    public void addModifierClass() throws Exception {

      // create concept if it doesn't exist
      String code = modifier + modifierCode;
      if (!conceptMap.containsKey(code)) {
        concept.setTerminologyId(modifier + modifierCode);
        /*concept.setEffectiveTime(dateFormat.parse(effectiveTime));
        concept.setActive(true);
        concept.setModuleId(new Long(conceptMap.get("defaultModule")
            .getTerminologyId()));
        concept.setDefinitionStatusId(new Long(conceptMap.get(
            "defaultDefinitionStatus").getTerminologyId()));*/
        concept.setTerminology(terminology);
        concept.setVersion(version);
        concept.setName(labelChars.toString());
        Logger.getLogger(getClass()).info(
            "  Add modifier concept " + concept.getTerminologyId() + " "
                + concept.getName());
        // NOTE: we don't persist these modifier classes, the
        // classes they generate get added during modifierHelper
        conceptMap.put(code, concept);
      }

      // add description to concept
      /*Description desc = new DescriptionJpa();
      desc.setTerminologyId(rubricId);
      desc.setEffectiveTime(dateFormat.parse(effectiveTime));
      desc.setActive(true);
      desc.setModuleId(new Long(conceptMap.get("defaultModule")
          .getTerminologyId()));
      desc.setTerminology(terminology);
      desc.setTerminologyVersion(terminologyVersion);
      desc.setTerm(chars.toString());
      desc.setConcept(concept);
      desc.setCaseSignificanceId(new Long(conceptMap.get(
          "defaultCaseSignificance").getTerminologyId()));
      desc.setLanguageCode("en");
      desc.setTypeId(new Long(conceptMap.get(rubricKind).getTerminologyId()));

      concept.addDescription(desc);*/
    }

    /**
     * Handle generating new concepts based on modifiers.
     *
     * @param codeToModify the code to modify
     * @throws Exception the exception
     */
    public void modifierHelper(String codeToModify) throws Exception {

      // Determine if "code" or any of its ancestor codes have modifiers
      // that are not blocked by excluded modifiers
      String cmpCode = codeToModify;
      Map<String, String> modifiersToMatchedCodeMap = new HashMap<>();
      Map<String, String> excludedModifiersToMatchedCodeMap = new HashMap<>();
      while (cmpCode.length() > 2) {
        Logger.getLogger(getClass()).info("    Determine if " + cmpCode + " has modifiers");

        // If a matching modifier is found for this or any ancestor code
        // add it
        if (classToModifierMap.containsKey(cmpCode)) {
          // Find and save all modifiers at this level
          for (String modifier : classToModifierMap.get(cmpCode)) {
            modifiersToMatchedCodeMap.put(modifier, codeToModify);
            Logger.getLogger(getClass()).info("      Use modifier " + modifier + " for " + cmpCode);
            // If this modifier has been explicitly excluded at a lower level
            // then remove it. Note: if there's an excluded modifier higher up
            // it doesn't apply here because this modifier explicitly overrides
            // that exclusion
            if (excludedModifiersToMatchedCodeMap.containsKey(modifier)
                && isDescendantCode(
                    excludedModifiersToMatchedCodeMap.get(modifier), cmpCode)) {
              Logger.getLogger(getClass()).info(
                  "      Exclude modifier " + modifier + " for "
                      + modifiersToMatchedCodeMap.get(modifier) + " due to "
                      + excludedModifiersToMatchedCodeMap.get(modifier));
              if (!overrideExclusion(codeToModify, modifier)) {
                modifiersToMatchedCodeMap.remove(modifier);
              } else {
                Logger.getLogger(getClass()).info(
                    "      Override exclude modifier " + modifier + " for "
                        + codeToModify);
              }
            }
          }
        }

        // If a matching exclusion of a modifier is found and there
        // is not an explicit modifier that is more specific, remove it
        // NOTE: this can go after the earlier section because we'll always
        // find an excluded modifier at a level lower than where it was defined
        if (classToExcludedModifierMap.containsKey(cmpCode)) {
          for (String modifier : classToExcludedModifierMap.get(cmpCode)) {
            // Check manual exclusion overrides.
            excludedModifiersToMatchedCodeMap.put(modifier, cmpCode);
          }
        }

        cmpCode =
            childToParentCodeMap.get(cmpCode);
        if (cmpCode == null) {
          break;
        }
      }

      // Determine the modifiers that apply to the current code
      Set<String> modifiersForCode = modifiersToMatchedCodeMap.keySet();
      Logger.getLogger(getClass()).info(
          "      Final modifiers to generate classes for: " + modifiersForCode);

      if (modifiersForCode.size() > 0) {

        // Loop through all modifiers identified as applying to this code
        for (String modifiedByCode : modifiersForCode) {

          // Apply 4th digit modifiers to 3 digit codes (and recursively call)
          // Apply 5th digit modifiers to 4 digit codes (which have length 5 due
          // to .)
          if (codeToModify.length() == 3 && modifiedByCode.endsWith("_4")
              || codeToModify.length() == 5 && modifiedByCode.endsWith("_5")) {

            Logger.getLogger(getClass()).info(
                "        Apply modifier " + modifiedByCode + " to "
                    + codeToModify);

            if (modifierMap.containsKey(modifiedByCode)) {
              // for each code on that modifier, create a
              // child and create a relationship
              for (Map.Entry<String, Concept> mapEntry : modifierMap.get(
                  modifiedByCode).entrySet()) {

                Concept modConcept =
                    modifierMap.get(modifiedByCode).get(mapEntry.getKey());

                // handle case where a _5 modifier is defined with a code having
                // .*
                String childCode = null;
                if (modifiedByCode.endsWith("_5")
                    && mapEntry.getKey().startsWith("."))
                  childCode =
                      conceptMap.get(codeToModify).getTerminologyId()
                          + mapEntry.getKey().substring(1);
                else
                  childCode =
                      conceptMap.get(codeToModify).getTerminologyId()
                          + mapEntry.getKey();
                createChildConcept(childCode, conceptMap.get(codeToModify),
                    modConcept, relIdCounter++);

                // Recursively call for 5th digit modifiers on generated classes
                if (codeToModify.length() == 3 && modifiedByCode.endsWith("_4")) {
                  modifierHelper(childCode);
                }
              }

            } else {
              throw new Exception("modifiedByCode not in map " + modifiedByCode);
            }

          }

          // Handle case of 3 digit code with a _5 modifier without any children
          else if (codeToModify.length() == 3
              && parentCodeHasChildrenMap.get(codeToModify) == null
              && modifiedByCode.endsWith("_5")) {

            Concept conceptToModify = conceptMap.get(codeToModify);
            Logger.getLogger(getClass()).info(
                "        Creating placeholder concept "
                    + conceptToModify.getTerminologyId() + ".X");
            Concept placeholderConcept = new ConceptJpa();
            placeholderConcept
                .setName(" - PLACEHOLDER 4th digit");

            // Recursively call for 5th digit modifiers where there are no
            // child
            // concepts and the code is only 3 digits, fill in with X
            // create intermediate layer with X
            createChildConcept(conceptToModify.getTerminologyId() + ".X",
                conceptToModify, placeholderConcept, relIdCounter++);

            modifierHelper(conceptMap.get(codeToModify).getTerminologyId()
                + ".X");
          } else {
            Logger.getLogger(getClass()).info(
                "        SKIPPING modifier " + modifiedByCode + " for "
                    + codeToModify);
          }

        }

      }
    }
    
    /**
     * Creates the child concept.
     * 
     * @param childCode the child code
     * @param parentConcept the concept
     * @param modConcept the mod concept
     * @param relId the rel id
     * @return the concept
     * @throws Exception the exception
     */
    private Concept createChildConcept(String childCode, Concept parentConcept,
      Concept modConcept, int relId) throws Exception {
      Logger.getLogger(getClass()).info(
          "        Creating concept " + childCode + " from "
              + parentConcept.getTerminologyId());
      Concept childConcept =
          createNewActiveConcept(childCode, terminology,
              version, parentConcept.getName() + " "
                  + modConcept.getName());
      if (conceptMap.containsKey(childConcept.getTerminologyId()))
        throw new IllegalStateException("ALERT2!  "
            + childConcept.getTerminologyId() + " already in map");

      conceptMap.put(childConcept.getTerminologyId(), childConcept);
      addConcept(childConcept);
      // add relationship
      createIsaRelationship(parentConcept, childConcept, ("" + relId),
          terminology, version, effectiveTime);
      childToParentCodeMap.put(childConcept.getTerminologyId(),
          parentConcept.getTerminologyId());
      parentCodeHasChildrenMap.put(parentConcept.getTerminologyId(), true);
      return childConcept;

    }
    
    /**
     * Override exclusions in certain cases.
     * 
     * @param code the code
     * @param modifier the modifier
     * @return true, if successful
     */
    private boolean overrideExclusion(String code, String modifier) {
      if (code.contains("-")) {
        return false;
      }
      String cmpCode = code.substring(0, 3);
      Logger.getLogger(getClass()).info(
          "    CHECK OVERRIDE " + code + ", " + cmpCode + ", " + modifier);

      Set<String> overrideCodes = new HashSet<>();

      // 4TH AND 5TH
      overrideCodes.add("V09");
      overrideCodes.add("V19");
      overrideCodes.add("V29");
      overrideCodes.add("V39");
      overrideCodes.add("V49");
      overrideCodes.add("V59");
      overrideCodes.add("V69");
      overrideCodes.add("V79");
      overrideCodes.add("V80");
      overrideCodes.add("V81");
      overrideCodes.add("V82");
      overrideCodes.add("V83");
      overrideCodes.add("V84");
      overrideCodes.add("V85");
      overrideCodes.add("V86");
      overrideCodes.add("V87");
      overrideCodes.add("V88");
      overrideCodes.add("V89");
      overrideCodes.add("V95");
      overrideCodes.add("V96");
      overrideCodes.add("V97");
      overrideCodes.add("W00");
      overrideCodes.add("W01");
      overrideCodes.add("W02");
      overrideCodes.add("W03");
      overrideCodes.add("W04");
      overrideCodes.add("W05");
      overrideCodes.add("W06");
      overrideCodes.add("W07");
      overrideCodes.add("W08");
      overrideCodes.add("W09");
      overrideCodes.add("W10");
      overrideCodes.add("W11");
      overrideCodes.add("W12");
      overrideCodes.add("W13");
      overrideCodes.add("W14");
      overrideCodes.add("W15");
      overrideCodes.add("W16");
      overrideCodes.add("W17");
      overrideCodes.add("W18");
      overrideCodes.add("W19");
      overrideCodes.add("W20");
      overrideCodes.add("W21");
      overrideCodes.add("W22");
      overrideCodes.add("W23");
      overrideCodes.add("W24");
      overrideCodes.add("W25");
      overrideCodes.add("W26");
      overrideCodes.add("W27");
      overrideCodes.add("W28");
      overrideCodes.add("W29");
      overrideCodes.add("W30");
      overrideCodes.add("W31");
      overrideCodes.add("W32");
      overrideCodes.add("W33");
      overrideCodes.add("W34");
      overrideCodes.add("W35");
      overrideCodes.add("W36");
      overrideCodes.add("W37");
      overrideCodes.add("W38");
      overrideCodes.add("W39");
      overrideCodes.add("W40");
      overrideCodes.add("W41");
      overrideCodes.add("W42");
      overrideCodes.add("W43");
      overrideCodes.add("W44");
      overrideCodes.add("W45");
      overrideCodes.add("W46");
      overrideCodes.add("W49");
      overrideCodes.add("W50");
      overrideCodes.add("W51");
      overrideCodes.add("W52");
      overrideCodes.add("W53");
      overrideCodes.add("W54");
      overrideCodes.add("W55");
      overrideCodes.add("W56");
      overrideCodes.add("W57");
      overrideCodes.add("W58");
      overrideCodes.add("W59");
      overrideCodes.add("W60");
      overrideCodes.add("W64");
      overrideCodes.add("W65");
      overrideCodes.add("W66");
      overrideCodes.add("W67");
      overrideCodes.add("W68");
      overrideCodes.add("W69");
      overrideCodes.add("W70");
      overrideCodes.add("W73");
      overrideCodes.add("W74");
      overrideCodes.add("W75");
      overrideCodes.add("W76");
      overrideCodes.add("W77");
      overrideCodes.add("W78");
      overrideCodes.add("W79");
      overrideCodes.add("W80");
      overrideCodes.add("W81");
      overrideCodes.add("W83");
      overrideCodes.add("W84");
      overrideCodes.add("W85");
      overrideCodes.add("W86");
      overrideCodes.add("W87");
      overrideCodes.add("W88");
      overrideCodes.add("W89");
      overrideCodes.add("W90");
      overrideCodes.add("W91");
      overrideCodes.add("W92");
      overrideCodes.add("W93");
      overrideCodes.add("W94");
      overrideCodes.add("W99");
      overrideCodes.add("X00");
      overrideCodes.add("X01");
      overrideCodes.add("X02");
      overrideCodes.add("X03");
      overrideCodes.add("X04");
      overrideCodes.add("X05");
      overrideCodes.add("X06");
      overrideCodes.add("X08");
      overrideCodes.add("X09");
      overrideCodes.add("X10");
      overrideCodes.add("X11");
      overrideCodes.add("X12");
      overrideCodes.add("X13");
      overrideCodes.add("X14");
      overrideCodes.add("X15");
      overrideCodes.add("X16");
      overrideCodes.add("X17");
      overrideCodes.add("X18");
      overrideCodes.add("X19");
      overrideCodes.add("X20");
      overrideCodes.add("X21");
      overrideCodes.add("X22");
      overrideCodes.add("X23");
      overrideCodes.add("X24");
      overrideCodes.add("X25");
      overrideCodes.add("X26");
      overrideCodes.add("X27");
      overrideCodes.add("X28");
      overrideCodes.add("X29");
      overrideCodes.add("X30");
      overrideCodes.add("X31");
      overrideCodes.add("X32");
      overrideCodes.add("X33");
      overrideCodes.add("X34");
      overrideCodes.add("X35");
      overrideCodes.add("X36");
      overrideCodes.add("X37");
      overrideCodes.add("X38");
      overrideCodes.add("X39");
      overrideCodes.add("X40");
      overrideCodes.add("X41");
      overrideCodes.add("X42");
      overrideCodes.add("X43");
      overrideCodes.add("X44");
      overrideCodes.add("X45");
      overrideCodes.add("X46");
      overrideCodes.add("X47");
      overrideCodes.add("X48");
      overrideCodes.add("X49");
      overrideCodes.add("X50");
      overrideCodes.add("X51");
      overrideCodes.add("X52");
      overrideCodes.add("X53");
      overrideCodes.add("X54");
      overrideCodes.add("X57");
      overrideCodes.add("X58");
      overrideCodes.add("X59");
      overrideCodes.add("Y06");
      overrideCodes.add("Y07");
      overrideCodes.add("Y35");
      overrideCodes.add("Y36");
      overrideCodes.add("Y40");
      overrideCodes.add("Y41");
      overrideCodes.add("Y42");
      overrideCodes.add("Y43");
      overrideCodes.add("Y44");
      overrideCodes.add("Y45");
      overrideCodes.add("Y46");
      overrideCodes.add("Y47");
      overrideCodes.add("Y48");
      overrideCodes.add("Y49");
      overrideCodes.add("Y50");
      overrideCodes.add("Y51");
      overrideCodes.add("Y52");
      overrideCodes.add("Y53");
      overrideCodes.add("Y54");
      overrideCodes.add("Y55");
      overrideCodes.add("Y56");
      overrideCodes.add("Y57");
      overrideCodes.add("Y58");
      overrideCodes.add("Y59");
      overrideCodes.add("Y63");
      overrideCodes.add("Y64");
      overrideCodes.add("Y65");
      overrideCodes.add("Y83");
      overrideCodes.add("Y84");
      overrideCodes.add("Y85");
      overrideCodes.add("Y87");
      overrideCodes.add("Y88");
      overrideCodes.add("Y89");
      overrideCodes.add("Y90");
      overrideCodes.add("Y91");

      // Override excludes for the code list above for S20W00_4
      if (overrideCodes.contains(cmpCode) && modifier.equals("S20W00_4")
          && !parentCodeHasChildrenMap.containsKey(cmpCode))
        return true;

      /**
       * Based on NIN feedback - don't have 5th digits in these cases //
       * Override excludes for the code list above for S20V01T_5 if
       * (overrideCodes.contains(cmpCode) && modifier.equals("S20V01T_5"))
       * return true;
       **/
      return false;
    }

    /**
     * Indicates whether or not descendant code is a descendant of the ancestor
     * code.
     * 
     * @param desc the descendant code
     * @param anc the candidate ancestor code
     * @return <code>true</code> if so, <code>false</code> otherwise
     */
    private boolean isDescendantCode(String desc, String anc) {
      String currentCode = desc;
      while (childToParentCodeMap
          .get(currentCode) != null) {
        String parent =
            childToParentCodeMap
                .get(currentCode);
        if (parent.equals(anc)) {
          return true;
        }
      }
      return false;
    }
    
  }
    
    /**
     * Find version.
     *
     * @param inputFile the input file
     * @throws Exception the exception
     */
    public void findVersion(String inputFile) throws Exception {
      BufferedReader br = new BufferedReader(new FileReader(inputFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        if (line.contains("<Title")) {
          int versionIndex = line.indexOf("version=");
          if (line.contains("></Title>"))
            version =
                line.substring(versionIndex + 9, line.indexOf("></Title>") - 1);
          else
            version =
                line.substring(versionIndex + 9, versionIndex + 13);
          effectiveTime = version + "0101";
          break;
        }
      }
      br.close();
      // Override terminology version with parameter
      Logger.getLogger(getClass()).info("terminologyVersion: " + version);
      Logger.getLogger(getClass()).info("effectiveTime: " + effectiveTime);
    }
    
    /**
     * Find language.
     *
     * @param inputFile the input file
     * @throws Exception the exception
     */
    public void findLanguage(String inputFile) throws Exception {
      BufferedReader br = new BufferedReader(new FileReader(inputFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        // <Meta name="lang" value="en"/>
        if (line.contains("<Meta") && line.contains("lang")) {
          int versionIndex = line.indexOf("value=");
          terminologyLanguage =
                line.substring(versionIndex + 7, line.indexOf("/>") - 1);        
          break;
        }
      }
      br.close();
      Logger.getLogger(getClass()).info("terminologyLanguage: " + terminologyLanguage);
    }
  
    /**
     * Creates the new active concept and attached atom using default
     * metadata.
     * 
     * @param terminologyId the terminology id
     * @param terminology the terminology
     * @param terminologyVersion the terminology version
     * @param defaultPreferredName the default preferred name
     * @param effectiveTime the effective time
     * @return the concept
     * @throws Exception the exception
     */
    public Concept createNewActiveConcept(String terminologyId,
      String terminology, String terminologyVersion, String defaultPreferredName)
          throws Exception {

      Concept concept = new ConceptJpa();
      concept.setTerminologyId(terminologyId);
      concept.setTerminology(terminology);
      concept.setVersion(terminologyVersion);
      concept.setName(defaultPreferredName);
       
      concept.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version)); 
      concept.setTimestamp(ConfigUtility.DATE_FORMAT3.parse(version));
      concept.setLastModifiedBy("loader");
      concept.setObsolete(false);
      concept.setSuppressible(false);
      concept.setPublishable(true);
      concept.setPublished(true);
      concept.setFullyDefined(false);
      concept.setAnonymous(false);
      concept.setUsesRelationshipIntersection(true);
      concept.setUsesRelationshipUnion(false);

      // Create a preferred name description
      /*Description desc = new DescriptionJpa();
      desc.setTerminologyId(terminologyId);
      desc.setEffectiveTime(dt.parse(effectiveTime));
      desc.setActive(true);
      // default module
      if (conceptMap.containsKey("defaultModule"))
        desc.setModuleId(new Long(conceptMap.get("defaultModule")
            .getTerminologyId()));
      desc.setTerminology(terminology);
      desc.setTerminologyVersion(terminologyVersion);
      desc.setTerm(defaultPreferredName);
      desc.setConcept(concept);
      // default case significance
      if (conceptMap.containsKey("defaultCaseSignificance"))
        desc.setCaseSignificanceId(new Long(conceptMap.get(
            "defaultCaseSignificance").getTerminologyId()));
      desc.setLanguageCode("en");
      // preferred description type
      if (conceptMap.containsKey("preferred"))
        desc.setTypeId(new Long(conceptMap.get("preferred").getTerminologyId()));

      concept.addDescription(desc);*/
      Atom atom = new AtomJpa();
      atom.setTerminologyId(terminologyId);
      atom.setTerminology(terminology);
      atom.setVersion(terminologyVersion);
      atom.setName(defaultPreferredName);
      atom.setCodeId(concept.getTerminologyId());
      atom.setConceptId(concept.getTerminologyId());
      atom.setDescriptorId("");
      atom.setLexicalClassId("");
      atom.setStringClassId("");
      atom.setLanguage(terminologyLanguage);
      atom.setLastModified(ConfigUtility.DATE_FORMAT3.parse(version)); 
      atom.setTimestamp(ConfigUtility.DATE_FORMAT3.parse(version));
      atom.setLastModifiedBy("loader");
      atom.setObsolete(false);
      atom.setSuppressible(false);
      atom.setPublishable(true);
      atom.setPublished(true);
      // is this the term type of the parent?
      //atom.setTermType(rubricKind);
      //termTypes.add(rubricKind);
      atom.setWorkflowStatus("PUBLISHED");

      addAtom(atom);
      concept.addAtom(atom);
      
      return concept;
    }

    /**
     * Creates the isa relationship with default metadata.
     * 
     * @param parentConcept the parent concept
     * @param childConcept the child concept
     * @param terminologyId the terminology id
     * @param terminology the terminology
     * @param terminologyVersion the terminology version
     * @param effectiveTime the effective time
     * @throws Exception the exception
     */
    public void createIsaRelationship(Concept parentConcept,
      Concept childConcept, String terminologyId, String terminology,
      String terminologyVersion, String effectiveTime) throws Exception {
      if (parentConcept == null) {
        throw new Exception("Parent concept may not be null");
      }
      ConceptRelationship relationship = new ConceptRelationshipJpa();
      relationship.setTerminologyId(terminologyId);
      relationship.setTerminology(terminology);
      relationship.setVersion(terminologyVersion);
      relationship.setTo(parentConcept);
      relationship.setFrom(childConcept);
      // default "isa" type
      relationship.setRelationshipType("CHD");
      relationship.setAdditionalRelationshipType("Isa");
      addRelationship(relationship);
      List<ConceptRelationship> rels = new ArrayList<>();
      rels.add(relationship);
      childConcept.setRelationships(rels);
    }
}
