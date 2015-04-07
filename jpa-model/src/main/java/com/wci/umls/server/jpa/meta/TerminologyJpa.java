package com.wci.umls.server.jpa.meta;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.IdentifierType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.SourceCitation;
import com.wci.umls.server.model.meta.Terminology;

/**
 * JPA-enabled implementation of {@link Terminology}.
 */
@Entity
@Table(name = "terminologies", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "terminology")
public class TerminologyJpa extends AbstractAbbreviation implements Terminology {


  /**
   * Instantiates an empty {@link TerminologyJpa}.
   */
  protected TerminologyJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link TerminologyJpa} from the specified parameters.
   *
   * @param atn the atn
   */
  protected TerminologyJpa(Terminology terminology) {
    super(terminology);

  }

  @Override
  public String getTerminologyVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setTerminologyVersion(String terminologyVersion) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isAssertsRelDirection() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setAssertsRelDirection(boolean assertsRelDirection) {
    // TODO Auto-generated method stub

  }

  @Override
  public Date getStartDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setStartDate(Date startDate) {
    // TODO Auto-generated method stub

  }

  @Override
  public Date getEndDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setEndDate(Date endDate) {
    // TODO Auto-generated method stub

  }

  @Override
  public SourceCitation getCitation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setCitation(SourceCitation citation) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isCurrent() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public RootTerminology getRootSource() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setRootSource(RootTerminology rootSource) {
    // TODO Auto-generated method stub

  }

  @Override
  public String getPreferredName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setPreferredName(String preferredName) {
    // TODO Auto-generated method stub

  }

  @Override
  public List<String> getSynonymousNames() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setSynonymousNames(List<String> synonymousNames) {
    // TODO Auto-generated method stub

  }

  @Override
  public IdentifierType getOrganizingClassType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setOrganizingClassType(IdentifierType organizingClassType) {
    // TODO Auto-generated method stub

  }
}