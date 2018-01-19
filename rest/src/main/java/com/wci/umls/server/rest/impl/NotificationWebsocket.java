/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

/**
 * Websocket for asynchronous notifications. For now just messages, but could be
 * JSON representations of objects.
 * 
 * <pre>
 * Useful URL: http://www.programmingforliving.com/2013/08/websocket-with-apache-tomcat-8.html
 * Useful URL: http://www.programmingforliving.com/2013/08/websocket-tomcat-8-ServerEndpointConfig-Configurator.html
 * </pre>
 */
@ServerEndpoint(value = "/websocket", configurator = NotificationWebsocketConfigurator.class)
public class NotificationWebsocket {

  /** The sessions. */
  private Map<String, Set<Session>> sessions =
      Collections.synchronizedMap(new HashMap<String, Set<Session>>());

  /**
   * Instantiates an empty {@link NotificationWebsocket}.
   */
  public NotificationWebsocket() {
    // n/a
  }

  /**
   * Handle opening of a connection.
   *
   * @param session the session
   */
  @OnOpen
  public void onOpen(final Session session) {

    // Add to sessions list
    synchronized (sessions) {
      // The username is passed in this way
      final String key = session.getQueryString();
      // Logger.getLogger(getClass()).info("  open websocket session = " + key);
      if (!sessions.containsKey(key)) {
        sessions.put(key, new HashSet<>());
      }
      sessions.get(key).add(session);
    }
  }

  /**
   * On close.
   *
   * @param session the session
   * @param reason the reason
   */
  @OnClose
  public void onClose(final Session session, final CloseReason reason) {
    synchronized (sessions) {
      try {
        final String key = session.getQueryString();
        if (sessions.containsKey(key)) {
          Logger.getLogger(getClass()).info("  close websocket session = " + key);
          session.close(reason);
          sessions.get(key).remove(session);
        }
      } catch (final Throwable e) {
        // Ignore
      }
    }

  }

  /**
   * On error.
   *
   * @param session the session
   * @param t the t
   * @throws Throwable the throwable
   */
  @OnError
  public void onError(final Session session, final Throwable t) throws Throwable {
    Logger.getLogger(getClass()).error(
        "Unexpected websocket error with session " + session.getId() + " = " + t.getMessage());
  }

  /**
   * Echo text.
   *
   * @param text the text
   */
  @OnMessage
  public void echoText(final String text) {
    // Logger.getLogger(getClass()).debug("message: " + text);
  }

  /**
   * Send.
   *
   * @param key the websocket session key
   * @param message the message
   */
  public void send(final String key, String message) {

    if (key == null || sessions.get(key) == null) {
      return;
    }

    // Remove closed sessions
    final Set<Session> copy = new HashSet<>(sessions.get(key));
    for (final Session session : copy) {
      if (!session.isOpen()) {
        sessions.get(key).remove(session);
      }
    }

    // Send message to all listeners

    for (final Session session : new HashSet<>(sessions.get(key))) {
      try {
        // Send synch message if for "all" or for specific ciitizen
        session.getBasicRemote().sendText(message);
      } catch (final Exception e) {
        e.printStackTrace();
        // if anything went wrong, close the session and remove it
        try {
          session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, "Closing"));
        } catch (final Exception e2) {
          // do nothing
        }
        sessions.get(key).remove(session);
      }
    }

  }

}