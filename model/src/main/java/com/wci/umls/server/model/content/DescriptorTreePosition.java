/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a position in a hierarchical tree of descriptors. The ancestor path
 * will be a delimiter-separated value of descriptor terminology ids.
 */
public interface DescriptorTreePosition extends TreePosition {

  /**
   * Returns the descriptor id.
   *
   * @return the descriptor id
   */
  public String getDescriptorId();

  /**
   * Sets the descriptor id.
   *
   * @param descriptorId the descriptor id
   */
  public void setDescriptorId(String descriptorId);

}