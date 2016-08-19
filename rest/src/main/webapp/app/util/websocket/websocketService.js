// Websocket service
tsApp.service('websocketService', [ '$rootScope', '$location', '$http', 'utilService', 'gpService',
  'securityService',
  function($rootScope, $location, $http, utilService, gpService, securityService) {
    console.debug('configure websocketService');

    // Scope vars
    this
    user = securityService.getUser();

    // Data model
    this.data = {
      message : null
    };

    // Determine URL without requiring injection
    // should support wss for https
    // and assumes REST services and websocket are deployed together
    this.getUrl = function() {
      var url = window.location.href;
      url = url.replace('http', 'ws');
      url = url.replace('index.html', '');
      url = url.replace('index2.html', '');
      url = url.substring(0, url.indexOf('#'));
      url = url + "/websocket";
      console.debug("Websocket URL" + url);
      return url;
    };

    // TODO Add wiki entry about registering scopes and broadcast event receipt
    // lists
    this.connection = new WebSocket(this.getUrl());

    this.connection.onopen = function() {
      // Log so we know it is happening
      console.debug('Connection open');
    };

    this.connection.onclose = function() {
      // Log so we know it is happening
      console.debug('Connection closed');
    };

    // error handler
    this.connection.onerror = function(error) {
      utilService.handleError(error, null, null, null);
    };

    // handle receipt of a message
    this.connection.onmessage = function(e) {
      console.debug("ONMESSAGE", e);
      var message = e.data;

      // Need to determine what kind of message it was.
      // First, if it's a "change event", then we can determine what changed
      // and whether to fire "concept changed" or "atom changed"

      console.debug('MESSAGE=', message);

    };

    // Send a message to the websocket server endpoint
    this.send = function(message) {
      this.connection.send(JSON.stringify(message));
    };

    //
    // Temporary broadcast functions
    // To be replaced once the WebSocket is functional
    //

    this.fireNoteChange = function(data) {
      $rootScope.$broadcast('termServer::noteChange', data);
    };

    this.fireFavoriteChange = function(data) {
      $rootScope.$broadcast('termServer::favoriteChange', data);
    };

    this.fireConceptChange = function(event) {
      // If not admin user, only send when session id matches
      if (this.applicationRole != 'ADMINISTRATOR') {
        if (event.sessionId !== $http.defaults.headers.common.Authorization) {
          // bail
          return;
        }
      }
      $rootScope.$broadcast('termServer::conceptChange', event.data);
    };

    this.fireAtomChange = function(event) {
      // If not admin user, only send when session id matches
      if (this.applicationRole != 'ADMINISTRATOR') {
        if (event.sessionId !== $http.defaults.headers.common.Authorization) {
          // bail
          return;
        }
      }
      $rootScope.$broadcast('termServer::atomChange', event.data);
    };

  } ]);