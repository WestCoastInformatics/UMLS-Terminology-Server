/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.ComponentInfo;

/**
 * Represents a relationship between two {@link ComponentInfo}s.
 */
public interface ComponentInfoRelationship extends Relationship<ComponentInfo, ComponentInfo> {
  // nothing extra, connects two component infos
}
