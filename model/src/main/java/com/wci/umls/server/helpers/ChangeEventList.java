/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.actions.ChangeEvent;

/**
 * Represents a sortable list of {@link ChangeEvent} objects.
 */
public interface ChangeEventList extends ResultList<ChangeEvent> {
  // nothing extra, a simple wrapper for easy serialization
}
