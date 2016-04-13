

// Login controller
tsApp.controller('LoginCtrl', [ '$scope', '$http', '$location', 'securityService', 'gpService',
  'utilService', 'projectService', 'configureService',
  function($scope, $http, $location, securityService, gpService, utilService, projectService, configureService) {
    console.debug('configure LoginCtrl');

    
    // Login function
    $scope.login = function(name, password) {
      if (!name) {
        alert("You must specify a user name");
        return;
      } else if (!password) {
        alert("You must specify a password");
        return;
      }

      // login
      gpService.increment();
      return $http({
        url : securityUrl + 'authenticate/' + name,
        method : 'POST',
        data : password,
        headers : {
          'Content-Type' : 'text/plain'
        }
      }).then(
      // success
      function(response) {
        utilService.clearError();
        securityService.setUser(response.data);

        // set request header authorization and reroute
        $http.defaults.headers.common.Authorization = response.data.authToken;
        projectService.getUserHasAnyRole();
        if (response.data.userPreferences && response.data.userPreferences.lastTab) {
          $location.path(response.data.userPreferences.lastTab);
        } else {
          // if no previous preferences, go to source for initial file upload
          if (response.data.applicationRole == 'VIEWER') {
            $location.path("/content");
          } else {
            $location.path("/source");
          }

        }
        gpService.decrement();
      },

      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
    };

    // Logout function
    $scope.logout = function() {
      securityService.logout();
    };
    
    //
    // Initialization: Check that application is configured
    //
    
    $scope.initialize = function() {
      // Clear user info
      securityService.clearUser();

      // Declare the user
      $scope.user = securityService.getUser();

    }
    configureService.isConfigured().then(function(isConfigured) {
      console.debug('login configured check: ', isConfigured);
      if (!isConfigured) {
        console.debug('routing to configure');
        $location.path('/configure');
      } else {
        //$scope.initialize();
      }
    });

  } ]);