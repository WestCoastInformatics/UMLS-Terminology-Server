// Util service
tsApp
  .service(
    'utilService',
    [
      '$location',
      '$anchorScroll',
      '$cookies',
      '$uibModal',
      'appConfig',
      function($location, $anchorScroll, $cookies, $uibModal, appConfig) {

        this.showHeaderFooter = true;

        // declare the error
        this.error = {
          message : null,
          longMessage : null,
          expand : false
        };

        this.success = {
          message : null,
          longMessage : null,
          expand : false
        };
        
        this.registrationModal = function(style) {
        	$uibModal.open({
            templateUrl: 'app/util/register/registerModal.html',
            backdrop : (style === "WARN") ? 'none' : 'static',
            controller : 'RegisterModalCtrl',
            bindToController : true,
          });
        }

        // tinymce options
        this.tinymceOptions = {
          menubar : false,
          statusbar : false,
          plugins : 'autolink autoresize link image charmap searchreplace lists paste',
          toolbar : 'undo redo | styleselect lists | bold italic underline strikethrough | charmap link image',
          forced_root_block : ''
        };

        // Prep query
        this.prepQuery = function(query) {
          if (!query) {
            return '';
          }

          // Add a * to the filter if set and doesn't contain a : ( or "
          if (query.indexOf("(") == -1 && query.indexOf(":") == -1 && query.indexOf("\"") == -1) {
            var query2 = query.concat('*');
            return encodeURIComponent(query2);
          }
          return encodeURIComponent(query);
        };

        // Prep pfs filter
        this.prepPfs = function(pfs) {
          if (!pfs) {
            return {};
          }

          // Add a * to the filter if set and doesn't contain a :
          if (pfs.queryRestriction && pfs.queryRestriction.indexOf(":") == -1
            && pfs.queryRestriction.indexOf("\"") == -1) {
            var pfs2 = angular.copy(pfs);
            pfs2.queryRestriction += "*";
            return pfs2;
          }
          return pfs;
        };

        // Sets the error
        this.setError = function(message) {
          console.error(message);
          this.error.message = message;
          $location.hash('top');
          $anchorScroll();
        };

        // Clears the error
        this.clearError = function() {
          this.error.message = null;
          this.error.longMessage = null;
          this.error.expand = false;
        };

        // Sets the success
        this.setSuccess = function(message) {
          this.success.message = message;
        };

        // Clears the success
        this.clearSuccess = function() {
          this.success.message = null;
          this.success.longMessage = null;
          this.success.expand = false;
        };

        this.handleSuccess = function(message) {
          if (message && message.legth > 100) {
            this.success.message = 'Successful process reported, click the icon to view full message';
            this.success.longMessage = message;
          } else {
            this.success.message = message;
          }

          // scroll to top of page
          $location.hash('top');
          $anchorScroll();
        };

        // Handle error message
        this.handleError = function(response) {
          if (response.data && response.data.length > 100) {
            this.error.message = "Unexpected error, click the icon to view attached full error";
            this.error.longMessage = response.data;
            console.error(this.error.longMessage);
          } else {
            this.error.message = response.data;
            console.error(this.error.message);
          }
          // handle no message
          if (!this.error.message) {
            // Print the stack trace so we know where the error came from
            e = new Error();
            console.log("ERROR", e.stack);

            console.error(this.error.message);
            this.error.message = "Unexpected server side error.";
          }
          // If authtoken expired, relogin
          if (this.error.message && this.error.message.indexOf('AuthToken') != -1) {
            // Reroute back to login page with 'auth token has
            // expired' message
            if (appConfig['deploy.login.enabled'] === 'true') {
              $location.path('/login');
            } else {
              $location.path('/');
            }
          } else {
            // scroll to top of page
            $location.hash('top');
            $anchorScroll();
          }
        };

        // Dialog error handler
        this.handleDialogError = function(errors, error) {
          // handle long error
          if (error && error.length > 100) {
            errors[0] = "Unexpected error, click the icon to view attached full error";
            errors[1] = error;
          } else {
            errors[0] = error;
          }
          // handle no message
          if (!error) {
            errors[0] = "Unexpected server side error.";
          }
          // If authtoken expired, relogin
          if (error && error.indexOf('AuthToken') != -1) {
            // Reroute back to login page with 'auth token has
            // expired' message
            $location.path('/');
          }
          // otherwise clear the top-level error
          else {
            this.clearError();
          }
        };

        // Set a flag indicating whether header/footer are to be showing
        this.setHeaderFooterShowing = function(showHeaderFooter) {
          this.showHeaderFooter = showHeaderFooter;
        };

        // Indicates whether header/footer are showing at all
        this.isHeaderFooterShowing = function() {
          return this.showHeaderFooter;
        };

        // Compose a URL properly for opening new window
        this.composeUrl = function(extension) {
          var currentUrl = $location.absUrl();
          var baseUrl = currentUrl.substring(0, currentUrl.indexOf('#') + 1);
          var newUrl = baseUrl + extension;
          return newUrl;
        }

        // Convert seconds to hour/min string
        this.toTime = function(secs) {
          if (secs) {
            var date = new Date(null);
            date.setSeconds(secs);
            return date.toISOString().substr(11, 8);
          }

          // if (d == 0)
          // return "";
          // var h = Math.floor(d / 3600);
          // var m = Math.floor(d % 3600 / 60);
          // return ((h + ":" + (m < 10 ? "0" : "") ) + m);
        }

        this.toText = function(camelCase, captializefirst) {
          if (capitalizeFirst) {
            var str = camelCase.replace(/([A-Z]+)/g, " $1").replace(/([A-Z][a-z])/g, " $1");
            return str[0].toUpperCase() + str.slice(1)
          } else {
            return camelCase.replace(/([A-Z]+)/g, " $1").replace(/([A-Z][a-z])/g, " $1")
          }
        }

        this.toCamelCase = function(text) {
          // Lower cases the string
          return text.toLowerCase()
          // Replaces any - or _ characters with a space
          .replace(/[-_]+/g, ' ')
          // Removes any non alphanumeric characters
          .replace(/[^\w\s]/g, '')
          // Uppercases the first character in each group immediately following
          // a space
          // (delimited by spaces)
          .replace(/ (.)/g, function($1) {
            return $1.toUpperCase();
          })
          // Removes spaces
          .replace(/ /g, '');
        }

        this.yyyymmdd = function(dateIn) {
          var yyyy = dateIn.getFullYear();
          // getMonth() is zero-based
          var mm = dateIn.getMonth() + 1;
          var dd = dateIn.getDate();
          // Leading zeros for mm and dd
          return String(10000 * yyyy + 100 * mm + dd);
        }

        this.yyyymmddhhmmss = function(dateIn) {
          var yyyy = dateIn.getFullYear();
          // getMonth() is zero-based
          var MM = dateIn.getMonth() + 1;
          var dd = dateIn.getDate();
          var hh = dateIn.getHours();
          var mm = dateIn.getMinutes();
          var ss = dateIn.getSeconds();
          // Leading zeros for mm and dd
          return String(10000000000 * yyyy + 100000000 * MM + 1000000 * dd + 10000 * hh + 100 * mm
            * ss);
        }

        // Convert date to a string
        this.toDate = function(lastModified) {
          if (!lastModified) {
            return "";
          }
          var date = new Date(lastModified);
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          var hour = '' + date.getHours();
          if (hour.length == 1) {
            hour = '0' + hour;
          }
          var minute = '' + date.getMinutes();
          if (minute.length == 1) {
            minute = '0' + minute;
          }
          var second = '' + date.getSeconds();
          if (second.length == 1) {
            second = '0' + second;
          }
          return year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;
        };

        // Convert date to a short string
        this.toShortDate = function(lastModified) {
          if (!lastModified) {
            return "";
          }
          var date = new Date(lastModified);
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          return year + '-' + month + '-' + day;
        };

        // Convert date to a simple string
        this.toSimpleDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          return year + month + day;
        };

        // Uniq an array of simple data types
        this.uniq = function uniq(a) {
          var seen = {};
          return a.filter(function(item) {
            return seen.hasOwnProperty(item) ? false : (seen[item] = true);
          });
        };

        // Table sorting mechanism
        this.setSortField = function(table, field, paging) {
          paging[table].sortField = field;
          // reset page number too
          paging[table].page = 1;
          // handles null case also
          if (!paging[table].sortAscending) {
            paging[table].sortAscending = true;
          } else {
            paging[table].sortAscending = false;
          }
          // reset the paging for the correct table
          for ( var key in paging) {
            if (paging.hasOwnProperty(key)) {
              if (key == table)
                paging[key].page = 1;
            }
          }
        };

        // Return up or down sort chars if sorted
        this.getSortIndicator = function(table, field, paging) {
          if (paging[table].sortAscending == null) {
            return '';
          }
          if (paging[table].sortField == field && paging[table].sortAscending) {
            return '▴';
          }
          if (paging[table].sortField == field && !paging[table].sortAscending) {
            return '▾';
          }
        };

        // Helper function to get a standard paging object
        // overwritten as needed
        // Example of filterFields
        // paging.filterFields.terminologyId = 1;
        // paging.filterFields.expandedForm = 1;
        //
        this.getPaging = function() {
          return {
            page : 1,
            pageSize : 10,
            filter : '',
            filterFields : null,
            sortField : null,
            sortAscending : true,
            sortOptions : []
          };
        };

        // Get page sizes
        this.getPageSizes = function() {
          return [ {
            name : 10,
            value : 10
          }, {
            name : 20,
            value : 20
          }, {
            name : 50,
            value : 50
          }, {
            name : 100,
            value : 100
          }, {
            name : 'All',
            value : 100000
          } ];
        }

        // Helper to get a paged array with show/hide flags
        // and filtered by query string
        // use when all data is already loaded
        this.getPagedArray = function(array, paging) {
          var newArray = new Array();
          // if array blank or not an array, return blank list
          if (array == null || array == undefined || !Array.isArray(array)) {
            return newArray;
          }

          newArray = array.slice(0);
          // apply suppressible/obsolete

          // apply sort if specified
          if (paging.sortField) {
            // if ascending specified, use that value, otherwise use false
            newArray.sort(this.sortBy(paging.sortField, !paging.sortAscending));
          }

          // apply filter
          if (paging.filter) {
            newArray = this.getArrayByFilter(newArray, paging.filter, paging.filterFields);
          }

          // get the page indices (if supplied)
          var results;
          if (paging.pageSize != -1) {
            var fromIndex = (paging.page - 1) * paging.pageSize;
            var toIndex = Math.min(fromIndex + paging.pageSize, array.length);

            // slice the array
            results = newArray.slice(fromIndex, toIndex);
          } else {
            results = newArray;
          }

          return {
            data : results,
            totalCount : newArray.length
          };
        };

        // function for sorting an array by (string) field and direction
        this.sortBy = function(field, reverse) {

          var fields = field.split(',');
          console.debug('fields', fields);

          reverse = !reverse ? 1 : -1;

          return function(x, y) {
            for (var i = 0; i < fields.length; i++) {
              var key = fields[i];
              var a = x[key];
              var b = y[key];
              if ((a + '').match(/^[a-zA-Z]/)) {
                a = a.toLowerCase();
              }
              if ((b + '').match(/^[a-zA-Z]/)) {
                b = b.toLowerCase();
              }
              if (a == b) {
                continue;
              }
              return reverse * ((a > b) - (b > a));
            }
            return 0;
          };
        };

        // Get array by filter text matching terminologyId or name
        this.getArrayByFilter = function(array, filter, fields) {
          var newArray = [];
          for ( var object in array) {

            if (this.objectContainsFilterText(array[object], filter, fields)) {
              newArray.push(array[object]);
            }
          }
          return newArray;
        };

        // Returns true if any field on object contains filter text
        this.objectContainsFilterText = function(object, filter, fields) {

          if (!filter || !object)
            return false;

          for ( var prop in object) {
            // skip if fields are defined but not specified in prop
            if (fields && !fields[prop]) {
              continue;
            }
            var value = object[prop];
            // check property for string, note this will cover child elements
            if (value && value.toString().toLowerCase().indexOf(filter.toLowerCase()) != -1) {
              return true;
            }
          }

          return false;
        };

        // Finds the object in a list by the field
        this.findBy = function(list, obj, field) {

          // key: function to return field value from object
          var key = function(x) {
            return x[field];
          };

          for (var i = 0; i < list.length; i++) {
            if (key(list[i]) == key(obj)) {
              return list[i];
            }
          }
          return null;
        };

        // Get words of a string
        this.getWords = function(str) {
          // Same as in tinymce options
          return str.match(/[^\s,\.]+/g);
        };

        // Single and multiple-word ordered phrases
        this.getPhrases = function(str) {
          var words = str.match(/[^\s,\.]+/g);
          var phrases = [];

          for (var i = 0; i < words.length; i++) {
            for (var j = i + 1; j <= words.length; j++) {
              var phrase = words.slice(i, j).join(' ');
              // a phrase have at least 5 chars and no start/end words that are
              // purely punctuation
              if (phrase.length > 5 && words[i].match(/.*[A-Za-z0-9].*/)
                && words[j - 1].match(/.*[A-Za-z0-9].*/)) {
                phrases.push(phrase.toLowerCase());
              }
            }
          }
          return phrases;
        };

        // Utility for cleaning a query
        this.cleanQuery = function(queryStr) {
          if (queryStr == null) {
            return "";
          }
          var cleanQuery = queryStr;
          // Replace all slash characters
          cleanQuery = queryStr.replace(new RegExp('[/\\\\]', 'g'), ' ');
          // Remove brackets if not using a fielded query
          if (queryStr.indexOf(':') == -1) {
            cleanQuery = queryStr.replace(new RegExp('[^a-zA-Z0-9:\\.\\-\'\\*"]', 'g'), ' ');
          }
          return cleanQuery;
        };

        //
        // Extends a callbacks object, checking for name clashes
        //
        this.extendCallbacks = function(callbacks, itemsToAdd) {
          if (!callbacks) {
            callbacks = {};
          }
          for ( var key in itemsToAdd) {
            if (callbacks.hasOwnProperty(key)) {
              utilService
                .setError('Error constructing callbacks, name clash for ' + key, callbacks);
              return;
            }
            callbacks[key] = itemsToAdd[key];
          }
        };
        
        this.validateEmail = function(email) {
          var regex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
          email = email.trim();
          return !(email === "" || !regex.test(email))
        };

      } ]);

// Glass pane service
tsApp.service('gpService', [ '$timeout', function($timeout) {
  console.debug('configure gpService');
  // declare the glass pane counter
  var glassPane = {
    counter : 0,
    messages : [],
    enabled : true,
    timeout : false
  };

  this.getGlassPane = function() {
    return glassPane;
  }

  this.isGlassPaneSet = function() {
    return glassPane.enabled;
  };

  this.isGlassPaneNegative = function() {
    return glassPane.counter < 0;
  };

  // Increments glass pane counter
  this.increment = function(message) {
    if (message) {
      glassPane.messages.push(message);
    }
    glassPane.counter++;
    if (!glassPane.timeout) {
      $timeout(function() {
        if (glassPane.counter > 0) {
          glassPane.enabled = true;
        }
        glassPane.timeout = false;
      }, 100);
    }
  };

  // Decrements glass pane counter
  this.decrement = function(message) {
    if (message) {
      var index = glassPane.messages.indexOf(message);
      if (index !== -1) {
        glassPane.messages.splice(index, 1);
      }
    }
    glassPane.counter--;
    if (glassPane.counter == 0) {
      $timeout(function() {
        if (glassPane.counter == 0) {
          glassPane.enabled = false;
        }
      }, 100);
    }
  };

} ]);
