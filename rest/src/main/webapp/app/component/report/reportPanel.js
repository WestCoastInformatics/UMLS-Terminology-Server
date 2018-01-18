// report panel
tsApp.directive('reportPanel', [
  '$window',
  '$routeParams',
  function($window, $routeParams) {
    return {
      restrict : 'A',
      scope : {
        selected : '=',
        callbacks : '='
      },
      templateUrl : 'app/component/report/reportPanel.html',
      controller : [ '$scope', '$window', 'reportService', 'utilService', 'securityService',
        function($scope, $window, reportService, utilService, securityService) {

          // Scope vars
          $scope.report = null;
          $scope.user = securityService.getUser();
          $scope.mode = $scope.user.userPreferences.properties['reportModeTab'] ? 
            $scope.user.userPreferences.properties['reportModeTab'] : 'Static';

          // watch component, blank out temporary cuis 
          $scope.$watch('selected.component',
              function() {
                console.debug('selected.component', $scope.selected.component,
                  $scope.selected.project);
                if ($scope.selected.component) {
                  $scope.tId = $scope.selected.component.terminologyId == $scope.selected.component.id ? 
                    '' : $scope.selected.component.terminologyId;
                }
              });
            
          // open report window
          $scope.openReportWindow = function() {
            reportService.popout($scope.selected.component);
          };
          
          $scope.setReportMode = function(mode) {
            $scope.mode = mode;
            securityService.saveProperty($scope.user.userPreferences, 'reportModeTab', $scope.mode);
          }

        } ]

    };
  }

]);
