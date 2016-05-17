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
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.model.content.Code;

/**
 * The Class CodeNoteJpa.
 */
@Entity
@Table(name = "code_notes")
@Audited
@XmlRootElement(name = "notes")
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
}
