// Semantic types controller

tsApp.controller('AtomsCtrl', [
  '$scope',
  '$http',
  '$location',
  '$routeParams',
  '$window',
  'gpService',
  'utilService',
  'tabService',
  'securityService',
  'utilService',
  'metadataService',
  'metaEditingService',
  '$uibModal',
  function($scope, $http, $location, $routeParams, $window, gpService, utilService, tabService,
    securityService, utilService, metadataService, metaEditingService, $uibModal) {

    console.debug("configure AtomsCtrl");

    // remove tabs, header and footer
    tabService.setShowing(false);
    utilService.setHeaderFooterShowing(false);

    // preserve parent scope reference
    $scope.parentWindowScope = window.opener.$windowScope;
    window.$windowScope = $scope;
    $scope.selected = $scope.parentWindowScope.selected;

    
    
    // Paging variables
    $scope.paging = {};
    $scope.paging['atoms'] = utilService.getPaging();
    $scope.paging['atoms'].sortField = 'id';
    $scope.paging['atoms'].pageSize = 10;
    $scope.paging['atoms'].filterFields = {};
    $scope.paging['atoms'].filterFields.expandedForm = 1;
    $scope.paging['atoms'].filterFields.typeId = 1;
    $scope.paging['atoms'].filterFields.treeNumber = 1;
    $scope.paging['atoms'].sortAscending = false;
    $scope.paging['atoms'].callback = {
      getPagedList : getPagedAtoms
    };

    
    $scope.$watch('selected.concept', function() {
      console.debug('in watch');
      $scope.getPagedAtoms();
    });

    // add atom
    $scope.addAtomToConcept = function(atom) {
      metaEditingService.addAtom($scope.selected.project.id, null, $scope.selected.concept,
        atom);
    }

    // remove atom
    $scope.removeAtomFromConcept = function(atom) {
      metaEditingService.removeAtom($scope.selected.project.id, null,
        $scope.selected.concept, atom.id, true);
    }

    // Get paged atoms (assume all are loaded)
    $scope.getPagedAtoms = function() {
      getPagedAtoms();
    }
    function getPagedAtoms() {
      // page from the stys that are available to add
      $scope.pagedAtoms = utilService.getPagedArray($scope.selected.concept.atoms, $scope.paging['atoms']);
    };

    // approve concept
    $scope.approveConcept = function() {
      $scope.parentWindowScope.approveConcept($scope.selected.concept);
    }

    // approve next
    $scope.approveNext = function() {
      $scope.parentWindowScope.approveNext();
    }
    
    // next
    $scope.next = function() {
      $scope.parentWindowScope.next();
    }

    // refresh
    $scope.refresh = function() {
      $scope.$apply();
    }

    // notify edit controller when semantic type window closes
    $window.onbeforeunload = function(evt) {
      $scope.parentWindowScope.removeWindow('atom');
    }

    // Table sorting mechanism
    $scope.setSortField = function(table, field, object) {
      utilService.setSortField(table, field, $scope.paging);
      $scope.getPagedAtoms();
    };

    // Return up or down sort chars if sorted
    $scope.getSortIndicator = function(table, field) {
      return utilService.getSortIndicator(table, field, $scope.paging);
    };
    
    $scope.getAtomStatus = function(atom) {
      
        // NEEDS_REVIEW (red)
        if (atom.workflowStatus == 'NEEDS_REVIEW') 
          return 'NEEDS_REVIEW';

        // UNRELEASABLE (green)
        if (!atom.publishable)
          return 'UNRELEASABLE';
        
        // RXNORM (orange)
        if (atom.terminology == 'RXNORM') {
          return 'RXNORM';
        }
        
        // OBSOLETE (purple)
        if (atom.obsolete)
          return 'OBSOLETE';
        
        // REVIEWED READY_FOR_PUBLICATION (black)
        return 'READY_FOR_PUBLICATION';
        
    }
    
    //
    // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
    //
    $scope.initialize = function() {    
          $scope.getPagedAtoms();

    }

    // Call initialize
    $scope.initialize();

    //
    // Modals
    //
    // Add atom modal
    $scope.openAddAtomModal = function(latom) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/edit/atoms/editAtom.html',
        backdrop : 'static',
        controller : 'AtomModalCtrl',
        resolve : {
          atom : function() {
            return latom;
          },
          action : function() {
            return 'Add';
          },
          selected : function() {
            return $scope.selected;
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(user) {
        $scope.getPagedAtoms();
      });
    };

    // Edit atom modal
    $scope.openEditAtomModal = function(latom) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/edit/atoms/editAtom.html',
        backdrop : 'static',
        controller : 'AtomModalCtrl',
        resolve : {
          atom : function() {
            return latom;
          },
          action : function() {
            return 'Edit';
          },
          selected : function() {
            return $scope.selected;
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(user) {
        $scope.getPagedAtoms();
      });
    };
  } ]);