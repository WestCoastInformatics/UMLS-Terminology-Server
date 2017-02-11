// Report directive
tsApp.directive('report', [ '$window', '$routeParams', function($window, $routeParams) {
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
    controller : [ '$scope', 'reportService', function($scope, reportService) {
      // Scope vars
      $scope.config = {
        showHidden : true
      };

      $scope.removeConcept = function() {
        if ($scope.callbacks.hasOwnProperty('removeConcept')) {
          $scope.callbacks.removeConcept($scope.selected.project.id, concept.id).then(function() {
            // do nothing
          })
        } else {
          editService.removeConcept($scope.selected.project.id, concept.id).then(function() {
            $scope.selected.component = null;
          });
        }
      }

    } ]
  };
} ]);
