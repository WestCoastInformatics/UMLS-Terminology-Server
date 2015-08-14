// Content controller
console.debug('configure report directive');
tsApp.directive('report', [ function() {
  return {
    restrict : 'A',
    scope : {
      metadata : '=',
      component : '=',
    },
    templateUrl : 'app/component/report/report.html'
  };
}]);
