/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.LexicalClass;

/**
 * Represents a sortable list of {@link LexicalClass}
 */
public interface LexicalClassList extends ResultList<LexicalClass> {
  // nothing extra, a simple wrapper for easy serialization
}
