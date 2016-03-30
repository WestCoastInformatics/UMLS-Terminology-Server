/**
 * Directive to append a image file generated via SVG to a parent div
 */

'use strict';
// jshint ignore: start
angular
  .module('tsApp')
  .directive(
    'svgComponentModel',
    function() {
      return {
        restrict : 'A',
        transclude : false,
        replace : true,
        scope : {
          concept : '=',
        },
        templateUrl : 'app/util/svg-component/svgComponentModel.html',

        link : function(scope, element, attrs, linkCtrl, snowowlService) {
          //scope.view = true;
          var idSequence = 0;
          if (scope.view !== 'snf') {
            setTimeout(function() {
              element.append($("<div></div>").addClass('modelContainer'));
              drawConceptDiagram(scope.concept, element.find('.modelContainer'), {}, {});
            }, 100);
          }
          scope.$watch('concept', function(newVal, oldVal) {
            if (scope.view !== 'snf') {
              setTimeout(function() {
                element.append($("<div></div>").addClass('modelContainer'));
                drawConceptDiagram(scope.concept, element.find('.modelContainer'), {}, {});
              }, 100);
            }
          }, true);
          

          function drawConceptDiagram(concept, div, options, snfConcept) {
            var svgIsaModel = [];
            var svgAttrModel = [];
            if (scope.view == 'stated') {
              $.each(concept.relationships, function(i, field) {
                if (field.active == true && field.characteristicType == "STATED_RELATIONSHIP") {
                  if (field.type.conceptId == 116680003) {
                    svgIsaModel.push(field);
                  } else {
                    svgAttrModel.push(field);
                  }
                }
              });
            } else if (scope.view == 'inferred') {
              if (concept.relationships) {
                $.each(concept.relationships, function(i, field) {
                  if (field.active == true && field.characteristicType == "INFERRED_RELATIONSHIP") {
                    if (field.type.conceptId == 116680003) {
                      svgIsaModel.push(field);
                    } else {
                      svgAttrModel.push(field);
                    }
                  }
                });
              }
            } else if (scope.view == 'snf') {
              concept.relationships = [];
              $.each(snfConcept.concepts, function(i, field) {
                field.target = {};
                if (field.primitive === true) {
                  field.target.definitionStatus = 'PRIMITIVE';
                } else {
                  field.target.definitionStatus = 'FULLY_DEFINED';
                }
                field.type = {};
                field.type.conceptId = 116680003;
                field.target.fsn = field.term;
                field.target.conceptId = field.id;
                concept.relationships.push(field);
              });
              if (snfConcept.attributes) {
                $.each(snfConcept.attributes, function(i, field) {
                  field.type.conceptId = field.type.id;
                  field.type.fsn = field.type.term;
                  field.target = {};
                  field.groupId = 0;
                  if (field.value.id) {
                    field.target.conceptId = field.value.id;
                    field.target.fsn = field.value.term;
                    if (field.value.primitive) {
                      field.target.definitionStatus = 'PRIMITIVE';
                    } else {
                      field.target.definitionStatus = 'FULLY_DEFINED';
                    }
                  } else if (field.value.concepts) {
                    field.target.conceptId = field.value.concepts[0].id;
                    field.target.fsn = field.value.concepts[0].term;
                    if (field.value.concepts[0].primitive) {
                      field.target.definitionStatus = 'PRIMITIVE';
                    } else {
                      field.target.definitionStatus = 'FULLY_DEFINED';
                    }
                    field.nest = [];
                    $.each(field.value.attributes, function(i, innerField) {
                      innerField.type.conceptId = innerField.type.id;
                      innerField.type.fsn = innerField.type.term;
                      innerField.target = {};
                      innerField.groupId = 0;
                      if (innerField.value.id) {
                        innerField.target.conceptId = innerField.value.id;
                        innerField.target.fsn = innerField.value.term;
                        if (innerField.value.primitive) {
                          innerField.target.definitionStatus = 'PRIMITIVE';
                        } else {
                          innerField.target.definitionStatus = 'FULLY_DEFINED';
                        }
                      }
                      field.nest.push(innerField);
                    });
                  }
                  concept.relationships.push(field);
                });
              }
              if (snfConcept.groups) {
                $.each(snfConcept.groups, function(i, group) {
                  $.each(group.attributes, function(j, field) {
                    field.type.conceptId = field.type.id;
                    field.type.fsn = field.type.term;
                    field.target = {};
                    field.groupId = i + 1;
                    if (field.value.id) {
                      field.target.conceptId = field.value.id;
                      field.target.fsn = field.value.term;
                      if (field.value.primitive) {
                        field.target.definitionStatus = 'PRIMITIVE';
                      } else {
                        field.target.definitionStatus = 'FULLY_DEFINED';
                      }
                    }
                    concept.relationships.push(field);
                  });
                });
              }
              $.each(concept.relationships, function(i, field) {
                if (field.type.conceptId == 116680003) {
                  svgIsaModel.push(field);
                } else {
                  svgAttrModel.push(field);
                }
              });
            }

            var parentDiv = div;

            parentDiv.svg({
              settings : {
                height : '1250px',
                width : '2500px',
                id : 'svg-' + concept.conceptId
              }
            });
            var svg = parentDiv.svg('get');
            loadDefs(svg);
            var x = 10;
            var y = 10;
            var maxX = 10;
            var sctClass = "";
            if (concept.definitionStatus == "PRIMITIVE") {
              sctClass = "sct-primitive-concept";
            } else {
              sctClass = "sct-defined-concept";
            }
            var rect1 = drawSctBox(svg, x, y, concept.fsn, concept.conceptId, sctClass);
            x = x + 90;
            y = y + rect1.getBBox().height + 40;
            var circle1;
            if (concept.definitionStatus == "PRIMITIVE") {
              circle1 = drawSubsumedByNode(svg, x, y);
            } else {
              circle1 = drawEquivalentNode(svg, x, y);
            }
            connectElements(svg, rect1, circle1, 'bottom-50', 'left');
            x = x + 55;
            var circle2 = drawConjunctionNode(svg, x, y);
            connectElements(svg, circle1, circle2, 'right', 'left', 'LineMarker');
            x = x + 40;
            y = y - 18;
            maxX = ((maxX < x) ? x : maxX);
            // load stated parents
            sctClass = "sct-defined-concept";
            $.each(svgIsaModel, function(i, relationship) {
              if (relationship.target.definitionStatus == "PRIMITIVE") {
                sctClass = "sct-primitive-concept";
              } else {
                sctClass = "sct-defined-concept";
              }
              var rectParent = drawSctBox(svg, x, y, relationship.target.fsn,
                relationship.target.conceptId, sctClass);
              // $("#" + rectParent.id).css({"top":
              // (rectParent.outerHeight()/2) + "px"});
              connectElements(svg, circle2, rectParent, 'center', 'left', 'ClearTriangle');
              y = y + rectParent.getBBox().height + 25;
              maxX = ((maxX < x + rectParent.getBBox().width + 50) ? x + rectParent.getBBox().width
                + 50 : maxX);
            });

            // load ungrouped attributes
            var maxRoleNumber = 0;
            $.each(svgAttrModel,
              function(i, relationship) {
                if (relationship.target.definitionStatus == "PRIMITIVE") {
                  sctClass = "sct-primitive-concept";
                } else {
                  sctClass = "sct-defined-concept";
                }
                if (relationship.groupId == 0) {
                  if (relationship.nest) {
                    var rectAttr = drawSctBox(svg, x, y, relationship.type.fsn,
                      relationship.type.conceptId, "sct-attribute");
                    connectElements(svg, circle2, rectAttr, 'center', 'left');
                    x = x + rectAttr.getBBox().width + 25;
                    y = y + rectAttr.getBBox().height / 2;
                    var circle3 = drawConjunctionNode(svg, x, y);
                    connectElements(svg, rectAttr, circle3, 'right', 'left', 'LineMarker');
                    y = y - rectAttr.getBBox().height / 2;
                    x = x - 100;
                    var rectTarget = drawSctBox(svg, x + rectAttr.getBBox().width + 50, y,
                      relationship.target.fsn, relationship.target.conceptId, sctClass);
                    x = x + 100;
                    connectElements(svg, circle3, rectTarget, 'right', 'left');
                    y = y + rectTarget.getBBox().height + 25;
                    maxX = ((maxX < x + rectAttr.getBBox().width + 50 + rectTarget.getBBox().width
                      + 50) ? x + rectAttr.getBBox().width + 50 + rectTarget.getBBox().width + 50
                      : maxX);
                  } else {
                    var rectAttr = drawSctBox(svg, x, y, relationship.type.fsn,
                      relationship.type.conceptId, "sct-attribute");
                    connectElements(svg, circle2, rectAttr, 'center', 'left');
                    var rectTarget = drawSctBox(svg, x + rectAttr.getBBox().width + 50, y,
                      relationship.target.fsn, relationship.target.conceptId, sctClass);
                    connectElements(svg, rectAttr, rectTarget, 'right', 'left');
                    y = y + rectTarget.getBBox().height + 25;
                    maxX = ((maxX < x + rectAttr.getBBox().width + 50 + rectTarget.getBBox().width
                      + 50) ? x + rectAttr.getBBox().width + 50 + rectTarget.getBBox().width + 50
                      : maxX);
                  }
                } else {
                  if (relationship.groupId > maxRoleNumber) {
                    maxRoleNumber = relationship.groupId;
                  }
                }
                if (relationship.nest) {
                  if (relationship.nest[0].target.definitionStatus == "PRIMITIVE") {
                    sctClass = "sct-primitive-concept";
                  } else {
                    sctClass = "sct-defined-concept";
                  }
                  y = y + 25;
                  x = x + 50;
                  var rectAttrNest = drawSctBox(svg, x, y, relationship.nest[0].type.fsn,
                    relationship.nest[0].type.conceptId, "sct-attribute");
                  connectElements(svg, circle3, rectAttrNest, 'center', 'left');
                  var rectTargetNest = drawSctBox(svg, x + rectAttrNest.getBBox().width + 50, y,
                    relationship.nest[0].target.fsn, relationship.nest[0].target.conceptId,
                    sctClass);
                  connectElements(svg, rectAttrNest, rectTargetNest, 'right', 'left');
                  y = y + rectTarget.getBBox().height + 25;
                  maxX = ((maxX < x + rectAttrNest.getBBox().width + 50
                    + rectTargetNest.getBBox().width + 50) ? x + rectAttrNest.getBBox().width + 50
                    + rectTargetNest.getBBox().width + 50 : maxX);
                }
              });
            y = y + 15;
            for (var i = 1; i <= maxRoleNumber; i++) {
              var groupNode = drawAttributeGroupNode(svg, x, y);
              connectElements(svg, circle2, groupNode, 'center', 'left');
              var conjunctionNode = drawConjunctionNode(svg, x + 55, y);
              connectElements(svg, groupNode, conjunctionNode, 'right', 'left');
              $.each(svgAttrModel, function(m, relationship) {
                if (relationship.groupId == i) {
                  if (relationship.target.definitionStatus == "PRIMITIVE") {
                    sctClass = "sct-primitive-concept";
                  } else {
                    sctClass = "sct-defined-concept";
                  }
                  var rectRole = drawSctBox(svg, x + 85, y - 18, relationship.type.fsn,
                    relationship.type.conceptId, "sct-attribute");
                  connectElements(svg, conjunctionNode, rectRole, 'center', 'left');
                  var rectRole2 = drawSctBox(svg, x + 85 + rectRole.getBBox().width + 30, y - 18,
                    relationship.target.fsn, relationship.target.conceptId, sctClass);
                  connectElements(svg, rectRole, rectRole2, 'right', 'left');
                  y = y + rectRole2.getBBox().height + 25;
                  maxX = ((maxX < x + 85 + rectRole.getBBox().width + 30
                    + rectRole2.getBBox().width + 50) ? x + 85 + rectRole.getBBox().width + 30
                    + rectRole2.getBBox().width + 50 : maxX);
                }
              });
            }
            var svgCode = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>'
              + parentDiv.html();
            svgCode = svgCode.substr(0, svgCode.indexOf("svg") + 4)
              + ' xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:cc="http://web.resource.org/cc/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:svg="http://www.w3.org/2000/svg" xmlns="http://www.w3.org/2000/svg" '
              + svgCode.substr(svgCode.indexOf("svg") + 4)
            svgCode = svgCode.replace('width="1000px" height="2000px"', 'width="' + maxX
              + '" height="' + y + '"');
            //            var b64 = Base64.encode(svgCode);

            convertToPng(svgCode, concept.conceptId);
          }

          function drawSctBox(svg, x, y, label, sctid, cssClass) {
            //console.log("In svg: " + label + " " + sctid + " " + cssClass);
            // x,y coordinates of the top-left corner
            var testText = "Test";
            if (label && sctid) {
              if (label.length > sctid.toString().length) {
                testText = label;
              } else {
                testText = sctid.toString();
              }
            } else if (label) {
              testText = label;
            } else if (sctid) {
              testText = sctid.toString();
            }
            var fontFamily = '"Helvetica Neue",Helvetica,Arial,sans-serif';
            //var fontFamily = 'sans-serif';
            var tempText = svg.text(x, y, testText, {
              fontFamily : fontFamily,
              fontSize : '12',
              fill : 'black'
            });
            var textHeight = tempText.getBBox().height;
            var textWidth = tempText.getBBox().width;
            textWidth = Math.round(textWidth * 1.2);
            svg.remove(tempText);

            var rect = null;
            var widthPadding = 20;
            var heightpadding = 25;

            if (!sctid || !label) {
              heightpadding = 15;
            }

            if (cssClass == "sct-primitive-concept") {
              rect = svg.rect(x, y, textWidth + widthPadding, textHeight + heightpadding, {
                id : 'rect' + idSequence,
                fill : '#99ccff',
                stroke : '#333',
                strokeWidth : 2
              });
            } else if (cssClass == "sct-defined-concept") {
              rect = svg.rect(x - 2, y - 2, textWidth + widthPadding + 4, textHeight
                + heightpadding + 4, {
                fill : 'white',
                stroke : '#333',
                strokeWidth : 1
              });
              var innerRect = svg.rect(x, y, textWidth + widthPadding, textHeight + heightpadding,
                {
                  id : 'rect' + idSequence,
                  fill : '#ccccff',
                  stroke : '#333',
                  strokeWidth : 1
                });
            } else if (cssClass == "sct-attribute") {
              rect = svg.rect(x - 2, y - 2, textWidth + widthPadding + 4, textHeight
                + heightpadding + 4, 18, 18, {
                fill : 'white',
                stroke : '#333',
                strokeWidth : 1
              });
              var innerRect = svg.rect(x, y, textWidth + widthPadding, textHeight + heightpadding,
                18, 18, {
                  id : 'rect' + idSequence,
                  fill : '#ffffcc',
                  stroke : '#333',
                  strokeWidth : 1
                });
            } else if (cssClass == "sct-slot") {
              rect = svg.rect(x, y, textWidth + widthPadding, textHeight + heightpadding, {
                id : 'rect' + idSequence,
                fill : '#99ccff',
                stroke : '#333',
                strokeWidth : 2
              });
            } else {
              rect = svg.rect(x, y, textWidth + widthPadding, textHeight + heightpadding, {
                id : 'rect' + idSequence,
                fill : 'white',
                stroke : 'black',
                strokeWidth : 1
              });
            }

            if (sctid && label) {
              svg.text(x + 10, y + 16, sctid.toString(), {
                fontFamily : fontFamily,
                fontSize : '10',
                fill : 'black'
              });
              svg.text(x + 10, y + 31, label, {
                fontFamily : fontFamily,
                fontSize : '12',
                fill : 'black'
              });
            } else if (label) {
              svg.text(x + 10, y + 18, label, {
                fontFamily : fontFamily,
                fontSize : '12',
                fill : 'black'
              });
            } else if (sctid) {
              svg.text(x + 10, y + 18, sctid.toString(), {
                fontFamily : fontFamily,
                fontSize : '12',
                fill : 'black'
              });
            }

            idSequence++;
            $('rect').click(function(evt) {
            });

            return rect;
          }

          function connectElements(svg, fig1, fig2, side1, side2, endMarker) {
            var rect1cx = fig1.getBBox().x;
            var rect1cy = fig1.getBBox().y;
            var rect1cw = fig1.getBBox().width;
            var rect1ch = fig1.getBBox().height;

            var rect2cx = fig2.getBBox().x;
            var rect2cy = fig2.getBBox().y;
            var rect2cw = fig2.getBBox().width;
            var rect2ch = fig2.getBBox().height;

            var markerCompensantion1 = 15;
            var markerCompensantion2 = 15;

            switch (side1) {
            case 'top':
              var originY = rect1cy;
              var originX = rect1cx + (rect1cw / 2);
              break;
            case 'bottom':
              var originY = rect1cy + rect1ch;
              var originX = rect1cx + (rect1cw / 2);
              break;
            case 'left':
              var originX = rect1cx - markerCompensantion1;
              var originY = rect1cy + (rect1ch / 2);
              break;
            case 'right':
              var originX = rect1cx + rect1cw;
              var originY = rect1cy + (rect1ch / 2);
              break;
            case 'bottom-50':
              var originY = rect1cy + rect1ch;
              var originX = rect1cx + 40;
              break;
            default:
              var originX = rect1cx + (rect1cw / 2);
              var originY = rect1cy + (rect1ch / 2);
              break;
            }

            switch (side2) {
            case 'top':
              var destinationY = rect2cy;
              var destinationX = rect2cx + (rect2cw / 2);
              break;
            case 'bottom':
              var destinationY = rect2cy + rect2ch;
              var destinationX = rect2cx + (rect2cw / 2);
              break;
            case 'left':
              var destinationX = rect2cx - markerCompensantion2;
              var destinationY = rect2cy + (rect2ch / 2);
              break;
            case 'right':
              var destinationX = rect2cx + rect2cw;
              var destinationY = rect2cy + (rect2ch / 2);
              break;
            case 'bottom-50':
              var destinationY = rect2cy + rect2ch;
              var destinationX = rect2cx + 50;
              break;
            default:
              var destinationX = rect2cx + (rect2cw / 2);
              var destinationY = rect2cy + (rect2ch / 2);
              break;
            }

            if (endMarker == null)
              endMarker = "BlackTriangle";

            var polyline1 = svg.polyline([ [ originX, originY ], [ originX, destinationY ],
              [ destinationX, destinationY ] ], {
              id : 'poly1',
              fill : 'none',
              stroke : 'black',
              strokeWidth : 2,
              'marker-end' : 'url(#' + endMarker + ')'
            });

          }

          function loadDefs(svg) {
            var defs = svg.defs();
            var blackTriangle = svg.marker(defs, 'BlackTriangle', 0, 0, 20, 20, {
              viewBox : '0 0 22 20',
              refX : '0',
              refY : '10',
              markerUnits : 'strokeWidth',
              markerWidth : '8',
              markerHeight : '6',
              fill : 'black',
              stroke : 'black',
              strokeWidth : 2
            });
            svg.path(blackTriangle, 'M 0 0 L 20 10 L 0 20 z');

            var clearTriangle = svg.marker(defs, 'ClearTriangle', 0, 0, 20, 20, {
              viewBox : '0 0 22 20',
              refX : '0',
              refY : '10',
              markerUnits : 'strokeWidth',
              markerWidth : '8',
              markerHeight : '8',
              fill : 'white',
              stroke : 'black',
              strokeWidth : 2
            });
            svg.path(clearTriangle, 'M 0 0 L 20 10 L 0 20 z');

            var lineMarker = svg.marker(defs, 'LineMarker', 0, 0, 20, 20, {
              viewBox : '0 0 22 20',
              refX : '0',
              refY : '10',
              markerUnits : 'strokeWidth',
              markerWidth : '8',
              markerHeight : '8',
              fill : 'white',
              stroke : 'black',
              strokeWidth : 2
            });
            svg.path(lineMarker, 'M 0 10 L 20 10');
          }

          function drawAttributeGroupNode(svg, x, y) {
            var circle = svg.circle(x, y, 20, {
              fill : 'white',
              stroke : 'black',
              strokeWidth : 2
            });
            return circle;
          }

          function drawConjunctionNode(svg, x, y) {
            var circle = svg.circle(x, y, 10, {
              fill : 'black',
              stroke : 'black',
              strokeWidth : 2
            });
            return circle;
          }

          function drawEquivalentNode(svg, x, y) {
            var g = svg.group();
            svg.circle(g, x, y, 20, {
              fill : 'white',
              stroke : 'black',
              strokeWidth : 2
            });
            svg.line(g, x - 7, y - 5, x + 7, y - 5, {
              stroke : 'black',
              strokeWidth : 2
            });
            svg.line(g, x - 7, y, x + 7, y, {
              stroke : 'black',
              strokeWidth : 2
            });
            svg.line(g, x - 7, y + 5, x + 7, y + 5, {
              stroke : 'black',
              strokeWidth : 2
            });
            return g;
          }

          function drawSubsumedByNode(svg, x, y) {
            var g = svg.group();
            svg.circle(g, x, y, 20, {
              fill : 'white',
              stroke : 'black',
              strokeWidth : 2
            });
            svg.line(g, x - 7, y - 8, x + 7, y - 8, {
              stroke : 'black',
              strokeWidth : 2
            });
            svg.line(g, x - 7, y + 3, x + 7, y + 3, {
              stroke : 'black',
              strokeWidth : 2
            });
            svg.line(g, x - 6, y - 8, x - 6, y + 3, {
              stroke : 'black',
              strokeWidth : 2
            });
            svg.line(g, x - 7, y + 7, x + 7, y + 7, {
              stroke : 'black',
              strokeWidth : 2
            });
            return g;
          }

          function drawSubsumesNode(svg, x, y) {
            var g = svg.group();
            svg.circle(g, x, y, 20, {
              fill : 'white',
              stroke : 'black',
              strokeWidth : 2
            });
            svg.line(g, x - 7, y - 8, x + 7, y - 8, {
              stroke : 'black',
              strokeWidth : 2
            });
            svg.line(g, x - 7, y + 3, x + 7, y + 3, {
              stroke : 'black',
              strokeWidth : 2
            });
            svg.line(g, x + 6, y - 8, x + 6, y + 3, {
              stroke : 'black',
              strokeWidth : 2
            });
            svg.line(g, x - 7, y + 7, x + 7, y + 7, {
              stroke : 'black',
              strokeWidth : 2
            });
            return g;
          }

          function convertToPng(svg, id) {
            element.find('*').not('.keep').remove();
            var canvas = document.createElement('canvas');
            canvas.id = "canvas-" + id;
            canvas.width = 2000;
            canvas.height = 1000;
            element.append(canvas);

            var c = document.getElementById('canvas-' + id);
            var ctx = c.getContext('2d');
            ctx.drawSvg(svg, 0, 0, 2000, 1000);
            cropImageFromCanvas(ctx, document.getElementById('canvas-' + id));

            element.find('#svg-' + id).remove();

            var img = new Image();
            img.id = 'image-' + id;
            if (element.find('#image-' + id)) {
              element.find('#image-' + id).remove();
            }
            img.src = canvas.toDataURL();
            scope.downloadLink = img.src;
            scope.img = img;
            element.find('#canvas-' + id).remove();
            element.append(img);
            element.find('#image-' + id).addClass('img-responsive');
            element.find('#image-' + id).css('max-width', '100%');
            element.find('#image-' + id).css('max-height', '100%');
            element.find('#image-' + id).css('padding', '10px');

          }

          scope.$on('openDrawModelConceptImage', function(event, data) {
            if (scope.concept.conceptId === data.conceptId
              || (!scope.concept.conceptId && !data.conceptId)) {
              scope.openImage(data.conceptId);
            }
          });

          scope.openImage = function(pageName) {
            var w = window.open(pageName);
            w.document.write(scope.img.outerHTML);
          };

          function cropImageFromCanvas(ctx, canvas) {

            var w = canvas.width, h = canvas.height, pix = {
              x : [],
              y : []
            }, imageData = ctx.getImageData(0, 0, canvas.width, canvas.height), x, y, index;

            for (y = 0; y < h; y++) {
              for (x = 0; x < w; x++) {
                index = (y * w + x) * 4;
                if (imageData.data[index + 3] > 0) {

                  pix.x.push(x);
                  pix.y.push(y);

                }
              }
            }
            pix.x.sort(function(a, b) {
              return a - b
            });
            pix.y.sort(function(a, b) {
              return a - b
            });
            var n = pix.x.length - 1;

            w = pix.x[n] - pix.x[0];
            h = pix.y[n] - pix.y[0];
            var cut = ctx.getImageData(pix.x[0], pix.y[0], w, h);

            canvas.width = w;
            canvas.height = h;
            ctx.putImageData(cut, 0, 0);

            var image = canvas.toDataURL();

          }

          function saveAsPng(svg) {
            //Create PNG Image
            //Get the svg
            //Create the canvas element
            var canvas = document.createElement('canvas');
            canvas.id = "canvas";
            document.body.appendChild(canvas);

            //Load the canvas element with our svg
            canvg(document.getElementById('canvas'), svg);

            //Save the svg to png
            Canvas2Image.saveAsPNG(canvas);

            //Clear the canvas
            canvas.width = canvas.width;
          }
        }
      };
    });