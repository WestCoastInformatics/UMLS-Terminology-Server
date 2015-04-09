/*
 * 
 */
package com.wci.umls.server.jpa.meta;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.SemanticTypeGroup;

/**
 * JPA-enabled implementation of {@link SemanticTypeGroup}.
 */
@Entity
@Table(name = "semantic_type_groups", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "semanticTypeGroup")
public class SemanticTypeGroupJpa extends AbstractAbbreviation implements
    SemanticTypeGroup {

  /** The relationships. */
  @OneToMany(mappedBy = "group", targetEntity = SemanticTypeJpa.class)
  private List<SemanticType> semanticTypes = null;

  /**
   * Instantiates an empty {@link SemanticTypeGroupJpa}.
   */
  public SemanticTypeGroupJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link SemanticTypeGroupJpa} from the specified parameters.
   *
   * @param group the sty
   */
  public SemanticTypeGroupJpa(SemanticTypeGroup group) {
    super(group);
    semanticTypes = group.getSemanticTypes();
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.SemanticTypeGroup#getSemanticTypes()
   */
  @Override
  @XmlElement(type = SemanticTypeJpa.class, name = "semanticType")
  public List<SemanticType> getSemanticTypes() {
    return semanticTypes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.SemanticTypeGroup#setSemanticTypes(java.
   * util.List)
   */
  @Override
  public void setSemanticTypes(List<SemanticType> semanticTypes) {
    this.semanticTypes = semanticTypes;
  }

}
