// Edit bin controller
tsApp
  .controller(
    'ProcessModalCtrl',
    [
      '$scope',
      '$uibModalInstance',
      'utilService',
      'processService',
      'selected',
      'lists',
      'user',
      'process',
      'action',
      function($scope, $uibModalInstance, utilService, processService, selected, lists, user,
        process, action) {
        console.debug("configure ProcessModalCtrl", process, action, selected);

        // Scope vars
        $scope.action = action;
        $scope.process = process ? process : {
          terminology : null,
          version : null
        };
        $scope.bins = lists.bins;
        $scope.lists = lists;
        $scope.project = selected.project;
        $scope.errors = [];
        $scope.messages = [];

        // Validate email
        function validateEmail(email) {
          var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
          return re.test(email);
        }

        // Update process
        $scope.submitProcess = function(process) {
          $scope.errors = [];
          if (process.feedbackEmail && !validateEmail(email)) {
            $scope.errors.push('Invalid email: ' + participant.email);
            return;
          }

          if (action == 'Edit') {
            processService.updateProcessConfig($scope.project.id, process).then(
            // Success - update definition
            function(data) {
              $uibModalInstance.close(process);
            },
            // Error - update definition
            function(data) {
              utilService.handleDialogError($scope.errors, data);
            });

          } else if (action == 'Add') {
            process.type = selected.processType;
            processService.addProcessConfig($scope.project.id, process).then(
            // Success - add definition
            function(data) {
              $uibModalInstance.close(process);
            },
            // Error - add definition
            function(data) {
              utilService.handleDialogError($scope.errors, data);
            });
          }

        };

        // Dismiss modal
        $scope.cancel = function() {
          $uibModalInstance.dismiss('cancel');
        };

        // Handler for the terminology changing
        $scope.setTerminology = function(terminology) {
          $scope.process.terminology = terminology;
          $scope.process.version = $scope.getTerminology(terminology).version;
        }
        // get the terminology object for an abbreviation
        $scope.getTerminology = function(terminology) {
          for (var i = 0; i < $scope.lists.terminologies.length; i++) {
            if ($scope.lists.terminologies[i].terminology == terminology) {
              return $scope.lists.terminologies[i];
            }
          }
          return {
            version : null
          };
        }

        // Initialize

        if ($scope.action == 'Edit') {
          processService.getProcessConfig($scope.project.id, $scope.process.id).then(
          // Success
          function(data) {
            $scope.process = data;
          });
        }
        if ($scope.action == 'Add') {
          $scope.setTerminology($scope.project.terminology);
        }

        // end
      } ]);
