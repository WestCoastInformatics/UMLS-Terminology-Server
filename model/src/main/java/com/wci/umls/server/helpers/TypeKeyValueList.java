/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Represents multiple data contexts via using list of {@link TypeKeyValue }.
 * 
 * Useful for sending input and output type key values to REST Server. 
 */
public interface TypeKeyValueList extends ResultList<TypeKeyValue> {
  // nothing extra, a simple wrapper for easy serialization
}
