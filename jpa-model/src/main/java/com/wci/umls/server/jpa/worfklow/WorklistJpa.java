/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.jpa.helpers.CollectionToCsvBridge;
import com.wci.umls.server.jpa.helpers.MapValueToCsvBridge;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * JPA-enabled implementation of a {@link Worklist}.
 */
@Entity
@Table(name = "worklists", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "description", "workflowBinName"
}))
@Audited
@Indexed
@XmlRootElement(name = "worklist")
public class WorklistJpa extends AbstractChecklist implements Worklist {

  /** The authors. */
  @ElementCollection
  @CollectionTable(name = "worklist_authors")
  private List<String> authors = new ArrayList<>();

  /** The reviewers. */
  @ElementCollection
  @CollectionTable(name = "worklist_reviewers")
  private List<String> reviewers = new ArrayList<>();

  /** The group. */
  @Column(nullable = true)
  private String worklistGroup;

  /** The workflow bin. */
  @Column(nullable = true)
  private String workflowBinName;

  /** The workflow status. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkflowStatus workflowStatus;
  
  /**  The number. */
  @Column(nullable = false)
  private int number;

  /**  The workflow state history. */
  @ElementCollection
  @Column(nullable = false)
  private Map<String, Date> workflowStateHistory = new HashMap<>();

  /**
   * Instantiates an empty {@link WorklistJpa}.
   */
  public WorklistJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link WorklistJpa} from the specified parameters.
   *
   * @param worklist the worklist
   * @param deepCopy the deep copy
   */
  public WorklistJpa(Worklist worklist, boolean deepCopy) {
    super(worklist, deepCopy);
    authors = worklist.getAuthors();
    reviewers = worklist.getReviewers();
    worklistGroup = worklist.getWorklistGroup();
    workflowStatus = worklist.getWorkflowStatus();
    workflowBinName = worklist.getWorkflowBin();
    number = worklist.getNumber();
    if (deepCopy) {
      workflowStateHistory = worklist.getWorkflowStateHistory();
    }
  }

  @Override
  public int getNumber() {
    return number;
  }
  
  @Override
  public void setNumber(int number) {
    this.number = number;
  }
  
  /* see superclass */
  @Field(bridge = @FieldBridge(impl = CollectionToCsvBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public List<String> getAuthors() {
    if (authors == null) {
      authors = new ArrayList<>();
    }
    return authors;
  }

  /* see superclass */
  @Override
  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }

  @Override
  @FieldBridge(impl = MapValueToCsvBridge.class)
  @Field(name = "workflowStateHistoryMap", index = Index.YES, analyze = Analyze.YES, store = Store.NO)  
  public Map<String, Date> getWorkflowStateHistory() {
    if (workflowStateHistory == null) {
      workflowStateHistory = new HashMap<>();
    }
    return workflowStateHistory;
  }
  
  @Override
  public void setWorkflowStateHistory(Map<String, Date> workflowStateHistory) {
    this.workflowStateHistory = workflowStateHistory;
  }
  
  /* see superclass */
  @Field(bridge = @FieldBridge(impl = CollectionToCsvBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public List<String> getReviewers() {
    if (reviewers == null) {
      reviewers = new ArrayList<>();
    }
    return reviewers;
  }

  /* see superclass */
  @Override
  public void setReviewers(List<String> reviewers) {
    this.reviewers = reviewers;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  /* see superclass */
  @Override
  public void setWorkflowStatus(WorkflowStatus workflowStatus) {
    this.workflowStatus = workflowStatus;

  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getWorklistGroup() {
    return worklistGroup;
  }

  /* see superclass */
  @Override
  public void setWorklistGroup(String group) {
    this.worklistGroup = group;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getWorkflowBin() {
    return workflowBinName;
  }

  /* see superclass */
  @Override
  public void setWorkflowBin(String workflowBin) {
    this.workflowBinName = workflowBin;
  }





  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((authors == null) ? 0 : authors.hashCode());
    result = prime * result + number;
    result = prime * result + ((reviewers == null) ? 0 : reviewers.hashCode());
    result = prime * result
        + ((workflowBinName == null) ? 0 : workflowBinName.hashCode());
    result = prime * result
        + ((worklistGroup == null) ? 0 : worklistGroup.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    WorklistJpa other = (WorklistJpa) obj;
    if (authors == null) {
      if (other.authors != null)
        return false;
    } else if (!authors.equals(other.authors))
      return false;
    if (number != other.number)
      return false;
    if (reviewers == null) {
      if (other.reviewers != null)
        return false;
    } else if (!reviewers.equals(other.reviewers))
      return false;
    if (workflowBinName == null) {
      if (other.workflowBinName != null)
        return false;
    } else if (!workflowBinName.equals(other.workflowBinName))
      return false;
    if (worklistGroup == null) {
      if (other.worklistGroup != null)
        return false;
    } else if (!worklistGroup.equals(other.worklistGroup))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "WorklistJpa [authors=" + authors + ", reviewers=" + reviewers
        + ", worklistGroup=" + worklistGroup + ", workflowBin="
            + workflowBinName + ", workflowStatus="
        + workflowStatus +  ", number=" + number + ", " + 
            "workflowStateHistory=" + workflowStateHistory + "]";
  }

}
