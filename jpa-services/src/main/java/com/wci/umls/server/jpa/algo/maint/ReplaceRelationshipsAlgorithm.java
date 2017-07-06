/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.UUID;

import com.wci.umls.server.jpa.algo.insert.RelationshipLoaderAlgorithm;

/**
 * Implementation of an algorithm to remove relationships before importing them.
 */
public class ReplaceRelationshipsAlgorithm extends RelationshipLoaderAlgorithm {

  /**
   * Instantiates an empty {@link ReplaceRelationshipsAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public ReplaceRelationshipsAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("REPLACERELATIONSHIPS");
    setLastModifiedBy("admin");
    // Set replace to true, so the RelationshipLoader will remove existing
    // relationships before reloading them.
    replace = true;
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a - No reset
    logInfo("Finished RESET " + getName());
  }

  @Override
  public String getDescription() {
    return "Loads and processes a relationships.src and contexts.src file to remove existing relationships that match terminology, version, relationship type, and additional relationship type (and their inverses), then load Relationship objects.";
  }

}