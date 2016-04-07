// Content controller
tsApp.directive('tsFooter', [ '$rootScope', '$routeParams', 'gpService', 'securityService',
  function($rootScope, $routeParams, gpService, securityService) {
    console.debug('configure footer directive');
    return {
      restrict : 'A',
      scope : {},
      templateUrl : 'app/page/footer/footer.html',
      link : function(scope, element, attrs) {

        scope.isShowing = function() {
          switch ($routeParams.mode) {
          case 'simple':
            return false;
          default:
            return true;
          }
        };

        // Declare user
        scope.user = securityService.getUser();

        // Logout method
        scope.logout = function() {
          console.debug('Footer logout request');
          securityService.logout();
        };

        // Check gp status
        scope.isGlassPaneNegative = function() {
          return gpService.isGlassPaneNegative();
        };

        // Get gp counter
        scope.getGlassPaneCounter = function() {
          return gpService.glassPane.counter;
        };
      }
    };
  } ]);
