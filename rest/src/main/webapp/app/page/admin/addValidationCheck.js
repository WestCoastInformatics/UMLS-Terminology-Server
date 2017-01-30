// Edit terminology modal controller
tsApp.controller('AddValidationCheckModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'projectService',
  'selected',
  'lists',
  'user',
  'terminology',
  function($scope, $uibModalInstance, utilService, projectService, selected, lists, user,
    terminology) {

    // Scope variables
    $scope.typeKeyValue = {};
    $scope.selected = selected;
    $scope.lists = lists;
    $scope.user = user;
    $scope.errors = [];

    // Add type key value
    $scope.addValidationCheck = function() {
      if (!$scope.typeKeyValue.type && !$scope.typeKeyValue.key) {
        window.alert('The name and value 1 fields cannot be blank. ');
        return;
      }

      // Add type key value
      projectService.addTypeKeyValue($scope.typeKeyValue).then(
      // Success
      function(data) {
        // Close modal and send back the project
        $uibModalInstance.close(data);
      },
      // Error
      function(data) {
        $scope.errors[0] = data;
        utilService.clearError();
      });
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