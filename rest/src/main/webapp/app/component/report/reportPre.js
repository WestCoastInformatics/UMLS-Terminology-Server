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
    controller : [ '$scope', '$sce', 'reportService', function($scope, $sce, reportService) {

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
      
      // Trust as HTML
      $scope.getX = function() {
        return $sce.trustAsHtml($scope.report);
      }
    } ]
  };
} ]);
