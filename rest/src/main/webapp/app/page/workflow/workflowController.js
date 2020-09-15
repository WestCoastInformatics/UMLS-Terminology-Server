// Workflow controller
tsApp.controller('WorkflowCtrl', [
  '$scope',
  '$http',
  '$location',
  '$uibModal',
  '$window',
  '$interval',
  'utilService',
  'websocketService',
  'tabService',
  'configureService',
  'securityService',
  'projectService',
  'metadataService',
  'workflowService',
  'reportService',
  function($scope, $http, $location, $uibModal, $window, $interval, utilService, websocketService, tabService,
    configureService, securityService, projectService, metadataService, workflowService,
    reportService) {
    console.debug("configure WorkflowCtrl");

    // Set up tabs and controller
    tabService.setShowing(true);
    utilService.clearError();
    $scope.user = securityService.getUser();
    $scope.ws = websocketService.getData();
    projectService.getUserHasAnyRole();
    tabService.setSelectedTabByLabel('Workflow');

    $scope.pageSizes = utilService.getPageSizes();
    
    // Progress tracking
    $scope.lookupInterval = null;
    $scope.binsInProcessProgress = [];
    $scope.binsProcessCompleted = [];

    // Selected variables
    $scope.selected = {
      project : null,
      config : null,
      bin : null,
      clusterType : null,
      projectRole : null,
      // Used to trigger events in worklist-table directive controller
      refreshCt : 0,
      terminology : null,
      metadata : null,
      epoch : null,
      report : null
    };

    // Lists
    $scope.lists = {
      bins : [],
      records : [],
      configs : [],
      projects : [],
      projectRoles : [],
      queryTypes : [],
      recordTypes : workflowService.getRecordTypes()
    }

    // Accordion Groups open status
    $scope.groups = [ {
      open : true
    }, {
      open : false
    }, {
      open : false
    }, {
      open : false
    } ];

    // clear and reload records to avoid stale ones appearing
    $scope.$watch('groups[0].open', function(isOpen){
      if (isOpen) {
        console.log('Bins group was opened'); 
        $scope.lists.records = null;
      }    
    });
    $scope.$watch('groups[1].open', function(isOpen){
      if (isOpen) {
        console.log('Checklists group was opened'); 
        $scope.lists.records = null;
        websocketService.fireChecklistChange($scope.selected.project);
      }    
    });
    $scope.$watch('groups[2].open', function(isOpen){
      if (isOpen) {
        console.log('Worklists group was opened'); 
        $scope.lists.records = null;
        websocketService.fireWorklistChange($scope.selected.project);
      }    
    });
    
    // Handle worklist actions
    $scope.$on('termServer::binsChange', function(event, project) {
      if (project.id == $scope.selected.project.id) {
        // Bins changed, refresh bins
        $scope.getBins($scope.selected.project.id, $scope.selected.config, $scope.selected.bin);
      }
    });

    // $scope.$on('termServer::checklistChange', -- n/a, no action on checklist
    // change
    $scope.$on('termServer::worklistChange', function(event, data) {
      if (data.id == $scope.selected.project.id) {
        // could affect worklist bin counts
        $scope.getBins($scope.selected.project.id, $scope.selected.config, $scope.selected.bin);
      }
    });

    // reconnect
    $scope.reconnect = function() {
      $window.location.reload();
    }

    // Paging parameters
    $scope.paging = {};
    $scope.pageSizes = utilService.getPageSizes();
    $scope.resetRecordPaging = function() {
      $scope.paging['records'] = utilService.getPaging();
      $scope.paging['records'].sortField = 'clusterId';
      $scope.paging['records'].callbacks = {
        getPagedList : getPagedList
      };
    }

    $scope.resetBinPaging = function() {
      $scope.paging['bins'] = utilService.getPaging();
      $scope.paging['bins'].sortField = 'rank';
      $scope.paging['bins'].callbacks = {
        getPagedList : getPagedBins
      };
    }
    $scope.resetBinPaging();
    
    $scope.resetReportPaging = function() {
      $scope.paging['reports'] = utilService.getPaging();
      $scope.paging['reports'].sortField = 'lastModified';
      $scope.paging['reports'].sortAscending = false;
      $scope.paging['reports'].disableFilter = true;
      $scope.paging['reports'].callbacks = {
        getPagedList : findPagedReports
      };
    }
    $scope.resetReportPaging();
    
    $scope.resetResultPaging = function() {
      $scope.paging['results'] = utilService.getPaging();
      $scope.paging['results'].sortField = 'value';
      $scope.paging['results'].callbacks = {
        getPagedList : getPagedResults
      };
    }
    $scope.resetResultPaging();

    // Set the workflow config
    $scope.setConfig = function(config) {
      $scope.selected.config = config;
      if ($scope.selected.config) {
        $scope.getBins($scope.selected.project.id, $scope.selected.config, $scope.selected.bin);
      }
      $scope.user.userPreferences.properties['workflowConfigType'] = $scope.selected.config.type;
      securityService.updateUserPreferences($scope.user.userPreferences);
      
      // when config type is switched, reset bin paging back to page 1
      $scope.resetBinPaging();
      $scope.user.userPreferences.properties['workflowBinPaging'] = JSON
      .stringify($scope.paging['bins']);
      securityService.updateUserPreferences($scope.user.userPreferences);
    }

    // Retrieve all bins with project and type
    $scope.getBins = function(projectId, config, bin) {
      // Clear the records
      $scope.lists.records = [];

      // Skip if no config types
      if (config.type) {
        workflowService.getWorkflowBins(projectId, config.type).then(
        // Success
        function(data) {
          $scope.lists.bins = data.bins;
          $scope.lists.bins.totalCount = $scope.lists.bins.length;
          if (bin) {
            var filtered = $scope.lists.bins.filter(function(item) {
              return item.id == bin.id;
            });
            if (filtered.length == 1) {
              $scope.selectBin(filtered[0]);
            }
          }
          var paging = $scope.paging['bins'];
          
          //if (recoverPreferences){
            paging = JSON.parse($scope.user.userPreferences.properties['workflowBinPaging']);
            angular.copy(paging, $scope.paging['bins']);
            $scope.paging['bins'].callbacks = {
              getPagedList : getPagedBins
            };
          //} 
          //$scope.resetBinPaging();
          $scope.getPagedBins();
        });
      }
    };

    $scope.getEpoch = function() {
      workflowService.getWorkflowEpoch($scope.selected.project.id).then(function(data) {
        $scope.selected.epoch = data;
      });
    }

    // handle change in project role
    $scope.changeProjectRole = function() {
      // save the change
      securityService.saveRole($scope.user.userPreferences, $scope.selected.projectRole);
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

        // Get configs
        $scope.getConfigs();
        $scope.getEpoch();
      });
      projectService.findAssignedUsersForProject($scope.selected.project.id, null, null).then(
        function(data) {
          $scope.lists.users = data.users;
          $scope.lists.users.totalCount = data.totalCount;
        });

      // Initialize metadata
      metadataService.getTerminology($scope.selected.project.terminology,
        $scope.selected.project.version).then(
      // Success
      function(data) {
        metadataService.setTerminology(data);
      });
      metadataService.getAllMetadata($scope.selected.project.terminology,
        $scope.selected.project.version).then(
      // Success
      function(data) {
        $scope.selected.metadata = data;
      });

    }

    // update project
    $scope.updateProject = function(project) {
    	projectService.updateProject(project);
    }
    
    // Retrieve all projects
    $scope.getProjects = function() {

      projectService.getProjectsForUser($scope.user).then(
      // Success
      function(data) {
        $scope.lists.projects = data.projects;
        $scope.setProject(data.project);
      });

    };

    // Determine which config lists to show
    $scope.getConfigList = function() {
      if ($scope.selected.projectRole == 'ADMINISTRATOR') {
        return $scope.lists.configs;
      } else {
        return $scope.lists.configs.filter(function(item) {
          return !item.adminConfig;
        });
      }
    }

    // Return report type list
    $scope.getReportTypeList = function() {
      var configs = $scope.lists.configs.filter(function(item) {
        return item.queryStyle == 'REPORT';
      });
      var reportTypes = [];
      for (var i = 0; i < configs.length; i++) {
        for (var j = 0; j < configs[i].workflowBinDefinitions.length; j++) {
          reportTypes.push(configs[i].workflowBinDefinitions[j]);
        }
      }
      return reportTypes;
    }

    // the report type is a workflowBinDefinition on a WorkflowConfig that has
    // QueryStyle.REPORT
    // This needs to be a function so it can be scoped properly for the
    // pager
    $scope.findPagedReports = function() {
      findPagedReports();
    };
    function findPagedReports() {

      var pfs = {
        startIndex : ($scope.paging['reports'].page - 1) * $scope.paging['reports'].pageSize,
        maxResults : $scope.paging['reports'].pageSize,
        sortField : $scope.paging['reports'].sortField,
        ascending : $scope.paging['reports'].sortAscending,
        queryRestriction : $scope.paging['reports'].filter ? $scope.paging['reports'].filter : ''
      };
      
      reportService.findReportsByName($scope.selected.project.id, $scope.selected.reportType.name, pfs).then(
      // Success
      function(data) {
        $scope.selected.reports = data;
        $scope.selected.reports.totalCount = data.totalCount;
        $scope.pagedResults = null;
        $scope.resetResultPaging();
      });
    }

    // generate report
    $scope.generateReport = function() {
      reportService.generateReport($scope.selected.project.id, $scope.selected.reportType.name, $scope.selected.reportType.query, 
        $scope.selected.reportType.queryType, 'CONCEPT').then(
       // Success
       function(data) {
        $scope.selected.report = data;
        $scope.findPagedReports();
      });
    }

    // remove report
    $scope.removeReport = function(report) {
      reportService.removeReport(report.id).then(
       // Success
       function(data) {
        $scope.findPagedReports();
      });
    }
    
    //
    $scope.setSelectedReport = function(report) {
      $scope.selected.report = report;
      // split result stys to they can be displayed in a list
      for (var i = 0; i<report.results.length; i++) {
        var value = $scope.selected.report.results[i].value;
        $scope.selected.report.results[i].valueArray = value.split('@');
      }

      $scope.getPagedResults();
    }
   
    // This needs to be a function so it can be scoped properly for the
    // pager
    $scope.getPagedResults = function() {
      getPagedResults();
    };
    function getPagedResults() {
      $scope.pagedResults = utilService.getPagedArray($scope.selected.report.results,
        $scope.paging['results']);
    }
    
/*    // create a new checklist from report results
    $scope.computeChecklist = function(result) {
      var query = 'select distinct itemId conceptId, itemId clusterId from report_result_items a, ' +
        ' report_results b where b.report_id = ' + $scope.selected.report.id  + ' and b.id = ' + result.id +
        ' and a.result_id = b.id';
      
      var name = $scope.selected.reportType.name.replace(/\s/g, '_');
      var fullName = 'chk_' + name + '_' + result.id;
      workflowService.computeChecklist($scope.selected.project.id, query, 
        'SQL', fullName, null).then(
      // Success
      function(data) {
        window.alert("Checklist created " + fullName + ".");
      },
      // Error
      function(data) {
        $scope.errors[0] = data;
        utilService.clearError();
      });
    }*/
    
    // Table sorting mechanism
    $scope.setSortField = function(table, field, object) {
      utilService.setSortField(table, field, $scope.paging);

      // retrieve the correct table
      if (table === 'results') {
        $scope.getPagedResults();
      }
      
    };

    // Return up or down sort chars if sorted
    $scope.getSortIndicator = function(table, field) {
      return utilService.getSortIndicator(table, field, $scope.paging);
    };
    
    
    // Retrieve all projects
    $scope.getConfigs = function() {
      workflowService.getWorkflowConfigs($scope.selected.project.id).then(
      // Success
      function(data) {
        $scope.lists.configs = data.configs.sort(utilService.sortBy('type'));

        // Select the MUTUALLY_EXCLUSIVE config if available.
        // If not, select the first config in the list.
        var selectConfig = $scope.lists.configs[0];
        var userSelectedConfig = $scope.user.userPreferences.properties['workflowConfigType'];
        for (var i = 0; i < $scope.lists.configs.length; i++) {
          if ($scope.lists.configs[i].type == 'MUTUALLY_EXCLUSIVE') {
            selectConfig = $scope.lists.configs[i];
          }
          if ($scope.lists.configs[i].type == userSelectedConfig) {
            selectConfig = $scope.lists.configs[i];
            break;
          }
        }
        $scope.setConfig(selectConfig);
        
      });
    };

    // Selects a bin (setting $scope.selected.bin)
    // clusterType is optional
    $scope.selectBin = function(bin, clusterType) {
      $scope.selected.bin = bin;
      $scope.selected.clusterType = clusterType;

      if (!bin.id) {
        return;
      }

      $scope.resetRecordPaging();
      if (clusterType && clusterType == 'default') {
        $scope.paging['records'].filter = ' NOT clusterType:[* TO *]';
      } else if (clusterType && clusterType != 'all') {
        $scope.paging['records'].filter = clusterType;
      } else if (clusterType == 'all') {
        $scope.paging['records'].filter = '';
      }
      getPagedList();
      $scope.user.userPreferences.properties['workflowBinPaging'] = JSON
      .stringify($scope.paging['bins']);
      securityService.updateUserPreferences($scope.user.userPreferences);
    };

    // This needs to be a function so it can be scoped properly for the
    // pager
    $scope.getPagedList = function() {
      getPagedList();
    };
    function getPagedList() {

      var pfs = {
        startIndex : ($scope.paging['records'].page - 1) * $scope.paging['records'].pageSize,
        maxResults : $scope.paging['records'].pageSize,
        sortField : $scope.paging['records'].sortField,
        ascending : $scope.paging['records'].sortAscending,
        queryRestriction : $scope.paging['records'].filter ? $scope.paging['records'].filter : ''
      };

      if ($scope.paging['records'].typeFilter) {
        var value = $scope.paging['records'].typeFilter;

        // Handle inactive
        if (value == 'N') {
          pfs.queryRestriction += (pfs.queryRestriction ? ' AND ' : '') + ' workflowStatus:N*';
        } else if (value == 'R') {
          pfs.queryRestriction += (pfs.queryRestriction ? ' AND ' : '') + ' workflowStatus:R*';
        }

      }

      workflowService.findTrackingRecordsForWorkflowBin($scope.selected.project.id,
        $scope.selected.bin.id, pfs).then(
      // Success
      function(data) {
        $scope.lists.records = data.records;
        $scope.lists.records.totalCount = data.totalCount;
      });

    }

    // Bin paging
    $scope.getPagedBins = function() {
      getPagedBins();
    };
    function getPagedBins() {
      $scope.selected.bin = null;
      $scope.pagedBins = utilService.getPagedArray($scope.lists.bins, $scope.paging['bins']);
    }

    // Regenerate single bin
    $scope.regenerateBin = function(bin) {
      
      if (!$scope.selected.project.editingEnabled) {
        window.alert("Bin cannot be regenerated while editing is disabled.");
        return;
      }
    	  // Get confirmation before regenerating
   		   var	 regenerate = confirm('This bin took ' + bin.creationTime/1000 + ' seconds to process last time it was run. \n\n Are you sure you want to regenerate now?');
   		 
   		   if (regenerate == true && bin.creationTime/1000 > 30) {
   		     regenerate = confirm('DO NOT refresh your screen during bin regeneration. \n\n No other operations will be allowed until the bin is complete. \n\n Are you ready to continue?');
   		   }
      if (regenerate == true) {
      		    // send both id and name
    	      workflowService.regenerateBin($scope.selected.project.id, bin.id, bin.name,
    	    	        $scope.selected.config.type).then(
    	    	      // Success
    	    	      function(data) {
    	    	        $scope.getBins($scope.selected.project.id, $scope.selected.config, bin);
    	    	        // Once the bin is finished processing 
                // stop the lookup
                $interval.cancel($scope.lookupInterval);
                $scope.lookupInterval = null;
    	    	      });
    	      
    	      
    	      $scope.startProcessProgressLookup = function(process) { 
            // Start if not already running
            if (!$scope.lookupInterval) {
              $scope.lookupInterval = $interval(function() {
                $scope.refreshProcessProgress(process);
              }, 2000);
            }
          } 
    	      
    	      $scope.startProcessProgressLookup(bin.name);
    	      
    	   
          // Refresh Process progress
          $scope.refreshProcessProgress = function(process) {
                             
            workflowService.getProcessProgress(process, $scope.selected.project.id).then(
            // Success
            function(data) {
              
              // Once bin is finished processing (i.e. process progress returns false), 
              // stop the lookup
              if(data === false){
                $interval.cancel($scope.lookupInterval);
                $scope.lookupInterval = null;
                workflowService.decrementGlassPane();
              }
            })};
    	  }
    };

    // Regenerate bins
    $scope.regenerateBins = function() {

      // Clear out process progress lists
      $scope.binsInProcessProgress = [];
      $scope.binsProcessCompleted = [];
      
      workflowService.clearBins($scope.selected.project.id, $scope.selected.config.type).then(
        // Success
        function(data) {
          workflowService.regenerateBins($scope.selected.project.id, $scope.selected.config.type)
            .then(
              // Success
              function(data) {
                $scope.getBins($scope.selected.project.id, $scope.selected.config,
                  $scope.selected.bin);
                  
              });
          // Start lookup for process progress
          $scope.startProcessProgressLookup = function(binNames) {
            
              // Start if not already running
              if (!$scope.lookupInterval) {
                $scope.lookupInterval = $interval(function() {
                  $scope.refreshProcessProgress(binNames);
                }, 2000);
              }
          }   
          var binNames = [];
          for (var i = 0; i < $scope.lists.bins.length; i++) {
            binNames.push($scope.lists.bins[i].name); 
          }
          $scope.startProcessProgressLookup(binNames);
          
          // Refresh Process progress
          $scope.refreshProcessProgress = function(binNames) {
                             
            workflowService.getBulkProcessProgress($scope.selected.project.id, binNames).then(
            // Success
            function(data) {
              var binsStillInProgress = [];
              for(var i=0; i <  data.strings.length; i++){
                binsStillInProgress.push(data.strings[i]);
              }
              
              var completed = binNames.length - binsStillInProgress.length;
              workflowService.updateGlassPaneMessage('Regenerating bins...  ' + 
                completed + ' out of ' + binNames.length + ' completed.');
              
              // Any time a bin finishes processing, update in-progress and completed lists
              if($scope.binsInProcessProgress.length !== binsStillInProgress.length){
                for(var i=0; i < $scope.binsInProcessProgress.length; i++){
                  if(!binsStillInProgress.includes($scope.binsInProcessProgress[i])){
                    $scope.binsProcessCompleted.push($scope.binsInProcessProgress[i]);
                    
                  }
                }
                
                $scope.binsInProcessProgress = binsStillInProgress;
              }
              
              // Once all bins are finished processing (i.e. the in-progress list comes back empty), 
              // stop the lookup and get the validation results
              if(data.strings.length === 0){
                $interval.cancel($scope.lookupInterval);
                $scope.lookupInterval = null;
                workflowService.updateGlassPaneMessage(null);
                workflowService.decrementGlassPane();
              } 
            });
          }
      });
    };

    // Recompute concept status
    $scope.recomputeConceptStatus = function(updateFlag) {
      workflowService.recomputeConceptStatus($scope.selected.project.id, updateFlag).then(
      // Success
      function(response) {
        $scope.getBins($scope.selected.project.id, $scope.selected.config, $scope.selected.bin);
      });
    };

    // enable/disable
    $scope.toggleEnable = function(bin) {

      workflowService.getWorkflowBinDefinition($scope.selected.project.id, bin.name,
        $scope.selected.config.type).then(
        function(response) {
          var bin = response;
          if (bin.enabled) {
            bin.enabled = false;
          } else {
            bin.enabled = true;
          }
          workflowService.updateWorkflowBinDefinition($scope.selected.project.id, bin).then(
            function(response) {
              $scope.getBins($scope.selected.project.id, $scope.selected.config, bin);
            });
        });
    };

    // remove config
    $scope.removeConfig = function(config) {
      workflowService.removeWorkflowConfig($scope.selected.project.id, config.id).then(
      // Success
      function(response) {
        $scope.getConfigs();
      });
    };

    // remove bin/definition
    $scope.removeBin = function(bin) {
      workflowService.getWorkflowBinDefinition($scope.selected.project.id, bin.name,
        $scope.selected.config.type).then(
        // Success
        function(response) {
          var definition = response;

          workflowService.removeWorkflowBinDefinition($scope.selected.project.id, definition.id)
            .then(
            // Successs
            function(response) {
              $scope.getBins($scope.selected.project.id, $scope.selected.config);
            });
        });
    };

    // Convert date to a string
    $scope.toDate = function(lastModified) {
      return utilService.toDate(lastModified);
    };

    // Save the accorion status
    $scope.saveAccordionStatus = function() {
      console.debug('saveAccordionStatus', $scope.groups);
      $scope.user.userPreferences.properties['workflowGroups'] = JSON.stringify($scope.groups);
      securityService.updateUserPreferences($scope.user.userPreferences);
    };

    // Indicate whether user has permission
    $scope.hasPermissions = function(action) {
      return securityService.hasPermissions(action);
    };

    // Export a workflow config
    $scope.exportWorkflow = function() {
      workflowService.exportWorkflow($scope.selected.project.id, $scope.selected.config.id);
    };

    // Run autofix on a bin
    $scope.runAutofix = function(bin) {
      console.log('Creating autofix process for bin');     	
      workflowService.runAutofix($scope.selected.project.id, bin.id, bin.autofix).then(
    	      // Success
    	      function(data) {
    	        // Go to process page, autofix section
    	          securityService.saveProperty($scope.user.userPreferences, 'processType',
    	          'Autofix');
    	          securityService.saveProperty($scope.user.userPreferences, 'processMode',
                  'Config');
    	    	  tabService.setSelectedTabByLabel('Process');
    	      });
    };

    
    //
    // MODALS
    //

    // Import a workflow
    $scope.openImportWorkflowModal = function() {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/importWorkflow.html',
        controller : 'ImportWorkflowModalCtrl',
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
      // Success
      function(data) {
        if (data) {
          $scope.getConfigs();
        }
      });
    };

    // Add checklist modal
    $scope.openAddChecklistModal = function(bin, clusterType, action, result) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/addChecklist.html',
        backdrop : 'static',
        controller : 'ChecklistModalCtrl',
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
          bin : function() {
            return bin;
          },
          clusterType : function() {
            return clusterType;
          },
          action : function() {
            return action;
          },
          result : function() {
            return result;
          }

        }
      });

      modalInstance.result.then(
      // Success
      function(checklist) {
        // "checklists" accordion should reload
        $scope.selected.refreshCt++;
      });
    };
    
    // Compute checklist modal
    $scope.openComputeChecklistModal = function(bin, clusterType, action) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/createChecklist.html',
        backdrop : 'static',
        controller : 'ChecklistModalCtrl',
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
          bin : function() {
            return bin;
          },
          clusterType : function() {
            return clusterType;
          },
          action : function() {
            return action;
          }

        }
      });

      modalInstance.result.then(
      // Success
      function(checklist) {
        // "checklists" accordion should reload
        $scope.selected.refreshCt++;
      });
    };

    // Add worklist modal
    $scope.openAddWorklistModal = function(bin, clusterType, availableClusterCt) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/addWorklist.html',
        backdrop : 'static',
        controller : 'WorklistModalCtrl',
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
          bin : function() {
            return bin;
          },
          clusterType : function() {
            return clusterType;
          },
          availableClusterCt : function() {
            return availableClusterCt;
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(project) {
        $scope.getBins($scope.selected.project.id, $scope.selected.config, $scope.selected.bin);
        // "worklists" accordion should reload
        $scope.selected.refreshCt++;

      });
    };

    // Add config modal
    $scope.openAddConfigModal = function() {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/editConfig.html',
        controller : 'ConfigModalCtrl',
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
            return 'Add';
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        if (data) {
          $scope.getConfigs();
        }
      });
    };

    // Edit config modal
    $scope.openEditConfigModal = function() {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/editConfig.html',
        controller : 'ConfigModalCtrl',
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
            return 'Edit';
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        if (data) {
          $scope.getConfigs();
        }
      });
    };
    // Edit bin modal
    $scope.openEditBinModal = function(lbin) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/editBin.html',
        controller : 'BinModalCtrl',
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
          bin : function() {
            return lbin;
          },
          action : function() {
            return 'Edit';
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        $scope.getBins($scope.selected.project.id, $scope.selected.config, lbin);
      });
    };

    // Clone bin modal
    $scope.openCloneBinModal = function(lbin) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/editBin.html',
        controller : 'BinModalCtrl',
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
          bin : function() {
            return lbin;
          },
          action : function() {
            return 'Clone';
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        $scope.getBins($scope.selected.project.id, $scope.selected.config);
      });
    };

    // Add bin modal
    $scope.openAddBinModal = function(lbin) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/editBin.html',
        controller : 'BinModalCtrl',
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
          bin : function() {
            return lbin;
          },
          action : function() {
            return 'Add';
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        $scope.getBins($scope.selected.project.id, $scope.selected.config);
      });
    };

    // Open edit epoch modal
    $scope.openEditEpochModal = function(lbin) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/editEpoch.html',
        controller : 'EpochModalCtrl',
        backdrop : 'static',
        resolve : {
          selected : function() {
            return $scope.selected;
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        $scope.getEpoch();
      });
    };

    //
    // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
    //
    $scope.initialize = function() {
      // configure tab
      securityService.saveTab($scope.user.userPreferences, '/workflow');

      $scope.getProjects();
      // Get query types
      projectService.getQueryTypes().then(function(data) {
        $scope.lists.queryTypes = data.strings;
      });
      if ($scope.user.userPreferences.properties['workflowGroups']) {
        var savedWorkflowGroups = JSON
          .parse($scope.user.userPreferences.properties['workflowGroups']);
        angular.copy(savedWorkflowGroups, $scope.groups);
      }
    };

    //
    // Initialization: Check that application is configured
    //
    configureService.isConfigured().then(function(isConfigured) {
      if (!isConfigured) {
        $location.path('/configure');
      } else {
        $scope.initialize();
      }
    });

    // end
  } ]);