'use strict'

var tsApp = angular.module('tsApp', [ 'ngRoute', 'ui.bootstrap' ]).config(
  function() {

  });

// Declare top level URL vars
var securityUrl = "security/";
var metadataUrl = "metadata/";
var contentUrl = "content/";

// Initialization of tsApp
tsApp.run(function($rootScope, $http, $location) {

});

console.debug('configure route provider');
// Route provider configuration
tsApp.config([ '$routeProvider', function($routeProvider) {

  $routeProvider.when('/', {
    templateUrl : 'ui2/partials/login.html',
    controller : 'LoginCtrl'
  }).when('/content', {
    templateUrl : 'ui2/partials/content.html',
    controller : 'ContentCtrl'
  }).when('/metadata', {
    templateUrl : 'ui2/partials/metadata.html',
    controller : 'MetadataCtrl'
  }).otherwise({
    redirectTo : '/'
  });

} ]);

// Simple glass pane controller
console.debug('configure GlassPaneCtrl');
tsApp.controller('GlassPaneCtrl', [ '$scope', 'gpService',
  function($scope, gpService) {

    $scope.glassPane = gpService.glassPane;

  } ]);

// Simple error controller
console.debug('configure ErrorCtrl');
tsApp.controller('ErrorCtrl', [ '$scope', 'errorService',
  function($scope, errorService) {

    $scope.error = errorService.error;

    $scope.clearError = function() {
      errorService.clearError();
    }

    $scope.setError = function(message) {
      errorService.setError(message);
    }

  } ]);

// Tab controller
console.debug('configure TabCtrl');
tsApp.controller('TabCtrl', [
  '$scope',
  '$interval',
  '$timeout',
  'securityService',
  'tabService',
  function($scope, $interval, $timeout, securityService, 
    tabService) {

    // Setup tabs
    $scope.tabs = tabService.tabs;

    // Set selected tab (change the view)
    $scope.setSelectedTab = function(tab) {
      tabService.setSelectedTab(tab);
    }

    // Set "active" or not
    $scope.tabClass = function(tab) {
      if (tabService.selectedTab == tab) {
        return "active";
      } else {
        return "";
      }
    }

    // for ng-show
    $scope.isShowing = function() {
      return securityService.isLoggedIn();
    }

  } ]);

// Header controller
console.debug('configure HeaderCtrl');
tsApp.controller('HeaderCtrl', [ '$scope', 'securityService',
  function($scope, securityService) {

    // Declare user
    $scope.user = securityService.getUser();

    // Logout method
    $scope.logout = function() {
      securityService.logout();
    }
  } ]);

// Footer controller
console.debug('configure FooterCtrl');
tsApp.controller('FooterCtrl', [ '$scope', 'gpService', 'securityService',
  function($scope, gpService, securityService) {
    // Declare user
    $scope.user = securityService.getUser();

    // Logout method
    $scope.logout = function() {
      securityService.logout();
    }

    // Check gp status
    $scope.isGlassPaneNegative = function() {
      return gpService.isGlassPaneNegative();
    }

    // Get gp counter
    $scope.getGlassPaneCounter = function() {
      return gpService.glassPane.counter;
    }

  } ]);
