/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.UUID;

import com.wci.umls.server.jpa.algo.insert.AttributeLoaderAlgorithm;

/**
 * Implementation of an algorithm to remove attributes before importing them.
 */
public class ReplaceAttributesAlgorithm extends AttributeLoaderAlgorithm {

  /**
   * Instantiates an empty {@link ReplaceAttributesAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public ReplaceAttributesAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("REPLACEATTRIBUTES");
    setLastModifiedBy("admin");
    // Set replace to true, so the AttributeLoader will remove existing
    // attributes before reloading them.
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
    return "Loads and processes an attributes.src file to remove existing attributes that match terminology, version, and attribute-name, then load Attribute objects.";
  }

}