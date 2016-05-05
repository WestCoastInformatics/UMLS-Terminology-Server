/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.helpers.SearchResultList;

/**
 * The Interface ExpressionHandler.
 */
public interface ExpressionHandler extends Configurable {
  
  /**
   * Gets the count.
   *
   * @param expr the expr
   * @return the count
   * @throws Exception 
   */
  public Integer getCount(String expr) throws Exception;
  
  /**
   * Resolve.
   *
   * @param expr the expr
   * @return the search resultlist
   * @throws Exception the exception
   */
  public SearchResultList resolve(String expr) throws Exception;
  
  /**
   * Parse.
   *
   * @param expr the expr
   * @return the string
   */
  public String parse(String expr);

}
