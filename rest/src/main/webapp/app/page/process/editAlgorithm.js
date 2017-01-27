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

    // Test the query to see if it returns any results
    $scope.testQuery = function(query, fieldName) {
      $scope.errors = [];
      $scope.messages = [];
      // Guess the query type
      var queryType = 'LUCENE';
      if (query.match(/select.*from +[^ ]+jpa/i)) {
        queryType = 'JQL';
      } else if (query.match(/select.*/i)) {
        queryType = 'SQL';
      }

      // Get the queryType.
      // If this is a QueryActionAlgorithm, get the objectType.  Otherwise leave empty, and it will be handled by the server
      var objectType = null;
      var queryType = null;
      for (var i = 0; i < $scope.algorithm.parameters.length; i++) {
        if ($scope.algorithm.parameters[i].fieldName == fieldName + 'Type') {
          if ($scope.algorithm.parameters[i].value == '') {
            utilService.handleDialogError($scope.errors, $scope.algorithm.parameters[i].name
              + ' needs to be set');
            return;
          }
          queryType = $scope.algorithm.parameters[i].value;
        }
        if ($scope.algorithm.parameters[i].fieldName == 'objectType') {
          if ($scope.algorithm.parameters[i].value == '') {
            utilService.handleDialogError($scope.errors, $scope.algorithm.parameters[i].name
              + ' needs to be set');
            return;
          }
          console.debug('objectType at the editAlgorithm.js level is being set to: '
            + $scope.algorithm.parameters[i].value);
          objectType = $scope.algorithm.parameters[i].value;
        }
      }

      processService
        .testQuery($scope.project.id, selected.process.id, queryType, query, objectType).then(
        // Success
        function(data) {
          console.debug("This is what is returned in data: " + data);
          $scope.messages.push('Query is properly formed and returned ' + data + ' results.')
        },
        // Error
        function(data) {
          console.debug("This is what is returned in data: " + data);
          utilService.handleDialogError($scope.errors, 'Query is improperly formed.');
        });
    };

    // end
  } ]);

tsApp.directive('stringToNumber', function() {
  return {
    require : 'ngModel',
    link : function(scope, element, attrs, ngModel) {
      ngModel.$parsers.push(function(value) {
        return value == null ? null : '' + value;
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
        return value == null ? null : '' + value;
      });
      ngModel.$formatters.push(function(value) {
        return value === 'true';
      });
    }
  };
});