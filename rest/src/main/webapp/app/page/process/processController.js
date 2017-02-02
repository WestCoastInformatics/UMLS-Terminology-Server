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
        console.debug('configure ProcessCtrl');

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
          step : null,
          processType : securityService.getProperty($scope.user.userPreferences, 'processType',
            'Insertion'),
          mode : securityService.getProperty($scope.user.userPreferences, 'processMode', 'Config'),
          configForExec : null,
          configForExecStep : null,
          configForExecStepCt : 0
        };

        // Lists
        $scope.lists = {
          processes : [],
          algorithms : [],
          projects : [],
          projectRoles : [],
          processTypes : [ 'Insertion', 'Maintenance', 'Release', 'Report' ],
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
        $scope.pageSizes = utilService.getPageSizes();
        $scope.paging = {};
        $scope.paging['process'] = {
          page : 1,
          pageSize : 10,
          filter : '',
          filterFields : null,
          sortField : 'lastModified',
          sortAscending : true,
          sortOptions : []
        };
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
          // utilService.clearError();
          $scope.selected.mode = mode;
          securityService.saveProperty($scope.user.userPreferences, 'processMode',
            $scope.selected.mode);
          $scope.selected.process = null;
          $scope.getProcesses();
          $scope.selected.configForExec = null;
          $scope.selected.configForExecStep = null;
          $scope.selected.configForExecStepCt = 0;
        }

        // Get $scope.lists.processes
        $scope.getProcesses = function(process) {
          // Set the processType property
          securityService.saveProperty($scope.user.userPreferences, 'processType',
            $scope.selected.processType);
          getProcesses(process);
        }
        function getProcesses(process) {
          $scope.selected.process = null;
          if ($scope.selected.mode == 'Config') {
            $scope.getProcessConfigs(process);
          } else if ($scope.selected.mode == 'Execution') {
            $scope.getProcessExecutions(process);
          }
        }

        // Select a process
        $scope.selectProcess = function(process, processProgress, algorithmProgress) {
          console.debug('selectProcess executed.  processProgres: ' + processProgress
            + ', algorithProgress: ' + algorithmProgress);
          // Read the process
          processService['getProcess' + $scope.selected.mode]($scope.selected.project.id,
            process.id).then(
            // Success
            function(data) {

              // Set selected
              $scope.selected.process = data;
              if ($scope.selected.lastAlgorithm
                && $scope.selected.lastAlgorithm.processId != data.id) {
                $scope.selected.lastAlgorithm = null;
              }
              // Start polling for this one
              if ($scope.selected.mode == 'Execution') {
                if (processProgress) {
                  $timeout(function() {
                    $scope.refreshProcessProgress();
                  }, 1000);
                }
                if (algorithmProgress) {
                  $timeout(function() {
                    $scope.refreshStepProgress();
                  }, 1000);
                }

                // Read the "configForExec"
                processService.getProcessConfig($scope.selected.project.id, data.processConfigId)
                  .then(
                  // Success
                  function(data) {
                    $scope.selected.configForExec = data;
                  });
              }

              for (var i = 0; i < $scope.lists.processes.length; i++) {
                if ($scope.lists.processes[i].id == $scope.selected.process.id) {
                  $scope.lists.processes[i] = $scope.selected.process;
                }

              }

            });

          if ($scope.selected.mode == 'Config') {
            $scope.lists.algorithmConfigTypes = [];
            processService.getAlgorithmsForType($scope.selected.project.id,
              $scope.selected.processType.toLowerCase()).then(
            // Success
            function(data) {
              for (var i = 0; i < data.keyValuePairs.length; i++) {
                $scope.lists.algorithmConfigTypes.push(data.keyValuePairs[i]);
              }
              $scope.lists.algorithmConfigTypes.sort(utilService.sortBy('value'));
              $scope.selected.algorithmConfigType = $scope.lists.algorithmConfigTypes[0];
            });
          }

        }

        // Get algorithm configs for unexecuted steps
        $scope.getUnexecutedAlgorithms = function() {
          if ($scope.selected.mode == 'Config' || !$scope.selected.configForExec) {
            return [];
          }
          var unexecuted = [];
          for (var i = 0; i < $scope.selected.configForExec.steps.length; i++) {
            var configStep = $scope.selected.configForExec.steps[i];
            if (!configStep.enabled) {
              continue;
            }
            for (var j = 0; j < $scope.selected.process.steps.length; j++) {
              var execStep = $scope.selected.process.steps[j];
              var found = false;
              if (configStep.name == execStep.name
                && configStep.description == execStep.description) {
                found = true;
                break;
              }
            }
            $scope.selected.configForExecStepStart = j;
            if (!found) {
              unexecuted.push(configStep);
            }
          }
          $scope.selected.configForExecStepCt = unexecuted.length;
          return unexecuted;
        }

        // prepare process
        $scope.prepareProcess = function() {
          $scope.processConfig = $scope.selected.process;
          processService.prepareProcess($scope.selected.project.id, $scope.selected.process.id)
            .then(
              // Success
              function(data) {
                // don't call setMode because it reloads processes
                $scope.selected.mode = 'Execution';
                securityService.saveProperty($scope.user.userPreferences, 'processMode',
                  $scope.selected.mode);
                gpService.increment();
                $timeout(function() {
                  $scope.getProcesses({
                    id : data
                  });
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
              $scope.selectProcess({
                id : data
              }, true, true);
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
              // stop interval if process progress has reached 100, reread
              // with no progress start
              if (data == '100' || data == '-1') {
                if ($scope.selected.process) {
                  $scope.selectProcess($scope.selected.process, false, false);
                }
              } else {
                // don't refresh if process is stopped
                if (!$scope.selected.process.stopDate) {
                  // Reselect the process to refresh everything and restart
                  // progress monitors as needed
                  $timeout(function() {
                    $scope.refreshProcessProgress();
                  }, 1000);
                }
              }
            });
        }

        // Refresh step progress
        $scope.refreshStepProgress = function() {
          if (!$scope.selected.process) {
            return;
          }
          var i = $scope.selected.process.steps.length;

          // Bail if there are no steps
          if (!i) {
            console.debug('process has no steps yet.');
            $scope.stepProgress = -1;
            return;
          }
          $scope.selected.lastAlgorithm = $scope.selected.process.steps[i - 1];
          processService.getAlgorithmProgress($scope.selected.project.id,
            $scope.selected.lastAlgorithm.id).then(
          // Success
          function(data) {

            $scope.stepProgress = data;

            // stop interval if step is not running or progress has finished
            if (data == '100' || data == '-1') {
              if (!$scope.selected.process.finishDate && !$scope.selected.process.stopDate) {
                $scope.selectProcess($scope.selected.process, false, data != '-1');
              }
            } else {
              // Reselect the process to refresh everything and restart
              // progress monitors as needed
              $timeout(function() {
                $scope.refreshStepProgress();
              }, 2000);
            }
          });
        };

        // Get process configs
        $scope.getProcessConfigs = function(process) {
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
            if (process) {
              $scope.selectProcess(process);
            } else if ($scope.lists.processes && $scope.lists.processes.length > 0) {
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

        // enable/disable
        $scope.toggleEnable = function(algorithm) {

          processService.getAlgorithmConfig($scope.selected.project.id, algorithm.id).then(
            function(response) {
              var retAlgorithm = response;
              if (retAlgorithm.enabled) {
                retAlgorithm.enabled = false;
              } else {
                retAlgorithm.enabled = true;
              }

              processService.updateAlgorithmConfig($scope.selected.project.id,
                $scope.selected.process.id, retAlgorithm).then(
              // Success
              function(data) {
                $scope.selectProcess($scope.selected.process);
              });
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
              $scope.selectProcess({
                id : processId
              });
              gpService.decrement();
            }, 750);
          });
        }

        // Restart process
        $scope.restartProcess = function(processId) {
          processService.restartProcess($scope.selected.project.id, processId, true).then(
          // Success
          function() {
            gpService.increment();
            $timeout(function() {
              $scope.selectProcess({
                id : processId
              }, true, true);
              gpService.decrement();
            }, 750);

          });
        }

        // Step process
        $scope.stepProcess = function(processId, step) {
          processService.stepProcess($scope.selected.project.id, processId, step, true).then(
          // Success
          function() {
            gpService.increment();
            $timeout(function() {
              $scope.selectProcess({
                id : processId
              }, true, true);
              gpService.decrement();
            }, 750);

          });
        }

        // Get process executions
        $scope.getProcessExecutions = function(process) {
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
            if (process) {
              $scope.selectProcess(process);
            } else if ($scope.lists.processes && $scope.lists.processes.length > 0) {
              $scope.selectProcess($scope.lists.processes[0]);
            }

            $scope.getProcessConfigsCt();
          });

        }

        // compute execution state based on process flags
        $scope.getExecutionState = function(execution) {
          if (!execution) {
            retval = '';
            return;
          } else if ($scope.selected.mode == 'Config') {
            retval = 'CONFIG';
          } else if (!execution.startDate) {
            retval = 'READY';
          } else if (!execution.stopDate && !execution.failDate && !execution.finishDate) {
            retval = 'RUNNING';
          } else if (execution.stopDate) {
            retval = 'STOPPED';
          } else if (execution.failDate && execution.finishDate) {
            retval = 'CANCELLED';
          } else if (!execution.failDate && execution.finishDate) {
            retval = 'COMPLETE';
          } else if (execution.failDate && !execution.finishDate) {
            retval = 'FAILED';
          }
          if (execution.warning) {
            retval += ', WARNING';
          }
          return retval;
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

        // Select $scope.selected.step
        $scope.selectConfigForExecStep = function(step) {
          $scope.selected.configForExecStep = step;
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

        // Export a process
        $scope.exportProcess = function() {
          processService.exportProcess($scope.selected.project.id, $scope.selected.process.id);
        }

        //
        // MODALS
        //

        // Import a process
        $scope.openImportProcessModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/process/importProcess.html',
            controller : 'ImportProcessModalCtrl',
            backdrop : 'static',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              lists : function() {
                return $scope.lists;
              }
            }
          });

          modalInstance.result.then(
          // dialog closed
          function(data) {
            if (data) {
              $scope.getProcesses();
            }
          });
        };

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
            $scope.getProcessConfigs(data);
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
            $scope.getProcessConfigs(data);
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