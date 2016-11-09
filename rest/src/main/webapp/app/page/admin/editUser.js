// Edit user modal controller
tsApp.controller('EditUserModalCtrl', [ '$scope', '$uibModalInstance', 'securityService', 'user',
  'applicationRoles', 'action',
  function($scope, $uibModalInstance, securityService, user, applicationRoles, action) {
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
      var fn = 'addUser';
      if ($scope.action == 'Edit') {
        fn = 'updateUser';
      }
      if (!user || !user.name || !user.userName || !user.applicationRole) {
        window.alert('The name, user name, and application role fields cannot be blank. ');
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