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
        if (tabService.tabs.length == 0) {
          utilService.setError('No tabs configured')
        } else {
          $location.path(tabService.getFirstViewableTab().link);
        }
      }
    };

    // Initialize
    $scope.initialize = function() {

      // on return to landing, clear user
      securityService.clearUser();
      
      // on return to landing page, clear any errors
      utilService.clearError();
    };

    $scope.initialize();

  } ]);