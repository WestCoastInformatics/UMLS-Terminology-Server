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
        callbacks : '=?',

      },
      templateUrl : 'app/component/report/report.html',
      link : function(scope, element, attrs) {

        console.debug('report', scope.component, scope.metadata, scope.navCallbacks,
          scope.editCallbacks);

        // declare the show hidden variable (suppressible/obsolete)
        scope.showHidden = false;

        scope.popout = function() {
          var currentUrl = window.location.href;
          var baseUrl = currentUrl.substring(0, currentUrl.indexOf('#') + 1);
          var newUrl = baseUrl + '/content/' + scope.component.object.terminology + '/'
            + scope.component.object.version + '/' + scope.component.object.terminologyId;
          var myWindow = window.open(newUrl, scope.component.object.terminology + '/'
            + scope.component.object.version + ', ' + scope.component.object.terminologyId + ', '
            + scope.component.object.name)
          ;
          myWindow.focus();
        }

        // callback functions specific to all report functionality
        scope.reportCallbacks = {

          // toggle an items collapsed state
          toggleItemCollapse : function(item) {
            item.expanded = !item.expanded;
          },

          // get the collapsed state icon
          getCollapseIcon : function(item) {

            // if no expandable content detected, return blank glyphicon
            // (see
            // tsApp.css)
            if (!item.hasContent)
              return 'glyphicon glyphicon-plus glyphicon-none';

            // return plus/minus based on current expanded status
            if (item.expanded)
              return 'glyphicon glyphicon-minus';
            else
              return 'glyphicon glyphicon-plus';
          }
        }

        scope.testCallback = function() {
          console.debug('TEST CALLBACK');
        }

        // append the navigation and editing callbacks if specified
        for ( var key in scope.callbacks) {
          if (scope.reportCallbacks[key]) {
            console.warn('Overriding report callback function', key);
          }
          scope.reportCallbacks[key] = scope.callbacks[key];
        }

      }
    };
  } ]);
