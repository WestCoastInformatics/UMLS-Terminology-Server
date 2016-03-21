// Content controller
tsApp.directive('report', [ function() {
  console.debug('configure report directive');
  return {
    restrict : 'A',
    scope : {
      metadata : '=',
      component : '=',
      ctrlFns : '='
    },
    templateUrl : 'app/component/report/report.html'
  };
} ]);
