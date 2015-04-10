/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.Relationship;

/**
 * Represents a sortable list of {@link Relationship}
 */
public interface RelationshipList extends
    ResultList<Relationship<? extends Component, ? extends Component>> {
  // nothing extra, a simple wrapper for easy serialization
}
