// Finder modal controller
tsApp.controller('FinderModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'contentService',
  'reportService',
  'selected',
  'lists',
  'user',
  function($scope, $uibModalInstance, utilService, contentService, reportService, selected, lists,
    user) {
    console.debug('Entered finder modal control');

    // Scope
    $scope.query = null;
    $scope.paging = utilService.getPaging();
    $scope.paging.pageSize = 10;
    $scope.paging.disableFilter = true;
    $scope.paging.callback = {
      getPagedList : getSearchResults
    };
    $scope.errors = [];

    // Send concept back to edit controller
    $scope.addConcept = function(concept) {
      $uibModalInstance.close(concept);
    };

    // clear, then get search results
    $scope.clearAndSearch = function() {
      paging.page = 1;
      getSearchResults();
    }
    // get search results
    $scope.getSearchResults = function() {

      // clear data structures
      $scope.errors = [];

      var paging = $scope.paging;
      var pfs = {
        startIndex : (paging.page - 1) * paging.pageSize,
        maxResults : paging.pageSize,
        sortField : paging.sortField,
        ascending : paging.sortAscending,
        queryRestriction : paging.filter
      };

      contentService.findConcepts("UMLS", "latest", search, pfs).then(
      // Success
      function(data) {
        $scope.searchResults = data.results;
        $scope.searchResults.totalCount = data.totalCount;
      },
      // Error
      function(data) {
        utilService.handleDialogError($scope.errors, data);
      });

    };

    // select concept and get concept data
    $scope.selectConcept = function(concept) {
      $scope.data.concept = concept;
      reportService.getConceptReport($scope.selected.project.id, concept.id).then(
      // Success
      function(data) {
        $scope.conceptReport = data;
      },
      // Error
      function(data) {
        utilService.handleDialogError($scope.errors, data);
      });
    };

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.close();
    };

    // end
  } ]);
;
