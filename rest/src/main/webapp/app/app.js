'use strict';

var tsApp = angular
  .module(
    'tsApp',
    [ 'ngRoute', 'ui.bootstrap', 'ui.tree', 'ui.tinymce', 'ngCookies', 'ngTable',
      'angularFileUpload' ]).config(function($rootScopeProvider) {

    // Set recursive digest limit higher to handle very deep trees.
    $rootScopeProvider.digestTtl(15);

  });

// Declare top level URL vars
var securityUrl = 'security/';
var metadataUrl = 'metadata/';
var contentUrl = 'content/';
var adminUrl = 'admin/';
var projectUrl = 'project/';
var validationUrl = 'validation/';
var sourceDataUrl = 'file/';
var configureUrl = 'configure/';

tsApp.run(function checkConfig($rootScope, $http, $route, appConfig, configureService, utilService,
  securityService) {

  var errMsg = '';

  // if appConfig not set or contains nonsensical values, throw error
  if (!appConfig) {
    errMsg += 'Application configuration (appConfig.js) could not be found';
  }

  console.debug('Application configuration variables set:');

  // Iterate through app config variables and verify interpolation
  for ( var key in appConfig) {
    if (appConfig.hasOwnProperty(key)) {
      console.debug('  ' + key + ': ' + appConfig[key]);
      if (appConfig[key].startsWith('${')) {
        errMsg += 'Configuration property ' + key + ' not set in project or configuration file';
      }
    }

    // if login not enabled, set guest user
    if (appConfig.loginEnabled !== 'true') {
      securityService.setGuestUser();
    }

  }

  // TODO Move this into a scope-accessible object of some kind (e.g. site-tracking directive analogous to header/footer)
  $rootScope.siteTrackingCode = appConfig['siteTrackingCode'];

  if (errMsg.length > 0) {
    // Send an embedded 'data' object
    utilService.handleError({
      data : 'Configuration Error:\n' + errMsg
    });
  }

  // check and set whether application is configured
  $http.get(configureUrl + 'configured').then(function(response) {
    $rootScope.isConfigured = response.data;
  }, function() {
    console.error('Could not determine configuration status');
    $rootScope.isConfigured = false;
  });
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
