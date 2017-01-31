// Add validation check modal controller
tsApp.controller('AddValidationCheckModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'projectService',
  'selected',
  function($scope, $uibModalInstance, utilService, projectService, selected) {

    // Scope variables
    $scope.typeKeyValue = {};
    $scope.selected = selected;
    $scope.errors = [];

    // Add type key value
    $scope.addValidationCheck = function() {
      if (!$scope.typeKeyValue.type && !$scope.typeKeyValue.key) {
        window.alert('The value 1 field cannot be blank. ');
        return;
      }

      $scope.selected.project.validationData.push($scope.typeKeyValue);
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