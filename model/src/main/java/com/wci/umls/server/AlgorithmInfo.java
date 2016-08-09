/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasName;
import com.wci.umls.server.helpers.HasTerminology;

/**
 * Represents a process configuration.
 * @param <T> the process info type (e.g. config or execution)
 */
public interface AlgorithmInfo<T extends ProcessInfo<?>>
    extends HasLastModified, HasTerminology, HasName {

  /**
   * Returns the description.
   * 
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * 
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Returns the algorithm key. This is the key used in the config.properties to
   * indicate a binding to an algorithm implementation. We're intentionally not
   * binding to class name but that has the effect of running the risk that the
   * key no longer points to the class actually used to execute the algorithm.
   * This is expected to be VERY stable, though, so this is not a major concern.
   *
   * @return the algorithm key.
   */
  public String getAlgorithmKey();

  /**
   * Sets the algorithm key.
   *
   * @param algorithKey the algorithm key
   */
  public void setAlgorithmKey(String algorithKey);

  /**
   * Returns the parameters.
   *
   * @return the parameters
   */
  public List<AlgorithmParameter> getParameters();

  /**
   * Sets the parameters.
   *
   * @param parameters the parameters
   */
  public void setParameters(List<AlgorithmParameter> parameters);

  /**
   * Returns the process.
   *
   * @return the process
   */
  public T getProcess();

  /**
   * Sets the process.
   *
   * @param p the process
   */
  public void setProcess(T p);

}