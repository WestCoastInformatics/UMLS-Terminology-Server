// Websocket service
tsApp.service('websocketService',
  [
    '$rootScope',
    '$location',
    '$http',
    '$interval',
    'utilService',
    'securityService',
    function($rootScope, $location, $http, $interval, utilService, securityService) {

      // Data model
      var data = {
        message : null,
        connected : false
      };

      var connection = null;

      // Track events to ignore
      var ignoreConcepts = {};

      // Get data
      this.getData = function() {
        return data;
      }

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
      function getUrl() {
        var url = window.location.href;
        url = url.replace('http', 'ws');
        url = url.replace('index.html', '');
        url = url.replace('index2.html', '');
        url = url.substring(0, url.indexOf('#'));
        var user = securityService.getUser();
        url = url + "/websocket?" + user.userName;
        console.debug("Websocket URL " + url);
        return url;
      }
      ;

      var interval = null;
      this.cancelInterval = function() {
        $interval.cancel(interval);
      }
      // Reopen the connection
      this.reopen = function() {
        reopen();
      }
      function reopen() {

        if (interval != null) {
          console.debug('cancel interval in reopen', interval, data.connected);
          $interval.cancel(interval);
          interval = null;
        }
        connection = new WebSocket(getUrl());
        interval = $interval(function() {
          if (data.connected) {
            console.debug('ping connection', data.connected);
            connection.send('ping');
          } else {
            // N/A - just have user reload the window.
            // reopen();
          }
        }, 5000)
      }
      reopen();

      connection.onopen = function() {
        // Log so we know it is happening
        console.debug('MESSAGE Connection open');
        data.connected = true;
      };

      connection.onclose = function(event) {
        // Log so we know it is happening
        console.debug('MESSAGE Connection closed', event);
        data.connected = false;
        if (interval != null) {
          console.debug('cancel interval in reopen', interval, data.connected);
          $interval.cancel(interval);
          interval = null;
        }
      };

      // error handler
      connection.onerror = function(error) {
        console.debug('MESSAGE Connection on error', error);
        data.connected = false;
        utilService.handleError(error, null, null, null);
      };

      // Send a message to the websocket server endpoint
      this.send = function(message) {
        connection.send(JSON.stringify(message));
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
      this.fireBinsChange = function(data) {
        fireBinsChange(data);
      }
      function fireBinsChange(data) {
        $rootScope.$broadcast('termServer::binsChange', data);
      }

      this.fireChecklistChange = function(data) {
        fireChecklistChange(data);
      }
      // Must be a local function to be accessed via the onmessage event
      function fireChecklistChange(data) {
        $rootScope.$broadcast('termServer::checklistChange', data);
      }

      this.fireWorklistChange = function(data) {
        fireWorklistChange(data);
      }
      // Must be a local function to be accessed via the onmessage event
      function fireWorklistChange(data) {
        $rootScope.$broadcast('termServer::worklistChange', data);
      }

      // handle receipt of a message
      connection.onmessage = function(event) {
        // Need to determine what kind of message it was.
        // First, if it's a "change event", then we can determine what changed
        // and whether to fire "concept changed" or "atom changed"
        var object = JSON.parse(event.data);
        console.debug('MESSAGE', object);

        // Handle changes involving concepts
        if (object.container && object.container.type == 'CONCEPT'
          && object.sessionId === $http.defaults.headers.common.Authorization) {

          // Handle ignore counter
          if (ignoreConcepts[object.container.id]) {
            decrementConceptIgnore(object.container.id);
          }
          // fire event
          else {
            fireConceptChange(object.container);
          }
        }

        // Handle multiple events
        if (object.events && object.events.length > 0) {
          for (var i = 0; i < object.events.length; i++) {
            var comp = object.events[i];
            if (comp.container && comp.container.type == 'CONCEPT'
              && comp.sessionId === $http.defaults.headers.common.Authorization) {
              // Only report if from this session

              // Handle ignore counter
              if (ignoreConcepts[comp.container.id]) {
                decrementConceptIgnore(comp.container.id);
              }
              // fire event
              else {
                // Fire an event with the first one, which selects the
                // concept.
                fireConceptChange(comp.container);
              }

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