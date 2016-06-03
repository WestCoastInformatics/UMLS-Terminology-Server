// Route
tsApp.config(function configureRoutes($routeProvider, appConfig) {

  console.debug('Configuring routes', appConfig);

  if (!appConfig) {
    console.error('Application configuration could not be retrieved, see appConfig.js');
  }
  if (appConfig && !appConfig.enabledTabs) {
    console.error('No tabs specified for user view in appConfig.js');
  }

  $routeProvider.when('/configure', {
    templateUrl : 'app/page/configure/configure.html',
    controller : 'ConfigureCtrl',
    reloadOnSearch : false
  });

  // Source Data Configurations
  if (appConfig.enabledTabs && appConfig.enabledTabs.split(',').indexOf('source') != -1
    && appConfig.loginEnabled === 'true') {
    console.debug('Route enabled: source');
    $routeProvider.when('/source', {
      controller : 'SourceCtrl',
      templateUrl : 'app/page/source/source.html',
      reloadOnSearch : false
    });
  }

  // Content -- Default Mode
  if (appConfig.enabledTabs && appConfig.enabledTabs.split(',').indexOf('content') != -1) {
    console.debug('Route enabled: content');
    $routeProvider.when('/content', {
      templateUrl : 'app/page/content/content.html',
      controller : 'ContentCtrl',
      reloadOnSearch : false
    });

    // Content with mode set (e.g. 'simple' for component report)
    $routeProvider.when('/content/:mode/:type/:terminology/:version/:terminologyId', {
      templateUrl : function(urlAttr) {
        return 'app/page/content/' + urlAttr.mode + '.html';
      },
      controller : 'ContentCtrl',
      reloadOnSearch : false
    });
  }

  // Metadata View
  if (appConfig.enabledTabs && appConfig.enabledTabs.split(',').indexOf('metadata') != -1) {
    console.debug('Route enabled: metadata');
    $routeProvider.when('/metadata', {
      templateUrl : 'app/page/metadata/metadata.html',
      controller : 'MetadataCtrl',
      reloadOnSearch : false
    });
  }

  // Administrative Page
  if (appConfig.enabledTabs && appConfig.enabledTabs.split(',').indexOf('admin') != -1) {
    console.debug('Route enabled: admin');
    $routeProvider.when('/admin', {
      templateUrl : 'app/page/admin/admin.html',
      controller : 'AdminCtrl',
      reloadOnSearch : false
    });
  }

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

  // if none enabled, default is content/
  if (appConfig.landingEnabled !== 'true' && appConfig.loginEnabled !== 'true'
    && appConfig.licenseEnabled !== 'true') {
    console.debug('No landing, license, or login pages -- default route is /content')
    $routeProvider.when('/', {
      templateUrl : 'app/page/content/content.html',
      controller : 'ContentCtrl',
      reloadOnSearch : false
    });
  }

  // otherwise, redirect to root
  $routeProvider.otherwise({
    redirectTo : '/'
  });

});
