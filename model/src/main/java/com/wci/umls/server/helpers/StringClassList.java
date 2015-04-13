/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.content.StringClass;

/**
 * Represents a sortable list of {@link StringClass}
 */
public interface StringClassList extends ResultList<StringClass> {
  // nothing extra, a simple wrapper for easy serialization
}
