// Semantic types controller

tsApp
  .controller(
    'ActionErrorsCtrl',
    [
      '$scope',
      '$window',
      'tabService',
      'utilService',
      'metaEditingService',
      'securityService',
      '$uibModal',
      'action',
      'warnings',
      'errors',
      '$uibModalInstance',
      function($scope, $window, tabService, utilService,metaEditingService,
        securityService, $uibModal, action, warnings, errors, $uibModalInstance) {

        console.debug("configure ActionErrorsCtrl");

        // remove tabs, header and footer
        tabService.setShowing(false);
        utilService.setHeaderFooterShowing(false);

        $scope.user = securityService.getUser();
        $scope.action = action;
        $scope.warnings = warnings;
        $scope.errors = errors;
        
 
        // Override warnings
        $scope.overrideWarnings = function() {
          $uibModalInstance.close();
        }

        // Dismiss modal
        $scope.cancel = function() {
          $uibModalInstance.dismiss('cancel');
        };

        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {


        }

        // Call initialize
        $scope.initialize();

      } ]);