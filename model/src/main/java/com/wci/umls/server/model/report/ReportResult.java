package com.wci.umls.server.model.report;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;

/**
 * Generically represents a result in a report.
 */
public interface ReportResult extends HasLastModified {

  /**
   * Returns the report.
   *
   * @return the report
   */
  public Report getReport();

  /**
   * Sets the report.
   *
   * @param report the report
   */
  public void setReport(Report report);

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value.
   *
   * @param value the value
   */
  public void setValue(String value);

  /**
   * Returns the ct.
   *
   * @return the ct
   */
  public long getCt();

  /**
   * Sets the ct.
   *
   * @param ct the ct
   */
  public void setCt(long ct);

  /**
   * Returns the report result items.
   *
   * @return the report result items
   */
  public List<ReportResultItem> getResultItems();

  /**
   * Sets the report result items.
   *
   * @param reportResultItems the report result items
   */
  public void setResultItems(List<ReportResultItem> reportResultItems);

}
