tsApp.controller('componentNoteModalCtrl', function($scope, $q, $uibModalInstance, utilService, callbacks, component) {

  console.debug('component notes modal opened', component, callbacks);

  // NOTE: Component must contain minimum of type, terminology, version, and terminologyId
  $scope.component = component;
  $scope.callbacks = callbacks;

  function getPagedList() {
    $scope.pagedData = utilService.getPagedArray($scope.component.object.userAnnotations,
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
    callbacks.addComponentNote($scope.component.type, $scope.component.terminology, $scope.component.version, $scope.component.terminologyId, note).then(function(response) {
      console.debug('Note added, new object = ', response);
      
    });
  }

  $scope.removeNote = function(note) {

  }
  
  //
  // Initialization
  // 
  $scope.initialize = function() {
    callbacks.getComponentFromType($scope.component.terminology, $scope.component.version, $scope.component.terminologyId, $scope.component.type).then(function(response) {
      $scope.component.name = response.name;
      $scope.component.notes = response.notes;
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