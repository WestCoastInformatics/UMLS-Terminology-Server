/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.content.LexicalClass;

/**
 * Represents a sortable list of {@link LexicalClass}
 */
public interface LexicalClassList extends ResultList<LexicalClass> {
  // nothing extra, a simple wrapper for easy serialization
}
