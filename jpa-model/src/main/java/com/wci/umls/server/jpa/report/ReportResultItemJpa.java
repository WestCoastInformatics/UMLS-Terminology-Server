/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.report;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.wci.umls.server.jpa.content.AbstractHasLastModified;
import com.wci.umls.server.model.report.ReportResult;
import com.wci.umls.server.model.report.ReportResultItem;

/**
 * The Class ReportResultItemJpa.
 */
@Entity
@Table(name = "report_result_items")
@XmlRootElement(name = "reportResultItem")
public class ReportResultItemJpa extends AbstractHasLastModified
    implements ReportResultItem {

  /** The id. */
  @Id
  @GeneratedValue
  private Long id;

  /** The item id. */
  @Column(nullable = false)
  private String itemId;

  /** The item name. */
  @Column(nullable = false)
  private String itemName;

  /** The report result. */
  // @ContainedIn
  @ManyToOne(targetEntity = ReportResultJpa.class, optional = false)
  private ReportResult result;

  /**
   * Instantiates an empty {@link ReportResultItemJpa}.
   */
  public ReportResultItemJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link ReportResultItemJpa} from the specified parameters.
   *
   * @param item the item
   */
  public ReportResultItemJpa(ReportResultItem item) {
    super(item);
    id = item.getId();
    itemId = item.getItemId();
    itemName = item.getItemName();
    result = item.getResult();
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
  public ReportResult getResult() {
    return result;
  }

  /* see superclass */
  @Override
  public void setResult(ReportResult result) {
    this.result = result;
  }

  /* see superclass */
  @Override
  public String getItemId() {
    return itemId;
  }

  /* see superclass */
  @Override
  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  /* see superclass */
  @Override
  public String getItemName() {
    return this.itemName;
  }

  /* see superclass */
  @Override
  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
    result = prime * result + ((itemName == null) ? 0 : itemName.hashCode());
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
    ReportResultItemJpa other = (ReportResultItemJpa) obj;
    if (itemId == null) {
      if (other.itemId != null)
        return false;
    } else if (!itemId.equals(other.itemId))
      return false;
    if (itemName == null) {
      if (other.itemName != null)
        return false;
    } else if (!itemName.equals(other.itemName))
      return false;

    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ReportResultItemJpa [id=" + id + ", itemId=" + itemId
        + ", itemName=" + itemName + ", result=" + result + "]";
  }

}
