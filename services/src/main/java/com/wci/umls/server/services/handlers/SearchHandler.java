/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import java.util.List;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.model.content.AtomClass;

/**
 * Generically represents an algorithm for lucene based searches.  Searches for literal 
 * matches first, followed by lucene matches, use of spell check dictionary, use of
 * acronym list and finally use of wildcards.
 */
public interface SearchHandler extends Configurable {

  public <T extends AtomClass> List<T> getLuceneQueryResults(
      String terminology, String version, String branch, String query,
      Class<?> fieldNamesKey, Class<T> clazz, PfsParameter pfs, int[] totalCt)
      throws Exception;
}
