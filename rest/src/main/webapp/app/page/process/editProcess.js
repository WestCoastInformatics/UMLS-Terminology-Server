// Edit bin controller
tsApp.controller('ProcessModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'processService',
  'selected',
  'lists',
  'user',
  'process',
  'action',
  function($scope, $uibModalInstance, utilService, processService, selected, lists, user, process,
    action) {
    console.debug("configure ProcessModalCtrl", process, action);

    // Scope vars
    $scope.action = action;
    $scope.process = process;
    $scope.bins = lists.bins;
    $scope.project = selected.project;
    $scope.errors = [];
    $scope.messages = [];

    if ($scope.action == 'Edit') {
      processService.getProcessConfig($scope.project.id, $scope.process.id)
        .then(
        // Success
        function(data) {
          $scope.process = data;
        });
    }



    // Update process
    $scope.submitProcess = function(process) {

      if (action == 'Edit') {
        processService.updateProcessConfig($scope.project.id, process).then(
        // Success - update definition
        function(data) {
          $uibModalInstance.close(process);
        },
        // Error - update definition
        function(data) {
          utilService.handleDialogError(errors, data);
        });

      } else if (action == 'Add') {
        processService.addProcessConfig($scope.project.id, process).then(
        // Success - add definition
        function(data) {
          $uibModalInstance.close(process);
        },
        // Error - add definition
        function(data) {
          utilService.handleDialogError(errors, data);
        });
      }

    };

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // end
  } ]);
