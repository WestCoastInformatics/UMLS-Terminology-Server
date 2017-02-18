/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.report;

import com.wci.umls.server.helpers.HasLastModified;

/**
 * Generically represents an item within a {@link ReportResult}.
 */
public interface ReportResultItem extends HasLastModified {

  /**
   * Returns the report result.
   *
   * @return the report result
   */
  public ReportResult getResult();

  /**
   * Sets the report result.
   *
   * @param result the report result
   */
  public void setResult(ReportResult result);

  /**
   * Gets the item id.
   * 
   * @return the item id
   */
  public String getItemId();

  /**
   * Sets the item id.
   * 
   * @param itemId the new item id
   */
  public void setItemId(String itemId);

  /**
   * Returns the item name.
   *
   * @return the item name
   */
  public String getItemName();

  /**
   * Sets the item name.
   *
   * @param itemName the item name
   */
  public void setItemName(String itemName);

}
