/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.HasTerminologyId;
import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.Relationship;

/**
 * Represents a sortable list of {@link Relationship}
 */
public interface RelationshipList
    extends
    ResultList<Relationship<? extends HasTerminologyId, ? extends HasTerminologyId>> {
  // nothing extra, a simple wrapper for easy serialization
}
