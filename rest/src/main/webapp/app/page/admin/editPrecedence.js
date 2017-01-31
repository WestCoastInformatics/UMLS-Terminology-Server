// Finder directive
tsApp.directive('editPrecedence', [ function() {
  return {
    restrict : 'A',
    scope : {
      selected : '=',
      lists : '=',
      callbacks : '='
    },
    templateUrl : 'app/page/admin/editPrecedence.html',
    controller : [ '$scope', '$uibModal', 'utilService', 'metadataService', 'contentService',
      function($scope, $uibModal, utilService, metadataService, contentService) {

      // precedence entries touched
      $scope.entriesTouched = {};
      
      // Paging variables
      $scope.pageSizes = utilService.getPageSizes();
      $scope.paging = {};
      $scope.paging['precedenceList'] = utilService.getPaging();
      $scope.paging['precedenceList'].pageSize = 1000000;
      $scope.paging['precedenceList'].callbacks = {
        getPagedList : getPagedPrecedenceList
      };
      
      $scope.$watch('lists.precedenceList', function() {
        if ($scope.lists.precedenceList) {
          $scope.precedenceOrder = $scope.lists.precedenceList.precedence.keyValuePairs;
          getPagedPrecedenceList();
        }
      });

      $scope.getPagedPrecedenceList = function() {
        console.debug('getprecedencelist');
        getPagedPrecedenceList();
      }
      function getPagedPrecedenceList() {
        $scope.pagedPrecedenceList = utilService.getPagedArray($scope.precedenceOrder,
          $scope.paging['precedenceList']);
        $scope.pagedPrecedenceList.totalCount = $scope.precedenceOrder.length;
      }
      ;
      
      // Check whether this is the first entry in the list
      $scope.isFirstIndex = function(entry) {
        return $scope.isEquivalent(entry, $scope.precedenceOrder[0]);
      }

      // Check whether this is the last entry in the list
      $scope.isLastIndex = function(entry) {
        return $scope.isEquivalent(entry,
          $scope.precedenceOrder[$scope.precedenceOrder.length - 1]);
      }

      // Update the precedence list.
      $scope.updatePrecedenceList = function() {
        $scope.entriesTouched = {};
        $scope.lists.precedenceList.precedence.keyValuePairs = $scope.precedenceOrder;
        metadataService.updatePrecedenceList($scope.lists.precedenceList);
      }
      

      // Move a termgroup up in precedence list
      $scope.moveTermgroupUp = function(termgroup) {
        $scope.entriesTouched[termgroup.key + termgroup.value] = 1;
        // Start at index 1 because we can't move the top one up
        for (var i = 1; i < $scope.precedenceOrder.length; i++) {
          if ($scope.isEquivalent(termgroup, $scope.precedenceOrder[i])) {
            $scope.precedenceOrder.splice(i, 1);
            $scope.precedenceOrder.splice(i - 1, 0, termgroup);
          }
        }
        $scope.getPagedPrecedenceList();
      };

      // Move a termgroup down in precedence list
      $scope.moveTermgroupDown = function(termgroup) {
        $scope.entriesTouched[termgroup.key + termgroup.value] = 1;
        // end at index -11 because we can't move the last one down
        for (var i = 0; i < $scope.precedenceOrder.length - 1; i++) {
          if ($scope.isEquivalent(termgroup, $scope.precedenceOrder[i])) {
            $scope.precedenceOrder.splice(i, 2, $scope.precedenceOrder[i + 1],
              termgroup);
            break;
          }
        }
        $scope.getPagedPrecedenceList();
      };

      // equivalent test for termgroups
      $scope.isEquivalent = function(tgrp1, tgrp2) {
        return tgrp1.key == tgrp2.key && tgrp1.value == tgrp2.value;
      };

      
        // end
      } ]

  };
} ]);
