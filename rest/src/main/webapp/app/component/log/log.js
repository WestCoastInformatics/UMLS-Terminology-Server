// Log controller
tsApp.directive('log', [ function() {
  console.debug('configure log directive');
  return {
    restrict : 'A',
    scope : {
      project : '='
    },
    templateUrl : 'app/component/log/log.html',
    controller : [ '$scope', '$uibModal', 'projectService', 'utilService',
      function($scope, $uibModal, projectService, utilService) {
        console.debug('configure LogModalCtrl');

        // Log modal
        $scope.openLogModal = function() {
          console.debug('openLogModal ');

          var modalInstance = $uibModal.open({
            templateUrl : 'app/component/log/logModal.html',
            controller : LogModalCtrl,
            backdrop : 'static',
            size : 'lg',
            resolve : {

              project : function() {
                return $scope.project;
              }
            }
          });

          // NO need for result function - no action on close
          // modalInstance.result.then(function(data) {});
        };
        var LogModalCtrl = function($scope, $uibModalInstance, project) {

          $scope.errors = [];
          $scope.warnings = [];

          // Get log to display
          $scope.getLog = function() {
            projectService.getLog(project.id).then(
            // Success
            function(data) {
              $scope.log = data;
            },
            // Error
            function(data) {
              utilService.handleDialogError($scope.errors, data);
            });

          };

          // Dismiss modal
          $scope.close = function() {
            // nothing changed, don't pass a refset
            $uibModalInstance.close();
          };

          // initialize
          $scope.getLog();
        };
      } ]
  };
} ]);