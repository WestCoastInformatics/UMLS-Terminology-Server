// Checklist modal controller
tsApp.controller('ChecklistModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'workflowService',
  'selected',
  'lists',
  'user',
  'bin',
  'clusterType',
  function($scope, $uibModalInstance, utilService, workflowService, selected, lists, user, bin,
    clusterType) {
    console.debug("configure ChecklistModalCtrl", bin, clusterType);

    // Scope vars
    $scope.bin = bin;
    $scope.clusterType = clusterType;
    // Initial checklist
    $scope.checklist = {
      excludeOnWorklist : false,
      randomize : false
    };
    $scope.errors = [];

    // Create the checklist
    $scope.createChecklist = function(checklist) {
      if (!checklist || !checklist.name) {
        window.alert('The name field cannot be blank. ');
        return;
      }

      // Create checklist
      workflowService.createChecklist(selected.project.id, bin.id, $scope.clusterType, checklist.name,
        checklist.description, checklist.randomize, checklist.excludeOnWorklist,
        checklist.query == undefined ? "" : checklist.query, checklist.pfs).then(
      // Success
      function(data) {
        $uibModalInstance.close(data);
      },
      // Error
      function(data) {
        $scope.errors[0] = data;
        utilService.clearError();
      });
    };

    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // end
  } ]);