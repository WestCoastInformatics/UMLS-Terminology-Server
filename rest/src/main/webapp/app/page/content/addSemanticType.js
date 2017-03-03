// Simple atom modal controller
tsApp.controller('SimpleSemanticTypeModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'metadataService',
  'utilService',
  'editService',
  'selected',
  'lists',
  function($scope, $uibModalInstance, metadataService, utilService, editService, selected, lists) {
    console.debug('Entered simple atom modal control', selected, lists);

    // Scope vars
    $scope.selected = selected;
    $scope.lists = lists;
    $scope.overrideWarnings = false;
    $scope.warnings = [];
    $scope.errors = [];
    $scope.workflowStatuses = [ 'NEEDS_REVIEW', 'READY_FOR_PUBLICATION' ];
    $scope.paging = {};
    $scope.paging['stys'] = utilService.getPaging();
    $scope.styPageCallbacks = {
      getPagedList : getPagedList
    }
    
    // Init modal
    function initialize() {
      // Initialize metadata
      metadataService.getSemanticTypes($scope.selected.project.terminology,
        $scope.selected.project.version).then(function(response) {
        console.debug('semantic types', response);

        $scope.fullStys = response.types;
        getPagedList();
      });
    }
    
 // Get paged stys (assume all are loaded)
    $scope.getPagedList = function() {
      getPagedStys();
    }
    function getPagedList() {
      $scope.stysForDisplay = [];
      // only display stys that aren't already on concept
      for (var i = 0; i < $scope.fullStys.length; i++) {
        var found = false;
        for (var j = 0; j < $scope.selected.component.semanticTypes.length; j++) {
          if ($scope.selected.component.semanticTypes[j].semanticType == $scope.fullStys[i].expandedForm) {
            found = true;
            break;
          }
        }
        if (!found) {
          $scope.stysForDisplay.push($scope.fullStys[i]);
        }
      }
      // page from the stys that are available to add
      $scope.pagedStys = utilService
        .getPagedArray($scope.stysForDisplay, $scope.paging['stys']);
      
      console.debug('paged stys', $scope.pagedStys);
    }

    // Perform add or edit/update
    $scope.submitSemanticType = function(sty) {
      $scope.errors = [];

      if (!sty) {
        $scope.errors.push('Name and  termgroup must be selected.');
        return;
      }
   

      editService.addSemanticType($scope.selected.project.id, $scope.selected.component.id, sty).then(
      // Success
      function(data) {
        $uibModalInstance.close();
      },
      // Error
      function(data) {
        utilService.handleDialogError($scope.errors, data);
      });

    };

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // initialize modal
    initialize();

    // end
  } ]);