// Finderdirective
tsApp.directive('finder', [ function() {
  return {
    restrict : 'A',
    scope : {
      selected : '=',
      lists : '=',
      callbacks : '=',
      type : '@'
    },
    templateUrl : 'app/actions/finder/finder.html',
    controller : [
      '$scope',
      '$uibModal',
      'utilService',
      'metadataService',
      'contentService',
      function($scope, $uibModal, utilService, metadataService, contentService) {

        // Scope vars
        $scope.lookupText = null;

        // Add finder modal
        $scope.openFinderModal = function() {
          console.debug('openFinderModal ');
          var modalInstance = $uibModal.open({
            templateUrl : 'app/actions/finder/finderModal.html',
            controller : FinderModalCtrl,
            backdrop : 'static',
            size : 'lg',
            resolve : {
              callbacks : function() {
                return $scope.callbacks;
              },
              selected : function() {
                return $scope.selected;
              },
              lists : function() {
                return $scope.lists;
              },
              type : function() {
                return $scope.type ? $scope.type : 'Concept';
              }
            }
          });

          // Handle the result, callback
          modalInstance.result.then(
          // Success
          function(data) {
            $scope.callbacks.addComponent(data);
          });

        };

        // Finder modal controller
        var FinderModalCtrl = function($scope, $uibModalInstance, utilService, metadataService,
          contentService, selected, lists, type, callbacks) {
          console.debug("configure FinderModalCtrl");

          // Scope vars
          $scope.type = type;
          $scope.callbacks = callbacks;
          $scope.selected = {
            component : null,
            project : selected.project,
            metadata : selected.metadata
          };
          $scope.query = null;
          $scope.searchResults = [];

          // Paging vars
          $scope.paging = utilService.getPaging();
          $scope.paging.pageSize = 10;
          $scope.paging.disableFilter = true;
          $scope.paging.callbacks = {
            getPagedList : getSearchResults
          };

          // Errors
          $scope.errors = [];

          // Send component back to edit controller
          $scope.addComponent = function(component) {
            $uibModalInstance.close(component);
          };

          // clear, then get search results
          $scope.clearAndSearch = function() {
            $scope.paging.page = 1;
            getSearchResults();
          }
          // get search results
          $scope.getSearchResults = function() {
            getSearchResults();
          }
          function getSearchResults() {

            // clear data structures
            $scope.errors = [];

            contentService.findComponentsAsList($scope.query, $scope.type,
              $scope.selected.project.terminology, $scope.selected.project.version, $scope.paging)
              .then(
              // Success
              function(data) {
                $scope.searchResults = data.results;
                $scope.searchResults.totalCount = data.totalCount;
                // Select first component automatically
                if ($scope.searchResults && $scope.searchResults.length > 1) {
                  $scope.selectComponent($scope.searchResults[0]);
                }
              },
              // Error
              function(data) {
                utilService.handleDialogError($scope.errors, data);
              });

          }

          // select component and get component data
          $scope.selectComponent = function(component) {
            // Read the component
            contentService.getComponent(component).then(
            // Success
            function(data) {
              $scope.selected.component = data;
            });
          };

          // Dismiss modal
          $scope.cancel = function() {
            $uibModalInstance.dismiss();
          };
        }

        // Lookup component, if valid, add it
        $scope.lookupComponent = function(componentId) {
          // componentId is a number
          if ((componentId+'').match(/^\d+$/)) {
            contentService.getConcept(componentId, $scope.selected.project.id).then(
            // Success
            function(data) {
              $scope.callbacks.addComponent(data);
              $scope.lookupText = '';
            });
          }
        }
        
        // end
      } ]

  };
} ]);