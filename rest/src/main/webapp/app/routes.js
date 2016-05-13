// Route
tsApp.config(function configureRoutes($routeProvider, appConfig) {

  console.debug('Configure routes');

  $routeProvider.when('/configure', {
    templateUrl : 'app/page/configure/configure.html',
    controller : 'ConfigureCtrl',
    reloadOnSearch : false
  });

  // Source Data Configurations
  $routeProvider.when('/source', {
    controller : 'SourceCtrl',
    templateUrl : 'app/page/source/source.html',
    reloadOnSearch : false
  });

  // Content -- Default Mode
  $routeProvider.when('/content', {
    templateUrl : 'app/page/content/content.html',
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

  //
  // Configurable routes
  //

  var loginRoute = {
    templateUrl : 'app/page/login/login.html',
    controller : 'LoginCtrl',
    reloadOnSearch : false
  };

  var landingRoute = {
    templateUrl : 'app/page/landing/landing.html',
    controller : 'LandingCtrl',
    reloadOnSearch : false
  };

  var licenseRoute = {
    templateUrl : 'app/page/license/license.html',
    controller : 'LicenseCtrl',
    reloadOnSearch : false
  };

  // if landing enabled
  if (appConfig && appConfig.landingEnabled === 'true') {
    $routeProvider.when('/landing', landingRoute);
    $routeProvider.when('/', landingRoute);
  }

  // if login enabled
  if (appConfig && appConfig.loginEnabled === 'true') {
    $routeProvider.when('/login', loginRoute);
    if (appConfig && appConfig.landingEnabled !== 'true') {
      $routeProvider.when('/', loginRoute);
    }
  }

  // if license enabled
  if (appConfig && appConfig.licenseEnabled === 'true') {
    $routeProvider.when('/license', licenseRoute);
    if (appConfig && appConfig.landingEnabled !== 'true' && appConfig.loginEnabled !== 'true') {
      $routeProvider.when('/', licenseRoute);
    }
  }

  // otherwise, redirect to content
  $routeProvider.otherwise({
    redirectTo : '/content'
  });

});
