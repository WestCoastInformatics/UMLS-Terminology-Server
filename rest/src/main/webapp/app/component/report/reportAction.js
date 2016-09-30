// Report directive
tsApp.directive('reportAction', [ '$window', '$routeParams', function($window, $routeParams) {
  console.debug('configure action report directive');
  return {
    restrict : 'A',
    scope : {
      // selected features
      selected : '=',
      // callbacks functions
      callbacks : '='
    },
    templateUrl : 'app/component/report/reportAction.html',
    controller : [ '$scope', 'projectService', 'utilService', 'metaEditingService',
                   function($scope, projectService, utilService, metaEditingService) {
      // Scope vars
      $scope.molecularActions = {};
      
      // Paging variables
      $scope.pagedActions = [];
      $scope.paging = utilService.getPaging();
      $scope.paging.sortField = 'timestamp';
      $scope.paging.pageSize = 5;
      $scope.paging.sortAscending = true;
      $scope.paging.callbacks = {
        getPagedList : getPagedActions
      };

      // watch component, generate the report
      $scope.$watch('selected.component', function() {
        console.debug('selected.component', $scope.selected.component, $scope.selected.project);
        if ($scope.selected.component) {
          $scope.findMolecularActions($scope.selected.component);
        }
      });

      // get the full molecular actions list
      $scope.findMolecularActions = function(component) {
        findMolecularActions(component);
      }
      function findMolecularActions(component) {
        
        projectService.findMolecularActions(component.id, $scope.selected.project.terminology, 
          $scope.selected.project.version, null, null).then(
        // Success
        function(data) {
          $scope.molecularActions = data.actions;
          $scope.molecularActions.totalCount = data.totalCount;
          $scope.getPagedActions();
        });
      }
      
      // Get paged actions (assume all are loaded)
      $scope.getPagedActions = function() {
        getPagedActions();
      }
      function getPagedActions() {

        var pagedArray = utilService.getPagedArray($scope.molecularActions,
          $scope.paging);
        $scope.pagedActions = pagedArray.data;
        $scope.pagedActions.totalCount = pagedArray.totalCount;
      }
      
      // Undo action
      $scope.undoAction = function(action, overrideWarnings) {
        if (!overrideWarnings && action.id != $scope.molecularActions[0].id ) {
          if (window.confirm('Warning.  Do you want to override the warning and execute the undo?')) {
            $scope.undoAction(action, true);
          } else {
            return;
          }
        } else {
          metaEditingService.undoAction($scope.selected.project.id, action.activityId, action.id, 
            overrideWarnings).then(
            // Success
            function(data) {
              $scope.validation = data;         
            });
        }
      }
      
      // Redo action
      $scope.redoAction = function(action, overrideWarnings) {
        metaEditingService.redoAction($scope.selected.project.id, action.activityId, action.id, 
          $scope.overrideWarnings).then(
          // Success
          function(data) {
            $scope.validation = data;           
          });
      }
      
      // returns if the action is undoable
      $scope.isUndoable = function(action) {
        var lowestNotUndone = -1;
        for (var i = 0; i < $scope.molecularActions.length; i++) {
          if (lowestNotUndone == -1 && !$scope.molecularActions[i].undoneFlag) {
            lowestNotUndone = i;
          }
        }
        for (var i = 0; i < $scope.molecularActions.length; i++) {
          if (action.id == $scope.molecularActions[i].id) {
            if ( i == lowestNotUndone ) {            
              return true;
            } else {
              return false;
            }
          }
        }
      }
      
      // returns if the action is redoable
      $scope.isRedoable = function(action) {
        var highestUndone = -1;
        for (var i = 0; i < $scope.molecularActions.length; i++) {
          if ($scope.molecularActions[i].undoneFlag == true) {
            highestUndone = i;
          }
        }
        for (var i = 0; i < $scope.molecularActions.length; i++) {
          if (action.id == $scope.molecularActions[i].id) {
            if (i == highestUndone ) {            
              return true;
            } else {
              return false;
            }
          }
        }
      }
      
      // Convert date to a string
      $scope.toDate = function(lastModified) {
        return utilService.toDate(lastModified);
      };

    } ]
  };
} ]);
