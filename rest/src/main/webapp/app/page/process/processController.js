// Process controller
tsApp
  .controller(
    'ProcessCtrl',
    [
      '$scope',
      '$location',
      '$uibModal',
      '$timeout',
      'configureService',
      'gpService',
      'tabService',
      'utilService',
      'securityService',
      'projectService',
      'processService',
      'metadataService',
      function($scope, $location, $uibModal, $timeout, configureService, gpService, tabService,
        utilService, securityService, projectService, processService, metadataService) {
        console.debug("configure ProcessCtrl");

        // Set up tabs and controller
        tabService.setShowing(true);
        utilService.clearError();
        $scope.user = securityService.getUser();
        projectService.getUserHasAnyRole();
        tabService.setSelectedTabByLabel('Process');

        // Scope vars
        $scope.counts = {
          Config : 0,
          Execution : 0
        }

        // Selected variables
        $scope.selected = {
          project : null,
          projectRole : null,
          process : null,
          algorithm : null,
          processType : securityService.getProperty($scope.user.userPreferences, 'processType',
            'Insertion'),
          mode : securityService.getProperty($scope.user.userPreferences, 'processMode', 'Config')
        };

        // Lists
        $scope.lists = {
          processes : [],
          algorithms : [],
          projects : [],
          projectRoles : [],
          processTypes : [ 'Insertion', 'Maintenance', 'Release' ],
          algorithmConfigTypes : [],
          modes : [ 'Config', 'Execution' ]
        }

        // Progress Monitor variables
        $scope.max = 100;
        $scope.processProgress = 1;
        $scope.processInterval = null;
        $scope.stepProgress = 1;
        $scope.stepInterval = null;

        // Paging variables
        $scope.paging = {};
        $scope.paging['process'] = {
          page : 1,
          pageSize : 10,
          filter : '',
          filterFields : null,
          sortField : null,
          sortAscending : true,
          sortOptions : []
        };// utilService.getPaging();
        $scope.paging['process'].sortField = 'lastModified';
        $scope.paging['process'].pageSize = 5;
        $scope.paging['process'].callbacks = {
          getPagedList : getProcesses
        };

        $scope.paging['algo'] = utilService.getPaging();
        $scope.paging['algo'].sortField = 'lastModified';
        $scope.paging['algo'].callbacks = {
          getPagedList : getAlgorithms
        };

        // handle change in project role
        $scope.changeProjectRole = function() {
          // save the change
          securityService.saveRole($scope.user.userPreferences, $scope.selected.projectRole);
          $scope.resetPaging();
          $scope.getProcesses();
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
            $scope.getProcesses();
          });

        }

        // Reset paging
        $scope.resetPaging = function() {
          $scope.paging['process'].page = 1;
          $scope.paging['process'].filter = null;
          $scope.paging['algo'].page = 1;
          $scope.paging['algo'].filter = null;
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

        // Set worklist mode
        $scope.setMode = function(mode) {
          // Set the processType property
          $scope.selected.mode = mode;
          securityService.saveProperty($scope.user.userPreferences, 'processMode',
            $scope.selected.mode);
          $scope.selected.process = null;
          $scope.getProcesses();
        }

        // Get $scope.lists.processes
        $scope.getProcesses = function() {
          // Set the processType property
          securityService.saveProperty($scope.user.userPreferences, 'processType',
            $scope.selected.processType);
          getProcesses();
        }
        function getProcesses() {
          if ($scope.selected.mode == 'Config') {
            $scope.getProcessConfigs();
          } else if ($scope.selected.mode == 'Execution') {
            $scope.getProcessExecutions();
          }
        }

        // Select a process
        $scope.selectProcess = function(process, noprogress) {
          // Read the process
          processService['getProcess' + $scope.selected.mode]($scope.selected.project.id,
            process.id).then(
          // Success
          function(data) {

            // Set selected
            $scope.selected.process = data;

            // Start polling for this one
            if (!noprogress && $scope.selected.mode == 'Execution') {
              $timeout(function() {
                $scope.refreshProcessProgress();
              }, 1000);
            }

          });

          // Look up algorithm types (if in config mode)
          if ($scope.selected.mode == 'Config') {
            $scope.lists.algorithmConfigTypes = [];
            processService['get' + $scope.selected.processType + 'Algorithms'](
              $scope.selected.project.id).then(
            // Success
            function(data) {
              for (var i = 0; i < data.keyValuePairs.length; i++) {
                $scope.lists.algorithmConfigTypes.push(data.keyValuePairs[i]);
              }
              $scope.lists.algorithmConfigTypes.sort(utilService.sortBy('value'));
              $scope.selected.algorithmConfigType = $scope.lists.algorithmConfigTypes[0];
            });
          }

          // // Start polling
          if (!noprogress && $scope.selected.mode == 'Execution') {
            $timeout(function() {
              $scope.refreshStepProgress();
            }, 1000);
          }
        }

        // prepare process
        $scope.prepareProcess = function() {
          $scope.processConfig = $scope.selected.process;
          processService.prepareProcess($scope.selected.project.id, $scope.selected.process.id)
            .then(
              // Success
              function(data) {
                $scope.selected.process.id = data;
                // don't call setMode because it reloads processes
                $scope.selected.mode = 'Execution';
                securityService.saveProperty($scope.user.userPreferences, 'processMode',
                  $scope.selected.mode);
                gpService.increment();
                $timeout(function() {
                  $scope.selectProcess($scope.selected.process);
                  gpService.decrement();
                }, 1000);
              });
        }

        // execute process
        $scope.executeProcess = function() {
          $scope.processConfig = $scope.selected.process;
          processService.executeProcess($scope.selected.project.id, $scope.selected.process.id,
            true).then(
          // Success
          function(data) {
            gpService.increment();
            $timeout(function() {
              $scope.selectProcess($scope.selected.process);
              gpService.decrement();
            }, 1000);
          });
        }

        // Refresh process progress
        $scope.refreshProcessProgress = function() {
          if (!$scope.selected.process) {
            return;
          }
          processService.getProcessProgress($scope.selected.project.id, $scope.selected.process.id)
            .then(
            // Success
            function(data) {
              $scope.processProgress = data;

              // stop interval if process progress has reached 100, reread with
              // no progress start
              if (data == "100" || data === "-1") {
                // finished
              } else {
                // Reselect the process to refresh everything and restart
                // progress monitors as needed
                $timeout(function() {
                  $scope.refreshProcessProgress();
                }, 1000);
              }
            });
        }

        // Refresh step progress
        $scope.refreshStepProgress = function() {
          var i = $scope.selected.process.steps.length;

          // Bail if there are no steps
          if (!i) {
            console.debug("process has no steps yet.");
            return;
          }

          $scope.selected.lastAlgorithm = $scope.selected.process.steps[i - 1].id;
          processService.getAlgorithmProgress($scope.selected.project.id,
            $scope.selected.lastAlgorithm).then(
          // Success
          function(data) {

            $scope.stepProgress = data;

            // stop interval if step is not running or progress has finished
            if (data == "100" || data == "-1") {
              // finished
            } else {
              // Reselect the process to refresh everything and restart
              // progress monitors as needed
              $timeout(function() {
                $scope.refreshStepProgress();
              }, 1000);
            }
          });
        };

        // Get process configs
        $scope.getProcessConfigs = function() {
          var paging = $scope.paging['process'];
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : (paging.filter ? paging.filter + ' AND ' : '')
              + $scope.selected.processType
          };
          processService.findProcessConfigs($scope.selected.project.id, null, pfs).then(
          // Success
          function(data) {
            $scope.lists.processes = data.processes;
            $scope.lists.processes.totalCount = data.totalCount;
            $scope.counts[$scope.selected.mode] = data.totalCount;
            if ($scope.lists.processes && $scope.lists.processes.length > 0) {
              $scope.selectProcess($scope.lists.processes[0]);
            }

            $scope.getProcessExecutionsCt();
          });

        }

        // Remove a process, then reread them
        $scope.removeProcess = function(processId) {
          processService['removeProcess' + $scope.selected.mode]($scope.selected.project.id,
            processId).then(
          // Success
          function(data) {
            $scope.getProcesses();
          });
        }

        // Remove an algorithm config
        $scope.removeAlgorithmConfig = function(algorithmId) {
          processService.removeAlgorithmConfig($scope.selected.project.id, algorithmId).then(
          // Success
          function(data) {
            $scope.selectProcess($scope.selected.process);
          });
        }

        // Cancel process
        $scope.cancelProcess = function(processId) {
          processService.cancelProcess($scope.selected.project.id, processId).then(
          // Success
          function() {
            gpService.increment();
            $timeout(function() {
              $scope.selectProcess($scope.selected.process);
              gpService.decrement();
            }, 750);
          });
        }

        // Restart process
        $scope.restartProcess = function(processId) {
          gpService.increment();
          processService.restartProcess($scope.selected.project.id, processId, true).then(
          // Success
          function() {
            gpService.increment();
            $timeout(function() {
              $scope.selectProcess($scope.selected.process);
              gpService.decrement();
            }, 750);

          });
        }

        // Get process executions
        $scope.getProcessExecutions = function() {
          var paging = $scope.paging['process'];
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : 'lastModified',
            ascending : false,
            queryRestriction : (paging.filter ? paging.filter + ' AND ' : '')
              + $scope.selected.processType
          };
          processService.findProcessExecutions($scope.selected.project.id, null, pfs).then(
          // Success
          function(data) {
            $scope.lists.processes = data.processes;
            $scope.lists.processes.totalCount = data.totalCount;
            $scope.counts[$scope.selected.mode] = data.totalCount;
            if ($scope.lists.processes && $scope.lists.processes.length > 0) {
              $scope.selectProcess($scope.lists.processes[0]);
            }

            $scope.getProcessConfigsCt();
          });

        }

        // compute execution state based on process flags
        $scope.getExecutionState = function(execution) {
          if (!execution) {
            return '';
          } else if ($scope.selected.mode == 'Config') {
            return 'CONFIG';
          } else if (!execution.failDate && !execution.finishDate) {
            return 'RUNNING';
          } else if (execution.failDate && execution.finishDate) {
            return 'STOPPED';
          } else if (!execution.failDate && execution.finishDate) {
            return 'COMPLETE';
          } else if (execution.failDate && !execution.finishDate) {
            return 'FAILED';
          } else if (!execution.startDate) {
            return 'READY';
          }
        }

        $scope.getProcessConfigsCt = function() {
          var pfs = {
            startIndex : 0,
            maxResults : 1,
            queryRestriction : $scope.selected.processType
          };
          processService.findProcessConfigs($scope.selected.project.id, null, pfs).then(
          // Success
          function(data) {
            $scope.counts['Config'] = data.totalCount;
          });
        }

        $scope.getProcessExecutionsCt = function() {
          var pfs = {
            startIndex : 0,
            maxResults : 1,
            queryRestriction : $scope.selected.processType
          };
          processService.findProcessExecutions($scope.selected.project.id, null, pfs).then(
          // Success
          function(data) {
            $scope.counts['Execution'] = data.totalCount;
          });
        }

        // Select $scope.selected.step
        $scope.selectStep = function(step) {
          $scope.selected.step = step;
        }

        // Get $scope.lists.algorithms
        // switch based on mode
        $scope.getAlgorithms = function(algorithm) {
          getAlgorithms(algorithm);
        }
        function getAlgorithms(algorithm) {
          if ($scope.selected.mode == 'Config') {
            $scope.getAlgorithmConfigs();
          } else if ($scope.selected.mode == 'Execution') {
            $scope.getAlgorithmExecutions();
          }
        }

        // Move a step up in step order
        $scope.moveStepUp = function(step) {
          // Start at index 1 because we can't move the top one up
          for (var i = 1; i < $scope.selected.process.steps.length; i++) {
            if (step.id == $scope.selected.process.steps[i].id) {
              $scope.selected.process.steps.splice(i, 1);
              $scope.selected.process.steps.splice(i - 1, 0, step);
            }
          }
          processService.updateProcessConfig($scope.selected.project.id, $scope.selected.process);

        };

        // Move a step down in step order
        $scope.moveStepDown = function(step) {
          // end at index -1 because we can't move the last one down
          for (var i = 0; i < $scope.selected.process.steps.length - 1; i++) {
            if (step.id == $scope.selected.process.steps[i].id) {
              $scope.selected.process.steps
                .splice(i, 2, $scope.selected.process.steps[i + 1], step);
              break;
            }
          }
          processService.updateProcessConfig($scope.selected.project.id, $scope.selected.process);
        };

        $scope.isFirstIndex = function(entry) {
          return entry.id == $scope.selected.process.steps[0].id;
        }

        $scope.isLastIndex = function(entry) {
          return entry.id == $scope.selected.process.steps[$scope.selected.process.steps.length - 1].id;
        }

        // Convert date to a string
        $scope.toDate = function(date) {
          return utilService.toDate(date);
        };

        // Table sorting mechanism
        $scope.setSortField = function(table, field, object) {
          utilService.setSortField(table, field, $scope.paging);

          // retrieve the correct table
          if (table === 'process') {
            $scope.getProcesses();
          }
          if (table === 'algo') {
            $scope.getAlgorithms();
          }
        };

        // Return up or down sort chars if sorted
        $scope.getSortIndicator = function(table, field) {
          return utilService.getSortIndicator(table, field, $scope.paging);
        };

        $scope.hasPermissions = function(action) {
          return securityService.hasPermissions(action);
        }

        //
        // MODALS
        //

        // Add new process
        $scope.openAddProcessModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/process/editProcess.html',
            controller : 'ProcessModalCtrl',
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
              process : function() {
                return null;
              },
              action : function() {
                return 'Add';
              }
            }
          });

          modalInstance.result.then(
          // dialog closed
          function(data) {
            $scope.getProcessConfigs();
          });
        };

        // Add new process
        $scope.openEditProcessModal = function(lprocess) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/process/editProcess.html',
            controller : 'ProcessModalCtrl',
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
              process : function() {
                return lprocess;
              },
              action : function() {
                return 'Edit';
              }
            }
          });

          modalInstance.result.then(
          // dialog closed
          function(data) {
            $scope.getProcessConfigs();
          });
        };

        // Add new algorithm
        $scope.openAddAlgorithmModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/process/editAlgorithm.html',
            controller : 'AlgorithmModalCtrl',
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
              algorithm : function() {
                return null;
              },
              action : function() {
                return 'Add';
              }
            }
          });

          modalInstance.result.then(
          // dialog closed
          function(data) {
            $scope.selectProcess($scope.selected.process);
          });
        };
        // edit algorithm
        $scope.openEditAlgorithmModal = function(lalgorithm) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/process/editAlgorithm.html',
            controller : 'AlgorithmModalCtrl',
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
              algorithm : function() {
                return lalgorithm;
              },
              action : function() {
                return 'Edit';
              }
            }
          });

          modalInstance.result.then(
          // dialog closed
          function(data) {
            $scope.selectProcess($scope.selected.process);
          });
        };

        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {
          // configure tab
          securityService.saveTab($scope.user.userPreferences, '/process');
          $scope.getProjects();

          // Get all terminologies
          metadataService.getTerminologies().then(
          // Success
          function(data) {
            $scope.lists.terminologies = data.terminologies;
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