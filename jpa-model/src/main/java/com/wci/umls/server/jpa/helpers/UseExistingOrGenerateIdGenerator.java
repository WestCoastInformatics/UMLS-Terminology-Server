/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.enhanced.TableGenerator;

/**
 * Generator to create a unique ID for the object. If the object's ID is already
 * set, keep it.
 */
public class UseExistingOrGenerateIdGenerator extends TableGenerator {

  /* see superclass */
  @Override
  public Serializable generate(SessionImplementor session, Object object)
    throws HibernateException {
    if (object == null)
      throw new HibernateException(new NullPointerException());

    Serializable id = session.getEntityPersister(null, object)
        .getClassMetadata().getIdentifier(object, session);
    return id != null ? id : super.generate(session, object);
  }
}