// Content controller
tsApp.directive('tsFooter', [ '$rootScope', '$location', '$routeParams', '$sce', 'gpService', 'securityService',
  'appConfig', function($rootScope, $location, $routeParams, $sce, gpService, securityService, appConfig) {
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

        // pass values to scope
        scope.appConfig = appConfig;

        // Convert to trusted HTML
        scope.deployPresentedBy = $sce.trustAsHtml(scope.appConfig.deployPresentedBy);

        // Declare user
        scope.user = securityService.getUser();

        // Logout method
        scope.logout = function() {
          if (scope.appConfig.loginEnabled === 'true') {
            securityService.logout();
          } else {
            securityService.clearUser();
          }
          $location.path('/');
        };

        // Check gp status
        scope.isGlassPaneNegative = function() {
          return gpService.isGlassPaneNegative();
        };

        // Get gp counter
        scope.getGlassPaneCounter = function() {
          return gpService.glassPane.counter;
        };
        
        // get truncated version (no dashed content)
        scope.getTruncatedVersion = function(version) {
          if (version && version.indexOf('-') != -1) {
            return version.substring(0, version.indexOf('-'));
          }
          else return version;
        }
      }
    };
  } ]);
