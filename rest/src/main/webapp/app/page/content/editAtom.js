// Simple atom modal controller
tsApp.controller('SimpleAtomModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'editService',
  'atom',
  'action',
  'selected',
  'lists',
  function($scope, $uibModalInstance, utilService, editService, atom, action, selected, lists) {
    console.debug('Entered simple atom modal control', atom, action, selected, lists);

    // Scope vars
    $scope.selected = selected;
    $scope.lists = lists;
    $scope.atom = atom ? atom : {
      language : $scope.selected.metadata.languages[0].key,
      publishable : true
    };
    $scope.action = action;
    $scope.overrideWarnings = false;
    $scope.warnings = [];
    $scope.errors = [];
    $scope.workflowStatuses = [ 'NEEDS_REVIEW', 'READY_FOR_PUBLICATION' ];
    $scope.selectedTermgroup = $scope.selected.project.newAtomTermgroups[0];

    // Init modal
    function initialize() {
      if (!$scope.atom) {
        $scope.atom = {
          workflowStatus : 'NEEDS_REVIEW',
          publishable : true,
          language : 'ENG'
        };

      }
    }

    // TODO: hopefully not needed anymore
    // // Initialize languages
    // if ($scope.selected.metadata.languages.length == 0) {
    // // TODO:
    // $scope.selected.metadata.languages = [ {
    // key : "ENG",
    // value : "English"
    // } ];
    // }

    // Get terminology object for a terminology value
    $scope.getTerminology = function(terminology) {
      for (var i = 0; i < $scope.lists.terminologies.length; i++) {
        if ($scope.lists.terminologies[i].terminology == terminology) {
          return $scope.lists.terminologies[i];
        }
      }
    }

    // Perform add or edit/update
    $scope.submitAtom = function(atom) {
      $scope.errors = [];
      if ($scope.action == 'Add') {
        if (!atom || !atom.name || !$scope.selectedTermgroup) {
          $scope.errors.push('Name and  termgroup must be selected.');
          return;
        }
        // terminology/version will be chosen by the concept
        atom.terminology = $scope.selectedTermgroup
          .substr(0, $scope.selectedTermgroup.indexOf('/'));
        atom.termType = $scope.selectedTermgroup.substr($scope.selectedTermgroup.indexOf('/') + 1);
        atom.terminologyId = '';

        if (!atom.workflowStatus) {
          atom.workflowStatus = 'NEEDS_REVIEW';
        }

        editService.addAtom($scope.selected.project.id, $scope.selected.component.id, atom).then(
        // Success
        function(data) {
          $uibModalInstance.close();
        },
        // Error
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
      } else {
        editService.updateAtom($scope.selected.project.id, $scope.selected.component.id, atom)
          .then(
          // Success
          function(data) {
            $uibModalInstance.close();
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