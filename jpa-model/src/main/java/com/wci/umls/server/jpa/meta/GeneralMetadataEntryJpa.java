package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.GeneralMetadataEntry;

/**
 * JPA-enabled implementation of {@link GeneralMetadataEntry}.
 */
@Entity
@Table(name = "general_metadata_entries", uniqueConstraints = @UniqueConstraint(columnNames = {
    "metadataKey", "keyType", "abbreviation"
}))
@Audited
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.GeneralMetadataEntry#getKey()
   */
  @Override
  public String getKey() {
    return key;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.GeneralMetadataEntry#setKey(java.lang.String
   * )
   */
  @Override
  public void setKey(String key) {
    this.key = key;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.GeneralMetadataEntry#getType()
   */
  @Override
  public String getType() {
    return type;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.GeneralMetadataEntry#setType(java.lang.String
   * )
   */
  @Override
  public void setType(String type) {
    this.type = type;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.meta.AbstractAbbreviation#equals(java.lang.Object)
   */
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
}
