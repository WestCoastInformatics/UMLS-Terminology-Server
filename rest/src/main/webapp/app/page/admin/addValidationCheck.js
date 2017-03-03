// Add validation check modal controller
tsApp.controller('AddValidationCheckModalCtrl', [ '$scope', '$uibModalInstance', 'utilService',
  'projectService', 'project', 'validationChecks',
  function($scope, $uibModalInstance, utilService, projectService, project, validationChecks) {
    console.debug('Add validation check modal', validationChecks);
    // Scope variables
    $scope.typeKeyValue = {};
    $scope.project = project;
    $scope.validationChecks = validationChecks;
    $scope.errors = [];

    // Add type key value
    $scope.addValidationCheck = function() {
      if (!$scope.typeKeyValue.type && !$scope.typeKeyValue.key) {
        window.alert('The value 1 field cannot be blank. ');
        return;
      }

      $scope.project.validationData.push($scope.typeKeyValue);
      $uibModalInstance.close();
    };

    // Dismiss the modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    //
    // INITIALIZE
    //

    // end
  } ]);