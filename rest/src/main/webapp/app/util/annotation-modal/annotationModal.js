tsApp.controller('annotationsModalCtrl', function($scope, $q, $uibModalInstance,
  contentService, component) {

  $scope.component = component;
  
  function getPagedList() {
    scope.pagedData = utilService.getPagedArray(scope.component.object.userAnnotations, scope.paging);
  }

  // instantiate paging and paging callback function
  scope.pagedData = [];
  scope.paging = utilService.getPaging();
  scope.pageCallback = {
    getPagedList : getPagedList
  };
  
  // default sort is by date descending
  scope.paging.sortField = 'lastModified';
  scope.paging.sortAscending = false;
 
  // Default is Group/Type, where in getpagedData
  // relationshipType is automatically appended as a multi-
  // sort search
  scope.paging.sortOptions = [ {
    key : 'Date',
    value : 'lastModified'
  }];
  
  //
  // Note controls
  //
  $scope.addNote = function(note) {
    
  }
  
  $scope.removeNote = function(note) {
    
  }
  
  $scope.updateNote = function(note) {
    
  }

  //
  // Modal Control functions
  // 
  $scope.done = function() {
    $uibModalInstance.close($scope.component);
  };

  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

});