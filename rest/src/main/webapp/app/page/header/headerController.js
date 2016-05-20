// Content controller
tsApp.directive('tsHeader', [ '$rootScope', '$routeParams', 'securityService', '$location', 'appConfig',
  function($rootScope, $routeParams, securityService, $location, appConfig) {
    console.debug('configure header directive');
    return {
      restrict : 'A',
      scope : {},
      templateUrl : 'app/page/header/header.html',
      link : function(scope, element, attrs) {
        
        // pass values to scope
        scope.appConfig = appConfig;

        // TODO Move this to utilService, combine with getHeaderOffset for anchorScroll points
        // scope.isShowing = utilService.isHeaderShowing or some such
        // use the window.innerWidth <= 800 to detect -xs 
        scope.isShowing = function() {
          switch ($routeParams.mode) {
          case 'simple':
            return false;
          default:
            return true;
          }
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
