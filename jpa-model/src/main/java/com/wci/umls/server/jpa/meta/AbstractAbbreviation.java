package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.Abbreviation;

/**
 * Abstract implementation of {@link Abbreviation} for use with JPA.
 */
@Audited
@MappedSuperclass
public abstract class AbstractAbbreviation implements Abbreviation {

  /** The id. */
  @Id
  @GeneratedValue
  private Long id;

  /** The abbreviation. */
  @Column(nullable = false)
  private String abbreviation;

  /** The expandedForm. */
  @Column(nullable = false)
  private String expandedForm;

  /**
   * Instantiates an empty {@link AbstractAbbreviation}.
   */
  protected AbstractAbbreviation() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractAbbreviation} from the specified parameters.
   *
   * @param abbreviation the abbreviation
   */
  protected AbstractAbbreviation(Abbreviation abbreviation) {
    id = abbreviation.getId();
    this.abbreviation = abbreviation.getAbbreviation();
    expandedForm = abbreviation.getExpandedForm();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.rf2.Component#getId()
   */
  @Override
  @XmlTransient
  public Long getId() {
    return this.id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.rf2.Component#setId(java.lang.Long)
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.Abbreviation#getAbbreviation()
   */
  @Override
  public String getAbbreviation() {
    return abbreviation;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.Abbreviation#getExpandedForm()
   */
  @Override
  public String getExpandedForm() {
    return expandedForm;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.Abbreviation#setAbbreviation(java.lang.String
   * )
   */
  @Override
  public void setAbbreviation(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.Abbreviation#setExpandedForm(java.lang.String
   * )
   */
  @Override
  public void setExpandedForm(String expandedForm) {
    this.expandedForm = expandedForm;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((abbreviation == null) ? 0 : abbreviation.hashCode());
    result =
        prime * result + ((expandedForm == null) ? 0 : expandedForm.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractAbbreviation other = (AbstractAbbreviation) obj;
    if (abbreviation == null) {
      if (other.abbreviation != null)
        return false;
    } else if (!abbreviation.equals(other.abbreviation))
      return false;
    if (expandedForm == null) {
      if (other.expandedForm != null)
        return false;
    } else if (!expandedForm.equals(other.expandedForm))
      return false;
    return true;
  }

}
