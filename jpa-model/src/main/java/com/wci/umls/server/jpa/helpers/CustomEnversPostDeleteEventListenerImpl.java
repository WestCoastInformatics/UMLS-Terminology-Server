/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import org.hibernate.envers.configuration.spi.AuditConfiguration;
import org.hibernate.envers.event.spi.EnversPostDeleteEventListenerImpl;

/**
 * Reference implementation of {@link CustomEnversPostDeleteEventListener}.
 * Includes hibernate tags for MEME database.
 */
public class CustomEnversPostDeleteEventListenerImpl
    extends EnversPostDeleteEventListenerImpl {

  /**
   * Instantiates a {@link CustomEnversPostDeleteEventListenerImpl} from the
   * specified parameters.
   *
   * @param enversConfiguration the envers configuration
   */
  public CustomEnversPostDeleteEventListenerImpl(
      AuditConfiguration enversConfiguration) {
    super(enversConfiguration);
  }

}