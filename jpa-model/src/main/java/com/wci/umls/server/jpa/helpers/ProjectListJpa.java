package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProjectJpa;

/**
 * JAXB enabled implementation of {@link ProjectList}.
 */
@XmlRootElement(name = "projectList")
public class ProjectListJpa extends AbstractResultList<Project> implements
    ProjectList {

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = ProjectJpa.class, name = "project")
  public List<Project> getObjects() {
    return super.getObjects();
  }

}
