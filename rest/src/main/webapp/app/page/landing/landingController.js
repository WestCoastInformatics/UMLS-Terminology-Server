// Landingcontroller
tsApp.controller('LandingCtrl', [ '$scope', '$location', 'utilService', 'securityService',
  'appConfig', 'tabService',
  function($scope, $location, utilService, securityService, appConfig, tabService) {
    console.debug('configure LandingCtrl');

    // disable tabs in landing view
    tabService.setShowing(false);

    // function to launch application
    $scope.launchApp = function() {
      if (appConfig.loginEnabled === 'true') {
        $location.path('/login');
      } else if (appConfig.licenseEnabled === 'true') {
        $location.path('/license');
      } else {
        if (tabService.getTabs().length == 0) {
          handleError('No tabs configured')
        }
        $location.path(tabService.getTabs()[0].link);
      }
    };

    // Initialize
    $scope.initialize = function() {

      // on return to landing page, clear any errors
      utilService.clearError();

      // Clear user info if we are using login
      if (appConfig.loginEnabled === 'true') {
        // Clear user info
        securityService.clearUser();
      }

      // Otherwise, Set guest user, in case there is no login
      else {
        securityService.setGuestUser();
      }
    };

    $scope.initialize();

  } ]);