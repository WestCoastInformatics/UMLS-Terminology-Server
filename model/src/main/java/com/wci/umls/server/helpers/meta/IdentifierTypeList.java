/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.IdentifierType;

/**
 * Represents a sortable list of {@link IdentifierType}
 */
public interface IdentifierTypeList extends ResultList<IdentifierType> {
  // nothing extra, a simple wrapper for easy serialization
}
