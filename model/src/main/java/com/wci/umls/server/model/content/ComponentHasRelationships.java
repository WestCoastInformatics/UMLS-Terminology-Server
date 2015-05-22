/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.HasRelationships;

/**
 * Represents a terminology component with relationships.
 */
public interface ComponentHasRelationships<T extends Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>>
    extends Component, HasRelationships<T> {
  // n/a
}