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
    $scope.clusterCtOptions = [ 20, 50, 100, 200, 500 ];

    // Initial checklist
    $scope.name = null;
    $scope.description = null;
    $scope.clusterCt = 100;
    $scope.skipClusterCt = 0;
    $scope.excludeOnWorklist = false;
    $scope.sortOrder = 'clusterId';

    $scope.errors = [];

    // Create the checklist
    $scope.createChecklist = function() {
      if (!$scope.name) {
        window.alert('The name field cannot be blank. ');
        return;
      }

      var pfs = {
        startIndex : $scope.skipClusterCt,
        maxResults : $scope.clusterCt ? $scope.clusterCt : 100
      }
      if ($scope.sortOrder != 'RANDOM') {
        pfs.sortField = $scope.sortOrder;
      }

      // Create checklist
      workflowService.createChecklist(selected.project.id, bin.id, $scope.clusterType, $scope.name,
        $scope.description, $scope.sortOrder == 'RANDOM', $scope.excludeOnWorklist, '', pfs).then(
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