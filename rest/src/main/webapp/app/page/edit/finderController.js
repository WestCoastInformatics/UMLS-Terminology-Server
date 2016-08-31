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
  function($scope, $uibModalInstance, utilService, metadataService, contentService, reportService,
    selected, lists, user) {
    console.debug('Entered finder modal control');

    // Scope
    $scope.concept = null;
    $scope.selected = selected;
    $scope.query = null;
    $scope.paging = utilService.getPaging();
    $scope.paging.pageSize = 10;
    $scope.paging.disableFilter = true;
    $scope.paging.callback = {
      getPagedList : getSearchResults
    };
    $scope.searchResults = [];
    $scope.errors = [];
    $scope.metadata = metadataService.getModel();

    // Send concept back to edit controller
    $scope.addConcept = function(concept) {
      $uibModalInstance.close(concept);
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

      var paging = $scope.paging;
      var pfs = {
        startIndex : (paging.page - 1) * paging.pageSize,
        maxResults : paging.pageSize,
        sortField : paging.sortField,
        ascending : paging.sortAscending,
        queryRestriction : paging.filter
      };

      var version = metadataService.getTerminologyVersion($scope.selected.project.terminology);
      contentService.findConcepts($scope.selected.project.terminology, version, $scope.query, pfs)
        .then(
        // Success
        function(data) {
          $scope.searchResults = data.results;
          $scope.searchResults.totalCount = data.totalCount;
        },
        // Error
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });

    }
    ;

    // select concept and get concept data
    $scope.selectConcept = function(concept) {
      $scope.concept = concept;
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
