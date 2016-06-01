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
      } else {

        // authenticate as guest
        securityService.authenticate('guest', 'guest').then(function(response) {

          securityService.setUser(response);

          // set request header authorization and reroute

          if (appConfig.licenseEnabled === 'true') {

            $location.path('/license');
          } else {
            if (tabService.tabs.length == 0) {
              handleError('No tabs configured')
            } else {
              $location.path(tabService.tabs[0].link);
            }
          }
        })
      }
    };

    // Initialize
    $scope.initialize = function() {

      // if landing not enabled, launch the app
      if (appConfig.landingEnabled !== 'true') {
        $scope.launchApp();
      }
      // on return to landing page, clear any errors
      utilService.clearError();

      securityService.clearUser();

    };

    $scope.initialize();

  } ]);