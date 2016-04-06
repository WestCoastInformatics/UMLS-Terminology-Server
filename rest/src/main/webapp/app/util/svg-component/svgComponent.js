
'use strict';
// jshint ignore: start
angular.module('tsApp')
  .directive('svgComponent', ['$rootScope',
    function(rootScope) {
    return {
      restrict: 'A',
      transclude: false,
      replace: true,
      scope: {
        component: '='
      },
      templateUrl: 'app/util/svg-component/svgComponent.html',

      link: function (scope, element, attrs, linkCtrl, snowowlService) {


        console.debug('entered svgComponent');
       

        // on open image requests, broadcast concept id to drawModel.js
        scope.openImage = function() {
          rootScope.$broadcast('openDrawModelConceptImage', {conceptId : scope.concept.conceptId});
        };
      }
    };
  }]);