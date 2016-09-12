/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;

/**
 * The Interface ReportService.
 */
public interface ReportService extends HistoryService {

  /**
   * Returns the concept report.
   *
   * @param concept the concept
   * @return the concept report
   * @throws Exception the exception
   */
  public String getConceptReport(Concept concept) throws Exception;

  /**
   * Returns the descriptor report.
   *
   * @param descriptor the descriptor
   * @return the descriptor report
   * @throws Exception the exception
   */
  public String getDescriptorReport(Descriptor descriptor) throws Exception;

  /**
   * Returns the concept report.
   *
   * @param code the code
   * @return the concept report
   * @throws Exception the exception
   */
  public String getCodeReport(Code code) throws Exception;
}
