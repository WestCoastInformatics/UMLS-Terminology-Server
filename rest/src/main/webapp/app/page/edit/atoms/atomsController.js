// Atoms types controller

tsApp
  .controller(
    'AtomsCtrl',
    [
      '$scope',
      '$http',
      '$location',
      '$routeParams',
      '$window',
      '$uibModal',
      '$q',
      'gpService',
      'utilService',
      'tabService',
      'securityService',
      'contentService',
      'metaEditingService',
      'websocketService',
      function($scope, $http, $location, $routeParams, $window, $uibModal, $q, gpService,
        utilService, tabService, securityService, contentService, metaEditingService,
        websocketService) {

        console.debug("configure AtomsCtrl");

        // remove tabs, header and footer
        tabService.setShowing(false);
        utilService.setHeaderFooterShowing(false);

        // preserve parent scope reference
        $scope.parentWindowScope = window.opener.$windowScope;
        $scope.parentClosing = false;
        window.$windowScope = $scope;
        $scope.selected = $scope.parentWindowScope.selected;
        $scope.lists = $scope.parentWindowScope.lists;
        $scope.user = $scope.parentWindowScope.user;
        $scope.selected.atoms = {};
        
        // Paging variables
        $scope.pageSizes = utilService.getPageSizes();
        $scope.paging = {};
        $scope.paging['atoms'] = utilService.getPaging();
        $scope.paging['atoms'].sortField = null;
        $scope.paging['atoms'].pageSize = 100000;
        $scope.paging['atoms'].filterFields = {};
        $scope.paging['atoms'].filterFields.name = 1;
        $scope.paging['atoms'].filterFields.codeId = 1;
        $scope.paging['atoms'].filterFields.descriptorId = 1;
        $scope.paging['atoms'].filterFields.conceptId = 1;
        $scope.paging['atoms'].filterFields.termType = 1;
        $scope.paging['atoms'].filterFields.codeId = 1;
        $scope.paging['atoms'].filterFields.terminology = 1;
        $scope.paging['atoms'].sortAscending = false;
        $scope.paging['atoms'].filterList = [];
        $scope.paging['atoms'].callbacks = {
          getPagedList : getPagedAtoms
        };

        // Watch for component changes
        $scope.$watch('selected.component', function() {
          $scope.getPagedAtoms();

          // Clear the checkbox selections if needed
          var conceptAtomIds = [];
          for (var i = 0; i < $scope.selected.component.atoms.length; i++) {
            conceptAtomIds.push($scope.selected.component.atoms[i].id);
          }
          for (atomIdStr in $scope.selected.atoms) {
            var atomId = parseInt(atomIdStr);
            if (!conceptAtomIds.includes(atomId)) {
              delete $scope.selected.atoms[atomId];
            }
          }

          // Clear filterList and reset based on current component
          $scope.paging['atoms'].filterList.length = 0;
          var list = $scope.getPagingFilterList();
          for (var i = 0; i < list.length; i++) {
            $scope.paging['atoms'].filterList.push(list[i]);
          }

        });

        // Scope method for accessing permissions
        $scope.editingDisabled = function() {
          return !$scope.selected.project.editingEnabled
            && !securityService.hasPermissions('OverrideEditDisabled');
        }

        // Get paging filter list
        $scope.getPagingFilterList = function() {
          var map = {};
          for (var i = 0; i < $scope.selected.component.atoms.length; i++) {
            map[$scope.selected.component.atoms[i].terminology] = 1;
          }
          var filterList = new Array();
          for ( var key in map) {
            filterList.push(key);
          }
          return filterList.sort();
        }

        // add atom
        $scope.addAtomToConcept = function(atom) {
          metaEditingService.addAtom($scope.selected.project.id, $scope.selected.activityId,
            $scope.selected.component, atom);
        }

        // remove atom
        $scope.removeAtomFromConcept = function(atom) {
          metaEditingService.removeAtom($scope.selected.project.id, $scope.selected.activityId,
            $scope.selected.component, atom.id, true);
        }

        // Get paged atoms (assume all are loaded)
        $scope.getPagedAtoms = function() {
          getPagedAtoms();
        }
        function getPagedAtoms() {
          // page from the stys that are available to add
          $scope.pagedAtoms = utilService.getPagedArray($scope.selected.component.atoms,
            $scope.paging['atoms']);
        }

        //
        // THE FOLLOWING CODE IS REPLICATED (with minor variation) in the 4
        // popup windows and the main edit window
        // 

        // Approve concept
        $scope.approveConcept = function(concept) {
          // Need a promise, so we can reload the tracking records
          var deferred = $q.defer();
          contentService.getConcept(concept.id, $scope.selected.project.id).then(
            function(data) {
              var concept = data;
              metaEditingService.approveConcept($scope.selected.project.id,
                $scope.selected.activityId, concept, false).then(
              // Success
              function(data) {
                deferred.resolve(data);
              },
              // Error
              function(data) {
                deferred.reject(data);
              });
            });
          return deferred.promise;
        }

        // Approves all selector concepts and moves on to next record
        $scope.approveNext = function() {
          var lastIndex = $scope.lists.concepts.length;
          var successCt = 0;
          for (var i = 0; i < $scope.lists.concepts.length; i++) {
            if (!$scope.lists.concepts[i].id) {
              continue;
            }
            // ignore the websocket event from this.
            websocketService.incrementConceptIgnore($scope.lists.concepts[i].id);
            $scope.approveConcept($scope.lists.concepts[i]).then(
            // Success
            function(data) {
              successCt++;
              if (successCt == lastIndex) {
                $scope.parentWindowScope.getRecords();
                $scope.next();
              }
            });
          }
        }

        // // approve concept
        // $scope.approveConcept = function() {
        // $scope.parentWindowScope.approveConcept($scope.selected.component);
        // }
        //
        // // approve next
        // $scope.approveNext = function() {
        // $scope.parentWindowScope.approveNext();
        // }
        //

        // next
        $scope.next = function() {
          $scope.parentWindowScope.next();
        }

        // Reload concept
        $scope.reloadConcept = function() {
          utilService.clearError();
          $scope.parentWindowScope.reloadConcept($scope.selected.component);
        }

        // refresh
        $scope.refresh = function() {
          $scope.$apply();
        }

        // notify edit controller when semantic type window closes
        $window.onbeforeunload = function(evt) {
          if (!$scope.parentClosing) {
            $scope.parentWindowScope.removeWindow('atom');
          }
        }
        $scope.$on('$destroy', function() {
          if (!$scope.parentClosing) {
            $scope.parentWindowScope.removeWindow('atom');
          }
        });

        // on window resize, save dimensions and screen location to user
        // preferences
        $window.onresize = function(evt) {
          clearTimeout(window.resizedFinished);
          window.resizedFinished = setTimeout(function() {
            console.log('Resized finished on atoms window.');
            $scope.user.userPreferences.properties['atomWidth'] = window.outerWidth;
            $scope.user.userPreferences.properties['atomHeight'] = window.outerHeight;
            $scope.user.userPreferences.properties['atomX'] = window.screenX;
            $scope.user.userPreferences.properties['atomY'] = window.screenY;
            $scope.parentWindowScope.saveWindowSettings('atom',
              $scope.user.userPreferences.properties);
          }, 250);
        }

        // Table sorting mechanism
        $scope.setSortField = function(table, field) {
          utilService.setSortField(table, field, $scope.paging);
          $scope.getPagedAtoms();
        };

        // Return up or down sort chars if sorted
        $scope.getSortIndicator = function(table, field) {
          return utilService.getSortIndicator(table, field, $scope.paging);
        };

        // Reset sort to "preferred"
        $scope.setSortPreferred = function() {
          $scope.paging['atoms'].sortField = null;
          $scope.paging['atoms'].sortAscending = false;
          $scope.getPagedAtoms();
        }

        // indicates the style for an atom
        $scope.getAtomClass = function(atom) {
          
          // DEMOTION (blue)
          if (atom.workflowStatus == 'DEMOTION')
            return 'DEMOTION';
          
          // NEEDS_REVIEW (red)
          if (atom.workflowStatus == 'NEEDS_REVIEW')
            return 'NEEDS_REVIEW';

          // UNRELEASABLE (green)
          if (!atom.publishable)
            return 'UNRELEASABLE';

          // RXNORM (orange)
          var orangeFlag = atom.attributes.filter(function (a) { 
            return a.name == 'AMBIGUITY_FLAG' && 
              a.value == 'Base' && 
              a.terminology == 'RXNORM' && 
              a.publishable; 
          });
          if (orangeFlag.length > 0) {
            return 'RXNORM';
          }  

          // OBSOLETE (purple)
          if (atom.obsolete)
            return 'OBSOLETE';

          // REVIEWED READY_FOR_PUBLICATION (black)
          return 'READY_FOR_PUBLICATION';

        }

        // select/deselect atom
        $scope.toggleSelection = function toggleSelection(atom) {
          // is currently selected
          if ($scope.selected.atoms[atom.id]) {
            delete $scope.selected.atoms[atom.id];
          }
          // is newly selected
          else {
            $scope.selected.atoms[atom.id] = atom;
          }
        };

        // indicates if a particular row is selected
        $scope.isRowSelected = function(atom) {
          return $scope.selected.atoms[atom.id];
        }

        // indicates the number of atoms selected
        $scope.getSelectedAtomCount = function() {
          return Object.keys($scope.selected.atoms).length;
        }

        // only atoms from allowed termgroups can be deleted
        $scope.isAtomDeleteable = function(atom) {
          for (var i = 0; i < $scope.selected.project.newAtomTermgroups.length; i++) {
            if ((atom.terminology + '/' + atom.termType) == $scope.selected.project.newAtomTermgroups[i]) {
              return true;
            }
          }
          return false;
        }

        // Toggle attributes for an atom
        $scope.attributes = {};
        $scope.toggleAttributes = function(atom) {
          if ($scope.attributes[atom.id]) {
            delete $scope.attributes[atom.id];
            return;
          }

          $scope.attributes[atom.id] = {};
          // Assemble the attributes for the atom
          $scope.attributes[atom.id].atom = angular.copy(atom.attributes.sort(utilService
            .sortBy('name')));
          if (atom) {
            $scope.attributes[atom.id].ct = atom.attributes.length;
          } else {
            $scope.attributes[atom.id].ct = 0;
          }
          if (atom.descriptorId) {
            contentService.getComponent({
              type : 'DESCRIPTOR',
              terminologyId : atom.descriptorId,
              terminology : atom.terminology,
              version : atom.version
            }).then(
              // Success
              function(data) {
                if (data && data.attributes.length > 0) {
                  $scope.attributes[atom.id].descriptor = data.attributes.sort(utilService
                    .sortBy('name'));
                  $scope.attributes[atom.id].ct += data.attributes.length;
                }
              });
          }
          if (atom.conceptId) {
            contentService.getComponent({
              type : 'CONCEPT',
              terminologyId : atom.conceptId,
              terminology : atom.terminology,
              version : atom.version
            }).then(
              // Success
              function(data) {
                if (data && data.attributes.length > 0) {
                  $scope.attributes[atom.id].concept = data.attributes.sort(utilService
                    .sortBy('name'));
                  $scope.attributes[atom.id].ct += data.attributes.length;
                }
              });
          }
          if (atom.codeId && atom.codeId != atom.conceptId && atom.codeId != atom.descriptorId) {
            contentService.getComponent({
              type : 'CODE',
              terminologyId : atom.codeId,
              terminology : atom.terminology,
              version : atom.version
            }).then(
            // Success
            function(data) {
              if (data && data.attributes.length > 0) {
                $scope.attributes[atom.id].code = data.attributes.sort(utilService.sortBy('name'));
                $scope.attributes[atom.id].ct += data.attributes.length;
              }
            });
          }

        }

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
              },
              lists : function() {
                return $scope.lists;
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
              },
              lists : function() {
                return $scope.lists;
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
            templateUrl : 'app/page/edit/mergeMoveSplit.html',
            controller : 'MergeMoveSplitModalCtrl',
            backdrop : 'static',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              lists : function() {
                return $scope.lists;
              },
              user : function() {
                return $scope.user;
              },
              action : function() {
                return 'Merge';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(data) {
            /*
             * $scope.parentWindowScope.getRecords(false);
             * $scope.parentWindowScope.getConcepts($scope.selected.record);
             */
          });
        };

        // Move modal
        $scope.openMoveModal = function() {
          if ($scope.getSelectedAtomCount() < 1 || $scope.lists.concepts.length < 2) {
            window
              .alert('Move requires at least one atom to be selected and at least two concepts to be in the concept list.');
            return;
          }
          if ($scope.selected.component.atoms.length == $scope.getSelectedAtomCount()) {
            window.alert('Not all atoms can be selected for move.  Concept cannot be left empty.');
            return;
          }
          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/edit/mergeMoveSplit.html',
            controller : 'MergeMoveSplitModalCtrl',
            backdrop : 'static',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              lists : function() {
                return $scope.lists;
              },
              user : function() {
                return $scope.user;
              },
              action : function() {
                return 'Move';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(data) {
 
          });
        };

        // Split modal
        $scope.openSplitModal = function() {
          if ($scope.getSelectedAtomCount() < 1) {
            window.alert('Split requires at least one atom to be selected.');
            return;
          }
          if ($scope.selected.component.atoms.length == $scope.getSelectedAtomCount()) {
            window.alert('Not all atoms can be selected for split.  Concept cannot be left empty.');
            return;
          }
          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/edit/mergeMoveSplit.html',
            controller : 'MergeMoveSplitModalCtrl',
            backdrop : 'static',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              lists : function() {
                return $scope.lists;
              },
              user : function() {
                return $scope.user;
              },
              action : function() {
                return 'Split';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(data) {
 
          });

        };

        $scope.openCodeConceptsWindow = function(atom, width, height) {

          var newUrl = utilService.composeUrl('/edit/codeConcepts');
          $scope.selected.linkedAtom = atom;
          window.$windowScope = $scope;

          if (width == null && height == null
            && $scope.user.userPreferences.properties['codeConceptsWidth']) {
            width = $scope.user.userPreferences.properties['codeConceptsWidth'];
            height = $scope.user.userPreferences.properties['codeConceptsHeight'];
          } else if (!$scope.user.userPreferences.properties['codeConceptsWidth']) {
            width = 600;
            height = 600;
          }
          $scope.windows['codeConcepts'] = $window.open(newUrl, 'codeConceptsWindow', 'width=' + width
            + ', height=' + height + ', scrollbars=yes');
          $scope.windows['codeConcepts'].document.title = 'Code Concepts Reference';
          $scope.windows['codeConcepts'].focus();
          if ($scope.user.userPreferences.properties.hasOwnProperty('codeConceptsX')) {
            $scope.windows['codeConcepts'].moveTo(
              $scope.user.userPreferences.properties['codeConceptsX'],
              $scope.user.userPreferences.properties['codeConceptsY']);
          }

          securityService.saveProperty($scope.user.userPreferences, 'codeConcepts', true);
        };

        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {
          $scope.getPagedAtoms();
        }

        // Call initialize
        $scope.initialize();

      } ]);