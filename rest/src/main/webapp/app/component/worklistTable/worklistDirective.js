// Worklist Table directive
// e.g. <div worklist-table value='Worklist' />
tsApp
  .directive(
    'worklistTable',
    [
      '$uibModal',
      '$window',
      '$sce',
      '$interval',
      'utilService',
      'securityService',
      'projectService',
      'workflowService',
      'reportService',
      function($uibModal, $window, $sce, $interval, utilService, securityService, projectService,
        workflowService, reportService) {
        console.debug('configure worklistTable directive');
        return {
          restrict : 'A',
          scope : {
            // Legal 'value' settings include:
            // Worklist, Checklist
            value : '@',
            projects : '=',
            metadata : '=',
            stats : '=',
            project : '='
          },
          templateUrl : 'app/component/worklistTable/worklistTable.html',
          controller : [
            '$scope',
            function($scope) {

              // Variables
              $scope.user = securityService.getUser();
              $scope.userProjectsInfo = projectService.getUserProjectsInfo();
              $scope.selected = {
                worklist : null,
                record : null,
                concept : null
              };
              $scope.worklists = null;

              // Page metadata
              $scope.recordTypes = [ 'N', 'R' ];

              // Paging variables
              $scope.visibleSize = 4;
              $scope.pageSize = 10;
              $scope.paging = {};
              $scope.paging['worklist'] = {
                page : 1,
                filter : '',
                sortField : 'lastModified',
                ascending : null
              };
              $scope.paging['record'] = {
                page : 1,
                filter : '',
                typeFilter : '',
                sortField : 'clusterId',
                ascending : true
              };

              // Worklist Changed handler
              $scope.$on('workflow:worklistChanged', function(event, data) {
                console.debug('on workflow:worklistChanged', data);
                $scope.getWorklists();
              });

              // Project Changed Handler
              $scope.$on('worklist:projectChanged', function(event, data) {
                console.debug('on worklist:projectChanged', data);
                // Set project, refresh worklist list
                $scope.setProject(data);
              });

              // link to error handling
              function handleError(errors, error) {
                utilService.handleDialogError(errors, error);
              }

              $scope.joinEditors = function(worklist) {
                var joinedEditors = '';
                var editors = new Array();
                if (worklist.reviewers && worklist.reviewers.length > 0) {
                  editors = worklist.reviewers;
                } else if (worklist.authors && worklist.authors.length > 0) {
                  editors = worklist.authors;
                }
                for (var i = 0; i < editors.length; i++) {
                  joinedEditors += editors[i];
                  joinedEditors += ' ';
                }
                return joinedEditors;
              };

              // Set $scope.project and reload
              // $scope.worklists
              $scope.setProject = function(project) {
                $scope.project = project;
                $scope.getWorklists();
                projectService.findAssignedUsersForProject($scope.project.id, null, null).then(
                  function(data) {
                    $scope.users = data.users;
                    $scope.users.totalCount = data.totalCount;
                  });
              };

              // Get $scope.worklists
              $scope.getWorklists = function() {
                var pfs = {
                  startIndex : ($scope.paging['worklist'].page - 1) * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging['worklist'].sortField,
                  ascending : $scope.paging['worklist'].ascending == null ? true
                    : $scope.paging['worklist'].ascending,
                  queryRestriction : null
                };

                if ($scope.value == 'Worklist') {
                  pfs.queryRestriction = $scope.paging['worklist'].filter;
                  workflowService.findWorklists($scope.project.id, $scope.query, pfs).then(
                    function(data) {
                      $scope.worklists = data.worklists;
                      $scope.worklists.totalCount = data.totalCount;
                    });
                }
                if ($scope.value == 'Checklist') {
                  pfs.queryRestriction = $scope.paging['worklist'].filter;
                  workflowService.findChecklists($scope.project.id, $scope.query, pfs).then(
                    function(data) {
                      $scope.worklists = data.checklists;
                      $scope.worklists.totalCount = data.totalCount;
                    });
                }
              };

              // Get $scope.records
              $scope.getRecords = function(worklist) {

                var pfs = {
                  startIndex : ($scope.paging['record'].page - 1) * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging['record'].sortField,
                  ascending : $scope.paging['record'].ascending == null ? false
                    : $scope.paging['record'].ascending,
                  queryRestriction : $scope.paging['record'].filter != undefined
                    && $scope.paging['record'].filter != "" ? $scope.paging['record'].filter : null
                };

                if ($scope.paging['record'].typeFilter) {
                  var value = $scope.paging['record'].typeFilter;

                  // Handle inactive
                  if (value == 'N') {
                    if (pfs.queryRestriction != null)
                      pfs.queryRestriction += ' AND workflowStatus:NEEDS_REVIEW';
                    else
                      pfs.queryRestriction = 'workflowStatus:NEEDS_REVIEW';
                  } else if (value == 'R') {
                    if (pfs.queryRestriction != null)
                      pfs.queryRestriction += ' AND workflowStatus:READY_FOR_PUBLICATION';
                    else
                      pfs.queryRestriction = 'workflowStatus:READY_FOR_PUBLICATION';
                  }

                }

                if ($scope.value == 'Worklist') {

                  workflowService.findTrackingRecordsForWorklist(worklist.projectId, worklist.id,
                    pfs).then(
                  // Success
                  function(data) {
                    worklist.records = data.worklists;
                    worklist.records.totalCount = data.totalCount;
                  });
                } else if ($scope.value == 'Checklist') {
                  workflowService.findTrackingRecordsForChecklist(worklist.projectId, worklist.id,
                    pfs).then(
                  // Success
                  function(data) {
                    worklist.records = data.worklists;
                    worklist.records.totalCount = data.totalCount;
                  });
                }

              };

              // Convert time to a string
              $scope.toTime = function(editingTime) {
                return utilService.toTime(editingTime);
              };

              // Convert date to a string
              $scope.toDate = function(lastModified) {
                return utilService.toDate(lastModified);
              };

              // Selects a worklist (setting $scope.selected.worklist).
              // Looks up current release info and records.
              $scope.selectWorklist = function(worklist) {
                $scope.selected.worklist = worklist;
                $scope.selected.terminology = worklist.terminology;
                $scope.selected.version = worklist.version;
                $scope.selected.concept = null;
                $scope.getRecords(worklist);
              };

              // Selects a concept (setting $scope.selected.concept)
              $scope.selectConcept = function(concept) {
                // Set the concept for display
                $scope.selected.concept = {
                  terminologyId : concept.terminologyId,
                  terminology : concept.terminology,
                  version : concept.version,
                  id : concept.id
                };
                reportService.getConceptReport($scope.project.id, $scope.selected.concept.id).then(
                // Success
                function(data) {
                  $scope.selected.concept.report = data;
                });
              };

              $scope.unassignWorklist = function(worklist) {
                workflowService.performWorkflowAction($scope.project.id, worklist.id,
                  $scope.joinEditors(worklist).trim(),
                  $scope.project.userRoleMap[$scope.user.userName], 'UNASSIGN').then(
                // Success
                function(data) {
                  $scope.getWorklists();
                });
              };

              // Remove a worklist
              $scope.removeWorklist = function(worklist) {
                /*
                 * workflowService.findAllAssignedWorklists($scope.project.id, {
                 * startIndex : 0, maxResults : 1, queryRestriction :
                 * 'worklistId:' + worklist.id }).then( // Success
                 * function(data) { if (data.records.length > 0 && !$window
                 * .confirm('The worklist is assigned, are you sure you want to
                 * proceed?')) { return; }
                 */
                $scope.removeWorklistHelper($scope.project.id, worklist);
                // });
              };

              // Helper for removing a worklist/checklist
              $scope.removeWorklistHelper = function(projectId, worklist) {

                /*
                 * workflowService.findWorklistMembersForQuery(worklist.id, '', {
                 * startIndex : 0, maxResults : 1 }).then( function(data) { if
                 * (data.records.length == 1) { if (!$window .confirm('The
                 * worklist has records, are you sure you want to proceed.')) {
                 * return; } }
                 */
                if ($scope.value == 'Worklist') {
                  workflowService.removeWorklist(projectId, worklist.id).then(function() {
                    $scope.selected.worklist = null;
                    workflowService.fireWorklistChanged(worklist);
                  });
                } else {
                  workflowService.removeChecklist(projectId, worklist.id).then(function() {
                    $scope.selected.worklist = null;
                    workflowService.fireWorklistChanged(worklist);
                  });
                }
                // });
              };

              // Unassign worklist from user
              $scope.unassign = function(worklist, userName) {
                $scope.performWorkflowAction(worklist, 'UNASSIGN', userName);
              };

              // handle workflow advancement
              $scope.handleWorkflow = function(worklist) {
                if ($scope.value == 'ASSIGNED'
                  && worklist
                  && (worklist.workflowStatus == 'NEW' || worklist.workflowStatus == 'READY_FOR_PUBLICATION')) {
                  $scope.performWorkflowAction(worklist, 'SAVE', $scope.user.userName);
                } else {
                  workflowService.fireWorklistChanged(worklist);
                }
              };

              // Performs a workflow action
              $scope.performWorkflowAction = function(worklist, action, userName) {

                workflowService.performWorkflowAction($scope.project.id, worklist.id, userName,
                  $scope.projects.role, action).then(function(data) {
                  workflowService.fireWorklistChanged(data);
                });
              };

              // Get the most recent note for display
              $scope.getLatestNote = function(worklist) {
                if (worklist && worklist.notes && worklist.notes.length > 0) {
                  return $sce.trustAsHtml(worklist.notes.sort(utilService.sort_by('lastModified',
                    -1))[0].note);
                }
                return $sce.trustAsHtml('');
              };

              $scope.getWorklists();

              //
              // MODALS
              //

              // Notes modal
              $scope.openNotesModal = function(lobject) {
                console.debug('openNotesModal ', lobject);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/page/workflow/notes.html',
                  controller : NotesModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    object : function() {
                      return lobject;
                    },
                    value : function() {
                      return $scope.value;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });

              };



              // Assign worklist modal
              $scope.openAssignWorklistModal = function(lworklist, laction, lrole) {
                console.debug('openAssignWorklistModal ', lworklist, laction);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/page/workflow/assignWorklist.html',
                  controller : AssignWorklistModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    worklist : function() {
                      return lworklist;
                    },
                    action : function() {
                      return laction;
                    },
                    currentUser : function() {
                      return $scope.user;
                    },
                    project : function() {
                      return $scope.project;
                    }
                  }

                });

                modalInstance.result.then(
                // Success
                function(data) {
                  workflowService.fireWorklistChanged(data);
                });
              };

              // end

            } ]
        };
      } ]);
