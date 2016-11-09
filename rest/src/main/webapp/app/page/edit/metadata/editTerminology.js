// Edit terminology modal controller
tsApp.controller('EditTerminologyModalCtrl', [
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

    console.debug('XXX',terminology);
    // Scope variables
    $scope.terminology = terminology;
    $scope.selected = selected;
    $scope.lists = lists;
    $scope.user = user;
    $scope.errors = [];

    // Edit root terminology
    $scope.submitTerminology = function() {
      if (!terminology) {
        window.alert('The name, ... fields cannot be blank. ');
        return;
      }

      // Edit root terminology
      metadataService.updateTerminology(terminology).then(
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