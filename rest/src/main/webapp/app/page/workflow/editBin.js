// Edit bin controller
tsApp.controller('BinModalCtrl', [
  '$scope',
  '$uibModalInstance',
  '$interval',
  'utilService',
  'workflowService',
  'processService',
  'selected',
  'lists',
  'user',
  'bin',
  'action',
  function($scope, $uibModalInstance, $interval, utilService, workflowService, processService, selected, lists, user, bin,
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
    $scope.allowSave = true;
    $scope.testSampleResults = [];
    $scope.autofixAlgorithms = [];
    $scope.selectedAutofixAlgorithm = {};
    $scope.lookupInterval = null;
    $scope.user = user;

    // Get the autofix algorithms
    processService.getAlgorithmsForType($scope.project.id,
      'autofix').then(
    // Success
    function(data) {
    	$scope.autofixAlgorithms.push({key:'', value:''});
      for (var i = 0; i < data.keyValuePairs.length; i++) {
    	  $scope.autofixAlgorithms.push(data.keyValuePairs[i]);
      }
      $scope.autofixAlgorithms.sort(utilService.sortBy('value'));
      $scope.selectedAutofixAlgorithm = $scope.autofixAlgorithms[0];
    });    
    
    if ($scope.action == 'Edit' || $scope.action == 'Clone') {
      workflowService.getWorkflowBinDefinition($scope.project.id, bin.name, $scope.config.type)
        .then(
        // Success
        function(data) {
          $scope.definition = data;
          for (var i = 0; i < $scope.autofixAlgorithms.length; i++) {
              if($scope.autofixAlgorithms[i].key == data.autofix){
                  $scope.selectedAutofixAlgorithm = $scope.autofixAlgorithms[i];
                  break;
              }
            }
          $scope.allowSave = true;
        });
    } else {
      $scope.definition.editable = true;
      $scope.allowSave = false;
    }
    
    // Formatter for SQL
    $scope.getSql = function(sql) {
      if (sql) {
        return sqlFormatter.format(sql);
      }
      return "";
    }

    // Turn allow save off when query changed
    $scope.queryChanged = function() {
      $scope.allowSave = false;
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
        // Once the test query is finished processing 
        // stop the lookup
        $interval.cancel($scope.lookupInterval);
        $scope.lookupInterval = null;
        
        $scope.queryTotalCount = data.totalCount;
        $scope.testSampleResults = [];
        for (var i=0; i<data.results.length; i++ ) {
          $scope.testSampleResults.push(data.results[i].value);
        }
        $scope.allowSave = true;
        $scope.messages.push("Query met validation requirements.");
      },
      // Error
      function(data) {
        $scope.allowSave = false;
        $scope.queryTotalCount = 0;
        $scope.testSampleResults = [];
        utilService.handleDialogError($scope.errors, data);
        workflowService.decrementGlassPane();
      });
      
      $scope.startProcessProgressLookup = function(process) { 
        // Start if not already running
        if (!$scope.lookupInterval) {
          $scope.lookupInterval = $interval(function() {
            $scope.refreshProcessProgress(process);
          }, 2000);
        }
      } 
      
      $scope.startProcessProgressLookup('test-query-' + $scope.user.name);
      
   
      // Refresh Process progress
      $scope.refreshProcessProgress = function(process) {
                         
        workflowService.getProcessProgress(process, $scope.project.id).then(
        // Success
        function(data) {
          
          // Once refset is finished processing (i.e. process progress returns false), 
          // stop the lookup and get the validation results
          if(data === false){
            $interval.cancel($scope.lookupInterval);
            $scope.lookupInterval = null;
          }
        })};
    }

    $scope.changeAutofixType = function() {
    	$scope.definition.autofix=$scope.selectedAutofixAlgorithm.key;
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
