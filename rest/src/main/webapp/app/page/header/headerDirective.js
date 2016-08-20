// Content controller
tsApp.directive('tsHeader', [ '$rootScope', '$routeParams', 'securityService', 'utilService',
  '$location', 'appConfig',
  function($rootScope, $routeParams, securityService, utilService, $location, appConfig) {
    console.debug('configure header directive');
    return {
      restrict : 'A',
      scope : {},
      templateUrl : 'app/page/header/header.html',
      link : function(scope, element, attrs) {

        // pass values to scope
        scope.appConfig = appConfig;

        // TODO Move this to utilService, combine with getHeaderOffset for
        // anchorScroll points
        // scope.isShowing = utilService.isHeaderShowing or some such
        // use the window.innerWidth <= 800 to detect -xs
        scope.isShowing = function() {
          if (!utilService.isHeaderFooterShowing()) {
            return false;
          } else {
            return true;
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
