// Finder modal controller
tsApp.controller('FinderModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'metadataService',
  'contentService',
  'reportService',
  'selected',
  'lists',
  'user',
  'type',
  function($scope, $uibModalInstance, utilService, metadataService, contentService, reportService,
    selected, lists, user, type) {
    console.debug('Entered finder modal control');

    // Scope vars
    $scope.type = type;
    $scope.selected = {
      component : null,
      project : selected.project,
      metadata : selected.metadata
    };
    $scope.query = null;
    $scope.searchResults = [];

    // Callbacks for report
    $scope.callbacks = {};
    utilService.extendCallbacks($scope.callbacks, metadataService.getCallbacks());
    utilService.extendCallbacks($scope.callbacks, contentService.getCallbacks());

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
        $scope.selected.project.terminology, $scope.selected.project.version, $scope.paging).then(
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
    ;

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

    // end
  } ]);
;
