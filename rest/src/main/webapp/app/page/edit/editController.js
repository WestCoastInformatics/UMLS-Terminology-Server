// Edit controller
tsApp.controller('EditCtrl', [
  '$scope',
  '$http',
  '$location',
  'gpService',
  'utilService',
  'tabService',
  'securityService',
  'workflowService',
  'utilService',
  'configureService',
  'projectService',
  'reportService', 
  '$uibModal',
  function($scope, $http, $location, gpService, utilService, tabService, securityService,
    workflowService, utilService, configureService, projectService, reportService, $uibModal) {
    console.debug("configure EditCtrl");

    // Clear error
    utilService.clearError();

    // Handle resetting tabs on "back" button
    tabService.setSelectedTabByLabel('Edit');

    $scope.user = securityService.getUser();
    $scope.projectRole;
    $scope.binTypeOptions = []; 
    $scope.currentBinType = 'MUTUALLY_EXCLUSIVE';
    // TODO: figure out how to boostrap 
    $scope.currentProject = {id : 1239500};
    $scope.recordTypes = [ 'N', 'R' ];
    $scope.projects;
    $scope.selected = {
      bin : null,
      clusterType : null
    };

    // Paging variables
    $scope.visibleSize = 4;
    $scope.pageSize = 10;
    $scope.paging = {};
    $scope.paging['worklist'] = {
      page : 1,
      filter : '',
      typeFilter : '',
      sortField : 'name',
      ascending : true
    };
    
    // Configure tab and accordion
    $scope.configureTab = function() {
      $scope.user.userPreferences.lastTab = '/edit';
      //securityService.updateUserPreferences($scope.user.userPreferences);
    };

   // Workflow Bins Changed handler
    $scope.$on('workflow:workflowBinsChanged', function(event, data) {
      console.debug('on workflow:workflowBinsChanged', data);
      $scope.getBins($scope.currentProject.id, $scope.currentBinType);
    });
    
    //
    // Initialize
    //

    $scope.initialize = function() {

      // Handle users with user preferences
      if ($scope.user.userPreferences) {
        $scope.configureTab();
      }
      
      projectService.getProjects().then(
        // success
        function(data) {

          $scope.projects = data.projects;
          $scope.currentProject = $scope.projects[0];
          $scope.projectRole = $scope.currentProject.userRoleMap[$scope.user.userName];
          if ($scope.projectRole == 'ADMINISTRATOR') {
            $scope.roleOptions = [ 'ADMINISTRATOR', 'REVIEWER', 'AUTHOR' ];
          } else if ($scope.projectRole == 'REVIEWER') {
            $scope.roleOptions = [ 'REVIEWER', 'AUTHOR' ];
          } else if ($scope.projectRole == 'AUTHOR') {
            $scope.roleOptions = [ 'AUTHOR' ];
          }
          
          $scope.getAvailableWorklists($scope.currentProject.id);
        });
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

    
    // Retrieve all bins with project and type
    $scope.getAvailableWorklists = function(projectId) {
      console.debug('getAvailableWorklists', projectId);

      var pfs = {
        startIndex : ($scope.paging['worklist'].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging['worklist'].sortField,
        ascending : $scope.paging['worklist'].ascending == null ? false
          : $scope.paging['worklist'].ascending,
        queryRestriction : $scope.paging['worklist'].filter != undefined && 
          $scope.paging['worklist'].filter != "" ? $scope.paging['worklist'].filter
          : null
      };
      
      workflowService.findAvailableWorklists(projectId, $scope.user.userName, pfs).then(function(response) {
        $scope.worklists = response.worklists;
        $scope.worklists.totalCount = $scope.worklists.length;
      });
    };

    // Set the project
    $scope.setProject = function(project) {
      $scope.currentProject = project;
      projectService.fireProjectChanged($scope.currentProject);
      //$scope.user.userPreferences.project = $scope.currentProject;
      //securityService.updateUserPreferences($scope.user.userPreferences);
      //$scope.getProjects();

      $scope.getBins($scope.currentProject.id, $scope.currentBinType);
    }
    
    // Retrieve all projects
    $scope.getProjects = function() {
      console.debug('getProjects');

      projectService.getProjects().then(function(response) {
        $scope.projects = response;
        $scope.currentProject = $scope.projects.projects[0];

        $scope.getBins($scope.currentProject.id, $scope.currentBinType);
      });
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
      reportService.getConceptReport($scope.currentProject.id, $scope.selected.concept.id).then(
      // Success
      function(data) {
        $scope.selected.concept.report = data;
      });
    };


    // Convert date to a string
    $scope.toDate = function(lastModified) {
      return utilService.toDate(lastModified);
    };
    
    // link to error handling
    function handleError(errors, error) {
      utilService.handleDialogError(errors, error);
    }
    
    //
    // MODALS
    //


  } ]);