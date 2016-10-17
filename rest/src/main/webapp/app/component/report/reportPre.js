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
        console.debug('selected.component1', $scope.selected.component, $scope.selected.project);
        if ($scope.selected.component) {
          $scope.getReport($scope.selected.component);
        }
      });

      // Trust as HTML
      $scope.getTrustedReport = function() {
        return $sce.trustAsHtml($scope.report);
      };

      // Get the report
      $scope.getReport = function(component) {
        $scope.report = "Loading...";
        reportService.getComponentReport($scope.selected.project ?
          $scope.selected.project.id : null, component).then(
        // Success
        function(data) {
          $scope.report = data;
        });

      }

    } ]
  };
} ]);
