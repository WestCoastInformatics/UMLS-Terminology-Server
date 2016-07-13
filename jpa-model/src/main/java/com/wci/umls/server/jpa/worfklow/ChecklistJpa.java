package com.wci.umls.server.jpa.worfklow;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Indexed;

import com.wci.umls.server.model.workflow.Checklist;

/**
 * JPA-enabled implementation of a {@link Checklist}.
 */
@Entity
@Table(name = "checklists", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "project_id"
}))
@Indexed
@XmlRootElement(name = "checklist")
public class ChecklistJpa extends AbstractChecklist {

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

}
