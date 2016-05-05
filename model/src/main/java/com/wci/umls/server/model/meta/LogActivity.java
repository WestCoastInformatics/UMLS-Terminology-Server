/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Enum for {@link TermType} code variant types.
 */
public enum LogActivity {

  /** The loader. */
  LOADER("Loader"),

  /** The remover. */
  REMOVER("Remover"),

  /** The editing. */
  EDITING("Editing"),

  /** The release. */
  RELEASE("Release");

  /** The activity. */
  private String activity;

  /**
   * Instantiates a {@link LogActivity} from the specified parameters.
   *
   * @param activity the activity
   */
  private LogActivity(String activity) {
    this.activity = activity;
  }

  /**
   * Returns the activity.
   *
   * @return the activity
   */
  public String getActivity() {
    return activity;
  }

}
