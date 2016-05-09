tsApp.controller('selectComponentModalCtrl', function($scope, $uibModalInstance, contentService,
  metadata) {

  //
  // Scope variables
  //

  // metadata (passed in)
  $scope.metadata = metadata;
  $scope.classType = null;
  if ($scope.metadata && $scope.metadata.terminology) {
    $scope.classType = $scope.metadata.terminology.organizingClassType;
    $scope.classType = $scope.classType.substring(0,1).toUpperCase() + $scope.classType.substring(1).toLowerCase();
  } 
  if (!$scope.classType || $scope.classType.length == 0) {
    $scope.classType = 'Component';
  }


  // default search params and paging
  $scope.searchParams = angular.copy(contentService.getSearchParams());
  $scope.searchResults = angular.copy(contentService.getSearchResults());
  console.debug('searchResults', $scope.searchResults);

  // the currently selected component
  $scope.component = null;

  //
  // Modal Control functions
  // 
  $scope.selectComponent = function() {
    $uibModalInstance.close($scope.component);
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
      $scope.metadata.terminology.terminology, $scope.metadata.terminology.version,
      $scope.searchParams.page, $scope.searchParams).then(
      function(data) {
        $scope.searchResults.list = data.results;
        $scope.searchResults.totalCount = data.totalCount;

        if (loadFirst && $scope.searchResults.list.length > 0) {
          $scope.getComponent($scope.searchResults.list[0].terminologyId,
            $scope.metadata.terminology.terminology, $scope.metadata.terminology.version);
        }
      });
  };

  //Get a component and set the local component data model
  // e.g. this is called when a user clicks on a search result
  $scope.getComponent = function(terminologyId, terminology, version) {
    contentService.getComponent(terminologyId, terminology, version).then(function(response) {
      $scope.setActiveRow(terminologyId);
      $scope.component = response;
    });
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
  
 

  //
  // Initialization
  //

  // none
});