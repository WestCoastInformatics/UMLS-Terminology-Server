tsApp.controller('componentNoteModalCtrl', function($scope, $q, $uibModalInstance, $sce,
  contentService, utilService, websocketService, component) {

  console.debug('component notes modal opened', component);

  // Component wrapper or full component
  $scope.component = component;
  $scope.tinymceOptions = utilService.tinymceOptions;

  function getPagedList() {
    console.debug('notes: getpageddata');
    $scope.pagedData = utilService.getPagedArray($scope.component.notes, $scope.paging);
    console.debug($scope.pagedData);
  }

  // instantiate paging and paging callback function
  $scope.pagedData = [];
  $scope.paging = utilService.getPaging();
  $scope.pageCallback = {
    getPagedList : getPagedList
  };

  // set defaults/overrides
  $scope.paging.pageSize = 5;
  $scope.paging.sortField = 'timestamp';
  $scope.paging.sortAscending = false;

  // Default is Group/Type, where in getpagedData
  // relationshipType is automatically appended as a multi-
  // sort search
  $scope.paging.sortOptions = [];

  $scope.getNoteValue = function(note) {
    return $sce.trustAsHtml(note.note);
  };

  //
  // Note controls
  //
  $scope.addNote = function(note) {
    console.debug('Adding note: ', note);
    contentService.addComponentNote($scope.component, note).then(function(response) {
      $scope.refreshConcept();
      websocketService.fireNoteChange({
        component : $scope.component
      });
    });
  };

  $scope.removeNote = function(note) {
    console.debug('Remove note: ', note.id);
    contentService.removeComponentNote($scope.component, note.id).then(function(response) {
      $scope.refreshConcept();
      websocketService.fireNoteChange({
        component : $scope.component
      });
    });
  };

  $scope.refreshConcept = function() {
    // re-retrieve the component (from either wrapper or full component)
    contentService.getComponent($scope.component).then(function(response) {
      $scope.component = response;
      getPagedList();
    });
  };
  
  // Render date
  $scope.toDate = function(x) {
    return utilService.toDate(x);
  };

  //
  // Initialization
  // 
  $scope.initialize = function() {
    $scope.refreshConcept();
  };
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