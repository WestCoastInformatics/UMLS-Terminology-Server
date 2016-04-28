// Route
tsApp.config(function configureRoutes($routeProvider, appConfig) {

  $routeProvider.when('/configure', {
    templateUrl : 'app/page/configure/configure.html',
    controller : 'ConfigureCtrl',
    reloadOnSearch : false,
    resolve : {
      'configured' : function($rootScope, $location) {
        if ($rootScope.isConfigured) {
          $location.path('/');
        }
      }
    }
  });

  // Source Data Configurations
  $routeProvider.when('/source', {
    controller : 'SourceCtrl',
    templateUrl : 'app/page/source/source.html',
    resolve : {
      'configured' : function($rootScope, $location) {
        if (!$rootScope.isConfigured) {
          $location.path('/configure');
        }
      }
    }
  });

  // Content -- Default Mode
  $routeProvider.when('/content', {
    templateUrl : 'app/page/content/content.html',
    controller : 'ContentCtrl',
    reloadOnSearch : false,
    resolve : {
      'configured' : function($rootScope, $location) {
        if (!$rootScope.isConfigured) {
          $location.path('/configure');
        }
      }
    }
  });

  // Content with mode set (e.g. 'simple' for component report)
  $routeProvider.when('/content/:mode/:terminology/:version/:terminologyId', {
    templateUrl : function(urlAttr) {
      return 'app/page/content/' + urlAttr.mode + '.html';
    },
    controller : 'ContentCtrl',
    reloadOnSearch : false,
    resolve : {
      'configured' : function($rootScope, $location) {
        if (!$rootScope.isConfigured) {
          $location.path('/configure');
        }
      }
    }
  });

  // Metadata View

  $routeProvider.when('/metadata', {
    templateUrl : 'app/page/metadata/metadata.html',
    controller : 'MetadataCtrl',
    reloadOnSearch : false,
    resolve : {
      'configured' : function($rootScope, $location) {
        if (!$rootScope.isConfigured) {
          $location.path('/configure');
        }
      }
    }
  });

  // Administrative Page
  $routeProvider.when('/admin', {
    templateUrl : 'app/page/admin/admin.html',
    controller : 'AdminCtrl',
    reloadOnSearch : false,
    resolve : {
      'configured' : function($rootScope, $location) {
        if (!$rootScope.isConfigured) {
          $location.path('/configure');
        }
      }
    }
  });

  if (appConfig.loginEnabled) {
    $routeProvider.when('/login', {
      templateUrl : 'app/page/login/login.html',
      controller : 'LoginCtrl',
      reloadOnSearch : false,
      resolve : {
        'configured' : function($rootScope, $location) {
          if (!$rootScope.isConfigured) {
            $location.path('/configure');
          }
        }
      }
    });
  }

  if (appConfig.landingEnabled) {
    $routeProvider.when('/landing', {
      templateUrl : 'app/page/landing/landing.html',
      controller : 'LandingCtrl',
      reloadOnSearch : false,
      resolve : {
        'configured' : function($rootScope, $location) {
          if (!$rootScope.isConfigured) {
            $location.path('/configure');
          }
        }
      }
    });
  }

  if (appConfig && appConfig.loginEnabled) {
    $routeProvider.when('/login', {
      templateUrl : 'app/page/login/login.html',
      controller : 'LoginCtrl',
      reloadOnSearch : false,
      resolve : {
        'configured' : function($rootScope, $location) {
          if (!$rootScope.isConfigured) {
            $location.path('/configure');
          }
        }
      }
    });
  }

  // if landing -> landing
  // if not landing & login -> login
  // if not landing & not login -> license
  // otherwise -> content
  $routeProvider.otherwise({
    redirectTo : function(appConfig) {
      if (appConfig.landingEnabled) {
        return '/landing';
      } else if (appConfig.loginEnabled) {
        return '/login';
      } else if (appConfig.licenseEnabled) {
        return '/license';
      } else {
        return '/content';
      }
    }
  });


});
