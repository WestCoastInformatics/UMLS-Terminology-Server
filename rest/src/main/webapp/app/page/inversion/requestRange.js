// Checklist modal controller
tsApp.controller('SourceIdRangeModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'workflowService',
  'inversionService',
  'selected',
  'lists',
  'user',
  'sab',
  'version',
  'action',
  function($scope, $uibModalInstance, utilService, workflowService, inversionService, selected, lists, user, sab, version, action) {
    console.debug("configure SourceIdRangeModalCtrl", sab, version, action);

    // Scope vars
    $scope.action = action;
    $scope.sab = sab;
    $scope.version = version;
    $scope.selected = selected;



    $scope.errors = [];
    $scope.warnings = [];

    // Submit checklist
    $scope.submitSourceIdRangeRequest = function() {
      inversionService.getSourceIdRange(selected.project.id, $scope.sab, $scope.version).then(
      // Success
      function(data) {
        $uibModalInstance.close(data);
      },
      // Error
      function(data) {
        $scope.errors[0] = data;
        utilService.clearError();
      });      
    }



    // create a new checklist from report results
    $scope.computeChecklist = function() {
      $scope.errors = new Array();
      var query = 'select distinct itemId conceptId, itemId clusterId from report_result_items a, '
        + ' report_results b where b.report_id = ' + $scope.selected.report.id + ' and b.id = '
        + result.id + ' and a.result_id = b.id';

      if (!$scope.name) {
        $scope.errors.push('Checklist name must be set');
        return;
      }

      var pfs = {
        startIndex : $scope.skipClusterCt,
        maxResults : $scope.clusterCt ? $scope.clusterCt : 100
      }

      workflowService.computeChecklist($scope.selected.project.id, query, 'SQL', $scope.name, pfs)
        .then(
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