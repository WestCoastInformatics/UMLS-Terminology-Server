// Edit bin controller
tsApp.controller('BinModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'workflowService',
  'selected',
  'lists',
  'user',
  'bin',
  'action',
  function($scope, $uibModalInstance, utilService, workflowService, selected, lists, user, bin,
    action) {
    console.debug("configure BinModalCtrl", bin, action);

    // Scope vars
    $scope.action = action;
    $scope.bin = bin;
    $scope.definition = {
      name : '',
      description : ''
    };
    $scope.bins = lists.bins;
    $scope.config = selected.config;
    $scope.workflowBinDefinition = null;
    $scope.positionBin = null;
    $scope.positionAfterDef = null;
    $scope.project = selected.project;
    $scope.projects = lists.projects;
    $scope.queryTypes = lists.queryTypes
    $scope.errors = [];
    $scope.messages = [];

    if ($scope.action == 'Edit' || $scope.action == 'Clone') {
      workflowService.getWorkflowBinDefinition($scope.project.id, bin.name, $scope.config.type)
        .then(
        // Success
        function(data) {
          $scope.definition = data;
        });
    }

    // get position after bin
    $scope.positionAfterBin = function(bin) {
      workflowService.getWorkflowBinDefinition($scope.project.id, bin.name, $scope.config.type)
        .then(
        // Success
        function(data) {
          $scope.positionAfterDef = data;
        },
        // Error
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
    }

    // Test query
    $scope.testQuery = function(binDefinition) {
      $scope.errors = [];
      $scope.messages = [];
      workflowService.testQuery($scope.project.id, binDefinition.query, binDefinition.queryType,
        $scope.config.queryStyle).then(
      // success
      function(data) {
        $scope.testSucceeded = true;
        $scope.messages.push("Query met validation requirements.");
      },
      // Error
      function(data) {
        $scope.testSucceeded = false;
        utilService.handleDialogError($scope.errors, data);
      });
    }

    // Update bin definition
    $scope.submitDefinition = function(bin, definition) {

      if (action == 'Edit') {
        workflowService.updateWorkflowBinDefinition($scope.project.id, definition).then(
        // Success - update definition
        function(data) {
          $uibModalInstance.close(definition);
        },
        // Error - update definition
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
      } else if (action == 'Clone') {
        definition.id = null;
        workflowService.addWorkflowBinDefinition($scope.project.id, definition,
          $scope.positionAfterDef ? $scope.positionAfterDef.id : null).then(
        // Success - add definition
        function(data) {
          $uibModalInstance.close(definition);
        },
        // Error - add definition
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
      } else if (action == 'Add') {
        definition.workflowConfigId = $scope.config.id;
        definition.enabled = true;
        workflowService.addWorkflowBinDefinition($scope.project.id, definition,
          $scope.positionAfterDef ? $scope.positionAfterDef.id : null).then(
        // Success - add definition
        function(data) {
          $uibModalInstance.close(definition);
        },
        // Error - add definition
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
      }

    };

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // end
  } ]);
