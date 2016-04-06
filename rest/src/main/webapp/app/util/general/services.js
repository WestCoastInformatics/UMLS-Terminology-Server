// Error service
tsApp
  .service(
    'utilService',
    [
      '$location',
      '$anchorScroll',
      '$cookies',
      function($location, $anchorScroll, $cookies) {
        console.debug('configure utilService');
        // declare the error
        this.error = {
          message : null,
          longMessage : null,
          expand : false
        };

        this.success = {
          message : null,
          longMessage : null,
          expand : false
        };

        // tinymce options
        this.tinymceOptions = {
          menubar : false,
          statusbar : false,
          plugins : 'autolink autoresize link image charmap searchreplace lists paste',
          toolbar : 'undo redo | styleselect lists | bold italic underline strikethrough | charmap link image',
          forced_root_block : ''
        };

        // Prep query
        this.prepQuery = function(query) {
          if (!query) {
            return '';
          }

          // Add a * to the filter if set and doesn't contain a :
          if (query.indexOf("(") == -1 && query.indexOf(":") == -1 && query.indexOf("\"") == -1) {
            var query2 = query.concat('*');
            return encodeURIComponent(query2);
          }
          return encodeURIComponent(query);
        };

        // Prep pfs filter
        this.prepPfs = function(pfs) {
          if (!pfs) {
            return {};
          }

          // Add a * to the filter if set and doesn't contain a :
          if (pfs.queryRestriction && pfs.queryRestriction.indexOf(":") == -1
            && pfs.queryRestriction.indexOf("\"") == -1) {
            var pfs2 = angular.copy(pfs);
            pfs2.queryRestriction += "*";
            return pfs2;
          }
          return pfs;
        };

        // Sets the error
        this.setError = function(message) {
          this.error.message = message;
        };

        // Clears the error
        this.clearError = function() {
          this.error.message = null;
          this.error.longMessage = null;
          this.error.expand = false;
        };

        // Sets the error
        this.setError = function(message) {
          this.error.message = message;
        };

        // Clears the error
        this.clearError = function() {
          this.error.message = null;
          this.error.longMessage = null;
          this.error.expand = false;
        };

        this.handleSuccess = function(message) {
          console.debug('Handle success: ', message);
          if (message && message.legth > 100) {
            this.success.message = 'Successful process reported, click the icon to view full message';
            this.success.longMessage = message;
          } else {
            this.success.message = message;
          }

          // scroll to top of page
          $location.hash('top');
          $anchorScroll();
        };

        // Handle error message
        this.handleError = function(response) {
          console.debug('Handle error: ', response);
          if (response.data && response.data.length > 100) {
            this.error.message = "Unexpected error, click the icon to view attached full error";
            this.error.longMessage = response.data;
          } else {
            this.error.message = response.data;
          }
          // handle no message
          if (!this.error.message) {
            this.error.message = "Unexpected server side error.";
          }
          // If authtoken expired, relogin
          if (this.error.message && this.error.message.indexOf('AuthToken') != -1) {
            // Reroute back to login page with 'auth token has
            // expired' message
            $location.path('/login');
          } else {
            // scroll to top of page
            $location.hash('top');
            $anchorScroll();
          }
        };

        // Dialog error handler
        this.handleDialogError = function(errors, error) {
          console.debug('Handle dialog error: ', errors, error);
          // handle long error
          if (error && error.length > 100) {
            errors[0] = "Unexpected error, click the icon to view attached full error";
            errors[1] = error;
          } else {
            errors[0] = error;
          }
          // handle no message
          if (!error) {
            errors[0] = "Unexpected server side error.";
          }
          // If authtoken expired, relogin
          if (error && error.indexOf('AuthToken') != -1) {
            // Reroute back to login page with 'auth token has
            // expired' message
            $location.path('/login');
          }
          // otherwise clear the top-level error
          else {
            this.clearError();
          }
        };

        // Convert date to a string
        this.toDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          var hour = '' + date.getHours();
          if (hour.length == 1) {
            hour = '0' + hour;
          }
          var minute = '' + date.getMinutes();
          if (minute.length == 1) {
            minute = '0' + minute;
          }
          var second = '' + date.getSeconds();
          if (second.length == 1) {
            second = '0' + second;
          }
          return year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;
        };

        // Convert date to a short string
        this.toShortDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          return year + '-' + month + '-' + day;
        };

        // Convert date to a simple string
        this.toSimpleDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          return year + month + day;
        };

        // Table sorting mechanism
        this.setSortField = function(table, field, paging) {
          paging[table].sortField = field;
          // reset page number too
          paging[table].page = 1;
          // handles null case also
          if (!paging[table].ascending) {
            paging[table].ascending = true;
          } else {
            paging[table].ascending = false;
          }
          // reset the paging for the correct table
          for ( var key in paging) {
            if (paging.hasOwnProperty(key)) {
              if (key == table)
                paging[key].page = 1;
            }
          }
        };

        // Return up or down sort chars if sorted
        this.getSortIndicator = function(table, field, paging) {
          if (paging[table].ascending == null) {
            return '';
          }
          if (paging[table].sortField == field && paging[table].ascending) {
            return '▴';
          }
          if (paging[table].sortField == field && !paging[table].ascending) {
            return '▾';
          }
        };

        // Helper function to get a standard paging object
        // overwritten as needed
        this.getPaging = function() {
          return {
            page : 1,
            pageSize : 10,
            filter : null,
            sortField : null,
            sortAscending : true,
            sortOptions : []
          };
        };

        // Helper to get a paged array with show/hide flags
        // and filtered by query string
        this.getPagedArray = function(array, paging) {
          var newArray = new Array();

          // if array blank or not an array, return blank list
          if (array == null || array == undefined || !Array.isArray(array)) {
            return newArray;
          }

          newArray = array;

          // apply suppressible/obsolete
          if (!paging.showHidden) {
            newArray = newArray.filter(function(item) {
              return !item.suppressible && !item.obsolete;
            });
          }

          // apply sort if specified
          if (paging.sortField) {
            // if ascending specified, use that value, otherwise use false
            newArray.sort(this.sortBy(paging.sortField, paging.ascending));
          }

          // apply filter
          if (paging.filter) {
            newArray = this.getArrayByFilter(newArray, paging.filter);
          }

          // apply active status filter
          if (paging.typeFilter) {
            newArray = this.getArrayByActiveStatus(newArray, paging.typeFilter);
          }

          // get the page indices (if supplied)
          if (paging.pageSize != -1) {
            var fromIndex = (paging.page - 1) * paging.pageSize;
            var toIndex = Math.min(fromIndex + paging.pageSize, array.length);

            // slice the array
            var results = newArray.slice(fromIndex, toIndex);
          } else {
            results = newArray;
          }

          return {
            data : results,
            totalCount : newArray.length
          };
        };

        // function for sorting an array by (string) field and direction
        this.sortBy = function(field, reverse) {

          // key: function to return field value from object
          var key = function(x) {
            return x[field];
          };

          // convert reverse to integer (1 = ascending, -1 =
          // descending)
          reverse = !reverse ? 1 : -1;

          return function(a, b) {
            return a = key(a), b = key(b), reverse * ((a > b) - (b > a));
          };
        };

        // Get array by filter text matching terminologyId or name
        this.getArrayByFilter = function(array, filter) {
          var newArray = [];

          for ( var object in array) {

            if (this.objectContainsFilterText(array[object], filter)) {
              newArray.push(array[object]);
            }
          }
          return newArray;
        };

        // Get array by filter on conceptActive status
        this.getArrayByActiveStatus = function(array, filter) {
          var newArray = [];

          for ( var object in array) {

            if (array[object].conceptActive && filter == 'Active') {
              newArray.push(array[object]);
            } else if (!array[object].conceptActive && filter == 'Retired') {
              newArray.push(array[object]);
            } else if (array[object].conceptActive && filter == 'All') {
              newArray.push(array[object]);
            }
          }
          return newArray;
        };

        // Returns true if any field on object contains filter text
        this.objectContainsFilterText = function(object, filter) {

          if (!filter || !object)
            return false;

          for ( var prop in object) {
            var value = object[prop];
            // check property for string, note this will cover child elements
            if (value && value.toString().toLowerCase().indexOf(filter.toLowerCase()) != -1) {
              return true;
            }
          }

          return false;
        };

        // Finds the object in a list by the field
        this.findBy = function(list, obj, field) {

          // key: function to return field value from object
          var key = function(x) {
            return x[field];
          };

          for (var i = 0; i < list.length; i++) {
            if (key(list[i]) == key(obj)) {
              return list[i];
            }
          }
          return null;
        };

        // Get words of a string
        this.getWords = function(str) {
          // Same as in tinymce options
          return str.match(/[^\s,\.]+/g);
        };

        // Single and multiple-word ordered phrases
        this.getPhrases = function(str) {
          var words = str.match(/[^\s,\.]+/g);
          var phrases = [];

          for (var i = 0; i < words.length; i++) {
            for (var j = i + 1; j <= words.length; j++) {
              var phrase = words.slice(i, j).join(' ');
              // a phrase have at least 5 chars and no start/end words that are
              // purely punctuation
              if (phrase.length > 5 && words[i].match(/.*[A-Za-z0-9].*/)
                && words[j - 1].match(/.*[A-Za-z0-9].*/)) {
                phrases.push(phrase.toLowerCase());
              }
            }
          }
          return phrases;
        };

        // Utility for cleaning a query
        this.cleanQuery = function(queryStr) {
          if (queryStr == null) {
            return "";
          }
          var cleanQuery = queryStr;
          // Replace all slash characters
          cleanQuery = queryStr.replace(new RegExp('[/\\\\]', 'g'), ' ');
          // Remove brackets if not using a fielded query
          if (queryStr.indexOf(':') == -1) {
            cleanQuery = queryStr.replace(new RegExp('[^a-zA-Z0-9:\\.\\-\'\\*"]', 'g'), ' ');
          }
          console.debug(queryStr, " => ", cleanQuery);
          return cleanQuery;
        };

      } ]);

