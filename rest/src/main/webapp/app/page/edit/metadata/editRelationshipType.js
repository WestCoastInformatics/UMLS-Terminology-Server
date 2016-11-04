// Edit term type / attribute name modal controller
tsApp.controller('EditRelationshipTypeModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'metadataService',
  'selected',
  'lists',
  'user',
  'action',
  'object',
  'mode',
  function($scope, $uibModalInstance, utilService, metadataService, selected,
    lists, user, action, object, mode) {

    // Scope variables
    $scope.action = action;
    $scope.mode = mode;

    // use term type if passed in
    $scope.object = object;
    $scope.selected = selected;
    $scope.lists = lists;
    $scope.user = user;
    $scope.errors = [];


    // Add/Edit the term type / attribute name
    $scope.submit = function(object) {
      if (!object || !object.abbreviation || !object.expandedForm) { 
        window.alert('The abbreviation and expanded form cannot be blank. ');
        return;
      }
      
  
      var fn = 'addRelationshipType';
      if ($scope.action == 'Edit' && $scope.mode == 'relationshipType') {
        fn = 'updateRelationshipType';
      } else if ($scope.action == 'Add' && $scope.mode == 'relationshipType') {
        fn = 'addRelationshipType';
      } else if ($scope.action == 'Edit' && $scope.mode == 'addRelType') {
        fn = 'updateAdditionalRelationshipType';
      } else if ($scope.action == 'Add' && $scope.mode == 'addRelType') {
        fn = 'addAdditionalRelationshipType';
      }
      
      // Add/Edit term type
      metadataService[fn](object).then(
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
    if ($scope.mode == 'relationshipType' && object) {
      metadataService.getRelationshipType(object.key, $scope.selected.project.terminology,
        $scope.selected.project.version).then(
          function(data) {
            $scope.object = data;
            metadataService.getRelationshipType($scope.object.inverseAbbreviation, $scope.selected.project.terminology,
              $scope.selected.project.version).then(
                function(data) {
                  $scope.inverse = data;
                });
          });
      
    } else if ($scope.mode == 'addRelType' && object) {
      metadataService.getAdditionalRelationshipType(object.key, $scope.selected.project.terminology,
        $scope.selected.project.version).then(
          function(data) {
            $scope.object = data;
            metadataService.getAdditionalRelationshipType($scope.object.inverseAbbreviation, $scope.selected.project.terminology,
              $scope.selected.project.version).then(
                function(data) {
                  $scope.inverse = data;
                });
          });   
    // Add new term type / attribute name
    } else {
      $scope.object = {};
      $scope.object.terminology = $scope.selected.project.terminology;
      $scope.object.version = $scope.selected.project.version;
    }
    

    // end
  } ]);