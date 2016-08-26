// Checklist modal controller
tsApp.controller('ImportModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'workflowService',
  'selected',
  'lists',
  'user',
  function($scope, $uibModalInstance, utilService, workflowService, selected, lists, user) {
    console.debug('Entered import export modal control');

    // Scope vars
    $scope.selected = selected;
    $scope.lists = lists;
    $scope.user = user;
    $scope.messages = [];
    $scope.errors = [];
    $scope.checklistName = null;
    $scope.query = null;
    $scope.queryTypes = lists.queryTypes
    $scope.queryType = 'SQL';
    $scope.clusterCt = 100;
    $scope.clusterCtOptions = [ 20, 50, 100, 200, 500 ];
    $scope.skipClusterCt = 0;
    $scope.sortOrder = 'clusterId';
    $scope.mode = "Import";

    // Test query
    $scope.testQuery = function(binDefinition) {
      $scope.errors = [];
      $scope.messages = [];
      workflowService.testQuery($scope.selected.project.id, $scope.query, $scope.queryType).then(
      // success
      function(data) {
        $scope.messages.push("Query met validation requirements.");
      },
      // Error
      function(data) {
        utilService.handleDialogError($scope.errors, data);
      });
    }

    // Handle import
    $scope.import = function() {
      $scope.errors = [];
      $scope.messages = [];

      workflowService.findChecklists($scope.selected.project.id,
        'nameSort:"' + $scope.checklistName + '"', {
          startIndex : 0,
          maxResults : 1
        }).then(
        // Success
        function(data) {

          if (data.totalCount > 0) {
            $scope.errors.push('A checklist with this name already exists.');
            return;
          }

          // Import checklist
          workflowService.importChecklist($scope.selected.project.id, $scope.checklistName,
            $scope.file).then(
          // Success - close dialog
          function(data) {
            $uibModalInstance.close(data);
          },
          // Failure - show error
          function(data) {
            utilService.handleDialogError($scope.errors, data);
          });

        });

    }

    // Handle compute
    $scope.compute = function() {
      $scope.errors = [];
      $scope.messages = [];

      workflowService.findChecklists($scope.selected.project.id,
        'nameSort:"' + $scope.checklistName + '"', {
          startIndex : 0,
          maxResults : 1
        }).then(
        // Success
        function(data) {

          if (data.totalCount > 0) {
            $scope.errors.push('A checklist with this name already exists.');
            return;
          }

          var pfs = {
            startIndex : $scope.skipClusterCt,
            maxResults : $scope.clusterCt ? $scope.clusterCt : 100
          }
          if ($scope.sortOrder != 'RANDOM') {
            pfs.sortField = $scope.sortOrder;
          }
          // projectId, query, queryType, name, pfs
          workflowService.computeChecklist($scope.selected.project.id, $scope.query,
            $scope.queryType, $scope.checklistName, pfs).then(
          // Success - close dialog
          function(data) {
            $uibModalInstance.close(data);
          },
          // Failure - show error
          function(data) {
            utilService.handleDialogError($scope.errors, data);
          });

        });
    }

    // Dismiss modal
    $scope.cancel = function() {
      // dismiss the dialog
      $uibModalInstance.dismiss('cancel');
    };

    // end
  } ]);