// Glass pane service
tsApp.service('gpService', function() {
  console.debug('configure gpService');
  // declare the glass pane counter
  this.glassPane = {
    counter : 0
  };

  this.isGlassPaneSet = function() {
    return this.glassPane.counter;
  };

  this.isGlassPaneNegative = function() {
    return this.glassPane.counter < 0;
  };

  // Increments glass pane counter
  this.increment = function(message) {
    this.glassPane.counter++;
  };

  // Decrements glass pane counter
  this.decrement = function() {
    this.glassPane.counter--;
  };

});

// Security service
tsApp.service('securityService', [
  '$http',
  '$location',
  '$q',
  '$cookies',
  'utilService',
  'gpService',
  function($http, $location, $q, $cookies, utilService, gpService) {
    console.debug('configure securityService');

    // Declare the user
    var user = {
      userName : null,
      password : null,
      name : null,
      authToken : null,
      applicationRole : null,
      userPreferences : null
    };

    // Search results
    var searchParams = {
      page : 1,
      query : null
    };

    // Gets the user
    this.getUser = function() {
      // Determine if page has been reloaded
      if (!$http.defaults.headers.common.Authorization) {
        // Retrieve cookie
        if ($cookies.get('user')) {
          var cookieUser = JSON.parse($cookies.get('user'));
          // If there is a user cookie, load it
          if (cookieUser) {
            this.setUser(cookieUser);
            $http.defaults.headers.common.Authorization = user.authToken;
          }
        }
      }
      // return user (blank if not found)
      return user;
    };

    // Sets the user
    this.setUser = function(data) {
      user.userName = data.userName;
      user.name = data.name;
      user.authToken = data.authToken;
      user.password = "";
      user.applicationRole = data.applicationRole;
      user.userPreferences = data.userPreferences;

      // Whenver set user is called, we should save a cookie
      $cookies.put('user', JSON.stringify(user));
    };

    this.setGuestUser = function() {
      user.userName = 'guest';
      user.name = 'Guest';
      user.authToken = 'guest';
      user.password = 'guest';
      user.applicationRole = 'VIEWER';
      user.userPreferences = {};

      // Whenever set user is called, we should save a cookie
      $cookies.put('user', JSON.stringify(user));

    };

    // Clears the user
    this.clearUser = function() {
      user.userName = null;
      user.name = null;
      user.authToken = null;
      user.password = null;
      user.applicationRole = null;
      user.userPreferences = null;

      $cookies.remove('user');

    };

    var httpClearUser = this.clearUser;

    // isLoggedIn function
    this.isLoggedIn = function() {
      return user.authToken;
    };

    // isAdmin function
    this.isAdmin = function() {
      return user.applicationRole == 'ADMINISTRATOR';
    };

    // isUser function
    this.isUser = function() {
      return user.applicationRole == 'ADMINISTRATOR' || user.applicationRole == 'USER';
    };

    this.logout = function() {
      if (user.authToken == null) {
        window.alert("You are not currently logged in");
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
        utilService.handleError(response);
        gpService.decrement();
      });
    };

    // get all users
    this.getUsers = function() {
      console.debug('getUsers');
      var deferred = $q.defer();

      // Get users
      gpService.increment();
      $http.get(securityUrl + 'user/users').then(
      // success
      function(response) {
        console.debug('  users = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

    // get user for auth token
    this.getUserForAuthToken = function() {
      console.debug('getUserforAuthToken');
      var deferred = $q.defer();

      // Get users
      gpService.increment();
      $http.get(securityUrl + 'user').then(
      // success
      function(response) {
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };
    // add user
    this.addUser = function(user) {
      console.debug('addUser');
      var deferred = $q.defer();

      // Add user
      gpService.increment();
      $http.put(securityUrl + 'user/add', user).then(
      // success
      function(response) {
        console.debug('  user = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

    // update user
    this.updateUser = function(user) {
      console.debug('updateUser');
      var deferred = $q.defer();

      // Add user
      gpService.increment();
      $http.post(securityUrl + 'user/update', user).then(
      // success
      function(response) {
        console.debug('  user = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

    // remove user
    this.removeUser = function(user) {
      console.debug('removeUser');
      var deferred = $q.defer();

      // Add user
      gpService.increment();
      $http['delete'](securityUrl + 'user/remove/' + user.id).then(
      // success
      function(response) {
        console.debug('  user = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

    // get application roles
    this.getApplicationRoles = function() {
      console.debug('getApplicationRoles');
      var deferred = $q.defer();

      // Get application roles
      gpService.increment();
      $http.get(securityUrl + 'roles').then(
      // success
      function(response) {
        console.debug('  roles = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

    // Finds users as a list
    this.findUsersAsList = function(query, pfs) {
      console.debug('findUsersAsList', query, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(securityUrl + 'user/find?query=' + utilService.prepQuery(query),
        utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  output = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });

      return deferred.promise;
    };

    // update user preferences
    this.updateUserPreferences = function(userPreferences) {
      console.debug('updateUserPreferences');
      // skip if user preferences is not set
      if (!userPreferences) {
        return;
      }

      // Whenever we update user preferences, we need to update the cookie
      $cookies.put('user', JSON.stringify(user));

      var deferred = $q.defer();

      gpService.increment();
      $http.post(securityUrl + 'user/preferences/update', userPreferences).then(
      // success
      function(response) {
        console.debug('  userPreferences = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

  } ]);

// Websocket service

tsApp.service('websocketService', [ '$location', 'utilService', 'gpService',
  function($location, utilService, gpService) {
    console.debug('configure websocketService');
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

    };

    this.connection = new WebSocket(this.getUrl());

    this.connection.onopen = function() {
      // Log so we know it is happening
      console.log('Connection open');
    };

    this.connection.onclose = function() {
      // Log so we know it is happening
      console.log('Connection closed');
    };

    // error handler
    this.connection.onerror = function(error) {
      utilService.handleError(error, null, null, null);
    };

    // handle receipt of a message
    this.connection.onmessage = function(e) {
      var message = e.data;
      console.log("MESSAGE: " + message);
      // TODO: what else to do?
    };

    // Send a message to the websocket server endpoint
    this.send = function(message) {
      this.connection.send(JSON.stringify(message));
    };

  } ]);

// Source data service
tsApp
  .service(
    'sourceDataService',
    [
      '$http',
      '$location',
      '$q',
      '$cookies',
      'utilService',
      'gpService',
      function($http, $location, $q, ngCookies, utilService, gpService) {
        console.debug('configure sourceDataService');

        // cached loader names
        var sourceDataHandlers = null;

        // Get details for all currently uploaded files
        this.findSourceDataFiles = function(query) {
          console.debug('find source data files', query);
          var deferred = $q.defer();
          gpService.increment();
          $http.get(sourceDataUrl + 'find?query=' + encodeURI(query), {}).then(
          // Success
          function(response) {
            console.debug('  data = ', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            gpService.decrement();
            utilService.handleError(response);
            deferred.reject(response);
          });
          return deferred.promise;
        };

        // Removes soure data file
        this.removeSourceDataFile = function(id) {
          console.debug('remove source data file', id);
          var deferred = $q.defer();
          gpService.increment();
          $http['delete'](sourceDataUrl + 'remove/' + id).then(
          // Success
          function(response) {
            console.debug('  data = ', response.data);
            gpService.decrement();
            deferred.resolve();
          },
          // Error
          function(response) {
            gpService.decrement();
            utilService.handleError(response);
            deferred.reject(response);
          });
          return deferred.promise;
        };

        // Save or add the source data file
        this.updateSourceDataFile = function(file) {
          console.debug('update source data file', file);
          var deferred = $q.defer();
          if (file.id) {
            gpService.increment();
            $http.post(sourceDataUrl + 'update', file).then(
            // Success
            function(response) {
              gpService.decrement();
              deferred.resolve(sourceDataFile);
            },
            // Error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response);
            });
          } else {
            gpService.increment();
            $http.put(sourceDataUrl + 'add', file).then(
            // Success
            function(response) {
              gpService.decrement();
              deferred.resolve(response);
            },
            // Error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response);
            });
          }
          return deferred.promise;
        };

        // update or add the source data
        this.updateSourceData = function(data) {
          console.debug('update source data', data);
          var deferred = $q.defer();
          if (data.id) {
            gpService.increment();
            $http.post(sourceDataUrl + 'data/update', data).then(
            // Success
            function(response) {
              gpService.decrement();
              deferred.resolve(data.data);
            },
            // Error
            function(response) {
              gpService.decrement();
              utilService.handleError(response);
              deferred.reject(response);
            });
          } else {
            gpService.increment();
            $http.put(sourceDataUrl + 'data/add', data).then(
            // Success
            function(response) {
              gpService.decrement();
              deferred.resolve(response.data);
            },
            // Error
            function(response) {
              gpService.decrement();
              utilService.handleError(response);
              deferred.reject(response);
            });
          }
          return deferred.promise;
        };

        // Remove the source data
        this.removeSourceData = function(data) {
          console.debug('remove source data', data);
          var deferred = $q.defer();
          gpService.increment();
          $http['delete'](sourceDataUrl + 'data/remove/' + data.id).then(
          // Success
          function(response) {
            gpService.decrement();
            deferred.resolve();
          },
          // Error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response);
          });
          return deferred.promise;
        };

        // find source data
        this.findSourceData = function(query, disableGlassPane) {
          console.debug('find source data', query);
          var deferred = $q.defer();
          if (!disableGlassPane) {
            gpService.increment();
          }
          $http.get(sourceDataUrl + 'data/find?query=' + encodeURI(query), {}).then(
          // Success
          function(response) {
            console.debug("  data =", response.data);
            if (!disableGlassPane) {
              gpService.decrement();
            }
            deferred.resolve(response.data);
          }, // Error
          function(response) {
            utilService.handleError(response);
            if (!disableGlassPane) {
              gpService.decrement();
            }
            deferred.reject(response);
          });
          return deferred.promise;
        };

        // get loaders
        this.getSourceDataHandlers = function() {
          console.debug('get source data handlers');
          var deferred = $q.defer();

          if (sourceDataHandlers) {
            deferred.resolve(sourceDataHandlers);
          } else {
            gpService.increment();
            $http.get(sourceDataUrl + 'data/sourceDataHandlers').then(
            // Success
            function(response) {
              console.debug("  data =", response.data);
              sourceDataHandlers = response.data.keyValuePais;
              gpService.decrement();
              deferred.resolve(response.data.keyValuePair);
            },
            // Error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response);
            });
          }
          return deferred.promise;
        };

        this.getSourceData = function(id, suppressGlassPane) {
          console.debug('loading source data from id ' + id);
          var deferred = $q.defer();
          if (!id) {
            console.error('Attempted to load from null or undefined id');
            deferred.reject('No id specified');
          } else {
            if (!suppressGlassPane) {
              gpService.increment();
            }
            $http.get(sourceDataUrl + 'data/id/' + id).then(function(response) {
              if (!suppressGlassPane) {
                gpService.decrement();
              }
              deferred.resolve(response.data);
            },
            // Error
            function(response) {
              utilService.handleError(response);
              if (!suppressGlassPane) {
                gpService.decrement();
              }
              deferred.resolve(null);
            });
          }
          return deferred.promise;
        }

        this.loadFromSourceData = function(sourceData) {

          console.debug('loading source data for ' + sourceData.name);
          var deferred = $q.defer();
          if (!sourceData) {
            console.error('Attempted to load from null or undefined source data');
            deferred.reject('No source data specified');
          } else {
            gpService.increment();

            $http.post(sourceDataUrl + 'data/load', sourceData).then(function(response) {
              gpService.decrement();
              deferred.resolve();
            },
            // Error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response);
            });
          }
          return deferred.promise;
        };
        
        this.removeFromSourceData = function(sourceData) {

          console.debug('removing from source data for ' + sourceData.name);
          var deferred = $q.defer();
          if (!sourceData) {
            console.error('Attempted to remove from null or undefined source data');
            deferred.reject('No source data specified');
          } else {
            gpService.increment();

            $http.post(sourceDataUrl + 'data/remove', sourceData).then(function(response) {
              gpService.decrement();
              deferred.resolve();
            },
            // Error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response);
            });
          }
          return deferred.promise;
        };

        this.getSourceDataLog = function(terminology, version, activity, lines) {
          console.debug('getSourceDataLog', terminology, version, activity, lines);
          var deferred = $q.defer();

          if (!terminology && !version) {
            console
              .error('Must specify all of terminology and version to retrieve log entries');
            deferred.reject(null);
          }

          else {

            $http.get(
              sourceDataUrl + 'log?terminology=' + terminology + '&version=' + version
                + (activity ? '&activity=' + activity : '')
                + (lines ? '&lines=' + lines : '')).then(function(response) {
              deferred.resolve(response.data);
            }, function(error) {
              utilService.handleError(error);
              
              deferred.reject('Error retrieving source data log entries');
            });
          }
          return deferred.promise;
        };

        // end.

      } ]);