/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.Concept;

/**
 * Represents a sortable list of {@link Concept}
 */
public interface ConceptList extends ResultList<Concept> {
  // nothing extra, a simple wrapper for easy serialization
}
