// Import modal controller
tsApp.controller('ImportWorkflowModalCtrl', [ '$scope', '$uibModalInstance', 'utilService',
  'workflowService', 'selected', 'lists',
  function($scope, $uibModalInstance, utilService, workflowService, selected, lists) {
    console.debug('Entered import workflow modal control', selected, lists);

    $scope.selected = selected;
    $scope.lists = lists;
    $scope.comments = [];
    $scope.warnings = [];
    $scope.errors = [];

    // Handle import
    $scope.importWorkflow = function(file) {
      $scope.errors = [];
      workflowService.importWorkflow($scope.selected.project.id, file).then(
      // Success - close dialog
      function(data) {
        $uibModalInstance.close(data);
      },
      // Failure - show error
      function(data) {
        handleError($scope.errors, data);
      });
    }

    // Dismiss modal
    $scope.cancel = function() {
      // dismiss the dialog
      $uibModalInstance.dismiss('cancel');
    };

    $scope.close = function() {
      // close the dialog and reload refsets
      $uibModalInstance.close();
    };

    // end

  } ]);
