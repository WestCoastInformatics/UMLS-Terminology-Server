package com.wci.umls.server.jpa.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;

/**
 * JPA and JAXB enabled implementation of a {@link MolecularAction}.
 */
@Entity
@Table(name = "molecular_actions", uniqueConstraints = @UniqueConstraint(columnNames = {
  "id"
}))
@Indexed
@XmlRootElement(name = "molecularActions")
// TODO Add a second terminology id field (of some awesome name) for the second
// concept involved in e.g. a relationship change, a merge change, a split
// change, etc. Don't use source/target.
public class MolecularActionJpa implements MolecularAction {

  /** The id. */
  @TableGenerator(name = "EntityIdGenAction", table = "table_generator_action", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGenAction")
  private Long id;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The terminology id. */
  @Column(nullable = false)
  private String terminologyId;

  /** The terminology id 2. */
  @Column(nullable = true)
  private String terminologyId2;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = new Date();

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The timestamp. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp = null;

  /** The macro action. */
  @Column(nullable = false)
  private boolean macroAction;

  /** The molecular action. */
  @IndexedEmbedded(targetElement = AtomicActionJpa.class)
  @OneToMany(mappedBy = "id", targetEntity = AtomicActionJpa.class)
  private List<AtomicAction> atomicActions = new ArrayList<>();

  /**
   * Instantiates an empty {@link MolecularActionJpa}.
   */
  public MolecularActionJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link MolecularActionJpa} from the specified parameters.
   *
   * @param action the action
   * @param deepCopy the deep copy
   */
  public MolecularActionJpa(MolecularAction action, boolean deepCopy) {
    id = action.getId();
    lastModified = action.getLastModified();
    lastModifiedBy = action.getLastModifiedBy();
    version = action.getVersion();
    version = action.getVersion();
    terminologyId = action.getTerminologyId();
    terminologyId2 = action.getTerminologyId2();
    terminology = action.getTerminology();
    macroAction = action.isMacroAction();
    name = action.getName();
    if (deepCopy) {
      atomicActions = new ArrayList<>(action.getAtomicActions());
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTerminologyId2() {
    return terminologyId2;
  }

  /* see superclass */
  @Override
  public void setTerminologyId2(String terminologyId2) {
    this.terminologyId2 = terminologyId2;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  @XmlTransient
  public List<AtomicAction> getAtomicActions() {
    if (atomicActions == null) {
      atomicActions = new ArrayList<>();
    }
    return atomicActions;
  }

  /* see superclass */
  @Override
  public void setAtomicActions(List<AtomicAction> atomicActions) {
    this.atomicActions = atomicActions;
  }

  /* see superclass */
  @Override
  public boolean isMacroAction() {
    return macroAction;
  }

  /* see superclass */
  @Override
  public void setMacroAction(boolean macroAction) {
    this.macroAction = macroAction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((atomicActions == null) ? 0 : atomicActions.hashCode());
    result =
        prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
    result = prime * result
        + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
    result = prime * result + (macroAction ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result
        + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result
        + ((terminologyId2 == null) ? 0 : terminologyId2.hashCode());
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
    MolecularActionJpa other = (MolecularActionJpa) obj;
    if (atomicActions == null) {
      if (other.atomicActions != null)
        return false;
    } else if (!atomicActions.equals(other.atomicActions))
      return false;
    if (lastModified == null) {
      if (other.lastModified != null)
        return false;
    } else if (!lastModified.equals(other.lastModified))
      return false;
    if (lastModifiedBy == null) {
      if (other.lastModifiedBy != null)
        return false;
    } else if (!lastModifiedBy.equals(other.lastModifiedBy))
      return false;
    if (macroAction != other.macroAction)
      return false;
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
    if (terminologyId2 == null) {
      if (other.terminologyId2 != null)
        return false;
    } else if (!terminologyId2.equals(other.terminologyId2))
      return false;
    if (timestamp == null) {
      if (other.timestamp != null)
        return false;
    } else if (!timestamp.equals(other.timestamp))
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
    return "MolecularActionJpa [id=" + id + ", version=" + version
        + ", terminologyId=" + terminologyId + ", terminology=" + terminology
        + ", name=" + name + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", timestamp=" + timestamp
        + ", macroAction=" + macroAction + ", atomicActions=" + atomicActions
        + "]";
  }

}
