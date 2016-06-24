/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.actions;

import com.wci.umls.server.helpers.ResultList;

/**
 * Represents a sortable list of {@link AtomicAction}
 */
public interface AtomicActionList extends ResultList<AtomicAction> {
  // nothing extra, a simple wrapper for easy serialization
}
