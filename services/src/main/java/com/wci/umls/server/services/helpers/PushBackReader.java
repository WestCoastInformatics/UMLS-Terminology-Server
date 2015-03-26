package com.wci.umls.server.services.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Extends {@link BufferedReader} to provide the ability to push a single line
 * of input back onto the reader to read it again on the next
 * <code>readLine()</code> call.
 */
public class PushBackReader extends BufferedReader {

  /**
   * The pushed-back line
   */
  private String pushedBackLine;

  /**
   * Instantiates a new {@link PushBackReader} wrapped around the specified
   * reader.
   * 
   * @param r {@link Reader}
   */
  public PushBackReader(Reader r) {
    super(r);
  }

  /**
   * Instantiates a new {@link PushBackReader} wrapped around the specified
   * reader.
   * 
   * @param r {@link Reader}
   * @param bufferSize the buffer size
   */
  public PushBackReader(Reader r, int bufferSize) {
    super(r, bufferSize);
  }


  /**
   * Returns the next line from the reader.
   * 
   * @return the next line from the reader
   * @throws IOException
   */
  @Override
  public String readLine() throws IOException {
    if (pushedBackLine != null) {
      String l = pushedBackLine;
      pushedBackLine = null;
      return l;
    } else {
      return super.readLine();
    }

  }


  /**
   * Pushes a line of input back onto the reader.
   * 
   * @param line input line to be pushed back on reader
   */
  public void push(String line) {
    this.pushedBackLine = line;
  }

  /**
   * Peek at push backed line without removing it.
   * 
   * @return the string
   */
  public String peek() {
    return this.pushedBackLine;
  }

}
