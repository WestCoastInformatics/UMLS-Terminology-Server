// Assign worklist controller
var AssignWorklistModalCtrl = function($scope, $uibModalInstance, $sce, workflowService, utilService, securityService, worklist, action,
  currentUser, project) {
  console.debug('Entered assign worklist modal control', worklist.id, action, currentUser, project.id);
  $scope.worklist = worklist;
  $scope.action = action;
  $scope.project = project;
  $scope.prospectiveUsers = Object.keys(project.userRoleMap);
  $scope.assignedUsers = [];
  $scope.allUsers = [];
  $scope.user = currentUser;
  $scope.note;
  $scope.errors = [];

  
    securityService.getUsers().then(
    // Success
    function(data) {
      $scope.allUsers = data.users;
      // if project.isTeamBased(), then restrict list to users with a team matching 
      // the worklist 
      if ($scope.project.teamBased) {
        for (var i = 0; i < $scope.allUsers.length; i++) {
          if ($scope.allUsers[i].team == $scope.worklist.team && 
              $scope.prospectiveUsers.indexOf($scope.allUsers[i].userName)) {
            $scope.assignedUsers.push($scope.allUsers[i]);
          }
        }
      } else {
        for (var i = 0; i < $scope.allUsers.length; i++) {
          if ($scope.prospectiveUsers.indexOf($scope.allUsers[i].userName)) {
            $scope.assignedUsers.push($scope.allUsers[i]);
          }
        }
      }
      $scope.assignedUsers = $scope.assignedUsers.sort(utilService.sort_by('userName'));
    },
    // Error
    function(data) {
      handleError($scope.errors, data);
    });
  

  // Assign (or reassign)
  $scope.assignWorklist = function() {
    if (!$scope.user) {
      $scope.errors[0] = 'The user must be selected. ';
      return;
    }

    if (action == 'ASSIGN') {
      workflowService.performWorkflowAction($scope.project.id, worklist.id, $scope.user.userName,
        $scope.project.userRoleMap[$scope.user.userName], 'ASSIGN').then(
      // Success
      function(data) {

        // Add a note as well
        if ($scope.note) {
          workflowService.addWorklistNote($scope.project.id, worklist.id, $scope.note).then(
          // Success
          function(data) {
            $uibModalInstance.close(worklist);
          },
          // Error
          function(data) {
            handleError($scope.errors, data);
          });
        }
        
        // If user has a team, update worklist
        securityService.getUserByName($scope.user).then(

          // Success
          function(data) {
            $scope.user = data;
            if ($scope.user.team) {
              worklist.team = $scope.user.team;
            }
            $uibModalInstance.close(worklist);
          },
          // Error
          function(data) {
            handleError($scope.errors, data);
          });
        


      },
      // Error
      function(data) {
        //handleError($scope.errors, data);
        $uibModalInstance.close();
      });
    }

  }

  // Dismiss modal
  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

};