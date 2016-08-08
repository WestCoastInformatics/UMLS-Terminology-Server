// Create merge controller
var MergeModalCtrl = function($scope, $uibModalInstance, $sce, utilService, 
  metaEditingService, workflowService, concept, data, project
  ) {
  console.debug('Entered merge modal control', concept, data, project.id);
  $scope.concept = concept;
  $scope.data = data;
  $scope.prospectiveMergeConcepts = [];
  $scope.project = project;
  $scope.overrideWarnings = false;
 
  $scope.warnings = [];
  $scope.errors = [];

  function initialize() {
    for (var i=0; i<$scope.data.length; i++) {
      if ($scope.data[i].id != $scope.concept.id) {
        $scope.prospectiveMergeConcepts.push($scope.data[i]);
      }
    }
    if ($scope.prospectiveMergeConcepts.length == 1) {
      $scope.mergeConcept = $scope.prospectiveMergeConcepts[0];
    }
  }

  // merge concepts
  $scope.merge = function(concept1, concept2) {

      metaEditingService.mergeConcepts($scope.project.id, concept1, concept2, $scope.overrideWarnings).then(
      // Success
      function(data) {
        $scope.warnings = data.warnings;
        $scope.errors = data.errors;
        if ($scope.warnings.length > 0) {
          $scope.overrideWarnings = true;
        }
        if ($scope.warnings.length == 0 && $scope.errors.length == 0) {
          $uibModalInstance.close();
        }
      },
      // Error
      function(data) {
        handleError($scope.errors, data);
      });
  };

  // Convert date to a string
  $scope.toDate = function(lastModified) {
    return utilService.toDate(lastModified);
  };

  // Dismiss modal
  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

  // initialize modal
  initialize();
};