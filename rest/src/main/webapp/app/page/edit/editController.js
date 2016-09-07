// Edit controller
tsApp
  .controller('EditCtrl',
    [
      '$scope',
      '$location',
      '$window',
      '$q',
      'tabService',
      'configureService',
      'securityService',
      'workflowService',
      'utilService',
      'websocketService',
      'configureService',
      'projectService',
      'metadataService',
      'reportService',
      'metaEditingService',
      'contentService',
      '$uibModal',
      function($scope, $location, $window, $q, tabService, configureService, securityService,
        workflowService, utilService, websocketService, configureService, projectService,
        metadataService, reportService, metaEditingService, contentService, $uibModal) {
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
        // Selected variables
        $scope.selected = {
          project : null,
          projectRole : null,
          worklist : null,
          record : null,
          concept : null,
          worklistMode : 'Assigned'
        };

        // Lists
        $scope.lists = {
          records : [],
          worklists : [],
          configs : [],
          projects : [],
          concepts : [],
          projectRoles : [],
          recordTypes : workflowService.getRecordTypes(),
          worklistModes : [ 'Available', 'Assigned', 'Checklists' ]
        }

        // Windows
        $scope.windows = {};

        // Paging variables
        $scope.paging = {};
        $scope.paging['worklists'] = {
          page : 1,
          pageSize : 10,
          filter : '',
          filterFields : null,
          sortField : null,
          sortAscending : true,
          sortOptions : []
        };// utilService.getPaging();
        $scope.paging['worklists'].sortField = 'lastModified';
        $scope.paging['worklists'].pageSize = 5;
        $scope.paging['worklists'].callback = {
          getPagedList : getWorklists
        };

        $scope.paging['records'] = {
          page : 1,
          pageSize : 10,
          filter : '',
          filterFields : null,
          sortField : null,
          sortAscending : true,
          sortOptions : []
        };// utilService.getPaging();
        $scope.paging['records'].sortField = 'clusterId';
        $scope.paging['records'].callback = {
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
        $scope.$on('termServer::conceptChange', function(event, concept) {

          // Refresh the selected concept
          if ($scope.selected.concept.id == concept.id) {
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
                contentService.getConcept(c.id, $scope.selected.project.id).then(
                // Success
                function(data) {
                  if (!data) {
                    $scope.removeConceptFromList(concept);
                  } else {
                    $scope.lists.concepts[i] = data;
                    $scope.selectConcept($scope.lists.concepts[0]);
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
          workflowService.findAvailableWorklists($scope.selected.project.id, $scope.user.userName,
            $scope.selected.projectRole, pfs).then(
          // Success
          function(data) {
            $scope.lists.worklists = data.worklists;
            $scope.lists.worklists.totalCount = data.totalCount
            $scope.availableCt = data.totalCount;
            $scope.resetSelected();
            $scope.getAssignedWorklistCt();
            $scope.getChecklistCt();
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

          workflowService.findAssignedWorklists($scope.selected.project.id, $scope.user.userName,
            $scope.selected.projectRole, pfs).then(
          // Success
          function(data) {
            $scope.lists.worklists = data.worklists;
            $scope.lists.worklists.totalCount = data.totalCount;
            $scope.assignedCt = data.totalCount;
            $scope.resetSelected();
            $scope.getAvailableWorklistCt();
            $scope.getChecklistCt();
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

          workflowService.findChecklists($scope.selected.project.id, '', pfs).then(
          // Success
          function(data) {
            $scope.lists.worklists = data.checklists;
            $scope.lists.worklists.totalCount = data.totalCount;
            $scope.checklistCt = data.totalCount;
            $scope.resetSelected();
            $scope.getAssignedWorklistCt();
            $scope.getAvailableWorklistCt();
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
          $scope.resetPaging();
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
            $scope.resetPaging();
            $scope.getWorklists();
          });

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
          $scope.selected.concept = null;
          if ($scope.value == 'Worklist') {
            $scope.parseStateHistory(worklist);
          }
          $scope.getRecords(worklist, true);
          // Set activity id
          $scope.selected.activityId = worklist.name;
        };

        // select record from 'Cluster' list
        $scope.selectRecord = function(record) {
          $scope.selected.record = record;

          // Don't push concepts on if in available modes
          if ($scope.worklistMode != 'Available') {
            $scope.getConcepts(record, true);
          }
        }

        // refresh the concept list
        $scope.getConcepts = function(record, selectFirst) {
          $scope.lists.concepts = [];
          for (var i = 0; i < record.concepts.length; i++) {
            contentService.getConcept(record.concepts[i].id, $scope.selected.project.id).then(
              function(data) {
                $scope.lists.concepts.push(data);
                $scope.lists.concepts.sort(utilService.sortBy('id'));
                // Select first, when the first concept is loaded
                if (selectFirst && data.id == record.concepts[0].id) {
                  $scope.selectConcept($scope.lists.concepts[0]);
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
            // TODO; somehow notify sty window that no more records are
            // available so msg can be displayed
            // BAC: recommend just setting a "warning"
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
          for ( var win in $scope.windows) {
            if ($scope.windows.hasOwnProperty(windowName)) {
              delete $scope.windows[windowName];
            }
          }
        }

        // select concept & get concept report
        $scope.selectConcept = function(concept) {
          $scope.selected.concept = concept;
          $scope.refreshWindows();
          reportService.getConceptReport($scope.selected.project.id, $scope.selected.concept.id)
            .then(
            // Success
            function(data) {
              $scope.selected.concept.report = data;
            });
          // Update the selected concept in the list
          for (var i = 0; i < $scope.lists.concepts.length; i++) {
            var concept = $scope.lists.concepts[i];
            if (concept.id == $scope.selected.concept.id) {
              $scope.lists.concepts[i] = $scope.selected.concept;
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
              if (pfs.queryRestriction != null)
                pfs.queryRestriction += ' AND workflowStatus:N*';
              else
                pfs.queryRestriction = 'workflowStatus:N*';
            } else if (value == 'R') {
              if (pfs.queryRestriction != null)
                pfs.queryRestriction += ' AND workflowStatus:R*';
              else
                pfs.queryRestriction = 'workflowStatus:R*';
            }
          }

          if ($scope.selected.worklistMode == 'Available'
            || $scope.selected.worklistMode == 'Assigned') {
            workflowService.findTrackingRecordsForWorklist($scope.selected.project.id,
              $scope.selected.worklist.id, pfs).then(
            // Success
            function(data) {
              $scope.lists.records = data.records;
              $scope.lists.records.totalCount = data.totalCount;
              if (selectFirst) {
                $scope.selectRecord($scope.lists.records[0]);
              }
            });
          } else if ($scope.selected.worklistMode == 'Checklists') {
            workflowService.findTrackingRecordsForChecklist($scope.selected.project.id,
              $scope.selected.worklist.id, pfs).then(
            // Success
            function(data) {
              $scope.lists.records = data.records;
              $scope.lists.records.totalCount = data.totalCount;
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
            function(data) {
              $scope.lists.concepts.push(data);
              $scope.lists.concepts.sort(utilService.sortBy('id'));
              // Select first, when the first concept is loaded
              if (selectFirst && data.id == record.concepts[0].id) {
                $scope.selectConcept($scope.lists.concepts[0]);
              }
            });
        }

        // unselects options from all tables
        $scope.resetSelected = function() {
          $scope.selected.worklist = null;
          $scope.selected.record = null;
          $scope.selected.concept = null;
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
        $scope.openStyWindow = function() {

          var newUrl = utilService.composeUrl('edit/semantic-types');
          window.$windowScope = $scope;

          $scope.windows['semanticType'] = $window.open(newUrl, 'styWindow',
            'width=600, height=600');
          $scope.windows['semanticType'].document.title = 'Semantic Type Editor';
          $scope.windows['semanticType'].focus();
        };
        
        // open atoms editor window
        $scope.openAtomsWindow = function() {

          var newUrl = utilService.composeUrl('edit/atoms');
          window.$windowScope = $scope;

          $scope.windows['atom'] = $window.open(newUrl, 'atomWindow',
            'width=1000, height=600');
          $scope.windows['atom'].document.title = 'Atoms Editor';
          $scope.windows['atom'].focus();
        };
        
        // open relationships editor window
        $scope.openRelationshipsWindow = function() {

          var newUrl = utilService.composeUrl('edit/relationships');
          window.$windowScope = $scope;

          $scope.windows['relationship'] = $window.open(newUrl, 'relationshipWindow',
            'width=1000, height=600');
          $scope.windows['relationship'].document.title = 'Relationships Editor';
          $scope.windows['relationship'].focus();
        };

        // open contexts window
        $scope.openContextsWindow = function() {

          var newUrl = utilService.composeUrl('contexts');
          window.$windowScope = $scope;

          $scope.windows['context'] = $window.open(newUrl, 'contextWindow',
            'width=1000, height=600');
          $scope.windows['context'].document.title = 'Contexts';
          $scope.windows['context'].focus();
        };

        // closes child windows when term server tab is closed
        $window.onbeforeunload = function(evt) {
          for ( var key in $scope.windows) {
            if ($scope.windows[key] && $scope.windows[key].$windowScope) {
              $scope.windows[key].close();
            }
          }
        }

        // closes child windows when edit tab is left
        $scope.$on('$destroy', function() {
          for ( var key in $scope.windows) {
            if ($scope.windows[key] && $scope.windows[key].$windowScope) {
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
            $scope.getRecords(false);
            $scope.getConcepts($scope.selected.record);
          });

        };

        // Add concept modal
        $scope.openFinderModal = function(lrecord) {
          console.debug('openFinderModal ', lrecord);
          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/edit/finder.html',
            controller : 'FinderModalCtrl',
            backdrop : 'static',
            size : 'lg',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              lists : function() {
                return $scope.lists;
              },
              user : function() {
                return $scope.user;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(data) {
            // return if concept is already on concept list
            for (var i = 0; i < $scope.lists.concepts.length; i++) {
              if ($scope.lists.concepts[i].id == data.id) {
                window.alert('Concept ' + data.id + ' is already on the concept list.');
                return;
              }
            }
            // get full concept
            contentService.getConcept(data.id, $scope.selected.project.id).then(
            // Success
            function(data) {
              $scope.lists.concepts.push(data);
              $scope.selectConcept(data);
            });
          });

        };
        
        

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
            $scope.getRecords(false);
            $scope.getConcepts($scope.selected.record, true);
          });
        };  
          
        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {
          // configure tab
          securityService.saveTab($scope.user.userPreferences, '/edit');
          $scope.getProjects();
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