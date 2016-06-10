/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import com.wci.umls.server.helpers.ResultList;

/**
 * Represents a sortable list of {@link Worklist}
 */
public interface WorklistList extends ResultList<Worklist> {
  // nothing extra, a simple wrapper for easy serialization
}
