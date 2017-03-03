/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.report;

import com.wci.umls.server.helpers.ResultList;

/**
 * Represents a sortable list of {@link Report}
 */
public interface ReportList extends ResultList<Report> {
  // nothing extra, a simple wrapper for easy serialization
}
