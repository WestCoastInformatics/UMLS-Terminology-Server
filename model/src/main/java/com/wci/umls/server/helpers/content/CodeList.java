/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.Code;

/**
 * Represents a sortable list of {@link Code}
 */
public interface CodeList extends ResultList<Code> {
  // nothing extra, a simple wrapper for easy serialization
}
