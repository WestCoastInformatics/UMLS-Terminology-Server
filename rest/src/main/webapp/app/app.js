'use strict';

// For dynamic configuring of routes
// See:
// http://blog.brunoscopelliti.com/how-to-defer-route-definition-in-an-angularjs-web-app/
var $routeProviderReference;

var tsApp = angular.module(
  'tsApp',
  [ 'ngRoute', 'ui.bootstrap', 'ui.tree', 'ngFileUpload', 'ui.tinymce', 'ngCookies', 'ngTable',
    'angularFileUpload' ]).config(function($rootScopeProvider, $routeProvider) {

  // Set recursive digest limit higher to handle very deep trees.
  $rootScopeProvider.digestTtl(15);
  // Save reference to route provider
  $routeProviderReference = $routeProvider;

});

// Declare any $rootScope vars
tsApp.run(function($rootScope) {
  // n/a
});

// Simple glass pane controller
tsApp.controller('GlassPaneCtrl', [ '$scope', 'gpService', function($scope, gpService) {
  console.debug('configure GlassPaneCtrl');

  $scope.glassPane = gpService.glassPane;

} ]);

// Simple error controller
tsApp.controller('ErrorCtrl', [ '$scope', 'utilService', function($scope, utilService) {
  console.debug('configure ErrorCtrl');

  $scope.error = utilService.error;

  $scope.clearError = function() {
    utilService.clearError();
  };

  $scope.setError = function(message) {
    utilService.setError(message);
  };

  $scope.success = utilService.success;

  $scope.clearSuccess = function() {
    utilService.clearSuccess();
  };

  $scope.setSuccess = function(message) {
    utilService.setSuccess(message);
  };

} ]);

// Confirm dialog conroller and directive

// Confirm dialog conroller and directive
tsApp.controller('ConfirmModalCtrl', function($scope, $uibModalInstance, data) {
  // Local data for scope
  $scope.data = angular.copy(data);

  // OK function
  $scope.ok = function() {
    $uibModalInstance.close();
  };
  // Cancel function
  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };
});

tsApp
  .value(
    '$confirmModalDefaults',
    {
      template : '<div class="modal-header"><h3 class="modal-title">Confirm</h3></div><div class="modal-body">{{data.text}}</div><div class="modal-footer"><button class="btn btn-primary" ng-click="ok()">OK</button><button class="btn btn-warning" ng-click="cancel()">Cancel</button></div>',
      controller : 'ConfirmModalCtrl'
    });

tsApp.factory('$confirm', function($uibModal, $confirmModalDefaults) {
  return function(data, settings) {
    var lsettings = angular.extend($confirmModalDefaults, (settings || {}));

    if ('templateUrl' in lsettings && 'template' in lsettings) {
      delete lsettings.template;
    }

    lsettings.resolve = {
      data : function() {
        return data || {};
      }
    };

    return $uibModal.open(lsettings).result;
  };
});

tsApp.directive('confirm', function($confirm) {
  return {
    priority : 1,
    restrict : 'A',
    scope : {
      confirmIf : '=',
      ngClick : '&',
      confirm : '@'
    },
    link : function(scope, element, attrs) {
      function reBind(func) {

        element.unbind('click').bind('click', function() {
          func();
        });
      }

      function bindConfirm() {
        $confirm({
          text : scope.confirm
        }).then(scope.ngClick);
      }

      if ('confirmIf' in attrs) {

        scope.$watch('confirmIf', function(newVal) {
          if (newVal || newVal === undefined) {
            reBind(bindConfirm);
          } else {
            reBind(function() {
              scope.$apply(scope.ngClick);
            });
          }
        });
      } else {
        reBind(bindConfirm);
      }
    }
  };
});

// Filter for ordering by key
tsApp.filter('toArrayKeys', function() {
  return function(obj, field, reverse) {
    var arr = [];
    if (obj != null) {
      arr = Object.keys(obj);
    }
    return arr;
  };
});
