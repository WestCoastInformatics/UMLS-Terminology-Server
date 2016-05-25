package com.wci.umls.server.jpa.content;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.model.content.Code;

/**
 * The Class CodeNoteJpa.
 */
@Entity
@Table(name = "code_notes")
@Audited
@Indexed
@XmlRootElement(name = "codeNote")
public class CodeNoteJpa extends AbstractNote {

  /** The code. */
  @ManyToOne(targetEntity = CodeJpa.class, optional = false)
  private Code code = null;

  /**
   * Instantiates a new code note jpa.
   */
  public CodeNoteJpa() {

  }

  /**
   * Instantiates a new code note jpa.
   *
   * @param note the note
   */
  public CodeNoteJpa(CodeNoteJpa note) {
    super(note);
    this.code = note.getCode();
  }

  /**
   * Gets the code.
   *
   * @return the code
   */
  @XmlTransient
  public Code getCode() {
    return this.code;
  }

  /**
   * Sets the code.
   *
   * @param code the new code
   */
  public void setCode(Code code) {
    this.code = code;
  }

  /**
   * Returns the code id.
   *
   * @return the code id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getCodeId() {
    return (code != null) ? code.getId() : 0;
  }
  
  /**
   * Returns the code name.
   *
   * @return the code name
   */
  @XmlElement
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getCodeName() {
    return (code != null) ? code.getName() : "";
  }
  
  /**
   * Returns the code name.
   *
   * @return the code name
   */
  @XmlElement
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getCodeTerminologyId() {
    return (code != null) ? code.getTerminologyId() : "";
  }

  /**
   * Sets the code id.
   *
   * @param codeId the code id
   */
  @SuppressWarnings("unused")
  private void setCodeId(Long codeId) {
    if (code == null) {
      code = new CodeJpa();
    }
    code.setId(codeId);
  }
  

  /* see superclass */
  @Override
  public String toString() {
    return "CodeNoteJpa [codeId=" + getCodeId() + "] " + super.toString();
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((getCodeId() == null) ? 0 : getCodeId().hashCode());
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
    CodeNoteJpa other = (CodeNoteJpa) obj;
    if (getCodeId() == null) {
      if (other.getCodeId() != null)
        return false;
    } else if (!getCodeId().equals(other.getCodeId()))
      return false;
    return true;
  }
}
