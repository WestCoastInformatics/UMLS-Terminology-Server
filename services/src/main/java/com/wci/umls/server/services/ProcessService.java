/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

/**
 * Represents a service for performing and monitoring processes.
 */
public interface ProcessService extends RootService {

  // get authoring algorithms : AlgorithmConfig??
  // get maintenance algorithms
  // get release algorithms

  // add/remove/update/get/find process configs
  // add/remove/update/get algorithm configs
  // add/remove/update/get/find process executions
  // add/remove/update/get algorithm executions
  // getProcessProgress(process execution id)
  // getAlgorithmProgress(algorithm execution id)
  // cancel process
  // find unfinished process executions (unfinished processes)
  // find failed process executions (unfinished processes)
  // restart process (process config/process execution)
  
  // get predefined processes? -> Process? ProcessConfig?

  
  // TODO: websocket for process execution changed?
}