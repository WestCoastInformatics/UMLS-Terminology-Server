/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.Language;

/**
 * Represents a sortable list of {@link Language}
 */
public interface LanguageList extends ResultList<Language> {
  // nothing extra, a simple wrapper for easy serialization
}
