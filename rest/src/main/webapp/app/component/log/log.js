// Administration controller
tsApp
  .controller(
    'LogModalCtrl',
    [
      '$scope',
      '$http',
      refset,
      project,
      function($scope, $uibModalInstance, refset, project) {
        console.debug('configure LogModalCtrl');


                $scope.errors = [];
                $scope.warnings = [];

                // Get log to display
                $scope.getLog = function() {
                  projectService.getLog(project.id, refset.id).then(
                  // Success
                  function(data) {
                    $scope.log = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
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
              

            } ]);