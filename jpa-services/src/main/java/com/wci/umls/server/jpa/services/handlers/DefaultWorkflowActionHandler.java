/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Properties;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.WorkflowService;
import com.wci.umls.server.services.handlers.WorkflowActionHandler;

/**
 * Default implementation of {@link WorkflowActionHandler}.
 */
public class DefaultWorkflowActionHandler implements WorkflowActionHandler {

  /**
   * Instantiates an empty {@link DefaultWorkflowActionHandler}.
   *
   * @throws Exception the exception
   */
  public DefaultWorkflowActionHandler() throws Exception {
    super();
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    // n/a
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Default workflow handler";
  }


  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Project refset, TrackingRecord trackingRecord,
    User user, UserRole userRole, WorkflowAction workflowAction)
    throws Exception {

    return new TrackingRecordJpa();
  }

  @Override
  public TrackingRecordList findAvailableWork(Project project, UserRole role,
    PfsParameter pfs, WorkflowService service) throws Exception {
    final StringBuilder sb = new StringBuilder();
    
    if (project == null) {
      sb.append("projectId:[* TO *]");
    } else {
      sb.append("projectId:" + project.getId());
    }
    sb.append(" AND ");
    if (role == null) {
      sb.append("userRole:[* TO *]");
    } else {
      sb.append("userRole:" + role.name());
    }   
    sb.append(" AND ").append("( NOT worklist:[* TO *])");
          

    return service.findTrackingRecordsForQuery(sb.toString(), pfs);
    
  }

  @Override
  public WorklistList findAvailableWorklists(Project project, UserRole role,
    PfsParameter pfs, WorkflowService service) throws Exception {
    
    final StringBuilder sb = new StringBuilder();

    if (project == null) {
      sb.append("projectId:[* TO *]");
    } else {
      sb.append("projectId:" + project.getId());
    }
    sb.append(" AND ");
    if (role == null) {
      sb.append("userRole:[* TO *]");
    } else {
      sb.append("userRole:" + role.name());
    }  
    sb.append(" AND ").append("(NOT editor:[* TO *])");
        
    return service.findWorklistsForQuery(sb.toString(), pfs);

  }


  @Override
  public ValidationResult validateWorkflowAction(Project project,
    Worklist worklist, User user, UserRole userRole,
    WorkflowAction workflowAction) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TrackingRecord validateWorkflowAction(Project project,
    TrackingRecord trackingRecord, User user, UserRole userRole,
    WorkflowAction workflowAction) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Worklist performWorkflowAction(Project project, Worklist worklist,
    User user, UserRole userRole, WorkflowAction workflowAction)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TrackingRecordList findAssignedWork(Project project, String userName,
    PfsParameter pfs, WorkflowService service) throws Exception {
    final StringBuilder sb = new StringBuilder();
    
    if (project == null) {
      sb.append("projectId:[* TO *]");
    } else {
      sb.append("projectId:" + project.getId());
    }
    sb.append(" AND ");
    if (userName == null || userName.equals("")) {
      sb.append("userName:[* TO *]");
    } else {
      sb.append("userName:" + userName);
    }   
    sb.append(" AND ").append("worklist:[* TO *]");
          

    return service.findTrackingRecordsForQuery(sb.toString(), pfs);
  }

  @Override
  public WorklistList findAssignedWorklists(Project project, String userName,
    PfsParameter pfs, WorkflowService service) throws Exception {
    final StringBuilder sb = new StringBuilder();

    if (project == null) {
      sb.append("projectId:[* TO *]");
    } else {
      sb.append("projectId:" + project.getId());
    }
    
    sb.append(" AND ").append(" editor:").append(userName);
        
    return service.findWorklistsForQuery(sb.toString(), pfs);
  }


}
