/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Constants for branch management.
 */
public class Branch {

  /** The root branch. */
  public static String ROOT = "$";

  /** The branchedTo separator. */
  public static String SEPARATOR = ",";

  /**
   * Returns the sub branches in decreasing levels. For example, for "$.1.2.3"
   * it returns
   * 
   * <pre>
   * $.1.2
   * $.1
   * $
   * </pre>
   *
   * @param branch the branch
   * @return the sub branches
   */
  public static List<String> getSubBranches(String branch) {
    String[] tokens = FieldedStringTokenizer.split(branch, ".");
    List<String> result = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    for (String token : tokens) {
      sb.append(token);
      result.add(sb.toString());
      sb.append(".");
    }
    // reverse alphabetical order
    Collections.sort(result, new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return o2.compareTo(o1);
      }
    });
    return result;

  }
}
