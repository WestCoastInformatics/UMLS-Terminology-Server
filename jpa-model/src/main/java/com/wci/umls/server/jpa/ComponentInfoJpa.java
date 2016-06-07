package com.wci.umls.server.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.model.meta.IdType;

/**
 * JPA and JAXB enabled implementation of {@link ComponentInfo}.
 */
@Entity
@Table(name = "component_infos", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminology", "version", "terminologyId", "id"
}))
@Audited
@XmlRootElement(name = "componentInfo")
public class ComponentInfoJpa implements ComponentInfo {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator_", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The timestamp. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp = null;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = null;

  /** The last modified by. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The terminology id. */
  @Column(nullable = false)
  private String terminologyId;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The type. */
  @Column(nullable = false)
  private IdType type;

  /**
   * Instantiates a new user favorite jpa.
   */
  public ComponentInfoJpa() {

  }

  /**
   * Constructor from a ~~ (double tilda) delimited string
   * Type~~Terminology~~Version~~TerminologyId~~Name~~LastModified Note: Double
   * tilda required as single tildas appear in component names.
   *
   * @param delimitedString the delimited string
   */
  public ComponentInfoJpa(String delimitedString) {
    String[] fields = delimitedString.split("~~");

    this.setType(IdType.valueOf(fields[0]));
    this.setTerminology(fields[1]);
    this.setVersion(fields[2]);
    this.setTerminologyId(fields[3]);
    this.setName(fields[4]);

    Date date = new Date();
    date.setTime(Long.parseLong(fields[5]));
    this.setTimestamp(date);
  }

  /**
   * Instantiates a new component info JPA object.
   *
   * @param componentInfo the component info
   */
  public ComponentInfoJpa(ComponentInfo componentInfo) {
    super();
    this.timestamp = componentInfo.getTimestamp();
    this.lastModified = componentInfo.getLastModified();
    this.lastModifiedBy = componentInfo.getLastModifiedBy();
    this.terminology = componentInfo.getTerminology();
    this.terminologyId = componentInfo.getTerminologyId();
    this.version = componentInfo.getVersion();
    this.name = componentInfo.getName();
    this.type = componentInfo.getType();
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
  public Date getTimestamp() {
    return timestamp;
  }

  /* see superclass */
  @Override
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  /* see superclass */
  @Override
  public Date getLastModified() {
    return lastModified;
  }

  /* see superclass */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /* see superclass */
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /* see superclass */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /* see superclass */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  public String getTerminologyId() {
    return terminologyId;
  }

  /* see superclass */
  @Override
  public void setTerminologyId(String terminologyId) {
    this.terminologyId = terminologyId;
  }

  /* see superclass */
  @Override
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  public String getName() {
    return this.name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "ComponentInfoJpa [id=" + id + ", timestamp=" + timestamp
        + ", lastModified=" + lastModified + ", lastModifiedBy="
        + lastModifiedBy + ", terminology=" + terminology + ", terminologyId="
        + terminologyId + ", version=" + version + ", name=" + name + ", type="
        + type + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result =
        prime * result
            + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ComponentInfoJpa other = (ComponentInfoJpa) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (terminologyId == null) {
      if (other.terminologyId != null)
        return false;
    } else if (!terminologyId.equals(other.terminologyId))
      return false;
    if (type != other.type)
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public void setType(IdType type) {
    this.type = type;

  }

  /* see superclass */
  @Override
  public IdType getType() {
    return this.type;
  }

}
