// route provider
tsApp.config(function($routeProvider) {
  $routeProvider.when('/landing', {
    templateUrl : 'app/page/landing/landing.html',
    controller : 'LandingCtrl',
    reloadOnSearch : false
  })
});


// Login controller
tsApp.controller('LandingCtrl', [ '$scope', '$http', '$location', 'securityService', 'gpService',
  'utilService', function($scope, $http, $location, securityService, gpService, utilService) {
    console.debug('configure LandingCtrl');

    // on return to landing page, clear any errors
    utilService.clearError();
    
  } ]);