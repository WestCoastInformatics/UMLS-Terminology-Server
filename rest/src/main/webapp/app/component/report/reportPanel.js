// report panel
tsApp.directive('reportPanel', [
  '$window',
  '$routeParams',
  function($window, $routeParams) {
    console.debug('configure reportPanel directive');
    return {
      restrict : 'A',
      scope : {
        selected : '=',
        callbacks : '='
      },
      templateUrl : 'app/component/report/reportPanel.html',
      controller : [ '$scope', '$window', 'reportService', 'utilService',
        function($scope, $window, reportService, utilService) {

          // Scope vars
          $scope.report = null;
          $scope.mode = 'Static';

          // open report window
          $scope.openReportWindow = function() {
            reportService.popout($scope.selected.component);
          };

        } ]

    };
  }

]);
