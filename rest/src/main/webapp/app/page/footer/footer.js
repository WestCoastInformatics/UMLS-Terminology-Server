// Content controller
tsApp.directive('footer', [ '$rootScope', 'gpService', 'securityService',
  function($rootScope, gpService, securityService) {
    console.debug('configure footer directive');
    return {
      restrict : 'A',
      scope : {},
      templateUrl : 'app/page/footer/footer.html',
      link : function(scope, element, attrs) {
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
        }

        // Get gp counter
        scope.getGlassPaneCounter = function() {
          return gpService.glassPane.counter;
        }
      }
    };
  } ]);
