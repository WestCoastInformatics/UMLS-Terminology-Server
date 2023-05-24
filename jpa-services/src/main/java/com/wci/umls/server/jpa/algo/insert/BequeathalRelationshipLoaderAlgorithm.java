/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;

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
  }
  
  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();

    // fileName
    AlgorithmParameter param = new AlgorithmParameterJpa("File name", "fileName",
        "The bequeathal file to be loaded", "e.g. bequeathal.relationships.src", 100,
        AlgorithmParameter.Type.STRING, "");
    params.add(param);

    return params;
  }
  
  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    if (p.getProperty("fileName") != null) {
      fileName = String.valueOf(p.getProperty("fileName"));
    }
  }
}