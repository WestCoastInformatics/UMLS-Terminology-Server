
// Landingcontroller
tsApp.controller('LandingCtrl', [ '$scope', '$http', '$location', 'securityService', 'gpService',
  'utilService', function($scope, $http, $location, securityService, gpService, utilService) {
    console.debug('configure LandingCtrl');

    // on return to landing page, clear any errors
    utilService.clearError();
    
  } ]);