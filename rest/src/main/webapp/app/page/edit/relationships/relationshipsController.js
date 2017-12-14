// Semantic types controller

tsApp
  .controller(
    'RelationshipsCtrl',
    [
      '$scope',
      '$window',
      '$uibModal',
      '$q',
      'utilService',
      'contentService',
      'tabService',
      'securityService',
      'metaEditingService',
      'websocketService',
      function($scope, $window, $uibModal, $q, utilService, contentService, tabService,
        securityService, metaEditingService, websocketService) {

        console.debug("configure RelationshipsCtrl");

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
        $scope.selected.relationship = null;
        $scope.preferredOnly = true;
        $scope.relatedConcept = null;

        // Paging variables
        $scope.pageSizes = utilService.getPageSizes();
        $scope.paging = {};
        $scope.paging['relationships'] = utilService.getPaging();
        $scope.paging['relationships'].sortField = 'lastModified';
        $scope.paging['relationships'].pageSize = 10;
        $scope.paging['relationships'].filterFields = {};
        $scope.paging['relationships'].filterFields.toName = 1;
        $scope.paging['relationships'].filterFields.fromName = 1;
        $scope.paging['relationships'].filterFields.toTerminologyId = 1;
        $scope.paging['relationships'].filterFields.fromTerminologyId = 1;
        $scope.paging['relationships'].filterFields.relationshipType = 1;
        $scope.paging['relationships'].filterFields.additionalRelationshipType = 1;
        $scope.paging['relationships'].filterFields.terminology = 1;
        $scope.paging['relationships'].filterFields.lastModifiedBy = 1;
        $scope.paging['relationships'].sortAscending = false;
        $scope.paging['relationships'].callbacks = {
          getPagedList : getPagedRelationships
        };

        // Watch for component changes
        $scope.$watch('selected.component', function() {
          $scope.selected.relationship = null;
          $scope.getPagedRelationships();
        });

        // Scope method for accessing permissions
        $scope.editingDisabled = function() {
          return !$scope.selected.project.editingEnabled
            && !securityService.hasPermissions('OverrideEditDisabled');
        }

        // add relationship
        $scope.addRelationshipToConcept = function(relationship) {
          metaEditingService.addRelationship($scope.selected.project.id,
            $scope.selected.activityId, $scope.selected.component, relationship);
        }

        // remove relationship
        $scope.removeRelationshipFromConcept = function(relationship) {
        // Because client wanted inverse rels displayed in relationship window
        // Need to load the related concept to run removeRelationship on.
            contentService.getConcept(relationship.fromId, $scope.selected.project.id).then(
                    // Success
                    function(data) {
                        metaEditingService.removeRelationship($scope.selected.project.id,
                                $scope.selected.activityId, data, relationship.id, true);

                   }); 
        }

        // Get paged relationships
        $scope.getPagedRelationships = function() {
          getPagedRelationships();
        }
        function getPagedRelationships() {
          contentService.findDeepRelationships({
            terminology : $scope.selected.project.terminology,
            version : $scope.selected.project.version,
            terminologyId : $scope.selected.component.terminologyId,
            type : $scope.selected.component.type
          }, true, true, $scope.preferredOnly, false, $scope.paging['relationships']).then(
            // Success
            function(data) {
              $scope.pagedRelationships = data.relationships;
              $scope.pagedRelationships.totalCount = data.totalCount

              for (var i = 0; i < $scope.pagedRelationships.length; i++) {
                $scope.pagedRelationships[i].level = $scope
                  .getRelationshipLevel($scope.pagedRelationships[i]);
              }
            });
        }

        $scope.transferConceptToEditor = function() {
          $scope.parentWindowScope.transferConceptToEditor($scope.selected.relationship.fromId);
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
            $scope.parentWindowScope.removeWindow('relationship');
          }
        }
        $scope.$on('$destroy', function() {
          if (!$scope.parentClosing) {
            $scope.parentWindowScope.removeWindow('relationshp');
          }
        });

        // on window resize, save dimensions and screen location to user
        // preferences
        $window.onresize = function(evt) {
          clearTimeout(window.resizedFinished);
          window.resizedFinished = setTimeout(function() {
            console.log('Resized finished on relationships window.');
            $scope.user.userPreferences.properties['relationshipWidth'] = window.outerWidth;
            $scope.user.userPreferences.properties['relationshipHeight'] = window.outerHeight;
            $scope.user.userPreferences.properties['relationshipX'] = window.screenX;
            $scope.user.userPreferences.properties['relationshipY'] = window.screenY;
            $scope.parentWindowScope.saveWindowSettings('relationship',
              $scope.user.userPreferences.properties);
          }, 250);
        }

        // Table sorting mechanism
        $scope.setSortField = function(table, field, object) {
          utilService.setSortField(table, field, $scope.paging);
          $scope.getPagedRelationships();
        };

        // Return up or down sort chars if sorted
        $scope.getSortIndicator = function(table, field) {
          return utilService.getSortIndicator(table, field, $scope.paging);
        };

        // indicates the style for an relationship
        $scope.getRelationshipClass = function(relationship) {

          // DEMOTION (blue)
          if (relationship.workflowStatus == 'DEMOTION')
            return 'DEMOTION';

          // NEEDS_REVIEW (red)
          if (relationship.workflowStatus == 'NEEDS_REVIEW')
            return 'NEEDS_REVIEW';

          // UNRELEASABLE (green)
          if (!relationship.publishable)
            return 'UNRELEASABLE';

          // OBSOLETE (purple)
          if (relationship.obsolete)
            return 'OBSOLETE';

          // RXNORM (orange)
          if (relationship.terminology == 'RXNORM') {
            return 'RXNORM';
          }

          // REVIEWED READY_FOR_PUBLICATION (black)
          return '';

        }

        // selects an relationship
        $scope.selectRelationship = function(event, relationship) {
          $scope.selected.relationship = relationship;
        };

        // indicates if a particular row is selected
        $scope.isRowSelected = function(relationship) {
          return $scope.selected.relationship && $scope.selected.relationship.id == relationship.id;
        };

        // returns relationship level
        $scope.getRelationshipLevel = function(rel) {
          if (rel.workflowStatus == 'DEMOTION') {
            return 'P';
          } else if (rel.terminology == rel.fromTerminology) {
            return 'C';
          } else {
            return 'S';
          }
        };

        //
        // Modals
        //

        // Merge modal
        $scope.openMergeModal = function() {

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
            $scope.getPagedRelationships();
          });
        };

        // Insert modal
        $scope.openInsertModal = function() {
          if (!$scope.selected.relationship && $scope.lists.concepts.length < 2) {
            window
              .alert('There is only one concept on the concept list.  Select a \'to\' concept for the relationship.');
            return;
          }
          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/edit/relationships/editRelationship.html',
            controller : 'EditRelationshipModalCtrl',
            backdrop : 'static',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              replaceRelationship : function() {
                  return null;
                },
              lists : function() {
                return $scope.lists;
              },
              user : function() {
                return $scope.user;
              },
              action : function() {
                return 'Add';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(data) {
            $scope.getPagedRelationships();
          });
        };
        
        // Replace modal
        $scope.openReplaceModal = function(relationship) {
          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/edit/relationships/editRelationship.html',
            controller : 'EditRelationshipModalCtrl',
            backdrop : 'static',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              replaceRelationship : function() {
                return relationship;
              },
              lists : function() {
                return $scope.lists;
              },
              user : function() {
                return $scope.user;
              },
              action : function() {
                return 'Replace';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(data) {     	  
            $scope.getPagedRelationships();
          });
        };
        

        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {
          $scope.getPagedRelationships();
        }

        // Call initialize
        $scope.initialize();

      } ]);
