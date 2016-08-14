// Create merge controller
var MergeModalCtrl = function($scope, $uibModalInstance, $sce, utilService, metaEditingService,
  workflowService, project, activityId, concept, data) {
  console.debug('Entered merge modal control', project, activityId, concept, data, project.id);

  // Scope vars
  $scope.project = project;
  $scope.activityId = activityId;
  $scope.concept = concept;
  $scope.data = data;
  $scope.prospectiveMergeConcepts = [];
  $scope.overrideWarnings = false;
  $scope.warnings = [];
  $scope.errors = [];

  // Init modal
  function initialize() {
    for (var i = 0; i < $scope.data.length; i++) {
      if ($scope.data[i].id != $scope.concept.id) {
        $scope.prospectiveMergeConcepts.push($scope.data[i]);
      }
    }
    if ($scope.prospectiveMergeConcepts.length == 1) {
      $scope.mergeConcept = $scope.prospectiveMergeConcepts[0];
    }
  }

  // Perform merge
  $scope.merge = function(concept1, concept2) {

    metaEditingService.mergeConcepts($scope.project.id, $scope.activityId, concept1, concept2,
      $scope.overrideWarnings).then(
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