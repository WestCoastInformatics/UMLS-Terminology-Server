// Log directive
tsApp.directive('log', [ function() {
  console.debug('configure log directive');
  return {
    restrict : 'A',
    scope : {
      selected : '=',
      type : '@',
      lines : '@',
      poll : '@'
    },
    templateUrl : 'app/actions/log/log.html',
    controller : [
      '$scope',
      '$uibModal',
      '$interval',
      '$location',
      '$anchorScroll',
      'utilService',
      'projectService',
      'workflowService',
      'processService',
      function($scope, $uibModal, $interval, $location, $anchorScroll, utilService, projectService,
        workflowService, processService) {
        console.debug('configure LogDirective', $scope.selected);

        // default lines of 100
        if (!$scope.lines) {
          $scope.lines = 100;
        }

        // Log modal
        $scope.openLogModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/actions/log/logModal.html',
            controller : LogModalCtrl,
            backdrop : 'static',
            size : 'lg',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              type : function() {
                return $scope.type;
              },
              poll : function() {
                return $scope.poll;
              }
            }
          });

          // NO need for result function - no action on close
          modalInstance.result.then(
          // success
          function(data) {
          },
          // dismiss
          function() {
            if ($scope.pollInterval) {
              $interval.cancel($scope.pollInterval);
            }

          });
        };
        var LogModalCtrl = function($scope, $window, $uibModalInstance, selected, type, poll) {
          $scope.type = type;
          $scope.poll = poll;
          $scope.errors = [];
          $scope.warnings = [];

          // Get log to display
          $scope.getLog = function() {

            if (type == 'Worklist' || type == 'Checklist') {
              var checklistId = (type == 'Checklist' ? selected.worklist.id : null);
              var worklistId = (type == 'Worklist' ? selected.worklist.id : null);
              // Make different calls depending upon the object type
              workflowService.getLog(selected.project.id, checklistId, worklistId).then(
              // Success
              function(data) {
                $scope.log = data;
              },
              // Error
              function(data) {
                utilService.handleDialogError($scope.errors, data);
              });

            }

            else if (type == 'Process') {
              processService.getProcessLog(selected.project.id, selected.process.id).then(
              // Success
              function(data) {
                $scope.log = data;
              },
              // Error
              function(data) {
                utilService.handleDialogError($scope.errors, data);
              });
            }

            else if (type == 'Step') {
              processService.getAlgorithmLog(selected.project.id, selected.step.id).then(
              // Success
              function(data) {
                $scope.log = data;
              },
              // Error
              function(data) {
                utilService.handleDialogError($scope.errors, data);
              });
            }

            // Project/component
            else if (type == 'Project' || type == 'Concept' || type == 'Descriptor'
              || type == 'Code') {

              // Make different calls depending upon the object type
              var objectId = (type == 'Project' ? null : selected.component.id);
              projectService.getLog(selected.project.id, objectId).then(
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

            $location.hash('bottom');
            $anchorScroll();
          };

          // Close modal
          $scope.close = function() {
            if ($scope.pollInterval) {
              $interval.cancel($scope.pollInterval);
            }

            // nothing changed, don't pass a refset
            $uibModalInstance.close();
          };

          // initialize
          $scope.getLog();
          // because of injection, this is evaulated as a string
          if ($scope.poll == 'true') {
            $scope.pollInterval = $interval(function() {
              $scope.getLog();
            }, 1000);
          }
        };

      } ]

  };
} ]);