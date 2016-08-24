/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.services.ProcessService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;
import com.wci.umls.server.services.handlers.NormalizedStringHandler;

/**
 * JPA and JAXB enabled implementation of {@link ProcessService}.
 */
public class ProcessServiceJpa extends HistoryService
    implements ProcessService {

  /** The helper map. */
  private static Map<String, Algorithm> pnHandlerMap = null;
  
  /**
   * Static initialization (also used by refreshCaches).
   */
  private static void init() {
    try {
      if (ConfigUtility.getConfigProperties()
          .containsKey("javax.persistence.query.timeout")) {
        queryTimeout = Integer.parseInt(ConfigUtility.getConfigProperties()
            .getProperty("javax.persistence.query.timeout"));
      }

      if (config == null)
        config = ConfigUtility.getConfigProperties();
      final String key = "identifier.assignment.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        final IdentifierAssignmentHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, IdentifierAssignmentHandler.class);
        idHandlerMap.put(handlerName, handlerService);
      }
      if (!idHandlerMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("identifier.assignment.handler."
            + ConfigUtility.DEFAULT + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      idHandlerMap = null;
    }

    pnHandlerMap = new HashMap<>();

    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "compute.preferred.name.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {

        // Add handlers to map
        final ComputePreferredNameHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ComputePreferredNameHandler.class);
        pnHandlerMap.put(handlerName, handlerService);
      }
      if (!pnHandlerMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("compute.preferred.name.handler."
            + ConfigUtility.DEFAULT + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      pnHandlerMap = null;
    }

    try {

      config = ConfigUtility.getConfigProperties();
      final String key = "normalized.string.handler";
      final String handlerName = config.getProperty(key);

      final NormalizedStringHandler handlerService =
          ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
              handlerName, NormalizedStringHandler.class);
      normalizedStringHandler = handlerService;
    } catch (Exception e) {
      e.printStackTrace();
      normalizedStringHandler = null;
    }
  }  
  
  public ProcessServiceJpa() throws Exception {
    super();
  }

  @Override
  public List<Algorithm> getAuthoringAlgorithms() throws Exception {
    List<Algorithm> algorithmList = new ArrayList<Algorithm>();
    
    return algorithmList;
  }

  @Override
  public List<Algorithm> getMaintenanceAlgorithms() throws Exception {
    List<Algorithm> algorithmList = new ArrayList<Algorithm>();

    return algorithmList;
  }

  @Override
  public List<Algorithm> getReleaseAlgorithms() throws Exception {
    List<Algorithm> algorithmList = new ArrayList<Algorithm>();

    return algorithmList;
  }


}
