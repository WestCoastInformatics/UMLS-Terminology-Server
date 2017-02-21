/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.report;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;

import com.wci.umls.server.jpa.content.AbstractHasLastModified;
import com.wci.umls.server.model.report.Report;
import com.wci.umls.server.model.report.ReportResult;
import com.wci.umls.server.model.report.ReportResultItem;

/**
 * JPA enabled implementation of {@link ReportResultJpa}.
 */
@Entity
@Table(name = "report_results")
@XmlRootElement(name = "reportResult")
public class ReportResultJpa extends AbstractHasLastModified
    implements ReportResult {

  /** The id. */
  @Id
  @GenericGenerator(name = "ExistingOrGeneratedId", strategy = "com.wci.umls.server.jpa.helpers.UseExistingOrGenerateIdGenerator")
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "ExistingOrGeneratedId")
  private Long id;

  /** The report. */
  @ManyToOne(targetEntity = ReportJpa.class, optional = false)
  // @ContainedIn
  private Report report;

  /** The value. */
  @Column(nullable = false)
  private String value;

  /** The count of items, defaults to zero. */
  @Column(nullable = false)
  private long ct = 0;

  /**
   * The report result items. NOTE: These are set to @XmlTransient below due to
   * the potentially huge size of the list
   */
  @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, targetEntity = ReportResultItemJpa.class)
  // @IndexedEmbedded(targetElement = ReportResultItemJpa.class)
  private List<ReportResultItem> resultItems = new ArrayList<>();

  /**
   * Instantiates an empty {@link ReportResultJpa}.
   */
  public ReportResultJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link ReportResultJpa} from the specified parameters.
   *
   * @param result the report result
   * @param collectionsFlag the collections flag
   */
  public ReportResultJpa(ReportResult result, boolean collectionsFlag) {
    super(result);
    id = result.getId();
    report = result.getReport();
    value = result.getValue();
    ct = result.getCt();
    if (collectionsFlag) {
      resultItems = new ArrayList<>(result.getResultItems());
    }
  }

  /* see superclass */
  @Override
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  @XmlTransient
  public Report getReport() {
    return report;
  }

  /* see superclass */
  @Override
  public void setReport(Report report) {
    this.report = report;
  }

  /* see superclass */
  @Override
  public String getValue() {
    return value;
  }

  /* see superclass */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /* see superclass */
  @Override
  public long getCt() {
    // always set ct to the size of the set
    if (resultItems != null) {
      ct = resultItems.size();
    }
    return ct;
  }

  /* see superclass */
  @Override
  public void setCt(long ct) {
    this.ct = ct;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public List<ReportResultItem> getResultItems() {
    if (resultItems == null) {
      resultItems = new ArrayList<>();
    }
    return resultItems;
  }

  /* see superclass */
  @Override
  public void setResultItems(List<ReportResultItem> reportResultItems) {
    this.resultItems = reportResultItems;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (ct ^ (ct >>> 32));
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ReportResultJpa other = (ReportResultJpa) obj;
    if (ct != other.ct)
      return false;

    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ReportResultJpa [id=" + id + ", report=" + report + ", value="
        + value + ", ct=" + ct + ", resultItems=" + resultItems + "]";
  }

}
