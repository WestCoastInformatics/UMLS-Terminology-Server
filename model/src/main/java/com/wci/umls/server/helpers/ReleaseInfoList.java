/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.ReleaseInfo;

/**
 * Represents a sortable list of {@link ReleaseInfo}
 */
public interface ReleaseInfoList extends ResultList<ReleaseInfo> {
  // nothing extra, a simple wrapper for easy serialization
}
