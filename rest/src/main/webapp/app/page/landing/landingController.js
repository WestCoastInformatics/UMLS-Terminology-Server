// Landingcontroller
tsApp.controller('LandingCtrl', [ '$scope', '$location', 'utilService', 'securityService',
  'appConfig', function($scope, $location, utilService, securityService, appConfig) {
    console.debug('configure LandingCtrl');

    // function to launch application
    $scope.launchApp = function() {
      if (appConfig.loginEnabled === 'true') {
        $location.path('/login');
      } else if (appConfig.licenseEnabled === 'true') {
        $location.path('/license');
      } else {
        $location.path('/content');
      }
    };

    // Initialize
    $scope.initialize = function() {

      // on return to landing page, clear any errors
      utilService.clearError();

      // Clear user info
      securityService.clearUser();

      // Set guest user, in case there is no login
      securityService.setGuestUser();
      $http.defaults.headers.common.Authorization = response.data.authToken;

      // Declare the user
      $scope.user = securityService.getUser();

    };

    $scope.initialize();

  } ]);