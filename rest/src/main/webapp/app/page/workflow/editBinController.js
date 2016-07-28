// Edit bin controller
var EditBinModalCtrl = function($scope, $uibModalInstance, workflowService, utilService, 
  bin, workflowConfig, bins, binType, project, projects, action) {
  console.debug('Entered edit bin modal control');

  $scope.action = action;
  $scope.bin = bin;
  $scope.bins = bins;
  $scope.binType = binType;
  $scope.workflowBinDefinition;
  $scope.positionBin;
  $scope.positionAfterDef;
  $scope.workflowConfig = workflowConfig;
  $scope.project = project;
  $scope.projects = projects;
  $scope.errors = [];
  $scope.messages = [];
  // TODO: get these dynamically
  $scope.queryTypes = ["JQL", "SQL", "LUCENE", "PROGRAM"];
  

  if ($scope.bin) {
    workflowService.getWorkflowBinDefinition($scope.project.id, bin.name, $scope.binType).then(
    function(response) {
      $scope.workflowBinDefinition = response;
    });
  }
  if ($scope.bin == undefined) {
    $scope.bin = {
      name : '',
      description : ''
    };
  }
  
  $scope.positionAfterBin = function(bin) {
    workflowService.getWorkflowBinDefinition($scope.project.id, bin.name, $scope.binType).then(
      function(response) {
        $scope.positionAfterDef = response;
      },
      function(response) {
        handleError($scope.errors, response);
      }
    );
  }
  
  $scope.testQuery = function(binDefinition) {
    $scope.errors = [];
    $scope.messages = [];
    workflowService.testQuery($scope.project.id, binDefinition.query, binDefinition.queryType).then(
      function(response) {
        $scope.messages.push("Query met validation requirements.");
      },
      function(response) {
        handleError($scope.errors, response);
      }
    );
  }
  
  //link to error handling
  function handleError(errors, error) {
    utilService.handleDialogError(errors, error);
  }
  
  // Update bin definition
  $scope.submitDefinition = function(bin, definition) {

    // Validate bin
/*    validationService.validateBin(bin).then(
      function(data) {

        // If there are errors, make them available and stop.
        if (data.errors && data.errors.length > 0) {
          $scope.errors = data.errors;
          return;
        } else {
          $scope.errors = [];
        }

        // if $scope.warnings is empty, and data.warnings is not,
        // show warnings and stop
        if (data.warnings && data.warnings.length > 0
          && $scope.warnings.join() !== data.warnings.join()) {
          $scope.warnings = data.warnings;
          return;
        } else {
          $scope.warnings = [];
        }
*/
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
/*      },
      // Error - validate bin
      function(data) {
        handleError($scope.errors, data);
      });*/

  };


// Dismiss modal
$scope.cancel = function() {
  $uibModalInstance.dismiss('cancel');
};

};
