/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a classification by case-sensitive string.  Any two
 * atoms with the same string class have the same case-sensitive name.
 */
public interface StringClass extends AtomClass {

  // nothing extra, the "name" is the string
  
}