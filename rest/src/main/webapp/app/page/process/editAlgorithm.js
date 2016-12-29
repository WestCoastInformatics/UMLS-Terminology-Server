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
    } else if ($scope.action == 'Add') {
      processService.newAlgorithmConfig($scope.project.id, selected.process.id,
        selected.algorithmConfigType.key).then(function(data) {
        $scope.algorithm = data;
        $scope.algorithm.algorithmKey = selected.algorithmConfigType.key;
        $scope.algorithm.name = selected.algorithmConfigType.value;
        $scope.description = selected.algorithmConfigType.value + ' <description>';
      });
    }

    // Update algorithm
    $scope.submitAlgorithm = function(algorithm) {
      if (algorithm && algorithm.value == 'null') {
        algorithm.value = null;
      }
      if (action == 'Edit') {
        processService.updateAlgorithmConfig($scope.project.id, selected.process.id, algorithm)
          .then(
          // Success
          function(data) {
            $uibModalInstance.close(algorithm);
          },
          // Error
          function(data) {
            utilService.handleDialogError($scope.errors, data);
          });

      } else if (action == 'Add') {
        processService.addAlgorithmConfig($scope.project.id, selected.process.id, algorithm).then(
        // Success
        function(data) {
          $uibModalInstance.close(algorithm);
        },
        // Error
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
      }

    };

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // Dismiss modal
    $scope.validate = function(algorithm) {
      $scope.errors = [];
      $scope.messages = [];
      // fix algorithm value - sometimes null
      if (algorithm.value == 'null') {
        algorithm.value = null;
      }
      processService.validateAlgorithmConfig($scope.project.id, selected.process.id, algorithm)
        .then(
        // Success
        function(data) {
          $scope.validated = true;
          $scope.messages.push('Algorithm configuration successfully validated.')
        },
        // Error
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
    };

    $scope.testQuery = function(query) {
      // Guess the query type
      var queryType = 'LUCENE';
      if (query.matches(/select.*from +[^ ]+jpa,/i)) {
        queryType = 'JQL';
      } else      if (query.matches(/select.*/i)) {
        queryType = 'SQL';
      }
      
    }

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