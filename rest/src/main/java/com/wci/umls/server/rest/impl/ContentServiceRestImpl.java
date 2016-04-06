/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.MapSetList;
import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreeList;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.algo.ClamlLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.LabelSetMarkedParentAlgorithm;
import com.wci.umls.server.jpa.algo.LuceneReindexAlgorithm;
import com.wci.umls.server.jpa.algo.OwlLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.RemoveTerminologyAlgorithm;
import com.wci.umls.server.jpa.algo.Rf2DeltaLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.Rf2FileSorter;
import com.wci.umls.server.jpa.algo.Rf2Readers;
import com.wci.umls.server.jpa.algo.Rf2SnapshotLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.RrfFileSorter;
import com.wci.umls.server.jpa.algo.RrfLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.RrfReaders;
import com.wci.umls.server.jpa.algo.TransitiveClosureAlgorithm;
import com.wci.umls.server.jpa.algo.TreePositionAlgorithm;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.jpa.helpers.content.CodeListJpa;
import com.wci.umls.server.jpa.helpers.content.ConceptListJpa;
import com.wci.umls.server.jpa.helpers.content.DescriptorListJpa;
import com.wci.umls.server.jpa.helpers.content.MapSetListJpa;
import com.wci.umls.server.jpa.helpers.content.MappingListJpa;
import com.wci.umls.server.jpa.helpers.content.RelationshipListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetMemberListJpa;
import com.wci.umls.server.jpa.helpers.content.TreeJpa;
import com.wci.umls.server.jpa.helpers.content.TreeListJpa;
import com.wci.umls.server.jpa.helpers.content.TreePositionListJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.LogActivity;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.HistoryService;
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.SecurityService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link ContentServiceRest}..
 */
