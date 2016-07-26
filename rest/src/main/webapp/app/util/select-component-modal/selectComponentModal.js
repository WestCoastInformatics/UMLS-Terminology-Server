tsApp.controller('selectComponentModalCtrl', function($scope, $q, $uibModalInstance,
  contentService, metadata) {

  //
  // Scope variables
  //

  // metadata (passed in)
  $scope.metadata = metadata;

  // determine class type for display
  $scope.classType = null;
  if ($scope.metadata && $scope.metadata.terminology) {
    $scope.classType = $scope.metadata.terminology.organizingClassType;
    $scope.classType = $scope.classType.substring(0, 1).toUpperCase()
      + $scope.classType.substring(1).toLowerCase();
  }
  if (!$scope.classType || $scope.classType.length == 0) {
    $scope.classType = 'Component';
  }

  // default search params and paging
  $scope.searchParams = contentService.getSearchParams();
  $scope.searchResults = null;
  $scope.pageSizes = contentService.getPageSizes();

  // the currently selected component
  $scope.component = null;

  //
  // Modal Control functions
  // 
  $scope.selectComponent = function() {
    $uibModalInstance.close($scope.component);
  };

  $scope.selectSearchResult = function(searchResult) {
    $scope.getComponent(searchResult).then(function(response) {

      $uibModalInstance.close(response);
    });
  };

  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

  //
  // Component Search functions
  // 

  //Perform search and populate list view
  $scope.findComponentsAsList = function(loadFirst) {
    $scope.queryForTree = false;
    $scope.queryForList = true;

    var hasQuery = $scope.searchParams && $scope.searchParams.query
      && $scope.searchParams.query.length > 0;
    var hasExpr = $scope.searchParams && $scope.searchParams.advancedMode
      && $scope.searchParams.expression && $scope.searchParams.expression.length > 0;

    // ensure query/expression string has appropriate length
    if (!hasQuery && !hasExpr) {
      alert("You must use at least one character to search"
        + ($scope.searchParams.advancedMode ? " or supply an expression" : ""));
      return;
    }
    contentService.findComponentsAsList($scope.searchParams.query,
      $scope.metadata.terminology.organizingClassType, $scope.metadata.terminology.terminology,
      $scope.metadata.terminology.version, $scope.searchParams.page, $scope.searchParams).then(
      function(data) {

        $scope.searchResults = data;
        console.debug('search results', data, $scope.searchResults);

        if (loadFirst && $scope.searchResults.results.length > 0) {
          $scope.getComponent($scope.searchResults.results[0]);
        }
      });
  };

  //Get a component and set the local component data model
  // e.g. this is called when a user clicks on a search result
  $scope.getComponent = function(wrapper) {
    var deferred = $q.defer();
    contentService.getComponent(wrapper).then(function(response) {
      $scope.setActiveRow(wrapper.terminologyId);
      $scope.component = response;
      deferred.resolve($scope.component);
    }, function() {
      deferred.reject();
    });
    return deferred.promise;
  };

  // Helper function to select an item in the list view
  $scope.setActiveRow = function(terminologyId) {
    if (!$scope.searchResults.list || $scope.searchResults.list.length == 0)
      return;
    for (var i = 0; i < $scope.searchResults.list.length; i++) {
      if ($scope.searchResults.list[i].terminologyId === terminologyId) {
        $scope.searchResults.list[i].active = true;
      } else {
        $scope.searchResults.list[i].active = false;
      }
    }
  };

  // component scoring
  $scope.scoreExcellent = 0.7;
  $scope.scoreGood = 0.3;

  $scope.getColorForScore = function(score) {
    if (score > $scope.scoreExcellent) {
      return 'green';
    } else if (score > $scope.scoreGood) {
      return 'yellow';
    } else {
      return 'orange';
    }
  };

  // set the top level component from a tree node
  $scope.getComponentFromTree = function(type, nodeScope) {
    var tree = nodeScope.$modelValue;
    $scope.getComponent(tree.nodeId, type, tree.nodeTerminologyId, tree.terminology, tree.version);
  };
  //
  // Initialization
  //

  $scope.componentReportCallbacks = {
    getComponent : $scope.getComponent,
    getComponentFromTree : $scope.getComponentFromTree

  };

  // none
});