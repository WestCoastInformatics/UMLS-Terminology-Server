tsApp.controller('componentNoteModalCtrl', function($scope, $q, $uibModalInstance, contentService, utilService, component) {

  console.debug('component notes modal opened', component, callbacks);

  // Component wrapper or full component
  $scope.component = component;
  
  console.debug('notes modal: ', component, callbacks);

  function getPagedList() {
    $scope.pagedData = utilService.getPagedArray($scope.component.userAnnotations,
      $scope.paging);
  }

  // instantiate paging and paging callback function
  $scope.pagedData = [];
  $scope.paging = utilService.getPaging();
  $scope.pageCallback = {
    getPagedList : getPagedList
  };

  // default sort is by date descending
  $scope.paging.sortField = 'lastModified';
  $scope.paging.sortAscending = false;

  // Default is Group/Type, where in getpagedData
  // relationshipType is automatically appended as a multi-
  // sort search
  $scope.paging.sortOptions = [ {
    key : 'Date',
    value : 'lastModified'
  } ];

  //
  // Note controls
  //
  $scope.addNote = function(note) {
    console.debug('Adding note: ', note);
    contentService.addComponentNote($scope.component, note).then(function(response) {
      console.debug('Note added, new object = ', response);
      
    });
  }

  $scope.removeNote = function(note) {

  }
  
  //
  // Initialization
  // 
  $scope.initialize = function() {
    
    // re-retrieve the component (from either wrapper or full component)
    contentService.getComponent($scope.component).then(function(response) {
      $scope.component.name = response;
    });
  }
  
  $scope.initialize();

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