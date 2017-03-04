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
  'action',
  'result',
  function($scope, $uibModalInstance, utilService, workflowService, selected, lists, user, bin,
    clusterType, action, result) {
    console.debug("configure ChecklistModalCtrl", bin, clusterType, action, result);

    // Scope vars
    $scope.bin = bin;
    $scope.clusterType = clusterType;
    $scope.clusterCtOptions = [ 20, 50, 100, 200, 500 ];
    $scope.action = action;
    $scope.result = result;
    $scope.selected = selected;

    // Initial checklist
    $scope.name = null;
    $scope.description = null;
    $scope.clusterCt = 100;
    $scope.skipClusterCt = 0;
    $scope.excludeOnWorklist = false;
    $scope.sortOrder = 'clusterId';

    $scope.errors = [];
    $scope.warnings = [];

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
    
    // create a new checklist from report results
    $scope.computeChecklist = function() {
      var query = 'select distinct itemId conceptId, itemId clusterId from report_result_items a, ' +
        ' report_results b where b.report_id = ' + $scope.selected.report.id  + ' and b.id = ' + result.id +
        ' and a.result_id = b.id';
      
      if (!$scope.name) {
        window.alert('The name field cannot be blank. ');
        return;
      }

      var pfs = {
        startIndex : $scope.skipClusterCt,
        maxResults : $scope.clusterCt ? $scope.clusterCt : 100
      }
      
      workflowService.computeChecklist($scope.selected.project.id, query, 
        'SQL', $scope.name, pfs).then(
      // Success
      function(data) {
        $scope.warnings[0] = "Checklist created " + $scope.name + ".";
      },
      // Error
      function(data) {
        $scope.errors[0] = data;
        utilService.clearError();
      });
    }

    $scope.close = function() {
      $uibModalInstance.close(null);
    };

    // end
  } ]);