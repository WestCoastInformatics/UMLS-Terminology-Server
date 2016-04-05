/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.io.File;
import java.io.InputStream;
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
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.SourceDataFile;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SourceDataFileList;
import com.wci.umls.server.helpers.SourceDataList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.jpa.SourceDataFileJpa;
import com.wci.umls.server.jpa.SourceDataJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.SourceDataServiceJpa;
import com.wci.umls.server.jpa.services.helper.SourceDataFileUtility;
import com.wci.umls.server.jpa.services.rest.SourceDataServiceRest;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.SourceDataService;
import com.wci.umls.server.services.handlers.SourceDataHandler;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link SourceDataServiceRest}.
 */
@Path("/file")
@Api(value = "/file", description = "Operations supporting file")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class SourceDataServiceRestImpl extends RootServiceRestImpl
    implements SourceDataServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link SourceDataServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public SourceDataServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /**
   * Upload source data file.
   *
   * @param fileInputStream the file input stream
   * @param contentDispositionHeader the content disposition header
   * @param unzip the unzip
   * @param authToken the auth token
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @Path("/upload/{id}")
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public void uploadSourceDataFile(
    @FormDataParam("file") InputStream fileInputStream,
    @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @QueryParam("unzip") boolean unzip,
    @ApiParam(value = "Source data id, e.g. 1", required = true) @PathParam("id") Long sourceDataId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Source Data): /upload "
            + (contentDispositionHeader != null
                ? contentDispositionHeader.getFileName() : "UNKNOWN FILE")
            + " unzip=" + unzip + " authToken=" + authToken);

    final SourceDataService service = new SourceDataServiceJpa();
    SourceData sourceData = null;
    try {
      final String userName = authorizeApp(securityService, authToken,
          "upload source data files", UserRole.ADMINISTRATOR);

      // get the source data to append files to
      sourceData = service.getSourceData(sourceDataId);

      if (sourceData == null) {
        throw new Exception(
            "Source data with id " + sourceDataId + " does not exist");
      }

      // get the base destination folder (by source data id)
      String destinationFolder =
          ConfigUtility.getConfigProperties().getProperty("source.data.dir")
              + File.separator + sourceDataId.toString();

      // create the destination folder if it does not already exist
      if (!(new File(destinationFolder).exists())) {
        new File(destinationFolder).mkdir();
      }

      final List<File> files = new ArrayList<>();
      // if unzipping requested and file is valid, extract compressed file to
      // destination folder
      if (unzip == true) {
        files.addAll(SourceDataFileUtility.extractCompressedSourceDataFile(
            fileInputStream, destinationFolder,
            contentDispositionHeader.getFileName()));
      }
      // otherwise, simply write the input stream
      else {
        files.add(SourceDataFileUtility.writeSourceDataFile(fileInputStream,
            destinationFolder, contentDispositionHeader.getFileName()));

      }

      // Iterate through file list and add source data files.
      for (final File file : files) {
        final SourceDataFile sdf = new SourceDataFileJpa();
        sdf.setName(file.getName());
        sdf.setPath(file.getAbsolutePath());
        sdf.setDirectory(file.isDirectory());
        sdf.setSize(file.length());
        sdf.setTimestamp(new Date());
        sdf.setLastModifiedBy(userName);

        sourceData.addSourceDataFile(sdf);

        service.addSourceDataFile(sdf);
      }
      
      fileInputStream.close();

      // finally, update the source data object itself
      service.updateSourceData(sourceData);

    } catch (Exception e) {
      handleException(e, "uploading a source data file");
    } finally {
      service.close();
    }
  }

  /**
   * Add source data file.
   *
   * @param sourceDataFile the source data file
   * @param authToken the auth token
   * @return the source data file
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @PUT
  @Path("/add")
  public SourceDataFile addSourceDataFile(
    @ApiParam(value = "SourceDataFile to add", required = true) SourceDataFileJpa sourceDataFile,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Source Data): /add");
    final SourceDataService service = new SourceDataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add source data file", UserRole.ADMINISTRATOR);

      sourceDataFile.setLastModifiedBy(userName);
      return service.addSourceDataFile(sourceDataFile);

    } catch (Exception e) {
      handleException(e, "update source data files");
    } finally {
      service.close();
    }
    return null;
  }

  /**
   * Update source data file.
   *
   * @param sourceDataFile the source data file
   * @param authToken the auth token
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @POST
  @Path("/update")
  public void updateSourceDataFile(
    @ApiParam(value = "SourceDataFile to update", required = true) SourceDataFileJpa sourceDataFile,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Source Data): /update");

    final SourceDataService service = new SourceDataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add source data file", UserRole.ADMINISTRATOR);

      sourceDataFile.setLastModifiedBy(userName);
      service.updateSourceDataFile(sourceDataFile);

    } catch (Exception e) {
      handleException(e, "update source data files");
    } finally {
      service.close();
    }
  }

  /**
   * Remove source data file.
   *
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @DELETE
  @Path("/remove/{id}")
  public void removeSourceDataFile(
    @ApiParam(value = "SourceDataFile id, e.g. 5", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Source Data): /remove/" + id);

    final SourceDataService service = new SourceDataServiceJpa();
    try {
      authorizeApp(securityService, authToken, "delete source data file",
          UserRole.ADMINISTRATOR);

      final SourceDataFile sourceDataFile = service.getSourceDataFile(id);

      try {

        // physically remove the file
        final File file = new File(sourceDataFile.getPath());
        file.delete();

      } catch (Exception e) {
        Logger.getLogger(getClass()).warn(
            "Unexpected error removing file " + sourceDataFile.getPath());
      }

      // remove this entry from its source data
      SourceData sourceData = sourceDataFile.getSourceData();
      sourceData.removeSourceDataFile(sourceDataFile);
      service.updateSourceData(sourceData);

      // remove the database entry
      service.removeSourceDataFile(sourceDataFile.getId());

    } catch (Exception e) {
      handleException(e, "delete source data files");
    } finally {
      service.close();
    }
  }

  /**
   * Find source data files for query.
   *
   * @param query the query
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the source data file list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @GET
  @Path("/find")
  @ApiOperation(value = "Query source data files", notes = "Returns list of details for uploaded files returned by query", response = StringList.class)
  public SourceDataFileList findSourceDataFilesForQuery(
    @ApiParam(value = "String query, e.g. SNOMEDCT", required = true) @QueryParam("query") String query,
    @ApiParam(value = "Paging/filtering/sorting object", required = false) PfsParameter pfsParameter,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Source Data): /find - " + query);

    final SourceDataService service = new SourceDataServiceJpa();
    try {
      authorizeApp(securityService, authToken, "search for source data files",
          UserRole.ADMINISTRATOR);

      return service.findSourceDataFilesForQuery(query, pfsParameter);

    } catch (Exception e) {
      handleException(e, "search for source data files");
      return null;
    } finally {
      service.close();
    }

  }

  /**
   * Add source data.
   *
   * @param sourceData the source data
   * @param authToken the auth token
   * @return the source data
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @Path("/data/add")
  @PUT
  public SourceData addSourceData(
    @ApiParam(value = "Source data to add", required = true) SourceDataJpa sourceData,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Source Data): /data/add");

    final SourceDataService service = new SourceDataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add new source data", UserRole.ADMINISTRATOR);

      sourceData.setLastModifiedBy(userName);
      return service.addSourceData(sourceData);

    } catch (Exception e) {
      handleException(e, "adding new source data");
      return null;
    } finally {
      service.close();
    }

  }

  /**
   * Update source data.
   *
   * @param sourceData the source data
   * @param authToken the auth token
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @Path("/data/update")
  @POST
  public void updateSourceData(
    @ApiParam(value = "Source data to update", required = true) SourceDataJpa sourceData,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Source Data): /data/update");

    final SourceDataService service = new SourceDataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add new source data", UserRole.ADMINISTRATOR);

      sourceData.setLastModifiedBy(userName);
      service.updateSourceData(sourceData);

    } catch (Exception e) {
      handleException(e, "adding new source data");
    } finally {
      service.close();
    }

  }

  /**
   * Remove source data.
   *
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @DELETE
  @Path("data/remove/{id}")
  public void removeSourceData(
    @ApiParam(value = "SourceData id, e.g. 5", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Source Data): /data/remove/" + id);

    final SourceDataService service = new SourceDataServiceJpa();
    try {
      authorizeApp(securityService, authToken, "delete source data with id " + id,
          UserRole.ADMINISTRATOR);
       
      // remove the directory containing this source data's files
      // TODO This is not working, revisit
     /* File sdDir = new File(ConfigUtility.getConfigProperties().getProperty("source.data.dir") + File.separator + id.toString());
      for (File f : sdDir.listFiles()) {
        f.delete();
      }*/

      // remove the source data
      service.removeSourceData(id);

    } catch (Exception e) {
      handleException(e, "delete source data");
    } finally {
      service.close();
    }
  }

  /**
   * Find source data for query.
   *
   * @param query the query
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the source data list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @GET
  @Path("/data/find")
  @ApiOperation(value = "Query source data files", notes = "Returns list of details for uploaded files returned by query", response = StringList.class)
  public SourceDataList findSourceDataForQuery(
    @ApiParam(value = "String query, e.g. SNOMEDCT", required = true) @QueryParam("query") String query,
    @ApiParam(value = "Paging/filtering/sorting object", required = false) PfsParameter pfsParameter,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Source Data): /data/find" + query);

    final SourceDataService service = new SourceDataServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get source datas",
          UserRole.ADMINISTRATOR);

      return service.findSourceDatasForQuery(query, pfsParameter);

    } catch (Exception e) {
      handleException(e, "retrieving uploaded file list");
      return null;
    } finally {
      service.close();
    }

  }

  /**
   * Gets the loader names.
   *
   * @param authToken the auth token
   * @return the loader names
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @GET
  @Path("/data/sourceDataHandlers")
  @ApiOperation(value = "Get source data handler names", notes = "Gets all loader names.", response = StringList.class)
  public KeyValuePairList getSourceDataHandlerNames(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Source Data): /data/loaders");

    final SourceDataService service = new SourceDataServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get source datas",
          UserRole.ADMINISTRATOR);

      return service.getSourceDataHandlerNames();

    } catch (Exception e) {
      handleException(e, "retrieving uploaded file list");
      return null;
    } finally {
      service.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/data/id/{id}")
  @ApiOperation(value = "Get source data by id", notes = "Gets a source data object by Hibernate id", response = SourceDataJpa.class)
  public SourceData getSourceData(
    @ApiParam(value = "Source data id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Source Data): /data/loaders");

    final SourceDataService service = new SourceDataServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get source datas",
          UserRole.ADMINISTRATOR);
      return service.getSourceData(id);
    } catch (Exception e) {
      handleException(e, "retrieving uploaded file list");
      return null;
    } finally {
      service.close();
    }
  }

  @Override
  @POST
  @Path("/data/load")
  @ApiOperation(value = "Load data from source data configuration", notes = "Invokes loading of data based on source data files and configuration")
  public void loadFromSourceData(
    @ApiParam(value = "Source data to load from", required = true) SourceDataJpa sourceData,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Source Data): /data/load " + sourceData.toString());

    try {
      authorizeApp(securityService, authToken, "load from source data",
          UserRole.ADMINISTRATOR);

      Thread t = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            // instantiate the handler
            Class<?> sourceDataHandlerClass =
                Class.forName(sourceData.getHandler());
            SourceDataHandler handler =
                (SourceDataHandler) sourceDataHandlerClass.newInstance();
            handler.setSourceData(sourceData);
            handler.compute();

          } catch (Exception e) {
            handleException(e, " during execution of load from source data");
          }
        }
      });
      t.start();

    } catch (Exception e) {
      handleException(e,
          " attempting to load data from source data configuration");
    }
  }
  
  @Override
  @POST
  @Path("/data/remove")
  @ApiOperation(value = "Remove data from source data configuration", notes = "Invokes removing of data based on source data files and configuration")
  public void removeFromSourceData(
    @ApiParam(value = "Source data to removed loaded data for", required = true) SourceDataJpa sourceData,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Source Data): /data/remove");

    try {
      authorizeApp(securityService, authToken, "remove loaded data from source data",
          UserRole.ADMINISTRATOR);

      Thread t = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            // instantiate the handler
            Class<?> sourceDataHandlerClass =
                Class.forName(sourceData.getHandler());
            SourceDataHandler handler =
                (SourceDataHandler) sourceDataHandlerClass.newInstance();
            handler.setSourceData(sourceData);
            handler.remove();

          } catch (Exception e) {
            handleException(e, " during removal of loaded data from source data");
          }
        }
      });
      t.start();

    } catch (Exception e) {
      handleException(e,
          " attempting to load data from source data configuration");
    }
  }
}
