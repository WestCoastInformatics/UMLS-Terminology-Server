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
    $scope.mode = "Import";
    $scope.testSuccess = false;

    // Formatter for SQL
    $scope.getSql = function(sql) {
      if (sql) {
        return sqlFormatter.format(sql);
      }
      return "";
    }

    // Test query
    $scope.testQuery = function() {
      $scope.testSuccess = false;
      $scope.errors = [];
      $scope.messages = [];
      workflowService.testQuery($scope.selected.project.id, $scope.query, $scope.queryType,
        $scope.selected.config.queryStyle).then(
      // success
      function(data) {
        $scope.testSuccess = true;
        $scope.messages.push("Query met validation requirements.");
      },
      // Error
      function(data) {
        utilService.handleDialogError($scope.errors, data);
      });
    }

    // submit
    $scope.submit = function() {
      if ($scope.mode == 'Import') {
        $scope.import();
      } else if ($scope.mode == 'Compute') {
        $scope.compute()
      }
    }
    // Handle import
    $scope.import = function() {
      $scope.errors = [];
      $scope.messages = [];

      if (!$scope.checklistName) {
        $scope.errors.push('Checklist name must be set');
        return;
      }
      if (!$scope.file) {
        $scope.errors.push('File must be selected');
        return;
      }
      workflowService.findChecklists($scope.selected.project.id,
        'nameSort:"' + $scope.checklistName + '"', {
          startIndex : 0,
          maxResults : 1
        }).then(
        // Success
        function(data) {

          if (data.totalCount > 0) {
            $scope.errors.push('Checklist with this name already exists');
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

      if (!$scope.checklistName) {
        $scope.errors.push('Checklist name must be set');
        return;
      }

      workflowService.findChecklists($scope.selected.project.id,
        'nameSort:"' + $scope.checklistName + '"', {
          startIndex : 0,
          maxResults : 1
        }).then(
        // Success
        function(data) {

          if (data.totalCount > 0) {
            $scope.errors.push('Checklist with this name already exists');
            return;
          }

          var pfs = {
            startIndex : $scope.skipClusterCt,
            maxResults : $scope.clusterCt ? $scope.clusterCt : 100
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