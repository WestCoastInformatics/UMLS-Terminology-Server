// Application Configuration
// These values are derived from either pom.xml properties
// or config.properties file settings.
// NOTE:  If additional properties added, update term-server-rest/pom.xml dev-windows defaults
tsApp.constant('appConfig', {

  // deployment display variables
  deployLink : '${deploy.link}',
  deployTitle : '${deploy.title}',
  deployPasswordReset : '${deploy.password.reset}',
  deployPresentedBy : '${deploy.presented.by}',
  deployCopyright : '${deploy.footer.copyright}',

  // project variables
  projectVersion : '${project.version}',

  // routing variables
  landingEnabled : '${deploy.landing.enabled}',
  licenseEnabled : '${deploy.license.enabled}',
  loginEnabled : '${deploy.login.enabled}',

  // other
  siteTrackingCode : '${site.tracking.code}'

});
