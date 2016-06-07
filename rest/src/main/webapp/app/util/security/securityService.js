// Security service
tsApp.service('securityService', [
  '$http',
  '$location',
  '$q',
  '$cookies',
  'utilService',
  'gpService',
  'projectService',
  'appConfig',
  function($http, $location, $q, $cookies, utilService, gpService, projectService, appConfig) {
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

    // accepts the license
    this.acceptLicense = function() {
      var deferred = $q.defer();
      var expireDate = new Date();
      expireDate.setDate(expireDate.getDate() + 30);
      $cookies.put('WCI ' + appConfig.deployTitle, 'license_accepted', {
        expires : expireDate
      });
      var cookie = $cookies.get('WCI ' + appConfig.deployTitle);
      // console.debug('Set cookie:', cookie);
      deferred.resolve();
      return deferred.promise;
    };

    // checks the license
    this.checkLicense = function() {
      var deferred = $q.defer();

      if (appConfig.licenseEnabled !== 'true') {
        deferred.resolve();
      } else {

        var cookie = $cookies.get('WCI ' + appConfig.deployTitle);
        // console.debug('License cookie', cookie);
        if (!cookie) {
          deferred.reject();
        } else {
          // refresh the cookie whenever license is checked
          this.acceptLicense();
          deferred.resolve();
        }
      }
      return deferred.promise;
    };

    // Gets the user
    this.getUser = function() {

      // if login is not enabled, set and return the Guest user
      if (appConfig.loginEnabled !== 'true') {
        this.setGuestUser();
      }
      // otherwise, determine if user is already logged in
      else if (!$http.defaults.headers.common.Authorization) {
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
      $http.defaults.headers.common.Authorization = data.authToken;
      projectService.getUserHasAnyRole();

      // Whenver set user is called, we should save a cookie
      $cookies.put('user', JSON.stringify(user));
    };

    // Set user to the guest user
    this.setGuestUser = function() {
      user.userName = 'guest';
      user.name = 'Guest';
      user.authToken = 'guest';
      user.password = 'guest';
      user.applicationRole = 'VIEWER';
      user.userPreferences = {};

      // Whenever set user is called, we should save a cookie
      $cookies.put('user', JSON.stringify(user));
      $http.defaults.headers.common.Authorization = 'guest';

    };

    // Determine if guest user
    this.isGuestUser = function() {
      return $http.defaults.headers.common.Authorization == 'guest';
    };

    // Set admin user
    this.setAdminUser = function() {
      user.userName = 'admin';
      user.name = 'Administrator';
      user.authToken = 'admin';
      user.password = 'admin';
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

    //
    // Role functions
    // Note that administrator is considered all roles
    //
    this.hasPrivilegesOf = function(role) {
      switch (role) {
      case 'ADMINISTRATOR':
        return this.isAdmin();
      case 'USER':
        return this.isUser() || this.isAdmin();
      case 'VIEWER':
        return this.isViewer() || this.isUser() || this.isAdmin();
      default:
        return true;
      }
      console.debug('fail');
      return false;
    };

    // isAdmin function
    this.isAdmin = function() {
      return user.userRole === 'ADMINISTRATOR';
    };

    // isUser function
    this.isUser = function() {
      return user.userRole === 'USER';
    };

    // isViewer function
    this.isViewer = function() {
      return user.userRole === 'VIEWER';
    };
    // Authenticate user
    this.authenticate = function(userName, password) {

      var deferred = $q.defer();

      gpService.increment();

      // login
      $http({
        url : securityUrl + 'authenticate/' + userName,
        method : 'POST',
        data : password,
        headers : {
          'Content-Type' : 'text/plain'
        }
      }).then(function(response) {
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        gpService.decrement();
        utilService.handleError(response);
        deferred.reject(response.data);
      });

      return deferred.promise;

    };

    // Logs user out
    this.logout = function() {

      var deferred = $q.defer();
      if (user.authToken == null) {
        window.alert("You are not currently logged in");
        deferred.reject('Not currently logged in');
      } else {
        gpService.increment();

        // logout
        $http.get(securityUrl + 'logout/' + user.authToken).then(
        // success
        function(response) {

          // clear scope variables
          httpClearUser();

          // clear http authorization header
          $http.defaults.headers.common.Authorization = null;
          gpService.decrement();
          deferred.resolve('Successfully logged out');

        },
        // error
        function(response) {
          utilService.handleError(response);
          gpService.decrement();
          deferred.reject('Failed to logout');
        });
        return deferred.promise;
      }
    };

    // get all users
    this.getUsers = function() {
      var deferred = $q.defer();

      // Get users
      gpService.increment();
      $http.get(securityUrl + 'user/users').then(
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

    // get user for auth token
    this.getUserForAuthToken = function() {
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

    // adds user
    this.addUser = function(user) {
      var deferred = $q.defer();

      // Add user
      gpService.increment();
      $http.put(securityUrl + 'user/add', user).then(
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

    // updates user
    this.updateUser = function(user) {
      var deferred = $q.defer();

      // Add user
      gpService.increment();
      $http.post(securityUrl + 'user/update', user).then(
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

    // removes user
    this.removeUser = function(user) {
      var deferred = $q.defer();

      // Add user
      gpService.increment();
      $http['delete'](securityUrl + 'user/remove/' + user.id).then(
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

    // gets application roles
    this.getApplicationRoles = function() {
      var deferred = $q.defer();

      // Get application roles
      gpService.increment();
      $http.get(securityUrl + 'roles').then(
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

    // Finds users as a list
    this.findUsersAsList = function(query, pfs) {
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(securityUrl + 'user/find?query=' + utilService.prepQuery(query),
        utilService.prepPfs(pfs)).then(
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

    //
    // User Favorites
    //

    // TODO Add null checks on user, user preferences to all functions

    // Create the base user favorite string, without timestamp
    function getUserFavoriteStr(type, terminology, version, terminologyId, name) {
      return type + '~~' + terminology + '~~' + version + '~~' + terminologyId + '~~' + name;
    }

    // Gets the user favorite string object without reference to name or
    // timestamp
    function getUserFavorite(type, terminology, version, terminologyId) {

      if (!user || !user.userPreferences || !user.userPreferences.favorites) {
        return null;
      }

      var delimitedStr = getUserFavoriteStr(type, terminology, version, terminologyId, name);

      var matchFound = false;
      for (var i = 0; i < user.userPreferences.favorites.length; i++) {
        if (user.userPreferences.favorites[i].indexOf(delimitedStr) != -1) {
          return user.userPreferences.favorites[i];
        }
      }
      return null;
    }

    // Determines whether object is in favorites (without reference to name or
    // timestamp)
    this.isUserFavorite = function(type, terminology, version, terminologyId) {
      var favorite = getUserFavorite(type, terminology, version, terminologyId);
      if (favorite) {
        return true;
      } else {
        return false;
      }

    };

    // Adds a user favorite
    this.addUserFavorite = function(type, terminology, version, terminologyId, name) {

      var deferred = $q.defer();
      if (this.isGuestUser()) {
        $q.reject('Cannot add favorites for guest user');
      } else {
        if (!user.userPreferences || !type || !terminology || !version || !terminologyId || !name) {
          deferred.reject('Insufficient arguments');
        }
        var delimitedStr = getUserFavoriteStr(type, terminology, version, terminologyId, name);
        if (!user.userPreferences.favorites) {
          user.userPreferences.favorites = [];
        }

        if (!this.isUserFavorite(type, terminology, version, terminologyId)) {

          // add the timestamp after verifying this component info is not
          // matched
          user.userPreferences.favorites.push(delimitedStr + '~~' + new Date().getTime());

          this.updateUserPreferences(user.userPreferences).then(function(response) {
            deferred.resolve(response);
          }, function(response) {
            deferred.reject(response);
          });
        } else {
          deferred.reject('Favorite already exists');
        }
      }

      return deferred.promise;

    };

    // Removes a user favorite
    this.removeUserFavorite = function(type, terminology, version, terminologyId, name) {

      console.debug('remove user favorite', type, terminology, version, terminologyId, name);

      var deferred = $q.defer();
      if (!user.userPreferences || !type || !terminology || !version || !terminologyId || !name) {
        deferred.reject('Insufficient arguments');
      }
      var delimitedStr = getUserFavoriteStr(type, terminology, version, terminologyId, name);

      var matchFound = false;
      for (var i = 0; i < user.userPreferences.favorites.length; i++) {
        if (user.userPreferences.favorites[i].indexOf(delimitedStr) != -1) {
          console.debug('match found: ', user.userPreferences.favorites[i]);
          matchFound = true;
          user.userPreferences.favorites.splice(i, 1);
          break;
        }
      }
      if (matchFound) {
        this.updateUserPreferences(user.userPreferences).then(function(response) {
          deferred.resolve(response);
        }, function(response) {
          deferred.reject(response);
        });
      } else {
        deferred.reject('Favorite not in list');
      }

      return deferred.promise;

    };

    // update user preferences
    this.updateUserPreferences = function(userPreferences) {

      // Whenever we update user preferences, we need to update the cookie
      $cookies.put('user', JSON.stringify(user));

      var deferred = $q.defer();

      // skip if user preferences is not set
      if (!userPreferences) {
        console.log('User preferences not set');
        deferred.reject('user preferences not set');
      }

      // Skip for guest user
      if (this.isGuestUser()) {
        console.log('Skipped updating preferences for guest user');
        deferred.reject('guest user');
      } else {

        gpService.increment();
        $http.post(securityUrl + 'user/preferences/update', userPreferences).then(
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
      }
      return deferred.promise;
    };

    // Get favorite
    this.getFavorite = function(type, terminology, version, terminologyId) {
      return this.getUser().userPreferences.favorites.filter(function(item) {
        return item.terminology === terminology && item.terminologyId === terminologyId
          && item.version === version && item.type === type;
      }).length > 0;
    };

  } ]);