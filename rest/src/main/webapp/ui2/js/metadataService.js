// MetadataService
console.debug("configure metadataService");
tsApp
  .service(
    'metadataService',
    [
      '$http',
      '$q',
      'gpService',
      'errorService',
      function($http, $q, gpService, errorService) {

        // The metadata for current terminology
        var metadata = {
          terminology : null,
          precedenceList : null,
          entries : null,
          relationshipTypes : null,
          attributeNames : null,
          termTypes : null,
          generalEntries : null,
          labelSets : null,
          atomsLabel : "Atoms",
          hierarchiesLabel : "Hierarchies",
          attributesLabel : "Attributes",
          definitionsLabel : "Definitions",
          subsetsLabel : "Subsets",
          relationshipsLabel : "Relationships",
          atomRelationshipsLabel : "References",
          extensionsLabel : "Extensions"
        };

        // Obtain the data model
        this.getMetadata = function() {
          return metadata;
        }

        // Performs service lookup of a terminology object
        this.getTerminology = function(name, version) {
          console.debug("get terminology", name, version);
          var deferred = $q.defer();
          // get terminology
          gpService.increment();
          $http.get(metadataUrl + 'terminology/' + name + '/' + version)
            .then(
            // success
            function(response) {
              metadata.terminology = response.data;
              console.debug("  terminology = ", metadata.terminology);
              gpService.decrement();
              deferred.resolve(response.data);
            },
            // error
            function(response) {
              errorService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // ASYNC
        // Sets terminology and performs lookup of all related metadata
        this.setTerminology = function(terminology) {
          console.debug("set terminology", terminology);
          metadata.terminology = terminology;

          var deferred = $q.defer();

          // get metadata
          gpService.increment();
          $http
            .get(
              metadataUrl + 'all/terminology/' + terminology.terminology
                + '/' + terminology.version)
            .then(
              // success
              function(response) {
                metadata.entries = response.data.keyValuePairList;
                console.debug("  entries = ", metadata.entries);
                metadata.relationshipTypes = null;
                metadata.attributeNames = null;
                metadata.termTypes = null;
                metadata.generalEntries = null;
                metadata.labelSets = null;

                if (metadata.terminology == null)
                  return;

                for (var i = 0; i < metadata.entries.length; i++) {
                  console.debug("metadata.entries["+i+"] = ",metadata.entries[i].name);
                  // extract relationship types for convenience
                  if (metadata.entries[i].name === 'Relationship_Types') {
                    metadata.relationshipTypes = metadata.entries[i].keyValuePair;
                  }
                  if (metadata.entries[i].name === 'Attribute_Names') {
                    metadata.attributeNames = metadata.entries[i].keyValuePair;
                  }
                  if (metadata.entries[i].name === 'Term_Types') {
                    metadata.termTypes = metadata.entries[i].keyValuePair;
                  }
                  if (metadata.entries[i].name === 'Label_Sets') {
                    metadata.labelSets = metadata.entries[i].keyValuePair;
                  }
                  if (metadata.entries[i].name === 'General_Metadata_Entries') {
                    metadata.generalEntries = metadata.entries[i].keyValuePair;

                    for (var j = 0; j < generalEntries.length; j++) {
                      if (metadatageneralEntries[j].key === "Atoms_Label") {
                        metadata.atomsLabel = generalEntries[j].value;
                      }
                      if (metadatageneralEntries[j].key === "Hierarchies_Label") {
                        metadata.hierarchiesLabel = generalEntries[j].value;
                      }
                      if (metadatageneralEntries[j].key === "Definitions_Label") {
                        metadata.definitionsLabel = generalEntries[j].value;
                      }
                      if (metadatageneralEntries[j].key === "Attributes_Label") {
                        metadata.attributesLabel = generalEntries[j].value;
                      }
                      if (metadatageneralEntries[j].key === "Subsets_Label") {
                        metadata.subsetsLabel = generalEntries[j].value;
                      }
                      if (metadatageneralEntries[j].key === "Relationships_Label") {
                        metadata.relationshipsLabel = generalEntries[j].value;
                      }
                      if (metadatageneralEntries[j].key === "Atom_Relationships_Label") {
                        metadata.atomRelationshipsLabel = generalEntries[j].value;
                      }
                      if (metadatageneralEntries[j].key === "Extensions_Label") {
                        metadata.extensionsLabel = generalEntries[j].value;
                      }
                      if (metadatageneralEntries[j].key === "Tree_Sort_Field") {
                        metadata.treeSortField = generalEntries[j].value;
                      }
                    }
                  }
                }
                gpService.decrement();
                deferred.resolve("");
              },
              // error
              function(response) {
                errorService.handleError(response);
                gpService.decrement();
                deferred.reject("");
              });

          // get precedence
          var deferred2 = $q.defer();
          gpService.increment();
          $http.get(
            metadataUrl + 'precedence/' + terminology.terminology + '/'
              + terminology.version).then(
          // success
          function(response) {
            console.debug("  precedenceList = ", response.data);
            metadata.precedenceList = response.data.precedence;
            gpService.decrement();
            deferred2.resolve("");
          },
          // error
          function(response) {
            errorService.handleError(response);
            gpService.decrement();
            deferred2.reject("");
          });
          
          // Return all deferred promises
          return $q.all([deferred.promise, deferred2.promise]);
        }

        // Returns a deferred promise that contains the terminology list
        this.getTerminologies = function() {
          console.debug("get terminologies");
          var deferred = $q.defer();

          // Get terminologies
          gpService.increment()
          $http.get(metadataUrl + 'terminology/terminologies').then(
          // success
          function(response) {
            gpService.decrement();
            console.debug("  terminologies = ", repsonse.data);
            deferred.resolve(response.data);
          },

          // error
          function(response) {
            errorService.handleError(response);
            gpService.decrement();
            defer.reject(response.data);
          });
          return deferred.promise;
        }

        // get relationship type name from its abbreviation
        this.getRelationshipTypeName = function(abbr) {
          for (var i = 0; i < metadata.relationshipTypes.length; i++) {
            if (metadata.relationshipTypes[i].key === abbr) {
              return metadata.relationshipTypes[i].value;
            }
          }
          return null;
        }

        // get attribute name name from its abbreviation
        this.getAttributeNameName = function(abbr) {
          for (var i = 0; i < metadata.attributeNames.length; i++) {
            if (metadata.attributeNames[i].key === abbr) {
              return metadata.attributeNames[i].value;
            }
          }
          return null;
        }

        // get term type name from its abbreviation
        this.getTermTypeName = function(abbr) {
          for (var i = 0; i < metadata.termTypes.length; i++) {
            if (metadata.termTypes[i].key === abbr) {
              return metadata.termTypes[i].value;
            }
          }
          return null;
        }

        // get general entry name from its abbreviation
        this.getGeneralEntryValue = function(abbr) {
          for (var i = 0; i < metadata.generalEntries.length; i++) {
            if (metadata.generalEntries[i].key === abbr) {
              return metadata.generalEntries[i].value;
            }
          }
          return null;
        }

        // get label sets name
        this.getLabelSetName = function(abbr) {
          for (var i = 0; i < metadata.labelSets.length; i++) {
            if (metadata.labelSets[i].key === abbr) {
              return metadata.labelSets[i].value;
            }
          }
          return null;
        }

      } ]);
