// Edit bin controller
tsApp.controller('AlgorithmModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'processService',
  'selected',
  'lists',
  'user',
  'algorithm',
  'action',
  function($scope, $uibModalInstance, utilService, processService, selected, lists, user,
    algorithm, action) {
    console.debug("configure AlgorithmModalCtrl", algorithm, action);

    // Scope vars
    $scope.action = action;
    $scope.algorithm = algorithm;
    $scope.project = selected.project;
    $scope.steps = selected.process.steps;
    $scope.errors = [];
    $scope.messages = [];

    if ($scope.action == 'Edit') {
      processService.getAlgorithmConfig($scope.project.id, $scope.algorithm.id).then(
      // Success
      function(data) {
        $scope.algorithm = data;
      });
    }
    else if ($scope.action == 'Add') {
        processService.newAlgorithmConfig($scope.project.id, selected.algorithmConfigType.key )
          .then(
          function(data) {
            $scope.algorithm = data;
            $scope.algorithm.algorithmKey = selected.algorithmConfigType.key;
            $scope.algorithm.name = selected.algorithmConfigType.value + ' algorithm';
            $scope.algorithm.description = selected.algorithmConfigType.value + ' description';
            $scope.algorithm.terminology = selected.project.terminology;
            $scope.algorithm.version = selected.project.version;
          });
      }

    // Update algorithm
    $scope.submitAlgorithm = function(algorithm) {

      if (action == 'Edit') {
        processService.updateAlgorithmConfig($scope.project.id, algorithm).then(
        // Success - update definition
        function(data) {
          $uibModalInstance.close(algorithm);
        },
        // Error - update definition
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });

      } else if (action == 'Add') {
        processService.addAlgorithmConfig($scope.project.id,  selected.process.id, algorithm).then(
        // Success - add definition
        function(data) {
          $uibModalInstance.close(algorithm);
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


tsApp.directive('stringToNumber', function() {
  return {
    require : 'ngModel',
    link : function(scope, element, attrs, ngModel) {
      ngModel.$parsers.push(function(value) {
        return '' + value;
      });
      ngModel.$formatters.push(function(value) {
        return parseFloat(value);
      });
    }
  };
});
tsApp.directive('stringToBoolean', function() {
  return {
    require : 'ngModel',
    link : function(scope, element, attrs, ngModel) {
      ngModel.$parsers.push(function(value) {
        return '' + value;
      });
      ngModel.$formatters.push(function(value) {
        return value === 'true';
      });
    }
  };
});