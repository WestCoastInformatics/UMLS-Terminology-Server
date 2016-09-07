// Finish workflow modal controller
tsApp.controller('FinishWorkflowModalCtrl',
  [
    '$scope',
    '$uibModalInstance',
    'workflowService',
    'utilService',
    'selected',
    'lists',
    'user',
    'worklist',
    function($scope, $uibModalInstance, workflowService, utilService, selected, lists, user,
      worklist) {
      console.debug('Entered finish worklist modal control');

      // Scope
      $scope.worklist = worklist;
      $scope.project = selected.project;
      $scope.projectRole = selected.projectRole;
      $scope.user = user;
      $scope.hours;
      $scope.minutes;
      $scope.errors = [];

      // Finish
      $scope.finish = function() {

        $scope.errors = [];
        if ($scope.hours > 23) {
          $scope.errors.push('Invalid number of hours, > 24')
          return;
        }
        if ($scope.minutes > 59) {
          $scope.errors.push('Invalid number of minutes, > 59')
        }

        var seconds = (($scope.hours * 60 * 60) + ($scope.minutes * 60));
        if ($scope.projectRole == 'AUTHOR') {
          $scope.worklist.authorTime = seconds;
        } else if ($scope.projectRole == 'REVIEWER') {
          $scope.worklist.reviewerTime = seconds;
        }
        // update hours/minutes
        workflowService.updateWorklist($scope.project.id, $scope.worklist).then(

        // Success
        function(data) {
          // finish workflow
          $scope.finishWorkflow(worklist);
        },
        // Error
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
      };

      // finish worklist
      $scope.finishWorkflow = function(worklist) {
        workflowService.performWorkflowAction($scope.project.id, worklist.id, $scope.user.userName,
          $scope.projectRole, 'FINISH').then(
        // Success
        function(data) {
          $uibModalInstance.close();
        },
        // Error
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
      }

      // Dismiss modal
      $scope.cancel = function() {
        $uibModalInstance.dismiss();
      };

      // end
    } ]);
