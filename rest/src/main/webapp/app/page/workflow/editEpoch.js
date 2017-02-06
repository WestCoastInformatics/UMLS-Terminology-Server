// Edit epoch modal controller
tsApp.controller('EpochModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'workflowService',
  'selected',
  function($scope, $uibModalInstance, utilService, workflowService, selected) {
    console.debug("configure EpochModalCtrl", selected);

    // Scope variables
    $scope.project = selected.project;
    $scope.errors = [];
    $scope.epoch;

    // link to error handling
    function handleError(errors, error) {
      utilService.handleDialogError($scope.errors, error);
    }

    // Add epoch and make it project current epoch
    $scope.submitEpoch = function(epoch) {

      $scope.errors = new Array();
      epoch.projectId = $scope.project.id;

        workflowService.addWorkflowEpoch($scope.project.id, epoch).then(
        function(data) {
          $scope.getAllWorkflowEpochs();
        },
        // Error - update definition
        function(data) {
          handleError($scope.errors, data);
        });
    };
    
    // Remove epoch from epochs on the project
    $scope.removeEpoch = function(epoch) {
      workflowService.removeWorkflowEpoch($scope.project.id, epoch.id).then(
        function(data) {
          $scope.getAllWorkflowEpochs();
        });
    }

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };
    
    $scope.close = function() {
      $uibModalInstance.close();
    };

    // Gets all epochs for the project
    $scope.getAllWorkflowEpochs = function() {
    workflowService.getWorkflowEpochs($scope.project.id).then(
      function(data) {
        $scope.epochs = data.epochs;
      });
    }
    
    // initialize
    $scope.getAllWorkflowEpochs();
    
    
    // end
  } ]);
