// Edit root terminology modal controller
tsApp.controller('EditRootTerminologyModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'metadataService',
  'selected',
  'lists',
  'user',
  'terminology',
  function($scope, $uibModalInstance, utilService, metadataService, selected, lists, user,
    terminology) {

    // Scope variables
    $scope.terminology = terminology;
    $scope.rootTerminology = null;
    $scope.lists = lists;
    $scope.user = user;
    $scope.errors = [];

    // Edit root terminology
    $scope.submitRootTerminology = function() {
      if (!$scope.rootTerminology) { // TODO
        window.alert('The name, ... fields cannot be blank. ');
        return;
      }

      // Edit root terminology
      metadataService.updateRootTerminology($scope.rootTerminology).then(
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

    // end

    // Initialize

    metadataService.getRootTerminology($scope.terminology.terminology).then(
    // success
    function(data) {
      $scope.rootTerminology = data;
    });

  } ]);