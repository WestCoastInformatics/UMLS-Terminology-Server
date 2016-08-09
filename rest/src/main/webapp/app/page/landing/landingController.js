// Landingcontroller
tsApp.controller('LandingCtrl', [ '$scope', '$anchorScroll', '$location', 'utilService', 'securityService',
  'appConfig', 'tabService',
  function($scope, $anchorScroll, $location, utilService, securityService, appConfig, tabService) {
    console.debug('configure LandingCtrl');

    // disable tabs in landing view
    tabService.setShowing(false);

    // function to launch application
    $scope.launchApp = function() {
      if (appConfig['deploy.login.enabled'] === 'true') {
        $location.path('/login');
      } else if (appConfig['deploy.license.enabled'] === 'true') {
        $location.path('/license');
      } 
      // Assume no user preferences (always guest user), route to first tab
      else {        
        tabService.routeAuthorizedUser();
      }
    };
    
    $scope.gotoAnchor = function(anchorLink) {
      $anchorScroll.yoffset = 500;
      $location.hash(anchorLink);
      $anchorScroll();
      $anchorScroll.yoffset = 0;
    }

    // Initialize
    $scope.initialize = function() {

      // on return to landing, clear user
      securityService.clearUser();

      // on return to landing page, clear any errors
      utilService.clearError();
    };

    $scope.initialize();

  } ]);