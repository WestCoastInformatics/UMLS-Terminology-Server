// Merge modal controller
tsApp.controller('MergeModalCtrl',
  [
    '$scope',
    '$uibModalInstance',
    'utilService',
    'metaEditingService',
    'selected',
    'lists',
    'user',
    function($scope, $uibModalInstance, utilService, metaEditingService, selected, lists,
      user) {
      console.debug('Entered merge modal control', lists);

      // Scope vars
      $scope.selected = selected;
      $scope.lists = lists;
      $scope.prospectiveMergeConcepts = [];
      $scope.overrideWarnings = false;
      $scope.warnings = [];
      $scope.errors = [];

      // Init modal
      function initialize() {
        for (var i = 0; i < $scope.lists.concepts.length; i++) {
          if ($scope.lists.concepts[i].id != $scope.selected.concept.id) {
            $scope.prospectiveMergeConcepts.push($scope.lists.concepts[i]);
          }
        }
        if ($scope.prospectiveMergeConcepts.length == 1) {
          $scope.mergeConcept = $scope.prospectiveMergeConcepts[0];
        }
      }

      // Perform merge
      $scope.merge = function(concept1, concept2) {

        metaEditingService.mergeConcepts($scope.selected.project.id, $scope.selected.activityId,
          concept1, concept2, $scope.overrideWarnings).then(
        // Success
        function(data) {
          $scope.warnings = data.warnings;
          $scope.errors = data.errors;
          if ($scope.warnings.length > 0) {
            $scope.overrideWarnings = true;
          }
          if ($scope.warnings.length == 0 && $scope.errors.length == 0) {
            $uibModalInstance.close();
          }
        },
        // Error
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
      };

      $scope.selectMergeConcept = function(concept) {
        $scope.mergeConcept = concept;        
      }
      
      // Dismiss modal
      $scope.cancel = function() {
        $uibModalInstance.dismiss('cancel');
      };

      // initialize modal
      initialize();

      // end
    } ]);