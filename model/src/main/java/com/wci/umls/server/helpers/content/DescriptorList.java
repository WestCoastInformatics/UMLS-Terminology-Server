/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.Descriptor;

/**
 * Represents a sortable list of {@link Descriptor}
 */
public interface DescriptorList extends ResultList<Descriptor> {
  // nothing extra, a simple wrapper for easy serialization
}
