// Content Service
var contentUrl = 'content';
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
          sibling : 10,
          filter : 5,
          sort : 1
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
          userNote : null,
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

        // Returns the expression types
        this.getExpressions = function() {
          return expressions;
        };

        // broadcasts a concept change
        this.fireConceptChanged = function(concept) {
          $rootScope.$broadcast('termServer::conceptChanged', project);
        };

        // Get autocomplete results
        this.autocomplete = function(searchTerms, autocompleteUrl) {

          // Setup deferred
          var deferred = $q.defer();

          // if invalid search terms or no url, return empty array
          if (searchTerms == null || searchTerms == undefined || searchTerms.length < 3
            || !autocompleteUrl) {
            deferred.resolve(new Array());
          } else {

            // NO GLASS PANE
            // Make GET call
            $http.get(contentUrl + '/' + autocompleteUrl + encodeURIComponent(searchTerms)).then(
            // success
            function(response) {
              deferred.resolve(response.data.strings);
            },
            // error
            function(response) {
              utilService.handleError(response);
              deferred.resolve(response.data);
            });
          }

          return deferred.promise;
        };

        // Get the component from a component wrapper
        // where wrapper is at minimum { id: ..., type: ..., terminology: ...,
        // version:
        // ..., terminologyId: ...}
        // Search results and components can be passed directly
        this.getComponent = function(wrapper) {

          console.debug('getComponent', wrapper);

          var deferred = $q.defer();

          // check prereqs
          if (!wrapper.type || !wrapper.terminologyId || !wrapper.terminology || !wrapper.version) {
            utilService.setError('Component object not fully specified');
            deferred.reject('Component object not fully specified');
          } else {

            // the component object to be returned
            var component = {};

            // Make GET call
            gpService.increment();

            // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the
            // path
            $http.get(
              contentUrl + '/' + wrapper.type.toLowerCase() + "/" + wrapper.terminology + "/"
                + wrapper.version + "/" + wrapper.terminologyId).then(
              // success
              function(response) {
                var data = response.data;

                if (!data) {
                  deferred.reject('Could not retrieve ' + wrapper.type + ' data for '
                    + wrapper.terminologyId + '/' + wrapper.terminology + '/' + wrapper.version);
                } else {

                  // Set the type of the returned component
                  data.type = wrapper.type;

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

                gpService.decrement();
                deferred.resolve(data);
              }, function(response) {
                utilService.handleError(response);
                gpService.decrement();
                deferred.reject(response.data);
              });

          }
          return deferred.promise;
        };

        //
        // Search history for preservation across route navigation
        // NOTE: Currently only set in contentController.js
        //

        this.setLastSearchParams = function(searchParams) {
          this.searchParams = searchParams;
        };

        this.getLastSearchParams = function() {
          return this.searchParams;
        };

        this.setLastComponent = function(component) {
          this.lastComponent = component;
        };

        this.getLastComponent = function() {
          return this.lastComponent;
        };

        // add a component history entry
        this.addComponentToHistory = function(terminologyId, terminology, version, type, name) {

          // if history exists
          if (history && history.index != -1) {

            var curObj = history.components[history.index];

            if (!curObj) {
              utilService.setError('Error accessing history at index: ' + history.index);
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
        };

        // Accessor for the history object
        this.getHistory = function() {
          return history;
        };

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

            // extract wrapper object
            var wrapper = history.components[index];

            // set the index and get the component from history
            // information
            this.getComponent(wrapper).then(function(data) {
              // set the index and return
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
        this.getTree = function(wrapper, startIndex) {

          console.debug('getTree', wrapper, startIndex);

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

          // Make post call
          gpService.increment();
          $http.post(
            contentUrl + '/' + wrapper.type.toLowerCase() + '/' + wrapper.terminology + '/'
              + wrapper.version + '/' + wrapper.terminologyId + '/trees', pfs).then(
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
        this.getChildTrees = function(tree, type, startIndex) {

          console.debug('getChildTrees', tree, type, startIndex);
          // Set up deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : startIndex,
            maxResults : pageSizes.general,
            sortField : 'nodeName',
            queryRestriction : null
          };

          gpService.increment();

          // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the
          // path
          $http.post(
            contentUrl + '/' + type.toLowerCase() + '/' + tree.terminology + '/' + tree.version
              + '/' + tree.nodeTerminologyId + '/trees/children', pfs).then(
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
        this.getTreeRoots = function(type, terminology, version, page) {
          // Setup deferred
          var deferred = $q.defer();

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

          // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the
          // path
          $http.post(
            contentUrl + '/' + type.toLowerCase() + "/" + terminology + "/" + version
              + "/trees/roots", pfs).then(
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
        this.findComponentsAsList = function(queryStr, type, terminology, version, page,
          searchParams) {

          console.debug('findComponentsAsList', queryStr, type, terminology, version, page);

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
            if (searchParams.userNote) {
              pfs.queryRestriction += " AND notes.note:\"" + searchParams.userNote + "\"";
            }
          }

          // Add anonymous condition for concepts
          if (type.toLowerCase() == "concept") {
            pfs.queryRestriction += " AND anonymous:false";
          }

          // Make POST call
          // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the
          // path
          gpService.increment();
          $http.post(
            contentUrl + '/' + type.toLowerCase() + "/" + terminology + "/" + version + "?query="
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
        this.findComponentsAsTree = function(queryStr, type, terminology, version, page,
          semanticType) {

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
               * semanticTypes.semanticType:\"" + searchParams.semanticType +
               * "\""; }
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

          // Make POST call
          // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the
          // path
          gpService.increment();
          $http.post(
            contentUrl + '/' + type.toLowerCase() + "/" + terminology + "/" + version
              + "/trees?query=" + encodeURIComponent(utilService.cleanQuery(queryStr)), pfs).then(
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

        this.findRelationshipsForQuery = function(terminology, version, terminologyId, type, query, pfs) {
          console.debug('find relationships', terminology, version, terminologyId, type,  query, pfs);

          // Setup deferred
          var deferred = $q.defer();
          
          // Make POST call
          gpService.increment();
          $http.post(
            contentUrl + '/' + type.toLowerCase() + '/' + terminology + '/' + version + '/' + 
              terminologyId + '/relationships?query='+ (query != '' && query != null ? '&query=' + query : ''),
            utilService.prepPfs(pfs)).then(
          // success
          function(response) {
            console.debug('  rels = ', response.data);
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
        
        // Handle paging of relationships (requires content service
        // call).
        this.findRelationships = function(wrapper, page, parameters) {

          console.debug('findRelationships', wrapper, page, parameters);
          var deferred = $q.defer();

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
          if (metadata.terminology && metadata.terminology.descriptionLogicTerminology) {
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

          // perform the call
          // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the
          // path
          gpService.increment();
          $http.post(
            contentUrl + '/' + wrapper.type.toLowerCase() + "/" + wrapper.terminology + "/"
              + wrapper.version + "/" + wrapper.terminologyId + "/relationships?query="
              + encodeURIComponent(utilService.cleanQuery(query)), pfs).then(function(response) {
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
        this.findDeepRelationships = function(wrapper, page, parameters) {

          var deferred = $q.defer();

          if (wrapper.type.toLowerCase() !== 'concept') {
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

          // do not use glass pane, produces additional user lag on initial
          // concept load
          // i.e. retrieve concept, THEN get deep relationships
          // gpService.increment();
          $http.post(
            contentUrl + '/' + wrapper.type.toLowerCase() + "/" + wrapper.terminology + "/"
              + wrapper.version + "/" + wrapper.terminologyId + "/relationships/deep?query="
              + encodeURIComponent(utilService.cleanQuery(query)), pfs).then(function(response) {
            // gpService.decrement();
            deferred.resolve(response.data);
          }, function(response) {
            utilService.handleError(response);
            // gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        };

        // function for testing whether a query is a valid expression
        this.isExpressionConstraintLanguage = function(terminology, version, query) {
          var deferred = $q.defer();
          if (!query || query.length == 0) {
            deferred.reject('Cannot check empty query for expressions');
            utilService.setError('Cannot check empty query for expressions');
          }
          gpService.increment();
          $http.get(
            contentUrl + '/ecl/isExpression/' + encodeURIComponent(utilService.cleanQuery(query)))
            .then(function(response) {
              gpService.decrement();
              deferred.resolve(response.data);
            }, function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
        };

        this.addComponentNote = function(wrapper, note) {
          var deferred = $q.defer();
          if (!wrapper || !note) {
            deferred.reject('Concept id and note must be specified');
          } else {

            gpService.increment();
            $http.post(
              contentUrl + '/' + wrapper.type.toLowerCase() + '/note/' + wrapper.terminology + '/'
                + wrapper.version + '/' + wrapper.terminologyId + '/add', note).then(
              function(response) {
                gpService.decrement();
                deferred.resolve(response.data);
              }, function(response) {
                utilService.handleError(response);
                gpService.decrement();
                // return the original concept without additional annotation
                deferred.reject();
              });

            return deferred.promise;
          }
        };

        // Remove component note
        this.removeComponentNote = function(wrapper, noteId) {
          console.debug('removeComponentNote', wrapper, noteId);
          var deferred = $q.defer();
          if (!wrapper || !noteId) {
            deferred.reject('Component wrapper (minimum type) and note id must be specified');
          } else {

            gpService.increment();
            $http['delete'](
              contentUrl + '/' + wrapper.type.toLowerCase() + '/note/' + noteId + '/remove').then(
              function(response) {
                console.debug('  successful remove note');
                gpService.decrement();
                deferred.resolve(response.data);
              }, function(response) {
                utilService.handleError(response);
                gpService.decrement();
                // return the original concept without additional annotation
                deferred.reject();
              });

            return deferred.promise;
          }
        };

        // Get the user favorites
        // NOTE: This uses the paging structure in utilService.getPaging
        // parallel to uses in component report elements (atoms,
        // relationships...)
        // instead of the getSearchParams structure for standard queries
        this.getUserFavorites = function(parameters) {
          console.debug('get user favorites', parameters);
          var deferred = $q.defer();
          if (!parameters) {
            deferred.reject('Parameters must be specified');
          } else {

            var pfs = {
              startIndex : (parameters.page - 1) * pageSizes.general,
              maxResults : pageSizes.general,
              sortField : parameters.sortField ? parameters.sortField : 'lastModified',
              queryRestriction : parameters.filter,
              ascending : parameters.sortAscending
            };

            gpService.increment();
            $http.post(contentUrl + '/favorites', pfs).then(function(response) {
              gpService.decrement();
              deferred.resolve(response.data);
            }, function(response) {
              utilService.handleError(response);
              gpService.decrement();
              // return the original concept without additional annotation
              deferred.reject();
            });

          }
          return deferred.promise;
        };

        this.getComponentsWithNotesForUser = function(query, parameters) {
          console.debug('get components with notes', query, parameters);
          var deferred = $q.defer();

          var pfs = {
            startIndex : (parameters.page - 1) * parameters.pageSize,
            maxResults : parameters.pageSize,
            sortField : parameters.sortField ? parameters.sortField : 'name',
            ascending : parameters.sortAscending
          };

          if (query && !query.endsWith("*")) {
            query += "*";
          }

          gpService.increment();
          $http.post(
            contentUrl + '/component/notes?query='
              + encodeURIComponent(utilService.cleanQuery(query)), pfs).then(function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          }, function(response) {
            utilService.handleError(response);
            gpService.decrement();
            // return the original concept without additional annotation
            deferred.reject();
          });

          return deferred.promise;
        };

        /**
         * Callback functions needed by directives NOTE: getComponent and
         * getComponentForTree deliberately excluded as each view should
         * interact with the content service directly for history and other
         * considerations
         */
        this.getCallbacks = function() {
          return {
            findRelationships : this.findRelationships,
            findDeepRelationships : this.findDeepRelationships
          };
        };

        // validate concept
        this.validateConcept = function(concept, projectId) {
          console.debug('validateConcept');
          var deferred = $q.defer();

          // validate concept
          gpService.increment();
          $http.put(contentUrl + '/concept?projectId' + projectId, concept).then(
          // success
          function(response) {
            console.debug('  result = ', response.data);
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
        // end

        // Finds concepts matching general query
        this.findConceptsForGeneralQuery = function(query, jql, pfs) {
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();

          $http.post(
            contentUrl + '/concept?query=' + query
              + (jql != '' && jql != null ? '&jql=' + jql : ''), pfs).then(
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

        // function for getting concept
        this.getConcept = function(conceptId, projectId) {
          var deferred = $q.defer();

          gpService.increment();

          $http.get(contentUrl + '/concept/' + conceptId + '?projectId=' + projectId).then(
            function(response) {
              gpService.decrement();
              deferred.resolve(response.data);
            }, function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });

          return deferred.promise;
        };

        // Finds concepts
        this.findConcepts = function(terminology, version, query, pfs) {
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();

          $http.post(contentUrl + '/concept/' + terminology + '/' + version + '?query=' + query,
            pfs).then(
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

      } ]);
