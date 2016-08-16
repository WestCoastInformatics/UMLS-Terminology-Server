// Preformatted report 
tsApp.directive('reportPre', [
  '$window',
  '$routeParams',
  function($window, $routeParams) {
    console.debug('configure reportPre directive');
    return {
      restrict : 'A',
      scope : {
        selected : '=',
        lists : '=',
        user : '=',
        paging : '='
      },
      templateUrl : 'app/component/report/reportPre.html',
      controller : [
        '$scope',
        'reportService',
        function($scope, reportService) {

          // Scope vars
          $scope.report = '';

          // watch the component, generate the report
          $scope.$watch('selected.concept', function() {
            if ($scope.selected.concept) {
              reportService
                .getConceptReport($scope.selected.project.id, $scope.selected.concept.id).then(
                // Success
                function(data) {
                  $scope.report = data;
                });
            }
          });

        } ]
    };
  } ]);
