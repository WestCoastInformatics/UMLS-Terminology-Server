// Content controller
tsApp.directive('tsHeader', [ '$rootScope', '$routeParams', 'securityService', '$location',
  function($rootScope, $routeParams, securityService, $location) {
    console.debug('configure header directive');
    return {
      restrict : 'A',
      scope : {},
      templateUrl : 'app/page/header/header.html',
      link : function(scope, element, attrs) {

        scope.isShowing = function() {
          switch ($routeParams.mode) {
          case 'simple':
            return false;
          default:
            return true;
          }
        };

        scope.isLanding = function() {
          return ($location.url() === '/landing');
        };

        scope.gotoTool = function() {
          if (securityService.isLoggedIn()) {
            $location.url('/content');
          } else {
            $location.url('/login');
          }
        };

        // Declare user
        scope.user = securityService.getUser();

        // Logout method
        scope.logout = function() {
          securityService.logout();
        };
      }
    };
  } ]);
