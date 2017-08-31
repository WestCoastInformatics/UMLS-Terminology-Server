// Edit user modal controller
tsApp.controller('EditUserModalCtrl', [ '$scope', '$uibModalInstance', 'securityService', 'user', 'loggedUser',
  'applicationRoles', 'action',
  function($scope, $uibModalInstance, securityService, user, loggedUser, applicationRoles, action) {
    // Scope vars
	$scope.action = action;
    $scope.applicationRoles = applicationRoles;
    $scope.user = (user ? user : {
      applicationRole : applicationRoles[0]
    });
    $scope.errors = [];

    // those without application admin roles, can't assign admin
    // roles to themselves or others
    if (loggedUser.applicationRole != 'ADMINISTRATOR') {
      var index = $scope.applicationRoles.indexOf('ADMINISTRATOR');
      if(index != -1){
        $scope.applicationRoles.splice(index, 1);
      }
    }

    $scope.submitUser = function(user) {
      $scope.errors = [];
      var fn = 'addUser';
      if ($scope.action == 'Edit') {
        fn = 'updateUser';
      }
      if (!user || !user.name || !user.userName || !user.applicationRole) {
        $scope.errors.push('The name, user name, and application role fields cannot be blank. ');
        return;
      }
      securityService[fn](user).then(
      // Success
      function(data) {
        $uibModalInstance.close(data);
      },
      // Error
      function(data) {
        $scope.errors[0] = data;
        utilService.clearError();
      });
    }

    // Dismiss the modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // end
  } ]);