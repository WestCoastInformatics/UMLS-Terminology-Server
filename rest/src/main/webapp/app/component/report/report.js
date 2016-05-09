// Content controller
tsApp.directive('report', [ '$window', '$routeParams', 'metadataService',
  function($window, $routeParams, metadataService) {
    console.debug('configure report directive');
    return {
      restrict : 'A',
      scope : {
        // the metadata for the terminology
        metadata : '=',

        // the component to display
        component : '=',

        // callback functions
        callbacks : '=',

      },
      templateUrl : 'app/component/report/report.html',
      link : function(scope, element, attrs) {

        // declare the show hidden variable (suppressible/obsolete)
        scope.showHidden = false;
      }
    };
  } ]);
