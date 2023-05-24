/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.event.spi.EnversPostDeleteEventListenerImpl;

/**
 * Envers "post delete" event listener to log deletes. This expects the
 * config.properties file to have this setting:
 * 
 * <pre>
 * org.hibernate.envers.store_data_at_delete = true
 * </pre>
 */
@SuppressWarnings("serial")
public class CustomEnversPostDeleteEventListenerImpl
    extends EnversPostDeleteEventListenerImpl {

  /**
   * Instantiates a {@link CustomEnversPostDeleteEventListenerImpl} from the
   * specified parameters.
   *
   * @param enversConfiguration the envers configuration
   */
  public CustomEnversPostDeleteEventListenerImpl(
      EnversService enversService) {
    super(enversService);
  }

}