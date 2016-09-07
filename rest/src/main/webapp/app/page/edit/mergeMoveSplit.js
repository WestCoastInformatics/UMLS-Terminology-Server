// Merge modal controller
tsApp.controller('MergeMoveSplitModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'metaEditingService',
  'metadataService',
  'contentService',
  'selected',
  'lists',
  'user',
  'action',
  function($scope, $uibModalInstance, utilService, metaEditingService, metadataService,
    contentService, selected, lists, user, action) {
    console.debug('Entered merge/move/split modal control', lists, action);

    // Scope vars
    $scope.selected = selected;
    $scope.lists = lists;
    $scope.user = user;
    $scope.action = action;

    $scope.toConcepts = [];
    $scope.toConcept = null;
    $scope.copyRelationships = true;
    $scope.copySemanticTypes = true;
    $scope.overrideWarnings = false;
    $scope.selectedRelationshipType = 'RO';
    $scope.acceptedRelationshipTypeStrings = [ 'RO', 'RB', 'RN', 'RQ', 'XR' ];
    $scope.acceptedRelationshipTypes = [ {
      'key' : '',
      'value' : '(none)'
    } ];
    $scope.metadata = metadataService.getModel();
    $scope.warnings = [];
    $scope.errors = [];

    $scope.selectedWorkflowStatus = 'NEEDS_REVIEW';
    $scope.workflowStatuses = [ 'NEEDS_REVIEW', 'READY_FOR_PUBLICATION' ];

    // Init modal
    function initialize() {
      for (var i = 0; i < $scope.lists.concepts.length; i++) {
        if ($scope.lists.concepts[i].id != $scope.selected.concept.id) {
          $scope.toConcepts.push($scope.lists.concepts[i]);
        }
      }
      // if selected relationship, add to prospective list
      // set default from_concept
      if ($scope.selected.relationship) {
        contentService.getConcept($scope.selected.relationship.toId, $scope.selected.project.id)
          .then(function(data) {
            var found = false;
            for (var i = 0; i < $scope.toConcepts.length; i++) {
              if ($scope.toConcepts[i].id == data.id) {
                found = true;
              }
            }
            if (!found) {
              $scope.toConcepts.push(data);
            }
            $scope.toConcept = data;
            $scope.selectedRelationshipType = $scope.selected.relationship.relationshipType;
          });

      } else {
        $scope.toConcept = $scope.toConcepts[0];
      }

      // get metadata
      var projectTerminologyVersion = metadataService
        .getTerminologyVersion($scope.selected.project.terminology);
      var termToSet = metadataService.getTerminology($scope.selected.project.terminology,
        projectTerminologyVersion);
      metadataService.setTerminology(termToSet).then(
        function() {
          // only keep rel types that are on accepted list
          for (var i = 0; i < $scope.metadata.relationshipTypes.length; i++) {
            if ($scope.acceptedRelationshipTypeStrings
              .includes($scope.metadata.relationshipTypes[i].key)) {
              $scope.acceptedRelationshipTypes.push($scope.metadata.relationshipTypes[i]);
            }
          }
        });
    }

    // Perform merge
    $scope.merge = function() {

      metaEditingService.mergeConcepts($scope.selected.project.id, $scope.selected.activityId,
        $scope.selected.concept, $scope.toConcept, $scope.overrideWarnings).then(
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
    $scope.move = function() {
      var atomsList = [];
      for (atom in $scope.selected.atoms) {
        atomsList.push(atom);
      }
      metaEditingService.moveAtoms($scope.selected.project.id, $scope.selected.activityId,
        $scope.selected.concept, $scope.toConcept, atomsList, $scope.overrideWarnings).then(
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
    $scope.split = function() {
      var atomsList = [];
      for (atom in $scope.selected.atoms) {
        atomsList.push(atom);
      }

      metaEditingService.splitConcept($scope.selected.project.id, $scope.selected.activityId,
        $scope.selectedConcept, atomsList, $scope.copyRelationships, $scope.copySemanticTypes,
        $scope.selectedRelationshipType, $scope.overrideWarnings).then(
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
    $scope.selectToConcept = function(concept) {
      $scope.toConcept = concept;
    }

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // initialize modal
    initialize();

    // end
  } ]);