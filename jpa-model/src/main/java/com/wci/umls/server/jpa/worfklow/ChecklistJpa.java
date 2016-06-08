package com.wci.umls.server.jpa.worfklow;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import com.wci.umls.server.model.workflow.Checklist;

/**
 * JPA-enabled implementation of a {@link Checklist}.
 */
@Entity
@Table(name = "checklists", uniqueConstraints = @UniqueConstraint(columnNames = {
    "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "checklist")
public class ChecklistJpa extends AbstractChecklist implements Checklist {

  /** The id. */
  @TableGenerator(name = "EntityIdGenWorkflow", table = "table_generator_wf", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGenWorkflow")
  private Long id;

  
  /**
   * Instantiates an empty {@link ChecklistJpa}.
   */
  public ChecklistJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ChecklistJpa} from the specified parameters.
   *
   * @param checklist the checklist
   * @param deepCopy the deep copy
   */
  public ChecklistJpa(Checklist checklist, boolean deepCopy) {
    super(checklist, deepCopy);
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


}
