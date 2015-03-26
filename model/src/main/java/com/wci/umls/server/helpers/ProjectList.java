package com.wci.umls.server.helpers;

import com.wci.umls.server.Project;

/**
 * Represents a sortable list of {@link Project}
 */
public interface ProjectList extends ResultList<Project> {
  // nothing extra, a simple wrapper for easy serialization
}
