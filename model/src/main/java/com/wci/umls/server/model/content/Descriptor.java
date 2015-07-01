/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a fuzzy conceptual meaning that is likely broader than a single
 * meaning but is useful in particular contexts (such as coding, or
 * search/retrieval).
 */
public interface Descriptor extends AtomClass, ComponentHasDefinitions,
    ComponentHasRelationships<DescriptorRelationship> {

  // Nothing extra

}