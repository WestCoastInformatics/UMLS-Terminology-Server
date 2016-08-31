// Merge modal controller
tsApp.controller('MergeModalCtrl',
  [
    '$scope',
    '$uibModalInstance',
    'utilService',
    'metaEditingService',
    'metadataService',
    'selected',
    'lists',
    'action',
    'user',
    function($scope, $uibModalInstance, utilService, metaEditingService, metadataService, selected, lists,
      action, user) {
      console.debug('Entered merge/move/split modal control', lists, action);

      // Scope vars
      $scope.selected = selected;
      $scope.lists = lists;
      $scope.action = action;
      $scope.prospectiveMergeConcepts = [];
      $scope.copyRelationships = true;
      $scope.copySemanticTypes = true;
      $scope.overrideWarnings = false;
      $scope.selectedRelationshipType = 'RO'; 
      $scope.acceptedRelationshipTypeStrings = ['RO', 'RB', 'RN', 'RQ', 'XR'];
      $scope.acceptedRelationshipTypes = [{'key': '', 'value':'(none)'}];
      $scope.metadata = metadataService.getModel();
      $scope.warnings = [];
      $scope.errors = [];

      // Init modal
      function initialize() {
        for (var i = 0; i < $scope.lists.concepts.length; i++) {
          if ($scope.lists.concepts[i].id != $scope.selected.concept.id) {
            $scope.prospectiveMergeConcepts.push($scope.lists.concepts[i]);
          }
        }
        $scope.mergeConcept = $scope.prospectiveMergeConcepts[0];
        
        // get metadata
        var projectTerminologyVersion = metadataService.getTerminologyVersion($scope.selected.project.terminology);
        var termToSet = metadataService.getTerminology($scope.selected.project.terminology, projectTerminologyVersion); 
        metadataService.setTerminology(termToSet).then(function() {
          // only keep rel types that are on accepted list
          for (var i=0; i<$scope.metadata.relationshipTypes.length; i++ ) {
            console.debug("retType", $scope.metadata.relationshipTypes[i]);
            if ($scope.acceptedRelationshipTypeStrings.includes($scope.metadata.relationshipTypes[i].key)) {
              $scope.acceptedRelationshipTypes.push($scope.metadata.relationshipTypes[i]);
            }              
          }
        });
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
      
      // Perform move
      $scope.move = function(concept1, concept2) {
        var atomsList = [];
        for (atom in $scope.selected.atoms) {
          atomsList.push(atom);
        }
        metaEditingService.moveAtoms($scope.selected.project.id, $scope.selected.activityId,
          concept1, concept2, atomsList, $scope.overrideWarnings).then(
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

      // Perform split
      $scope.split = function(concept) {
        var atomsList = [];
        for (atom in $scope.selected.atoms) {
          atomsList.push(atom);
        }
        
        metaEditingService.splitConcept($scope.selected.project.id, $scope.selected.activityId,
          concept, atomsList, $scope.copyRelationships, $scope.copySemanticTypes, $scope.selectedRelationshipType, $scope.overrideWarnings).then(
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
      
      
      // select the merge concept
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