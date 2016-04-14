/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.PropertyChain;

/**
 * Represents a sortable list of {@link PropertyChain}
 */
public interface PropertyChainList extends ResultList<PropertyChain> {
  // nothing extra, a simple wrapper for easy serialization
}
