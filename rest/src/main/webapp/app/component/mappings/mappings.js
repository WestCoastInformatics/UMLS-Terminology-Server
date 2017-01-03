// Mappings
tsApp.directive('mappings', [ function() {
  console.debug('configure mappingss directive');
  return {
    restrict : 'A',
    scope : {
      component : '=',
      metadata : '=',
      showHidden : '=',
      callbacks : '='
    },
    templateUrl : 'app/component/mappings/mappings.html',
    controller : [
      '$scope',
      'utilService',
      'contentService',
      function($scope, utilService, contentService) {
        $scope.showing = true;
        $scope.mapSets = {};

        // watch the component
        $scope.$watch('component', function() {
          if ($scope.component) {
            // Get data
            getMappings();
          }
        }, true);

        // Get paged data
        function getMappings() {

          // Request from service
          contentService.findMappings($scope.component, {}).then(
            // Success
            function(data) {
              $scope.mappings = data.mappings;

              if (data.mappings.length > 0) {
                // Request map setsfrom service
                contentService.getMapSets($scope.metadata.terminology.terminology,
                  $scope.metadata.terminology.version).then(
                // Success
                function(data) {
                  for (var i; i < data.mapSets.length; i++) {
                    var mapSet = data.mapSets[i];
                    $scope.mapSets[mapSet.id] = [];
                    for (var j = 0; j < $scope.mappings.length; j++) {
                      if (data.mapSets[i].id == $scope.mappings[i].mapSetId) {
                        $scope.mapSets[mapSet.id].push($scope.mappings[i]);
                      }
                    }
                  }
                });
              }
            });

        }

        // end controller
      } ]
  };
} ]);
