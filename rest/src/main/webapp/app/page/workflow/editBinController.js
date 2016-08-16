// Edit bin controller
var EditBinModalCtrl = function($scope, $uibModalInstance, workflowService, utilService,
  projectService, bin, workflowConfig, bins, config, project, projects, action) {
  console.debug('Entered edit bin modal control');

  $scope.action = action;
  $scope.bin = bin;
  $scope.bins = bins;
  $scope.config = config;
  $scope.workflowBinDefinition;
  $scope.positionBin;
  $scope.positionAfterDef;
  $scope.workflowConfig = workflowConfig;
  $scope.project = project;
  $scope.projects = projects;
  $scope.errors = [];
  $scope.messages = [];

  $scope.initialize = function() {
    projectService.getQueryTypes().then(function(response) {
      $scope.queryTypes = response.strings;
    });

    if ($scope.bin) {
      workflowService.getWorkflowBinDefinition($scope.project.id, bin.name, $scope.config.type)
        .then(function(response) {
          $scope.workflowBinDefinition = response;
        });
    } else if ($scope.bin == undefined) {
      $scope.bin = {
        name : '',
        description : ''
      };
    }
  }

  $scope.positionAfterBin = function(bin) {
    workflowService.getWorkflowBinDefinition($scope.project.id, bin.name, $scope.config.type).then(
      function(response) {
        $scope.positionAfterDef = response;
      }, function(response) {
        handleError($scope.errors, response);
      });
  }

  $scope.testQuery = function(binDefinition) {
    $scope.errors = [];
    $scope.messages = [];
    workflowService.testQuery($scope.project.id, binDefinition.query, binDefinition.queryType)
      .then(function(response) {
        $scope.messages.push("Query met validation requirements.");
      }, function(response) {
        handleError($scope.errors, response);
      });
  }

  // link to error handling
  function handleError(errors, error) {
    utilService.handleDialogError(errors, error);
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
        handleError($scope.errors, data);
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
        handleError($scope.errors, data);
      });
    } else if (action == 'Add') {
      definition.workflowConfigId = $scope.workflowConfig.id;
      definition.enabled = true;
      workflowService.addWorkflowBinDefinition($scope.project.id, definition,
        $scope.positionAfterDef ? $scope.positionAfterDef.id : null).then(
      // Success - add definition
      function(data) {
        $uibModalInstance.close(definition);
      },
      // Error - add definition
      function(data) {
        handleError($scope.errors, data);
      });
    }

  };

  // Dismiss modal
  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

  $scope.initialize();

};
