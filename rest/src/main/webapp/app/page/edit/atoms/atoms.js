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
    $scope.lists = $scope.parentWindowScope.lists;
    $scope.user = $scope.parentWindowScope.user;
    $scope.selected.atoms = {};
    
    
    // Paging variables
    $scope.paging = {};
    $scope.paging['atoms'] = utilService.getPaging();
    $scope.paging['atoms'].sortField = 'id';
    $scope.paging['atoms'].pageSize = 10;
    $scope.paging['atoms'].filterFields = {};
    $scope.paging['atoms'].filterFields.name = 1;
    $scope.paging['atoms'].filterFields.codeId = 1;
    $scope.paging['atoms'].filterFields.descriptorId = 1;
    $scope.paging['atoms'].filterFields.conceptId = 1;
    $scope.paging['atoms'].filterFields.termType = 1;
    $scope.paging['atoms'].filterFields.codeId = 1;
    $scope.paging['atoms'].filterFields.terminology = 1;
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
    
    // indicates the style for an atom
    $scope.getAtomClass = function(atom) {
      
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
    
    // selects an atom
    $scope.selectAtom = function(event, atom) {
      
      if (event.ctrlKey) {
        selectWithCtrl(atom);
      } else {
        $scope.selected.atoms = {};
        $scope.selected.atoms[atom.id] = atom;
      }
    };

    // selects or deselects additional atom
    function selectWithCtrl(atom) {
      if ($scope.selected.atoms[atom.id]) {
        delete $scope.selected.atoms[atom.id];
      } else {
        $scope.selected.atoms[atom.id] = atom;
      }
    }

    // indicates if a particular row is selected
    $scope.isRowSelected = function(atom) {
      return $scope.selected.atoms[atom.id];
    }
    
    // indicates the number of atoms selected
    $scope.getSelectedAtomCount = function() {
      return Object.keys($scope.selected.atoms).length;
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
            return null;
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


    // Merge modal
    $scope.openMergeModal = function() {
      if ($scope.lists.concepts.length < 2) {
        window.alert('Merge requires at least two concepts in the list.');
        return;
      }
      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/edit/merge.html',
        controller : 'MergeModalCtrl',
        backdrop : 'static',
        resolve : {
          selected : function() {
            return $scope.selected;
          },
          lists : function() {
            return $scope.lists;
          },
          action : function() {
            return 'Merge';
          },
          user : function() {
            return $scope.user;
          }
        }
      });
    

      modalInstance.result.then(
      // Success
      function(data) {
        $scope.parentWindowScope.getRecords(false);
        $scope.parentWindowScope.getConcepts($scope.selected.record);
      });
    };

      // Move modal
      $scope.openMoveModal = function() {
        if ($scope.getSelectedAtomCount() < 1 || $scope.lists.concepts.length < 2) {
          window.alert('Move requires at least one atom to be selected and at least two concepts to be in the concept list.');
          return;
        }
        if ($scope.selected.concept.atoms.length == $scope.getSelectedAtomCount()) {
          window.alert('Not all atoms can be selected for move.  Concept cannot be left empty.');
          return;
        }
        var modalInstance = $uibModal.open({
          templateUrl : 'app/page/edit/merge.html',
          controller : 'MergeModalCtrl',
          backdrop : 'static',
          resolve : {
            selected : function() {
              return $scope.selected;
            },
            lists : function() {
              return $scope.lists;
            },
            action : function() {
              return 'Move';
            },
            user : function() {
              return $scope.user;
            }
          }
        });

        modalInstance.result.then(
        // Success
        function(data) {
          $scope.parentWindowScope.getRecords(false);
          $scope.parentWindowScope.getConcepts($scope.selected.record, true);
        });     
    };
    
    
    // Split modal
    $scope.openSplitModal = function() {
      if ($scope.getSelectedAtomCount() < 1) {
        window.alert('Split requires at least one atom to be selected.');
        return;
      }
      if ($scope.selected.concept.atoms.length == $scope.getSelectedAtomCount()) {
        window.alert('Not all atoms can be selected for split.  Concept cannot be left empty.');
        return;
      }
      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/edit/merge.html',
        controller : 'MergeModalCtrl',
        backdrop : 'static',
        resolve : {
          selected : function() {
            return $scope.selected;
          },
          lists : function() {
            return $scope.lists;
          },
          action : function() {
            return 'Split';
          },
          user : function() {
            return $scope.user;
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        $scope.parentWindowScope.getRecords(false);
        $scope.parentWindowScope.getConcepts($scope.selected.record, true);
      });
      
    
  };

  } ]);