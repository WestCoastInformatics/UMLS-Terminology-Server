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

        $scope.sab = '';
        $scope.version = '';
        
        // Lists
        $scope.lists = {
          projects : [],
          projectRoles : []
        }

        // Request range modal
        $scope.openRequestRangeModal = function(sab, version) {

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
              sab : function() {
                return sab;
              },
              version : function() {
                return version;
              },
              action : function() {
                return 'Add';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(sourceIdRange) {
            

          });
        };
        
        // Request range modal
        $scope.openSubmitRangeUpdateModal = function(sab, version) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/inversion/submitRangeUpdate.html',
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
              sab : function() {
                return sab;
              },
              version : function() {
                return version;
              },
              action : function() {
                return 'Update';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(sourceIdRange) {
            

          });
        };
        
        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {
          // configure tab
          securityService.saveTab($scope.user.userPreferences, '/inversion');
          //$scope.getProjects();

          // Get all terminologies
          metadataService.getTerminologies().then(
          // Success
          function(data) {
            $scope.lists.terminologies = data.terminologies;
          });
          
          /*inversionService.removeSourceIdRange(1).then(
            function(data) {
              
            });*/
          inversionService.getSourceIdRange(39751, "MTH", "2018AB").then(
            // Success
            function(data) {
             
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