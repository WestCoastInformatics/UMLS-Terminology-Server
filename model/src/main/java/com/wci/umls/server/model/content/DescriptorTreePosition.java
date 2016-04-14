/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a position in a hierarchical tree of descriptors. The ancestor
 * path will be a delimiter-separated value of descriptor terminology ids.
 */
public interface DescriptorTreePosition extends TreePosition<Descriptor> {
  // n/a
}