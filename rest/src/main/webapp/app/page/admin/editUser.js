// Edit user modal controller
tsApp.controller('EditUserModalCtrl', [ '$scope', '$uibModalInstance', 'user', 'applicationRoles',
  'action', function($scope, $uibModalInstance, user, applicationRoles, action) {

    // Scope vars
    $scope.action = action;
    $scope.applicationRoles = applicationRoles;
    $scope.user = (user ? user : {
      applicationRole : applicationRoles[0]
    });
    $scope.errors = [];

    // those without application admin roles, can't give themselves admin
    // roles
    if (user && user.applicationRole != 'ADMINISTRATOR') {
      var index = $scope.applicationRoles.indexOf('ADMINISTRATOR');
      $scope.applicationRoles.splice(index, 1);
    }

    $scope.submitUser = function(user) {
      if ($scope.action == 'Add') {
        $scope.addUser(user);
      } else if ($scope.action == 'Edit') {
        $scope.updateUser(user);
      }
    }
    // Add user
    $scope.addUser = function(user) {
      if (!user || !user.name || !user.userName || !user.applicationRole) {
        window.alert('The name, user name, and application role fields cannot be blank. ');
        return;
      }
      securityService.addUser(user).then(
      // Success
      function(data) {
        $uibModalInstance.close(data);
      },
      // Error
      function(data) {
        $scope.errors[0] = data;
        utilService.clearError();
      });

    };

    // Save the user
    $scope.updateUser = function(user) {

      if (!user || !user.name || !user.userName || !user.applicationRole) {
        window.alert('The name, user name, and application role fields cannot be blank. ');
        return;
      }

      securityService.updateUser(user).then(
      // Success
      function(data) {
        $uibModalInstance.close(data);
      },
      // Error
      function(data) {
        $scope.error[0] = data;
        utilService.clearError();
      });
    };

    // Dismiss the modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // end
  } ]);