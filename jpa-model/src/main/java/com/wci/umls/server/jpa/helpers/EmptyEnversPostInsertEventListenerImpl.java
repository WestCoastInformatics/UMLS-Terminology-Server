/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

//import org.hibernate.envers.configuration.spi.AuditConfiguration;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.event.spi.EnversPostInsertEventListenerImpl;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;

/**
 * Empty implementation of "post insert" listener because the custom integrator
 * requries at least one post insert listener.
 */
@SuppressWarnings("serial")
public class EmptyEnversPostInsertEventListenerImpl
    extends EnversPostInsertEventListenerImpl {

  /**
   * Instantiates a {@link EmptyEnversPostInsertEventListenerImpl} from the
   * specified parameters.
   *
   * @param enversConfiguration the envers configuration
   */
  public EmptyEnversPostInsertEventListenerImpl(
  		EnversService enversConfiguration) {
    super(enversConfiguration);
  }

  /* see superclass */
  @Override
  public void onPostInsert(PostInsertEvent event) {
    // Do Nothing
  }

  /* see superclass */
  @Override
  public boolean requiresPostCommitHanding(EntityPersister persister) {
    // Do nothing
    return false;
  }

}