// Edit config modal controller
tsApp.controller('ConfigModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'workflowService',
  'selected',
  'lists',
  'user',
  'action',
  function($scope, $uibModalInstance, utilService, workflowService, selected, lists, user, action) {
    console.debug("configure ConfigModalCtrl", selected, lists, user, action);

    // Scope variables
    $scope.action = action;
    $scope.config = action == 'Add' ? {} : angular.copy(selected.config);
    $scope.project = selected.project;
    $scope.errors = [];
    $scope.queryStyles = [ 'CLUSTER', 'REPORT', 'OTHER' ];

    // link to error handling
    function handleError(errors, error) {
      utilService.handleDialogError($scope.errors, error);
    }

    // Add or update the config
    $scope.submitConfig = function(config) {

      $scope.errors = new Array();
      config.projectId = $scope.project.id;

      if (action == 'Add') {
        if (!config.type) {
          $scope.errors.push('Config type must be set');
          return;
        }
        if (!config.queryStyle) {
          $scope.errors.push('Config query style must be set');
          return;
        }
        // Check that this type doesn't already exist
        for (var i = 0; i < lists.configs.length; i++) {
          if (lists.configs[i].type == $scope.config.type) {
            $scope.errors.push('Workflow configuration with type ' + $scope.config.type
              + ' already exists');
            return;
          }
        }
      }

      // Edit
      if (action == 'Edit') {
        workflowService.updateWorkflowConfig($scope.project.id, config).then(
        // Success - update definition
        function(data) {
          $uibModalInstance.close(config);
        },
        // Error - update definition
        function(data) {
          handleError($scope.errors, data);
        });

      }

      // Add
      else if (action == 'Add') {
        workflowService.addWorkflowConfig($scope.project.id, config).then(
        // Success - update definition
        function(data) {
          $uibModalInstance.close(data);
        },
        // Error - update definition
        function(data) {
          handleError($scope.errors, data);
        });
      }

    };

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // end
  } ]);
