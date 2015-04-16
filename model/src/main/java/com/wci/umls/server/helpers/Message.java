/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A message class for use with web sockets.
 */
@XmlRootElement
public class Message {

  /** The author. */
  public String author = "";

  /** The message. */
  public String message = "";

  /**
   * Instantiates an empty {@link Message}.
   */
  public Message() {
    // n/a
  }

  /**
   * Instantiates a {@link Message} from the specified parameters.
   *
   * @param author the author
   * @param message the message
   */
  public Message(String author, String message) {
    this.author = author;
    this.message = message;
  }

}