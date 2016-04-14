/**
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.rest.client;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.apache.log4j.Logger;

/**
 * Sample websocket client in Java. Following is an example of creating and
 * connecting this client.
 * 
 * <pre>
 * WebSocketContainer container = ContainerProvider.getWebSocketContainer();
 * String uri = &quot;ws://localhost:8080/umls-server-rest/websocket&quot;;
 * container.connectToServer(NotificationClient.class, URI.create(uri));
 * </pre>
 */
@ClientEndpoint
public class NotificationClient {

  /**
   * On open.
   *
   * @param session the session
   */
  @OnOpen
  public void onOpen(Session session) {
    // n/a
  }

  /**
   * On message.
   *
   * @param session the session
   * @param msg the msg
   */
  @OnMessage
  public void onMessage(Session session, String msg) {
    Logger.getLogger(getClass()).info(msg);
  }

  /**
   * On close.
   *
   * @param close the close
   */
  public void onClose(CloseReason close) {
    // n/a
  }

}