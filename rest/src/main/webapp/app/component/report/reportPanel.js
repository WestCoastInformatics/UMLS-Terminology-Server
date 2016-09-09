
// report panel
tsApp.directive('reportPanel', [ '$window', '$routeParams', function($window, $routeParams) {
  console.debug('configure reportPanel directive');
  return {
    restrict : 'A',
    scope : {
      selected : '=',
      lists : '=',
      user : '=',
      paging : '='
    },
    templateUrl : 'app/component/report/reportPanel.html',
    controller : [
                  '$scope',
                  '$window',
                  'reportService',
                  'utilService',
                  function($scope, $window, reportService, utilService) {

                    // Scope vars
                    $scope.report = '';
                    $scope.mode = 'Static';
                    $scope.windows = {};

                    // watch the component, generate the report
                    $scope.$watch('selected.concept', function() {
                      if ($scope.selected.concept) {
                        reportService.getConceptReport($scope.selected.project.id, $scope.selected.concept.id)
                          .then(
                          // Success
                          function(data) {
                            $scope.report = data;
                          });
                      }
                    });

                    // open report window
                    $scope.openReportWindow = function() {

                      var newUrl = utilService.composeUrl('report');

                      var reportWin =  $window.open(newUrl, 'reportWindow', 'width=600, height=600');
                      reportWin.document.title = 'Report';
                      reportWin.focus();
                    };

                  } ]

  };
}

]);
