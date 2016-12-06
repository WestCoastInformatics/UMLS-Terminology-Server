/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import com.wci.umls.server.Project;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.helpers.NoteList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchResult;
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
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.ComponentInfoJpa;
import com.wci.umls.server.jpa.algo.ClamlLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.EclConceptIndexingAlgorithm;
import com.wci.umls.server.jpa.algo.LabelSetMarkedParentAlgorithm;
import com.wci.umls.server.jpa.algo.LuceneReindexAlgorithm;
import com.wci.umls.server.jpa.algo.OwlLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.RemoveTerminologyAlgorithm;
import com.wci.umls.server.jpa.algo.Rf2DeltaLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.Rf2FullLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.Rf2SnapshotLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.RrfLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.SimpleLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.TransitiveClosureAlgorithm;
import com.wci.umls.server.jpa.algo.TreePositionAlgorithm;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomNoteJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeNoteJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptNoteJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorNoteJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultJpa;
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
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.handlers.EclExpressionHandler;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.Code;
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
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.SecurityService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * Reference implementation of {@link ContentServiceRest}. Includes hibernate
 * tags for MEME database.
 */
@Path("/content")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Api(value = "/content")
@SwaggerDefinition(info = @Info(description = "Operations to retrieve content for a terminology.", title = "Content API", version = "1.0.1"))
public class ContentServiceRestImpl extends RootServiceRestImpl
    implements ContentServiceRest {

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
    Logger.getLogger(getClass())
        .info("RESTful call (Content): /reindex "
            + (indexedObjects == null ? "with no objects specified"
                : "with specified objects " + indexedObjects));

    // Track system level information
    long startTimeOrig = System.nanoTime();
    final LuceneReindexAlgorithm algo = new LuceneReindexAlgorithm();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "reindex", UserRole.ADMINISTRATOR);
      algo.setLastModifiedBy(userName);
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
  @GET
  @Path("/expression/count/{terminology}/{version}/{query}")
  @ApiOperation(value = "Returns count for a (presumably) valid EC query", notes = "Returns total count if the query can be parsed as an ECL expression, false if not")
  public Integer getEclExpressionResultCount(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "The expression to be checked", required = true) @PathParam("query") String query,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Content): /expression/count/" + terminology
            + "/version/" + query);

    try {
      authorizeApp(securityService, authToken, "create ECL indexes",
          UserRole.ADMINISTRATOR);
      EclExpressionHandler handler =
          new EclExpressionHandler(terminology, version);
      return handler.getCount(query);

    } catch (Exception e) {
      handleException(e, "checking query for expression syntax");
      return -1;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */

  @Override
  @GET
  @Path("/expression/query/{terminology}/{version}/{query}")
  @ApiOperation(value = "Returns results for EC query", notes = "Returns list of result terminology and hibernate ids as search results for an expression constraint query.")
  public SearchResultList getEclExpressionResults(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "The expression to be checked", required = true) @PathParam("query") String query,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Content): /expression/count/" + terminology
            + "/version/" + query);

    try {
      authorizeApp(securityService, authToken, "create ECL indexes",
          UserRole.ADMINISTRATOR);
      EclExpressionHandler handler =
          new EclExpressionHandler(terminology, version);
      return handler.resolve(query);

    } catch (Exception e) {
      handleException(e, "checking query for expression syntax");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/expression/index/{terminology}/{version}")
  @ApiOperation(value = "Computes expression indexes", notes = "Computes the indexes required for expression searches for a given terminology and version")
  public void computeExpressionIndexes(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /reindex/ecl/" + terminology + "/" + version);

    final ContentServiceJpa contentService = new ContentServiceJpa();

    // NOTE: Currently only ECL indexing supported
    final EclConceptIndexingAlgorithm algo = new EclConceptIndexingAlgorithm();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "create ECL indexes", UserRole.ADMINISTRATOR);
      algo.setLastModifiedBy(userName);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.compute();
      algo.close();

    } catch (Exception e) {
      handleException(e, "trying to create ECL indexes");
    } finally {
      algo.close();
      contentService.close();
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

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /terminology/closure/compute/"
            + terminology + "/" + version);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    final TransitiveClosureAlgorithm algo = new TransitiveClosureAlgorithm();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "compute transitive closure", UserRole.ADMINISTRATOR);
      algo.setLastModifiedBy(userName);

      // Compute transitive closure
      Logger.getLogger(getClass()).info(
          "  Compute transitive closure for  " + terminology + "/" + version);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setIdType(
          algo.getTerminology(terminology, version).getOrganizingClassType());
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

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /terminology/treepos/compute/"
            + terminology + "/" + version);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    final TreePositionAlgorithm algo = new TreePositionAlgorithm();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "compute tree positions ", UserRole.ADMINISTRATOR);
      algo.setLastModifiedBy(userName);

      // Compute tree positions
      Logger.getLogger(getClass())
          .info("  Compute tree positions for " + terminology + "/" + version);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setIdType(
          algo.getTerminology(terminology, version).getOrganizingClassType());
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
  @Path("/terminology/load/simple")
  @Consumes(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Load simple terminology from directory", notes = "Loads simple terminology from specified directory")
  public void loadTerminologySimple(
    @ApiParam(value = "Terminology, e.g. UMLS", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. latest", required = true) @QueryParam("version") String version,
    @ApiParam(value = "Input directory", required = true) String inputDir,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /terminology/load/simple " + terminology
            + ", " + version + " from input directory " + inputDir);

    // Track system level information
    ContentService contentService = null;
    SimpleLoaderAlgorithm algo = new SimpleLoaderAlgorithm();
    TransitiveClosureAlgorithm algo2 = null;
    TreePositionAlgorithm algo3 = null;
    try {
      final String userName = authorizeApp(securityService, authToken,
          "load simple", UserRole.ADMINISTRATOR);

      algo = new SimpleLoaderAlgorithm();
      algo.setLastModifiedBy(userName);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setInputPath(inputDir);
      algo.compute();
      algo.close();

      //
      // Compute transitive closure - CURRENTLY DISABLED
      //
      contentService = new ContentServiceJpa();
      final TerminologyList list =
          contentService.getTerminologyLatestVersions();
      // for (final Terminology t : list.getObjects()) {
      // // Only compute for organizing class types
      // if (t.getOrganizingClassType() != null) {
      // algo2 = new TransitiveClosureAlgorithm();
      // algo2.setLastModifiedBy(userName);
      // algo2.setTerminology(t.getTerminology());
      // algo2.setVersion(t.getVersion());
      // algo2.setIdType(t.getOrganizingClassType());
      // // some terminologies may have cycles, allow these for now.
      // algo2.setCycleTolerant(true);
      // algo2.compute();
      // algo2.close();
      // }
      // }

      //
      // Compute tree positions
      //
      for (final Terminology t : list.getObjects()) {
        // Only compute for organizing class types
        if (t.getOrganizingClassType() != null) {
          algo3 = new TreePositionAlgorithm();
          algo3.setLastModifiedBy(userName);
          algo3.setTerminology(t.getTerminology());
          algo3.setVersion(t.getVersion());
          algo3.setIdType(t.getOrganizingClassType());
          // some terminologies may have cycles, allow these for now.
          algo3.setCycleTolerant(true);
          // compute "semantic types" for concept hierarchies
          if (t.getOrganizingClassType() == IdType.CONCEPT) {
            algo3.setComputeSemanticType(true);
          }
          algo3.compute();
          algo3.close();
        }
      }

    } catch (Exception e) {
      handleException(e, "trying to load simple terminology from directory");
    } finally {
      algo.close();
      if (algo2 != null) {
        algo2.close();
      }
      algo3.close();

      contentService.close();
      securityService.close();
    }
  }

  /**
   * Load terminology rrf.
   *
   * @param terminology the terminology
   * @param version the version
   * @param singleMode the single mode
   * @param editMode the edit mode
   * @param codeFlag the code flag
   * @param prefix the prefix
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
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
    @ApiParam(value = "Edit mode, e.g. false", required = true) @QueryParam("editMode") Boolean editMode,
    @ApiParam(value = "Code flag, e.g. false", required = true) @QueryParam("codeFlag") Boolean codeFlag,
    @ApiParam(value = "Prefix, e.g. MR or RXN", required = false) @QueryParam("prefix") String prefix,
    @ApiParam(value = "RRF input directory", required = true) String inputDir,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /terminology/load/rrf/umls/" + terminology
            + "/" + version + " from input directory " + inputDir);

    // Track system level information
    ContentService contentService = null;
    RrfLoaderAlgorithm algo = new RrfLoaderAlgorithm();
    TransitiveClosureAlgorithm algo2 = null;
    TreePositionAlgorithm algo3 = null;
    LabelSetMarkedParentAlgorithm algo4 = null;
    try {
      final String userName = authorizeApp(securityService, authToken,
          "load rrf", UserRole.ADMINISTRATOR);

      algo = new RrfLoaderAlgorithm();
      algo.setLastModifiedBy(userName);
      algo.setSingleMode(singleMode);
      algo.setEditMode(editMode);
      algo.setCodesFlag(codeFlag);
      algo.setPrefix(prefix);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setInputPath(inputDir);
      algo.compute();
      algo.close();

      //
      // Compute transitive closure - CURRENTLY DISABLED
      //
      contentService = new ContentServiceJpa();
      final TerminologyList list =
          contentService.getTerminologyLatestVersions();
      // for (final Terminology t : list.getObjects()) {
      // // Only compute for organizing class types
      // if (t.getOrganizingClassType() != null) {
      // algo2 = new TransitiveClosureAlgorithm();
      // algo2.setLastModifiedBy(userName);
      // algo2.setTerminology(t.getTerminology());
      // algo2.setVersion(t.getVersion());
      // algo2.setIdType(t.getOrganizingClassType());
      // // some terminologies may have cycles, allow these for now.
      // algo2.setCycleTolerant(true);
      // algo2.compute();
      // algo2.close();
      // }
      // }

      //
      // Compute tree positions
      // Refresh caches after metadata has changed in loader
      for (final Terminology t : list.getObjects()) {
        // Only compute for organizing class types
        if (t.getOrganizingClassType() != null) {
          algo3 = new TreePositionAlgorithm();
          algo3.setLastModifiedBy(userName);
          algo3.setTerminology(t.getTerminology());
          algo3.setVersion(t.getVersion());
          algo3.setIdType(t.getOrganizingClassType());
          // some terminologies may have cycles, allow these for now.
          algo3.setCycleTolerant(true);
          // compute "semantic types" for concept hierarchies
          if (t.getOrganizingClassType() == IdType.CONCEPT) {
            algo3.setComputeSemanticType(!editMode);
          }
          algo3.compute();
          algo3.close();

          // Also try "atom" just in case the organizing class
          // type doesn't have PAR/CHD - this handles legacy terminologies
          algo3 = new TreePositionAlgorithm();
          algo3.setLastModifiedBy(userName);
          algo3.setTerminology(t.getTerminology());
          algo3.setVersion(t.getVersion());
          algo3.setIdType(IdType.ATOM);
          // some terminologies may have cycles, allow these for now.
          algo3.setCycleTolerant(true);
          algo3.compute();
          algo3.close();
        }
      }

      // Compute label sets - after transitive closure
      // for each subset, compute the label set
      for (final Terminology t : list.getObjects()) {
        for (final Subset subset : contentService
            .getConceptSubsets(t.getTerminology(), t.getVersion(), Branch.ROOT)
            .getObjects()) {
          final ConceptSubset conceptSubset = (ConceptSubset) subset;
          if (conceptSubset.isLabelSubset()) {
            Logger.getLogger(getClass())
                .info("  Create label set for subset = " + subset);
            algo4 = new LabelSetMarkedParentAlgorithm();
            algo4.setLastModifiedBy(userName);
            algo4.setTerminology(t.getTerminology());
            algo4.setVersion(t.getVersion());
            algo4.setSubset(conceptSubset);
            algo4.compute();
            algo4.close();
          }
        }
      }

    } catch (

    Exception e) {
      handleException(e, "trying to load terminology from RRF directory");
    } finally {
      algo.close();
      if (algo2 != null) {
        algo2.close();
      }
      if (algo3 != null) {
        algo3.close();
      }
      if (algo4 != null) {
        algo4.close();
      }
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

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /terminology/load/rf2/delta/"
            + terminology + " from input directory " + inputDir);

    // Track system level information
    ContentService contentService = null;
    Rf2DeltaLoaderAlgorithm algo = null;
    TransitiveClosureAlgorithm algo2 = null;
    LabelSetMarkedParentAlgorithm algo3 = null;
    TreePositionAlgorithm algo4 = null;
    EclConceptIndexingAlgorithm algo5 = null;
    try {
      final String userName = authorizeApp(securityService, authToken,
          "load delta", UserRole.ADMINISTRATOR);

      algo = new Rf2DeltaLoaderAlgorithm();
      algo.setLastModifiedBy(userName);
      String version = algo.getLatestVersion(terminology);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setInputPath(inputDir);
      algo.compute();
      algo.close();

      //
      // Compute transitive closure - CURRENTLY DISABLED
      //
      // algo2 = new TransitiveClosureAlgorithm();
      // algo2.setLastModifiedBy(userName);
      // algo2.setCycleTolerant(false);
      // algo2.setIdType(IdType.CONCEPT);
      // algo2.setTerminology(terminology);
      // algo2.setVersion(terminology);
      // algo2.reset();
      // algo2.compute();
      // algo2.close();

      // Compute label sets - after transitive closure
      // for each subset, compute the label set
      contentService = new ContentServiceJpa();
      for (final Subset subset : getConceptSubsets(terminology, version,
          Branch.ROOT).getObjects()) {
        final ConceptSubset conceptSubset = (ConceptSubset) subset;
        if (conceptSubset.isLabelSubset()) {
          Logger.getLogger(getClass())
              .info("  Create label set for subset = " + subset);
          algo3 = new LabelSetMarkedParentAlgorithm();
          algo3.setLastModifiedBy(userName);
          algo3.setTerminology(terminology);
          algo3.setVersion(version);
          algo3.setSubset(conceptSubset);
          algo3.compute();
          algo3.close();
        }
      }

      // Tree positions
      algo4 = new TreePositionAlgorithm();
      algo4.setLastModifiedBy(userName);
      algo4.setCycleTolerant(false);
      algo4.setIdType(IdType.CONCEPT);
      // some terminologies may have cycles, allow these for now.
      algo4.setCycleTolerant(true);
      algo4.setComputeSemanticType(true);
      algo4.setTerminology(terminology);
      algo4.setVersion(version);
      algo4.reset();
      algo4.compute();
      algo4.close();

      // Expressions
      algo5 = new EclConceptIndexingAlgorithm();
      algo5.setLastModifiedBy(userName);
      algo5.setTerminology(terminology);
      algo5.setVersion(version);
      algo5.compute();
      algo5.close();

    } catch (Exception e) {
      handleException(e, "trying to load terminology delta from RF2 directory");
    } finally {
      algo.close();
      if (algo2 != null) {
        algo2.close();
      }
      algo3.close();
      algo4.close();
      algo5.close();
      contentService.close();
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
        .info("RESTful call (Content): /terminology/load/rf2/snapshot/"
            + terminology + "/" + version + " from input directory "
            + inputDir);

    // Track system level information
    ContentService contentService = null;
    Rf2SnapshotLoaderAlgorithm algo = null;
    TransitiveClosureAlgorithm algo2 = null;
    LabelSetMarkedParentAlgorithm algo3 = null;
    TreePositionAlgorithm algo4 = null;
    EclConceptIndexingAlgorithm algo5 = null;
    try {
      final String userName = authorizeApp(securityService, authToken,
          "load snapshot", UserRole.ADMINISTRATOR);

      algo = new Rf2SnapshotLoaderAlgorithm();
      algo.setLastModifiedBy(userName);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setInputPath(inputDir);
      algo.compute();
      algo.close();

      //
      // Compute transitive closure - CURRENTLY DISABLED
      //
      // algo2 = new TransitiveClosureAlgorithm();
      // algo2.setLastModifiedBy(userName);
      // algo2.setCycleTolerant(false);
      // algo2.setIdType(IdType.CONCEPT);
      // algo2.setTerminology(terminology);
      // algo2.setVersion(version);
      // algo2.reset();
      // algo2.compute();
      // algo2.close();

      // Compute label sets - after transitive closure
      // for each subset, compute the label set
      contentService = new ContentServiceJpa();
      for (final Subset subset : contentService
          .getConceptSubsets(terminology, version, Branch.ROOT).getObjects()) {
        final ConceptSubset conceptSubset = (ConceptSubset) subset;
        if (conceptSubset.isLabelSubset()) {
          algo3 = new LabelSetMarkedParentAlgorithm();
          algo3.setLastModifiedBy(userName);
          algo3.setTerminology(terminology);
          algo3.setVersion(version);
          algo3.setSubset(conceptSubset);
          algo3.compute();
          algo3.close();
        }
      }

      // Tree positions
      algo4 = new TreePositionAlgorithm();
      algo4.setLastModifiedBy(userName);
      algo4.setCycleTolerant(false);
      algo4.setIdType(IdType.CONCEPT);
      // some terminologies may have cycles, allow these for now.
      algo4.setCycleTolerant(true);
      algo4.setComputeSemanticType(true);
      algo4.setTerminology(terminology);
      algo4.setVersion(version);
      algo4.reset();
      algo4.compute();
      algo4.close();

      // expressions
      algo5 = new EclConceptIndexingAlgorithm();
      algo5.setLastModifiedBy(userName);
      algo5.setTerminology(terminology);
      algo5.setVersion(version);
      algo5.compute();
      algo5.close();

    } catch (Exception e) {
      handleException(e,
          "trying to load terminology snapshot from RF2 directory");
    } finally {
      algo.close();
      if (algo2 != null) {
        algo2.close();
      }
      if (algo3 != null) {
        algo3.close();
      }
      algo4.close();
      algo5.close();
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */

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

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /terminology/load/rf2/full/" + terminology
            + "/" + version + " from input directory " + inputDir);

    // Track system level information
    ContentService contentService = null;
    Rf2FullLoaderAlgorithm algo = null;
    TransitiveClosureAlgorithm algo2 = null;
    LabelSetMarkedParentAlgorithm algo3 = null;
    TreePositionAlgorithm algo4 = null;
    EclConceptIndexingAlgorithm algo5 = null;
    try {
      final String userName = authorizeApp(securityService, authToken,
          "load full", UserRole.ADMINISTRATOR);

      algo = new Rf2FullLoaderAlgorithm();
      algo.setLastModifiedBy(userName);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setInputPath(inputDir);
      algo.compute();
      algo.close();

      //
      // Compute transitive closure - CURRENTLY DISABLED
      //
      // algo2 = new TransitiveClosureAlgorithm();
      // algo2.setLastModifiedBy(userName);
      // algo2.setCycleTolerant(false);
      // algo2.setIdType(IdType.CONCEPT);
      // algo2.setTerminology(terminology);
      // algo2.setVersion(version);
      // algo2.reset();
      // algo2.compute();
      // algo2.close();

      // Compute label sets - after transitive closure
      // for each subset, compute the label set
      contentService = new ContentServiceJpa();
      for (final Subset subset : contentService
          .getConceptSubsets(terminology, version, Branch.ROOT).getObjects()) {
        final ConceptSubset conceptSubset = (ConceptSubset) subset;
        if (conceptSubset.isLabelSubset()) {
          algo3 = new LabelSetMarkedParentAlgorithm();
          algo3.setLastModifiedBy(userName);
          algo3.setTerminology(terminology);
          algo3.setVersion(version);
          algo3.setSubset(conceptSubset);
          algo3.compute();
          algo3.close();
        }
      }

      // Tree positions
      algo4 = new TreePositionAlgorithm();
      algo4.setLastModifiedBy(userName);
      algo4.setCycleTolerant(false);
      algo4.setIdType(IdType.CONCEPT);
      // some terminologies may have cycles, allow these for now.
      algo4.setCycleTolerant(true);
      algo4.setComputeSemanticType(true);
      algo4.setTerminology(terminology);
      algo4.setVersion(version);
      algo4.reset();
      algo4.compute();
      algo4.close();

      // expressions
      algo5 = new EclConceptIndexingAlgorithm();
      algo5.setLastModifiedBy(userName);
      algo5.setTerminology(terminology);
      algo5.setVersion(version);
      algo5.compute();
      algo5.close();

    } catch (Exception e) {
      handleException(e, "trying to load terminology full from RF2 directory");
    } finally {
      algo.close();
      if (algo2 != null) {
        algo2.close();
      }
      algo3.close();
      algo4.close();
      algo5.close();
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

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /terminology/load/claml/" + terminology
            + "/" + version + " from input file " + inputFile);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    ClamlLoaderAlgorithm algo = null;
    TransitiveClosureAlgorithm algo2 = null;
    TreePositionAlgorithm algo3 = null;
    try {
      final String userName = authorizeApp(securityService, authToken,
          "loading claml", UserRole.ADMINISTRATOR);

      // TODO: this really has the hallmarks of a "process"

      // Load data
      Logger.getLogger(getClass()).info("Load ClaML data from " + inputFile);
      algo = new ClamlLoaderAlgorithm();
      algo.setLastModifiedBy(userName);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setInputPath(inputFile);
      algo.compute();
      algo.close();

      //
      // Compute transitive closure - CURRENTLY DISABLED
      //
      // Logger.getLogger(getClass()).info("Start computing transtive closure");
      // algo2 = new TransitiveClosureAlgorithm();
      // algo2.setLastModifiedBy(userName);
      // algo2.setIdType(IdType.CONCEPT);
      // algo2.setCycleTolerant(false);
      // algo2.setTerminology(terminology);
      // algo2.setVersion(version);
      // algo2.compute();
      // algo2.close();

      // compute tree positions
      algo3 = new TreePositionAlgorithm();
      algo3.setLastModifiedBy(userName);
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
      if (algo2 != null) {
        algo2.close();
      }
      algo3.close();
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

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /terminology/load/owl/" + terminology
            + "/" + version + " from input file " + inputFile);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    OwlLoaderAlgorithm algo = null;
    TransitiveClosureAlgorithm algo2 = null;
    TreePositionAlgorithm algo3 = null;
    try {
      final String userName = authorizeApp(securityService, authToken,
          "loading owl", UserRole.ADMINISTRATOR);

      // Load snapshot
      Logger.getLogger(getClass()).info("Load Owl data from " + inputFile);
      algo = new OwlLoaderAlgorithm();
      algo.setLastModifiedBy(userName);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.setInputPath(inputFile);
      algo.compute();
      algo.close();

      //
      // Compute transitive closure - CURRENTLY DISABLED
      //
      // Logger.getLogger(getClass()).info("Start computing transtive closure");
      // algo2 = new TransitiveClosureAlgorithm();
      // algo2.setLastModifiedBy(userName);
      // algo2.setIdType(IdType.CONCEPT);
      // algo2.setCycleTolerant(false);
      // algo2.setTerminology(terminology);
      // algo2.setVersion(version);
      // algo2.compute();
      // algo2.close();

      // compute tree positions
      algo3 = new TreePositionAlgorithm();
      algo3.setLastModifiedBy(userName);
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
      if (algo2 != null) {
        algo2.close();
      }
      algo3.close();
      securityService.close();
    }
  }

  /* see superclass */

  @Override
  @DELETE
  @Path("/terminology/{terminology}/{version}")
  @ApiOperation(value = "Remove a terminology", notes = "Removes all elements for a specified terminology and version")
  public boolean removeTerminology(
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /terminology/" + terminology + "/" + version);

    // Track system level information
    long startTimeOrig = System.nanoTime();

    final RemoveTerminologyAlgorithm algo = new RemoveTerminologyAlgorithm();
    try {
      String userName = authorizeApp(securityService, authToken,
          "remove terminology", UserRole.ADMINISTRATOR);

      // Remove terminology
      Logger.getLogger(getClass())
          .info("  Remove terminology for  " + terminology + "/" + version);
      algo.setLastModifiedBy(userName);
      algo.setTerminology(terminology);
      algo.setVersion(version);
      algo.compute();
      algo.close();

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

      securityService.addLogEntry(userName, terminology, version, null,
          "REMOVER", "Remove terminology");

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
  @Path("/concept/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get concept by id, terminology, and version", notes = "Get the root branch concept matching the specified parameters", response = ConceptJpa.class)
  public Concept getConcept(
    @ApiParam(value = "Concept terminology id, e.g. C0000039", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /concept/"
        + terminology + "/" + version + "/" + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      String userName = authorizeApp(securityService, authToken,
          "retrieve the concept", UserRole.VIEWER);

      final Concept concept = contentService.getConcept(terminologyId,
          terminology, version, Branch.ROOT);
      final Project project =
          projectId == null ? null : contentService.getProject(projectId);

      if (concept != null) {
        contentService.getGraphResolutionHandler(terminology).resolve(concept);
        sortAtoms(securityService, contentService, userName, concept, project);
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
  @Path("/concept/{conceptId}")
  @ApiOperation(value = "Get concept by concept id", notes = "Get the root branch concept matching the specified parameters", response = ConceptJpa.class)
  public Concept getConcept(
    @ApiParam(value = "Concept id, e.g. 2145", required = true) @PathParam("conceptId") Long conceptId,
    @ApiParam(value = "Project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /concept/" + conceptId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "retrieve the concept", UserRole.VIEWER);
      final Concept concept = contentService.getConcept(conceptId);

      final Project project =
          projectId == null ? null : contentService.getProject(projectId);

      if (concept != null) {
        final String terminology = concept.getTerminology();
        contentService.getGraphResolutionHandler(terminology).resolve(concept);
        sortAtoms(securityService, contentService, userName, concept, project);
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

  @Override
  @GET
  @Path("/atom/{atomId}")
  @ApiOperation(value = "Get atom by atom id", notes = "Get the root branch atom matching the specified parameters", response = AtomJpa.class)
  public Atom getAtom(
    @ApiParam(value = "Atom id, e.g. 2145", required = true) @PathParam("atomId") Long atomId,
    @ApiParam(value = "Project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /atom/" + atomId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the atom",
          UserRole.VIEWER);
      final Atom atom = contentService.getAtom(atomId);

      if (atom != null) {
        final String terminology = atom.getTerminology();
        contentService.getGraphResolutionHandler(terminology).resolve(atom);
      }
      return atom;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a atom");
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

    Logger.getLogger(getClass()).info("RESTful call (Content): /mapset/"
        + terminology + "/" + version + "/" + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the mapSet",
          UserRole.VIEWER);

      final MapSet mapSet = contentService.getMapSet(terminologyId, terminology,
          version, Branch.ROOT);

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
      for (int i = 0; i < list.size(); i++) {
        contentService.getGraphResolutionHandler(terminology)
            .resolve(list.getObjects().get(i));
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
  @Path("/concept/{terminology}/{version}")
  @ApiOperation(value = "Find concepts matching a search query", notes = "Gets a list of search results that match the lucene query for the root branch", response = SearchResultListJpa.class)
  public SearchResultList findConcepts(
    @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'aspirin'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFSC Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /concept/" + terminology + "/" + version
            + "?query=" + queryStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find concepts by query",
          UserRole.VIEWER);

      // Empty queries return all results
      final SearchResultList sr = contentService.findConcepts(terminology,
          version, Branch.ROOT, queryStr, pfs);
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
  @Path("/concept")
  @ApiOperation(value = "Find concepts matching a lucene or jql search query", notes = "Gets a list of search results that match the lucene or jql query for the root branch", response = SearchResultListJpa.class)
  public SearchResultList findConceptsForGeneralQuery(
    @ApiParam(value = "Lucene Query", required = true) @QueryParam("query") String query,
    @ApiParam(value = "JQL Query", required = true) @QueryParam("jql") String jql,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;
    final String jqlStr = jql == null ? "" : jql;

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /concept?" + "query=" + queryStr
            + "&jql=" + jqlStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find concepts by query",
          UserRole.VIEWER);

      final SearchResultList sr = contentService
          .findConceptsForGeneralQuery(queryStr, jqlStr, Branch.ROOT, pfs);
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
    Logger.getLogger(getClass())
        .info("RESTful call (Content): /code?" + "query=" + queryStr + "&jql="
            + jqlStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find codes by query",
          UserRole.VIEWER);

      final SearchResultList sr = contentService
          .findCodesForGeneralQuery(queryStr, jqlStr, Branch.ROOT, pfs);
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
  @Path("/concept/{terminology}/{version}/autocomplete/{searchTerm}")
  @ApiOperation(value = "Find autocomplete matches for concept searches", notes = "Gets a list of search autocomplete matches for the specified search term", response = StringList.class)
  public StringList autocompleteConcepts(
    @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Search term, e.g. 'sul'", required = true) @PathParam("searchTerm") String searchTerm,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /concept/"
        + terminology + "/" + version + "/autocomplete/" + searchTerm);
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
  @Path("/descriptor/{terminology}/{version}/{terminologyId}")
  @ApiOperation(value = "Get descriptor by id, terminology, and version", notes = "Get the root branch descriptor matching the specified parameters", response = DescriptorJpa.class)
  public Descriptor getDescriptor(
    @ApiParam(value = "Descriptor terminology id, e.g. D003933", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /descriptor/"
        + terminology + "/" + version + "/" + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      String userName = authorizeApp(securityService, authToken,
          "retrieve the descriptor", UserRole.VIEWER);

      final Descriptor descriptor = contentService.getDescriptor(terminologyId,
          terminology, version, Branch.ROOT);
      final Project project =
          projectId == null ? null : contentService.getProject(projectId);

      if (descriptor != null) {
        contentService.getGraphResolutionHandler(terminology)
            .resolve(descriptor);
        sortAtoms(securityService, contentService, userName, descriptor,
            project);
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
  @Path("/descriptor/{terminology}/{version}")
  @ApiOperation(value = "Find descriptors matching a search query", notes = "Gets a list of search results that match the lucene query for the root branch", response = SearchResultListJpa.class)
  public SearchResultList findDescriptors(
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'aspirin'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFSC Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /descriptor/" + terminology + "/"
            + version + "?query=" + queryStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find descriptors by query",
          UserRole.VIEWER);

      // Empty queries return all results
      final SearchResultList sr = contentService.findDescriptors(terminology,
          version, Branch.ROOT, queryStr, pfs);
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
  @Path("/descriptor/")
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

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /descriptor" + "?query=" + queryStr
            + "&jql=" + jqlStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find concepts by query",
          UserRole.VIEWER);

      final SearchResultList sr = contentService
          .findDescriptorsForGeneralQuery(queryStr, jqlStr, Branch.ROOT, pfs);
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
  @Path("/descriptor/{terminology}/{version}/autocomplete/{searchTerm}")
  @ApiOperation(value = "Find autocomplete matches for descriptor searches", notes = "Gets a list of search autocomplete matches for the specified search term", response = StringList.class)
  public StringList autocompleteDescriptors(
    @ApiParam(value = "Terminology, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Search term, e.g. 'sul'", required = true) @PathParam("searchTerm") String searchTerm,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /descriptor/"
        + terminology + "/" + version + "/autocomplete/" + searchTerm);
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
    @ApiParam(value = "Project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /code/"
        + terminology + "/" + version + "/" + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      String userName = authorizeApp(securityService, authToken,
          "retrieve the code", UserRole.VIEWER);

      final Code code = contentService.getCode(terminologyId, terminology,
          version, Branch.ROOT);
      final Project project =
          projectId == null ? null : contentService.getProject(projectId);

      if (code != null) {
        contentService.getGraphResolutionHandler(terminology).resolve(code);
        sortAtoms(securityService, contentService, userName, code, project);
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
  public SearchResultList findCodes(
    @ApiParam(value = "Code terminology name, e.g. MTH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code version, e.g. 2014AB", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'aspirin'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFSC Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /code/" + terminology + "/" + version
            + "?query=" + queryStr + " with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find codes by query",
          UserRole.VIEWER);

      // Empty queries returns all results
      final SearchResultList sr = contentService.findCodes(terminology, version,
          Branch.ROOT, queryStr, pfs);
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

    Logger.getLogger(getClass()).info("RESTful call (Content): /code/"
        + terminology + "/" + version + "/autocomplete/" + searchTerm);
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
    @ApiParam(value = "Project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /lui/"
        + terminology + "/" + version + "/" + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      String userName = authorizeApp(securityService, authToken,
          "retrieve the lexical class", UserRole.VIEWER);

      final LexicalClass lexicalClass = contentService
          .getLexicalClass(terminologyId, terminology, version, Branch.ROOT);
      final Project project =
          projectId == null ? null : contentService.getProject(projectId);

      if (lexicalClass != null) {
        contentService.getGraphResolutionHandler(terminology)
            .resolve(lexicalClass);
        sortAtoms(securityService, contentService, userName, lexicalClass,
            project);
      }
      return lexicalClass;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a lexicalClass");
      return null;
    } finally {
      contentService.close();
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
    @ApiParam(value = "Project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /sui/"
        + terminology + "/" + version + "/" + terminologyId);
    final ContentService contentService = new ContentServiceJpa();
    try {
      String userName = authorizeApp(securityService, authToken,
          "retrieve the string class", UserRole.VIEWER);

      final StringClass stringClass = contentService
          .getStringClass(terminologyId, terminology, version, Branch.ROOT);
      final Project project =
          projectId == null ? null : contentService.getProject(projectId);

      if (stringClass != null) {
        contentService.getGraphResolutionHandler(terminology)
            .resolve(stringClass);
        sortAtoms(securityService, contentService, userName, stringClass,
            project);
      }
      return stringClass;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a stringClass");
      return null;
    } finally {
      contentService.close();
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/concept/{terminology}/{version}/{terminologyId}/ancestors/{parentsOnly}")
  @ApiOperation(value = "Find ancestor concepts", notes = "Gets a list of ancestor concepts", response = ConceptListJpa.class)
  public ConceptList findAncestorConcepts(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("parentsOnly") boolean parentsOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /concept/" + terminology + "/" + version
            + "/" + terminologyId + "/ancestors with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find ancestor concepts",
          UserRole.VIEWER);

      final ConceptList list = contentService.findAncestorConcepts(
          terminologyId, terminology, version, parentsOnly, Branch.ROOT, pfs);

      for (final Concept concept : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology)
            .resolveEmpty(concept);
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
  @Path("/concept/{terminology}/{version}/{terminologyId}/descendants/{childrenOnly}")
  @ApiOperation(value = "Find descendant concepts", notes = "Gets a list of descendant concepts", response = ConceptListJpa.class)
  public ConceptList findDescendantConcepts(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("childrenOnly") boolean childrenOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /concept/" + terminology + "/" + version
            + "/" + terminologyId + "/descendants with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find descendant concepts",
          UserRole.VIEWER);

      final ConceptList list = contentService.findDescendantConcepts(
          terminologyId, terminology, version, childrenOnly, Branch.ROOT, pfs);

      for (final Concept concept : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology)
            .resolveEmpty(concept);
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
  @Path("/descriptor/{terminology}/{version}/{terminologyId}/ancestors/{parentsOnly}")
  @ApiOperation(value = "Find ancestor descriptors", notes = "Gets a list of ancestor descriptors", response = DescriptorListJpa.class)
  public DescriptorList findAncestorDescriptors(
    @ApiParam(value = "Descriptor terminology id, e.g. D003423", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("parentsOnly") boolean parentsOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /descriptor/" + terminology + "/"
            + version + terminologyId + "/ancestors with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find ancestor descriptors",
          UserRole.VIEWER);

      final DescriptorList list = contentService.findAncestorDescriptors(
          terminologyId, terminology, version, parentsOnly, Branch.ROOT, pfs);

      for (final Descriptor descriptor : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology)
            .resolveEmpty(descriptor);
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
  @Path("/descriptor/{terminology}/{version}/{terminologyId}/descendants/{childrenOnly}")
  @ApiOperation(value = "Find descendant descriptors", notes = "Gets a list of descendant descriptors", response = DescriptorListJpa.class)
  public DescriptorList findDescendantDescriptors(
    @ApiParam(value = "Descriptor terminology id, e.g. D002342", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Children only flag, e.g. true", required = true) @PathParam("childrenOnly") boolean childrenOnly,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /descriptor/" + terminology + "/"
            + version + terminologyId + "/descendants with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find descendant descriptors",
          UserRole.VIEWER);

      final DescriptorList list = contentService.findDescendantDescriptors(
          terminologyId, terminology, version, childrenOnly, Branch.ROOT, pfs);

      for (final Descriptor descriptor : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology)
            .resolveEmpty(descriptor);
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

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /code/" + terminology + "/" + version
            + terminologyId + "/ancestors with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find ancestor codes",
          UserRole.VIEWER);
      final CodeList list = contentService.findAncestorCodes(terminologyId,
          terminology, version, parentsOnly, Branch.ROOT, pfs);

      for (final Code code : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology)
            .resolveEmpty(code);
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

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /code/" + terminology + "/" + version
            + terminologyId + "/descendants with PFS parameter "
            + (pfs == null ? "empty" : pfs.toString()));
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find descendant codes",
          UserRole.VIEWER);

      final CodeList list = contentService.findDescendantCodes(terminologyId,
          terminology, version, childrenOnly, Branch.ROOT, pfs);

      for (final Code code : list.getObjects()) {
        contentService.getGraphResolutionHandler(terminology)
            .resolveEmpty(code);
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
  @Path("/concept/{terminology}/{version}/{terminologyId}/members")
  @ApiOperation(value = "Get subset members with this terminologyId", notes = "Get the subset members with the given concept id", response = SubsetMemberListJpa.class)
  public SubsetMemberList getConceptSubsetMembers(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /concept/"
        + terminology + "/" + version + "/" + terminologyId + "/members");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve subset members for the concept", UserRole.VIEWER);

      final SubsetMemberList list = contentService.getConceptSubsetMembers(
          terminologyId, terminology, version, Branch.ROOT);

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
  public SubsetMemberList getAtomSubsetMembers(
    @ApiParam(value = "Atom terminology id, e.g. 102751015", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Atom terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Atom version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /aui/"
        + terminology + "/" + version + "/" + terminologyId + "/members");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve subset members for the atom", UserRole.VIEWER);

      final SubsetMemberList list = contentService.getAtomSubsetMembers(
          terminologyId, terminology, version, Branch.ROOT);

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
  @Path("/concept/{terminology}/{version}/{terminologyId}/relationships")
  @ApiOperation(value = "Get relationships with this terminologyId", notes = "Get the relationships with the given concept id", response = RelationshipListJpa.class)
  public RelationshipList findConceptRelationships(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /concept/" + terminology + "/" + version
            + "/" + terminologyId + "/relationships?query=" + query);
    final String queryStr = query == null ? "" : query;

    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve relationships for the concept", UserRole.VIEWER);

      final RelationshipList list =
          contentService.findConceptRelationships(terminologyId, terminology,
              version, Branch.ROOT, queryStr, false, pfs);

      // Use graph resolver
      for (final Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : list
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
  @Path("/concept/{terminology}/{version}/{terminologyId}/relationships/deep")
  @ApiOperation(value = "Get deep relationships with this terminologyId", notes = "Get the relationships for the concept and also for any other atoms, concepts, descirptors, or codes in its graph for the specified concept id", response = RelationshipListJpa.class)
  public RelationshipList findConceptDeepRelationships(
    @ApiParam(value = "Concept terminology id, e.g. C0000039", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Inverse flag", required = false) @QueryParam("inverseFlag") boolean inverseFlag,
    @ApiParam(value = "Include concept rels", required = false) @QueryParam("includeConceptRels") boolean includeConceptRels,
    @ApiParam(value = "Preferred only", required = false) @QueryParam("preferredOnly") boolean preferredOnly,
    @ApiParam(value = "Include self referential", required = false) @QueryParam("includeSelfReferential") boolean includeSelfReferential,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @QueryParam("query") String query,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /concept/" + terminology + "/" + version
            + "/" + terminologyId + "/relationships/deep with query: " + query);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve deep relationships for the concept", UserRole.VIEWER);

      return contentService.findConceptDeepRelationships(terminologyId,
          terminology, version, Branch.ROOT, query, inverseFlag,
          includeConceptRels, preferredOnly, includeSelfReferential, pfs);

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
  @Path("/descriptor/{terminology}/{version}/{terminologyId}/relationships")
  @ApiOperation(value = "Get relationships with this terminologyId", notes = "Get the relationships with the given descriptor id", response = RelationshipListJpa.class)
  public RelationshipList findDescriptorRelationships(
    @ApiParam(value = "Descriptor terminology id, e.g. D042033", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info(
        "RESTful call (Content): /descriptor/" + terminology + "/" + version
            + "/" + terminologyId + "/relationships?query=" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve relationships for the descriptor", UserRole.VIEWER);

      final RelationshipList list =
          contentService.findDescriptorRelationships(terminologyId, terminology,
              version, Branch.ROOT, queryStr, false, pfs);

      // Use graph resolver
      for (final Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : list
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
  public RelationshipList findCodeRelationships(
    @ApiParam(value = "Code terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Code terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /code/" + terminology + "/" + version
            + "/" + terminologyId + "/relationships?query=" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve relationships for the code", UserRole.VIEWER);

      final RelationshipList list =
          contentService.findCodeRelationships(terminologyId, terminology,
              version, Branch.ROOT, queryStr, false, pfs);

      for (final Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : list
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

    Logger.getLogger(getClass()).info("RESTful call (Content): /aui/"
        + terminology + "/" + version + "/subsets");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve atom subsets",
          UserRole.VIEWER);

      final SubsetList list =
          contentService.getAtomSubsets(terminology, version, Branch.ROOT);
      for (int i = 0; i < list.size(); i++) {
        contentService.getGraphResolutionHandler(terminology)
            .resolve(list.getObjects().get(i));
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
  @Path("/concept/subset/all/{terminology}/{version}")
  @ApiOperation(value = "Get concept subsets", notes = "Get the concept level subsets", response = SubsetListJpa.class)
  public SubsetList getConceptSubsets(
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /concept/"
        + terminology + "/" + version + "/subsets");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve concept subsets",
          UserRole.VIEWER);
      final SubsetList list =
          contentService.getConceptSubsets(terminology, version, Branch.ROOT);
      for (int i = 0; i < list.size(); i++) {
        contentService.getGraphResolutionHandler(terminology)
            .resolve(list.getObjects().get(i));
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

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /aui/subset/" + subsetId + "/"
            + terminology + "/" + version + "/members?query=" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find atom subset members",
          UserRole.VIEWER);

      final SubsetMemberList list = contentService.findAtomSubsetMembers(
          subsetId, terminology, version, Branch.ROOT, queryStr, pfs);
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
  @Path("/concept/subset/{subsetId}/{terminology}/{version}/members")
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

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /concept/subset/" + subsetId + "/"
            + terminology + "/" + version + "/members?query=" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find concept subset members",
          UserRole.VIEWER);

      final SubsetMemberList list = contentService.findConceptSubsetMembers(
          subsetId, terminology, version, Branch.ROOT, queryStr, pfs);
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
  @Path("/concept/{terminology}/{version}/{terminologyId}/trees")
  @ApiOperation(value = "Get trees with this terminologyId", notes = "Get the trees with the given concept id", response = TreeListJpa.class)
  public TreeList findConceptTrees(
    @ApiParam(value = "Concept terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /concept/"
        + terminology + "/" + version + "/" + terminologyId + "/trees");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve trees for the concept ", UserRole.VIEWER);

      final TreePositionList list = contentService.findConceptTreePositions(
          terminologyId, terminology, version, Branch.ROOT, null, pfs);

      final TreeList treeList = new TreeListJpa();
      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {
        final Tree tree = contentService.getTreeForTreePosition(treepos);
        treeList.getObjects().add(tree);
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
  @Path("/descriptor/{terminology}/{version}/{terminologyId}/trees/")
  @ApiOperation(value = "Get trees with this terminologyId", notes = "Get the trees with the given descriptor id", response = TreeListJpa.class)
  public TreeList findDescriptorTrees(
    @ApiParam(value = "Descriptor terminology id, e.g. D002943", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Descriptor terminology name, e.g. MSH", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2015_2014_09_08", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Content): /descriptor/"
        + terminology + "/" + version + "/" + terminologyId + "/trees");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve trees for the descriptor ", UserRole.VIEWER);

      final TreePositionList list = contentService.findDescriptorTreePositions(
          terminologyId, terminology, version, Branch.ROOT, null, pfs);

      final TreeList treeList = new TreeListJpa();
      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {
        final Tree tree = contentService.getTreeForTreePosition(treepos);

        treeList.getObjects().add(tree);
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

    Logger.getLogger(getClass()).info("RESTful call (Content): /code/"
        + terminology + "/" + version + "/" + terminologyId + "/trees");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve trees for the code",
          UserRole.VIEWER);

      final TreePositionList list = contentService.findCodeTreePositions(
          terminologyId, terminology, version, Branch.ROOT, null, pfs);
      final TreeList treeList = new TreeListJpa();
      for (final TreePosition<? extends ComponentHasAttributesAndName> treepos : list
          .getObjects()) {
        final Tree tree = contentService.getTreeForTreePosition(treepos);

        treeList.getObjects().add(tree);
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
  @Path("/concept/{terminology}/{version}/trees")
  @ApiOperation(value = "Find concept trees matching the query", notes = "Finds all merged trees matching the specified parameters", response = TreeJpa.class)
  public Tree findConceptTree(
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query search term, e.g. 'vitamin'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info("RESTful call (Content): /concept/"
        + terminology + "/" + version + "/trees?query=" + query);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the concept",
          UserRole.VIEWER);

      final TreePositionList list = contentService.findConceptTreePositions(
          null, terminology, version, Branch.ROOT, queryStr, pfs);

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
        returnTree.mergeTree(treeForTreePos,
            pfs != null ? pfs.getSortField() : null);
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
  @Path("/descriptor/{terminology}/{version}/trees")
  @ApiOperation(value = "Find descriptor trees matching the query", notes = "Finds all merged trees matching the specified parameters", response = TreeJpa.class)
  public Tree findDescriptorTree(
    @ApiParam(value = "Descriptor terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query search term, e.g. 'vitamin'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass()).info("RESTful call (Content): /descriptor/"
        + terminology + "/" + version + "/trees?query=" + query);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the descriptor",
          UserRole.VIEWER);

      final TreePositionList list = contentService.findDescriptorTreePositions(
          null, terminology, version, Branch.ROOT, queryStr, pfs);

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
        returnTree.mergeTree(treeForTreePos,
            pfs != null ? pfs.getSortField() : null);
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
  public Tree findCodeTree(
    @ApiParam(value = "Code terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Code version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query search term, e.g. 'vitamin'", required = true) @PathParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    String queryStr = query == null ? "" : query;
    Logger.getLogger(getClass()).info("RESTful call (Content): /code/"
        + terminology + "/" + version + "/trees?query=" + query);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      TreePositionList list = contentService.findCodeTreePositions(null,
          terminology, version, Branch.ROOT, queryStr, pfs);

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
        returnTree.mergeTree(treeForTreePos,
            pfs != null ? pfs.getSortField() : null);
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
  @Path("/concept/{terminology}/{version}/{terminologyId}/trees/children")
  @ApiOperation(value = "Find children trees for a concept", notes = "Returns paged children trees for a concept. Note: not ancestorPath-sensitive", response = TreeJpa.class)
  public TreeList findConceptTreeChildren(
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Concept terminologyId, e.g. C0000061", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /concept/" + terminology + "/" + version
            + "/" + terminologyId + "/" + "/trees/children");
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
        childTrees.getObjects().add(childTree);
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

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /code/" + terminology + "/" + version
            + "/" + terminologyId + "/" + "/trees/children");
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
        childTrees.getObjects().add(childTree);
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
  @Path("/descriptor/{terminology}/{version}/{terminologyId}/trees/children")
  @ApiOperation(value = "Find children trees for a descriptor", notes = "Returns paged children trees for a descriptor. Note: not ancestorPath-sensitive", response = TreeJpa.class)
  public TreeList findDescriptorTreeChildren(
    @ApiParam(value = "Descriptor terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Descriptor terminologyId, e.g. D0000061", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /descriptor/" + terminology + "/"
            + version + "/" + terminologyId + "/" + "/trees/children");
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
        childTrees.getObjects().add(childTree);
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
  @Path("/concept/{terminology}/{version}/trees/roots")
  @ApiOperation(value = "Find root trees for a concept-based terminology", notes = "Returns paged root trees for a concept-based terminology.", response = TreeJpa.class)
  public Tree findConceptTreeRoots(
    @ApiParam(value = "Concept terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /concept/"
        + terminology + "/" + version + "/" + "/trees/roots");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      // instantiate root tree positions array, used to construct trees
      TreePositionList rootTreePositions = new TreePositionListJpa();

      // get tree positions where ancestor path is empty
      rootTreePositions = contentService.findConceptTreePositions(null,
          terminology, version, Branch.ROOT, "-ancestorPath:[* TO *]", pfs);

      Tree rootTree = null;
      // if a terminology with a single root concept
      if (rootTreePositions.size() == 1) {

        // construct root tree from single root
        rootTree = new TreeJpa(rootTreePositions.getObjects().get(0));
        rootTree.setTotalCount(rootTreePositions.getTotalCount());

        // get the children tree positions
        final TreePositionList childTreePositions = contentService
            .findConceptTreePositionChildren(rootTree.getNodeTerminologyId(),
                terminology, version, Branch.ROOT, pfs);

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
  @Path("/descriptor/{terminology}/{version}/trees/roots")
  @ApiOperation(value = "Find root trees for a descriptor-based terminology", notes = "Returns paged root trees for a descriptor-based terminology.", response = TreeJpa.class)
  public Tree findDescriptorTreeRoots(
    @ApiParam(value = "Descriptor terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Descriptor version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Content): /descriptor/"
        + terminology + "/" + version + "/" + "/trees/roots");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      // instantiate root tree positions array, used to construct trees
      TreePositionList rootTreePositions = new TreePositionListJpa();

      // get tree positions where ancestor path is empty
      rootTreePositions = contentService.findDescriptorTreePositions(null,
          terminology, version, Branch.ROOT, "-ancestorPath:[* TO *]", pfs);

      Tree rootTree = null;

      // if a terminology with a single root descriptor
      if (rootTreePositions.size() == 1) {

        // construct root tree from single root
        rootTree = new TreeJpa(rootTreePositions.getObjects().get(0));
        rootTree.setTotalCount(rootTreePositions.getTotalCount());

        // get the children tree positions
        final TreePositionList childTreePositions = contentService
            .findDescriptorTreePositionChildren(rootTree.getNodeTerminologyId(),
                terminology, version, Branch.ROOT, pfs);

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

    Logger.getLogger(getClass()).info("RESTful call (Content): /code/"
        + terminology + "/" + version + "/" + "/trees/roots");
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find trees for the code",
          UserRole.VIEWER);

      // instantiate root tree positions array, used to construct trees
      TreePositionList rootTreePositions = new TreePositionListJpa();

      // get tree positions where ancestor path is empty
      rootTreePositions = contentService.findCodeTreePositions(null,
          terminology, version, Branch.ROOT, "-ancestorPath:[* TO *]", pfs);

      Tree rootTree = null;

      // if a terminology with a single root code
      if (rootTreePositions.size() == 1) {

        // construct root tree from single root
        rootTree = new TreeJpa(rootTreePositions.getObjects().get(0));
        rootTree.setTotalCount(rootTreePositions.getTotalCount());

        // get the children tree positions
        final TreePositionList childTreePositions = contentService
            .findCodeTreePositionChildren(rootTree.getNodeTerminologyId(),
                terminology, version, Branch.ROOT, pfs);

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

  @Override
  @POST
  @Path("/mapset/{mapSetId}/{terminology}/{version}/mappings")
  @ApiOperation(value = "Find mappings", notes = "Get the mappings for the indicated mapset", response = MappingListJpa.class)
  public MappingList findMappings(
    @ApiParam(value = "MapSet terminology id, e.g. 341823003", required = true) @PathParam("mapSetId") String mapSetId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /mapset/" + mapSetId + "/" + terminology
            + "/" + version + "/mappings" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find mappings",
          UserRole.VIEWER);

      final MapSet mapSet =
          contentService.getMapSet(mapSetId, terminology, version, Branch.ROOT);
      final MappingList mappingList =
          contentService.findMappings(mapSet.getId(), query, pfs);
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

  @Override
  @POST
  @Path("/concept/{terminologyId}/{terminology}/{version}/mappings")
  @ApiOperation(value = "Find mappings", notes = "Get the mappings for the indicated concept", response = MappingListJpa.class)
  public MappingList findConceptMappings(
    @ApiParam(value = "Concept terminology id, e.g. 341823003", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /concept/" + terminologyId + "/"
            + terminology + "/" + version + "/mappings" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find mappings",
          UserRole.VIEWER);

      final MappingList mappingList = contentService.findConceptMappings(
          terminologyId, terminology, version, Branch.ROOT, query, pfs);
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

  @Override
  @POST
  @Path("/code/{terminologyId}/{terminology}/{version}/mappings")
  @ApiOperation(value = "Find mappings", notes = "Get the mappings for the indicated code", response = MappingListJpa.class)
  public MappingList findCodeMappings(
    @ApiParam(value = "Code terminology id, e.g. 341823003", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /code/" + terminologyId + "/"
            + terminology + "/" + version + "/mappings" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find mappings",
          UserRole.VIEWER);

      final MappingList mappingList = contentService.findCodeMappings(
          terminologyId, terminology, version, Branch.ROOT, query, pfs);
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

  @Override
  @POST
  @Path("/descriptor/{terminologyId}/{terminology}/{version}/mappings")
  @ApiOperation(value = "Find mappings", notes = "Get the mappings for the indicated descriptor", response = MappingListJpa.class)
  public MappingList findDescriptorMappings(
    @ApiParam(value = "Descriptor terminology id, e.g. 341823003", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Terminology version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Query, e.g. 'iron'", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // Fix query
    final String queryStr = query == null ? "" : query;

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /descriptor/" + terminologyId + "/"
            + terminology + "/" + version + "/mappings" + queryStr);
    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find mappings",
          UserRole.VIEWER);

      final MappingList mappingList = contentService.findDescriptorMappings(
          terminologyId, terminology, version, Branch.ROOT, query, pfs);
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

  /* see superclass */

  @POST
  @Path("/favorites")
  @ApiOperation(value = "Get user favorites", notes = "Gets user favorites for a terminology and version", response = String.class)
  @Override
  public SearchResultList getFavoritesForUser(
    @ApiParam(value = "Paging/filtering/sorting object", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Content): /content/favorites for authToken "
            + authToken);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "get user favorites", UserRole.VIEWER);

      final UserPreferences preferences =
          securityService.getUser(userName).getUserPreferences();

      // bail if no preference
      if (preferences == null) {
        return new SearchResultListJpa();
      }
      List<ComponentInfo> favorites = new ArrayList<>();
      for (final String str : preferences.getFavorites()) {
        ComponentInfo favorite = new ComponentInfoJpa(str);
        favorites.add(favorite);
      }

      // apply pfs to list
      final int[] totalCt = new int[1];
      favorites = contentService.applyPfsToList(favorites, ComponentInfo.class,
          totalCt, pfs);

      // declare results list and set total count
      final SearchResultList results = new SearchResultListJpa();
      results.setTotalCount(totalCt[0]);

      for (final ComponentInfo info : favorites) {
        boolean hasNotes = false;
        AtomClass atomClass = null;
        IdType type = null;
        switch (info.getType()) {
          case CODE:
            final Code code = contentService.getCode(info.getTerminologyId(),
                info.getTerminology(), info.getVersion(), Branch.ROOT);
            hasNotes = code.getNotes().size() > 0;
            atomClass = code;
            type = IdType.CODE;
            break;
          case CONCEPT:
            final Concept concept =
                contentService.getConcept(info.getTerminologyId(),
                    info.getTerminology(), info.getVersion(), Branch.ROOT);
            hasNotes = concept.getNotes().size() > 0;
            atomClass = concept;
            type = IdType.CONCEPT;
            break;
          case DESCRIPTOR:
            final Descriptor descriptor =
                contentService.getDescriptor(info.getTerminologyId(),
                    info.getTerminology(), info.getVersion(), Branch.ROOT);
            hasNotes = descriptor.getNotes().size() > 0;
            atomClass = descriptor;
            type = IdType.DESCRIPTOR;
            break;
          default:
            throw new Exception(
                "Non atom-class object on favorites list: " + info.toString());
        }

        final SearchResult searchResult = new SearchResultJpa();
        searchResult.setId(atomClass.getId());
        searchResult.setType(type);
        searchResult.setTerminology(atomClass.getTerminology());
        searchResult.setVersion(atomClass.getVersion());
        searchResult.setTerminologyId(atomClass.getTerminologyId());
        searchResult.setValue(atomClass.getName());
        searchResult.setProperty(
            new KeyValuePair("hasNotes", String.valueOf(hasNotes)));
        results.getObjects().add(searchResult);
      }
      return results;

    } catch (Exception e) {
      handleException(e, "trying to get user favorites");
    } finally {
      contentService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @POST
  @Path("/concept/{id}/note")
  @Produces("text/plain")
  @ApiOperation(value = "Adds a user note to a concept", notes = "Adds a user note to a concept", response = String.class)
  @Override
  public void addConceptNote(
    @ApiParam(value = "id, e.g. 12345", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Note to add", required = true) String noteText,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Content): /concept/" + id
        + "note for authToken " + authToken);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add concept note", UserRole.VIEWER);
      contentService.setLastModifiedBy(userName);
      final Concept concept = contentService.getConcept(id);

      if (concept == null) {
        throw new Exception("Could not retrieve concept for note addition");
      }
      final ConceptNoteJpa note = new ConceptNoteJpa();
      note.setNote(noteText);
      note.setLastModifiedBy(userName);
      note.setTimestamp(new Date());
      note.setConcept(concept);

      // add the note, add it to the concept, and update the concept
      final Note newNote = contentService.addNote(note);
      concept.getNotes().add(newNote);
      contentService.setMolecularActionFlag(false);
      contentService.updateConcept(concept);

    } catch (Exception e) {
      handleException(e, "trying to add concept note");
    } finally {
      securityService.close();
    }

  }

  /* see superclass */
  @POST
  @Path("/atom/{id}/note")
  @Produces("text/plain")
  @ApiOperation(value = "Adds a user note to a atom", notes = "Adds a user note to a atom", response = String.class)
  @Override
  public void addAtomNote(
    @ApiParam(value = "id, e.g. 12345", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Note to add", required = true) String noteText,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Content): /atom/" + id
        + "note for authToken " + authToken);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add atom note", UserRole.VIEWER);
      contentService.setLastModifiedBy(userName);
      final Atom atom = contentService.getAtom(id);

      if (atom == null) {
        throw new Exception("Could not retrieve atom for note addition");
      }
      final AtomNoteJpa note = new AtomNoteJpa();
      note.setNote(noteText);
      note.setLastModifiedBy(userName);
      note.setTimestamp(new Date());
      note.setAtom(atom);

      // add the note, add it to the atom, and update the atom
      final Note newNote = contentService.addNote(note);
      atom.getNotes().add(newNote);
      contentService.setMolecularActionFlag(false);
      contentService.updateAtom(atom);

    } catch (Exception e) {
      handleException(e, "trying to add atom note");
    } finally {
      securityService.close();
    }

  }

  /* see superclass */

  @DELETE
  @Path("/concept/note/{id}")
  @Produces("text/plain")
  @ApiOperation(value = "Remove a note from a concept", notes = "Remove a note from a concept", response = String.class)
  @Override
  public void removeConceptNote(
    @ApiParam(value = "Id of note to remove", required = true) @PathParam("id") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Content): /concept/note"
        + noteId + " for authToken " + authToken);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "remove concept note", UserRole.VIEWER);
      contentService.setLastModifiedBy(userName);

      final ConceptNoteJpa note =
          (ConceptNoteJpa) contentService.getNote(noteId, ConceptNoteJpa.class);
      final Concept concept = note.getConcept();

      concept.getNotes().remove(note);
      contentService.setMolecularActionFlag(false);
      contentService.updateConcept(concept);
      contentService.removeNote(noteId, ConceptNoteJpa.class);

    } catch (Exception e) {
      handleException(e, "trying to remove note from concept");
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  @DELETE
  @Path("/atom/note/{id}")
  @Produces("text/plain")
  @ApiOperation(value = "Remove a note from a atom", notes = "Remove a note from a atom", response = String.class)
  @Override
  public void removeAtomNote(
    @ApiParam(value = "Id of note to remove", required = true) @PathParam("id") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Content): /atom/note"
        + noteId + " for authToken " + authToken);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "remove atom note", UserRole.VIEWER);
      contentService.setLastModifiedBy(userName);

      final AtomNoteJpa note =
          (AtomNoteJpa) contentService.getNote(noteId, AtomNoteJpa.class);
      final Atom atom = note.getAtom();

      atom.getNotes().remove(note);
      contentService.setMolecularActionFlag(false);
      contentService.updateAtom(atom);
      contentService.removeNote(noteId, AtomNoteJpa.class);

    } catch (Exception e) {
      handleException(e, "trying to remove note from atom");
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */

  @POST
  @Path("/code/{id}/note")
  @Produces("text/plain")
  @ApiOperation(value = "Adds a user note to a code", notes = "Adds a user note to a code", response = String.class)
  @Override
  public void addCodeNote(
    @ApiParam(value = "id, e.g. 12345", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Note to add", required = true) String noteText,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Content): /code/" + id
        + "note for authToken " + authToken);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add code note", UserRole.VIEWER);
      contentService.setLastModifiedBy(userName);

      Code code = contentService.getCode(id);

      if (code == null) {
        throw new Exception("Could not retrieve code for note addition");
      }
      CodeNoteJpa note = new CodeNoteJpa();
      note.setNote(noteText);
      note.setLastModifiedBy(userName);
      note.setTimestamp(new Date());
      note.setCode(code);

      // add the note, add it to the code, and update the code
      Note newNote = contentService.addNote(note);
      code.getNotes().add(newNote);
      contentService.setMolecularActionFlag(false);
      contentService.updateCode(code);

    } catch (Exception e) {
      handleException(e, "trying to add user favorite");
    } finally {
      securityService.close();
    }

  }

  /* see superclass */
  @DELETE
  @Path("/code/note/{id}")
  @Produces("text/plain")
  @ApiOperation(value = "Remove a note from a code", notes = "Remove a note from a code", response = String.class)
  @Override
  public void removeCodeNote(
    @ApiParam(value = "Id of note to remove", required = true) @PathParam("id") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Content): /code/note"
        + noteId + " for authToken " + authToken);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "remove code note", UserRole.VIEWER);
      contentService.setLastModifiedBy(userName);

      final CodeNoteJpa note =
          (CodeNoteJpa) contentService.getNote(noteId, CodeNoteJpa.class);
      final Code code = note.getCode();

      code.getNotes().remove(note);
      contentService.setMolecularActionFlag(false);
      contentService.updateCode(code);
      contentService.removeNote(noteId, CodeNoteJpa.class);

    } catch (Exception e) {
      handleException(e, "trying to remove note from code");
    } finally {
      securityService.close();
    }

  }

  /* see superclass */
  @POST
  @Path("/descriptor/{id}/note")
  @Produces("text/plain")
  @ApiOperation(value = "Adds a user note to a descriptor", notes = "Adds a user note to a descriptor", response = String.class)
  @Override
  public void addDescriptorNote(
    @ApiParam(value = "id, e.g. 12345", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Note to add", required = true) String noteText,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Content): /descriptor/"
        + id + "note for authToken " + authToken);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add descriptor note", UserRole.VIEWER);
      contentService.setLastModifiedBy(userName);

      Descriptor descriptor = contentService.getDescriptor(id);

      if (descriptor == null) {
        throw new Exception("Could not retrieve descriptor for note addition");
      }
      DescriptorNoteJpa note = new DescriptorNoteJpa();
      note.setNote(noteText);
      note.setLastModifiedBy(userName);
      note.setTimestamp(new Date());
      note.setDescriptor(descriptor);

      // add the note, add it to the descriptor, and update the descriptor
      Note newNote = contentService.addNote(note);
      descriptor.getNotes().add(newNote);
      contentService.setMolecularActionFlag(false);
      contentService.updateDescriptor(descriptor);

    } catch (Exception e) {
      handleException(e, "trying to add user favorite");
    } finally {
      securityService.close();
    }

  }

  /* see superclass */
  @DELETE
  @Path("/descriptor/note/{id}")
  @Produces("text/plain")
  @ApiOperation(value = "Remove a note from a descriptor", notes = "Remove a note from a descriptor", response = String.class)
  @Override
  public void removeDescriptorNote(
    @ApiParam(value = "Id of note to remove", required = true) @PathParam("id") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Content): /descriptor/note"
        + noteId + " for authToken " + authToken);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "remove descriptor note", UserRole.VIEWER);
      contentService.setLastModifiedBy(userName);

      final DescriptorNoteJpa note = (DescriptorNoteJpa) contentService
          .getNote(noteId, DescriptorNoteJpa.class);
      final Descriptor descriptor = note.getDescriptor();

      descriptor.getNotes().remove(note);
      contentService.setMolecularActionFlag(false);
      contentService.updateDescriptor(descriptor);
      contentService.removeNote(noteId, DescriptorNoteJpa.class);

    } catch (Exception e) {
      handleException(e, "trying to remove note from descriptor");
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @POST
  @Path("/component/notes")
  @ApiOperation(value = "Get components annotated by a user", notes = "Gets user favorites for a terminology and version", response = String.class)
  @Override
  public SearchResultList getComponentsWithNotes(
    @ApiParam(value = "Query text", required = false) @QueryParam("query") String query,
    @ApiParam(value = "Paging/filtering/sorting object", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Content): /content/component/notes/?query=" + query
            + " for authToken " + authToken);

    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "get components with notes for query", UserRole.VIEWER);

      final SearchResultList results = new SearchResultListJpa();

      final PfsParameter pfsUser = new PfsParameterJpa();

      // Restrict results only to those left by the current user
      pfsUser.setQueryRestriction("lastModifiedBy:" + authToken);

      // get concept notes for this user with no paging
      final NoteList conceptNotes =
          contentService.findConceptNotes(query, pfsUser);

      for (final Note note : conceptNotes.getObjects()) {
        final ConceptNoteJpa conceptNote = (ConceptNoteJpa) note;
        final SearchResult sr = new SearchResultJpa();
        sr.setId(conceptNote.getConcept().getId());
        sr.setTerminology(conceptNote.getConcept().getTerminology());
        sr.setVersion(conceptNote.getConcept().getVersion());
        sr.setTerminologyId(conceptNote.getConcept().getTerminologyId());
        sr.setType(conceptNote.getConcept().getType());
        sr.setValue(conceptNote.getConcept().getName());

        if (!results.contains(sr)) {
          results.getObjects().add(sr);
        }
      }

      // get descriptor notes for this user with no paging
      final NoteList descriptorNotes =
          contentService.findDescriptorNotes(query, pfsUser);

      for (final Note note : descriptorNotes.getObjects()) {
        final DescriptorNoteJpa descriptorNote = (DescriptorNoteJpa) note;
        final SearchResult sr = new SearchResultJpa();
        sr.setId(descriptorNote.getDescriptor().getId());
        sr.setTerminology(descriptorNote.getDescriptor().getTerminology());
        sr.setVersion(descriptorNote.getDescriptor().getVersion());
        sr.setTerminologyId(descriptorNote.getDescriptor().getTerminologyId());
        sr.setType(descriptorNote.getDescriptor().getType());
        sr.setValue(descriptorNote.getDescriptor().getName());

        if (!results.contains(sr)) {
          results.getObjects().add(sr);
        }
      }

      // get code notes for this user with no paging
      final NoteList codeNotes = contentService.findCodeNotes(query, pfsUser);
      results
          .setTotalCount(results.getTotalCount() + codeNotes.getTotalCount());
      for (final Note note : codeNotes.getObjects()) {
        final CodeNoteJpa codeNote = (CodeNoteJpa) note;
        final SearchResult sr = new SearchResultJpa();
        sr.setId(codeNote.getCode().getId());
        sr.setTerminology(codeNote.getCode().getTerminology());
        sr.setVersion(codeNote.getCode().getVersion());
        sr.setTerminologyId(codeNote.getCode().getTerminologyId());
        sr.setType(codeNote.getCode().getType());
        sr.setValue(codeNote.getCode().getName());

        if (!results.contains(sr)) {
          results.getObjects().add(sr);
        }
      }

      // construct new local pfs from original pfs
      final PfsParameter pfsLocal = new PfsParameterJpa(pfs);

      // clear query restriction (used in
      pfsLocal.setQueryRestriction(null);
      if (pfs.getSortField().equals("name")) {
        pfsLocal.setSortField("value");
      } else if (pfs.getSortFields() != null
          && pfs.getSortFields().contains("name")) {
        List<String> sortFields = pfs.getSortFields();
        sortFields.set(sortFields.indexOf("name"), "value");
        pfsLocal.setSortFields(sortFields);
      }

      // set total count
      results.setTotalCount(results.size());

      // apply paging/sorting
      final int totalCt[] = new int[1];
      results.setObjects(contentService.applyPfsToList(results.getObjects(),
          SearchResult.class, totalCt, pfsLocal));

      return results;

    } catch (Exception e) {
      handleException(e, "trying to find components with notes for query");
    } finally {
      contentService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/component/{type}/{terminology}/{version}/{terminologyId}/relationships")
  @ApiOperation(value = "Get relationships with this terminologyId", notes = "Get the relationships with the given component info id", response = RelationshipListJpa.class)
  public RelationshipList findComponentInfoRelationships(
    @ApiParam(value = "Component info terminology id, e.g. 102751005", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Component info terminology name, e.g. SNOMEDCT_US", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Component info version, e.g. 2014_09_01", required = true) @PathParam("version") String version,
    @ApiParam(value = "Component info type, e.g. CODE", required = true) @PathParam("type") IdType type,
    @ApiParam(value = "Query for searching relationships, e.g. component info id or concept name", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /component/" + type + "/" + terminology
            + "/" + version + "/" + terminologyId + "/relationships?query="
            + query);
    final String queryStr = query == null ? "" : query;

    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve relationships for the component info", UserRole.VIEWER);

      final RelationshipList list =
          contentService.findComponentInfoRelationships(terminologyId,
              terminology, version, type, Branch.ROOT, queryStr, false, pfs);

      // Use graph resolver
      for (final Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : list
          .getObjects()) {
        contentService.getGraphResolutionHandler(terminology).resolve(rel);
      }

      return list;

    } catch (Exception e) {
      handleException(e,
          "trying to retrieve relationships for a component info");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/validate/descriptor")
  @ApiOperation(value = "Validate Descriptor", notes = "Validates a descriptor")
  public ValidationResult validateDescriptor(
    @ApiParam(value = "The project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Descriptor", required = true) DescriptorJpa descriptor,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Content): /validate/descriptor " + descriptor);

    final ContentService conceptService = new ContentServiceJpa();
    try {
      authorizeProject(conceptService, projectId, securityService, authToken,
          "validate descriptor", UserRole.USER);
      final Project project = conceptService.getProject(projectId);
      return conceptService.validateDescriptor(project.getValidationChecks(), descriptor);
    } catch (Exception e) {
      handleException(e, "trying to validate descriptor");
      return null;
    } finally {
      conceptService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/validate/atom")
  @ApiOperation(value = "Validate Atom", notes = "Validates a atom")
  public ValidationResult validateAtom(
    @ApiParam(value = "The project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Atom", required = true) AtomJpa atom,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Content): /validate/atom " + atom);

    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeProject(contentService, projectId, securityService, authToken,
          "validate atom", UserRole.USER);
      final Project project = contentService.getProject(projectId);
      return contentService.validateAtom(project.getValidationChecks(), atom);
    } catch (Exception e) {
      handleException(e, "trying to validate atom");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/validate/code")
  @ApiOperation(value = "Validate Code", notes = "Validates a code")
  public ValidationResult validateCode(
    @ApiParam(value = "The project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Code", required = true) CodeJpa code,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Content): /validate/code " + code);

    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeProject(contentService, projectId, securityService, authToken,
          "validate code", UserRole.USER);
      final Project project = contentService.getProject(projectId);
      return contentService.validateCode(project.getValidationChecks(), code);
    } catch (Exception e) {
      handleException(e, "trying to validate code");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/validate/concept")
  @ApiOperation(value = "Validate Concept", notes = "Validates a concept")
  public ValidationResult validateConcept(
    @ApiParam(value = "The project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept", required = true) ConceptJpa concept,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Content): /validate/concept " + concept);

    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeProject(contentService, projectId, securityService, authToken,
          "validate conceptm", UserRole.USER);
      final Project project = contentService.getProject(projectId);
      return contentService.validateConcept(project.getValidationChecks(), concept);
    } catch (Exception e) {
      handleException(e, "trying to validate concept");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/concept/{terminology}/{version}/{terminologyId}/treePositions/deep")
  @ApiOperation(value = "Get deep tree positions with this terminologyId", notes = "Get the tree positions for the concept and also for any other atoms, concepts, descirptors, or codes in its graph for the specified concept id", response = TreePositionListJpa.class)
  public TreePositionList findConceptDeepTreePositions(
    @ApiParam(value = "Concept terminology id, e.g. C0000039", required = true) @PathParam("terminologyId") String terminologyId,
    @ApiParam(value = "Concept terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Concept version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Query for searching relationships, e.g. concept id or concept name", required = true) @QueryParam("query") String query,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Content): /concept/" + terminology + "/" + version
            + "/" + terminologyId + "/treePositions/deep with query: " + query);

    final ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve deep relationships for the concept", UserRole.VIEWER);

      return contentService.findConceptDeepTreePositions(terminologyId,
          terminology, version, Branch.ROOT, query, pfs);

    } catch (Exception e) {
      handleException(e,
          "trying to retrieve deep tree positions for a concept");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }
}
