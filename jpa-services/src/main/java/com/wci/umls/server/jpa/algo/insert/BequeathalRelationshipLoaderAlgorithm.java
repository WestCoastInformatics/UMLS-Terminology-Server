/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.util.UUID;

/**
 * Implementation of an algorithm to create bequeathal relationships.
 */
public class BequeathalRelationshipLoaderAlgorithm
    extends RelationshipLoaderAlgorithm {

  /**
   * Instantiates an empty {@link BequeathalRelationshipLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public BequeathalRelationshipLoaderAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("BEQUEATHALRELATIONSHIPLOADER");
    setLastModifiedBy("admin");
    // Set bequeathalRels to true and filename to bequeathal name, so the
    // RelationshipLoader will create only bequeathal relationships
    bequeathalRels = true;
    fileName = "bequeathal.relationships.src";
  }
}