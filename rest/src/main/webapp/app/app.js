'use strict'

var tsApp = angular
  .module(
    'tsApp',
    [ 'ngRoute', 'ui.bootstrap', 'ui.tree', 'ui.tinymce', 'ngCookies', 'ngTable',
      'angularFileUpload' ]).config(function($rootScopeProvider) {

    // Set recursive digest limit higher to handle very deep trees.
    $rootScopeProvider.digestTtl(15);

  });

// Declare top level URL vars
var securityUrl = "security/";
var metadataUrl = "metadata/";
var contentUrl = "content/";
var adminUrl = "admin/";
var projectUrl = "project/";
var validationUrl = "validation/";
var sourceDataUrl = 'file/';

// Initialization of tsApp
tsApp.run(function($rootScope, $http, $location) {
  // nothing yet -- may want to put metadata retrieval here
});

// Route provider configuration - MOVED to individual controllers for routes
// e.g. contentController.js, metadataController.js
//tsApp.config([ '$routeProvider', function($routeProvider) {
//  console.debug('configure $routeProvider');
//
//  $routeProvider.otherwise({
//    redirectTo : '/content'
//  });
//} ]);

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
  }

} ]);

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
    settings = angular.extend($confirmModalDefaults, (settings || {}));
    data = data || {};

    if ('templateUrl' in settings && 'template' in settings) {
      delete settings.template;
    }

    settings.resolve = {
      data : function() {
        return data;
      }
    };

    return $uibModal.open(settings).result;
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
          if (newVal) {
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
  }
});
