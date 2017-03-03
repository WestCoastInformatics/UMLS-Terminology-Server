/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.MultipleHiLoPerTableGenerator;

import com.wci.umls.server.helpers.HasId;

/**
 * Generator to create a unique ID for the object. If the object's ID is already
 * set, keep it.
 */
public class UseExistingOrGenerateIdGenerator
    extends MultipleHiLoPerTableGenerator {

  /* see superclass */
  @Override
  public synchronized Serializable generate(SessionImplementor session,
    Object object) throws HibernateException {
    if (object == null) {
      throw new HibernateException(new NullPointerException());
    }
    // NOTE: this may through a cast class exception if things don't implement
    // HasId - this generator should ONLY be used where a class does implement HasId.
    final Long id = ((HasId) object).getId();
    // final Serializable id = session.getEntityPersister(null, object)
    // .getClassMetadata().getIdentifier(object, session);
    return id != null ? id : super.generate(session, object);
  }
}