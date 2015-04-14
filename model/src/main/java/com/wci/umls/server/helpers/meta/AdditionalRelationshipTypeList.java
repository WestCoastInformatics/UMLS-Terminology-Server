/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;

/**
 * Represents a sortable list of {@link AdditionalRelationshipType}
 */
public interface AdditionalRelationshipTypeList extends ResultList<AdditionalRelationshipType> {
  // nothing extra, a simple wrapper for easy serialization
}
