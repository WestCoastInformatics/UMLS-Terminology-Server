/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.SemanticTypeComponent;

/**
 * Represents a sortable list of {@link SemanticTypeComponent}
 */
public interface SemanticTypeComponentList extends ResultList<SemanticTypeComponent> {
  // nothing extra, a simple wrapper for easy serialization
}
