// Permissions
tsApp.run([ 'securityService', function(securityService) {
  console.debug('Adding application permissions for UI visibility');

  // permissions for determining visibility in ui
  securityService.addPermission('CreateWorklist', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('CreateChecklist', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('RegenerateBins', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('RecomputeConceptStatus', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('UndoRedo', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('GenerateReport', {
    'REVIEWER' : true,
    'AUTHOR' : true,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('ImportChecklist', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('RemoveChecklist', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('RemoveWorklist', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('Stamp', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('Unapprove', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('AssignWorklist', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('UnassignWorklist', {
    'REVIEWER' : true,
    'AUTHOR' : false,
    'EDITOR5' : true,
    'ADMINISTRATOR' : true
  });
  securityService.addPermission('EditProjectOrUser', {
    'REVIEWER' : false,
    'AUTHOR' : false,
    'EDITOR5' : false,
    'ADMINISTRATOR' : false,
    'APP_ADMINISTRATOR' : true
  });
  securityService.addPermission('AddProjectOrUser', {
    'REVIEWER' : false,
    'AUTHOR' : false,
    'EDITOR5' : false,
    'ADMINISTRATOR' : false,
    'APP_USER' : true
  });
  securityService.addPermission('EditProcessOrStep', {
    'REVIEWER' : false,
    'AUTHOR' : false,
    'EDITOR5' : false,
    'ADMINISTRATOR' : true
  });

} ]);