tsApp.controller('annotationModalCtrl', function($scope, $q, $uibModalInstance, contentService,
  utilService, component) {

  console.debug('annotation modal opened', component);

  $scope.component = component;

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

  // determine class type for display
  if ($scope.component.object.type) {
    $scope.type = $scope.component.object.type.substring(0, 1).toUpperCase()
      + $scope.component.object.type.substring(1).toLowerCase();
  } else {
    $scope.type = 'Component';
  }

  //
  // Note controls
  //
  $scope.addNote = function(note) {
    console.debug('Adding note: ', note);
    contentService.addComponentAnnotation($scope.component, note).then(function(response) {
      console.debug('Note added, new object = ', response);
      $scope.component.object = response;
    })
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