@Path("/content")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Api(value = "/content", description = "Operations to retrieve RF2 content for a terminology.")
public class ContentServiceRestImpl extends RootServiceRestImpl implements
    ContentServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link ContentServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ContentServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @POST
  @Path("/reindex")
  @ApiOperation(value = "Reindexes specified objects", notes = "Recomputes lucene indexes for the specified comma-separated objects")
  public void luceneReindex(
    @ApiParam(value = "Comma-separated list of objects to reindex, e.g. ConceptJpa (optional)", required = false) String indexedObjects,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Content): /reindex "
            + (indexedObjects == null ? "with no objects specified"
                : "with specified objects " + indexedObjects));

    // Track system level information
    long startTimeOrig = System.nanoTime();
    final LuceneReindexAlgorithm algo = new LuceneReindexAlgorithm();
    try {
      authorizeApp(securityService, authToken, "reindex",
          UserRole.ADMINISTRATOR);
      algo.setIndexedObjects(indexedObjects);
      algo.compute();
      algo.close();
      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

    } catch (Exception e) {
      handleException(e, "trying to reindex");
    } finally {
      algo.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/terminology/closure/compute/{terminology}/{version}")
  @ApiOperation(value = "Computes terminology transitive closure", notes = "Computes transitive closure for the latest version of the specified terminology")
  public void computeTransitiveClosure(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)

  throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Content): /terminology/closure/compute/"
            + terminology + "/" + version);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    final TransitiveClosureAlgorithm algo = new TransitiveClosureAlgorithm();
    try {
      authorizeApp(securityService, authToken, "compute transitive closure",
          UserRole.ADMINISTRATOR);

      // Compute transitive closure
      Logger.getLogger(getClass()).info(
          "  Compute transitive closure for  " + terminology + "/" + version);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setIdType(algo.getTerminology(terminology, version)
          .getOrganizingClassType());
      algo.reset();
      algo.compute();

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

    } catch (Exception e) {
      handleException(e, "trying to compute transitive closure");
    } finally {
      algo.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/terminology/treepos/compute/{terminology}/{version}")
  @ApiOperation(value = "Computes terminology tree positions", notes = "Computes tree positions for the latest version of the specified terminology")
  public void computeTreePositions(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)

  throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Content): /terminology/treepos/compute/"
            + terminology + "/" + version);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    final TreePositionAlgorithm algo = new TreePositionAlgorithm();
    try {
      authorizeApp(securityService, authToken, "compute tree positions ",
          UserRole.ADMINISTRATOR);

      // Compute tree positions
      Logger.getLogger(getClass()).info(
          "  Compute tree positions for " + terminology + "/" + version);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setIdType(algo.getTerminology(terminology, version)
          .getOrganizingClassType());
      algo.setCycleTolerant(true);
      // compute "semantic types" for concept hierarchies
      if (algo.getIdType() == IdType.CONCEPT) {
        algo.setComputeSemanticType(true);
      }

      algo.reset();
      algo.compute();

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

    } catch (Exception e) {
      handleException(e, "trying to compute tree positions");
    } finally {
      algo.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/terminology/load/rrf")
  @Consumes(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Load all terminologies from an RRF directory", notes = "Loads terminologies from an RRF directory for specified terminology and version")
  public void loadTerminologyRrf(
    @ApiParam(value = "Terminology, e.g. UMLS", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. latest", required = true) @QueryParam("version") String version,
    @ApiParam(value = "Single mode, e.g. false", required = true) @QueryParam("singleMode") Boolean singleMode,
    @ApiParam(value = "Code flag, e.g. false", required = true) @QueryParam("codeFlag") Boolean codeFlag,
    @ApiParam(value = "Prefix, e.g. MR or RXN", required = false) @QueryParam("prefix") String prefix,
    @ApiParam(value = "RRF input directory", required = true) String inputDir,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info(
            "RESTful POST call (Content): /terminology/load/rrf/umls/"
                + terminology + "/" + version + " from input directory "
                + inputDir);

    // Track system level information
    long startTimeOrig = System.nanoTime();
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "load RRF",
          UserRole.ADMINISTRATOR);

      // Check the input directory
      File inputDirFile = new File(inputDir);
      if (!inputDirFile.exists()) {
        throw new Exception("Specified input directory does not exist");
      }

      // Sort files - not really needed because files are already sorted
      Logger.getLogger(getClass()).info("  Sort RRF Files");
      final RrfFileSorter sorter = new RrfFileSorter();
      // Be flexible about missing files for RXNORM
      sorter
          .setRequireAllFiles(!(prefix == null ? "MR" : prefix).equals("RXN"));
      // File outputDir = new File(inputDirFile, "/RRF-sorted-temp/");
      // sorter.sortFiles(inputDirFile, outputDir);
      String releaseVersion = sorter.getFileVersion(inputDirFile);
      if (releaseVersion == null) {
        releaseVersion = version;
      }
      Logger.getLogger(getClass()).info("  releaseVersion = " + releaseVersion);

      // Open readers - just open original RRF
      final RrfReaders readers = new RrfReaders(inputDirFile);
      // Use default prefix if not specified
      readers.openOriginalReaders(prefix == null ? "MR" : prefix);

      // Load RRF
      final RrfLoaderAlgorithm algorithm = new RrfLoaderAlgorithm();
      algorithm.setTerminology(terminology);
      algorithm.setVersion(version);
      if (codeFlag == null || codeFlag) {
        algorithm.setCodesFlag(true);
      } else {
        algorithm.setCodesFlag(false);
      }
      algorithm.setSingleMode(singleMode);
      algorithm.setReleaseVersion(releaseVersion);
      algorithm.setReaders(readers);
      algorithm.compute();
      algorithm.close();

      // Compute transitive closure
      // Obtain each terminology and run transitive closure on it with the
      // correct id type
      // Refresh caches after metadata has changed in loader
      contentService.refreshCaches();
      for (final Terminology t : contentService.getTerminologyLatestVersions()
          .getObjects()) {
        // Only compute for organizing class types
        if (t.getOrganizingClassType() != null) {
          TransitiveClosureAlgorithm algo = new TransitiveClosureAlgorithm();
          algo.setTerminology(t.getTerminology());
          algo.setVersion(t.getVersion());
          algo.setIdType(t.getOrganizingClassType());
          // some terminologies may have cycles, allow these for now.
          algo.setCycleTolerant(true);
          algo.compute();
          algo.close();
        }
      }

      // Compute tree positions
      // Refresh caches after metadata has changed in loader
      for (final Terminology t : contentService.getTerminologyLatestVersions()
          .getObjects()) {
        // Only compute for organizing class types
        if (t.getOrganizingClassType() != null) {
          TreePositionAlgorithm algo = new TreePositionAlgorithm();
          algo.setTerminology(t.getTerminology());
          algo.setVersion(t.getVersion());
          algo.setIdType(t.getOrganizingClassType());
          // some terminologies may have cycles, allow these for now.
          algo.setCycleTolerant(true);
          // compute "semantic types" for concept hierarchies
          if (t.getOrganizingClassType() == IdType.CONCEPT) {
            algo.setComputeSemanticType(true);
          }
          algo.compute();
          algo.close();
        }
      }

      // Compute label sets - after transitive closure
      // for each subset, compute the label set
      for (final Terminology t : contentService.getTerminologyLatestVersions()
          .getObjects()) {
        for (final Subset subset : contentService.getConceptSubsets(
            t.getTerminology(), t.getVersion(), Branch.ROOT).getObjects()) {
          final ConceptSubset conceptSubset = (ConceptSubset) subset;
          if (conceptSubset.isLabelSubset()) {
            Logger.getLogger(getClass()).info(
                "  Create label set for subset = " + subset);
            LabelSetMarkedParentAlgorithm algo3 =
                new LabelSetMarkedParentAlgorithm();
            algo3.setSubset(conceptSubset);
            algo3.compute();
            algo3.close();
          }
        }
      }
      // Clean-up

      ConfigUtility
          .deleteDirectory(new File(inputDirFile, "/RRF-sorted-temp/"));

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

    } catch (Exception e) {
      handleException(e, "trying to load terminology from RRF directory");
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/terminology/load/rf2/delta/{terminology}")
  @Consumes(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Loads terminology RF2 delta from directory", notes = "Loads terminology RF2 delta from directory for specified terminology and version")
  public void loadTerminologyRf2Delta(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "RF2 input directory", required = true) String inputDir,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Content): /terminology/load/rf2/delta/"
            + terminology + " from input directory " + inputDir);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      authorizeApp(securityService, authToken, "start editing cycle",
          UserRole.ADMINISTRATOR);

      Logger.getLogger(getClass()).info("Starting RF2 delta loader");
      Logger.getLogger(getClass()).info("  terminology = " + terminology);
      Logger.getLogger(getClass()).info("  inputDir = " + inputDir);

      // Check the input directory
      final File inputDirFile = new File(inputDir);
      if (!inputDirFile.exists()) {
        throw new Exception("Specified input directory does not exist");
      }

      // Previous computation of version is based on file name
      // but for delta/daily build files, this is not the current version
      // look up the current version instead
      final String version = metadataService.getLatestVersion(terminology);
      metadataService.close();
      if (version == null) {
        throw new Exception("Unable to determine version.");
      }

      // Sort files
      Logger.getLogger(getClass()).info("  Sort RF2 Files");
      final Rf2FileSorter sorter = new Rf2FileSorter();
      sorter.setSortByEffectiveTime(false);
      sorter.setRequireAllFiles(false);
      File outputDir = new File(inputDirFile, "/RF2-sorted-temp/");
      sorter.sortFiles(inputDirFile, outputDir);

      // Open readers
      final Rf2Readers readers = new Rf2Readers(outputDir);
      readers.openReaders();

      // Load delta
      final Rf2DeltaLoaderAlgorithm algorithm = new Rf2DeltaLoaderAlgorithm();
      algorithm.setTerminology(terminology);
      algorithm.setVersion(version);
      algorithm.setReleaseVersion(sorter.getFileVersion());
      algorithm.setReaders(readers);
      algorithm.compute();
      algorithm.close();

      // Compute transitive closure
      Logger.getLogger(getClass()).info(
          "  Compute transitive closure from  " + terminology + "/" + version);
      final TransitiveClosureAlgorithm algo = new TransitiveClosureAlgorithm();
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setIdType(IdType.CONCEPT);
      algo.reset();
      algo.compute();

      // Clean-up
      readers.closeReaders();
      Logger.getLogger(getClass()).info("...done");

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

    } catch (Exception e) {
      handleException(e, "trying to load terminology delta from RF2 directory");
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/terminology/load/rf2/snapshot/{terminology}/{version}")
  @Consumes({
    MediaType.TEXT_PLAIN
  })
  @ApiOperation(value = "Loads terminology RF2 snapshot from directory", notes = "Loads terminology RF2 snapshot from directory for specified terminology and version")
  public void loadTerminologyRf2Snapshot(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "RF2 input directory", required = true) String inputDir,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info(
            "RESTful POST call (Content): /terminology/load/rf2/snapshot/"
                + terminology + "/" + version + " from input directory "
                + inputDir);

    // Track system level information
    final ContentService contentService = new ContentServiceJpa();

    try {
      authorizeApp(securityService, authToken, "load snapshot",
          UserRole.ADMINISTRATOR);
      
      Rf2SnapshotLoaderAlgorithm algo = new Rf2SnapshotLoaderAlgorithm();
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setInputDir(inputDir);
      algo.compute();
      

      

    } catch (Exception e) {
      handleException(e,
          "trying to load terminology snapshot from RF2 directory");
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @SuppressWarnings("resource")
  @Override
  @PUT
  @Path("/terminology/load/rf2/full/{terminology}/{version}")
  @Consumes({
    MediaType.TEXT_PLAIN
  })
  @ApiOperation(value = "Loads terminology RF2 full from directory", notes = "Loads terminology RF2 full from directory for specified terminology and version")
  public void loadTerminologyRf2Full(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "RF2 input directory", required = true) String inputDir,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info(
            "RESTful POST call (Content): /terminology/load/rf2/full/"
                + terminology + "/" + version + " from input directory "
                + inputDir);

    // Track system level information
    long startTimeOrig = System.nanoTime();
    ContentService contentService = new ContentServiceJpa();

    try {
      authorizeApp(securityService, authToken, "load full",
          UserRole.ADMINISTRATOR);

      // Check the input directory
      File inputDirFile = new File(inputDir);
      if (!inputDirFile.exists()) {
        throw new Exception("Specified input directory does not exist");
      }

      // Get the release versions (need to look in complex map too for October
      // releases)
      Logger.getLogger(getClass()).info("  Get release versions");
      Rf2FileSorter sorter = new Rf2FileSorter();
      final File conceptsFile =
          sorter.findFile(new File(inputDir, "Terminology"), "sct2_Concept");
      final Set<String> releaseSet = new HashSet<>();
      BufferedReader reader = new BufferedReader(new FileReader(conceptsFile));
      String line;
      while ((line = reader.readLine()) != null) {
        final String fields[] = FieldedStringTokenizer.split(line, "\t");
        if (!fields[1].equals("effectiveTime")) {
          try {
            ConfigUtility.DATE_FORMAT.parse(fields[1]);
          } catch (Exception e) {
            throw new Exception("Improperly formatted date found: " + fields[1]);
          }
          releaseSet.add(fields[1]);
        }
      }
      reader.close();
      final File complexMapFile =
          sorter.findFile(new File(inputDir, "Refset/Map"),
              "der2_iissscRefset_ComplexMap");
      reader = new BufferedReader(new FileReader(complexMapFile));
      while ((line = reader.readLine()) != null) {
        final String fields[] = FieldedStringTokenizer.split(line, "\t");
        if (!fields[1].equals("effectiveTime")) {
          try {
            ConfigUtility.DATE_FORMAT.parse(fields[1]);
          } catch (Exception e) {
            throw new Exception("Improperly formatted date found: " + fields[1]);
          }
          releaseSet.add(fields[1]);
        }
      }
      File extendedMapFile =
          sorter.findFile(new File(inputDir, "Refset/Map"),
              "der2_iisssccRefset_ExtendedMap");
      reader = new BufferedReader(new FileReader(extendedMapFile));
      while ((line = reader.readLine()) != null) {
        final String fields[] = FieldedStringTokenizer.split(line, "\t");
        if (!fields[1].equals("effectiveTime")) {
          try {
            ConfigUtility.DATE_FORMAT.parse(fields[1]);
          } catch (Exception e) {
            throw new Exception("Improperly formatted date found: " + fields[1]);
          }
          releaseSet.add(fields[1]);
        }
      }

      reader.close();
      final List<String> releases = new ArrayList<>(releaseSet);
      Collections.sort(releases);

      // check that release info does not already exist
      final HistoryService historyService = new HistoryServiceJpa();
      Logger.getLogger(getClass()).info("  Releases to process");
      for (final String release : releases) {
        Logger.getLogger(getClass()).info("    release = " + release);
        ReleaseInfo releaseInfo =
            historyService.getReleaseInfo(terminology, release);
        if (releaseInfo != null) {
          throw new Exception("A release info already exists for " + release);
        }
      }
      historyService.close();

      // Sort files
      Logger.getLogger(getClass()).info("  Sort RF2 Files");
      sorter = new Rf2FileSorter();
      sorter.setSortByEffectiveTime(true);
      sorter.setRequireAllFiles(true);
      File outputDir = new File(inputDirFile, "/RF2-sorted-temp/");
      sorter.sortFiles(inputDirFile, outputDir);

      // Open readers
      final Rf2Readers readers = new Rf2Readers(outputDir);
      readers.openReaders();

      // Load initial snapshot - first release version
      final Rf2SnapshotLoaderAlgorithm algorithm =
          new Rf2SnapshotLoaderAlgorithm();
      algorithm.setTerminology(terminology);
      algorithm.setVersion(version);
      algorithm.setReleaseVersion(releases.get(0));
      algorithm.setReaders(readers);
      algorithm.compute();
      algorithm.close();

      // Load deltas
      for (final String release : releases) {
        // Refresh caches for metadata handlers
        new MetadataServiceJpa().refreshCaches();

        if (release.equals(releases.get(0))) {
          continue;
        }

        Rf2DeltaLoaderAlgorithm algorithm2 = new Rf2DeltaLoaderAlgorithm();
        algorithm2.setTerminology(terminology);
        algorithm2.setVersion(version);
        algorithm2.setReleaseVersion(release);
        algorithm2.setReaders(readers);
        algorithm2.compute();
        algorithm2.close();
        algorithm2.closeFactory();
        algorithm2 = null;

      }

      // Refresh caches for metadata handlers
      new MetadataServiceJpa().refreshCaches();

      // Compute transitive closure
      Logger.getLogger(getClass()).info(
          "  Compute transitive closure from  " + terminology + "/" + version);
      final TransitiveClosureAlgorithm algo = new TransitiveClosureAlgorithm();
      algo.setCycleTolerant(false);
      algo.setIdType(IdType.CONCEPT);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.reset();
      algo.compute();
      algo.close();

      // compute tree positions
      final TreePositionAlgorithm algo2 = new TreePositionAlgorithm();
      algo2.setCycleTolerant(false);
      algo2.setComputeSemanticType(true);
      algo2.setIdType(IdType.CONCEPT);
      algo2.setTerminology(terminology);
      algo2.setVersion(version);
      algo2.reset();
      algo2.compute();
      algo2.close();

      // RE-open content service
      contentService = new ContentServiceJpa();

      // Compute label sets - after transitive closure
      // for each subset, compute the label set
      for (final Subset subset : contentService.getConceptSubsets(terminology,
          version, Branch.ROOT).getObjects()) {
        final ConceptSubset conceptSubset = (ConceptSubset) subset;
        if (conceptSubset.isLabelSubset()) {
          Logger.getLogger(getClass()).info(
              "  Create label set for subset = " + subset);
          LabelSetMarkedParentAlgorithm algo3 =
              new LabelSetMarkedParentAlgorithm();
          algo3.setSubset(conceptSubset);
          algo3.compute();
          algo3.close();
        }
      }

      // Clean-up
      readers.closeReaders();
      ConfigUtility
          .deleteDirectory(new File(inputDirFile, "/RF2-sorted-temp/"));

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

    } catch (Exception e) {
      handleException(e,
          "trying to load terminology snapshot from RF2 directory");
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/terminology/load/claml/{terminology}/{version}")
  @Consumes({
    MediaType.TEXT_PLAIN
  })
  @ApiOperation(value = "Loads ClaML terminology from file", notes = "Loads terminology from ClaML file, assigning specified version")
  public void loadTerminologyClaml(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "ClaML input file", required = true) String inputFile,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Content): /terminology/load/claml/" + terminology
            + "/" + version + " from input file " + inputFile);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    final ClamlLoaderAlgorithm algo = new ClamlLoaderAlgorithm();
    final TransitiveClosureAlgorithm algo2 = new TransitiveClosureAlgorithm();
    final TreePositionAlgorithm algo3 = new TreePositionAlgorithm();
    try {
      authorizeApp(securityService, authToken, "loading claml",
          UserRole.ADMINISTRATOR);

      // Load snapshot
      Logger.getLogger(getClass()).info("Load ClaML data from " + inputFile);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setInputFile(inputFile);
      algo.compute();

      // Let service begin its own transaction
      Logger.getLogger(getClass()).info("Start computing transtive closure");
      algo2.setIdType(IdType.CONCEPT);
      algo2.setCycleTolerant(false);
      algo2.setTerminology(terminology);
      algo2.setVersion(version);
      algo2.compute();
      algo2.close();

      // compute tree positions
      algo3.setCycleTolerant(false);
      algo3.setIdType(IdType.CONCEPT);
      algo3.setComputeSemanticType(true);
      algo3.setTerminology(terminology);
      algo3.setVersion(version);
      algo3.compute();
      algo3.close();

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

    } catch (Exception e) {
      handleException(e, "trying to load terminology from ClaML file");
    } finally {
      algo.close();
      algo2.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/terminology/load/owl/{terminology}/{version}")
  @Consumes({
    MediaType.TEXT_PLAIN
  })
  @ApiOperation(value = "Loads Owl terminology from file", notes = "Loads terminology from Owl file, assigning specified version")
  public void loadTerminologyOwl(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Owl input file", required = true) String inputFile,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Content): /terminology/load/owl/" + terminology
            + "/" + version + " from input file " + inputFile);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    final OwlLoaderAlgorithm algo = new OwlLoaderAlgorithm();
    final TransitiveClosureAlgorithm algo2 = new TransitiveClosureAlgorithm();
    final TreePositionAlgorithm algo3 = new TreePositionAlgorithm();
    try {
      authorizeApp(securityService, authToken, "loading owl",
          UserRole.ADMINISTRATOR);

      // Load snapshot
      Logger.getLogger(getClass()).info("Load Owl data from " + inputFile);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setInputFile(inputFile);
      algo.compute();

      final MetadataService service = new MetadataServiceJpa();
      service.refreshCaches();

      // Let service begin its own transaction
      Logger.getLogger(getClass()).info("Start computing transtive closure");
      algo2.setIdType(IdType.CONCEPT);
      algo2.setCycleTolerant(false);
      algo2.setTerminology(terminology);
      algo2.setVersion(version);
      algo2.compute();
      algo2.close();

      // compute tree positions
      algo3.setCycleTolerant(false);
      algo3.setIdType(IdType.CONCEPT);
      algo3.setComputeSemanticType(true);
      algo3.setTerminology(terminology);
      algo3.setVersion(version);
      algo3.compute();
      algo3.close();

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

    } catch (Exception e) {
      handleException(e, "trying to load terminology from Owl file");
    } finally {
      algo.close();
      algo2.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/terminology/remove/{terminology}/{version}")
  @ApiOperation(value = "Remove a terminology", notes = "Removes all elements for a specified terminology and version")
  public boolean removeTerminology(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful GET call (Content): /terminology/remove/" + terminology + "/"
            + version);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    final RemoveTerminologyAlgorithm algo = new RemoveTerminologyAlgorithm();
    try {
      String authUser =
          authorizeApp(securityService, authToken, "remove terminology",
              UserRole.ADMINISTRATOR);

      // Remove terminology
      Logger.getLogger(getClass()).info(
          "  Remove terminology for  " + terminology + "/" + version);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.compute();
      algo.close();

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

      securityService.addLogEntry(authUser, terminology, version,
          LogActivity.RELEASE, "Remove terminology");

      return true;

    } catch (Exception e) {
      handleException(e, "trying to remove terminology");
      return false;
    } finally {
      algo.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/cui/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get concept by id, terminology, and version", notes = "Get the root branch concept matching the specified parameters", response = ConceptJpa.class)
  public Concept getConcept(
    @ApiParam(value = "Concept terminology id, e.g. C0000039", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the concept",
          UserRole.VIEWER);

      final Concept concept =
          contentService.getConcept(terminologyId, terminology, version,
              Branch.ROOT);

      if (concept != null) {
        contentService.getGraphResolutionHandler(terminology).resolve(concept);
        concept.setAtoms(contentService.getComputePreferredNameHandler(
            concept.getTerminology()).sortByPreference(concept.getAtoms()));
      }
      return concept;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a concept");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/mapset/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get mapset by id, terminology, and version", notes = "Get the root branch mapset matching the specified parameters", response = MapSetJpa.class)
  public MapSet getMapSet(
    @ApiParam(value = "mapSet terminology id, e.g. C0000039", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "mapSet terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "mapSet terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /mapset/" + terminology + "/" + version + "/"
            + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the mapSet",
          UserRole.VIEWER);

      final MapSet mapSet =
          contentService.getMapSet(terminologyId, terminology, version,
              Branch.ROOT);

      if (mapSet != null) {
        contentService.getGraphResolutionHandler(terminology).resolve(mapSet);

      }
      return mapSet;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a mapSet");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/mapset/all/{terminology}/{version}")
  @ApiOperation(value = "Get mapsets", notes = "Get the mapsets", response = MapSetListJpa.class)
  public MapSetList getMapSets(
    @ApiParam(value = "MapSet terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "MapSet terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /mapset/all/" + terminology + "/" + version);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve mapsets",
          UserRole.VIEWER);
      final MapSetList list =
          contentService.getMapSets(terminology, version, Branch.ROOT);
      for (int i = 0; i < list.getCount(); i++) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            list.getObjects().get(i));
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to retrieve mapsets");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}")
  @ApiOperation(value = "Find concepts matching a search query", notes = "Gets a list of search results that match the lucene query for the root branch", response = SearchResultListJpa.class)
  public SearchResultList findConceptsForQuery(
    @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'aspirin'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFSC Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfscParameterJpa pfsc,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version
            + "?query=" + queryStr + " with PFS parameter "
            + (pfsc == null ? "empty" : pfsc.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find concepts by query",
          UserRole.VIEWER);
      final SearchResultList sr =
          contentService.findConceptsForQuery(terminology, version,
              Branch.ROOT, queryStr, pfsc);
      return sr;

    } catch (Exception e) {
      handleException(e, "trying to find the concepts by query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/cui")
  @ApiOperation(value = "Find concepts matching a lucene or jql search query", notes = "Gets a list of search results that match the lucene or jql query for the root branch", response = SearchResultListJpa.class)
  public SearchResultList findConceptsForGeneralQuery(
    @ApiParam(value = "Lucene Query", required = true) @QueryParam("query") String query,
    @ApiParam(value = "HQL Query", required = true) @QueryParam("jql") String jql,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;
    final String jqlStr = jql == null ? "" : jql;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui?" + "query=" + queryStr + "&jql="
            + jqlStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find concepts by query",
          UserRole.VIEWER);

      final SearchResultList sr =
          contentService.findConceptsForGeneralQuery(queryStr, jqlStr,
              Branch.ROOT, pfs);
      return sr;

    } catch (Exception e) {
      handleException(e, "trying to find the concepts by query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/code")
  @ApiOperation(value = "Find codes matching a lucene or jql search query", notes = "Gets a list of search results that match the lucene or jql query for the root branch", response = SearchResultListJpa.class)
  public SearchResultList findCodesForGeneralQuery(
    @ApiParam(value = "Lucene Query", required = true) @QueryParam("query") String query,
    @ApiParam(value = "HQL Query", required = true) @QueryParam("jql") String jql,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;
    final String jqlStr = jql == null ? "" : jql;
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code?" + "query=" + queryStr + "&jql="
            + jqlStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find codes by query",
          UserRole.VIEWER);

      final SearchResultList sr =
          contentService.findCodesForGeneralQuery(queryStr, jqlStr,
              Branch.ROOT, pfs);
      return sr;

    } catch (Exception e) {
      handleException(e, "trying to find the codes by query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/cui/{terminology}/{version}/autocomplete/{searchTerm}")
  @ApiOperation(value = "Find autocomplete matches for concept searches", notes = "Gets a list of search autocomplete matches for the specified search term", response = StringList.class)
  public StringList autocompleteConcepts(
    @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Search term, e.g. 'sul'", required = true) @PathParam("searchTerm") String searchTerm,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version
            + "/autocomplete/" + searchTerm);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find concepts by query",
          UserRole.VIEWER);

      return contentService.autocompleteConcepts(terminology, version,
          searchTerm);

    } catch (Exception e) {
      handleException(e, "trying to autocomplete for concepts");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/dui/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get descriptor by id, terminology, and version", notes = "Get the root branch descriptor matching the specified parameters", response = DescriptorJpa.class)
  public Descriptor getDescriptor(
    @ApiParam(value = "Descriptor terminology id, e.g. D003933", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version + "/"
            + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the descriptor",
          UserRole.VIEWER);

      final Descriptor descriptor =
          contentService.getDescriptor(terminologyId, terminology, version,
              Branch.ROOT);

      if (descriptor != null) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            descriptor);
        descriptor.setAtoms(contentService.getComputePreferredNameHandler(
            descriptor.getTerminology())
            .sortByPreference(descriptor.getAtoms()));

      }
      return descriptor;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a descriptor");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}")
  @ApiOperation(value = "Find descriptors matching a search query", notes = "Gets a list of search results that match the lucene query for the root branch", response = SearchResultListJpa.class)
  public SearchResultList findDescriptorsForQuery(
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'aspirin'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFSC Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfscParameterJpa pfsc,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version
            + "?query=" + queryStr + " with PFS parameter "
            + (pfsc == null ? "empty" : pfsc.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find descriptors by query",
          UserRole.VIEWER);

      final SearchResultList sr =
          contentService.findDescriptorsForQuery(terminology, version,
              Branch.ROOT, queryStr, pfsc);
      return sr;

    } catch (Exception e) {
      handleException(e, "trying to find the descriptors by query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/dui/")
  @ApiOperation(value = "Find descriptors matching a lucene or jql search query", notes = "Gets a list of search results that match the lucene or jql query for the root branch", response = SearchResultListJpa.class)
  public SearchResultList findDescriptorsForGeneralQuery(
    @ApiParam(value = "Lucene Query", required = true) @QueryParam("query") String query,
    @ApiParam(value = "HQL Query", required = true) @QueryParam("jql") String jql,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;
    final String jqlStr = jql == null ? "" : jql;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui" + "?query=" + queryStr + "&jql="
            + jqlStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find concepts by query",
          UserRole.VIEWER);

      final SearchResultList sr =
          contentService.findDescriptorsForGeneralQuery(queryStr, jqlStr,
              Branch.ROOT, pfs);
      return sr;

    } catch (Exception e) {
      handleException(e, "trying to find the concepts by query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/dui/{terminology}/{version}/autocomplete/{searchTerm}")
  @ApiOperation(value = "Find autocomplete matches for descriptor searches", notes = "Gets a list of search autocomplete matches for the specified search term", response = StringList.class)
  public StringList autocompleteDescriptors(
    @ApiParam(value = "Terminology, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Search term, e.g. 'sul'", required = true) @PathParam("searchTerm") String searchTerm,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version
            + "/autocomplete/" + searchTerm);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find descriptors by query",
          UserRole.VIEWER);

      return contentService.autocompleteDescriptors(terminology, version,
          searchTerm);

    } catch (Exception e) {
      handleException(e, "trying to autocomplete for descriptors");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/code/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get code by id, terminology, and version", notes = "Get the root branch code matching the specified parameters", response = CodeJpa.class)
  public Code getCode(
    @ApiParam(value = "Code terminology id, e.g. U002135", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Code terminology name, e.g. MTH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code version, e.g. 2014AB", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version + "/"
            + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the code",
          UserRole.VIEWER);

      final Code code =
          contentService.getCode(terminologyId, terminology, version,
              Branch.ROOT);

      if (code != null) {
        contentService.getGraphResolutionHandler(terminology).resolve(code);
        code.setAtoms(contentService.getComputePreferredNameHandler(
            code.getTerminology()).sortByPreference(code.getAtoms()));

      }
      return code;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a code");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/code/{terminology}/{version}")
  @ApiOperation(value = "Find codes matching a search query", notes = "Gets a list of search results that match the lucene query for the root branch", response = SearchResultListJpa.class)
  public SearchResultList findCodesForQuery(
    @ApiParam(value = "Code terminology name, e.g. MTH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code version, e.g. 2014AB", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'aspirin'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFSC Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfscParameterJpa pfsc,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version
            + "?query=" + queryStr + " with PFS parameter "
            + (pfsc == null ? "empty" : pfsc.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find codes by query",
          UserRole.VIEWER);

      final SearchResultList sr =
          contentService.findCodesForQuery(terminology, version, Branch.ROOT,
              queryStr, pfsc);
      return sr;

    } catch (Exception e) {
      handleException(e, "trying to find the codes by query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/code/{terminology}/{version}/autocomplete/{searchTerm}")
  @ApiOperation(value = "Find autocomplete matches for code searches", notes = "Gets a list of search autocomplete matches for the specified search term", response = StringList.class)
  public StringList autocompleteCodes(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Search term, e.g. 'sul'", required = true) @PathParam("searchTerm") String searchTerm,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version
            + "/autocomplete/" + searchTerm);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find code by query",
          UserRole.VIEWER);
      return contentService.autocompleteCodes(terminology, version, searchTerm);

    } catch (Exception e) {
      handleException(e, "trying to autocomplete for codes");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/lui/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get lexical class by id, terminology, and version", notes = "Get the root branch lexical class matching the specified parameters", response = LexicalClassJpa.class)
  public LexicalClass getLexicalClass(
    @ApiParam(value = "Lexical class terminology id, e.g. L0356926", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Lexical class terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Lexical class version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /lui/" + terminology + "/" + version + "/"
            + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the lexical class",
          UserRole.VIEWER);

      final LexicalClass lexicalClass =
          contentService.getLexicalClass(terminologyId, terminology, version,
              Branch.ROOT);

      if (lexicalClass != null) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            lexicalClass);
        lexicalClass.setAtoms(contentService.getComputePreferredNameHandler(
            lexicalClass.getTerminology()).sortByPreference(
            lexicalClass.getAtoms()));

      }
      return lexicalClass;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a lexicalClass");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/sui/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get string class by id, terminology, and version", notes = "Get the root branch string class matching the specified parameters", response = StringClassJpa.class)
  public StringClass getStringClass(
    @ApiParam(value = "String class terminology id, e.g. S0356926", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "String class terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "String class version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /sui/" + terminology + "/" + version + "/"
            + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the string class",
          UserRole.VIEWER);

      final StringClass stringClass =
          contentService.getStringClass(terminologyId, terminology, version,
              Branch.ROOT);

      if (stringClass != null) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            stringClass);
        stringClass.setAtoms(contentService.getComputePreferredNameHandler(
            stringClass.getTerminology()).sortByPreference(
            stringClass.getAtoms()));
      }
      return stringClass;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a stringClass");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/ancestors/{parentsOnly}")
  @ApiOperation(value = "Find ancestor concepts", notes = "Gets a list of ancestor concepts", response = ConceptListJpa.class)
  public ConceptList findAncestorConcepts(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("parentsOnly") boolean parentsOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/ancestors with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find ancestor concepts",
          UserRole.VIEWER);

      final ConceptList list =
          contentService.findAncestorConcepts(terminologyId, terminology,
              version, parentsOnly, Branch.ROOT, pfs);

      for (final Concept concept : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(concept);
      }

      return list;

    } catch (Exception e) {
      handleException(e, "trying to find the ancestor concepts");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/descendants/{childrenOnly}")
  @ApiOperation(value = "Find descendant concepts", notes = "Gets a list of descendant concepts", response = ConceptListJpa.class)
  public ConceptList findDescendantConcepts(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("childrenOnly") boolean childrenOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/descendants with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find descendant concepts",
          UserRole.VIEWER);

      final ConceptList list =
          contentService.findDescendantConcepts(terminologyId, terminology,
              version, childrenOnly, Branch.ROOT, pfs);

      for (final Concept concept : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(concept);
      }

      return list;

    } catch (Exception e) {
      handleException(e, "trying to find the descendant concepts");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/{terminologyId}/ancestors/{parentsOnly}")
  @ApiOperation(value = "Find ancestor descriptors", notes = "Gets a list of ancestor descriptors", response = DescriptorListJpa.class)
  public DescriptorList findAncestorDescriptors(
    @ApiParam(value = "Descriptor terminology id, e.g. D003423", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("parentsOnly") boolean parentsOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version
            + terminologyId + "/ancestors with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find ancestor descriptors",
          UserRole.VIEWER);

      final DescriptorList list =
          contentService.findAncestorDescriptors(terminologyId, terminology,
              version, parentsOnly, Branch.ROOT, pfs);

      for (final Descriptor descriptor : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            descriptor);
      }

      return list;
    } catch (Exception e) {
      handleException(e, "trying to find the ancestor descriptors");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/{terminologyId}/descendants/{childrenOnly}")
  @ApiOperation(value = "Find descendant descriptors", notes = "Gets a list of descendant descriptors", response = DescriptorListJpa.class)
  public DescriptorList findDescendantDescriptors(
    @ApiParam(value = "Descriptor terminology id, e.g. D002342", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("childrenOnly") boolean childrenOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version
            + terminologyId + "/descendants with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find descendant descriptors",
          UserRole.VIEWER);

      final DescriptorList list =
          contentService.findDescendantDescriptors(terminologyId, terminology,
              version, childrenOnly, Branch.ROOT, pfs);

      for (final Descriptor descriptor : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            descriptor);
      }

      return list;
    } catch (Exception e) {
      handleException(e, "trying to find the descendant descriptors");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/{terminologyId}/ancestors/{parentsOnly}")
  @ApiOperation(value = "Find ancestor codes", notes = "Gets a list of ancestor codes", response = CodeListJpa.class)
  public CodeList findAncestorCodes(
    @ApiParam(value = "Code terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("parentsOnly") boolean parentsOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version
            + terminologyId + "/ancestors with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find ancestor codes",
          UserRole.VIEWER);
      final CodeList list =
          contentService.findAncestorCodes(terminologyId, terminology, version,
              parentsOnly, Branch.ROOT, pfs);

      for (final Code code : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(code);
      }

      return list;
    } catch (Exception e) {
      handleException(e, "trying to find the ancestor codes");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/{terminologyId}/descendants/{childrenOnly}")
  @ApiOperation(value = "Find descendant codes", notes = "Gets a list of descendant codes", response = CodeListJpa.class)
  public CodeList findDescendantCodes(
    @ApiParam(value = "Code terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("childrenOnly") boolean childrenOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version
            + terminologyId + "/descendants with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find descendant codes",
          UserRole.VIEWER);

      final CodeList list =
          contentService.findDescendantCodes(terminologyId, terminology,
              version, childrenOnly, Branch.ROOT, pfs);

      for (final Code code : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(code);
      }

      return list;
    } catch (Exception e) {
      handleException(e, "trying to find the descendant codes");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/cui/{terminology}/{version}/{terminologyId}/members")
  @ApiOperation(value = "Get subset members with this terminologyId", notes = "Get the subset members with the given concept id", response = SubsetMemberListJpa.class)
  public SubsetMemberList getSubsetMembersForConcept(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/members");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve subset members for the concept", UserRole.VIEWER);

      final SubsetMemberList list =
          contentService.getSubsetMembersForConcept(terminologyId, terminology,
              version, Branch.ROOT);

      for (final SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member : list
          .getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(member);
      }
      return list;

    } catch (Exception e) {
      handleException(e, "trying to retrieve subset members for a concept");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/aui/{terminology}/{version}/{terminologyId}/members")
  @ApiOperation(value = "Get subset members with this terminologyId", notes = "Get the subset members with the given atom id", response = SubsetMemberListJpa.class)
  public SubsetMemberList getSubsetMembersForAtom(
    @ApiParam(value = "Atom terminology id, e.g. 102751015", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Atom terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Atom version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /aui/" + terminology + "/" + version + "/"
            + terminologyId + "/members");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve subset members for the atom", UserRole.VIEWER);

      final SubsetMemberList list =
          contentService.getSubsetMembersForAtom(terminologyId, terminology,
              version, Branch.ROOT);

      for (final SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member : list
          .getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(member);
      }
      return list;

    } catch (Exception e) {
      handleException(e, "trying to retrieve subset members for a atom");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/relationships")
  @ApiOperation(value = "Get relationships with this terminologyId", notes = "Get the relationships with the given concept id", response = RelationshipListJpa.class)
  public RelationshipList findRelationshipsForConcept(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/relationships?query=" + query);
    final String queryStr = query == null ? "" : query;

    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve relationships for the concept", UserRole.VIEWER);

      final RelationshipList list =
          contentService.findRelationshipsForConcept(terminologyId,
              terminology, version, Branch.ROOT, queryStr, false, pfs);

      // Use graph resolver
      for (final Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel : list
          .getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(rel);
      }

      // For any relationships to anonymous concepts, we need to push that up to
      // the current level and set relationship groups and inferred/stated
      final RelationshipList result = new RelationshipListJpa();
      int group = 0;
      for (final Relationship rel : list.getObjects()) {
        final ConceptRelationship rel2 = (ConceptRelationship) rel;
        if (rel2.getTo().isAnonymous()) {

          // count how many relationships there are
          int ct = 0;
          for (final ConceptRelationship innerRel : rel2.getTo()
              .getRelationships()) {
            // this is only for grouped role relationships
            if (!innerRel.isHierarchical()) {
              ct++;
            }
          }
          // if >1, then group them
          if (ct > 1) {
            group++;
          }
          for (final ConceptRelationship innerRel : rel2.getTo()
              .getRelationships()) {
            // this is only for grouped role relationships
            if (!innerRel.isHierarchical()) {
              final ConceptRelationship innerRel2 =
                  new ConceptRelationshipJpa(innerRel, true);
              innerRel2.setFrom(rel2.getFrom());
              innerRel2.setStated(rel2.isStated());
              innerRel2.setInferred(rel2.isInferred());
              // If >1 rels in anonymous concept, group them
              if (ct > 1) {
                innerRel2.setGroup(String.valueOf(group));
              }
              result.getObjects().add(innerRel2);
            }
          }

        } else {
          result.getObjects().add(rel);
        }
      }
      result.setTotalCount(list.getTotalCount());

      return result;

    } catch (Exception e) {
      handleException(e, "trying to retrieve relationships for a concept");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/relationships/deep")
  @ApiOperation(value = "Get deep relationships with this terminologyId", notes = "Get the relationships for the concept and also for any other atoms, concepts, descirptors, or codes in its graph for the specified concept id", response = RelationshipListJpa.class)
  public RelationshipList findDeepRelationshipsForConcept(
    @ApiParam(value = "Concept terminology id, e.g. C0000039", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @QueryParam("query") String query,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/relationships/deep with query: " + query);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve deep relationships for the concept", UserRole.VIEWER);

      return contentService.findDeepRelationshipsForConcept(terminologyId,
          terminology, version, Branch.ROOT, query, false, pfs);

    } catch (Exception e) {
      handleException(e, "trying to retrieve deep relationships for a concept");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/{terminologyId}/relationships")
  @ApiOperation(value = "Get relationships with this terminologyId", notes = "Get the relationships with the given descriptor id", response = RelationshipListJpa.class)
  public RelationshipList findRelationshipsForDescriptor(
    @ApiParam(value = "Descriptor terminology id, e.g. D042033", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version + "/"
            + terminologyId + "/relationships?query=" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve relationships for the descriptor", UserRole.VIEWER);

      final RelationshipList list =
          contentService.findRelationshipsForDescriptor(terminologyId,
              terminology, version, Branch.ROOT, queryStr, false, pfs);

      // Use graph resolver
      for (final Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel : list
          .getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(rel);
      }

      return list;

    } catch (Exception e) {
      handleException(e, "trying to retrieve relationships for a descriptor");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/{terminologyId}/relationships")
  @ApiOperation(value = "Get relationships with this terminologyId", notes = "Get the relationships with the given code id", response = RelationshipListJpa.class)
  public RelationshipList findRelationshipsForCode(
    @ApiParam(value = "Code terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Code terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version + "/"
            + terminologyId + "/relationships?query=" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve relationships for the code", UserRole.VIEWER);

      final RelationshipList list =
          contentService.findRelationshipsForCode(terminologyId, terminology,
              version, Branch.ROOT, queryStr, false, pfs);

      for (final Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel : list
          .getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(rel);
      }
      return list;

    } catch (Exception e) {
      handleException(e, "trying to retrieve relationships for a code");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/aui/subset/all/{terminology}/{version}")
  @ApiOperation(value = "Get atom subsets", notes = "Get the atom level subsets", response = SubsetListJpa.class)
  public SubsetList getAtomSubsets(
    @ApiParam(value = "Atom terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Atom version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /aui/" + terminology + "/" + version
            + "/subsets");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve atom subsets",
          UserRole.VIEWER);

      final SubsetList list =
          contentService.getAtomSubsets(terminology, version, Branch.ROOT);
      for (int i = 0; i < list.getCount(); i++) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            list.getObjects().get(i));
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to retrieve atom subsets");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/cui/subset/all/{terminology}/{version}")
  @ApiOperation(value = "Get concept subsets", notes = "Get the concept level subsets", response = SubsetListJpa.class)
  public SubsetList getConceptSubsets(
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version
            + "/subsets");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve concept subsets",
          UserRole.VIEWER);
      final SubsetList list =
          contentService.getConceptSubsets(terminology, version, Branch.ROOT);
      for (int i = 0; i < list.getCount(); i++) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            list.getObjects().get(i));
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to retrieve concept subsets");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Produces({
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
  })
  @Override
  @POST
  @Path("/aui/subset/{subsetId}/{terminology}/{version}/members")
  @ApiOperation(value = "Find atom subset members", notes = "Get the members for the indicated atom subset", response = SubsetMemberListJpa.class)
  public SubsetMemberList findAtomSubsetMembers(
    @ApiParam(value = "Subset id, e.g. 341823003", required = true) @PathParam("subsetId") String subsetId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /aui/subset/" + subsetId + "/" + terminology
            + "/" + version + "/members?query=" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find atom subset members",
          UserRole.VIEWER);

      final SubsetMemberList list =
          contentService.findAtomSubsetMembers(subsetId, terminology, version,
              Branch.ROOT, queryStr, pfs);
      for (final SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member : list
          .getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(member);
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to retrieve atom subsets");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/cui/subset/{subsetId}/{terminology}/{version}/members")
  @ApiOperation(value = "Find concept subset members", notes = "Get the members for the indicated concept subset", response = SubsetMemberListJpa.class)
  public SubsetMemberList findConceptSubsetMembers(
    @ApiParam(value = "Subset id, e.g. 341823003", required = true) @PathParam("subsetId") String subsetId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/subset/" + subsetId + "/" + terminology
            + "/" + version + "/members?query=" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find concept subset members",
          UserRole.VIEWER);

      final SubsetMemberList list =
          contentService.findConceptSubsetMembers(subsetId, terminology,
              version, Branch.ROOT, queryStr, pfs);
      for (final SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member : list
          .getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(member);
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to retrieve concept subsets");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/trees")
  @ApiOperation(value = "Get trees with this terminologyId", notes = "Get the trees with the given concept id", response = TreeListJpa.class)
  public TreeList findConceptTrees(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/trees");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve trees for the concept ", UserRole.VIEWER);

      final TreePositionList list =
          contentService.findTreePositionsForConcept(terminologyId,
              terminology, version, Branch.ROOT, pfs);

      final TreeList treeList = new TreeListJpa();
      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {
        final Tree tree = contentService.getTreeForTreePosition(treepos);
        treeList.addObject(tree);
      }
      treeList.setTotalCount(list.getTotalCount());
      return treeList;

    } catch (Exception e) {
      handleException(e, "trying to retrieve trees for a concept");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/{terminologyId}/trees/")
  @ApiOperation(value = "Get trees with this terminologyId", notes = "Get the trees with the given descriptor id", response = TreeListJpa.class)
  public TreeList findDescriptorTrees(
    @ApiParam(value = "Descriptor terminology id, e.g. D002943", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version + "/"
            + terminologyId + "/trees");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve trees for the descriptor ", UserRole.VIEWER);

      final TreePositionList list =
          contentService.findTreePositionsForDescriptor(terminologyId,
              terminology, version, Branch.ROOT, pfs);

      final TreeList treeList = new TreeListJpa();
      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {
        final Tree tree = contentService.getTreeForTreePosition(treepos);

        treeList.addObject(tree);
      }
      treeList.setTotalCount(list.getTotalCount());
      return treeList;

    } catch (Exception e) {
      handleException(e, "trying to trees relationships for a descriptor");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/{terminologyId}/trees")
  @ApiOperation(value = "Get trees with this terminologyId", notes = "Get the trees with the given code id", response = TreeListJpa.class)
  public TreeList findCodeTrees(
    @ApiParam(value = "Code terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Code terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version + "/"
            + terminologyId + "/trees");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve trees for the code",
          UserRole.VIEWER);

      final TreePositionList list =
          contentService.findTreePositionsForCode(terminologyId, terminology,
              version, Branch.ROOT, pfs);
      final TreeList treeList = new TreeListJpa();
      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {
        final Tree tree = contentService.getTreeForTreePosition(treepos);

        treeList.addObject(tree);
      }
      treeList.setTotalCount(list.getTotalCount());
      return treeList;

    } catch (Exception e) {
      handleException(e, "trying to retrieve trees for a code");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/trees")
  @ApiOperation(value = "Find concept trees matching the query", notes = "Finds all merged trees matching the specified parameters", response = TreeJpa.class)
  public Tree findConceptTreeForQuery(
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query search term, e.g. 'vitamin'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version
            + "/trees?query=" + query);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the concept",
          UserRole.VIEWER);

      final TreePositionList list =
          contentService.findConceptTreePositionsForQuery(terminology, version,
              Branch.ROOT, queryStr, pfs);

      // dummy variables for construction of artificial root
      Tree dummyTree = new TreeJpa();
      dummyTree.setTerminology(terminology);
      dummyTree.setVersion(version);
      dummyTree.setNodeTerminologyId("dummy id");
      dummyTree.setNodeName("Root");
      dummyTree.setTotalCount(list.getTotalCount());

      // initialize the return tree with dummy root and set total count
      Tree returnTree = new TreeJpa(dummyTree);

      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {

        // get tree for tree position
        final Tree tree = contentService.getTreeForTreePosition(treepos);

        // construct a new dummy-root tree for merging with existing tree
        final Tree treeForTreePos = new TreeJpa(dummyTree);

        // add retrieved tree to dummy root level
        treeForTreePos.addChild(tree);

        // merge into the top-level dummy tree
        returnTree.mergeTree(treeForTreePos, pfs != null ? pfs.getSortField()
            : null);
      }

      // if only one child, dummy root not necessary
      if (returnTree.getChildren().size() == 1) {
        Tree tree = returnTree.getChildren().get(0);
        tree.setTotalCount(returnTree.getTotalCount());
        return tree;
      }

      // otherwise return the populated dummy root tree
      return returnTree;

    } catch (Exception e) {
      handleException(e, "trying to find trees for a query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/trees")
  @ApiOperation(value = "Find descriptor trees matching the query", notes = "Finds all merged trees matching the specified parameters", response = TreeJpa.class)
  public Tree findDescriptorTreeForQuery(
    @ApiParam(value = "Descriptor terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query search term, e.g. 'vitamin'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version
            + "/trees?query=" + query);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the descriptor",
          UserRole.VIEWER);

      final TreePositionList list =
          contentService.findDescriptorTreePositionsForQuery(terminology,
              version, Branch.ROOT, queryStr, pfs);

      // dummy variables for construction of artificial root
      final Tree dummyTree = new TreeJpa();
      dummyTree.setTerminology(terminology);
      dummyTree.setVersion(version);
      dummyTree.setNodeTerminologyId("dummy id");
      dummyTree.setNodeName("Root");
      dummyTree.setTotalCount(list.getTotalCount());

      // initialize the return tree with dummy root and set total count
      final Tree returnTree = new TreeJpa(dummyTree);

      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {

        // get tree for tree position
        final Tree tree = contentService.getTreeForTreePosition(treepos);

        // construct a new dummy-root tree for merging with existing tree
        final Tree treeForTreePos = new TreeJpa(dummyTree);

        // add retrieved tree to dummy root level
        treeForTreePos.addChild(tree);

        // merge into the top-level dummy tree
        returnTree.mergeTree(treeForTreePos, pfs != null ? pfs.getSortField()
            : null);
      }

      // if only one child, dummy root not necessary
      if (returnTree.getChildren().size() == 1) {
        final Tree tree = returnTree.getChildren().get(0);
        tree.setTotalCount(returnTree.getTotalCount());
        return tree;
      }

      // otherwise return the populated dummy root tree
      return returnTree;

    } catch (Exception e) {
      handleException(e, "trying to find trees for a query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/trees")
  @ApiOperation(value = "Find code trees matching the query", notes = "Finds all merged trees matching the specified parameters", response = TreeJpa.class)
  public Tree findCodeTreeForQuery(
    @ApiParam(value = "Code terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query search term, e.g. 'vitamin'", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query == null ? "" : query;
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version
            + "/trees?query=" + query);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      TreePositionList list =
          contentService.findCodeTreePositionsForQuery(terminology, version,
              Branch.ROOT, queryStr, pfs);

      // dummy variables for construction of artificial root
      final Tree dummyTree = new TreeJpa();
      dummyTree.setTerminology(terminology);
      dummyTree.setVersion(version);
      dummyTree.setNodeTerminologyId("dummy id");
      dummyTree.setNodeName("Root");
      dummyTree.setTotalCount(list.getTotalCount());

      // initialize the return tree with dummy root and set total count
      final Tree returnTree = new TreeJpa(dummyTree);

      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {

        // get tree for tree position
        final Tree tree = contentService.getTreeForTreePosition(treepos);

        // construct a new dummy-root tree for merging with existing tree
        final Tree treeForTreePos = new TreeJpa(dummyTree);

        // add retrieved tree to dummy root level
        treeForTreePos.addChild(tree);

        // merge into the top-level dummy tree
        returnTree.mergeTree(treeForTreePos, pfs != null ? pfs.getSortField()
            : null);
      }

      // if only one child, dummy root not necessary
      if (returnTree.getChildren().size() == 1) {
        final Tree tree = returnTree.getChildren().get(0);
        tree.setTotalCount(returnTree.getTotalCount());
        return tree;
      }

      // otherwise return the populated dummy root tree
      return returnTree;

    } catch (Exception e) {
      handleException(e, "trying to find trees for a query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/trees/children")
  @ApiOperation(value = "Find children trees for a concept", notes = "Returns paged children trees for a concept. Note: not ancestorPath-sensitive", response = TreeJpa.class)
  public TreeList findConceptTreeChildren(
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Concept terminologyId, e.g. C0000061", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/" + "/trees/children");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      // the TreeList to return
      final TreeList childTrees = new TreeListJpa();

      // instantiate child tree positions array, used to construct trees
      final TreePositionList childTreePositions =
          contentService.findConceptTreePositionChildren(terminologyId,
              terminology, version, Branch.ROOT, pfs);

      // for each tree position, construct a tree
      for (final TreePosition<? extends ComponentHasAttributesAndName> childTreePosition : childTreePositions
          .getObjects()) {
        final Tree childTree = new TreeJpa(childTreePosition);
        childTrees.addObject(childTree);
      }

      childTrees.setTotalCount(childTreePositions.getTotalCount());
      return childTrees;

    } catch (Exception e) {
      handleException(e, "trying to find tree children");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/{terminologyId}/trees/children")
  @ApiOperation(value = "Find children trees for a code", notes = "Returns paged children trees for a code. Note: not ancestorPath-sensitive", response = TreeJpa.class)
  public TreeList findCodeTreeChildren(
    @ApiParam(value = "Code terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Code terminologyId, e.g. C0000061", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version + "/"
            + terminologyId + "/" + "/trees/children");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      // the TreeList to return
      final TreeList childTrees = new TreeListJpa();

      // instantiate child tree positions array, used to construct trees
      final TreePositionList childTreePositions =
          contentService.findCodeTreePositionChildren(terminologyId,
              terminology, version, Branch.ROOT, pfs);

      // for each tree position, construct a tree
      for (final TreePosition<? extends ComponentHasAttributesAndName> childTreePosition : childTreePositions
          .getObjects()) {
        final Tree childTree = new TreeJpa(childTreePosition);
        childTrees.addObject(childTree);
      }

      childTrees.setTotalCount(childTreePositions.getTotalCount());
      return childTrees;

    } catch (Exception e) {
      handleException(e, "trying to find tree children");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/{terminologyId}/trees/children")
  @ApiOperation(value = "Find children trees for a descriptor", notes = "Returns paged children trees for a descriptor. Note: not ancestorPath-sensitive", response = TreeJpa.class)
  public TreeList findDescriptorTreeChildren(
    @ApiParam(value = "Descriptor terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Descriptor terminologyId, e.g. D0000061", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version + "/"
            + terminologyId + "/" + "/trees/children");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the descriptor",
          UserRole.VIEWER);

      // the TreeList to return
      final TreeList childTrees = new TreeListJpa();

      // instantiate child tree positions array, used to construct trees
      final TreePositionList childTreePositions =
          contentService.findDescriptorTreePositionChildren(terminologyId,
              terminology, version, Branch.ROOT, pfs);

      // for each tree position, construct a tree
      for (final TreePosition<? extends ComponentHasAttributesAndName> childTreePosition : childTreePositions
          .getObjects()) {
        final Tree childTree = new TreeJpa(childTreePosition);
        childTrees.addObject(childTree);
      }

      childTrees.setTotalCount(childTreePositions.getTotalCount());
      return childTrees;

    } catch (Exception e) {
      handleException(e, "trying to find tree children");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/trees/roots")
  @ApiOperation(value = "Find root trees for a concept-based terminology", notes = "Returns paged root trees for a concept-based terminology.", response = TreeJpa.class)
  public Tree findConceptTreeRoots(
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + "/trees/roots");
    final ContentService contentService = new ContentServiceJpa();

    try {
      authorizeApp(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      // instantiate root tree positions array, used to construct trees
      TreePositionList rootTreePositions = new TreePositionListJpa();

      // get tree positions where ancestor path is empty
      rootTreePositions =
          contentService.findConceptTreePositionsForQuery(terminology, version,
              Branch.ROOT, "-ancestorPath:[* TO *]", pfs);

      Tree rootTree = null;
      // if a terminology with a single root concept
      if (rootTreePositions.getCount() == 1) {

        // construct root tree from single root
        rootTree = new TreeJpa(rootTreePositions.getObjects().get(0));
        rootTree.setTotalCount(rootTreePositions.getTotalCount());

        // get the children tree positions
        final TreePositionList childTreePositions =
            contentService.findConceptTreePositionChildren(
                rootTree.getNodeTerminologyId(), terminology, version,
                Branch.ROOT, pfs);

        // construct and add children
        for (final TreePosition<? extends ComponentHasAttributesAndName> childTreePosition : childTreePositions
            .getObjects()) {
          final Tree childTree = new TreeJpa(childTreePosition);
          rootTree.addChild(childTree);
        }
      }

      // otherwise, no single root concept
      else {
        // create a dummy tree position to serve as root
        rootTree = new TreeJpa();
        rootTree.setTerminology(terminology);
        rootTree.setVersion(version);
        rootTree.setNodeName("Root");
        rootTree.setTotalCount(1);

        // construct and add children
        for (final TreePosition<? extends ComponentHasAttributesAndName> rootTreePosition : rootTreePositions
            .getObjects()) {
          final Tree childTree = new TreeJpa(rootTreePosition);
          rootTree.addChild(childTree);
        }
      }

      return rootTree;
    } catch (Exception e) {
      handleException(e, "trying to find root trees");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/trees/roots")
  @ApiOperation(value = "Find root trees for a descriptor-based terminology", notes = "Returns paged root trees for a descriptor-based terminology.", response = TreeJpa.class)
  public Tree findDescriptorTreeRoots(
    @ApiParam(value = "Descriptor terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version + "/"
            + "/trees/roots");
    final ContentService contentService = new ContentServiceJpa();

    try {
      authorizeApp(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      // instantiate root tree positions array, used to construct trees
      TreePositionList rootTreePositions = new TreePositionListJpa();

      // get tree positions where ancestor path is empty
      rootTreePositions =
          contentService.findDescriptorTreePositionsForQuery(terminology,
              version, Branch.ROOT, "-ancestorPath:[* TO *]", pfs);

      Tree rootTree = null;

      // if a terminology with a single root descriptor
      if (rootTreePositions.getCount() == 1) {

        // construct root tree from single root
        rootTree = new TreeJpa(rootTreePositions.getObjects().get(0));
        rootTree.setTotalCount(rootTreePositions.getTotalCount());

        // get the children tree positions
        final TreePositionList childTreePositions =
            contentService.findDescriptorTreePositionChildren(
                rootTree.getNodeTerminologyId(), terminology, version,
                Branch.ROOT, pfs);

        // construct and add children
        for (final TreePosition<? extends ComponentHasAttributesAndName> childTreePosition : childTreePositions
            .getObjects()) {
          final Tree childTree = new TreeJpa(childTreePosition);
          rootTree.mergeTree(childTree, null);
        }
      }

      // otherwise, no single root descriptor
      else {
        // create a dummy tree position to serve as root
        rootTree = new TreeJpa();
        rootTree.setTerminology(terminology);
        rootTree.setVersion(version);
        rootTree.setNodeName("Root");
        rootTree.setTotalCount(1);

        // construct and add children
        for (final TreePosition<? extends ComponentHasAttributesAndName> rootTreePosition : rootTreePositions
            .getObjects()) {
          final Tree childTree = new TreeJpa(rootTreePosition);
          rootTree.addChild(childTree);
        }
      }

      return rootTree;
    } catch (Exception e) {
      handleException(e, "trying to find root trees");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/trees/roots")
  @ApiOperation(value = "Find root trees for a code-based terminology", notes = "Returns paged root trees for a code-based terminology.", response = TreeJpa.class)
  public Tree findCodeTreeRoots(
    @ApiParam(value = "Code terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version + "/"
            + "/trees/roots");
    final ContentService contentService = new ContentServiceJpa();

    try {
      authorizeApp(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      // instantiate root tree positions array, used to construct trees
      TreePositionList rootTreePositions = new TreePositionListJpa();

      // get tree positions where ancestor path is empty
      rootTreePositions =
          contentService.findCodeTreePositionsForQuery(terminology, version,
              Branch.ROOT, "-ancestorPath:[* TO *]", pfs);

      Tree rootTree = null;

      // if a terminology with a single root code
      if (rootTreePositions.getCount() == 1) {

        // construct root tree from single root
        rootTree = new TreeJpa(rootTreePositions.getObjects().get(0));
        rootTree.setTotalCount(rootTreePositions.getTotalCount());

        // get the children tree positions
        final TreePositionList childTreePositions =
            contentService.findCodeTreePositionChildren(
                rootTree.getNodeTerminologyId(), terminology, version,
                Branch.ROOT, pfs);

        // construct and add children
        for (final TreePosition<? extends ComponentHasAttributesAndName> childTreePosition : childTreePositions
            .getObjects()) {
          final Tree childTree = new TreeJpa(childTreePosition);
          rootTree.mergeTree(childTree, null);
        }
      }

      // otherwise, no single root code
      else {
        // create a dummy tree position to serve as root
        rootTree = new TreeJpa();
        rootTree.setTerminology(terminology);
        rootTree.setVersion(version);
        rootTree.setNodeName("Root");
        rootTree.setTotalCount(1);

        // construct and add children
        for (final TreePosition<? extends ComponentHasAttributesAndName> rootTreePosition : rootTreePositions
            .getObjects()) {
          final Tree childTree = new TreeJpa(rootTreePosition);
          rootTree.addChild(childTree);
        }
      }

      return rootTree;
    } catch (Exception e) {
      handleException(e, "trying to find root trees");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Produces({
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
  })
  @Override
  @POST
  @Path("/mapset/{mapSetId}/{terminology}/{version}/mappings")
  @ApiOperation(value = "Find mappings", notes = "Get the mappings for the indicated mapset", response = MappingListJpa.class)
  public MappingList findMappingsForMapSet(
    @ApiParam(value = "MapSet terminology id, e.g. 341823003", required = true) @PathParam("mapSetId") String mapSetId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /mapset/" + mapSetId + "/" + terminology + "/"
            + version + "/mappings" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find mappings", UserRole.VIEWER);

      final MapSet mapSet =
          contentService.getMapSet(mapSetId, terminology, version, Branch.ROOT);
      final MappingList mappingList =
          contentService.findMappingsForMapSet(mapSet.getId(), query, pfs);
      for (final Mapping member : mappingList.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(member);
      }
      return mappingList;
    } catch (Exception e) {
      handleException(e, "trying to retrieve mappings from mapset");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Produces({
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
  })
  @Override
  @POST
  @Path("/cui/{terminologyId}/{terminology}/{version}/mappings")
  @ApiOperation(value = "Find mappings", notes = "Get the mappings for the indicated concept", response = MappingListJpa.class)
  public MappingList findMappingsForConcept(
    @ApiParam(value = "Concept terminology id, e.g. 341823003", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminologyId + "/" + terminology
            + "/" + version + "/mappings" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find mappings", UserRole.VIEWER);

      final MappingList mappingList =
          contentService.findMappingsForConcept(terminologyId, terminology,
              version, Branch.ROOT, query, pfs);
      for (final Mapping member : mappingList.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(member);
      }
      return mappingList;
    } catch (Exception e) {
      handleException(e, "trying to retrieve mappings from concept");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Produces({
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
  })
  @Override
  @POST
  @Path("/code/{terminologyId}/{terminology}/{version}/mappings")
  @ApiOperation(value = "Find mappings", notes = "Get the mappings for the indicated code", response = MappingListJpa.class)
  public MappingList findMappingsForCode(
    @ApiParam(value = "Code terminology id, e.g. 341823003", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminologyId + "/" + terminology
            + "/" + version + "/mappings" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find mappings", UserRole.VIEWER);

      final MappingList mappingList =
          contentService.findMappingsForCode(terminologyId, terminology,
              version, Branch.ROOT, query, pfs);
      for (final Mapping member : mappingList.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(member);
      }
      return mappingList;
    } catch (Exception e) {
      handleException(e, "trying to retrieve mappings from code");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Produces({
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
  })
  @Override
  @POST
  @Path("/dui/{terminologyId}/{terminology}/{version}/mappings")
  @ApiOperation(value = "Find mappings", notes = "Get the mappings for the indicated descriptor", response = MappingListJpa.class)
  public MappingList findMappingsForDescriptor(
    @ApiParam(value = "Descriptor terminology id, e.g. 341823003", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminologyId + "/" + terminology
            + "/" + version + "/mappings" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find mappings", UserRole.VIEWER);

      final MappingList mappingList =
          contentService.findMappingsForDescriptor(terminologyId, terminology,
              version, Branch.ROOT, query, pfs);
      for (final Mapping member : mappingList.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(member);
      }
      return mappingList;
    } catch (Exception e) {
      handleException(e, "trying to retrieve mappings from descriptor");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }
}
