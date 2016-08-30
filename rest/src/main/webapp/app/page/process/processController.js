// Process controller
tsApp.controller('ProcessCtrl', [
  '$scope',
  '$location',
  '$uibModal',
  'configureService',
  'tabService',
  'utilService',
  'securityService',
  'processService',
  function($scope, $location, $uibModal, configureService, tabService, utilService,
    securityService, processService) {
    console.debug("configure ProcessCtrl");

    // Set up tabs and controller
    tabService.setShowing(true);
    utilService.clearError();
    $scope.user = securityService.getUser();
    projectService.getUserHasAnyRole();
    tabService.setSelectedTabByLabel('Process');

    // Selected variables
    $scope.selected = {
      project : null,
      projectRole : null,
      process : null,
      algorithm : null,
      mode : 'config' // vs 'exec'
    };

    // Lists
    $scope.lists = {
      processes : [],
      algorithms : [],
      projects : [],
      projectRoles : [],
      modes : [ 'Configure', 'Execute' ]
    }

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
    $scope.paging['process'].callback = {
      getPagedList : getProcesses
    };

    $scope.paging['algo'] = utilService.getPaging();
    $scope.paging['algo'].sortField = 'lastModified';
    $scope.paging['algo'].callback = {
      getPagedList : getAlgorithms
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
      $scope.selected.mode = mode;
      $scope.getProcesses();
    }

    // Get $scope.lists.processes
    // switch based on mode
    $scope.getProcesses = function(process) {
      getProcesss(process);
    }
    function getProcesss(process) {
      if ($scope.selected.mode == 'config') {
        $scope.getProcessConfigs();
      } else if ($scope.selected.mode == 'exec') {
        $scope.getProcessExecutions();
      }
      if (process) {
        $scope.setProcess(process);
      }
    }

    // Set $scope.selected.process
    $scope.setProcess = function(process) {
      $scope.selected.process = process;
    }

    // Get $scope.lists.algorithms
    // switch based on mode
    $scope.getAlgorithms = function(algorithm) {
      getAlgorithms(algorithm);
    }
    function getAlgorithms(algorithm) {
      if ($scope.selected.mode == 'config') {
        $scope.getAlgorithmConfigs();
      } else if ($scope.selected.mode == 'exec') {
        $scope.getAlgorithmExecutions();
      }
      if (algorithm) {
        $scope.setAlgorithm(algorithm);
      }
    }

    // Set $scope.selected.algorithm
    $scope.setAlgorithm = function(algorithm) {
      $scope.selected.algorithm = algorithm;
    }

    // Convert date to a string
    $scope.toDate = function(lastModified) {
      return utilService.toDate(lastModified);
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

    //
    // MODALS
    //

    // TBD

    //
    // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
    //
    $scope.initialize = function() {
      // configure tab
      securityService.saveTab($scope.user.userPreferences, '/process');
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