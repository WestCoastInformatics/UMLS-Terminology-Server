package com.wci.umls.server.rest.impl;

/**
 * The listener interface for receiving websocketWorfklow events.
 * The class that is interested in processing a websocketWorfklow
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's addWebsocketWorfklowListener method. When
 * the websocketWorfklow event occurs, that object's appropriate
 * method is invoked.
 *
 */
//@ServiceEndpoint
@SuppressWarnings("javadoc")
public class WebsocketWorfklowListener {
  // still working on this. - may need Jetty instead of Tomcat.
}
