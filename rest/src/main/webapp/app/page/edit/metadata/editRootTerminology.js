// Edit root terminology modal controller
tsApp.controller('EditRootTerminologyModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'metadataService',
  'selected',
  'lists',
  'user',
  'rootTerminology',
  function($scope, $uibModalInstance, utilService, metadataService, selected,
    lists, user, rootTerminology) {

    // Scope variables
    $scope.rootTerminology = rootTerminology;
    $scope.selected = selected;
    $scope.lists = lists;
    $scope.user = user;
    $scope.errors = [];


    // Edit root terminology
    $scope.submitRootTerminology = function(rootTerminology) {
      if (!rootTerminology ) {  // TODO
        window.alert('The name, ... fields cannot be blank. ');
        return;
      }
            
      // Edit root terminology
      metadataService.updateRootTerminology(rootTerminology).then(
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