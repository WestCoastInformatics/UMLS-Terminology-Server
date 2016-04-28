// Route
tsApp.config(function configureRoutes($routeProvider, appConfig) {

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

  // TODO -- Make sensitive to l/l/l flags
  $routeProvider.when('/', {
    templateUrl : 'app/page/login/login.html',
    controller : 'LoginCtrl',
    reloadOnSearch : false
  });
  
  // TODO -- Change this to '/' once landing page complete
  if (appConfig.landingEnabled) {
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
  }
  // TODO Otherwise set to:
  // if landing -> landing
  // if not landing & login -> login
  // if not landing & not login -> license
  // if not all -> content

  // TODO -- Make sensitive to l/l/l flags
  $routeProvider.when('/login', {
    templateUrl : 'app/page/login/login.html',
    controller : 'LoginCtrl',
    reloadOnSearch : false
  });

  $routeProvider.otherwise({
    redirectTo : '/content'
  });

});
