// Atom modal controller
tsApp.controller('AtomModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'metaEditingService',
  'atom',
  'action',
  'selected',
  'lists',
  function($scope, $uibModalInstance, utilService, metaEditingService, atom, action, selected, lists) {
    console.debug('Entered atom modal control', atom, action);

    // Scope vars
    $scope.selected = selected;
    $scope.lists = lists;
    $scope.atom = atom;
    $scope.action = action;
    $scope.overrideWarnings = false;
    $scope.warnings = [];
    $scope.errors = [];
    $scope.workflowStatuses = [ 'NEEDS_REVIEW', 'READY_FOR_PUBLICATION' ];

    // Init modal
    function initialize() {
      if (!$scope.atom) {
        $scope.atom = {
          workflowStatus : 'NEEDS_REVIEW',
          publishable : true,
          language : 'ENG'
        };
        $scope.selectedTermgroup = $scope.selected.project.newAtomTermgroups[0];
      }

    }

    $scope.getTerminology = function(terminology) {
      for (var i = 0; i < $scope.lists.terminologies.length; i++) {
        if ($scope.lists.terminologies[i].terminology == terminology) {
          return $scope.lists.terminologies[i];
        }
      }
    }
    // Perform add or edit/update
    $scope.submitAtom = function(atom) {
      if ($scope.action == 'Add') {
        if (!atom || !atom.name || !$scope.selectedTermgroup
          || (!atom.codeId && !atom.conceptId && !atom.descriptorId)) {
          window.alert('Name, termgroup and at least one id must be entered for new atom.');
          return;
        }
        atom.terminology = $scope.selectedTermgroup
          .substr(0, $scope.selectedTermgroup.indexOf('/'));
        atom.termType = $scope.selectedTermgroup.substr($scope.selectedTermgroup.indexOf('/') + 1);
        atom.version = $scope.getTerminology(atom.terminology).version;
        atom.terminologyId = '';
        if (!atom.conceptId)
          atom.conceptId = '';
        if (!atom.codeId)
          atom.codeId = '';
        if (!atom.descriptorId)
          atom.descriptorId = '';

        metaEditingService.addAtom($scope.selected.project.id, $scope.selected.activityId,
          $scope.selected.concept, atom, $scope.overrideWarnings).then(
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
          utilService.handleDialogError($scope.errors, data);
        });
      } else {
        metaEditingService.updateAtom($scope.selected.project.id, $scope.selected.activityId,
          $scope.selected.concept, atom, $scope.overrideWarnings).then(
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
          utilService.handleDialogError($scope.errors, data);
        });
      }
    };

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // initialize modal
    initialize();

    // end
  } ]);