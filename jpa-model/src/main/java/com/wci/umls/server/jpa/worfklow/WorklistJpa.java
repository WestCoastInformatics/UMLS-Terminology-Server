package com.wci.umls.server.jpa.worfklow;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.jpa.helpers.CollectionToCsvBridge;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * JPA-enabled implementation of a {@link Worklist}.
 */
@Entity
@Table(name = "worklists", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "description", "workflowBin_id"
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

  /** The status. */
  @Column(nullable = true)
  private String status;

  /** The tracking records. */
  @OneToMany(mappedBy = "worklist", targetEntity = TrackingRecordJpa.class)
  private List<TrackingRecord> trackingRecords = new ArrayList<>();
 
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
    super(worklist);
    authors = worklist.getAuthors();
    reviewers = worklist.getReviewers();
    worklistGroup = worklist.getWorklistGroup();
    status = worklist.getStatus();
    if (deepCopy) {
      trackingRecords = new ArrayList<>(worklist.getTrackingRecords());
    }
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
  public String getWorklistGroup() {
    return worklistGroup;
  }

  /* see superclass */
  @Override
  public void setWorklistGroup(String group) {
    this.worklistGroup = group;
  }



  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getStatus() {
    return status;
  }

  /* see superclass */
  @Override
  public void setStatus(String worklistStatus) {
    this.status = worklistStatus;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public List<TrackingRecord> getTrackingRecords() {
    if (trackingRecords == null) {
      return new ArrayList<>();
    }
    return trackingRecords;
  }

  /* see superclass */
  @Override
  public void setTrackingRecords(List<TrackingRecord> records) {
    this.trackingRecords = records;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((authors == null) ? 0 : authors.hashCode());
    result = prime * result + ((reviewers == null) ? 0 : reviewers.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result
        + ((trackingRecords == null) ? 0 : trackingRecords.hashCode());
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
    if (reviewers == null) {
      if (other.reviewers != null)
        return false;
    } else if (!reviewers.equals(other.reviewers))
      return false;
    if (status == null) {
      if (other.status != null)
        return false;
    } else if (!status.equals(other.status))
      return false;
    if (trackingRecords == null) {
      if (other.trackingRecords != null)
        return false;
    } else if (!trackingRecords.equals(other.trackingRecords))
      return false;
    if (worklistGroup == null) {
      if (other.worklistGroup != null)
        return false;
    } else if (!worklistGroup.equals(other.worklistGroup))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "WorklistJpa [authors=" + authors + ", reviewers=" + reviewers
        + ", worklistGroup=" + worklistGroup + ", status=" + status
        + ", trackingRecords=" + trackingRecords + "]";
  }




}
