// Errors directive
tsApp.directive('errors', [ function() {
  console.debug('configure errors directive');
  return {
    restrict : 'A',
    scope : {
      errors : '=',
      warnings : '='
    },
    templateUrl : 'app/util/errors/errors.html',
    controller : [ '$scope', function($scope) {

      // n/a - this is just a view into $scope.errors

      // end controller
    } ]
  };

} ]);