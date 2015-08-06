/**
 * Copyright 2015 West Coast Informatics, LLC
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
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Websocket for asynchronous notifications. For now just messages, but could be
 * JSON representations of objects.
 */
@ServerEndpoint(value = "/websocket", configurator = NotificationWebsocketConfigurator.class)
public class NotificationWebsocket {

  /** The sessions. */
  private static Set<Session> sessions = Collections
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
  @SuppressWarnings("static-method")
  @OnOpen
  public void onOpen(Session session) {
    // Add to sessions list
    sessions.add(session);
  }

  /**
   * On close.
   *
   * @param userSession the user session
   */
  @SuppressWarnings("static-method")
  @OnClose
  public void onClose(Session userSession) {
    sessions.remove(userSession);
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
  @SuppressWarnings("static-method")
  public void send(String message) {
    // Remove closed sessions
    Set<Session> copy = new HashSet<>(sessions);
    for (Session session : copy) {
      if (!session.isOpen()) {
        sessions.remove(session);
      }
    }

    // Send message to all listeners
    synchronized (sessions) {
      for (Session session : sessions) {
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
}