/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.io.File;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreeList;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.algo.ClamlLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.LuceneReindexAlgorithm;
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
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.helpers.content.TreeJpa;
import com.wci.umls.server.jpa.helpers.content.TreeListJpa;
import com.wci.umls.server.jpa.helpers.content.TreePositionListJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.helper.TerminologyUtility;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.ContentService;
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#luceneReindex(
   * java.lang.String, java.lang.String)
   */
  @Override
  @POST
  @Path("/reindex")
  @ApiOperation(value = "Reindexes specified objects", notes = "Recomputes lucene indexes for the specified comma-separated objects")
  public void luceneReindex(
    @ApiParam(value = "Comma-separated list of objects to reindex, e.g. ConceptJpa (optional)", required = false) String indexedObjects,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)

  throws Exception {
    Logger.getLogger(getClass()).info("test");
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Content): /reindex "
            + (indexedObjects == null ? "with no objects specified"
                : "with specified objects " + indexedObjects));

    // Track system level information
    long startTimeOrig = System.nanoTime();
    LuceneReindexAlgorithm algo = new LuceneReindexAlgorithm();
    try {
      authenticate(securityService, authToken, "reindex",
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * computeTransitiveClosure(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  @Override
  @POST
  @Path("/terminology/closure/compute/{terminology}/{version}")
  @ApiOperation(value = "Computes terminology transitive closure", notes = "Computes transitive closure for the latest version of the specified terminology")
  public void computeTransitiveClosure(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)

  throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Content): /terminology/closure/compute/"
            + terminology + "/" + version);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    TransitiveClosureAlgorithm algo = new TransitiveClosureAlgorithm();
    MetadataService service = new MetadataServiceJpa();
    try {
      authenticate(securityService, authToken, "compute transitive closure",
          UserRole.ADMINISTRATOR);

      // Compute transitive closure
      Logger.getLogger(getClass()).info(
          "  Compute transitive closure for  " + terminology + "/" + version);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setIdType(service.getTerminology(terminology, version)
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
      service.close();
      securityService.close();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#computeTreePositions
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @POST
  @Path("/terminology/treepos/compute/{terminology}/{version}")
  @ApiOperation(value = "Computes terminology tree positions", notes = "Computes tree positions for the latest version of the specified terminology")
  public void computeTreePositions(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)

  throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Content): /terminology/treepos/compute/"
            + terminology + "/" + version);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    TreePositionAlgorithm algo = new TreePositionAlgorithm();
    MetadataService service = new MetadataServiceJpa();
    try {
      authenticate(securityService, authToken, "compute tree positions ",
          UserRole.ADMINISTRATOR);

      // Compute tree positions
      Logger.getLogger(getClass()).info(
          "  Compute tree positions for " + terminology + "/" + version);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setIdType(service.getTerminology(terminology, version)
          .getOrganizingClassType());
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
      service.close();
      securityService.close();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#loadTerminologyRrf
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @PUT
  @Path("/terminology/load/rrf/{singleMode}/{terminology}/{version}")
  @Consumes(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Load all terminologies from an RRF directory", notes = "Loads terminologies from an RRF directory for specified terminology and version")
  public void loadTerminologyRrf(
    @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Single mode, e.g. false", required = true) @PathParam("singleMode") boolean singleMode,
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

    try {
      authenticate(securityService, authToken, "load RRF",
          UserRole.ADMINISTRATOR);

      // Check the input directory
      File inputDirFile = new File(inputDir);
      if (!inputDirFile.exists()) {
        throw new Exception("Specified input directory does not exist");
      }

      // Sort files - not really needed because files are already sorted
      Logger.getLogger(getClass()).info("  Sort RRF Files");
      RrfFileSorter sorter = new RrfFileSorter();
      sorter.setRequireAllFiles(true);
      // File outputDir = new File(inputDirFile, "/RRF-sorted-temp/");
      // sorter.sortFiles(inputDirFile, outputDir);
      String releaseVersion = sorter.getFileVersion(inputDirFile);
      Logger.getLogger(getClass()).info("  releaseVersion = " + releaseVersion);

      // Open readers - just open original RRF
      RrfReaders readers = new RrfReaders(inputDirFile);
      readers.openOriginalReaders();

      // Load snapshot
      RrfLoaderAlgorithm algorithm = new RrfLoaderAlgorithm();
      algorithm.setTerminology(terminology);
      algorithm.setVersion(version);
      algorithm.setSingleMode(singleMode);
      algorithm.setReleaseVersion(releaseVersion);
      algorithm.setReaders(readers);
      algorithm.compute();
      algorithm.close();
      algorithm = null;

      // Compute transitive closure
      // Obtain each terminology and run transitive closure on it with the
      // correct id type
      MetadataService metadataService = new MetadataServiceJpa();
      // Refresh caches after metadata has changed in loader
      metadataService.refreshCaches();
      for (Terminology t : metadataService.getTerminologyLatestVersions()
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
      for (Terminology t : metadataService.getTerminologyLatestVersions()
          .getObjects()) {
        // Only compute for organizing class types
        if (t.getOrganizingClassType() != null) {
          TreePositionAlgorithm algo = new TreePositionAlgorithm();
          algo.setTerminology(t.getTerminology());
          algo.setVersion(t.getVersion());
          algo.setIdType(t.getOrganizingClassType());
          // some terminologies may have cycles, allow these for now.
          algo.setCycleTolerant(true);
          algo.compute();
          algo.close();
        }
      }

      // Clean-up
      // readers.closeReaders();
      metadataService.close();
      ConfigUtility
          .deleteDirectory(new File(inputDirFile, "/RRF-sorted-temp/"));

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

    } catch (Exception e) {
      handleException(e, "trying to load terminology from RRF directory");
    } finally {
      securityService.close();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * loadTerminologyRf2Delta(java.lang.String, java.lang.String,
   * java.lang.String)
   */
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

    try {
      authenticate(securityService, authToken, "start editing cycle",
          UserRole.ADMINISTRATOR);

      Logger.getLogger(getClass()).info("Starting RF2 delta loader");
      Logger.getLogger(getClass()).info("  terminology = " + terminology);
      Logger.getLogger(getClass()).info("  inputDir = " + inputDir);

      // Check the input directory
      File inputDirFile = new File(inputDir);
      if (!inputDirFile.exists()) {
        throw new Exception("Specified input directory does not exist");
      }

      // Previous computation of terminology version is based on file name
      // but for delta/daily build files, this is not the current version
      // look up the current version instead
      MetadataService metadataService = new MetadataServiceJpa();
      final String version = metadataService.getLatestVersion(terminology);
      metadataService.close();
      if (version == null) {
        throw new Exception("Unable to determine terminology version.");
      }

      // Sort files
      Logger.getLogger(getClass()).info("  Sort RF2 Files");
      Rf2FileSorter sorter = new Rf2FileSorter();
      sorter.setSortByEffectiveTime(false);
      sorter.setRequireAllFiles(false);
      File outputDir = new File(inputDirFile, "/RF2-sorted-temp/");
      sorter.sortFiles(inputDirFile, outputDir);

      // Open readers
      Rf2Readers readers = new Rf2Readers(outputDir);
      readers.openReaders();

      // Load delta
      Rf2DeltaLoaderAlgorithm algorithm = new Rf2DeltaLoaderAlgorithm();
      algorithm.setTerminology(terminology);
      algorithm.setVersion(version);
      algorithm.setReleaseVersion(sorter.getFileVersion());
      algorithm.setReaders(readers);
      algorithm.compute();
      algorithm.close();

      // Compute transitive closure
      Logger.getLogger(getClass()).info(
          "  Compute transitive closure from  " + terminology + "/" + version);
      TransitiveClosureAlgorithm algo = new TransitiveClosureAlgorithm();
      algo.setTerminology(terminology);
      algo.setVersion(version);
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * loadTerminologyRf2Snapshot(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  @PUT
  @Path("/terminology/load/rf2/snapshot/{terminology}/{version}")
  @Consumes({
    MediaType.TEXT_PLAIN
  })
  @ApiOperation(value = "Loads terminology RF2 snapshot from directory", notes = "Loads terminology RF2 snapshot from directory for specified terminology and version")
  public void loadTerminologyRf2Snapshot(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "RF2 input directory", required = true) String inputDir,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info(
            "RESTful POST call (Content): /terminology/load/rf2/snapshot/"
                + terminology + "/" + version + " from input directory "
                + inputDir);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    try {
      authenticate(securityService, authToken, "start editing cycle",
          UserRole.ADMINISTRATOR);

      // Check the input directory
      File inputDirFile = new File(inputDir);
      if (!inputDirFile.exists()) {
        throw new Exception("Specified input directory does not exist");
      }

      // Sort files
      Logger.getLogger(getClass()).info("  Sort RF2 Files");
      Rf2FileSorter sorter = new Rf2FileSorter();
      sorter.setSortByEffectiveTime(false);
      sorter.setRequireAllFiles(true);
      File outputDir = new File(inputDirFile, "/RF2-sorted-temp/");
      sorter.sortFiles(inputDirFile, outputDir);
      String releaseVersion = sorter.getFileVersion();
      Logger.getLogger(getClass()).info("  releaseVersion = " + releaseVersion);

      // Open readers
      Rf2Readers readers = new Rf2Readers(outputDir);
      readers.openReaders();

      // Load snapshot
      Rf2SnapshotLoaderAlgorithm algorithm = new Rf2SnapshotLoaderAlgorithm();
      algorithm.setTerminology(terminology);
      algorithm.setVersion(version);
      algorithm.setReleaseVersion(releaseVersion);
      algorithm.setReaders(readers);
      algorithm.compute();
      algorithm.close();
      algorithm = null;

      // Compute transitive closure
      Logger.getLogger(getClass()).info(
          "  Compute transitive closure from  " + terminology + "/" + version);
      TransitiveClosureAlgorithm algo = new TransitiveClosureAlgorithm();
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.reset();
      algo.compute();

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
      securityService.close();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#loadTerminologyClaml
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @PUT
  @Path("/terminology/load/claml/{terminology}/{version}")
  @Consumes({
    MediaType.TEXT_PLAIN
  })
  @ApiOperation(value = "Loads ClaML terminology from file", notes = "Loads terminology from ClaML file, assigning specified version")
  public void loadTerminologyClaml(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "ClaML input file", required = true) String inputFile,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Content): /terminology/load/claml/" + terminology
            + "/" + version + " from input file " + inputFile);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    ClamlLoaderAlgorithm clamlAlgorithm = new ClamlLoaderAlgorithm();
    TransitiveClosureAlgorithm transitiveClosureAlgorithm =
        new TransitiveClosureAlgorithm();
    try {
      authenticate(securityService, authToken, "start editing cycle",
          UserRole.ADMINISTRATOR);

      // Load snapshot
      Logger.getLogger(getClass()).info("Load ClaML data from " + inputFile);
      clamlAlgorithm.setTerminology(terminology);
      clamlAlgorithm.setVersion(version);
      clamlAlgorithm.setInputFile(inputFile);
      clamlAlgorithm.compute();

      // Let service begin its own transaction
      Logger.getLogger(getClass()).info("Start computing transtive closure");
      transitiveClosureAlgorithm.setTerminology(terminology);
      transitiveClosureAlgorithm.setVersion(version);
      transitiveClosureAlgorithm.reset();
      transitiveClosureAlgorithm.compute();

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

    } catch (Exception e) {
      handleException(e, "trying to load terminology from ClaML file");
    } finally {
      clamlAlgorithm.close();
      transitiveClosureAlgorithm.close();
      securityService.close();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#removeTerminology
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/terminology/remove/{terminology}/{version}")
  @ApiOperation(value = "Remove a terminology", notes = "Removes all elements for a specified terminology and version")
  public SearchResult removeTerminology(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful GET call (Content): /terminology/remove/" + terminology + "/"
            + version);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    RemoveTerminologyAlgorithm algo = new RemoveTerminologyAlgorithm();
    MetadataService service = new MetadataServiceJpa();
    try {
      authenticate(securityService, authToken, "remove terminology",
          UserRole.ADMINISTRATOR);

      // Compute transitive closure
      Logger.getLogger(getClass()).info(
          "  Remove terminology for  " + terminology + "/" + version);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.compute();

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

      
    } catch (Exception e) {
      handleException(e, "trying to remove terminology");
    } finally {
      algo.close();
      service.close();
      securityService.close();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getConcept(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/cui/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get concept by id, terminology, and version", notes = "Get the root branch concept matching the specified parameters", response = Concept.class)
  public Concept getConcept(
    @ApiParam(value = "Concept terminology id, e.g. C0000039", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "retrieve the concept",
          UserRole.VIEWER);

      Concept concept =
          contentService.getConcept(terminologyId, terminology, version,
              Branch.ROOT);

      if (concept != null) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            concept,
            TerminologyUtility.getHierarchicalIsaRels(concept.getTerminology(),
                concept.getVersion()));
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findConceptsForQuery
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfscParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/query/{query}")
  @ApiOperation(value = "Find concepts matching a search query", notes = "Gets a list of search results that match the lucene query for the root branch", response = SearchResultList.class)
  public SearchResultList findConceptsForQuery(
    @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'aspirin'", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFSC Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfscParameterJpa pfsc,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query;
    if (query == null || query.equals(ContentServiceRest.QUERY_BLANK)) {
      queryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version
            + "/query/" + queryStr + " with PFS parameter "
            + (pfsc == null ? "empty" : pfsc.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find concepts by query",
          UserRole.VIEWER);
      SearchResultList sr =
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findConceptsForGeneralQuery(java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/cui/luceneQuery/{luceneQuery}/hqlQuery/{hqlQuery}")
  @ApiOperation(value = "Find concepts matching a lucene or hql search query", notes = "Gets a list of search results that match the lucene or hql query for the root branch", response = SearchResultList.class)
  public SearchResultList findConceptsForGeneralQuery(
    @ApiParam(value = "Lucene Query", required = true) @PathParam("luceneQuery") String luceneQuery,
    @ApiParam(value = "HQL Query", required = true) @PathParam("hqlQuery") String hqlQuery,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String luceneQueryStr = luceneQuery;
    if (luceneQuery == null
        || luceneQuery.equals(ContentServiceRest.QUERY_BLANK)) {
      luceneQueryStr = "";
    }
    String hqlQueryStr = hqlQuery;
    if (hqlQuery == null || hqlQuery.equals(ContentServiceRest.QUERY_BLANK)) {
      hqlQueryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + "/luceneQuery/" + luceneQueryStr
            + "/hqlQuery/" + hqlQueryStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find concepts by query",
          UserRole.VIEWER);

      SearchResultList sr =
          contentService.findConceptsForGeneralQuery(luceneQueryStr,
              hqlQueryStr, Branch.ROOT, pfs);
      return sr;

    } catch (Exception e) {
      handleException(e, "trying to find the concepts by query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findCodesForGeneralQuery(java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/code/luceneQuery/{luceneQuery}/hqlQuery/{hqlQuery}")
  @ApiOperation(value = "Find codes matching a lucene or hql search query", notes = "Gets a list of search results that match the lucene or hql query for the root branch", response = SearchResultList.class)
  public SearchResultList findCodesForGeneralQuery(
    @ApiParam(value = "Lucene Query", required = true) @PathParam("luceneQuery") String luceneQuery,
    @ApiParam(value = "HQL Query", required = true) @PathParam("hqlQuery") String hqlQuery,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String luceneQueryStr = luceneQuery;
    if (luceneQuery == null
        || luceneQuery.equals(ContentServiceRest.QUERY_BLANK)) {
      luceneQueryStr = "";
    }
    String hqlQueryStr = hqlQuery;
    if (hqlQuery == null || hqlQuery.equals(ContentServiceRest.QUERY_BLANK)) {
      hqlQueryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + "/luceneQuery/" + luceneQueryStr
            + "/hqlQuery/" + hqlQueryStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find codes by query",
          UserRole.VIEWER);

      SearchResultList sr =
          contentService.findCodesForGeneralQuery(luceneQueryStr, hqlQueryStr,
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#autocompleteConcepts
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/cui/{terminology}/{version}/autocomplete/{searchTerm}")
  @ApiOperation(value = "Find autocomplete matches for concept searches", notes = "Gets a list of search autocomplete matches for the specified search term", response = StringList.class)
  public StringList autocompleteConcepts(
    @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Search term, e.g. 'sul'", required = true) @PathParam("searchTerm") String searchTerm,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version
            + "/autocomplete/" + searchTerm);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find concepts by query",
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getDescriptor(
   * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/dui/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get descriptor by id, terminology, and version", notes = "Get the root branch descriptor matching the specified parameters", response = Descriptor.class)
  public Descriptor getDescriptor(
    @ApiParam(value = "Descriptor terminology id, e.g. D003933", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor terminology version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version + "/"
            + terminologyId);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "retrieve the descriptor",
          UserRole.VIEWER);

      Descriptor descriptor =
          contentService.getDescriptor(terminologyId, terminology, version,
              Branch.ROOT);

      if (descriptor != null) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            descriptor,
            TerminologyUtility.getHierarchicalIsaRels(
                descriptor.getTerminology(), descriptor.getVersion()));
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findDescriptorsForQuery(java.lang.String, java.lang.String,
   * java.lang.String, com.wci.umls.server.jpa.helpers.PfscParameterJpa,
   * java.lang.String)
   */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/query/{query}")
  @ApiOperation(value = "Find descriptors matching a search query", notes = "Gets a list of search results that match the lucene query for the root branch", response = SearchResultList.class)
  public SearchResultList findDescriptorsForQuery(
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor terminology version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'aspirin'", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFSC Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfscParameterJpa pfsc,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query;
    if (query == null || query.equals(ContentServiceRest.QUERY_BLANK)) {
      queryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version
            + "/query/" + queryStr + " with PFS parameter "
            + (pfsc == null ? "empty" : pfsc.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find descriptors by query",
          UserRole.VIEWER);

      SearchResultList sr =
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findDescriptorsForGeneralQuery(java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/dui/luceneQuery/{luceneQuery}/hqlQuery/{hqlQuery}/")
  @ApiOperation(value = "Find descriptors matching a lucene or hql search query", notes = "Gets a list of search results that match the lucene or hql query for the root branch", response = SearchResultList.class)
  public SearchResultList findDescriptorsForGeneralQuery(
    @ApiParam(value = "Lucene Query", required = true) @PathParam("luceneQuery") String luceneQuery,
    @ApiParam(value = "HQL Query", required = true) @PathParam("hqlQuery") String hqlQuery,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String luceneQueryStr = luceneQuery;
    if (luceneQuery == null
        || luceneQuery.equals(ContentServiceRest.QUERY_BLANK)) {
      luceneQueryStr = "";
    }
    String hqlQueryStr = hqlQuery;
    if (hqlQuery == null || hqlQuery.equals(ContentServiceRest.QUERY_BLANK)) {
      hqlQueryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + "/luceneQuery/" + luceneQueryStr
            + "/hqlQuery/" + hqlQueryStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find concepts by query",
          UserRole.VIEWER);

      SearchResultList sr =
          contentService.findDescriptorsForGeneralQuery(luceneQueryStr,
              hqlQueryStr, Branch.ROOT, pfs);
      return sr;

    } catch (Exception e) {
      handleException(e, "trying to find the concepts by query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * autocompleteDescriptors(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/dui/{terminology}/{version}/autocomplete/{searchTerm}")
  @ApiOperation(value = "Find autocomplete matches for descriptor searches", notes = "Gets a list of search autocomplete matches for the specified search term", response = StringList.class)
  public StringList autocompleteDescriptors(
    @ApiParam(value = "Terminology, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Search term, e.g. 'sul'", required = true) @PathParam("searchTerm") String searchTerm,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version
            + "/autocomplete/" + searchTerm);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find descriptors by query",
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#getCode(java
   * .lang .String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/code/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get code by id, terminology, and version", notes = "Get the root branch code matching the specified parameters", response = Code.class)
  public Code getCode(
    @ApiParam(value = "Code terminology id, e.g. U002135", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Code terminology name, e.g. MTH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code terminology version, e.g. 2014AB", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version + "/"
            + terminologyId);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "retrieve the code",
          UserRole.VIEWER);

      Code code =
          contentService.getCode(terminologyId, terminology, version,
              Branch.ROOT);

      if (code != null) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            code,
            TerminologyUtility.getHierarchicalIsaRels(code.getTerminology(),
                code.getVersion()));
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findCodesForQuery
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfscParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/query/{query}")
  @ApiOperation(value = "Find codes matching a search query", notes = "Gets a list of search results that match the lucene query for the root branch", response = SearchResultList.class)
  public SearchResultList findCodesForQuery(
    @ApiParam(value = "Code terminology name, e.g. MTH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code terminology version, e.g. 2014AB", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'aspirin'", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFSC Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfscParameterJpa pfsc,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query;
    if (query == null || query.equals(ContentServiceRest.QUERY_BLANK)) {
      queryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version
            + "/query/" + queryStr + " with PFS parameter "
            + (pfsc == null ? "empty" : pfsc.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find codes by query",
          UserRole.VIEWER);

      SearchResultList sr =
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#autocompleteCodes
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/code/{terminology}/{version}/autocomplete/{searchTerm}")
  @ApiOperation(value = "Find autocomplete matches for code searches", notes = "Gets a list of search autocomplete matches for the specified search term", response = StringList.class)
  public StringList autocompleteCodes(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Search term, e.g. 'sul'", required = true) @PathParam("searchTerm") String searchTerm,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version
            + "/autocomplete/" + searchTerm);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find code by query",
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getLexicalClass
   * (java.lang .String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/lui/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get lexical class by id, terminology, and version", notes = "Get the root branch lexical class matching the specified parameters", response = LexicalClass.class)
  public LexicalClass getLexicalClass(
    @ApiParam(value = "Lexical class terminology id, e.g. L0356926", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Lexical class terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Lexical class terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /lui/" + terminology + "/" + version + "/"
            + terminologyId);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "retrieve the lexical class",
          UserRole.VIEWER);

      LexicalClass lexicalClass =
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getStringClass
   * (java.lang .String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/sui/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get string class by id, terminology, and version", notes = "Get the root branch string class matching the specified parameters", response = StringClass.class)
  public StringClass getStringClass(
    @ApiParam(value = "String class terminology id, e.g. S0356926", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "String class terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "String class terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /sui/" + terminology + "/" + version + "/"
            + terminologyId);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "retrieve the string class",
          UserRole.VIEWER);

      StringClass stringClass =
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findAncestorConcepts
   * (java.lang.String, java.lang.String, java.lang.String, boolean,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/ancestors/{parentsOnly}")
  @ApiOperation(value = "Find ancestor concepts", notes = "Gets a list of ancestor concepts", response = ConceptList.class)
  public ConceptList findAncestorConcepts(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("parentsOnly") boolean parentsOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/ancestors with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find ancestor concepts",
          UserRole.VIEWER);

      ConceptList list =
          contentService.findAncestorConcepts(terminologyId, terminology,
              version, parentsOnly, Branch.ROOT, pfs);

      for (Concept concept : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            concept,
            TerminologyUtility.getHierarchicalIsaRels(concept.getTerminology(),
                concept.getVersion()));
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findDescendantConcepts (java.lang.String, java.lang.String,
   * java.lang.String, boolean, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/descendants/{childrenOnly}")
  @ApiOperation(value = "Find descendant concepts", notes = "Gets a list of descendant concepts", response = ConceptList.class)
  public ConceptList findDescendantConcepts(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("childrenOnly") boolean childrenOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/descendants with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find descendant concepts",
          UserRole.VIEWER);

      ConceptList list =
          contentService.findDescendantConcepts(terminologyId, terminology,
              version, childrenOnly, Branch.ROOT, pfs);

      for (Concept concept : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            concept,
            TerminologyUtility.getHierarchicalIsaRels(concept.getTerminology(),
                concept.getVersion()));
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findAncestorDescriptors(java.lang.String, java.lang.String,
   * java.lang.String, boolean, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/{terminologyId}/ancestors/{parentsOnly}")
  @ApiOperation(value = "Find ancestor descriptors", notes = "Gets a list of ancestor descriptors", response = DescriptorList.class)
  public DescriptorList findAncestorDescriptors(
    @ApiParam(value = "Descriptor terminology id, e.g. D003423", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("parentsOnly") boolean parentsOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version
            + terminologyId + "/ancestors with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find ancestor descriptors",
          UserRole.VIEWER);

      DescriptorList list =
          contentService.findAncestorDescriptors(terminologyId, terminology,
              version, parentsOnly, Branch.ROOT, pfs);

      for (Descriptor descriptor : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            descriptor,
            TerminologyUtility.getHierarchicalIsaRels(
                descriptor.getTerminology(), descriptor.getVersion()));
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findDescendantDescriptors(java.lang.String, java.lang.String,
   * java.lang.String, boolean, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/{terminologyId}/descendants/{childrenOnly}")
  @ApiOperation(value = "Find descendant descriptors", notes = "Gets a list of descendant descriptors", response = DescriptorList.class)
  public DescriptorList findDescendantDescriptors(
    @ApiParam(value = "Descriptor terminology id, e.g. D002342", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("childrenOnly") boolean childrenOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version
            + terminologyId + "/descendants with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find descendant descriptors",
          UserRole.VIEWER);

      DescriptorList list =
          contentService.findDescendantDescriptors(terminologyId, terminology,
              version, childrenOnly, Branch.ROOT, pfs);

      for (Descriptor descriptor : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            descriptor,
            TerminologyUtility.getHierarchicalIsaRels(
                descriptor.getTerminology(), descriptor.getVersion()));
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findAncestorCodes
   * (java.lang.String, java.lang.String, java.lang.String, boolean,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/{terminologyId}/ancestors/{parentsOnly}")
  @ApiOperation(value = "Find ancestor codes", notes = "Gets a list of ancestor codes", response = CodeList.class)
  public CodeList findAncestorCodes(
    @ApiParam(value = "Code terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("parentsOnly") boolean parentsOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version
            + terminologyId + "/ancestors with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find ancestor codes",
          UserRole.VIEWER);
      CodeList list =
          contentService.findAncestorCodes(terminologyId, terminology, version,
              parentsOnly, Branch.ROOT, pfs);

      for (Code code : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            code,
            TerminologyUtility.getHierarchicalIsaRels(code.getTerminology(),
                code.getVersion()));
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findDescendantCodes
   * (java.lang.String, java.lang.String, java.lang.String, boolean,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/{terminologyId}/descendants/{childrenOnly}")
  @ApiOperation(value = "Find descendant codes", notes = "Gets a list of descendant codes", response = CodeList.class)
  public CodeList findDescendantCodes(
    @ApiParam(value = "Code terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("childrenOnly") boolean childrenOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version
            + terminologyId + "/descendants with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find descendant codes",
          UserRole.VIEWER);

      CodeList list =
          contentService.findDescendantCodes(terminologyId, terminology,
              version, childrenOnly, Branch.ROOT, pfs);

      for (Code code : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(
            code,
            TerminologyUtility.getHierarchicalIsaRels(code.getTerminology(),
                code.getVersion()));
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * getSubsetMembersForConcept(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/cui/{terminology}/{version}/{terminologyId}/members")
  @ApiOperation(value = "Get subset members with this terminologyId", notes = "Get the subset members with the given concept id", response = SubsetMemberList.class)
  public SubsetMemberList getSubsetMembersForConcept(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/members");
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken,
          "retrieve subset members for the concept", UserRole.VIEWER);

      SubsetMemberList list =
          contentService.getSubsetMembersForConcept(terminologyId, terminology,
              version, Branch.ROOT);

      for (SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member : list
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * getSubsetMembersForAtom(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/aui/{terminology}/{version}/{terminologyId}/members")
  @ApiOperation(value = "Get subset members with this terminologyId", notes = "Get the subset members with the given atom id", response = SubsetMemberList.class)
  public SubsetMemberList getSubsetMembersForAtom(
    @ApiParam(value = "Atom terminology id, e.g. 102751015", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Atom terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Atom terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /aui/" + terminology + "/" + version + "/"
            + terminologyId + "/members");
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken,
          "retrieve subset members for the atom", UserRole.VIEWER);

      SubsetMemberList list =
          contentService.getSubsetMembersForAtom(terminologyId, terminology,
              version, Branch.ROOT);

      for (SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member : list
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findRelationshipsForConcept(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/relationships/query/{query}")
  @ApiOperation(value = "Get relationships with this terminologyId", notes = "Get the relationships with the given concept id", response = RelationshipList.class)
  public RelationshipList findRelationshipsForConcept(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/relationships/query/" + query);
    String queryStr = query;
    if (query == null || query.equals(ContentServiceRest.QUERY_BLANK)) {
      queryStr = "";
    }

    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken,
          "retrieve relationships for the concept", UserRole.VIEWER);

      RelationshipList list =
          contentService.findRelationshipsForConcept(terminologyId,
              terminology, version, Branch.ROOT, queryStr, false, pfs);

      // Use graph resolver
      for (Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel : list
          .getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(rel);
      }

      return list;

    } catch (Exception e) {
      handleException(e, "trying to retrieve relationships for a concept");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findDeepRelationshipsForConcept(java.lang.String, java.lang.String,
   * java.lang.String, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/relationships/deep")
  @ApiOperation(value = "Get deep relationships with this terminologyId", notes = "Get the relationships for the concept and also for any other atoms, concepts, descirptors, or codes in its graph for the specified concept id", response = RelationshipList.class)
  public RelationshipList findDeepRelationshipsForConcept(
    @ApiParam(value = "Concept terminology id, e.g. C0000039", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/relationships/deep");
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken,
          "retrieve deep relationships for the concept", UserRole.VIEWER);

      return contentService.findDeepRelationshipsForConcept(terminologyId,
          terminology, version, Branch.ROOT, false, pfs);

    } catch (Exception e) {
      handleException(e, "trying to retrieve deep relationships for a concept");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findRelationshipsForDescriptor(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/{terminologyId}/relationships/query/{query}")
  @ApiOperation(value = "Get relationships with this terminologyId", notes = "Get the relationships with the given descriptor id", response = RelationshipList.class)
  public RelationshipList findRelationshipsForDescriptor(
    @ApiParam(value = "Descriptor terminology id, e.g. D042033", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor terminology version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    String queryStr = query;
    if (query == null || query.equals(ContentServiceRest.QUERY_BLANK)) {
      queryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version + "/"
            + terminologyId + "/relationships/query/" + queryStr);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken,
          "retrieve relationships for the descriptor", UserRole.VIEWER);

      RelationshipList list =
          contentService.findRelationshipsForDescriptor(terminologyId,
              terminology, version, Branch.ROOT, queryStr, false, pfs);

      // Use graph resolver
      for (Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel : list
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findRelationshipsForCode(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/{terminologyId}/relationships/query/{query}")
  @ApiOperation(value = "Get relationships with this terminologyId", notes = "Get the relationships with the given code id", response = RelationshipList.class)
  public RelationshipList findRelationshipsForCode(
    @ApiParam(value = "Code terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Code terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    String queryStr = query;
    if (query == null || query.equals(ContentServiceRest.QUERY_BLANK)) {
      queryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version + "/"
            + terminologyId + "/relationships/query" + queryStr);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken,
          "retrieve relationships for the code", UserRole.VIEWER);

      RelationshipList list =
          contentService.findRelationshipsForCode(terminologyId, terminology,
              version, Branch.ROOT, queryStr, false, pfs);

      for (Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel : list
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getAtomSubsets
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/aui/subset/all/{terminology}/{version}")
  @ApiOperation(value = "Get atom subsets", notes = "Get the atom level subsets", response = SubsetList.class)
  public SubsetList getAtomSubsets(
    @ApiParam(value = "Atom terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Atom terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /aui/" + terminology + "/" + version
            + "/subsets");
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "retrieve atom subsets",
          UserRole.VIEWER);

      SubsetList list =
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getConceptSubsets
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @GET
  @Path("/cui/subset/all/{terminology}/{version}")
  @ApiOperation(value = "Get concept subsets", notes = "Get the concept level subsets", response = SubsetList.class)
  public SubsetList getConceptSubsets(
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept terminology version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version
            + "/subsets");
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "retrieve concept subsets",
          UserRole.VIEWER);
      SubsetList list =
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findAtomSubsetMembers(java.lang.String, java.lang.String, java.lang.String,
   * java.lang.String, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Produces({
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
  })
  @Override
  @POST
  @Path("/aui/subset/{subsetId}/{terminology}/{version}/members/query/{query}")
  @ApiOperation(value = "Find atom subset members", notes = "Get the members for the indicated atom subset", response = SubsetMemberList.class)
  public SubsetMemberList findAtomSubsetMembers(
    @ApiParam(value = "Subset id, e.g. 341823003", required = true) @PathParam("subsetId") String subsetId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query;
    if (query == null || query.equals(ContentServiceRest.QUERY_BLANK)) {
      queryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /aui/subset/" + subsetId + "/" + terminology
            + "/" + version + "/members/query/" + queryStr);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find atom subset members",
          UserRole.VIEWER);

      SubsetMemberList list =
          contentService.findAtomSubsetMembers(subsetId, terminology, version,
              Branch.ROOT, queryStr, pfs);
      for (SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member : list
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findConceptSubsetMembers(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/cui/subset/{subsetId}/{terminology}/{version}/members/query/{query}")
  @ApiOperation(value = "Find concept subset members", notes = "Get the members for the indicated concept subset", response = SubsetMemberList.class)
  public SubsetMemberList findConceptSubsetMembers(
    @ApiParam(value = "Subset id, e.g. 341823003", required = true) @PathParam("subsetId") String subsetId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query;
    if (query == null || query.equals(ContentServiceRest.QUERY_BLANK)) {
      queryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/subset/" + subsetId + "/" + terminology
            + "/" + version + "/members/query/" + queryStr);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find concept subset members",
          UserRole.VIEWER);

      SubsetMemberList list =
          contentService.findConceptSubsetMembers(subsetId, terminology,
              version, Branch.ROOT, queryStr, pfs);
      for (SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member : list
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findTreesForConcept
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/trees")
  @ApiOperation(value = "Get trees with this terminologyId", notes = "Get the trees with the given concept id", response = TreeList.class)
  public TreeList findConceptTrees(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + terminologyId + "/trees");
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken,
          "retrieve trees for the concept ", UserRole.VIEWER);

      TreePositionList list =
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findTreesForDescriptor(java.lang.String, java.lang.String,
   * java.lang.String, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/{terminologyId}/trees/")
  @ApiOperation(value = "Get trees with this terminologyId", notes = "Get the trees with the given descriptor id", response = TreeList.class)
  public TreeList findDescriptorTrees(
    @ApiParam(value = "Descriptor terminology id, e.g. D002943", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor terminology version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version + "/"
            + terminologyId + "/trees");
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken,
          "retrieve trees for the descriptor ", UserRole.VIEWER);

      TreePositionList list =
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findTreesForCode
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/{terminologyId}/trees")
  @ApiOperation(value = "Get trees with this terminologyId", notes = "Get the trees with the given code id", response = TreeList.class)
  public TreeList findCodeTrees(
    @ApiParam(value = "Code terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Code terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /code/" + terminology + "/" + version + "/"
            + terminologyId + "/trees");
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "retrieve trees for the code",
          UserRole.VIEWER);

      TreePositionList list =
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findConceptTreeForQuery(java.lang.String, java.lang.String,
   * java.lang.String, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  @POST
  @Path("/cui/{terminology}/{version}/trees/query/{query}")
  @ApiOperation(value = "Find concept trees matching the query", notes = "Finds all merged trees matching the specified parameters", response = Tree.class)
  public Tree findConceptTreeForQuery(
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query search term, e.g. 'vitamin'", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query;
    if (query == null || query.equals(ContentServiceRest.QUERY_BLANK)) {
      queryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version
            + "/trees/query/" + query);
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find trees for the concept",
          UserRole.VIEWER);

      TreePositionList list =
          contentService.findConceptTreePositionsForQuery(terminology, version,
              Branch.ROOT, queryStr, pfs);

      // dummy variables for construction of artificial root
      Tree dummyTree = new TreeJpa();
      dummyTree.setTerminology(terminology);
      dummyTree.setVersion(version);
      dummyTree.setTerminologyId("dummy id");
      dummyTree.setName("Top");
      dummyTree.setTotalCount(list.getTotalCount());

      // initialize the return tree with dummy root and set total count
      Tree returnTree = new TreeJpa(dummyTree);

      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {

        // get tree for tree position
        final Tree tree = contentService.getTreeForTreePosition(treepos);

        // construct a new dummy-root tree for merging with existing tree
        Tree treeForTreePos = new TreeJpa(dummyTree);

        // add retrieved tree to dummy root level
        treeForTreePos.addChild(tree);

        // merge into the top-level dummy tree
        returnTree.mergeTree(treeForTreePos);
      }

      // if only one child, dummy root not necessary
      if (dummyTree.getChildren().size() == 1) {
        Tree tree = dummyTree.getChildren().get(0);
        return tree;
      }

      // otherwise return the populated dummy root tree
      return dummyTree;

    } catch (Exception e) {
      handleException(e, "trying to find trees for a query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findDescriptorTreeForQuery(java.lang.String, java.lang.String,
   * java.lang.String, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  @POST
  @Path("/dui/{terminology}/{version}/trees/query/{query}")
  @ApiOperation(value = "Find descriptor trees matching the query", notes = "Finds all merged trees matching the specified parameters", response = Tree.class)
  public Tree findDescriptorTreeForQuery(
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor terminology version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query search term, e.g. 'pneumonia'", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query;
    if (query == null || query.equals(ContentServiceRest.QUERY_BLANK)) {
      queryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /dui/" + terminology + "/" + version
            + "/trees/query/ + query");
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find trees for the descriptor",
          UserRole.VIEWER);

      TreePositionList list =
          contentService.findDescriptorTreePositionsForQuery(terminology,
              version, Branch.ROOT, queryStr, pfs);

      // dummy variables for construction of artificial root
      Tree dummyTree = new TreeJpa();
      dummyTree.setTerminology(terminology);
      dummyTree.setVersion(version);
      dummyTree.setName("Top");

      dummyTree = new TreeJpa();
      dummyTree.setTotalCount(list.getTotalCount());

      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {
        final Tree tree = contentService.getTreeForTreePosition(treepos);

        // construct a new dummy-root tree
        Tree treeForTreePos = new TreeJpa(dummyTree);
        treeForTreePos.addChild(tree);
        dummyTree.mergeTree(treeForTreePos);
      }

      // if only one child, dummy root not necessary
      if (dummyTree.getChildren().size() == 1) {

        Tree tree = dummyTree.getChildren().get(0);
        tree.setTotalCount(dummyTree.getTotalCount());
        return tree;
      }

      // otherwise return the populated dummy root tree
      return dummyTree;

    } catch (Exception e) {
      handleException(e, "trying to find trees for a query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findCodeTreeForQuery
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/code/{terminology}/{version}/trees/query/{query}")
  @ApiOperation(value = "Find code trees matching the query", notes = "Finds all merged trees matching the specified parameters", response = Tree.class)
  public Tree findCodeTreeForQuery(
    @ApiParam(value = "Code terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Codeterminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query search term, e.g. 'aspirin'", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query;
    if (query == null || query.equals(ContentServiceRest.QUERY_BLANK)) {
      queryStr = "";
    }
    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/"
            + "/trees/query/ + query");
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      TreePositionList list =
          contentService.findCodeTreePositionsForQuery(terminology, version,
              Branch.ROOT, queryStr, pfs);

      // dummy variables for construction of artificial root
      Tree dummyTree = new TreeJpa();
      dummyTree.setTerminology(terminology);
      dummyTree.setVersion(version);
      dummyTree.setName("Top");

      dummyTree = new TreeJpa();
      dummyTree.setTotalCount(list.getTotalCount());

      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {
        final Tree tree = contentService.getTreeForTreePosition(treepos);

        // construct a new dummy-root tree
        Tree treeForTreePos = new TreeJpa(dummyTree);
        treeForTreePos.addChild(tree);
        dummyTree.mergeTree(treeForTreePos);
      }

      // if only one child, dummy root not necessary
      if (dummyTree.getChildren().size() == 1) {

        Tree tree = dummyTree.getChildren().get(0);

        // set the count and total count
        tree.setTotalCount(dummyTree.getTotalCount());

        return tree;
      }

      // otherwise return the populated dummy root tree
      return dummyTree;

    } catch (Exception e) {
      handleException(e, "trying to find trees for a query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  @Override
  @POST
  @Path("/cui/{terminology}/{version}/{terminologyId}/trees/children")
  @ApiOperation(value = "Find children trees for a concept", notes = "Returns paged children trees for a concept. Note: not ancestorPath-sensitive", response = Tree.class)
  public TreeList findConceptTreeChildren(
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Concept terminologyId, e.g. C0000061", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /cui/" + terminology + "/" + version + "/" + terminologyId + "/"
            + "/trees/children");
    ContentService contentService = new ContentServiceJpa();
    try {
      authenticate(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      // get the child
      ConceptList childConcepts = contentService.findDescendantConcepts(terminologyId, terminology, version, true, Branch.ROOT, pfs);

      // instantiate child tree positions array
      TreePositionList childTreePositions = new TreePositionListJpa();
      
      // construct pfs parameter for tree position lookup, only need first one
      PfsParameter childPfs = new PfsParameterJpa();
      childPfs.setStartIndex(0);
      childPfs.setMaxResults(1);
      
      // get a tree position for each child, for child ct
      for (Concept childConcept : childConcepts.getObjects()) {
        TreePositionList tpList =
            contentService.findTreePositionsForConcept(childConcept.getTerminologyId(), childConcept.getTerminology(), childConcept.getVersion(), Branch.ROOT, childPfs);
     
        if (tpList.getCount() != 1)
          throw new Exception("Unexpected number of tree positions for concept " + terminologyId);
        
        childTreePositions.addObject(tpList.getObjects().get(0));
      }
      
      // the TreeList to return
      TreeList childTrees = new TreeListJpa();
      
      // for each tree position, construct a tree
      for (TreePosition<? extends ComponentHasAttributesAndName> childTreePosition : childTreePositions.getObjects()) {
        Tree childTree = new TreeJpa();
        childTree.setFromTreePosition(childTreePosition);
        childTrees.addObject(childTree);
      }
      
      return childTrees;
      
    } catch (Exception e) {
      handleException(e, "trying to find trees for a query");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  @Override
  public TreeList findDescriptorTreeChildren(String terminology,
    String version, String terminologyId, PfsParameterJpa pfs, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TreeList findCodeTreeChildren(String terminology, String version,
    String terminologyId, PfsParameterJpa pfs, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
