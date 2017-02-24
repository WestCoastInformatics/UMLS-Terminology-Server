/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.HasNotes;
import com.wci.umls.server.helpers.HasTreePositions;

/**
 * Represents a fuzzy conceptual meaning that is likely broader than a single
 * meaning but is useful in particular contexts (such as coding, or
 * search/retrieval).
 */
public interface Descriptor extends AtomClass, ComponentHasDefinitions,
    ComponentHasRelationships<DescriptorRelationship>, HasNotes,
    HasTreePositions<DescriptorTreePosition> {

  // n/a
}