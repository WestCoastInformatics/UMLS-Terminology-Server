package com.wci.umls.server.jpa.worfklow;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Indexed;

import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.TrackingRecord;

/**
 * JPA-enabled implementation of a {@link Checklist}.
 */
@Entity
@Table(name = "checklists", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "project_id"
}))
@Indexed
@XmlRootElement(name = "checklist")
public class ChecklistJpa extends AbstractChecklist {

  /** The tracking records. */
  @OneToMany(targetEntity = TrackingRecordJpa.class)
  private List<TrackingRecord> trackingRecords = new ArrayList<>();

  
  /**
   * Instantiates an empty {@link ChecklistJpa}.
   */
  public ChecklistJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ChecklistJpa} from the specified parameters.
   *
   * @param checklist the checklist
   * @param deepCopy the deep copy
   */
  public ChecklistJpa(Checklist checklist, boolean deepCopy) {
    super(checklist);
    if (deepCopy) {
      trackingRecords = new ArrayList<>(checklist.getTrackingRecords());
    }
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
}
