// Worklist modal controller
tsApp
  .controller(
    'WorklistModalCtrl',
    [
      '$scope',
      '$uibModalInstance',
      'utilService',
      'workflowService',
      'selected',
      'lists',
      'user',
      'bin',
      'clusterType',
      'availableClusterCt',
      function($scope, $uibModalInstance, utilService, workflowService, selected, lists, user, bin,
        clusterType, availableClusterCt) {
        console.debug("configure WorklistModalCtrl", bin, clusterType, availableClusterCt);

        // Scope vars
        $scope.bin = bin;
        $scope.clusterType = clusterType;
        $scope.availableClusterCt = availableClusterCt;

        $scope.user = user;
        $scope.skipClusterCt = 0;
        $scope.numberOfWorklists = 1;
        $scope.worklistsCompleted = 0;
        $scope.completionMessage = '';
        $scope.clusterCtOptions = [ 20, 50, 100, 200, 500 ];
        $scope.errors = [];

        // Create the worklist
        $scope.createWorklist = function(clusterCt, skipClusterCt, numberOfWorklists) {

          if (isNaN(numberOfWorklists) || numberOfWorklists < 1) {
            window.alert('Please enter the Number of Worklists and confirm that it is an integer.');
            return;
          }
          if (numberOfWorklists > 10
            && !$window.confirm('Are you sure you want to create ' + numberOfWorklists
              + ' worklists?')) {
            return;
          }
          if (isNaN(clusterCt) || clusterCt > 1000) {
            window
              .alert('Please enter the Cluster Count and confirm that it is an integer less than or equal to 1000.');
            return;
          }
          if (isNaN(skipClusterCt) || skipClusterCt < 0) {
            window
              .alert('Please enter the Start Index and confirm that it is an integer greater or equal to 0.');
            return;
          }
          if (numberOfWorklists * clusterCt > $scope.availableClusterCt) {
            window.alert('You have requested to make ' + (numberOfWorklists * clusterCt)
              + ' clusters.  There are only ' + $scope.availableClusterCt
              + ' clusters available.  Please revise your selections.');
            return;
          }
          createWorklistHelper(clusterCt, skipClusterCt, numberOfWorklists);
        }

        // Helper function for multiple worklists
        function createWorklistHelper(clusterCt, skipClusterCt, numberOfWorklists) {
          var pfs = {
            maxResults : clusterCt,
            startIndex : skipClusterCt
          };
          // Create worklist
          workflowService.createWorklist($scope.selected.project.id, bin.id, clusterType, pfs)
            .then(
            // Success
            function(data) {
              workflowService.fireWorklistChanged(data);
              $scope.completionMessage += data.name;
              $scope.completionMessage += ' completed. \n';
              $scope.worklistsCompleted++;
              if ($scope.worklistsCompleted < $scope.numberOfWorklists) {
                createWorklistHelper(clusterCt, skipClusterCt, numberOfWorklists);
              } else {
                $uibModalInstance.close();
              }
            },
            // Error
            function(data) {
              $scope.errors[0] = data;
              utilService.clearError();
            });
        }
        ;

        $scope.cancel = function() {
          $uibModalInstance.dismiss('cancel');
        };

        // end
      } ]);