/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.User;

/**
 * Represents a sortable list of {@link User}.
 */
public interface UserList extends ResultList<User> {
  // nothing extra, a simple wrapper for easy serialization
}
