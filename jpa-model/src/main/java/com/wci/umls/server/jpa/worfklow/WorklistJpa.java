package com.wci.umls.server.jpa.worfklow;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.workflow.Worklist;

/**
 * JPA-enabled implementation of a {@link Worklist}.
 */
@Entity
@Table(name = "worklists", uniqueConstraints = @UniqueConstraint(columnNames = {
    "assignDate", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "worklist")
public class WorklistJpa extends AbstractChecklist implements Worklist {

  
  /** The assign date. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date assignDate = new Date();

  /** The return date. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date returnDate = new Date();

  /** The stamp date. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date stampDate = new Date();

  /** The editor. */
  @Column(nullable = false)
  private String editor;
  
  /** The group. */
  @Column(nullable = false)
  private String worklistGroup;
  
  /** The stamped by. */
  @Column(nullable = false)
  private String stampedBy;
   
  /** The status. */
  @Column(nullable = false)
  private String status;
  
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
    this.assignDate = worklist.getAssignDate();
    this.returnDate = worklist.getReturnDate();
    this.stampDate = worklist.getStampDate();
    this.editor = worklist.getEditor();
    this.worklistGroup = worklist.getWorklistGroup();
    this.stampedBy = worklist.getStampedBy();
    this.setWorkflowBin(worklist.getWorkflowBin());
    if (deepCopy) {
      this.setTrackingRecords(worklist.getTrackingRecords());
    }
  }


  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getEditor() {
    return editor;
  }

  /* see superclass */
  @Override
  public void setEditor(String editor) {
    this.editor = editor;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
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
  public Date getAssignDate() {
   return assignDate;
  }

  /* see superclass */
  @Override
  public void setAssignDate(Date assignDate) {
    this.assignDate = assignDate;
  }

  /* see superclass */
  @Override
  public Date getReturnDate() {
    return returnDate;
  }

  /* see superclass */
  @Override
  public void setReturnDate(Date returnDate) {
    this.returnDate = returnDate;
  }

  /* see superclass */
  @Override
  public Date getStampDate() {
    return stampDate;
  }

  /* see superclass */
  @Override
  public void setStampDate(Date stampDate) {
    this.stampDate = stampDate;
  }

  /* see superclass */
  @Override
  public String getStampedBy() {
    return stampedBy;
  }

  /* see superclass */
  @Override
  public void setStampedBy(String stampedBy) {
    this.stampedBy = stampedBy;
  }


  /* see superclass */
  @Override
  public String getStatus() {
    return status;
  }

  /* see superclass */
  @Override
  public void setStatus(String worklistStatus) {
    this.status = worklistStatus;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((assignDate == null) ? 0 : assignDate.hashCode());
    result = prime * result + ((editor == null) ? 0 : editor.hashCode());
    result = prime * result + ((worklistGroup == null) ? 0 : worklistGroup.hashCode());
    result =
        prime * result + ((returnDate == null) ? 0 : returnDate.hashCode());
    result = prime * result + ((stampDate == null) ? 0 : stampDate.hashCode());
    result = prime * result + ((stampedBy == null) ? 0 : stampedBy.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());

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
    if (assignDate == null) {
      if (other.assignDate != null)
        return false;
    } else if (!assignDate.equals(other.assignDate))
      return false;
    if (editor == null) {
      if (other.editor != null)
        return false;
    } else if (!editor.equals(other.editor))
      return false;
    if (worklistGroup == null) {
      if (other.worklistGroup != null)
        return false;
    } else if (!worklistGroup.equals(other.worklistGroup))
      return false;
    if (returnDate == null) {
      if (other.returnDate != null)
        return false;
    } else if (!returnDate.equals(other.returnDate))
      return false;
    if (stampDate == null) {
      if (other.stampDate != null)
        return false;
    } else if (!stampDate.equals(other.stampDate))
      return false;
    if (stampedBy == null) {
      if (other.stampedBy != null)
        return false;
    } else if (!stampedBy.equals(other.stampedBy))
      return false;
    if (status != other.status)
      return false;
    
    return true;
  }

  @Override
  public String toString() {
    return "WorklistJpa [id=" + getId() + ", assignDate=" + assignDate
        + ", returnDate=" + returnDate + ", stampDate=" + stampDate
        + ", editor=" + editor + ", group=" + worklistGroup + ", stampedBy=" + stampedBy
        + ", status=" + status + "]";
  }

}
