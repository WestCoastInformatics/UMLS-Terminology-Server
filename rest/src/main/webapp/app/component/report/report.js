// Report directive
tsApp.directive('report', [
  '$window',
  '$routeParams',
  function($window, $routeParams) {
    return {
      restrict : 'A',
      scope : {
        // selected features
        selected : '=',
        lists : '=',
        // callbacks functions
        callbacks : '='
      },
      templateUrl : 'app/component/report/report.html',
      controller : [
        '$scope',
        'reportService',
        'editService',
        'utilService',
        function($scope, reportService, editService, utilService) {
          // Scope vars
          $scope.config = {
            showHidden : true
          };

          // NOTE: Only applicable to simple edit mode, only for concepts so far
          $scope.removeComponent = function() {
            console.debug('remove component', $scope.callbacks);

            if ($scope.selected.component.type != 'CONCEPT') {
             utilService.setError('Only components of type Concept can be removed at this time')
              return;
            }

            editService.removeConcept($scope.selected.project.id, $scope.selected.component.id)
              .then(function() {
                $scope.selected.component = null;

                // call get component with null value to trigger callback behavior
                $scope.callbacks.getComponent(null);
              });

          }

        } ]
    };
  } ]);
