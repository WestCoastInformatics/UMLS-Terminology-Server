/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A semantic type {@link ComponentHasAttributes}.
 */
public interface SemanticTypeComponent extends Component {

  /**
   * Returns the semantic type.
   *
   * @return the semantic type
   */
  public String getSemanticType();

  /**
   * Sets the semantic type.
   *
   * @param semanticType the semantic type
   */
  public void setSemanticType(String semanticType);

  /**
   * Returns the workflow status.
   *
   * @return the workflow status
   */
  public WorkflowStatus getWorkflowStatus();

  /**
   * Sets the workflow status.
   *
   * @param workflowStatus the workflow status
   */
  public void setWorkflowStatus(WorkflowStatus workflowStatus);

}