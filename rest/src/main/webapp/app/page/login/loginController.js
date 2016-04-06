// Route
tsApp.config(function config($routeProvider) {
  // TODO Remove this when landing has been reenabled
  $routeProvider.when('/', {
    templateUrl : 'app/page/login/login.html',
    controller : 'LoginCtrl',
    reloadOnSearch : false
  });

  $routeProvider.when('/login', {
    templateUrl : 'app/page/login/login.html',
    controller : 'LoginCtrl',
    reloadOnSearch : false
  });
});

// Login controller
tsApp.controller('LoginCtrl', [ '$scope', '$http', '$location', 'securityService', 'gpService',
  'utilService', 'projectService',
  function($scope, $http, $location, securityService, gpService, utilService, projectService) {
    console.debug('configure LoginCtrl');

    // Clear user info
    securityService.clearUser();

    // Declare the user
    $scope.user = securityService.getUser();

    // TODO Check status and either revert to #/content or #/landing depending
    // locationChange

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

  } ]);