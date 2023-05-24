/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.GeneralMetadataEntry;

/**
 * JPA and JAXB enabled implementation of {@link GeneralMetadataEntry}.
 */
@Entity
@Table(name = "general_metadata_entries")
//@Audited
@XmlRootElement(name = "generalMetadataEntry")
public class GeneralMetadataEntryJpa extends AbstractAbbreviation implements
    GeneralMetadataEntry {

  /** The key. */
  @Column(name = "metadataKey", nullable = false)
  private String key;

  /** The type. */
  @Column(name = "keyType", nullable = false)
  private String type;

  /**
   * Instantiates an empty {@link GeneralMetadataEntryJpa}.
   */
  public GeneralMetadataEntryJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link GeneralMetadataEntryJpa} from the specified
   * parameters.
   *
   * @param entry the entry
   */
  public GeneralMetadataEntryJpa(GeneralMetadataEntry entry) {
    super(entry);
    key = entry.getKey();
    type = entry.getType();
  }

  /* see superclass */
  @Override
  public String getKey() {
    return key;
  }

  /* see superclass */
  @Override
  public void setKey(String key) {
    this.key = key;
  }

  /* see superclass */
  @Override
  public String getType() {
    return type;
  }

  /* see superclass */
  @Override
  public void setType(String type) {
    this.type = type;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    GeneralMetadataEntryJpa other = (GeneralMetadataEntryJpa) obj;
    if (key == null) {
      if (other.key != null)
        return false;
    } else if (!key.equals(other.key))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "GeneralMetadataEntryJpa [key=" + key + ", type=" + type + "] "
        + super.toString();
  }
}
