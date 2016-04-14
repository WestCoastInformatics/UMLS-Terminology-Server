/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.Definition;

/**
 * Represents a sortable list of {@link Definition}
 */
public interface DefinitionList extends ResultList<Definition> {
  // nothing extra, a simple wrapper for easy serialization
}
