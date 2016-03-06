// Login controller
tsApp.controller('LoginCtrl', [ '$scope', '$http', '$location', 'securityService', 'gpService',
  'utilService', function($scope, $http, $location, securityService, gpService, utilService) {
    console.debug('configure LoginCtrl');

    // Declare the user
    $scope.user = securityService.getUser();
    
    // TODO Check status and either revert to #/content or #/landing depending locationChange

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
        $location.path("/content");
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