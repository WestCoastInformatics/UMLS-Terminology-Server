// Error service
console.debug('configure errorService');
tsApp
  .service(
    'errorService',
    [
      '$location',
      function($location) {
        // declare the error
        this.error = {
          message : null
        };

        // Sets the error
        this.setError = function(message) {
          this.error.message = message;
        }

        // Clears the error
        this.clearError = function() {
          this.error.message = null;
        }

        // Handle error message
        this.handleError = function(response) {
          console.debug("Handle error: ", response);
          this.error.message = response.data.replace(/"/g, '');
          // If authtoken expired, relogin
          if (this.error.message.startsWith("AuthToken has expired")) {
            // Reroute back to login page with "auth token has expired" message
            $location.path("/");
          }
          if (this.error.message
            .startsWith("Attempt to access a service without an authorization token")) {
            // Reroute back to login page with "auth token has expired" message
            $location.path("/");
          }
        }

        // Convert date to a string
        this.toDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = "" + date.getFullYear();
          var month = "" + (date.getMonth() + 1);
          if (month.length == 1) {
            month = "0" + month;
          }
          var day = "" + date.getDate();
          if (day.length == 1) {
            day = "0" + day;
          }
          var hour = "" + date.getHours();
          if (hour.length == 1) {
            hour = "0" + hour;
          }
          var minute = "" + date.getMinutes();
          if (minute.length == 1) {
            minute = "0" + minute;
          }
          var second = "" + date.getSeconds();
          if (second.length == 1) {
            second = "0" + second;
          }
          return year + "-" + month + "-" + day + " " + hour + ":" + minute
            + ":" + second;
        }

        // Convert date to a short string
        this.toShortDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = "" + date.getFullYear();
          var month = "" + (date.getMonth() + 1);
          if (month.length == 1) {
            month = "0" + month;
          }
          var day = "" + date.getDate();
          if (day.length == 1) {
            day = "0" + day;
          }
          return year + "-" + month + "-" + day;
        }
      } ]);

// Glass pane service
console.debug('configure gpService');
tsApp.service('gpService', function() {
  // declare the glass pane counter
  this.glassPane = {
    counter : 0
  };

  this.isGlassPaneSet = function() {
    return this.glassPane.counter;
  }

  this.isGlassPaneNegative = function() {
    return this.glassPane.counter < 0;
  }

  // Increments glass pane counter
  this.increment = function(message) {
    this.glassPane.counter++;
  }

  // Decrements glass pane counter
  this.decrement = function() {
    this.glassPane.counter--;
  }

});

// Security service
console.debug('configure securityService');
tsApp.service('securityService', [ '$http', '$location', 'errorService',
  'gpService', function($http, $location, errorService, gpService) {

    // Declare the user
    var user = {
      userName : null,
      password : null,
      name : null,
      authToken : null,
      applicationRole : null
    };

    // Gets the user
    this.getUser = function() {
      return user;
    }

    // Sets the user
    this.setUser = function(data) {
      user.userName = data.userName;
      user.name = data.name;
      user.authToken = data.authToken;
      user.password = "";
      user.applicationRole = data.applicationRole;
    }

    // Clears the user
    this.clearUser = function() {
      user.userName = null;
      user.name = null;
      user.authToken = null;
      user.password = null;
      user.applicationRole = null;
    }

    var httpClearUser = this.clearUser;

    // isLoggedIn function
    this.isLoggedIn = function() {
      return user.authToken;
    }

    this.logout = function() {
      if (user.authToken == null) {
        alert("You are not currently logged in");
        return;
      }
      gpService.increment();

      // logout
      $http.get(securityUrl + 'logout/' + user.authToken).then(
      // success
      function(response) {

        // clear scope variables
        httpClearUser();

        // clear http authorization header
        $http.defaults.headers.common.Authorization = null;
        $location.path("/");
        gpService.decrement();

      },
      // error
      function(response) {
        errorService.handleError(response);
        gpService.decrement();
      });
    }
  } ]);

// Tab service
console.debug('configure tabService');
tsApp.service('tabService', [ '$location', 'errorService', 'gpService',
  function($location, errorService, gpService) {
    // Available tabs
    this.tabs = [ {
      link : '#/content',
      label : 'Content'
    }, {
      link : '#/metadata',
      label : 'Metadata'
    } ];

    // the selected tab
    this.selectedTab = this.tabs[0];

    // Sets the selected tab
    this.setSelectedTab = function(tab) {
      this.selectedTab = tab;
    }

    // sets the selected tab by label
    // to be called by controllers when their
    // respective tab is select3ed
    this.setSelectedTabByLabel = function(label) {
      for (var i = 0; i < this.tabs.length; i++) {
        if (this.tabs[i].label === label) {
          this.selectedTab = this.tabs[i];
          break;
        }
      }
    }

  } ]);

// Websocket service
console.debug('configure websocketService');
tsApp.service('websocketService', [ '$location', 'errorService', 'gpService',
  function($location, errorService, gpService) {

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
      console.debug("url = " + url);
      return url;

    }

    this.connection = new WebSocket(this.getUrl());

    this.connection.onopen = function() {
      // Log so we know it is happening
      console.log('Connection open');
    }

    this.connection.onclose = function() {
      // Log so we know it is happening
      console.log('Connection closed');
    }

    // error handler
    this.connection.onerror = function(error) {
      errorService.handleError(error, null, null, null);
    }

    // handle receipt of a message
    this.connection.onmessage = function(e) {
      var message = e.data;
      console.log("MESSAGE: " + message);
      // TODO: what else to do?
    }

    // Send a message to the websocket server endpoint
    this.send = function(message) {
      this.connection.send(JSON.stringify(message));
    }

  } ]);