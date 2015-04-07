package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.User;
import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.UserJpa;

/**
 * JAXB enabled implementation of {@link UserList}.
 */
@XmlRootElement(name = "userList")
public class UserListJpa extends AbstractResultList<User> implements UserList {

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = UserJpa.class, name = "user")
  public List<User> getObjects() {
    return super.getObjects();
  }

}
