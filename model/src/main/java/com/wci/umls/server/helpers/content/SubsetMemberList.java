/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * Represents a sortable list of {@link SubsetMember}
 */
public interface SubsetMemberList extends
    ResultList<SubsetMember<? extends ComponentHasAttributes>> {
  // nothing extra, a simple wrapper for easy serialization
}
