/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.HasDefinitions;
import com.wci.umls.server.helpers.HasRelationships;

/**
 * Represents a fuzzy conceptual meaning that is likely broader than a single
 * meaning but is useful in particular contexts (such as coding, or
 * search/retrieval).
 */
public interface Descriptor extends AtomClass, HasDefinitions,
    HasRelationships<DescriptorRelationship> {

  // Nothing extra

}