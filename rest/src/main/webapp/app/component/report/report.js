// Content controller
tsApp.directive('report', [
  '$window',
  'metadataService',
  function($window, metadataService) {
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

        scope.popout = function() {
          var currentUrl = window.location.href;
          var baseUrl = currentUrl.substring(0, currentUrl.indexOf('#') + 1);
          var newUrl = baseUrl + '/content/simple/' + scope.component.object.terminology + '/'
            + scope.component.object.version + '/' + scope.component.object.terminologyId;
          var myWindow = window.open(newUrl, scope.component.object.terminology + '/'
            + scope.component.object.version + ', ' + scope.component.object.terminologyId + ', '
            + scope.component.object.name)
          ;
          myWindow.focus();
        }

      }
    };
  } ]);
