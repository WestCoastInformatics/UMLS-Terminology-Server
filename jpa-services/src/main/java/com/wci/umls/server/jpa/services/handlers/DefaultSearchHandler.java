/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.List;
import java.util.Properties;

import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * Default implementation of {@link SearchHandler}. This provides an algorithm to aide
 * in lucene searches.
 */
public class DefaultSearchHandler implements SearchHandler {

  @Override
  public void setProperties(Properties p) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public <T extends AtomClass> List<T> getLuceneQueryResults(
    String terminology, String version, String branch, String query,
    Class<?> fieldNamesKey, Class<T> clazz, PfsParameter pfs, int[] totalCt)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


}
