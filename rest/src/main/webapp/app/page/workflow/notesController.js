// Create notes controller
var NotesModalCtrl = function($scope, $uibModalInstance, $sce, utilService, workflowService, object, value, project,
  tinymceOptions) {
  console.debug('Entered notes modal control', object, value, project.id);
  $scope.object = object;
  $scope.value = value;
  $scope.project = project;
  $scope.tinymceOptions = tinymceOptions;
  $scope.newNote = null;

  // Paging parameters
  $scope.pageSize = 5;
  $scope.pagedNotes = [];
  $scope.paging = {};
  $scope.paging['notes'] = {
    page : 1,
    filter : '',
    typeFilter : '',
    sortField : 'lastModified',
    ascending : true
  };
  $scope.errors = [];

  // Get paged notes (assume all are loaded)
  $scope.getPagedNotes = function() {
    var pagedArray = utilService.getPagedArray($scope.object.notes, $scope.paging['notes'],
      $scope.pageSize);
    $scope.pagedNotes = pagedArray.data;
    $scope.pagedNotes.totalCount = pagedArray.totalCount;
  };

  $scope.getNoteValue = function(note) {
    return $sce.trustAsHtml(note.note);
  };

  // remove note
  $scope.removeNote = function(object, note) {

    if ($scope.value == 'Worklist') {
      workflowService.removeWorklistNote($scope.project.id, note.id).then(
      // Success - remove worklist
      function(data) {
        $scope.newNote = null;
        workflowService.getWorklist($scope.project.id, object.id).then(function(data) {
          object.notes = data.notes;
          $scope.getPagedNotes();
        },
        // Error - remove worklist
        function(data) {
          handleError($scope.errors, data);
        });
      },
      // Error - remove worklist
      function(data) {
        handleError($scope.errors, data);
      });
    } else if ($scope.value == 'Checklist') {
      workflowService.removeChecklistNote($scope.project.id, note.id).then(
      // Success - remove checklist
      function(data) {
        $scope.newNote = null;
        workflowService.getChecklist($scope.project.id, object.id).then(function(data) {
          object.notes = data.notes;
          $scope.getPagedNotes();
        },
        // Error - remove checklist
        function(data) {
          handleError($scope.errors, data);
        });
      },
      // Error - remove checklist
      function(data) {
        handleError($scope.errors, data);
      });
    }
  };

  // add new note
  $scope.submitNote = function(object, text) {

    if ($scope.value == 'Checklist') {
      workflowService.addChecklistNote($scope.project.id, object.id, text).then(
      // Success - add checklist note
      function(data) {
        $scope.newNote = null;
        workflowService.getChecklist($scope.project.id, object.id).then(function(data) {
          object.notes = data.notes;
          $scope.getPagedNotes();
        },
        // Error - add checklist note
        function(data) {
          handleError($scope.errors, data);
        });
      },
      // Error - add checklist note
      function(data) {
        handleError($scope.errors, data);
      });
    } else if ($scope.value == 'Worklist') {
      workflowService.addWorklistNote($scope.project.id, object.id, text).then(
      // Success - add worklist note
      function(data) {
        $scope.newNote = null;
        workflowService.getWorklist($scope.project.id, object.id).then(function(data) {
          object.notes = data.notes;
          $scope.getPagedNotes();
        },
        // Error - add worklist note
        function(data) {
          handleError($scope.errors, data);
        });
      },
      // Error - add worklist note
      function(data) {
        handleError($scope.errors, data);
      });
    }
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
  $scope.getPagedNotes();
};