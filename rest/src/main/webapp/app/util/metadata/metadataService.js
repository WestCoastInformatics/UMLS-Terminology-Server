// MetadataService
var metadataUrl = 'metadata';
tsApp.service('metadataService', [ '$http', '$q', 'gpService', 'utilService', 'tabService',
  function($http, $q, gpService, utilService, tabService) {
    console.debug('configure metadataService');

    var metadata = getDefaultMetadata();

    // Default for metadata
    function getDefaultMetadata() {
      return {
        terminology : null,
        version : null,
        entries : null,
        languages : null,
        relationshipTypes : null,
        attributeNames : null,
        termTypes : null,
        generalEntries : null,
        labelSets : null,
        atomsLabel : 'Atoms',
        hierarchiesLabel : 'Hierarchies',
        attributesLabel : 'Attributes',
        definitionsLabel : 'Definitions',
        mappingsLabel : 'Mappings',
        subsetsLabel : 'Subsets',
        relationshipsLabel : 'Relationships',
        semanticTypesLabel : 'Semantic Types',
        atomRelationshipsLabel : 'Relationships',
        extensionsLabel : 'Extensions',
        obsoleteLabel : 'Obsolete',
        obsoleteIndicator : 'O',
        suppressibleLabel : 'Suppressible',
        suppressibleIndicator : 'S',
        treeSortField : 'nodeName'
      };
    }
    // Share a metadata model across controllers
    this.setModel = function(model) {
      for ( var key in model) {
        metadata[key] = model[key];
      }
    }

    // Share a terminology model across controllers
    this.setTerminology = function(terminology) {
      for ( var key in terminology) {
        metadata.terminology[key] = terminology[key];
      }
    }

    this.getModel = function() {
      return metadata;
    }

    // Returns all metadata for terminology/version
    this.getAllMetadata = function(terminology, version) {
      console.debug('getAllMetadata', terminology, version);
      var deferred = $q.defer();
      // get metadata
      gpService.increment();
      $http.get(metadataUrl + '/all/' + terminology + '/' + version).then(
      // success
      function(response) {
        // declare metadata defaults
        var metadata = getDefaultMetadata();
        metadata.terminology = {};
        metadata.terminology.terminology = terminology;
        metadata.terminology.version = version;
        metadata.entries = response.data.keyValuePairLists;

        // Fail if no terminology found
        if (metadata.terminology == null) {
          gpService.decrement();
          deferred.reject('Unable to find terminology');
          return;
        }

        for (var i = 0; i < metadata.entries.length; i++) {
          // extract relationship types for
          // convenience
          if (metadata.entries[i].name === 'Relationship_Types') {
            metadata.relationshipTypes = metadata.entries[i].keyValuePairs;
          }
          if (metadata.entries[i].name === 'Languages') {
            metadata.languages = metadata.entries[i].keyValuePairs;
          }
          if (metadata.entries[i].name === 'Attribute_Names') {
            metadata.attributeNames = metadata.entries[i].keyValuePairs;
          }
          if (metadata.entries[i].name === 'Term_Types') {
            metadata.termTypes = metadata.entries[i].keyValuePairs;
          }
          if (metadata.entries[i].name === 'Semantic_Types') {
            metadata.semanticTypes = metadata.entries[i].keyValuePairs;
          }
          if (metadata.entries[i].name === 'Label_Sets') {
            metadata.labelSets = metadata.entries[i].keyValuePairs;
          }
          if (metadata.entries[i].name === 'General_Metadata_Entries') {
            metadata.generalEntries = metadata.entries[i].keyValuePairs;

            for (var j = 0; j < metadata.generalEntries.length; j++) {
              if (metadata.generalEntries[j].key === 'Atoms_Label') {
                metadata.atomsLabel = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Hierarchies_Label') {
                metadata.hierarchiesLabel = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Definitions_Label') {
                metadata.definitionsLabel = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Languages_Label') {
                metadata.languagesLabel = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Attributes_Label') {
                metadata.attributesLabel = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Subsets_Label') {
                metadata.subsetsLabel = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Relationships_Label') {
                metadata.relationshipsLabel = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Semantic_Type_Label') {
                metadata.semanticTypeLabel = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Atom_Relationships_Label') {
                metadata.atomRelationshipsLabel = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Extensions_Label') {
                metadata.extensionsLabel = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Obsolete_Label') {
                metadata.obsoleteLabel = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Obsolete_Indicator') {
                metadata.obsoleteIndicator = metadata.generalEntries[j].value;
              }
              if (metadata.generalEntries[j].key === 'Tree_Sort_Field') {
                metadata.treeSortField = metadata.generalEntries[j].value;
              }
            }
          }
        }
        gpService.decrement();
        deferred.resolve(metadata);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });

      return deferred.promise;
    }

    // Gets the precedence for the terminology/version
    this.getPrecedenceList = function(terminology, version) {
      console.debug('getPrecedence', terminology, version);
      // get precedence
      var deferred = $q.defer();
      gpService.increment();
      $http.get(metadataUrl + '/precedence/' + terminology + '/' + version).then(
      // success
      function(response) {
        console.debug('  precedence = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });

      // Return all deferred promises
      return deferred.promise;

    };

    // Returns the terminology object for the terminology name
    this.getTerminology = function(terminology, version) {
      console.debug('getTerminology', terminology, version);
      // get precedence
      var deferred = $q.defer();
      gpService.increment();
      $http.get(metadataUrl + '/terminology/' + terminology + '/' + version).then(
      // success
      function(response) {
        console.debug('  terminology = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });

      // Return all deferred promises
      return deferred.promise;

    };

    // Get semantic types
    this.getSemanticTypes = function(terminology, version) {
      console.debug('getSemanticTypes', terminology, version);
      var deferred = $q.defer();

      gpService.increment();
      $http.get(metadataUrl + '/sty/' + terminology + '/' + version).then(
      // Success
      function(response) {
        console.debug('  stys = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // Error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });

      return deferred.promise;
    };

    // get relationship type name from its abbreviation
    this.getRelationshipTypeName = function(abbr, metadata) {
      for (var i = 0; i < metadata.relationshipTypes.length; i++) {
        if (metadata.relationshipTypes[i].key === abbr) {
          return metadata.relationshipTypes[i].value;
        }
      }
      return null;
    };

    // get attribute name name from its abbreviation
    this.getAttributeNameName = function(abbr, metadata) {
      for (var i = 0; i < metadata.attributeNames.length; i++) {
        if (metadata.attributeNames[i].key === abbr) {
          return metadata.attributeNames[i].value;
        }
      }
      return null;
    };

    // get term type name from its abbreviation
    this.getTermTypeName = function(abbr, metadata) {
      for (var i = 0; i < metadata.termTypes.length; i++) {
        if (metadata.termTypes[i].key === abbr) {
          return metadata.termTypes[i].value;
        }
      }
      return null;
    };

    // get general entry name from its abbreviation
    this.getGeneralEntryValue = function(abbr, metadata) {
      for (var i = 0; i < metadata.generalEntries.length; i++) {
        if (metadata.generalEntries[i].key === abbr) {
          return metadata.generalEntries[i].value;
        }
      }
      return null;
    };

    // get label sets name
    this.getLabelSetName = function(abbr, metadata) {
      for (var i = 0; i < metadata.labelSets.length; i++) {
        if (metadata.labelSets[i].key === abbr) {
          return metadata.labelSets[i].value;
        }
      }
      return null;
    };

    // indicates whether this is a derived label set
    this.isDerivedLabelSet = function(tree) {
      for (var i = 0; i < tree.labels.length; i++) {
        if (tree.labels[i].startsWith('LABELFOR')) {
          return true;
        }
      }
      return false;
    };

    // Indicates whether this is a label set
    this.isLabelSet = function(tree) {
      for (var i = 0; i < tree.labels.length; i++) {
        if (!tree.labels[i].startsWith('LABELFOR:')) {
          return true;
        }
      }
      return false;
    };

    // for popovers
    this.getDerivedLabelSetsValue = function(tree, metadata) {
      if (tree.labels == undefined) {
        return;
      }
      var retval = 'Ancestor of content in:<br>';
      var j = 0;
      for (var i = 0; i < tree.labels.length; i++) {
        var name = this.getLabelSetName(tree.labels[i], metadata);
        if (tree.labels[i].startsWith('LABELFOR')) {
          if (j++ > 0) {
            retval += '<br>';
          }
          retval += '&#x2022;&nbsp;' + name;
        }
      }
      return retval;
    };

    // For popovers
    this.getLabelSetsValue = function(tree, metadata) {
      if (tree.labels == undefined) {
        return;
      }
      var retval = 'Content in:<br>';
      var j = 0;
      for (var i = 0; i < tree.labels.length; i++) {
        var name = this.getLabelSetName(tree.labels[i], metadata);
        if (!tree.labels[i].startsWith('LABELFOR')) {
          if (j++ > 0) {
            retval += '<br>';
          }
          retval += '&#x2022;&nbsp;' + name;
        }
      }
      return retval;
    };

    // Returns the label count
    this.countLabels = function(component) {
      var retval = 0;
      if (typeof component == 'undefined' || !component) {
        return 0;
      }
      if (typeof component.labels == 'undefined') {
        return 0;
      }
      for (var i = 0; i < component.labels.length; i++) {
        if (!component.labels[i].startsWith('LABELFOR')) {
          retval++;
        }
      }
      return retval;
    };

    // Return metadata callbacks
    this.getCallbacks = function() {
      return {
        getAllMetadata : this.getAllMetadata,
        getTerminologies : this.getTerminologies,
        getTerminology : this.getTerminology,
        getRelationshipTypeName : this.getRelationshipTypeName,
        getAttributeNameName : this.getAttributeNameName,
        getTermTypeName : this.getTermTypeName,
        getGeneralEntryValue : this.getGeneralEntryValue,
        getLabelSetName : this.getLabelSetName,
        isDerivedLabelSet : this.isDerivedLabelSet,
        isLabelSet : this.isLabelSet,
        getDerivedLabelSetsValue : this.getDerivedLabelSetsValue,
        getLabelSetsValue : this.getLabelSetsValue,
        countLabels : this.countLabels
      }
    }

    // Get current terminologies
    this.getTerminologies = function() {
      console.debug('getTerminologies');
      var deferred = $q.defer();

      // Get terminologies
      gpService.increment();
      $http.get(metadataUrl + '/terminology/current').then(
      // success
      function(response) {
        console.debug('  terminologies = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

  }

]);
