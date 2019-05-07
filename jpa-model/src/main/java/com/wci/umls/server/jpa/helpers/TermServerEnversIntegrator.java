/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.envers.boot.internal.EnversIntegrator;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.event.spi.EnversListenerDuplicationStrategy;
import org.hibernate.envers.event.spi.EnversPostDeleteEventListenerImpl;
import org.hibernate.envers.event.spi.EnversPostInsertEventListenerImpl;
import org.hibernate.envers.event.spi.EnversPostUpdateEventListenerImpl;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import com.wci.umls.server.helpers.ConfigUtility;

/**
 * Provides integration for Envers into Hibernate, which mainly means
 * registering the proper event listeners.
 *
 * For this project, we will only be logging delete events.
 */
public class TermServerEnversIntegrator extends EnversIntegrator {

  /* see superclass */
  @Override
  public void integrate(Metadata metadata,
    SessionFactoryImplementor sessionFactory,
    SessionFactoryServiceRegistry serviceRegistry) {

    // Avoid custom behavior is autoregister is true
    try {
      if (!"true".equals(ConfigUtility.getConfigProperties()
          .getProperty("hibernate.listeners.envers.autoRegister"))) {

        //super.integrate(metadata, sessionFactory, serviceRegistry);
        
        final EventListenerRegistry listenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);
        final EnversService enversConfiguration = serviceRegistry.getService(EnversService.class);
        listenerRegistry.addDuplicationStrategy(EnversListenerDuplicationStrategy.INSTANCE);        

        if (enversConfiguration.getEntitiesConfigurations().hasAuditedEntities()) {
          //listenerRegistry.appendListeners(EventType.POST_INSERT, new EmptyEnversPostInsertEventListenerImpl(enversConfiguration));
          //listenerRegistry.appendListeners(EventType.POST_DELETE, new CustomEnversPostDeleteEventListenerImpl(enversConfiguration));
        	//////listenerRegistry.appendListeners( EventType.POST_UPDATE, new EnversPostUpdateEventListenerImpl( enversConfiguration ) );
          //////listenerRegistry.appendListeners( EventType.POST_INSERT, new EnversPostInsertEventListenerImpl( enversConfiguration ) );
          //listenerRegistry.appendListeners( EventType.PRE_UPDATE, new EnversPreUpdateEventListenerImpl( enversConfiguration ) );
          //////listenerRegistry.appendListeners( EventType.POST_DELETE, new EnversPostDeleteEventListenerImpl( enversConfiguration ) );
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}