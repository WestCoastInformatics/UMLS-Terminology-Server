// Edit controller
tsApp
  .controller(
    'EditCtrl',
    [
      '$scope',
      '$location',
      '$window',
      '$q',
      '$uibModal',
      'tabService',
      'utilService',
      'configureService',
      'securityService',
      'websocketService',
      'workflowService',
      'configureService',
      'projectService',
      'metadataService',
      'contentService',
      'reportService',
      'metaEditingService',
      function($scope, $location, $window, $q, $uibModal, tabService, utilService,
        configureService, securityService, websocketService, workflowService, configureService,
        projectService, metadataService, contentService, reportService, metaEditingService) {
        console.debug("configure EditCtrl");

        // Set up tabs and controller
        tabService.setShowing(true);
        utilService.clearError();
        $scope.user = securityService.getUser();
        projectService.getUserHasAnyRole();
        tabService.setSelectedTabByLabel('Edit');

        // scope vars
        $scope.assignedCt = 0;
        $scope.availableCt = 0;
        $scope.checklistCt = 0;

        // Callbacks for report
        $scope.callbacks = {
          addComponent : addFinderComponent
        };
        utilService.extendCallbacks($scope.callbacks, metadataService.getCallbacks());
        utilService.extendCallbacks($scope.callbacks, contentService.getCallbacks());

        // Selected variables
        $scope.selected = {
          project : null,
          projectRole : null,
          worklist : null,
          record : null,
          component : null,
          worklistMode : $scope.user.userPreferences.properties['worklistModeTab'] ? $scope.user.userPreferences.properties['worklistModeTab']
            : 'Assigned',
          terminology : null,
          metadata : metadataService.getModel()
        };

        // Lists
        $scope.lists = {
          records : [],
          worklists : [],
          configs : [],
          projects : [],
          components : [],
          projectRoles : [],
          recordTypes : workflowService.getRecordTypes(),
          worklistModes : [ 'Available', 'Assigned', 'Checklists' ],
          terminologies : []
        }

        // Windows
        $scope.windows = {};

        // Paging variables
        $scope.paging = {};
        $scope.paging['worklists'] = utilService.getPaging();
        $scope.paging['worklists'].sortField = 'lastModified';
        $scope.paging['worklists'].callbacks = {
          getPagedList : getWorklists
        };

        $scope.paging['records'] = utilService.getPaging();
        $scope.paging['records'].sortField = 'clusterId';
        $scope.paging['records'].callbacks = {
          getPagedList : getRecords
        };

        // Handle workflow changes
        $scope.$on('termServer::checklistChange', function(event, data) {
          if (data.id == $scope.selected.project.id) {
            // Checklists changed, refresh checklists list if showing
            if ($scope.selected.worklistMode == 'Checklists') {
              $scope.getWorklists();
            } else {
              $scope.getChecklistCt();
            }

          }
        });

        $scope.$on('termServer::worklistChange', function(event, data) {
          if (data.id == $scope.selected.project.id) {
            // Worklists changed, refresh worklists if not checklists tab
            if ($scope.selected.worklistMode != 'Checklists') {
              $scope.getWorklists();
            } else {
              $scope.getAssignedWorklistCt();
              $scope.getAvailableWorklistCt();
            }
          }
        });

        // Handle changes from actions performed by this user
        $scope
          .$on(
            'termServer::conceptChange',
            function(event, concept) {

              // Refresh the selected concept
              if ($scope.selected.component.id == concept.id) {
                contentService.getConcept(concept.id, $scope.selected.project.id).then(
                // Success
                function(data) {
                  if (!data) {
                    // if selected concept no longer exists, just bail from this
                    $scope.removeConceptFromList(concept);
                  } else {
                    $scope.selectConcept(data);
                  }
                  $scope.getRecords();

                });
              }

              // If a concept is referenced that isn't selected,
              // update the concept in the list, and/or add to the list
              else {
                // well
                var found = false;

                for (var i = 0; i < $scope.lists.concepts.length; i++) {
                  var c = $scope.lists.concepts[i];
                  if (c.id == concept.id) {
                    contentService
                      .getConcept(c.id, $scope.selected.project.id)
                      .then(
                        // Success
                        function(data) {
                          if (!data) {
                            $scope.removeConceptFromList(concept);
                          } else {
                            $scope.lists.concepts[i] = data;

                            if ($scope.user.userPreferences.properties['editConcept']) {
                              for (var i = 0; i < $scope.lists.concepts.length; i++) {
                                if ($scope.lists.concepts[i].id == $scope.user.userPreferences.properties['editConcept']) {
                                  $scope.selectConcept($scope.lists.concepts[i]);
                                }
                                ;
                              }
                            } else {
                              $scope.selectConcept($scope.lists.concepts[0]);
                            }
                          }
                          $scope.getRecords();
                        });
                    found = true;
                  }
                }

                // If no matching concept found, add it to to the list
                if (!found) {
                  contentService.getConcept(concept.id, $scope.selected.project.id).then(
                  // Success
                  function(data) {
                    // no need to remove anything or select anything
                    $scope.lists.concepts.push(data);
                    $scope.refreshWindows();
                    $scope.getRecords();
                  });
                }
              }
            });

        // Remove a concept from the concepts list
        $scope.removeConceptFromList = function(concept) {
          for (var i = 0; i < $scope.lists.concepts.length; i++) {
            var c = $scope.lists.concepts[i];
            if (concept.id == c.id) {
              // Cut this element out
              $scope.lists.concepts.splice(i, 1);
              break;
            }
          }
        }

        // Set worklist mode
        $scope.setWorklistMode = function(mode) {
          $scope.selected.worklistMode = mode;
          $scope.getWorklists();
          securityService.saveProperty($scope.user.userPreferences, 'worklistModeTab',
            $scope.selected.worklistMode);
        }

        // Get $scope.lists.worklists
        // switch based on type
        $scope.getWorklists = function(worklist) {
          getWorklists(worklist);
        }
        function getWorklists(worklist) {
          $scope.clearLists();
          if ($scope.selected.worklistMode == 'Available') {
            $scope.getAvailableWorklists();
          } else if ($scope.selected.worklistMode == 'Assigned') {
            $scope.getAssignedWorklists();
          } else if ($scope.selected.worklistMode == 'Checklists') {
            $scope.getChecklists();
          }
        }

        // Get all available worklists with project and type
        $scope.getAvailableWorklists = function() {
          var paging = $scope.paging['worklists'];
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : paging.filter
          };
          workflowService
            .findAvailableWorklists($scope.selected.project.id, $scope.user.userName,
              $scope.selected.projectRole, pfs)
            .then(
              // Success
              function(data) {
                $scope.lists.worklists = data.worklists;
                $scope.lists.worklists.totalCount = data.totalCount
                $scope.availableCt = data.totalCount;
                $scope.resetSelected();
                $scope.getAssignedWorklistCt();
                $scope.getChecklistCt();
                // select previously selected list if saved in user preferences
                if ($scope.user.userPreferences.properties['editWorklist']) {
                  for (var i = 0; i < $scope.lists.worklists.length; i++) {
                    if ($scope.lists.worklists[i].id == $scope.user.userPreferences.properties['editWorklist']) {
                      $scope.selectWorklist($scope.lists.worklists[i]);
                    }
                    ;
                  }
                }
              });

        };
        $scope.getAvailableWorklistCt = function() {
          var pfs = {
            startIndex : 0,
            maxResults : 1
          };
          workflowService.findAvailableWorklists($scope.selected.project.id, $scope.user.userName,
            $scope.selected.projectRole, pfs).then(
          // Success
          function(data) {
            $scope.availableCt = data.totalCount;
          });
        };

        // Get assigned worklists with project and type
        $scope.getAssignedWorklists = function() {
          var paging = $scope.paging['worklists'];
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : paging.filter
          };

          workflowService
            .findAssignedWorklists($scope.selected.project.id, $scope.user.userName,
              $scope.selected.projectRole, pfs)
            .then(
              // Success
              function(data) {
                $scope.lists.worklists = data.worklists;
                $scope.lists.worklists.totalCount = data.totalCount;
                $scope.assignedCt = data.totalCount;
                $scope.resetSelected();
                $scope.getAvailableWorklistCt();
                $scope.getChecklistCt();
                // select previously selected list if saved in user preferences
                if ($scope.user.userPreferences.properties['editWorklist']) {
                  for (var i = 0; i < $scope.lists.worklists.length; i++) {
                    if ($scope.lists.worklists[i].id == $scope.user.userPreferences.properties['editWorklist']) {
                      $scope.selectWorklist($scope.lists.worklists[i]);
                    }
                    ;
                  }
                }
              });
        };
        $scope.getAssignedWorklistCt = function() {
          var pfs = {
            startIndex : 0,
            maxResults : 1
          };
          workflowService.findAssignedWorklists($scope.selected.project.id, $scope.user.userName,
            $scope.selected.projectRole, pfs).then(
          // Success
          function(data) {
            $scope.assignedCt = data.totalCount;
          });
        };

        // Find checklists
        $scope.getChecklists = function() {
          var paging = $scope.paging['worklists'];
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : paging.filter
          };

          workflowService
            .findChecklists($scope.selected.project.id, '', pfs)
            .then(
              // Success
              function(data) {
                $scope.lists.worklists = data.checklists;
                $scope.lists.worklists.totalCount = data.totalCount;
                $scope.checklistCt = data.totalCount;
                $scope.resetSelected();
                $scope.getAssignedWorklistCt();
                $scope.getAvailableWorklistCt();
                // select previously selected list if saved in user preferences
                if ($scope.user.userPreferences.properties['editWorklist']) {
                  for (var i = 0; i < $scope.lists.worklists.length; i++) {
                    if ($scope.lists.worklists[i].id == $scope.user.userPreferences.properties['editWorklist']) {
                      $scope.selectWorklist($scope.lists.worklists[i]);
                    }
                    ;
                  }
                }
              });
        }
        $scope.getChecklistCt = function() {
          var pfs = {
            startIndex : 0,
            maxResults : 1
          };
          workflowService.findChecklists($scope.selected.project.id, '', pfs).then(
          // Success
          function(data) {
            $scope.checklistCt = data.totalCount;
          });
        };

        // handle change in project role
        $scope.changeProjectRole = function() {
          // save the change
          securityService.saveRole($scope.user.userPreferences, $scope.selected.projectRole);
          // $scope.resetPaging();
          $scope.getWorklists();
        }

        // Set the project
        $scope.setProject = function(project) {
          $scope.selected.project = project;

          // Get role for project (requires a lookup and will save user prefs
          projectService.getRoleForProject($scope.user, $scope.selected.project.id).then(
          // Success
          function(data) {
            // Get role and set role options
            $scope.selected.projectRole = data.role;
            $scope.lists.projectRoles = data.options;

            // Get worklists
            // $scope.resetPaging();
            $scope.getWorklists();
          });

          // $scope.lists.terminologies exists here
          // set the metadata service terminology to match
          for (var i = 0; i < $scope.lists.terminologies.length; i++) {
            var terminology = $scope.lists.terminologies[i];
            // this works because we're only getting current terminologies
            if (terminology.terminology == project.terminology) {
              metadataService.setTerminology(terminology);
            }
          }

          // Initialize metadata - this also sets the model
          metadataService.getAllMetadata($scope.selected.project.terminology,
            $scope.selected.project.version).then(
          // Success
          function(data) {
            metadataService.setModel(data);
          });

          $scope.removeWindows();

        }

        // Reset paging
        $scope.resetPaging = function() {
          $scope.paging['worklists'].page = 1;
          $scope.paging['worklists'].filter = null;
          $scope.paging['records'].page = 1;
          $scope.paging['records'].filter = null;
        }

        // Get all projects for the user
        $scope.getProjects = function() {
          projectService.getProjectsForUser($scope.user).then(
          // Success
          function(data) {
            $scope.lists.projects = data.projects;
            $scope.setProject(data.project);
          });

        };

        // Selects a worklist (setting $scope.selected.worklist).
        // Looks up current release info and records.
        $scope.selectWorklist = function(worklist) {
          $scope.selected.worklist = worklist;
          $scope.selected.component = null;
          if ($scope.value == 'Worklist') {
            $scope.parseStateHistory(worklist);
          }
          $scope.getRecords(worklist, true);
          // Set activity id
          $scope.selected.activityId = worklist.name;
          $scope.user.userPreferences.properties['editWorklist'] = $scope.selected.worklist.id;
          $scope.user.userPreferences.properties['editWorklistPaging'] = JSON
            .stringify($scope.paging['worklists']);
          securityService.updateUserPreferences($scope.user.userPreferences);
        };

        // select record from 'Cluster' list
        $scope.selectRecord = function(record) {
          $scope.selected.record = record;

          // Don't push concepts on if in available modes
          if ($scope.worklistMode != 'Available') {
            $scope.getConcepts(record, true);
          }
          $scope.user.userPreferences.properties['editRecord'] = $scope.selected.record.id;
          $scope.user.userPreferences.properties['editRecordPaging'] = JSON
            .stringify($scope.paging['records']);
          securityService.updateUserPreferences($scope.user.userPreferences);
        }

        // refresh the concept list
        $scope.getConcepts = function(record, selectFirst) {
          $scope.lists.concepts = [];
          for (var i = 0; i < $scope.selected.record.concepts.length; i++) {
            contentService.getConcept($scope.selected.record.concepts[i].id,
              $scope.selected.project.id).then(function(data) {
              // prevent duplicates (due to websocket msgs) from being added
              // to concept list
              var found = false;
              for (var j = 0; j < $scope.lists.concepts.length; j++) {
                if ($scope.lists.concepts[j].id == data.id) {
                  found = true;
                }
              }
              if (!found) {
                $scope.lists.concepts.push(data);
                $scope.refreshWindows();
                $scope.lists.concepts.sort(utilService.sortBy('id'));
                // Select first, when the first concept is loaded
                if (selectFirst && data.id == $scope.selected.record.concepts[0].id) {
                  $scope.selectConcept($scope.lists.concepts[0]);
                }
              }
            });
          }
        }

        // from the worklist, select the record after the currently selected one
        $scope.selectNextRecord = function(record) {
          for (var i = 0; i < $scope.lists.records.length - 1; i++) {
            if (record.id == $scope.lists.records[i].id) {
              $scope.selectRecord($scope.lists.records[++i]);
              return;
            }
          }
          // next record is not loaded, need to get more
          if ($scope.lists.records.totalCount > $scope.paging['records'].pageSize
            * $scope.paging['records'].page) {
            $scope.paging['records'].page += 1;
            $scope.getRecords(true);
          } else {
            confirm('No more clusters.');
          }
        }

        // refresh windows
        $scope.refreshWindows = function() {
          for ( var key in $scope.windows) {
            if ($scope.windows[key] && $scope.windows[key].$windowScope) {
              $scope.windows[key].$windowScope.refresh();
            }
          }
        }

        // remove window from map when it is closed
        $scope.removeWindow = function(windowName) {
          if ($scope.windows.hasOwnProperty(windowName)) {
            delete $scope.windows[windowName];
          }
          securityService.saveProperty($scope.user.userPreferences, windowName, false);
        }

        // remove windows
        $scope.removeWindows = function() {
          for ( var win in $scope.windows) {
            delete $scope.windows[win];
            securityService.saveProperty($scope.user.userPreferences, win, false);
          }
        }

        // focus windows and open those saved to user preferences
        $scope.focusWindows = function() {
          // focus windows that are already open
          for ( var win in $scope.windows) {
            $scope.windows[win].focus();
          }
          var width = 400;
          var height = 400;
          // open windows that were saved to user preferences
          if (!$scope.windows['semanticType']
            && $scope.user.userPreferences.properties['semanticType'] == 'true') {
            if ($scope.user.userPreferences.properties['semanticTypeWidth']) {
              width = $scope.user.userPreferences.properties['semanticTypeWidth'];
            }
            if ($scope.user.userPreferences.properties['semanticTypeHeight']) {
              height = $scope.user.userPreferences.properties['semanticTypeHeight'];
            }
            $scope.openStyWindow(width, height);
          }
          if (!$scope.windows['relationship']
            && $scope.user.userPreferences.properties['relationship'] == 'true') {
            if ($scope.user.userPreferences.properties['relationshipWidth']) {
              width = $scope.user.userPreferences.properties['relationshipWidth'];
            }
            if ($scope.user.userPreferences.properties['relationshipHeight']) {
              height = $scope.user.userPreferences.properties['relationshipHeight'];
            }
            $scope.openRelationshipsWindow(width, height);
          }
          if (!$scope.windows['context']
            && $scope.user.userPreferences.properties['context'] == 'true') {
            if ($scope.user.userPreferences.properties['contextWidth']) {
              width = $scope.user.userPreferences.properties['contextWidth'];
            }
            if ($scope.user.userPreferences.properties['contextHeight']) {
              height = $scope.user.userPreferences.properties['contextHeight'];
            }
            $scope.openContextsWindow(width, height);
          }
          if (!$scope.windows['atom'] && $scope.user.userPreferences.properties['atom'] == 'true') {
            if ($scope.user.userPreferences.properties['atomWidth']) {
              width = $scope.user.userPreferences.properties['atomWidth'];
            }
            if ($scope.user.userPreferences.properties['atomHeight']) {
              height = $scope.user.userPreferences.properties['atomHeight'];
            }
            $scope.openAtomsWindow(width, height);
          }
        }

        // select concept & get concept report
        $scope.selectConcept = function(concept) {

          $scope.selected.component = concept;

          securityService.saveProperty($scope.user.userPreferences, 'editConcept',
            $scope.selected.component.id);

          $scope.refreshWindows();

          // Update the selected concept in the list
          for (var i = 0; i < $scope.lists.concepts.length; i++) {
            var concept = $scope.lists.concepts[i];
            if (concept.id == $scope.selected.component.id) {
              $scope.lists.concepts[i] = $scope.selected.component;
              break;
            }
          }
        };

        // Get $scope.lists.records
        $scope.getRecords = function(selectFirst) {
          getRecords(selectFirst);
        }
        function getRecords(selectFirst) {
          var paging = $scope.paging['records'];

          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : paging.filter
          };

          if ($scope.paging['records'].typeFilter) {
            var value = $scope.paging['records'].typeFilter;

            // Handle status
            if (value == 'N') {
              if (pfs.queryRestriction != null && pfs.queryRestriction != '')
                pfs.queryRestriction += ' AND workflowStatus:N*';
              else
                pfs.queryRestriction = 'workflowStatus:N*';
            } else if (value == 'R') {
              if (pfs.queryRestriction != null && pfs.queryRestriction != '')
                pfs.queryRestriction += ' AND workflowStatus:R*';
              else
                pfs.queryRestriction = 'workflowStatus:R*';
            }
          }

          if ($scope.selected.worklistMode == 'Available'
            || $scope.selected.worklistMode == 'Assigned') {
            workflowService
              .findTrackingRecordsForWorklist($scope.selected.project.id,
                $scope.selected.worklist.id, pfs)
              .then(
                // Success
                function(data) {
                  $scope.lists.records = data.records;
                  $scope.lists.records.totalCount = data.totalCount;

                  // select previously selected record if saved in user
                  // preferences
                  if ($scope.user.userPreferences.properties['editRecord']) {
                    for (var i = 0; i < $scope.lists.records.length; i++) {
                      if ($scope.lists.records[i].id == $scope.user.userPreferences.properties['editRecord']) {
                        $scope.selectRecord($scope.lists.records[i]);
                      }
                    }
                  } else if (selectFirst) {
                    $scope.selectRecord($scope.lists.records[0]);
                  }
                });
          } else if ($scope.selected.worklistMode == 'Checklists') {
            workflowService
              .findTrackingRecordsForChecklist($scope.selected.project.id,
                $scope.selected.worklist.id, pfs)
              .then(
                // Success
                function(data) {
                  $scope.lists.records = data.records;
                  $scope.lists.records.totalCount = data.totalCount;

                  // select previously selected record if saved in user
                  // preferences
                  if ($scope.user.userPreferences.properties['editRecord']) {
                    for (var i = 0; i < $scope.lists.records.length; i++) {
                      if ($scope.lists.records[i].id == $scope.user.userPreferences.properties['editRecord']) {
                        $scope.selectRecord($scope.lists.records[i]);
                      }
                    }
                  } else if (selectFirst) {
                    $scope.selectRecord($scope.lists.records[0]);
                  }
                });
          }

        }

        // claim worklist
        $scope.assignWorklistToSelf = function(worklist) {
          workflowService.performWorkflowAction($scope.selected.project.id, worklist.id,
            $scope.user.userName, $scope.selected.projectRole, 'ASSIGN').then(
          // Success
          function(data) {
            // No need to reload worklist it won't be on available page anymore
            $scope.getWorklists();
          });
        }

        // unassign worklist
        $scope.unassignWorklist = function(worklist) {
          var editor;
          workflowService.performWorkflowAction($scope.selected.project.id, worklist.id,
            $scope.user.userName, $scope.selected.projectRole, 'UNASSIGN').then(
          // Success
          function(data) {
            // No need to reload worklist it won't be on assigned page anymore
            $scope.getWorklists();
          });
        };

        // Helper for removing a worklist/checklist
        $scope.removeWorklist = function(worklist) {

          if ($scope.type == 'Worklist') {
            workflowService.removeWorklist($scope.selected.project.id, worklist.id).then(
              function() {
                $scope.selected.worklist = null;
                $scope.getWorklists();
              });
          } else {
            workflowService.removeChecklist($scope.selected.project.id, worklist.id).then(
              function() {
                $scope.selected.worklist = null;
                $scope.getWorklists();
              });
          }
          // });
        };

        // return string of authors
        $scope.joinAuthors = function(worklist) {
          var joinedAuthors = '';
          var editors = new Array();
          if (worklist.authors && worklist.authors.length > 0) {
            editors = worklist.authors;
          }
          for (var i = 0; i < editors.length; i++) {
            joinedAuthors += editors[i];
            joinedAuthors += ' ';
          }
          return joinedAuthors;
        };

        // return string of reviewers
        $scope.joinReviewers = function(worklist) {
          var joinedReviewers = '';
          var editors = new Array();
          if (worklist.reviewers && worklist.reviewers.length > 0) {
            editors = worklist.reviewers;
          }
          for (var i = 0; i < editors.length; i++) {
            joinedReviewers += editors[i];
            joinedReviewers += ' ';
          }
          return joinedReviewers;
        };

        // Convert date to a string
        $scope.toDate = function(lastModified) {
          return utilService.toDate(lastModified);
        };

        // Table sorting mechanism
        $scope.setSortField = function(table, field, object) {
          utilService.setSortField(table, field, $scope.paging);

          // retrieve the correct table
          if (table === 'worklists') {
            $scope.getWorklists();
          }
          if (table === 'records') {
            $scope.getRecords();
          }
        };

        // Return up or down sort chars if sorted
        $scope.getSortIndicator = function(table, field) {
          return utilService.getSortIndicator(table, field, $scope.paging);
        };

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
          for (var i = 0; i < $scope.lists.concepts.length; i++) {
            if (!$scope.lists.concepts[i].id) {
              continue;
            }
            // ignore the websocket event from this.
            websocketService.incrementConceptIgnore($scope.lists.concepts[i].id);
            $scope.approveConcept($scope.lists.concepts[i]).then(
            // Success
            function(data) {
              $scope.getRecords();
            });
          }
          $scope.selectNextRecord($scope.selected.record);
        }

        // Moves to next record without approving selected record
        $scope.next = function() {
          $scope.selectNextRecord($scope.selected.record);
        }

        // adds an additional concept to list
        $scope.transferConceptToEditor = function(conceptId) {
          contentService.getConcept(conceptId, $scope.selected.project.id).then(
          // Success
          function(data) {
            // Only add if not already there
            for (var i = 0; i < $scope.lists.concepts.length; i++) {
              if (conceptId == $scope.lists.concepts[i].id) {
                return;
              }
            }
            $scope.lists.concepts.push(data);
            $scope.refreshWindows();
            $scope.lists.concepts.sort(utilService.sortBy('id'));
          });
        }

        // unselects options from all tables
        $scope.resetSelected = function() {
          $scope.selected.worklist = null;
          $scope.selected.record = null;
          $scope.selected.component = null;
        }

        // clears lists
        $scope.clearLists = function() {
          $scope.lists.records = [];
          $scope.lists.concepts = [];
          // This gets automatically overwritten by whatever is calling
          // clearLists
          // and setting it here interferes with paging
          // $scope.lists.worklists = [];
        }

        // Convert time to a string
        $scope.toTime = function(editingTime) {
          return utilService.toTime(editingTime);
        };

        $scope.performWorkflowAction = function(worklist, action) {
          workflowService.performWorkflowAction($scope.selected.project.id, worklist.id,
            $scope.user.userName, $scope.selected.projectRole, action).then(
          // Success
          function(data) {
            $scope.getWorklists();
            $scope.clearLists();
          },
          // Error
          function(data) {
            utilService.handleDialogError($scope.errors, data);
          });
        }

        // open semantic type editor window
        $scope.openStyWindow = function(width, height) {

          var newUrl = utilService.composeUrl('/edit/semantic-types');
          window.$windowScope = $scope;

          if (width == null && height == null && $scope.user.userPreferences.properties['semanticTypeWidth']) {
            width = $scope.user.userPreferences.properties['semanticTypeWidth'];
            height = $scope.user.userPreferences.properties['semanticTypeHeight'];
          } else if (!$scope.user.userPreferences.properties['semanticTypeWidth']){
            width = 600;
            height = 600;
          }
          $scope.windows['semanticType'] = $window.open(newUrl, 'styWindow',
            'width=' + width + ', height=' + height + ', scrollbars=yes');
          $scope.windows['semanticType'].document.title = 'Semantic Type Editor';
          $scope.windows['semanticType'].focus();
          if ($scope.user.userPreferences.properties['semanticTypeX']) {
            $scope.windows['semanticType'].moveTo($scope.user.userPreferences.properties['semanticTypeX'],
              $scope.user.userPreferences.properties['semanticTypeY']);
          }
          
          securityService.saveProperty($scope.user.userPreferences, 'semanticType', true);          
        };

        // open atoms editor window
        $scope.openAtomsWindow = function(width, height) {

          var newUrl = utilService.composeUrl('/edit/atoms');
          window.$windowScope = $scope;
          
          if (width == null && height == null && $scope.user.userPreferences.properties['atomWidth']) {
            width = $scope.user.userPreferences.properties['atomWidth'];
            height = $scope.user.userPreferences.properties['atomHeight'];
          } else if (!$scope.user.userPreferences.properties['atomWidth']){
            width = 600;
            height = 600;
          }
          $scope.windows['atom'] = $window.open(newUrl, 'atomWindow', 
            'width=' + width + ', height=' + height + ', scrollbars=yes');
          $scope.windows['atom'].document.title = 'Atoms Editor';
          $scope.windows['atom'].focus();
          if ($scope.user.userPreferences.properties['atomX']) {
            $scope.windows['atom'].moveTo($scope.user.userPreferences.properties['atomX'],
              $scope.user.userPreferences.properties['atomY']);
          }
          
          securityService.saveProperty($scope.user.userPreferences, 'atom', true);
        };

        // open relationships editor window
        $scope.openRelationshipsWindow = function(width, height) {

          var newUrl = utilService.composeUrl('/edit/relationships');
          window.$windowScope = $scope;
          if (width == null && height == null && $scope.user.userPreferences.properties['relationshipWidth']) {
            width = $scope.user.userPreferences.properties['relationshipWidth'];
            height = $scope.user.userPreferences.properties['relationshipHeight'];
          } else if (!$scope.user.userPreferences.properties['relationshipWidth']){
            width = 600;
            height = 600;
          }
          $scope.windows['relationship'] = $window.open(newUrl, 'relationshipWindow',
            'width=' + width + ', height=' + height + ', scrollbars=yes');
          $scope.windows['relationship'].document.title = 'Relationships Editor';
          $scope.windows['relationship'].focus();
          if ($scope.user.userPreferences.properties['relationshipX']) {
            $scope.windows['relationship'].moveTo($scope.user.userPreferences.properties['relationshipX'],
              $scope.user.userPreferences.properties['relationshipY']);
          }
          securityService.saveProperty($scope.user.userPreferences, 'relationship', true);
        };

        // open contexts window
        $scope.openContextsWindow = function(width, height) {

          var newUrl = utilService.composeUrl('contexts');
          window.$windowScope = $scope;

          if (width == null && height == null && $scope.user.userPreferences.properties['contextWidth']) {
            width = $scope.user.userPreferences.properties['contextWidth'];
            height = $scope.user.userPreferences.properties['contextHeight'];
          } else if (!$scope.user.userPreferences.properties['contextWidth']){
            width = 600;
            height = 600;
          }
          $scope.windows['context'] = $window.open(newUrl, 'contextWindow',
            'width=' + width + ', height=' + height + ', scrollbars=yes');
          $scope.windows['context'].document.title = 'Contexts';
          $scope.windows['context'].focus();
          if ($scope.user.userPreferences.properties['contextX']) {
            $scope.windows['context'].moveTo($scope.user.userPreferences.properties['contextX'],
              $scope.user.userPreferences.properties['contextY']);
          }
          
          securityService.saveProperty($scope.user.userPreferences, 'context', true);
        };

        // closes child windows when term server tab is closed
        $window.onbeforeunload = function(evt) {
          for ( var key in $scope.windows) {
            if ($scope.windows[key] && $scope.windows[key].$windowScope) {
                $scope.windows[key].$windowScope.parentClosing = true;
            	$scope.windows[key].close();
            }
          }
        }

        // closes child windows when edit tab is left
        $scope.$on('$destroy', function() {
          for ( var key in $scope.windows) {
            if ($scope.windows[key] && $scope.windows[key].$windowScope) {
                $scope.windows[key].$windowScope.parentClosing = true;
            	$scope.windows[key].close();
            }
          }
        });

        //
        // MODALS
        //

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
          });

        };

        // For callback from finder - needs to be not a $scope function
        function addFinderComponent(data) {
          // return if concept is already on concept list
          for (var i = 0; i < $scope.lists.concepts.length; i++) {
            if ($scope.lists.concepts[i].id == data.id) {
              window.alert('Concept ' + data.id + ' is already on the concept list.');
              return;
            }
          }
          // If full concept, simply push
          if (data.atoms && data.atoms.length > 0) {
            $scope.lists.concepts.push(data);
            $scope.refreshWindows();
            $scope.selectConcept(data);
            return;
          }

          // get full concept
          contentService.getConcept(data.id, $scope.selected.project.id).then(
          // Success
          function(data) {
            $scope.lists.concepts.push(data);
            $scope.refreshWindows();
            $scope.selectConcept(data);
          });
        }

        // Add time modal
        $scope.openFinishWorkflowModal = function(lworklist) {
          console.debug('openFinishWorkflowModal ', lworklist);

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/edit/finishWorkflow.html',
            controller : 'FinishWorkflowModalCtrl',
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
              worklist : function() {
                return lworklist;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(data) {
            $scope.getWorklists();
          });

        };

        // Move modal
        $scope.openMoveModal = function() {

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
          });
        };

        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {
          // configure tab
          securityService.saveTab($scope.user.userPreferences, '/edit');
          metadataService.getTerminologies().then(
            // Success
            function(data) {
              $scope.lists.terminologies = data.terminologies;
              $scope.getProjects();

              // reinitialize paging saved in user preferences
              if ($scope.user.userPreferences.properties['editWorklistPaging']) {
                var savedPaging = JSON
                  .parse($scope.user.userPreferences.properties['editWorklistPaging']);
                $scope.paging['worklists'].page = savedPaging.page;
              }
              if ($scope.user.userPreferences.properties['editRecordPaging']) {
                var savedPaging = JSON
                  .parse($scope.user.userPreferences.properties['editRecordPaging']);
                $scope.paging['records'].page = savedPaging.page;
              }
            });

        };

        //
        // Initialization: Check that application is configured
        //
        configureService.isConfigured().then(
        // Success
        function(isConfigured) {
          if (!isConfigured) {
            $location.path('/configure');
          } else {
            $scope.initialize();
          }
        });

        // end
      } ]);