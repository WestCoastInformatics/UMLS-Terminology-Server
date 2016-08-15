// Add concept controller
var AddConceptModalCtrl = function($scope, $uibModalInstance, contentService, reportService,
  workflowService, record, project) {
  console.debug('Entered add concept modal control');
  $scope.record = record;
  $scope.project = project;
  $scope.pageSize = 10;
  $scope.searchResults = null;
  $scope.data = {};
  $scope.pageSize = 10;
  $scope.paging = {};
  $scope.paging['search'] = {
    page : 1,
    filter : '',
    sortField : null,
    ascending : null
  };
  $scope.errors = [];

  $scope.addConcept = function(concept) {
    $uibModalInstance.close(concept);
  };

  // get search results
  $scope.getSearchResults = function(search, clearPaging) {

    if (clearPaging) {
      $scope.paging['search'].page = 1;
    }

    // skip search if blank
    if (!search) {
      return;
    }
    // clear data structures
    $scope.errors = [];

    var pfs = {
      startIndex : ($scope.paging['search'].page - 1) * $scope.pageSize,
      maxResults : $scope.pageSize,
      sortField : null,
      ascending : null,
      queryRestriction : null
    };

    contentService.findConcepts("UMLS", "latest", search, pfs).then(
    // Success
    function(data) {
      $scope.searchResults = data.results;
      $scope.searchResults.totalCount = data.totalCount;
    },
    // Error
    function(data) {
      handleError($scope.errors, data);
    });

  };

  // select concept and get concept data
  $scope.selectConcept = function(concept) {
    $scope.data.concept = concept;
    reportService.getConceptReport($scope.project.id, concept.id).then(
    // Success
    function(data) {
      $scope.conceptReport = data;
    },
    // Error
    function(data) {
      handleError($scope.errors, data);
    });
  };

  // Dismiss modal
  $scope.cancel = function() {
    $uibModalInstance.close();
  };

};
