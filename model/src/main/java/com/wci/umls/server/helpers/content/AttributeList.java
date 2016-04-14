/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.Attribute;

/**
 * Represents a sortable list of {@link Attribute}
 */
public interface AttributeList extends ResultList<Attribute> {
  // nothing extra, a simple wrapper for easy serialization
}
