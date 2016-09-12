// Preformatted report 
tsApp.directive('reportPre', [ '$window', '$routeParams', function($window, $routeParams) {
  console.debug('configure reportPre directive');
  return {
    restrict : 'A',
    scope : {
      selected : '=',
      callbacks : '='
    },
    templateUrl : 'app/component/report/reportPre.html',
    controller : [ '$scope', 'reportService', function($scope, reportService) {

      // Scope vars
      $scope.report = null;

      // watch component, generate the report
      $scope.$watch('selected.component', function() {
        if ($scope.selected.component) {
          $scope.getReport($scope.selected.component);
        }
      });

      // Get the report
      $scope.getReport = function(component) {
        reportService.getComponentReport(component).then(
        // Success
        function(data) {
          $scope.report = data;
        });
      }
    } ]
  };
} ]);
