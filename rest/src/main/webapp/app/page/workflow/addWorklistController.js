    // Create worklist controller
    var CreateWorklistModalCtrl = function($scope, $uibModalInstance, $window, workflowService, utilService, 
      user, projectId, bin, clusterType, availableClusterCt) {

      $scope.bin = bin;
      $scope.clusterType = clusterType;

      $scope.user = user;
      $scope.errors = [];
      $scope.skipClusterCt = 0;
      $scope.availableClusterCt = availableClusterCt;
      $scope.numberOfWorklists = 1;
      $scope.worklistsCompleted = 0;
      $scope.completionMessage = '';
      
      
      var item1 = {index : 20 };
      var item2 = {index : 50 };
      var item3 = {index : 100 };
      var item4 = {index : 200 };
      $scope.clusterCtOptions = [item1, item2, item3, item4];


      // Create the worklist
      $scope.createWorklist = function(clusterCt, skipClusterCt, numberOfWorklists) {
        if (isNaN(numberOfWorklists) || numberOfWorklists < 1) {
          window.alert('Please enter the Number of Worklists and confirm that it is an integer.');
          return;
        }
        if (numberOfWorklists > 10
          && !$window
            .confirm('Are you sure you want to create ' + numberOfWorklists + ' worklists?')) {
              return;
        }
        if (isNaN(clusterCt) || clusterCt > 1000) {
          window.alert('Please enter the Cluster Count and confirm that it is an integer less than 1000.');
          return;
        }
        if (isNaN(skipClusterCt) || skipClusterCt < 0) {
          window.alert('Please enter the Start Index and confirm that it is an integer greater or equal to 0.');
          return;
        }
        if (numberOfWorklists * clusterCt > $scope.availableClusterCt) {
          window.alert('You have requested to make ' + (numberOfWorklists * clusterCt) + ' clusters.  There are only ' 
            + $scope.availableClusterCt + ' clusters available.  Please revise your selections.');
          return;
        }
        
        createWorklistHelper(clusterCt, skipClusterCt, numberOfWorklists);
      }
      
      function createWorklistHelper(clusterCt, skipClusterCt, numberOfWorklists) {
        var pfs = {
          maxResults : clusterCt,
          startIndex : skipClusterCt
        };
        // Create worklist 
        workflowService.createWorklist(projectId, bin.id, clusterType,
          pfs).then(
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
      };

      $scope.cancel = function() {
        $uibModalInstance.dismiss('cancel');
      };

    };