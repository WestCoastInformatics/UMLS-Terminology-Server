// Report directive
tsApp.directive('report', [ '$window', '$routeParams', function($window, $routeParams) {
  console.debug('configure report directive');
  return {
    restrict : 'A',
    scope : {
      // selected features
      selected : '=',
      // callbacks functions
      callbacks : '='
    },
    templateUrl : 'app/component/report/report.html',
    controller : [ '$scope', 'reportService', function($scope, reportService) {
      // Scope vars
      $scope.showHidden = true;

    } ]
  };
} ]);
