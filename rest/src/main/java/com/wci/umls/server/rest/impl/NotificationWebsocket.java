/**
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.rest.impl;

import java.util.Collections;
import java.util.HashSet;
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
  private Set<Session> sessions = Collections
      .synchronizedSet(new HashSet<Session>());

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
  public void onOpen(Session session) {
    // Add to sessions list
    synchronized (sessions) {
      sessions.add(session);
    }
  }

  /**
   * On close.
   *
   * @param userSession the user session
   * @param reason the reason
   */
  @OnClose
  public void onClose(Session userSession, CloseReason reason) {
    closeSession(userSession);
  }

  /**
   * On error.
   *
   * @param session the session
   * @param t the t
   * @throws Throwable the throwable
   */
  @OnError
  public void onError(Session session, Throwable t) throws Throwable {
    Logger.getLogger(getClass()).error(
        "SimpleMonitor2: onError() invoked, Exception = " + t.getMessage());
  }

  /**
   * Echo text.
   *
   * @param name the name
   * @return the string
   */
  @SuppressWarnings("static-method")
  @OnMessage
  public String echoText(String name) {
    return name;
  }

  /**
   * Send.
   *
   * @param message the message
   */
  public void send(String message) {
    // Remove closed sessions
    final Set<Session> copy = new HashSet<>(sessions);
    for (final Session session : copy) {
      if (!session.isOpen()) {
        sessions.remove(session);
      }
    }

    // Send message to all listeners
    synchronized (sessions) {
      for (final Session session : new HashSet<>(sessions)) {
        try {
          // Send async message
          session.getAsyncRemote().sendText(message);
        } catch (Exception e) {
          e.printStackTrace();
          // if anything went wrong, close the session and remove it
          try {
            session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION,
                "Closing"));
          } catch (Exception e2) {
            // do nothing
          }
          sessions.remove(session);
        }
      }
    }

  }

  /**
   * Close session.
   *
   * @param s the s
   */
  private void closeSession(Session s) {
    synchronized (sessions) {
      try {
        s.close();
      } catch (Throwable e) {
        // Ignore
      }
      sessions.remove(s);
    }
  }
}