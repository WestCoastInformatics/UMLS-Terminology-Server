// Application Configuration
// These values are derived from either pom.xml properties
// or config.properties file settings.
// NOTE:  If additional properties added, update term-server-rest/pom.xml dev-windows defaults
console.debug("Initialize appConfig");
tsApp.constant('appConfig', {

  // project variables
  projectVersion : '${project.version}',

});
