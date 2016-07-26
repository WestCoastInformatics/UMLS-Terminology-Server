// Application Configuration
// These values are derived from either pom.xml properties
// or config.properties file settings.
// NOTE:  If additional properties added, update term-server-rest/pom.xml dev-windows defaults
console.debug("Initialize appConfig");
tsApp.constant('appConfig', {

  // deployment display variables
  deployLink : '',
  deployTitle : '',
  deployPasswordReset : '',
  deployPresentedBy : '',
  deployCopyright : '',
  deployFeedbackEmail : '',

  // project variables
  projectVersion : '${project.version}',

  // routing variables
  enabledTabs : '',
  landingEnabled : '',
  licenseEnabled : '',
  loginEnabled : '',

  // other
  siteTrackingCode : ''

});
