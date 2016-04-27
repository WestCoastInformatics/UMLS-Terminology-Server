// Route
tsApp.config(function config($routeProvider) {
  
  // TODO -- Change this to '/' once landing page complete
  $routeProvider.when('/configure', {
    templateUrl : 'app/page/configure/configure.html',
    controller : 'ConfigureCtrl',
    reloadOnSearch : false,
    resolve : {
      'configured' : function($rootScope, $location) {
        if ($rootScope.isConfigured) {
          $location.path('/login');
        }
      }
    }
  });
  
  // TODO -- Change this to '/' once landing page complete
  $routeProvider.when('/landing', {
    templateUrl : 'app/page/landing/landing.html',
    controller : 'LandingCtrl',
    reloadOnSearch : false,
    resolve : {
      'configured' : function($rootScope, $location) {
        if ($rootScope.isConfigured) {
          $location.path('/configure');
        }
      }
    }
  });
  
  // Source Data Configurations
  $routeProvider.when('/source', {
    controller : 'SourceCtrl',
    templateUrl : 'app/page/source/source.html'
  });
  

  // Content -- Default Mode
  $routeProvider.when('/content', {
    templateUrl : function() {
      return 'app/page/content/content.html';
    },
    controller : 'ContentCtrl',
    reloadOnSearch : false
  });

  // Content with mode set (e.g. 'simple' for component report)
  $routeProvider.when('/content/:mode/:terminology/:version/:terminologyId', {
    templateUrl : function(urlAttr) {
      return 'app/page/content/' + urlAttr.mode + '.html';
    },
    controller : 'ContentCtrl',
    reloadOnSearch : false
  });

  // Metadata View

  $routeProvider.when('/metadata', {
    templateUrl : 'app/page/metadata/metadata.html',
    controller : 'MetadataCtrl',
    reloadOnSearch : false
  });
  
  // Administrative Page
  $routeProvider.when('/admin', {
    templateUrl : 'app/page/admin/admin.html',
    controller : 'AdminCtrl',
    reloadOnSearch : false
  });

  // TODO -- Change to landing once landing page complete
  $routeProvider.when('/', {
    templateUrl : 'app/page/login/login.html',
    controller : 'LoginCtrl',
    reloadOnSearch : false
  });

  // Login page
  $routeProvider.when('/login', {
    templateUrl : 'app/page/login/login.html',
    controller : 'LoginCtrl',
    reloadOnSearch : false
  });
  
  $routeProvider.otherwise({
    redirectTo: '/content'
  });
  
});
