/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.workflow.Worklist;

/**
 * Represents a sortable list of {@link Worklist}
 */
public interface WorklistList extends ResultList<Worklist> {
  // nothing extra, a simple wrapper for easy serialization
}
