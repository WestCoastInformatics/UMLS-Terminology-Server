/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.workflow.Checklist;

/**
 * Represents a sortable list of {@link Checklist}
 */
public interface ChecklistList extends ResultList<Checklist> {
  // nothing extra, a simple wrapper for easy serialization
}
