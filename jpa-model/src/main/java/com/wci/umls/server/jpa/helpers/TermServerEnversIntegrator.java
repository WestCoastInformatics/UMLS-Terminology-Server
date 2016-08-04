/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.envers.configuration.spi.AuditConfiguration;
import org.hibernate.envers.event.spi.EnversIntegrator;
import org.hibernate.envers.event.spi.EnversListenerDuplicationStrategy;
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
  public void integrate(Configuration configuration,
    SessionFactoryImplementor sessionFactory,
    SessionFactoryServiceRegistry serviceRegistry) {

    // Avoid custom behavior is autoregister is true
    try {
      if (!"true".equals(ConfigUtility.getConfigProperties()
          .getProperty("hibernate.listeners.envers.autoRegister"))) {

        super.integrate(configuration, sessionFactory, serviceRegistry);

        final AuditConfiguration enversConfiguration =
            AuditConfiguration.getFor(configuration,
                serviceRegistry.getService(ClassLoaderService.class));
        EventListenerRegistry listenerRegistry =
            serviceRegistry.getService(EventListenerRegistry.class);

        listenerRegistry
            .addDuplicationStrategy(EnversListenerDuplicationStrategy.INSTANCE);

        System.out.println("Registering event listeners");

        if (enversConfiguration.getEntCfg().hasAuditedEntities()) {
          listenerRegistry.appendListeners(EventType.POST_INSERT,
              new EmptyEnversPostInsertEventListenerImpl(enversConfiguration));
          listenerRegistry.appendListeners(EventType.POST_DELETE,
              new CustomEnversPostDeleteEventListenerImpl(enversConfiguration));
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}