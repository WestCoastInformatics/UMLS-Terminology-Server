// Websocket service
tsApp.service('websocketService',
  [
    '$rootScope',
    '$location',
    '$http',
    'utilService',
    function($rootScope, $location, $http, utilService) {
      console.debug('configure websocketService');

      // Data model
      this.data = {
        message : null
      };

      // Track events to ignore
      var ignoreConcepts = {};

      // Increment an ignore counter for this concept
      this.incrementConceptIgnore = function(conceptId) {
        if (!ignoreConcepts[conceptId]) {
          ignoreConcepts[conceptId] = 1;
        } else {
          ignoreConcepts[conceptId]++;
        }
      }

      // Internal function
      function decrementConceptIgnore(conceptId) {
        ignoreConcepts[conceptId]--;
        if (!ignoreConcepts[conceptId]) {
          delete ignoreConcepts[conceptId];
        }
      }

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

      // TODO Add wiki entry about registering scopes and broadcast event
      // receipt
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

      // Must be a local function to be accessed via the onmessage event
      function fireConceptChange(data) {
        $rootScope.$broadcast('termServer::conceptChange', data);
      }

      // Must be a local function to be accessed via the onmessage event
      function fireAtomChange(event) {
        $rootScope.$broadcast('termServer::atomChange', data);
      }

      // Must be a local function to be accessed via the onmessage event
      function fireBinsChange(data) {
        $rootScope.$broadcast('termServer::binsChange', data);
      }

      // Must be a local function to be accessed via the onmessage event
      function fireChecklistChange(data) {
        $rootScope.$broadcast('termServer::checklistChange', data);
      }

      // Must be a local function to be accessed via the onmessage event
      function fireWorklistChange(data) {
        $rootScope.$broadcast('termServer::worklistChange', data);
      }

      // handle receipt of a message
      this.connection.onmessage = function(event) {
        // Need to determine what kind of message it was.
        // First, if it's a "change event", then we can determine what changed
        // and whether to fire "concept changed" or "atom changed"
        var object = JSON.parse(event.data);
        console.debug('MESSAGE', object);

        // Handle changes involving concepts
        if (object.container && object.container.type == 'CONCEPT') {
          // Only report if from this session
          // TODO: make this configurable (by role?)
          if (object.sessionId === $http.defaults.headers.common.Authorization) {

            // Handle ignore counter
            if (ignoreConcepts[object.container.id]) {
              decrementConceptIgnore(object.container.id);
            }
            // fire event
            else {
              fireConceptChange(object.container);
            }
          }
        }

        // Handle workflow changes if the session id does not match
        // if it matches we already know about the change.
        if (object.type == 'BINS'
          && object.sessionId !== $http.defaults.headers.common.Authorization) {
          fireBinsChange(object.container);
        }

        // checklists
        else if (object.type == 'CHECKLIST'
          && object.sessionId !== $http.defaults.headers.common.Authorization) {
          object.container.objectId = object.objectId;
          fireChecklistChange(object.container);
        }

        // worklists
        else if (object.type == 'WORKLIST'
          && object.sessionId !== $http.defaults.headers.common.Authorization) {
          object.container.objectId = object.objectId;
          fireWorklistChange(object.container);
        }

      };

    } ]);