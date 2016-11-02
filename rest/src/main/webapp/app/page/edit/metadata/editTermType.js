// Edit term type modal controller
tsApp.controller('EditTermTypeModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'metadataService',
  'selected',
  'lists',
  'user',
  'action',
  'termType',
  function($scope, $uibModalInstance, utilService, metadataService, selected,
    lists, user, action, termType) {

    // Scope variables
    $scope.action = action;

    // use term type if passed in
    $scope.termType = termType;
    $scope.selected = selected;
    $scope.lists = lists;
    $scope.user = user;
    $scope.errors = [];


    // Add/Edit the term type
    $scope.submitTermType = function(termType) {
      if (!termType || !termType.abbreviation || !termType.expandedForm) {  // TODO
        window.alert('The name, ... fields cannot be blank. ');
        return;
      }
      
  
      var fn = 'addTermType';
      if ($scope.action == 'Edit') {
        fn = 'updateTermType';
      }
      // Add/Edit term type
      metadataService[fn](termType).then(
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
    // Edit case
    if (termType) {
      metadataService.getTermType(termType.key, $scope.selected.project.terminology,
        $scope.selected.project.version).then(
          function(data) {
            $scope.termType = data;
          });
    // Add new term type case  
    } else {
      $scope.termType = {};
      $scope.termType.terminology = $scope.selected.project.terminology;
      $scope.termType.version = $scope.selected.project.version;
    }
    

    // end
  } ]);