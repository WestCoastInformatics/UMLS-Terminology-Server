// Content Service
tsApp
  .service(
    'contentService',
    [
      '$http',
      '$q',
      'gpService',
      'utilService',
      'tabService',
      'metadataService',
      function($http, $q, gpService, utilService, tabService, metadataService) {
        console.debug("configure contentService");

        // Initialize
        var metadata = metadataService.getModel();

        // global history, used for main content view
        var history = {
          // the components stored
          components : [],

          // the index of the currently viewed component
          index : -1
        };

        // Default page sizes object
        var pageSizes = {
          general : 10,
          rels : 10,
          roots : 125,
          trees : 5,
          search : 10,
          sibling : 10
        };

        // Default search results object
        var searchParams = {
          page : 1,
          query : null,
          expression : null,
          advancedMode : false,
          semanticType : null,
          termType : null,
          matchTerminology : null,
          language : null,
          showExtension : false,
          expression : {
            name : null,
            value : null,
            fields : [],
            isDisabled : false,
            isRawInput : false

          }
        };

        // the last search params saved by a controller or service
        var lastSearchParams = null;

        // the last component saved by a controller or service
        var lastComponent = null;

        // Get new copy of default page sizes
        this.getPageSizes = function() {
          return angular.copy(pageSizes);
        };

        // Get new copy of default search parameters
        this.getSearchParams = function() {
          return angular.copy(searchParams);
        };

        // get the expressions
        var expressions = [
          {
            name : 'Raw Expression',
            isRawInput : true,
            value : null
          },
          {
            name : '------',
            isDisabled : true
          },
          {
            name : 'Ancestor of',
            fields : {
              'Concept' : ''
            },
            compute : function() {
              this.value = '> ' + this.fields['Concept'];
            }
          },
          {
            name : 'Descendant of',
            fields : {
              'Concept' : ''
            },
            compute : function() {
              this.value = '< ' + this.fields['Concept'];
            }
          },
          {
            name : 'Member of',
            fields : {
              'Concept' : ''
            },
            compute : function() {
              this.value = '^ ' + this.fields['Concept'];
            }
          },
          {
            name : 'Has Attribute',
            fields : {
              'Focus Concept' : '',
              'Attribute' : '',
              'Target' : '',
            },
            compute : function() {
              this.value = this.fields['Focus Concept'] + ': ' + this.fields['Attribute'] + "= "
                + this.fields['Target'];
            }
          }

        ];

        this.getExpressions = function() {
          return expressions;
        }

        // Autocomplete function
        this.autocomplete = function(searchTerms, autocompleteUrl) {

          // if invalid search terms, return empty array
          if (searchTerms == null || searchTerms == undefined || searchTerms.length < 3) {
            return new Array();
          }

          // Setup deferred
          var deferred = $q.defer();

          // NO GLASS PANE
          // Make GET call
          $http.get(autocompleteUrl + encodeURIComponent(searchTerms)).then(
          // success
          function(response) {
            deferred.resolve(response.data.strings);
          },
          // error
          function(response) {
            utilHandler.handleError(response);
            deferred.resolve(response.data);
          });

          return deferred.promise;
        };

        // Helper function to get the proper html prefix based on class
        // type
        this.getPrefixForType = function(classType) {
          switch (classType) {
          case 'CONCEPT':
            return 'cui';
          case 'DESCRIPTOR':
            return 'dui';
          case 'CODE':
            return 'code';
          default:
            return 'prefix error detected';
          }
        };

        // Helper function to get the component type from the url prefix
        this.getTypeForPrefix = function(prefix) {
          switch (prefix) {
          case 'cui':
            return 'CONCEPT';
          case 'dui':
            return 'DESCRIPTOR';
          case 'code':
            return 'CODE';
          default:
            return 'component type error detected';
          }
        };

        // Helper function to get a type prefix for the terminology
        this.getPrefixForTerminologyAndVersion = function(terminology, version) {
          return this
            .getPrefixForType(metadataService.getTerminology(terminology, version).organizingClassType);
        };

        // Get the component by type
        this.getComponentFromType = function(terminologyId, terminology, version, type) {
          console.debug('getComponentFromType', terminologyId, terminology, version, type);
          switch (type) {
          case 'CONCEPT':
            return this.getConcept(terminologyId, terminology, version);
          case 'DESCRIPTOR':
            return this.getDescriptor(terminologyId, terminology, version);
          case 'CODE':
            return this.getCode(terminologyId, terminology, version);
          default:
            console.error('Error retrieving component from type');
            this.componentError = "Could not retrieve " + type + " for " + terminologyId + "/"
              + terminology + "/" + version;
          }
        };

        // Get the component based on id/terminology/version
        // uses the organizing class type for the type prefix
        this.getComponent = function(terminologyId, terminology, version) {
          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);
          return this.getComponentHelper(terminologyId, terminology, version, prefix);
        };

        // Get a concept component
        this.getConcept = function(terminologyId, terminology, version) {
          return this.getComponentHelper(terminologyId, terminology, version, this
            .getPrefixForType('CONCEPT'));
        };

        // Get a descriptor component
        this.getDescriptor = function(terminologyId, terminology, version) {
          return this.getComponentHelper(terminologyId, terminology, version, this
            .getPrefixForType('DESCRIPTOR'));
        };

        // Get a code component
        this.getCode = function(terminologyId, terminology, version) {
          return this.getComponentHelper(terminologyId, terminology, version, this
            .getPrefixForType('CODE'));
        };

        // Helper function for loading a component and setting the
        // component
        // data fields
        this.getComponentHelper = function(terminologyId, terminology, version, prefix) {
          var deferred = $q.defer();

          // the component object to be returned
          var component = {};

          // Here the prefix is passed in because of terminologies
          // like MSH
          // that may have legitimate types that are not the
          // organizing class
          // type

          // set component top-level fields
          component.prefix = prefix;
          component.type = this.getTypeForPrefix(prefix);
          component.object = null;
          component.error = null;

          if (!terminologyId || !terminology || !version) {
            component.error = "An unexpected display error occurred. Click a concept or perform a new search to continue";
            return;
          }

          // Make GET call
          gpService.increment();
          $http
            .get(
              contentUrl + component.prefix + "/" + terminology + "/" + version + "/"
                + terminologyId).then(
              // success
              function(response) {
                var data = response.data;

                if (!data) {
                  component.error = "Could not retrieve " + component.type + " data for "
                    + terminologyId + "/" + terminology + "/" + version;
                } else {

                  // cycle over all atoms for pre-processing
                  for (var i = 0; i < data.atoms.length; i++) {

                    // assign expandable content flag
                    data.atoms[i].hasContent = atomHasContent(data.atoms[i]);

                    // push any definitions up to top level
                    for (var j = 0; j < data.atoms[i].definitions.length; j++) {
                      var definition = data.atoms[i].definitions[j];

                      // set the atom element flag
                      definition.atomElement = true;

                      // add the atom information for tooltip
                      // display
                      definition.atomElementStr = data.atoms[i].name + " ["
                        + data.atoms[i].terminology + "/" + data.atoms[i].termType + "]";

                      // add the definition to the top level
                      // component
                      data.definitions.push(definition);
                    }
                  }

                }
                component.object = data;

                gpService.decrement();
                deferred.resolve(component);
              }, function(response) {
                utilService.handleError(response);
                gpService.decrement();
                deferred.reject(response.data);
              });
          return deferred.promise;
        };

        //
        // Search history for preservation across route navigation
        // NOTE: Currently only set in contentController.js
        //

        this.setLastSearchParams = function(searchParams) {
          this.searchParams = searchParams;
        }

        this.getLastSearchParams = function() {
          return this.searchParams;
        }

        this.setLastComponent = function(component) {
          this.lastComponent = component;
        }

        this.getLastComponent = function() {
          return this.lastComponent;
        }

        // add a component history entry
        this.addComponentToHistory = function(terminologyId, terminology, version, type, name) {

          // if history exists
          if (history && history.index != -1) {

            var curObj = history.components[history.index];

            if (!curObj) {
              console.error('Error accessing history at index: ' + history.index);
            }

            // if this component currently viewed, do not add
            if (curObj.terminology === terminology && curObj.version === version
              && curObj.terminologyId === terminologyId)
              return;
          }

          // add item and set index to last
          history.components.push({
            'version' : version,
            'terminology' : terminology,
            'terminologyId' : terminologyId,
            'type' : type,
            'name' : name,
            'index' : history.length
          });
          history.index = history.components.length - 1;
        }

        // Accessor for the history object
        this.getHistory = function() {
          return history;
        }

        // Clears history
        this.clearHistory = function() {

          history = {
            components : [],
            index : -1
          };
        };

        // Retrieve a component from history based on the index
        this.getComponentFromHistory = function(index) {
          var deferred = $q.defer();

          if (index < 0 || index > history.components.length) {
            deferred.reject('Invalid history index: ' + index);
          } else {

            // extract current quintuplet object for convenience
            var obj = history.components[index];

            var type = this.getTypeForPrefix(obj.type);

            // set the index and get the component from history
            // information
            this.getComponentFromType(obj.terminologyId, obj.terminology, obj.version, type).then(
              function(data) {

                // set the index and count variables
                history.index = index;

                deferred.resolve(data);
              });
          }
          return deferred.promise;
        };

        // Helper function for determining if an atom has content
        function atomHasContent(atom) {
          if (!atom)
            return false;
          if (atom.attributes.length > 0)
            return true;
          if (atom.definitions.length > 0)
            return true;
          if (atom.relationships.length > 0)
            return true;
          return false;
        }

        // Gets the tree for the specified component
        this.getTree = function(terminologyId, terminology, version, startIndex) {
          if (startIndex === undefined) {
            startIndex = 0;
          }
          // set up deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : startIndex,
            maxResults : 1,
            sortField : 'ancestorPath',
            queryRestriction : null
          };

          // Get prefix
          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          // Make post call
          gpService.increment();
          $http.post(
            contentUrl + prefix + '/' + terminology + '/' + version + '/' + terminologyId
              + '/trees', pfs).then(
          // success
          function(response) {
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

        // Get child trees for the tree (and start index)
        this.getChildTrees = function(tree, startIndex) {
          // Set up deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : startIndex,
            maxResults : pageSizes.general,
            sortField : 'nodeName',
            queryRestriction : null
          };

          // Get prefix
          var prefix = this.getPrefixForTerminologyAndVersion(tree.terminology, tree.version);

          // Make POST call
          // @Path("/cui/{terminology}/{version}/{terminologyId}/trees/children")
          gpService.increment();
          $http.post(
            contentUrl + prefix + '/' + tree.terminology + '/' + tree.version + '/'
              + tree.nodeTerminologyId + '/trees/children', pfs).then(
          // success
          function(response) {
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

        // Gets the tree roots for the specified params
        this.getTreeRoots = function(terminology, version, page) {
          // Setup deferred
          var deferred = $q.defer();

          // Get prefix
          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          // PFS
          // construct the pfs
          var pfs = {
            startIndex : (page - 1) * pageSizes.general,
            maxResults : pageSizes.roots,
            sortField : metadata.treeSortField,
            queryRestriction : null
          };

          // Make POST call
          gpService.increment();
          $http.post(contentUrl + prefix + "/" + terminology + "/" + version + "/trees/roots", pfs)
            .then(
            // success
            function(response) {
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

        // Finds components as a list
        this.findComponentsAsList = function(queryStr, terminology, version, page, searchParams) {
          // Setup deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : (page - 1) * pageSizes.general,
            maxResults : pageSizes.general,
            sortField : null,
            expression : searchParams && searchParams.expression ? searchParams.expression.value
              : null,
            queryRestriction : "(suppressible:false^20.0 OR suppressible:true) AND (atoms.suppressible:false^20.0 OR atoms.suppressible:true)"
          };

          // check parameters for advanced mode
          if (searchParams && searchParams.advancedMode) {
            if (searchParams.semanticType) {
              pfs.queryRestriction += " AND semanticTypes.semanticType:\""
                + searchParams.semanticType + "\"";
            }

            if (searchParams.matchTerminology) {
              pfs.queryRestriction += " AND atoms.terminology:\"" + searchParams.matchTerminology
                + "\"";
            }
            if (searchParams.termType) {
              pfs.queryRestriction += " AND atoms.termType:\"" + searchParams.termType + "\"";
            }
            if (searchParams.language) {
              pfs.queryRestriction += " AND atoms.language:\"" + searchParams.language + "\"";
            }
          }

          // Get prefix
          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          // Add anonymous condition for concepts
          if (prefix == "cui") {
            pfs.queryRestriction += " AND anonymous:false";
          }

          // Make POST call
          gpService.increment();
          $http.post(
            contentUrl + this.getPrefixForType(metadata.terminology.organizingClassType) + "/"
              + terminology + "/" + version + "?query="
              + encodeURIComponent(utilService.cleanQuery(queryStr)), pfs).then(
          // success
          function(response) {
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

        // Finds components as a tree
        this.findComponentsAsTree = function(queryStr, terminology, version, page, semanticType) {

          // Setup deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : (page - 1) * pageSizes.trees,
            maxResults : pageSizes.trees,
            sortField : metadata.treeSortField,
            queryRestriction : null
          };

          // check parameters for advanced mode
          if (searchParams.advancedMode) {

            if (semanticType) {
              pfs.queryRestriction = "ancestorPath:" + semanticType.replace("~", "\\~") + "*";
            }/*
                           * if (searchParams.semanticType) { pfs.queryRestriction += " AND
                           * semanticTypes.semanticType:\"" + searchParams.semanticType + "\""; }
                           */

            if (searchParams.matchTerminology) {
              pfs.queryRestriction += " AND atoms.terminology:\"" + searchParams.matchTerminology
                + "\"";
            }
            if (searchParams.termType) {
              pfs.queryRestriction += " AND atoms.termType:\"" + searchParams.termType + "\"";
            }
            if (searchParams.language) {
              pfs.queryRestriction += " AND atoms.language:\"" + searchParams.language + "\"";
            }
          }

          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          // Make POST call
          gpService.increment();
          $http.post(
            contentUrl + prefix + "/" + terminology + "/" + version + "/trees?query="
              + encodeURIComponent(utilService.cleanQuery(queryStr)), pfs).then(
          // success
          function(response) {
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

        // Explicitly find concepts (cui)
        this.findConceptsAsList = function(queryStr, terminology, version, page, semanticType) {
          // Setup deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : (page - 1) * pageSizes.general,
            maxResults : pageSizes.general,
            sortField : null,
            queryRestriction : "(suppressible:false^20.0 OR suppressible:true) AND (atoms.suppressible:false^20.0 OR atoms.suppressible:true)"
          };

          // check parameters for advanced mode
          if (searchParams && searchParams.advancedMode) {
            if (searchParams.semanticType) {
              pfs.queryRestriction += " AND semanticTypes.semanticType:\""
                + searchParams.semanticType + "\"";
            }

            if (searchParams.matchTerminology) {
              pfs.queryRestriction += " AND atoms.terminology:\"" + searchParams.matchTerminology
                + "\"";
            }
            if (searchParams.termType) {
              pfs.queryRestriction += " AND atoms.termType:\"" + searchParams.termType + "\"";
            }
            if (searchParams.language) {
              pfs.queryRestriction += " AND atoms.language:\"" + searchParams.language + "\"";
            }
          }

          // Get prefix
          var prefix = 'cui';

          // Add anonymous condition for concepts
          if (prefix == "cui") {
            pfs.queryRestriction += " AND anonymous:false";
          }

          // Make POST call
          gpService.increment();
          $http.post(
            contentUrl + prefix + "/" + terminology + "/" + version + "?query="
              + encodeURIComponent(utilService.cleanQuery(queryStr)), pfs).then(
          // success
          function(response) {
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
        }

        // Handle paging of relationships (requires content service
        // call).
        this.findRelationships = function(terminologyId, terminology, version, page, parameters) {
          var deferred = $q.defer();

          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          if (parameters)

            var pfs = {
              startIndex : (page - 1) * pageSizes.general,
              maxResults : pageSizes.general,
              sortFields : parameters.sortFields ? parameters.sortFields : [ 'group',
                'relationshipType' ],
              ascending : parameters.sortAscending,
              queryRestriction : null
            // constructed from filters
            };

          // Show only inferred rels for now
          // construct query restriction if needed
          var qr = '';
          if (!parameters.showSuppressible) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'suppressible:false';
          }
          if (!parameters.showObsolete) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'obsolete:false';
          }
          if (parameters.showInferred) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'inferred:true';
          }
          if (!parameters.showInferred) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'stated:true';
          }
          pfs.queryRestriction = qr;

          // For description logic sources, simply read all rels.
          // That way we ensure all "groups" are represented.
          if (metadata.terminology.descriptionLogicTerminology) {
            pfs.startIndex = -1;
            pfs.maxResults = 1000000;
          } else {
            pfs.maxResults = pageSizes.general;
          }

          var query = parameters.text;

          // Add wildcard to allow better matching from basic search
          // NOTE: searching for "a"* is interpreted by lucene as a
          // leading wildcard search (i.e. "a" *)
          if (query && !query.endsWith('*') && !query.endsWith('"')) {
            query += '*';
          }
          gpService.increment();
          $http.post(
            contentUrl + prefix + "/" + terminology + "/" + version + "/" + terminologyId
              + "/relationships?query=" + encodeURIComponent(utilService.cleanQuery(query)), pfs)
            .then(function(response) {
              gpService.decrement();
              deferred.resolve(response.data);
            }, function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });

          return deferred.promise;
        };

        // Handle paging of relationships (requires content service
        // call).
        this.findDeepRelationships = function(terminologyId, terminology, version, page, parameters) {

          var deferred = $q.defer();

          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          if (prefix !== 'cui') {
            defer.reject('Deep relationships cannot be retrieved for type previs ' + prefix);
          }

          if (parameters) {

            var pfs = {
              startIndex : (page - 1) * pageSizes.general,
              maxResults : pageSizes.general,
              sortFields : parameters.sortFields ? parameters.sortFields : [ 'group',
                'relationshipType' ],
              ascending : parameters.sortAscending,

              // NOTE: Deep relationships do not support query restrictions,
              // instead using
              // text filter as only query parameter
              queryRestriction : null
            };
          }

          // set filter/query; unlike relationships, does not require * for
          // filtering
          var query = parameters.text;

          // For description logic sources, simply read all rels.
          // That way we ensure all "groups" are represented.
          /*
                     * if (metadata.terminology.descriptionLogicTerminology) { pfs.startIndex = -1;
                     * pfs.maxResults = 1000000; } else { pfs.maxResults = pageSizes.general; }
                     */

          // gpService.increment();
          $http.post(
            contentUrl + prefix + "/" + terminology + "/" + version + "/" + terminologyId
              + "/relationships/deep?query=" + encodeURIComponent(utilService.cleanQuery(query)),
            pfs).then(function(response) {
            // gpService.decrement();
            deferred.resolve(response.data);
          }, function(response) {
            utilService.handleError(response);
            // gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        };

        // Handle paging of mappings (requires content service
        // call).
        this.findMappings = function(terminologyId, terminology, version, page, parameters) {
          var deferred = $q.defer();

          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          if (parameters)

            var pfs = {
              startIndex : (page - 1) * pageSizes.general,
              maxResults : pageSizes.general,
              sortField : parameters.sortField ? parameters.sortField : 'fromTerminologyId',
              ascending : parameters.sortAscending,
              queryRestriction : null
            // constructed from filters
            };

          // Show only inferred rels for now
          // construct query restriction if needed
          var qr = '';
          if (!parameters.showSuppressible) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'suppressible:false';
          }
          if (!parameters.showObsolete) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'obsolete:false';
          }

          pfs.queryRestriction = qr;

          // For description logic sources, simply read all rels.
          // That way we ensure all "groups" are represented.
          if (metadata.terminology.descriptionLogicTerminology) {
            pfs.startIndex = -1;
            pfs.maxResults = 1000000;
          } else {
            pfs.maxResults = pageSizes.general;
          }

          var query = parameters.text;
          gpService.increment();
          $http.post(
            contentUrl + prefix + "/" + terminologyId + "/" + component.object.terminology + "/"
              + version + "/mappings?query=" + encodeURIComponent(utilService.cleanQuery(query)),
            pfs).then(function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          }, function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        };

        this.isExpressionConstraintLanguage = function(terminology, version, query) {
          var deferred = $q.defer();
          if (!query || query.length == 0) {
            console.error('Cannot check empty query for expressions');
            deferred.reject('Cannot check empty query for expressions');
          }
          gpService.increment();
          $http.get(contentUrl + '/ecl/isExpression/' + URIEncode(query)).then(function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          }, function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
        }

        this.addComponentAnnotation = function(component, annotationText) {
          var deferred = $q.defer();
          if (!component || !annotationText) {
            deferred.reject('Concept id and annotation text must be specified');
          } else {

            var prefix = this.getPrefixForTerminologyAndVersion(component.object.terminology,
              component.object.version);
            gpService.increment();
            $http.post(contentUrl + prefix + '/annotate/' + component.object.id + '/add',
              annotationText).then(function(response) {
              deferred.resolve(response.data);
            }, function(response) {
              utilService.handleError(response);
              gpService.decrement();
              // return the original concept without additional annotation
              deferred.reject();
            });

            return deferred.promise;
          }
        }

        this.updateComponentAnnotation = function(component, annotationId, annotationText) {
          var deferred = $q.defer();
          if (!component || !annotationId || !annotationText) {
            deferred.reject('Component, annotation id, and annotation text must be specified');
          } else {

            var prefix = this.getPrefixForTerminologyAndVersion(component.object.terminology,
              component.object.version);
            gpService.increment();
            $http.post(
              contentUrl + prefix + '/annotate/' + component.object.id + '/update/' + annotationId,
              annotationText).then(function(response) {
              deferred.resolve(response.data);
            }, function(response) {
              utilService.handleError(response);
              gpService.decrement();
              // return the original concept without additional annotation
              deferred.reject();
            });

            return deferred.promise;
          }
        }

        this.removeComponentAnnotation = function(component, annotationId, annotationText) {
          var deferred = $q.defer();
          if (!component || !annotationId || !annotationText) {
            deferred.reject('Component, annotationId, and annotation text must be specified');
          } else {

            var prefix = this.getPrefixForTerminologyAndVersion(component.object.terminology,
              component.object.version);
            gpService.increment();
            $http.post(
              contentUrl + prefix + '/annotate/' + component.object.id + '/remove' + annotationId)
              .then(function(response) {
                deferred.resolve(response.data);
              }, function(response) {
                utilService.handleError(response);
                gpService.decrement();
                // return the original concept without additional annotation
                deferred.reject();
              });

            return deferred.promise;
          }
        }

        this.getUserConceptFavorites = function(terminology, version, parameters) {
          var deferred = $q.defer();
          if (!terminology || !version || !parameters) {
            deferred.reject('Parameters must be specified');
          } else {
            
            var prefix = this.getPrefixForTerminologyAndVersion(component.object.terminology,
              component.object.version);

            var pfs = {
              startIndex : (parameters.page - 1) * parameters.pageSize,
              maxResults : parameters.pageSize,
              sortField : parameters.sortField ? parameters.sortField : 'lastModified',
              queryRestriction : null
            };

            gpService.increment();
            $http.post(
              contentUrl + '/' + prefix + '/favorites/' + terminology + '/' + version + '?query='
                + UriEncode(parameters.query)).then(function(response) {
                  
                  // TODO Remove once the list objects are normalized
                  if (response.data.hasOwnProperty('concepts')) {
                    response.data.objects = response.data.concepts;
                    delete response.data.concepts;
                  }
                  if (response.data.hasOwnProperty('descriptors')) {
                    response.data.objects = response.data.descriptors;
                    delete response.data.descriptors;
                  }
                  if (response.data.hasOwnProperty('codes')) {
                    response.data.objects = response.data.codes;
                    delete response.data.codes;
                  }
                  
              deferred.resolve(response.data);
            }, function(response) {
              utilService.handleError(response);
              gpService.decrement();
              // return the original concept without additional annotation
              deferred.reject();
            });

            return deferred.promise;
          }
        }

        // end

      } ]);
