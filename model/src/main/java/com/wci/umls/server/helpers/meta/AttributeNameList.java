/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.AttributeName;

/**
 * Represents a sortable list of {@link AttributeName}
 */
public interface AttributeNameList extends ResultList<AttributeName> {
  // nothing extra, a simple wrapper for easy serialization
}
