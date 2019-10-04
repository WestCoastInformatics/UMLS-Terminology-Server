// Process controller
tsApp
  .controller(
    'InversionCtrl',
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
      'inversionService',
      'metadataService',
      function($scope, $location, $uibModal, $timeout, configureService, gpService, tabService,
        utilService, securityService, projectService, inversionService, metadataService) {
        console.debug('configure InversionCtrl');

        // Set up tabs and controller
        tabService.setShowing(true);
        utilService.clearError();
        $scope.user = securityService.getUser();
        projectService.getUserHasAnyRole();
        tabService.setSelectedTabByLabel('Inversion');

        $scope.vsab = '';
        
        // Lists
        $scope.lists = {
          projects : [],
          projectRoles : []
        }

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
        // Set the project
        $scope.setProject = function(project) {
          $scope.selected.project = project;

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
        
        // Convert date to a string
        $scope.toDate = function(lastModified) {
          return utilService.toDate(lastModified);
        };
        
        // Request range modal
        $scope.openRequestRangeModal = function(vsab) {
          if (!vsab) {
            window.alert('Versioned Source Abbreviation must be set before requesting a new range. ');
            return;
          }
          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/inversion/requestRange.html',
            backdrop : 'static',
            controller : 'SourceIdRangeModalCtrl',
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
              vsab : function() {
                return vsab;
              },
              action : function() {
                return 'Add';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(sourceIdRange) {
            $scope.entry = sourceIdRange;
            $scope.entry.numberOfIds = $scope.entry.endSourceId - $scope.entry.beginSourceId + 1;
          });
        };
        
        // Request range modal
        $scope.openSubmitRangeUpdateModal = function(vsab) {
          if (!vsab) {
            window.alert('Versioned Source Abbreviation must be set before updating the range. ');
            return;
          }
          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/inversion/requestRange.html',
            backdrop : 'static',
            controller : 'SourceIdRangeModalCtrl',
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
              vsab : function() {
                return vsab;
              },
              action : function() {
                return 'Update';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(sourceIdRange) {
            $scope.entry = sourceIdRange;
            $scope.entry.numberOfIds = $scope.entry.endSourceId - $scope.entry.beginSourceId + 1;         
          });
        };
        
        $scope.search = function(vsab) {
          if (!vsab) {
            window.alert('Source Abbreviation and Version must be set before retrieving the range. ');
            return;
          }
          inversionService.getSourceIdRange($scope.selected.project.id, vsab).then(
          // Success
          function(data) {
            $scope.entry = data;
            $scope.entry.numberOfIds = $scope.entry.endSourceId - $scope.entry.beginSourceId + 1;
          },
          // Error
          function(data) {
            $scope.errors[0] = data;
            utilService.clearError();
          });      
        }
        
        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {
          // configure tab
          securityService.saveTab($scope.user.userPreferences, '/inversion');
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