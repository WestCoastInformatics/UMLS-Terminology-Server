// Content controller
tsApp.directive('report', [ function() {
  console.debug('configure report directive');
  return {
    restrict : 'A',
    scope : {
      metadata : '=',
      component : '=',
      container : '='
    },
    templateUrl : 'app/component/report/report.html'
  };
} ]);
