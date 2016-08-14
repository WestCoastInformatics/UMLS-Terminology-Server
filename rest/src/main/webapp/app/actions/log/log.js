// Log directive
tsApp.directive('log', [ function() {
  console.debug('configure log directive');
  return {
    restrict : 'A',
    scope : {
      project : '=',
      object : '=',
      type : '@',
      lines : '@'
    },
    templateUrl : 'app/actions/log/log.html',
    controller : [ '$scope', '$uibModal', 'utilService', 'projectService', 'workflowService',
      function($scope, $uibModal, utilService, projectService, workflowService) {
        console.debug('configure LogModalCtrl');

        // Log modal
        $scope.openLogModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/actions/log/logModal.html',
            controller : LogModalCtrl,
            backdrop : 'static',
            size : 'lg',
            resolve : {
              project : function() {
                return $scope.project;
              },
              object : function() {
                return $scope.object;
              },
              type : function() {
                return $scope.type;
              },
            }
          });

          // NO need for result function - no action on close
          // modalInstance.result.then(function(data) {});
        };
        var LogModalCtrl = function($scope, $uibModalInstance, project, object, type) {

          $scope.errors = [];
          $scope.warnings = [];

          // Get log to display
          $scope.getLog = function() {

            if (type == 'Worklist' || type == 'Checklist') {
              var checklistId = (type == 'Checklist' ? object.id : null);
              var worklistId = (type == 'Worklist' ? object.id : null);
              // Make different calls depending upon the object type
              workflowService.getLog(project.id, checklistId, worklistId).then(
              // Success
              function(data) {
                $scope.log = data;
              },
              // Error
              function(data) {
                utilService.handleDialogError($scope.errors, data);
              });

            }

            // Project/concept
            else if (type == 'Project' || type == 'Concept') {

              // Make different calls depending upon the object type
              projectService.getLog(project.id, (object == null ? null : object.id)).then(
              // Success
              function(data) {
                $scope.log = data;
              },
              // Error
              function(data) {
                utilService.handleDialogError($scope.errors, data);
              });
            }

            // fail
            else {
              $scope.errors.push('Invalid type passed to log modal controller - ' + type);
            }
          };

          // Close modal
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