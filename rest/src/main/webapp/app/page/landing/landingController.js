// Landingcontroller
tsApp.controller('LandingCtrl', [ '$scope', '$location', 'utilService', 'appConfig',
  function($scope, $location, utilService, appConfig) {
    console.debug('configure LandingCtrl');

    // on return to landing page, clear any errors
    utilService.clearError();

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

  } ]);