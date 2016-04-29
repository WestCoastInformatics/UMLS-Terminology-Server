// Landingcontroller
tsApp.controller('LandingCtrl', [ '$scope', '$location', 'utilService', 'securityService',
  'appConfig', function($scope, $location, utilService, securityService, appConfig) {
    console.debug('configure LandingCtrl');

    // function to launch application
    $scope.launchApp = function() {
      if (appConfig.loginEnabled) {
        $location.path('/login');
      } else if (appConfig.licenseEnabled) {
        $location.path('/license');
      } else {
        $location.path('/content');
      }
    }

    $scope.initialize = function() {

      // on return to landing page, clear any errors
      utilService.clearError();

      // Clear user info
      securityService.clearUser();

      // Declare the user
      $scope.user = securityService.getUser();

    };
    
    $scope.initialize();

  } ]);