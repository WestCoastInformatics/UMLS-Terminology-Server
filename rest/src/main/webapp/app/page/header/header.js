// Content controller
tsApp.directive('header', [ '$rootScope', 'securityService', function($rootScope, securityService) {
  console.debug('configure header directive');
  return {
    restrict : 'A',
    scope : {},
    templateUrl : 'app/page/header/header.html',
    link : function(scope, element, attrs) {

      // Declare user
      scope.user = securityService.getUser();

      // Logout method
      scope.logout = function() {
        securityService.logout();
      };
    }
  };
} ]);
