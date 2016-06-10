package com.wci.umls.server.jpa.actions;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.meta.IdType;

/**
 * The Class AtomicActionJpa.
 */
/**
 * JPA and JAXB enabled implementation of a {@link AtomicAction}.
 */
@Entity
@Table(name = "atomic_actions", uniqueConstraints = @UniqueConstraint(columnNames = {
     "id"
}))
@Indexed
@XmlRootElement(name = "atomicActions")
public class AtomicActionJpa implements AtomicAction {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;
  
  /** The version. */
  @Column(nullable = false)
  private String version;
  
  /** The terminology id. */
  @Column(nullable = false)
  private String terminologyId;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The old value. */
  @Column(nullable = true)
  private String oldValue;

  /** The new value. */
  @Column(nullable = true)
  private String newValue;

  /** The field. */
  @Column(nullable = false)
  private String field;
  
  /** The type. */
  @Column(nullable = false)
  private IdType type;
  
  /** The molecular action. */
  @ManyToOne(targetEntity = AtomicActionJpa.class, optional = false)
  private MolecularAction molecularAction;
  
  /**
   * Instantiates a new atomic action jpa.
   */
  public AtomicActionJpa() {
    // do nothing
  }
  

  /**
   * Instantiates a new atomic action jpa.
   *
   * @param atomicAction the atomic action
   */
  public AtomicActionJpa(AtomicAction atomicAction) {
    super();
    this.version = atomicAction.getVersion();
    this.terminologyId = atomicAction.getTerminologyId();
    this.terminology = atomicAction.getTerminology();
    this.oldValue = atomicAction.getOldValue();
    this.newValue = atomicAction.getNewValue();
    this.field = atomicAction.getField();
    this.type = atomicAction.getIdType();
    this.molecularAction = atomicAction.getMolecularAction();
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
  @XmlTransient
  public MolecularAction getMolecularAction() {
    return molecularAction;
  }

  /* see superclass */
  @Override
  public void setMolecularAction(MolecularAction molecularAction) {
    this.molecularAction = molecularAction;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public IdType getIdType() {
    return type;
  }

  /* see superclass */
  @Override
  public void setIdType(IdType idType) {
    this.type = idType;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getField() {
    return field;
  }

  /* see superclass */
  @Override
  public void setField(String field) {
    this.field = field;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getOldValue() {
    return oldValue;
  }

  /* see superclass */
  @Override
  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getNewValue() {
    return newValue;
  }

  /* see superclass */
  @Override
  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }


  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((field == null) ? 0 : field.hashCode());
    result = prime * result
        + ((molecularAction == null) ? 0 : molecularAction.hashCode());
    result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
    result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result
        + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    AtomicActionJpa other = (AtomicActionJpa) obj;
    if (field == null) {
      if (other.field != null)
        return false;
    } else if (!field.equals(other.field))
      return false;
    if (molecularAction == null) {
      if (other.molecularAction != null)
        return false;
    } else if (!molecularAction.equals(other.molecularAction))
      return false;
    if (newValue == null) {
      if (other.newValue != null)
        return false;
    } else if (!newValue.equals(other.newValue))
      return false;
    if (oldValue == null) {
      if (other.oldValue != null)
        return false;
    } else if (!oldValue.equals(other.oldValue))
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
  public String toString() {
    return "AtomicActionJpa [version=" + version + ", terminologyId="
        + terminologyId + ", terminology=" + terminology + ", oldValue="
        + oldValue + ", newValue=" + newValue + ", field=" + field + ", type="
        + type + ", molecularAction=" + molecularAction + "]";
  }

}
