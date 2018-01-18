// Edit relationship controller
tsApp.controller('EditRelationshipModalCtrl', [
  '$scope',
  '$uibModalInstance',
  '$uibModal',
  'utilService',
  'metadataService',
  'contentService',
  'metaEditingService',
  'selected',
  'replaceRelationship',
  'lists',
  'user',
  'action',
  function($scope, $uibModalInstance, $uibModal, utilService, metadataService, contentService,
    metaEditingService, selected, replaceRelationship, lists, user, action) {
    console.debug('Entered edit relationship modal control', lists, action);
    
    // Scope vars
    $scope.selected = selected;
    $scope.replaceRelationship = replaceRelationship;
    $scope.lists = lists;
    $scope.user = user;
    $scope.action = action;

    $scope.toConcepts = [];
    $scope.selectedToConcepts = {};
    $scope.selectedToConceptObjects = [];
    $scope.selectedConceptCount = 0;
    $scope.toConcept = null;
    $scope.overrideWarnings = false;
    $scope.selectedRelationshipType = 'RO';
    $scope.acceptedRelationshipTypeStrings = [ 'RO', 'RB', 'RN', 'BRO', 'BRB', 'BRN'];
    $scope.acceptedRelationshipTypes = [ {
      'key' : 'XR',
      'value' : '(none)'
    } ];
    $scope.warnings = [];
    $scope.errors = [];

    $scope.selectedWorkflowStatus = 'NEEDS_REVIEW';
    $scope.workflowStatuses = [ 'NEEDS_REVIEW', 'READY_FOR_PUBLICATION' ];
    $scope.defaultOrder = true;
    
    $scope.checkboxValue;

    // Callbacks for finder
    $scope.callbacks = {
      addComponent : addFinderComponent
    };
    utilService.extendCallbacks($scope.callbacks, metadataService.getCallbacks());
    utilService.extendCallbacks($scope.callbacks, contentService.getCallbacks());

    // Init modal
    function initialize() {
      for (var i = 0; i < $scope.lists.concepts.length; i++) {
        if ($scope.lists.concepts[i].id != $scope.selected.component.id) {
          $scope.toConcepts.push($scope.lists.concepts[i]);
        }
      }
      if ($scope.toConcepts.length == 1) {
        $scope.toConcept = $scope.toConcepts[0];
      }
      
      // if replacing, only keep that one relationship
      if($scope.replaceRelationship){
    	    $scope.toConcepts = [];
          contentService.getConcept($scope.replaceRelationship.fromId, $scope.selected.project.id)
          .then(function(data) {
        	$scope.toConcepts.push(data);
            $scope.toConcept = data;
            $scope.selectedRelationshipType = $scope.replaceRelationship.relationshipType;
          });
      }
      // if not replacing and if selected relationship, add to prospective list
      // set default from_concept
      else if (!$scope.replaceRelationship && $scope.selected.relationship) {
        contentService.getConcept($scope.selected.relationship.fromId, $scope.selected.project.id)
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

      // compute accepted relationship types
      if (!$scope.selected.component.publishable) {
        $scope.acceptedRelationshipTypeStrings.push('BRO');
        $scope.acceptedRelationshipTypeStrings.push('BRN');
        $scope.acceptedRelationshipTypeStrings.push('BBT');
      }
      for (var i = 0; i < $scope.selected.metadata.relationshipTypes.length; i++) {
        if ($scope.acceptedRelationshipTypeStrings
          .includes($scope.selected.metadata.relationshipTypes[i].key)) {
          $scope.acceptedRelationshipTypes.push($scope.selected.metadata.relationshipTypes[i]);
        }
      }

    }

    // Perform insert rel
    $scope.addRelationships = function() {
      $scope.errors = [];
      relationships = [];

      // Must have at least one concept selected
      if($scope.selectedToConceptObjects.length == 0){
          $scope.errors
          .push("Must select at least one To concept");
      }


      // Reverse the relationship Type based on NCI request NE-429
      var inverseRelationshipType = '';
      contentService.getInverseRelationshipType($scope.selectedRelationshipType, $scope.selected.project.terminology, $scope.selected.project.version).then(
      //Success
      function(relType) {
        inverseRelationshipType = relType;
      
	      // Create relationships for each selected ToConcept
	      for (var i = 0; i < $scope.selectedToConceptObjects.length; i++) {
	    	  $scope.toConcept = $scope.selectedToConceptObjects[i];
	    	                	  
		      // Only allow bequeathal to publishable
		      if (!$scope.toConcept.publishable && $scope.selectedRelationshipType.match(/BR./)) {
		        $scope.errors
		          .push("Illegal attempt to create a bequeathal relationship to an unpublishable concept");
		        return;
		      }
		      
		      var relationship = {
		        assertedDirection : false,
		        fromId : $scope.selected.component.id,
		        fromName : $scope.selected.component.name,
		        fromTerminology : $scope.selected.component.terminology,
		        fromTerminologyId : $scope.selected.component.terminologyId,
		        fromVersion : $scope.selected.component.version,
		        group : null,
		        hierarchical : false,
		        inferred : false,
		        name : null,
		        obsolete : false,
		        published : false,
		        relationshipType : inverseRelationshipType,
		        additionalRelationshipType : '',
		        stated : false,
		        suppressible : false,
		        terminology : $scope.selected.project.terminology,
		        terminologyId : "",
		        toId : $scope.toConcept.id,
		        toName : $scope.toConcept.name,
		        toTerminology : $scope.toConcept.terminology,
		        toTerminologyId : $scope.toConcept.terminologyId,
		        toVersion : $scope.toConcept.version,
		        type : "RELATIONSHIP",
		        version : $scope.toConcept.version,
		        workflowStatus : $scope.selectedWorkflowStatus
		      };
		
		      relationships.push(relationship);
	      } 
      
      //Once all relationships have been added to list, send the request
         metaEditingService.addRelationships($scope.selected.project.id, $scope.selected.activityId,
	        $scope.selected.component, relationships, $scope.overrideWarnings).then(
	      // Success
	      function(data) {
	        $scope.warnings.push(data.warnings);
	        $scope.errors.push(data.errors);
	        if ($scope.warnings.length > 0) {
	          $scope.overrideWarnings = true;
	        }
	      },
	      // Error
	      function(data) {
	        utilService.handleDialogError($scope.errors, data);
	      });
      });
      if ($scope.warnings.length == 0 && $scope.errors.length == 0) {
          $uibModalInstance.close();
      }
    }; 

    // select the to concept
    $scope.selectToConcept = function(concept) {
    	if($scope.selectedToConcepts[concept.id]){
    		$scope.selectedToConcepts[concept.id] = false;
    		for (var i =0; i < $scope.selectedToConceptObjects.length; i++) {
			   if ($scope.selectedToConceptObjects[i] == concept) {
				   $scope.selectedToConceptObjects.splice(i,1);
			      break;
			   }
    		}
    	}
    	else{
    		$scope.selectedToConcepts[concept.id] = true;
    	    $scope.selectedToConceptObjects.push(concept);
    	}
    	
    }
    
    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // Add finder component
    function addFinderComponent(data) {
      // return if concept is already on concept list
      for (var i = 0; i < $scope.lists.concepts.length; i++) {
        if ($scope.lists.concepts[i].id == data.id) {
          return;
        }
      }
      // If full concept, simply push
      if (data.atoms && data.atoms.length > 0) {
        $scope.toConcepts.push(data);
        $scope.selectToConcept(data);
        return;
      }

      // get full concept
      contentService.getConcept(data.id, $scope.selected.project.id).then(
      // Success
      function(data) {
        // $scope.lists.concepts.push(data);
        $scope.toConcepts.push(data);
      });
    }

    // initialize modal
    initialize();

    // end
  } ]);