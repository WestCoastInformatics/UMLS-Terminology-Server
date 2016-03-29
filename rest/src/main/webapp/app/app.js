'use strict'

var tsApp = angular.module('tsApp',
  [ 'ngRoute', 'ui.bootstrap', 'ui.tree', 'ui.tinymce', 'ngCookies' ]).config(
  function($rootScopeProvider) {

    // Set recursive digest limit higher to handle very deep trees.
    $rootScopeProvider.digestTtl(15);

  });

// Declare top level URL vars
var securityUrl = "security/";
var metadataUrl = "metadata/";
var contentUrl = "content/";
var adminUrl = "admin/";
var projectUrl = "project/";

// Initialization of tsApp
tsApp.run(function($rootScope, $http, $location) {
  // nothing yet -- may want to put metadata retrieval here
});

// Route provider configuration
tsApp.config([ '$routeProvider', function($routeProvider) {
  console.debug('configure $routeProvider');

  // Set reloadOnSearch so that $location.hash() calls do not reload the
  // controller
  // TODO MOve these to the appropriate directives/controllers
  $routeProvider.when('/login', {
    templateUrl : 'app/page/login/login.html',
    controller : 'LoginCtrl',
    reloadOnSearch : false
  })

  /*
     * .when('/', { templateUrl : 'app/page/landing/landing.html', controller: 'LandingCtrl',
     * reloadOnSearch : false })
     */

  .when('/', {
    templateUrl : 'app/page/login/login.html',
    controller : 'LoginCtrl',
    reloadOnSearch : false
  }).when('/content', {
    templateUrl : 'app/page/content/content.html',
    controller : 'ContentCtrl',
    reloadOnSearch : false
  }).when('/metadata', {
    templateUrl : 'app/page/metadata/metadata.html',
    controller : 'MetadataCtrl',
    reloadOnSearch : false
  }).when('/admin', {
    templateUrl : 'app/page/admin/admin.html',
    controller : 'AdminCtrl',
    reloadOnSearch : false
  }).when('/content/:mode/:terminology/:version/:terminologyId', {
    templateUrl : function(urlAttr) {
      return 'app/page/content/' + urlAttr.mode + '.html';
    },
    controller : 'ContentCtrl',
    reloadOnSearch : false
  }).otherwise({
    redirectTo : '/content'
  });
} ]);

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

} ]);

// Tab controller
tsApp.controller('TabCtrl', [
  '$scope',
  '$routeParams',
  '$interval',
  '$timeout',
  '$location',
  'securityService',
  'tabService',
  function($scope, $routeParams, $interval, $timeout, $location, securityService, tabService) {
    console.debug('configure TabCtrl');

    // Setup tabs
    $scope.tabs = tabService.tabs;

    // Set selected tab (change the view)
    $scope.setSelectedTab = function(tab) {
      tabService.setSelectedTab(tab);
    };

    // Set "active" or not
    $scope.tabClass = function(tab) {
      if (tabService.selectedTab == tab) {
        return "active";
      } else {
        return "";
      }
    };

    // tabs are only shown if logged in and not on the landing or login page
    $scope.isShowing = function() {
      switch ($routeParams.mode) {
      case 'simple':
        return false;
      default:
        return securityService.isLoggedIn() && $location.url() !== '/'
          && $location.url() !== '/#top' && $location.url().indexOf('login') == -1;

      }
    };

    // configure whether certain tabs require permissions here
    $scope.isTabShowing = function(tab) {
      return isAdmin() || tab.label == 'Directory' || userHasAnyRole;
    };

    // returns true if a user has a defined role on a project
    $scope.userHasAnyRole = function() {
      return userProjectsInfo.anyrole;
    };

    // returns true if user has administrator application role
    $scope.isAdmin = function() {
      return user.applicationRole == 'ADMIN';
    };
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
