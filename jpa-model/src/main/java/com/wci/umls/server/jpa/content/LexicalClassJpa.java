/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.content.LexicalClass;

/**
 * JPA-enabled implementation of {@link LexicalClass}.
 */
@Entity
@Table(name = "lexical_classes", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "lexicalClass")
@Indexed
public class LexicalClassJpa extends AbstractAtomClass implements LexicalClass {

  /** The normalized string. */
  @Column(nullable = true, length = 4000)
  private String normalizedString;

  /**
   * Instantiates an empty {@link LexicalClassJpa}.
   */
  public LexicalClassJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link LexicalClassJpa} from the specified parameters.
   *
   * @param code the code
   * @param deepCopy the deep copy
   */
  public LexicalClassJpa(LexicalClass code, boolean deepCopy) {
    super(code, deepCopy);
  }

  /**
   * Returns the normalized string.
   *
   * @return the normalized string
   */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "normalizedStringSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @Analyzer(definition = "noStopWord")
  public String getNormalizedString() {
    return normalizedString;
  }

  /**
   * Sets the normalized string.
   *
   * @param normalizedString the normalized string
   */
  @Override
  public void setNormalizedString(String normalizedString) {
    this.normalizedString = normalizedString;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractAtomClass#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result
            + ((normalizedString == null) ? 0 : normalizedString.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractAtomClass#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    LexicalClassJpa other = (LexicalClassJpa) obj;
    if (normalizedString == null) {
      if (other.normalizedString != null)
        return false;
    } else if (!normalizedString.equals(other.normalizedString))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractAtomClass#toString()
   */
  @Override
  public String toString() {
    return "LexicalClassJpa [normalizedString=" + normalizedString + "]";
  }
}
