// Worklist Table directive
// e.g. <div worklist-table value='PUBLISHED' />
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
      'validationService',
      function($uibModal, $window, $sce, $interval, utilService, securityService, projectService,
        workflowService, validationService) {
        console.debug('configure worklistTable directive');
        return {
          restrict : 'A',
          scope : {
            // Legal 'value' settings include
            // For directory tab: PUBLISHED, BETA
            // For worklist tab: AVAILABLE, ASSIGNED
            // RELEASE
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
                member : null,
                concept : null,
                terminology : null,
                version : null
              };
              $scope.worklistReleaseInfo = null;
              $scope.worklists = null;
              $scope.worklistLookupProgress = {};
              $scope.lookupInterval = null;
              //$scope.project = null;
              $scope.cancelling = null;
              $scope.showLatest = true;
              $scope.filters = [];
                           

              // Page metadata
              $scope.memberTypes = [ 'Member', 'Exclusion', 'Inclusion', 'Active', 'Retired' ];

              // Used for project admin to know what users are assigned to
              // something.
              $scope.worklistAuthorsMap = {};
              $scope.worklistReviewersMap = {};

              // Paging variables
              $scope.visibleSize = 4;
              $scope.pageSize = 10;
              $scope.paging = {};
              $scope.paging['worklist'] = {
                page : 1,
                filter : '',
                sortField : $scope.value == 'ASSIGNED' ? 'worklistName' : 'name',
                ascending : null
              };
              $scope.paging['member'] = {
                page : 1,
                filter : '',
                typeFilter : '',
                sortField : $scope.value == 'PUBLISHED' || $scope.value == 'BETA' ? 'conceptName'
                  : 'lastModified',
                ascending : true
              };


              $scope.ioImportHandlers = [];
              $scope.ioExportHandlers = [];

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

              // Set $scope.project and reload
              // $scope.worklists
              $scope.setProject = function(project) {
                $scope.project = project;
                $scope.getWorklists();
                $scope.getFilters();
                // $scope.projects.role already updated
              };

              // Get $scope.worklists
              // Logic for this depends on the $scope.value and
              // $scope.projects.role
              $scope.getWorklists = function() {
                var pfs = {
                  startIndex : ($scope.paging['worklist'].page - 1) * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging['worklist'].sortField,
                  ascending : $scope.paging['worklist'].ascending == null ? true
                    : $scope.paging['worklist'].ascending,
                  queryRestriction : null
                };

                if ($scope.value == 'PUBLISHED' || $scope.value == 'BETA') {
                  pfs.queryRestriction = 'workflowStatus:' + $scope.value;
                  workflowService.findWorklistsForQuery($scope.paging['worklist'].filter, pfs).then(
                    function(data) {
                      $scope.worklists = data.worklists;
                      $scope.worklists.totalCount = data.totalCount;
                      $scope.stats.count = $scope.worklists.totalCount;
                      $scope.reselect();
                    });
                }

                if ($scope.value == 'Worklist' /*&& $scope.projects.role == 'AUTHOR'*/) {
                  pfs.queryRestriction = $scope.paging['worklist'].filter;
                  workflowService.findWorklists($scope.project.id,
                    //$scope.user.userName, pfs).then(function(data) {
                    $scope.query, pfs).then(function(data) {
                    $scope.worklists = data.worklists;
                    $scope.worklists.totalCount = data.totalCount;
                    //$scope.stats.count = $scope.worklists.totalCount;
                    //$scope.reselect();
                  });
                }
               /* if ($scope.value == 'AVAILABLE' && $scope.projects.role == 'REVIEWER') {
                  pfs.queryRestriction = $scope.paging['worklist'].filter;
                  workflowService.findAvailableReviewWork($scope.project.id,
                    $scope.user.userName, pfs).then(function(data) {
                    $scope.worklists = data.worklists;
                    $scope.worklists.totalCount = data.totalCount;
                    $scope.stats.count = $scope.worklists.totalCount;
                    $scope.reselect();
                  });
                }
                if ($scope.value == 'AVAILABLE' && $scope.projects.role == 'ADMIN') {
                  pfs.queryRestriction = $scope.paging['worklist'].filter;
                  workflowService.findAllAvailableWorklists($scope.project.id, pfs).then(
                    function(data) {
                      $scope.worklists = data.worklists;
                      $scope.worklists.totalCount = data.totalCount;
                      $scope.stats.count = $scope.worklists.totalCount;
                      $scope.reselect();
                    });
                }
                if ($scope.value == 'ASSIGNED' && $scope.projects.role == 'ADMIN') {
                  pfs.queryRestriction = $scope.paging['worklist'].filter;
                  workflowService
                    .findAllAssignedWorklists($scope.project.id, pfs)
                    .then(
                      // Success
                      function(data) {
                        $scope.worklists = $scope.getWorklistsFromRecords(data.records);
                        $scope.worklists.totalCount = data.totalCount;
                        $scope.stats.count = $scope.worklists.totalCount;
                        // get worklist tracking records in order to get worklist
                        // authors
                        for (var i = 0; i < data.records.length; i++) {
                          if (data.records[i].authors.length > 0) {
                            $scope.worklistAuthorsMap[data.records[i].worklist.id] = data.records[i].authors;
                          }
                          if (data.records[i].reviewers.length > 0) {
                            $scope.worklistReviewersMap[data.records[i].worklist.id] = data.records[i].reviewers;
                          }
                        }
                        $scope.reselect();
                      });
                }
                if ($scope.value == 'ASSIGNED' && $scope.projects.role == 'AUTHOR') {
                  pfs.queryRestriction = $scope.paging['worklist'].filter;
                  workflowService.findAssignedEditingWorklists($scope.project.id,
                    $scope.user.userName, pfs).then(
                  // Success
                  function(data) {
                    $scope.worklists = $scope.getWorklistsFromRecords(data.records);
                    $scope.worklists.totalCount = data.totalCount;
                    $scope.stats.count = $scope.worklists.totalCount;
                    $scope.reselect();
                  });
                }
                if ($scope.value == 'ASSIGNED' && $scope.projects.role == 'REVIEWER') {
                  pfs.queryRestriction = $scope.paging['worklist'].filter;
                  workflowService.findAssignedReviewWorklists($scope.project.id,
                    $scope.user.userName, pfs).then(
                  // Success
                  function(data) {
                    $scope.worklists = $scope.getWorklistsFromRecords(data.records);
                    $scope.worklists.totalCount = data.totalCount;
                    $scope.stats.count = $scope.worklists.totalCount;
                    $scope.reselect();
                  });
                }
                if ($scope.value == 'RELEASE') {
                  pfs.queryRestriction = 'projectId:'
                    + $scope.project.id
                    + ' AND revision:false AND (workflowStatus:READY_FOR_PUBLICATION OR workflowStatus:BETA  OR workflowStatus:PUBLISHED)';
                  pfs.latestOnly = $scope.showLatest;
                  workflowService.findWorklistsForQuery($scope.paging['worklist'].filter, pfs).then(
                    function(data) {
                      $scope.worklists = data.worklists;
                      $scope.worklists.totalCount = data.totalCount;
                      $scope.stats.count = $scope.worklists.totalCount;
                      $scope.reselect();
                    });
                }
*/
              };
              // Convert an array of tracking records to an array of worklists.
              $scope.getWorklistsFromRecords = function(records) {
                var worklists = new Array();
                for (var i = 0; i < records.length; i++) {
                  worklists.push(records[i].worklist);
                }
                return worklists;
              };

              // Reselect selected worklist to refresh
              $scope.reselect = function() {
                // if there is a selection...
                // Bail if nothing selected
                if ($scope.selected.worklist) {
                  // If $scope.selected.worklist is in the list, select it, if not
                  // clear $scope.selected.worklist
                  var found = false;
                  if ($scope.selected.worklist) {
                    for (var i = 0; i < $scope.worklists.length; i++) {
                      if ($scope.selected.worklist.id == $scope.worklists[i].id) {
                        $scope.selectWorklist($scope.worklists[i]);
                        found = true;
                        break;
                      }
                    }
                  }
                  if (!found) {
                    $scope.selected.worklist = null;
                    $scope.selected.concept = null;
                  }
                }

                // If 'lookup in progress' is set, get progress
                for (var i = 0; i < $scope.worklists.length; i++) {
                  if ($scope.worklists[i].lookupInProgress) {
                    $scope.refreshLookupProgress($scope.worklists[i]);
                  }
                }
              };

              // Get $scope.filters
              $scope.getFilters = function() {
                var projectId = $scope.project ? $scope.project.id : null;
                var workflowStatus = null;
                if ($scope.value == 'PUBLISHED' || $scope.value == 'BETA') {
                  workflowStatus = $scope.value;
                }
                workflowService.getFilters(projectId, workflowStatus).then(
                // Success
                function(data) {
                  $scope.filters = data.keyValuePairs;
                });
              };

              // Get $scope.metadata.descriptionTypes
              $scope.getStandardDescriptionTypes = function(terminology, version) {
                projectService.getStandardDescriptionTypes(terminology, version).then(
                // Success
                function(data) {
                  // Populate 'selected' for worklistTable.html
                  // and metadata for addMember.html
                  $scope.selected.descriptionTypes = data.types;
                  $scope.metadata.descriptionTypes = data.types;
                });
              };

              // Get $scope.members
              $scope.getMembers = function(worklist) {

                var pfs = {
                  startIndex : ($scope.paging['member'].page - 1) * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging['member'].sortField,
                  ascending : $scope.paging['member'].ascending == null ? false
                    : $scope.paging['member'].ascending,
                  queryRestriction : null
                };

                if ($scope.paging['member'].typeFilter) {
                  var value = $scope.paging['member'].typeFilter;

                  // Handle inactive
                  if (value == 'Retired') {
                    pfs.queryRestriction = 'conceptActive:false';
                  } else if (value == 'Active') {
                    pfs.queryRestriction = 'conceptActive:true';
                  }

                  else {
                    // Handle member type

                    value = value.replace(' ', '_').toUpperCase();
                    pfs.queryRestriction = 'memberType:' + value;
                  }
                }

                workflowService.findWorklistMembersForQuery(worklist.id, $scope.paging['member'].filter,
                  pfs).then(
                // Success
                function(data) {
                  worklist.members = data.members;
                  worklist.members.totalCount = data.totalCount;
                });

              };


              // optimizes the definition
              $scope.optimizeDefinition = function(worklist) {
                workflowService.optimizeDefinition(worklist.id).then(function() {
                  workflowService.fireWorklistChanged(worklist);
                });
              };

              // Convert time to a string
              $scope.toTime = function(editingTime) {
                return utilService.toTime(editingTime);
              };
              
              // Convert date to a string
              $scope.toDate = function(lastModified) {
                return utilService.toDate(lastModified);
              };

              // Convert date to a string
              $scope.toSimpleDate = function(lastModified) {
                return utilService.toSimpleDate(lastModified);

              };

              // Convert date to a string
              $scope.toShortDate = function(lastModified) {
                return utilService.toShortDate(lastModified);

              };

              // Indicates whether we are in a directory page section
              var valueFlag = ($scope.value == 'PUBLISHED' || $scope.value == 'BETA');
              $scope.isDirectory = function() {
                return valueFlag;
              };

              // Return the name for a terminology
              $scope.getTerminologyName = function(terminology) {
                return $scope.metadata.terminologyNames[terminology];
              };

              // Get ordered definition clauses
              $scope.getOrderedDefinitionClauses = function() {
                if ($scope.selected.worklist && $scope.selected.worklist.definitionClauses) {
                  return $scope.selected.worklist.definitionClauses.sort(utilService
                    .sort_by('negated'));
                }
              };

              // Table sorting mechanism
              $scope.setSortField = function(table, field, object) {

                // handle 'ASSIGNED' vs 'AVAILABLE' fields
                // worklistTable.html expresses the fields in terms of available
                var lfield = field;
                if (table == 'worklist' && ($scope.value == 'ASSIGNED')) {
                  if (field == 'terminologyId') {
                    lfield = 'worklistId';
                  } else if (field == 'lastModified') {
                    lfield = 'lastModified';
                  } else if (field == 'workflowStatus') {
                    lfield = 'workflowStatus';
                  } else {
                    // uppercase and prepend worklist in all other cases
                    lfield = 'worklist' + field.charAt(0).toUpperCase() + field.slice(1);
                  }
                }

                utilService.setSortField(table, lfield, $scope.paging);
                // retrieve the correct table
                if (table === 'worklist') {
                  $scope.getWorklists();
                }
                if (table === 'member') {
                  $scope.getMembers(object);
                }
              };

              // Return up or down sort chars if sorted
              $scope.getSortIndicator = function(table, field) {
                var lfield = field;
                if (table == 'worklist' && ($scope.value == 'ASSIGNED')) {
                  if (field == 'terminologyId') {
                    lfield = 'worklistId';
                  } else if (field == 'lastModified') {
                    lfield = 'lastModified';
                  } else if (field == 'workflowStatus') {
                    lfield = 'workflowStatus';
                  } else {
                    // uppercase and prepend worklist in all other cases
                    lfield = 'worklist' + field.charAt(0).toUpperCase() + field.slice(1);
                  }
                }
                return utilService.getSortIndicator(table, lfield, $scope.paging);
              };

              // Selects a worklist (setting $scope.selected.worklist).
              // Looks up current release info and members.
              $scope.selectWorklist = function(worklist) {
                $scope.selected.worklist = worklist;
                $scope.selected.terminology = worklist.terminology;
                $scope.selected.version = worklist.version;
                $scope.getMembers(worklist);
                $scope.getStandardDescriptionTypes(worklist.terminology, worklist.version);
              };

              // Selects a member (setting $scope.selected.member)
              $scope.selectMember = function(member) {
                $scope.selected.member = member;
                // Set the concept for display in concept-info
                $scope.selected.concept = {
                  terminologyId : member.conceptId,
                  terminology : member.terminology,
                  version : member.version
                };

              };

              // Member type style
              $scope.getMemberStyle = function(member) {
                if (member.memberType == 'MEMBER') {
                  return '';
                }
                return member.memberType.replace('_STAGED', '');
              };

              // Remove a worklist
              $scope.removeWorklist = function(worklist) {
               /* workflowService.findAllAssignedWorklists($scope.project.id, {
                  startIndex : 0,
                  maxResults : 1,
                  queryRestriction : 'worklistId:' + worklist.id
                }).then(
                  // Success
                  function(data) {
                    if (data.records.length > 0
                      && !$window
                        .confirm('The worklist is assigned, are you sure you want to proceed?')) {
                      return;
                    }*/
                    $scope.removeWorklistHelper($scope.project.id, worklist);
                  //});
              };

              // Helper for removing a refest
              $scope.removeWorklistHelper = function(projectId, worklist) {

                /*workflowService.findWorklistMembersForQuery(worklist.id, '', {
                  startIndex : 0,
                  maxResults : 1
                }).then(
                  function(data) {
                    if (data.members.length == 1) {
                      if (!$window
                        .confirm('The worklist has members, are you sure you want to proceed.')) {
                        return;
                      }
                    }*/
                    workflowService.removeWorklist(projectId, worklist.id).then(function() {
                      $scope.selected.worklist = null;
                      workflowService.fireWorklistChanged(worklist);
                    });
                  //});
              };

              // Remove worklist member
              $scope.removeWorklistMember = function(worklist, member) {

                workflowService.removeWorklistMember(member.id).then(
                // Success
                function() {
                  $scope.selected.concept = null;
                  $scope.handleWorkflow(worklist);
                });
              };
              // Remove worklist inclusion
              $scope.removeWorklistInclusion = function(worklist, member) {

                workflowService.removeWorklistMember(member.id).then(
                // Success
                function() {
                  $scope.handleWorkflow(worklist);
                });
              };

              // Adds a worklist exclusion and refreshes member
              // list with current PFS settings
              $scope.addWorklistExclusion = function(worklist, member) {
                workflowService.addWorklistExclusion(worklist, member.conceptId, false).then(function() {
                  $scope.handleWorkflow(worklist);
                });

              };

              // Remove worklist exclusion and refreshes members
              $scope.removeWorklistExclusion = function(worklist, member) {
                workflowService.removeWorklistExclusion(member.id).then(function() {
                  $scope.handleWorkflow(worklist);
                });

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

              // Removes all worklist members
              $scope.removeAllWorklistMembers = function(worklist) {
                workflowService.removeAllWorklistMembers(worklist.id).then(function(data) {
                  workflowService.fireWorklistChanged(worklist);
                });
              };

              // Exports a release artifact (and begins the
              // download)
              $scope.exportReleaseArtifact = function(artifact) {
                releaseService.exportReleaseArtifact(artifact);
              };

              // Directive scoped method for cancelling an import/migration
              $scope.cancelAction = function(worklist) {
                $scope.cancelling = true;
                if (worklist.stagingType == 'IMPORT') {
                  workflowService.cancelImportMembers(worklist.id).then(
                  // Success
                  function() {
                    $scope.cancelling = false;
                    workflowService.fireWorklistChanged(worklist);
                  },
                  // Error
                  function() {
                    $scope.cancelling = false;
                  });
                }
                if (worklist.stagingType == 'MIGRATION') {

                  workflowService.cancelMigration(worklist.id).then(
                  // Success
                  function(data) {
                    // Some local management of worklist state to avoid
                    // a million callbacks to the server while
                    // startLookup is running
                    worklist.staged = false;
                    worklist.stagingType = null;
                    // If INTENSIONAL, we need to re-look up old/not/new members
                    if (worklist.type == 'INTENSIONAL') {
                      worklist.lookupInProgress = true;
                      startLookup(worklist);
                    }
                    $scope.cancelling = false;
                    // workflowService.fireWorklistChanged($scope.worklist);
                  },
                  // Error
                  function() {
                    $scope.cancelling = false;
                  });

                }
                if (worklist.stagingType == 'BETA') {
                  releaseService.cancelWorklistRelease(worklist.id).then(
                  // Success
                  function() {
                    $scope.cancelling = false;
                    workflowService.fireWorklistChanged(worklist);
                  },
                  // Error
                  function() {
                    $scope.cancelling = false;
                  });
                }
              };

              // cancelling a release given the staged worklist
              $scope.cancelActionForStaged = function(worklist) {
                if (worklist.workflowStatus == 'BETA') {
                  workflowService.getOriginForStagedWorklistId(worklist.id).then(
                  // Success
                  function(data) {
                    workflowService.getWorklist(data).then(
                    // Success
                    function(data) {
                      $scope.cancelAction(data);
                    });
                  });
                }
              };

              // Start lookup again - not $scope because modal must access it
              function startLookup(worklist) {
                workflowService.startLookup(worklist.id).then(
                // Success
                function(data) {
                  $scope.worklistLookupProgress[worklist.id] = 1;
                  // Start if not already running
                  if (!$scope.lookupInterval) {
                    $scope.lookupInterval = $interval(function() {
                      $scope.refreshLookupProgress(worklist);
                    }, 2000);
                  }
                });
              }

              // Refresh lookup progress
              $scope.refreshLookupProgress = function(worklist) {
                workflowService.getLookupProgress(worklist.id).then(
                // Success
                function(data) {
                  if (data === "100" || data == 100) {
                    worklist.lookupInProgress = false;
                  }
                  $scope.worklistLookupProgress[worklist.id] = data;
                  // If all lookups in progress are at 100%, stop interval
                  var found = true;
                  for ( var key in $scope.worklistLookupProgress) {
                    if ($scope.worklistLookupProgress[key] < 100) {
                      found = false;
                      break;
                    }
                  }
                  if (found) {
                    $interval.cancel($scope.lookupInterval);
                    $scope.lookupInterval = null;
                  }

                },
                // Error
                function(data) {
                  // Cancel automated lookup on error
                  $interval.cancel($scope.lookupInterval);
                });
              };
              // Get the most recent note for display
              $scope.getLatestNote = function(worklist) {
                if (worklist && worklist.notes && worklist.notes.length > 0) {
                  return $sce.trustAsHtml(worklist.notes
                    .sort(utilService.sort_by('lastModified', -1))[0].value);
                }
                return $sce.trustAsHtml('');
              };

              // Initialize if project setting isn't used
              if ($scope.value == 'BETA' || $scope.value == 'PUBLISHED') {
                $scope.getWorklists();
              }

              //$scope.getFilters();
              $scope.getWorklists();

              //
              // MODALS
              //

              // Definition clauses modal
              $scope.openDefinitionClausesModal = function(lworklist, lvalue) {
                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/definitionClauses.html',
                  controller : DefinitionClausesModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    worklist : function() {
                      return lworklist;
                    },
                    value : function() {
                      return lvalue;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });

              };

              // Definition clauses controller
              var DefinitionClausesModalCtrl = function($scope, $uibModalInstance, worklist, value) {
                console.debug('Entered definition clauses modal control', worklist, value);

                $scope.worklist = worklist;
                $scope.value = value;
                $scope.newClause = null;

                // Paging parameters
                $scope.newClauses = angular.copy($scope.worklist.definitionClauses);
                $scope.pageSize = 5;
                $scope.pagedClauses = [];
                $scope.paging = {};
                $scope.paging['clauses'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.warnings = {};
                $scope.warningFlag = false;
                $scope.errors = [];

                // Indicate whether a clause is in a warning condition
                $scope.isWarning = function(value) {
                  return $scope.warnings[value];
                };

                // Get paged clauses (assume all are loaded)
                $scope.getPagedClauses = function() {
                  $scope.pagedClauses = utilService.getPagedArray($scope.newClauses
                    .sort(utilService.sort_by('negated')), $scope.paging['clauses'],
                    $scope.pageSize);
                };

                // identify whether defintion has changed
                $scope.isDefinitionDirty = function() {
                  if ($scope.newClauses.length != $scope.worklist.definitionClauses.length) {
                    return true;
                  }

                  // Compare scope.worklist.definitionClauses to newClauses
                  for (var i = 0; i < $scope.newClauses.length; i++) {
                    if ($scope.newClauses[i].value != $scope.worklist.definitionClauses[i].value) {
                      return true;
                    }
                    if ($scope.newClauses[i].negated != $scope.worklist.definitionClauses[i].negated) {
                      return true;
                    }
                  }
                  return false;
                };

                // remove clause
                $scope.removeClause = function(worklist, clause) {
                  for (var i = 0; i < $scope.newClauses.length; i++) {
                    var index = $scope.newClauses.indexOf(clause);
                    if (index != -1) {
                      $scope.newClauses.splice(index, 1);
                    }
                  }
                  $scope.getPagedClauses();

                  // reset the warnings based on remaining clauses
                  if ($scope.warnings.length > 0) {
                    $scope.warnings = {};
                    $scope.warningFlag = false;
                    for (var i = 0; i < $scope.newClauses.length; i++) {
                      workflowService
                        .countExpression($scope.newClauses[i].value, worklist.terminology,
                          worklist.version)
                        .then(
                          // Success - count expression
                          function(data) {
                            var count = data;
                            if (count >= 20000) {
                              $scope.warnings[$scope.newClauses[i].value] = 'Definition clause resolves to '
                                + count + ' members.';
                              $scope.warningFlag = true;
                            }
                          },
                          // Error - count expression
                          function(data) {
                            handleError($scope.errors, data);
                          });
                    }
                  }
                };

                // add new clause
                $scope.addClause = function(worklist, clause) {
                  $scope.errors = [];

                  // Confirm clauses are unique, skip if not
                  for (var i = 0; i < $scope.newClauses.length; i++) {
                    if ($scope.newClauses[i].value == clause.value) {
                      $scope.errors[0] = 'Duplicate definition clause';
                      return;
                    }
                    if ($scope.newClauses[i].value.indexOf("MINUS") != -1) {
                      $scope.errors[0] = 'Definition clause may not contain MINUS';
                      return;
                    }
                    if ($scope.newClauses[i].value.indexOf(" OR ") != -1) {
                      $scope.errors[0] = 'Definition clause may not contain OR';
                      return;
                    }
                  }
                  workflowService
                    .isExpressionValid(clause.value, worklist.terminology, worklist.version)
                    .then(
                      // Success - add worklist
                      function(data) {
                        if (data == 'true') {
                          $scope.newClauses.push(clause);
                          $scope.getPagedClauses();
                          $scope.newClause = null;
                          $scope.warnings = {};
                          $scope.warningFlag = false;
                          workflowService
                            .countExpression(clause.value, worklist.terminology, worklist.version)
                            .then(
                              // Success - count expression
                              function(data) {
                                var count = data;
                                if (count >= 20000) {
                                  $scope.warnings[$scope.newClauses[i].value] = 'Definition clause resolves to '
                                    + count + ' members.';
                                  $scope.warningFlag = true;
                                }
                              },
                              // Error - count expression
                              function(data) {
                                handleError($scope.errors, data);
                              });
                        } else {
                          $scope.errors[0] = 'Submitted definition clause is invalid';
                          return;
                        }
                      },
                      // Error - add worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });
                };

                // Save worklist
                $scope.save = function(worklist) {
                  worklist.definitionClauses = $scope.newClauses;
                  $scope.warnings = [];
                  workflowService.updateWorklist(worklist).then(
                  // Success - add worklist
                  function(data) {
                    $uibModalInstance.close(worklist);
                  },
                  // Error - add worklist
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

                // initialize modal
                $scope.getPagedClauses();
              };

              // Notes modal
              $scope.openNotesModal = function(lobject, ltype) {
                console.debug('openNotesModal ', lobject, ltype);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/notes.html',
                  controller : NotesModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    object : function() {
                      return lobject;
                    },
                    type : function() {
                      return ltype;
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

              // Notes controller
              var NotesModalCtrl = function($scope, $uibModalInstance, $sce, object, type,
                tinymceOptions) {
                console.debug('Entered notes modal control', object, type);
                $scope.object = object;
                $scope.type = type;
                $scope.tinymceOptions = tinymceOptions;
                $scope.newNote = null;

                // Paging parameters
                $scope.pageSize = 5;
                $scope.pagedNotes = [];
                $scope.paging = {};
                $scope.paging['notes'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.errors = [];

                // Get paged notes (assume all are loaded)
                $scope.getPagedNotes = function() {
                  $scope.pagedNotes = utilService.getPagedArray($scope.object.notes,
                    $scope.paging['notes'], $scope.pageSize);
                };

                $scope.getNoteValue = function(note) {
                  return $sce.trustAsHtml(note.value);
                };

                // remove note
                $scope.removeNote = function(object, note) {

                  if ($scope.type == 'Worklist') {
                    workflowService.removeWorklistNote(object.id, note.id).then(
                    // Success - add worklist
                    function(data) {
                      $scope.newNote = null;
                      workflowService.getWorklist(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add worklist
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  } else if ($scope.type == 'Member') {
                    workflowService.removeWorklistMemberNote(object.id, note.id).then(
                    // Success - add worklist
                    function(data) {
                      $scope.newNote = null;
                      workflowService.getMember(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add worklist
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                // add new note
                $scope.submitNote = function(object, text) {

                  if ($scope.type == 'Worklist') {
                    workflowService.addWorklistNote(object.id, text).then(
                    // Success - add worklist
                    function(data) {
                      $scope.newNote = null;
                      workflowService.getWorklist(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add worklist
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  } else if ($scope.type == 'Member') {
                    workflowService.addWorklistMemberNote(object.worklistId, object.id, text).then(
                    // Success - add worklist
                    function(data) {
                      $scope.newNote = null;

                      workflowService.getMember(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add worklist
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                // Convert date to a string
                $scope.toDate = function(lastModified) {
                  return utilService.toDate(lastModified);
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

                // initialize modal
                $scope.getPagedNotes();
              };

              // Clone Worklist modal
              $scope.openCloneWorklistModal = function(lworklist) {
                console.debug('cloneWorklistModal ', lworklist);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/editWorklist.html',
                  controller : CloneWorklistModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    worklist : function() {
                      return lworklist;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    filters : function() {
                      return $scope.filters;
                    },
                    projects : function() {
                      return $scope.projects;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });

              };

              // Clone Worklist controller
              var CloneWorklistModalCtrl = function($scope, $uibModalInstance, worklist, filters,
                metadata, projects) {
                console.debug('Entered clone worklist modal control', worklist, projects);

                $scope.action = 'Clone';
                $scope.projects = projects;
                $scope.metadata = metadata;
                $scope.filters = filters;
                $scope.versions = metadata.versions[worklist.terminology].sort().reverse();
                // Copy worklist and clear terminology id
                $scope.worklist = JSON.parse(JSON.stringify(worklist));
                $scope.worklist.terminologyId = null;
                $scope.modules = [];
                $scope.errors = [];

                // Handler for project change
                $scope.projectSelected = function(project) {
                  $scope.worklist.namespace = project.namespace;
                  $scope.worklist.moduleId = project.moduleId;
                };

                // Get $scope.modules
                $scope.getModules = function() {
                  projectService.getModules($scope.worklist.terminology, $scope.worklist.version).then(
                  // Success
                  function(data) {
                    $scope.modules = data.concepts;
                  });
                };

                // Initialize modules if terminology/version set
                if ($scope.worklist.terminology && $scope.worklist.version) {
                  $scope.getModules();
                }

                // Handle terminology selected
                $scope.terminologySelected = function(terminology) {
                  $scope.versions = $scope.metadata.versions[terminology].sort().reverse();
                  $scope.getModules();
                };

                // Handle version selected
                $scope.versionSelected = function(version) {
                  $scope.getModules();
                };

                // Assign worklist id
                $scope.assignWorklistTerminologyId = function(worklist) {
                  workflowService.assignWorklistTerminologyId(worklist.projectId, worklist).then(
                  // success
                  function(data) {
                    worklist.terminologyId = data;
                  },
                  // error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Submit worklist
                $scope.submitWorklist = function(worklist) {

                  if (!worklist.project) {
                    $scope.errors[0] = 'A project must be chosen from the picklist.';
                    return;
                  }
                  // validate worklist before cloning it
                  validationService.validateWorklist(worklist).then(
                    function(data) {

                      // If there are errors, make them available and stop.
                      if (data.errors && data.errors.length > 0) {
                        $scope.errors = data.errors;
                        return;
                      } else {
                        $scope.errors = [];
                      }

                      // if $scope.warnings is empty, and data.warnings is not,
                      // show warnings and stop
                      if (data.warnings && data.warnings.length > 0
                        && $scope.warnings.join() !== data.warnings.join()) {
                        $scope.warnings = data.warnings;
                        return;
                      } else {
                        $scope.warnings = [];
                      }

                      workflowService.cloneWorklist(worklist.project.id, worklist).then(
                      // Success - clone worklist
                      function(data) {
                        var newWorklist = data;
                        $uibModalInstance.close(newWorklist);
                      },
                      // Error - clone worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - validate
                    function(data) {
                      handleError($scope.errors, data);
                    });
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Import/Export modal
              $scope.openImportExportModal = function(lworklist, loperation, ltype) {
                console.debug('exportModal ', lworklist);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/importExport.html',
                  controller : ImportExportModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    worklist : function() {
                      return lworklist;
                    },
                    operation : function() {
                      return loperation;
                    },
                    type : function() {
                      return ltype;
                    },
                    ioHandlers : function() {
                      if (loperation == 'Import') {
                        return $scope.metadata.importHandlers;
                      } else {
                        return $scope.metadata.exportHandlers;
                      }
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  if (loperation == 'Import') {
                    $scope.handleWorkflow(data);
                  } else {
                    workflowService.fireWorklistChanged(data);
                  }
                });
              };

              // Import/Export controller
              var ImportExportModalCtrl = function($scope, $uibModalInstance, worklist, operation,
                type, ioHandlers) {
                console.debug('Entered import export modal control', worklist.id, ioHandlers,
                  operation, type);

                $scope.worklist = worklist;
                $scope.ioHandlers = [];

                // Skip "with name" handlers if user is not logged in
                // IHTSDO-specific, may be able make this more data driven
                for (var i = 0; i < ioHandlers.length; i++) {
                  if (!securityService.isLoggedIn()
                    && ioHandlers[i].name.toLowerCase().indexOf('with name') != -1) {
                    continue;
                  }
                  $scope.ioHandlers.push(ioHandlers[i]);
                }

                $scope.selectedIoHandler = null;
                for (var i = 0; i < ioHandlers.length; i++) {

                  // Choose first one if only one
                  if ($scope.selectedIoHandler == null) {
                    $scope.selectedIoHandler = ioHandlers[i];
                  }
                  // choose 'rf2' as default otherwise
                  // IHTSDO-specific, may be able make this more data driven
                  if (ioHandlers[i].name.endsWith('RF2')) {
                    $scope.selectedIoHandler = ioHandlers[i];
                  }
                }
                $scope.type = type;
                $scope.operation = operation;
                $scope.comments = [];
                $scope.warnings = [];
                $scope.errors = [];
                $scope.importStarted = false;
                $scope.importFinished = false;

                // Handle export
                $scope.export = function(file) {
                  if (type == 'Definition') {
                    workflowService.exportDefinition($scope.worklist, $scope.selectedIoHandler.id,
                      $scope.selectedIoHandler.fileTypeFilter);
                  }
                  if (type == 'Worklist Members') {
                    workflowService.exportMembers($scope.worklist, $scope.selectedIoHandler.id,
                      $scope.selectedIoHandler.fileTypeFilter);
                  }
                  $uibModalInstance.close(worklist);
                };

                // Handle import
                $scope.import = function(file) {

                  if (type == 'Definition') {
                    workflowService.importDefinition($scope.worklist.id, $scope.selectedIoHandler.id,
                      file).then(
                    // Success - close dialog
                    function(data) {
                      $uibModalInstance.close(worklist);
                    },
                    // Failure - show error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }

                  if (type == 'Worklist Members') {
                    workflowService.beginImportMembers($scope.worklist.id, $scope.selectedIoHandler.id)
                      .then(

                        // Success
                        function(data) {
                          $scope.importStarted = true;
                          // data is a validation result, check for errors
                          if (data.errors.length > 0) {
                            $scope.errors = data.errors;
                          } else {

                            // If there are no errors, finish import
                            workflowService.finishImportMembers($scope.worklist.id,
                              $scope.selectedIoHandler.id, file).then(
                            // Success - close dialog
                            function(data) {
                              $scope.importFinished = true;
                              $scope.comments = data.comments;
                              $scope.warnings = data.warnings;
                              $scope.errors = data.errors;
                              startLookup(worklist);
                            },
                            // Failure - show error
                            function(data) {
                              handleError($scope.errors, data);
                            });
                          }
                        },

                        // Failure - show error, clear global error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                  }
                };

                // Handle continue import
                $scope.continueImport = function(file) {

                  if (type == 'Worklist Members') {
                    workflowService.finishImportMembers($scope.worklist.id,
                      $scope.selectedIoHandler.id, file).then(
                    // Success - close dialog
                    function(data) {
                      $scope.importFinished = true;
                      $scope.comments = data.comments;
                      $scope.warnings = data.warnings;
                      $scope.errors = data.errors;
                      startLookup(worklist);
                    },
                    // Failure - show error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                // Dismiss modal
                $scope.cancel = function() {
                  // If there are lingering errors, cancel the import
                  if ($scope.errors.length > 0 && type == 'Worklist Members') {
                    workflowService.cancelImportMembers($scope.worklist.id);
                  }
                  // dismiss the dialog
                  $uibModalInstance.dismiss('cancel');
                };

                $scope.close = function() {
                  // close the dialog and reload worklists
                  $uibModalInstance.close(worklist);
                };
              };

              // Release Process modal
              $scope.openReleaseProcessModal = function(lworklist) {
                console.debug('releaseProcessModal ', lworklist);
                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/release.html',
                  controller : ReleaseProcessModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    worklist : function() {
                      return lworklist;
                    },
                    ioHandlers : function() {
                      return $scope.metadata.exportHandlers;
                    },
                    utilService : function() {
                      return utilService;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  workflowService.fireWorklistChanged(data);
                });
              };

              // Open release process modal given staged worklist
              $scope.openReleaseProcessModalForStaged = function(worklist) {

                workflowService.getOriginForStagedWorklistId(worklist.id).then(
                // Success
                function(data) {
                  workflowService.getWorklist(data).then(
                  // Success
                  function(data) {
                    $scope.openReleaseProcessModal(data);
                  });
                });
              };

              // Release Process controller
              var ReleaseProcessModalCtrl = function($scope, $uibModalInstance, worklist, ioHandlers,
                utilService) {
                console.debug('Entered release process modal', worklist.id, ioHandlers);

                $scope.worklist = worklist;
                $scope.ioHandlers = ioHandlers;
                $scope.selectedIoHandler = $scope.ioHandlers[0];
                $scope.releaseInfo = [];
                $scope.validationResult = null;
                $scope.format = 'yyyyMMdd';
                $scope.releaseDate = utilService.toSimpleDate($scope.worklist.effectiveTime);
                $scope.status = {
                  opened : false
                };
                $scope.errors = [];

                if (worklist.stagingType == 'BETA') {
                  releaseService.resumeRelease(worklist.id).then(
                  // Success
                  function(data) {
                    $scope.stagedWorklist = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                }

                // Begin release
                $scope.beginWorklistRelease = function(worklist) {

                  releaseService.beginWorklistRelease(worklist.id,
                    utilService.toSimpleDate(worklist.effectiveTime)).then(
                  // Success
                  function(data) {
                    $scope.releaseInfo = data;
                    $scope.worklist.inPublicationProcess = true;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // Validate release
                $scope.validateWorklistRelease = function(worklist) {

                  releaseService.validateWorklistRelease(worklist.id).then(
                  // Success
                  function(data) {
                    $scope.validationResult = data;
                    workflowService.fireWorklistChanged(worklist);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Initiate Beta
                $scope.betaWorklistRelease = function(worklist) {
                  // clear validation result
                  $scope.validationResult = null;
                  releaseService.betaWorklistRelease(worklist.id, $scope.selectedIoHandler.id).then(
                  // Success
                  function(data) {
                    $scope.stagedWorklist = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Finish the release
                $scope.finishWorklistRelease = function(worklist) {

                  releaseService.finishWorklistRelease(worklist.id, $scope.selectedIoHandler.id).then(
                  // Success
                  function(data) {
                    $uibModalInstance.close(worklist);
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Cancel release process and dismiss modal
                $scope.cancel = function() {
                  releaseService.cancelWorklistRelease($scope.worklist.id).then(
                  // Success
                  function(data) {
                    $uibModalInstance.close($scope.worklist);
                  });
                };

                // Close the window - to return later
                $scope.close = function() {
                  $uibModalInstance.close($scope.worklist);
                };

                $scope.open = function($event) {
                  $scope.status.opened = true;
                };

                $scope.format = 'yyyyMMdd';
              };

              // Assign worklist modal
              $scope.openAssignWorklistModal = function(lworklist, laction, lrole) {
                console.debug('openAssignWorklistModal ', lworklist, laction);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/assignWorklist.html',
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
                    assignedUsers : function() {
                      return $scope.projects.assignedUsers;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    role : function() {
                      if (lrole) {
                        return lrole;
                      } else {
                        return $scope.projects.role;
                      }
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    }
                  }

                });

                modalInstance.result.then(
                // Success
                function(data) {
                  workflowService.fireWorklistChanged(data);
                });
              };

              // Assign worklist controller
              var AssignWorklistModalCtrl = function($scope, $uibModalInstance, $sce, worklist, action,
                currentUser, assignedUsers, project, role, tinymceOptions) {
                console.debug('Entered assign worklist modal control', assignedUsers, project.id);
                $scope.worklist = worklist;
                $scope.action = action;
                $scope.project = project;
                $scope.role = role;
                $scope.tinymceOptions = tinymceOptions;
                $scope.assignedUsers = [];
                $scope.user = utilService.findBy(assignedUsers, currentUser, 'userName');
                $scope.note;
                $scope.errors = [];

                // Sort users by name and role restricts
                var sortedUsers = assignedUsers.sort(utilService.sort_by('name'));
                for (var i = 0; i < sortedUsers.length; i++) {
                  if ($scope.role == 'AUTHOR'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'REVIEWER'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'ADMIN') {
                    $scope.assignedUsers.push(sortedUsers[i]);
                  }
                }

                // Assign (or reassign)
                $scope.assignWorklist = function() {
                  if (!$scope.user) {
                    $scope.errors[0] = 'The user must be selected. ';
                    return;
                  }

                  if (action == 'ASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, worklist.id,
                      $scope.user.userName, $scope.role, 'ASSIGN').then(
                    // Success
                    function(data) {

                      // Add a note as well
                      if ($scope.note) {
                        workflowService.addWorklistNote(worklist.id, $scope.note).then(
                        // Success
                        function(data) {
                          $uibModalInstance.close(worklist);
                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      }
                      // close dialog if no note
                      else {
                        $uibModalInstance.close(worklist);
                      }

                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }

                  // else reassign
                  else if (action == 'REASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, worklist.id,
                      $scope.user.userName, 'AUTHOR', 'REASSIGN').then(
                    // success - reassign
                    function(data) {
                      // Add a note as well
                      if ($scope.note) {
                        workflowService.addWorklistNote(worklist.id, $scope.note).then(
                        // Success - add note
                        function(data) {
                          $uibModalInstance.close(worklist);
                        },
                        // Error - remove note
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      }
                      // close dialog if no note
                      else {
                        $uibModalInstance.close(worklist);
                      }
                    },
                    // Error - reassign
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }

                  // else unassign, then reassign
                  else if (action == 'UNASSIGN-REASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, worklist.id,
                      $scope.user.userName, $scope.role, 'UNASSIGN').then(
                      // Success - unassign
                      function(data) {
                        // The username doesn't matter - it'll go back to the
                        // author
                        workflowService.performWorkflowAction($scope.project.id, worklist.id,
                          $scope.user.userName, 'AUTHOR', 'REASSIGN').then(
                        // success - reassign
                        function(data) {
                          // Add a note as well
                          if ($scope.note) {
                            workflowService.addWorklistNote(worklist.id, $scope.note).then(
                            // Success - add note
                            function(data) {
                              $uibModalInstance.close(worklist);
                            },
                            // Error - remove note
                            function(data) {
                              handleError($scope.errors, data);
                            });
                          }
                          // close dialog if no note
                          else {
                            $uibModalInstance.close(worklist);
                          }
                        },
                        // Error - reassign
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      },
                      // Error - unassign
                      function(data) {
                        handleError($scope.errors, data);
                      });
                  }
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Log modal
              $scope.openLogModal = function() {
                console.debug('openLogModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/log.html',
                  controller : LogModalCtrl,
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {
                    worklist : function() {
                      return $scope.selected.worklist;
                    },
                    project : function() {
                      return $scope.project;
                    }
                  }
                });

                // NO need for result function - no action on close
                // modalInstance.result.then(function(data) {});
              };

              // Log controller
              var LogModalCtrl = function($scope, $uibModalInstance, worklist, project) {
                console.debug('Entered log modal control', worklist, project);

                $scope.errors = [];
                $scope.warnings = [];

                // Get log to display
                $scope.getLog = function() {
                  projectService.getLog(project.id, worklist.id).then(
                  // Success
                  function(data) {
                    $scope.log = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // Dismiss modal
                $scope.close = function() {
                  // nothing changed, don't pass a worklist
                  $uibModalInstance.close();
                };

                // initialize
                $scope.getLog();
              };

              // Add Worklist Member List modal
              $scope.openAddWorklistMemberListModal = function() {
                console.debug('openAddWorklistMemberListModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/addMemberList.html',
                  controller : AddWorklistMemberListModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    worklist : function() {
                      return $scope.selected.worklist;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });
              };

              // Add Worklist Member List controller
              var AddWorklistMemberListModalCtrl = function($scope, $uibModalInstance, worklist) {
                console.debug('Entered add worklist member list modal control', worklist);

                $scope.worklistMemberList = '';
                $scope.worklist = worklist;
                $scope.memberIdList = '';
                $scope.ids = [];
                $scope.added = [];
                $scope.exists = [];
                $scope.removed = [];
                $scope.notExists = [];
                $scope.errors = [];
                $scope.warnings = [];
                $scope.comments = [];

                // Used for enabling/disabling in UI
                $scope.hasResults = function() {
                  return $scope.added.length > 0 || $scope.removed.length > 0
                    || $scope.exists.length > 0 || $scope.notExists.length > 0;
                };

                // Add members in the list
                $scope.includeMembers = function() {
                  $scope.errors = [];
                  $scope.ids = getIds($scope.memberIdList);
                  for (var i = 0; i < $scope.ids.length; i++) {
                    var conceptId = $scope.ids[i];
                    includeMember(worklist, conceptId);
                  }
                };

                // find member and add if not exists
                function includeMember(worklist, conceptId) {
                  workflowService.findWorklistMembersForQuery(worklist.id, 'conceptId:' + conceptId, {
                    startIndex : 0,
                    maxResults : 1
                  }).then(
                  // Success
                  function(data) {
                    if (data.members.length > 0) {
                      $scope.exists.push(conceptId);
                    } else {
                      var member = {
                        active : true,
                        conceptId : conceptId,
                        memberType : 'MEMBER',
                        moduleId : worklist.moduleId,
                        worklistId : worklist.id
                      };
                      workflowService.addWorklistMember(member).then(
                      // Success
                      function(data) {
                        $scope.added.push(conceptId);
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                }

                // Exclude members in list
                $scope.excludeMembers = function() {
                  $scope.errors = [];
                  $scope.ids = getIds($scope.memberIdList);
                  var notExists = new Array();
                  var removed = new Array();
                  for (var i = 0; i < $scope.ids.length; i++) {
                    var conceptId = $scope.ids[i];
                    removeMember(worklist, conceptId);
                  }
                };

                // validation
                function removeMember(worklist, conceptId) {
                  workflowService.findWorklistMembersForQuery(worklist.id, 'conceptId:' + conceptId, {
                    startIndex : 0,
                    maxResults : 1
                  }).then(
                  // Success
                  function(data) {

                    if (data.members.length == 0) {
                      $scope.notExists.push(conceptId);
                    } else {

                      var memberId = data.members[0].id;
                      workflowService.removeWorklistMember(memberId).then(
                      // Success
                      function(data) {
                        $scope.removed.push(conceptId);
                      },
                      // Error
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    }
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                }

                // Get the ids from the value
                function getIds(value) {
                  // Split on punctuation
                  var list = $scope.memberIdList.split(/[\s;,\.]/);

                  var result = new Array();
                  // remove empty stuff
                  for (var i = 0; i < list.length; i++) {
                    if (list[i]) {
                      result.push(list[i]);
                    }
                  }
                  return result;
                }

                // Dismiss modal
                $scope.close = function() {
                  $uibModalInstance.close(worklist);
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Add Worklist modal
              $scope.openAddWorklistModal = function() {
                console.debug('openAddWorklistModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/editWorklist.html',
                  controller : AddWorklistModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    metadata : function() {
                      return $scope.metadata;
                    },
                    filters : function() {
                      return $scope.filters;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    projects : function() {
                      return $scope.projects;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  workflowService.fireWorklistChanged(data);
                });
              };

              // Add Worklist controller
              var AddWorklistModalCtrl = function($scope, $uibModalInstance, metadata, filters,
                project, projects) {
                console.debug('Entered add worklist modal control', metadata);

                $scope.action = 'Add';
                $scope.definition = null;
                $scope.metadata = metadata;
                $scope.filters = filters;
                $scope.project = project;
                $scope.projects = projects;
                $scope.versions = metadata.versions[$scope.project.terminology].sort().reverse();
                $scope.clause = {
                  value : null
                };
                $scope.worklist = {
                  workflowPath : metadata.workflowPaths[0],
                  version : $scope.versions[0],
                  namespace : $scope.project.namespace,
                  moduleId : $scope.project.moduleId,
                  organization : $scope.project.organization,
                  terminology : $scope.project.terminology,
                  feedbackEmail : $scope.project.feedbackEmail,
                  type : metadata.worklistTypes[0],
                  definitionClauses : [],
                  project : $scope.project
                };
                $scope.modules = [];
                $scope.errors = [];
                $scope.warnings = [];

                // Get $scope.modules
                $scope.getModules = function() {
                  projectService.getModules($scope.worklist.terminology, $scope.worklist.version).then(
                  // Success
                  function(data) {
                    $scope.modules = data.concepts;
                  });
                };

                // Initialize modules if terminology/version set
                if ($scope.worklist.terminology && $scope.worklist.version) {
                  $scope.getModules();
                }

                // Handle terminology selected
                $scope.terminologySelected = function(terminology) {
                  $scope.versions = metadata.versions[terminology].sort().reverse();
                  $scope.worklist.version = $scope.versions[0];
                  $scope.getModules();
                };

                // Handle version selected
                $scope.versionSelected = function(version) {
                  $scope.getModules();
                };

                // Assign worklist id
                $scope.assignWorklistTerminologyId = function(worklist) {
                  workflowService.assignWorklistTerminologyId(project.id, worklist).then(
                  // success
                  function(data) {
                    worklist.terminologyId = data;
                  },
                  // error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Add worklist
                $scope.submitWorklist = function(worklist) {

                  worklist.projectId = project.id;
                  // Setup definition if configured
                  if (worklist.type == 'EXTENSIONAL') {
                    $scope.clause = null;
                  }
                  if ($scope.clause && $scope.clause.value) {
                    worklist.definitionClauses = [ {
                      value : $scope.clause.value,
                      negated : false
                    } ];
                  }

                  // validate worklist before adding it
                  validationService
                    .validateWorklist(worklist)
                    .then(
                      function(data) {

                        // If there are errors, make them available and stop.
                        if (data.errors && data.errors.length > 0) {
                          $scope.errors = data.errors;
                          return;
                        } else {
                          $scope.errors = [];
                        }

                        // if $scope.warnings is empty, and data.warnings is
                        // not,
                        // show warnings and stop
                        if (data.warnings && data.warnings.length > 0
                          && $scope.warnings.join() !== data.warnings.join()) {
                          $scope.warnings = data.warnings;
                          return;
                        } else {
                          $scope.warnings = [];
                        }

                        if (!worklist.name || !worklist.description || !worklist.moduleId) {
                          $scope.errors[0] = 'Worklist name, description and moduleId must not be empty.';
                          return;
                        }

                        // Success - validate worklist
                        workflowService.addWorklist(worklist).then(
                        // Success - add worklist
                        function(data) {
                          var newWorklist = data;
                          $uibModalInstance.close(newWorklist);
                        },
                        // Error - add worklist
                        function(data) {
                          handleError($scope.errors, data);
                        });

                      },
                      // Error - validate worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Edit worklist modal
              $scope.openEditWorklistModal = function(lworklist) {
                console.debug('openEditWorklistModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/editWorklist.html',
                  controller : EditWorklistModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    worklist : function() {
                      return lworklist;
                    },
                    metadata : function() {
                      return $scope.metadata;
                    },
                    filters : function() {
                      return $scope.filters;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    projects : function() {
                      return $scope.projects;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  // handle workflow advancement
                  $scope.handleWorkflow(data);
                });
              };

              // Edit worklist controller
              var EditWorklistModalCtrl = function($scope, $uibModalInstance, worklist, metadata,
                filters, project, projects) {
                console.debug('Entered edit worklist modal control');

                $scope.action = 'Edit';
                $scope.worklist = worklist;
                $scope.filters = filters;
                $scope.project = project;
                $scope.worklist.project = project;
                $scope.projects = projects;
                $scope.metadata = metadata;
                $scope.versions = $scope.metadata.versions[worklist.terminology].sort().reverse();
                $scope.modules = [];
                $scope.errors = [];

                // Get $scope.modules
                $scope.getModules = function() {
                  projectService.getModules($scope.worklist.terminology, $scope.worklist.version).then(
                  // Success
                  function(data) {
                    $scope.modules = data.concepts;
                  });
                }; // Initialize modules if terminology/version set
                if ($scope.worklist.terminology && $scope.worklist.version) {
                  $scope.getModules();
                }

                // Handle terminology selected
                $scope.terminologySelected = function(terminology) {
                  $scope.versions = $scope.metadata.versions[terminology].sort().reverse();
                  $scope.getModules();
                };

                // Handle version selected
                $scope.versionSelected = function(version) {
                  $scope.getModules();
                };

                // Assign worklist id
                $scope.assignWorklistTerminologyId = function(worklist) {
                  workflowService.assignWorklistTerminologyId(project.id, worklist).then(
                  // success
                  function(data) {
                    worklist.terminologyId = data;
                  },
                  // error
                  function(data) {
                    handleError($scope.errors, data);
                  });
                };

                // Update worklist
                $scope.submitWorklist = function(worklist) {

                  worklist.projectId = worklist.project.id;

                  // Validate worklist
                  validationService.validateWorklist(worklist).then(
                    function(data) {

                      // If there are errors, make them available and stop.
                      if (data.errors && data.errors.length > 0) {
                        $scope.errors = data.errors;
                        return;
                      } else {
                        $scope.errors = [];
                      }

                      // if $scope.warnings is empty, and data.warnings is not,
                      // show warnings and stop
                      if (data.warnings && data.warnings.length > 0
                        && $scope.warnings.join() !== data.warnings.join()) {
                        $scope.warnings = data.warnings;
                        return;
                      } else {
                        $scope.warnings = [];
                      }

                      // Success - validate worklist
                      workflowService.updateWorklist(worklist).then(
                      // Success - update worklist
                      function(data) {
                        $uibModalInstance.close(worklist);
                      },
                      // Error - update worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });

                    },
                    // Error - validate worklist
                    function(data) {
                      handleError($scope.errors, data);
                    });

                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };


              // end

            } ]
        };
      } ]);
