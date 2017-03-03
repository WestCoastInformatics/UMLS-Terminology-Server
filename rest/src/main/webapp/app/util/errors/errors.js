// Errors directive
tsApp.directive('errors', [ function() {
  return {
    restrict : 'A',
    scope : {
      errors : '=',
      warnings : '=',
      comments : '='
    },
    templateUrl : 'app/util/errors/errors.html',
    controller : [ '$scope', function($scope) {

      // n/a - this is just a view into $scope.errors

      // end controller
    } ]
  };

} ]);