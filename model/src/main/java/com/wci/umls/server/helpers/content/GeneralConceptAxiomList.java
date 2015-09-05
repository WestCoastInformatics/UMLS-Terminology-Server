/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.GeneralConceptAxiom;

/**
 * Represents a sortable list of {@link GeneralConceptAxiom}
 */
public interface GeneralConceptAxiomList extends ResultList<GeneralConceptAxiom> {
  // nothing extra, a simple wrapper for easy serialization
}
