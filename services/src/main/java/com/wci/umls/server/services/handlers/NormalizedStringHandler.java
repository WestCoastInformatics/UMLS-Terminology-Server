/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.helpers.Configurable;

/**
 * Generically represents a handler that can lexically normalize a string.
 */
public interface NormalizedStringHandler extends Configurable {

  /**
   * Returns the normalized string.
   *
   * @param string the string
   * @return the normalized string
   * @throws Exception the exception
   */
  public String getNormalizedString(String string) throws Exception;

}
