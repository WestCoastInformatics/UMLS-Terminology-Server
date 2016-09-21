/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.meta.IdType;

/**
 * Abstract implementation of {@link ComponentHasAttributes} for use with JPA.
 */
@Audited
@MappedSuperclass
@XmlSeeAlso({
    ConceptJpa.class
})
public abstract class AbstractComponent extends AbstractHasLastModified
    implements Component {

  /** The id. */
  @Id
  @GenericGenerator(name = "ExistingOrGeneratedId", strategy = "com.wci.umls.server.jpa.helpers.UseExistingOrGenerateIdGenerator")
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "ExistingOrGeneratedId")
  private Long id;

  /** The suppressible flag. */
  @Column(nullable = false)
  private boolean suppressible = false;

  /** The obsolete flag. */
  @Column(nullable = false)
  private boolean obsolete = false;

  /** The published flag. */
  @Column(nullable = false)
  private boolean published = false;

  /** The publishable flag. */
  @Column(nullable = false)
  private boolean publishable = true;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The terminology id. */
  @Column(nullable = false)
  private String terminologyId;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The branch set to include empty branch. */
  @Column(nullable = true)
  private String branch = Branch.ROOT;

  /**
   * Instantiates an empty {@link AbstractComponent}.
   */
  public AbstractComponent() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractComponent} from the specified parameters.
   *
   * @param component the component
   */
  public AbstractComponent(Component component) {
    super(component);
    id = component.getId();
    terminology = component.getTerminology();
    terminologyId = component.getTerminologyId();
    version = component.getVersion();
    publishable = component.isPublishable();
    published = component.isPublished();
    obsolete = component.isObsolete();
    suppressible = component.isSuppressible();
    branch = component.getBranch();
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getId() {
    return this.id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  @Field(name = "suppressible", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isSuppressible() {
    return suppressible;
  }

  /* see superclass */
  @Override
  public void setSuppressible(boolean suppressible) {
    this.suppressible = suppressible;
  }

  /* see superclass */
  @Field(name = "obsolete", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public boolean isObsolete() {
    return obsolete;
  }

  /**
   * Indicates whether or not active is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Field(name = "active", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  private boolean isActive() {
    return !obsolete;
  }

  /* see superclass */
  @Override
  public void setObsolete(boolean obsolete) {
    this.obsolete = obsolete;
  }

  /* see superclass */
  @Override
  @Field(name = "published", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isPublished() {
    return published;
  }

  /* see superclass */
  @Override
  public void setPublished(boolean published) {
    this.published = published;
  }

  /* see superclass */
  @Override
  @Field(name = "publishable", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isPublishable() {
    return publishable;
  }

  /* see superclass */
  @Override
  public void setPublishable(boolean publishable) {
    this.publishable = publishable;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getBranch() {
    return branch;
  }

  /* see superclass */
  @Override
  public void setBranch(String branch) {
    this.branch = branch;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTerminologyId() {
    return terminologyId;
  }

  /* see superclass */
  @Override
  public void setTerminologyId(String terminologyId) {
    this.terminologyId = terminologyId;
  }

  @Override
  public void setType(IdType type) {
    // n/a
  }

  /* see superclass */
  @Override
  public IdType getType() throws Exception {
    return IdType.getIdType(getClass());
  }

  /* see superclass */
  @Override
  public String getName() {
    return null;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    // n/a
  }

  /**
   * CUSTOM equals: uses .toString() on the concept terminology ids map.
   *
   * @return the int
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (obsolete ? 1231 : 1237);
    result = prime * result + (publishable ? 1231 : 1237);
    result = prime * result + (published ? 1231 : 1237);
    result = prime * result + (suppressible ? 1231 : 1237);
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result
        + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    AbstractComponent other = (AbstractComponent) obj;

    if (obsolete != other.obsolete)
      return false;
    if (publishable != other.publishable)
      return false;
    if (published != other.published)
      return false;
    if (suppressible != other.suppressible)
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
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "id=" + id + ", terminologyId=" + terminologyId + ", lastModified="
        + getLastModified() + ", lastModifiedBy=" + getLastModifiedBy()
        + ", suppressible=" + suppressible + ", obsolete=" + obsolete
        + ", published=" + published + ", publishable=" + publishable
        + ", terminology=" + terminology + ", version=" + version + ", branch="
        + branch;
  }

}
