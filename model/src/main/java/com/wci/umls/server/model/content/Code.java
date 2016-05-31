/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.HasNotes;

/**
 * Represents a fuzzy conceptual meaning that may be a {@link Concept} or may be
 * a {@link Descriptor} but the exact nature of the classification is not
 * explicit. This construct is typically used for older, legacy sources that do
 * not have well-defined semantics.
 * 
 * For "legacy" UMLS sources, it's also an alternative to concept/descriptor as
 * it has not clearly been defined which is which.
 */
public interface Code extends AtomClass,
    ComponentHasRelationships<CodeRelationship>, HasNotes, ComponentInfo {

  // n/a

}