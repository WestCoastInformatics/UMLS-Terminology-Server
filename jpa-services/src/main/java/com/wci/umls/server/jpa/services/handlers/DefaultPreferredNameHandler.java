/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Properties;
import java.util.Set;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * Default implementation of {@link ComputePreferredNameHandler}.
 */
public class DefaultPreferredNameHandler implements
    ComputePreferredNameHandler {

  @Override
  public void setProperties(Properties p) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String computePreferredName(Concept concept) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String computePreferredName(Set<Atom> atoms) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isPreferredName(Atom atom) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

}
