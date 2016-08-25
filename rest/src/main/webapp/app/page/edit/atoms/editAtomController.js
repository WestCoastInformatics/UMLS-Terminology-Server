// Atom modal controller
tsApp.controller('AtomModalCtrl',
  [
    '$scope',
    '$uibModalInstance',
    'utilService',
    'metaEditingService',
    'atom',
    'action',
    'selected',
    function($scope, $uibModalInstance, utilService, metaEditingService, atom, action, selected) {
      console.debug('Entered atom modal control', atom, action);

      // Scope vars
      $scope.selected = selected;
      $scope.atom = atom;
      $scope.action = action;
      $scope.overrideWarnings = false;
      $scope.warnings = [];
      $scope.errors = [];
      $scope.workflowStatuses = [ 'NEEDS_REVIEW', 'READY_FOR_PUBLICATION' ];


      // Init modal
      function initialize() {
        
      }

      // Perform add or edit/update
      $scope.submitAtom = function(atom) {
      if ($scope.action == 'Add') {
        metaEditingService.addAtom($scope.selected.project.id, $scope.selected.activityId,
          $scope.selected.concept, atom, $scope.overrideWarnings).then(
        // Success
        function(data) {
          $scope.warnings = data.warnings;
          $scope.errors = data.errors;
          if ($scope.warnings.length > 0) {
            $scope.overrideWarnings = true;
          }
          if ($scope.warnings.length == 0 && $scope.errors.length == 0) {
            $uibModalInstance.close();
          }
        },
        // Error
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
      } else {
        metaEditingService.updateAtom($scope.selected.project.id, $scope.selected.activityId,
          $scope.selected.concept, atom, $scope.overrideWarnings).then(
        // Success
        function(data) {
          $scope.warnings = data.warnings;
          $scope.errors = data.errors;
          if ($scope.warnings.length > 0) {
            $scope.overrideWarnings = true;
          }
          if ($scope.warnings.length == 0 && $scope.errors.length == 0) {
            $uibModalInstance.close();
          }
        },
        // Error
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
      }
    };

      
      
      // Dismiss modal
      $scope.cancel = function() {
        $uibModalInstance.dismiss('cancel');
      };

      // initialize modal
      initialize();

      // end
    } ]);