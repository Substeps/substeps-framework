
var svgTemplate = Handlebars.compile(  '<svg viewBox="0 0 32 32" style="margin-right:4px"> ' +
  '<title>{{passPC}}% Pass {{notRunPC}}% Not run {{failPC}}% Fail</title> ' +
  '<circle r="16" cx="16" cy="16" stroke-dasharray="{{passPC}} 100"></circle> ' +
  '<circle class="not-run" r="16" cx="16" cy="16" stroke-dashoffset="-{{passPC}}" stroke-dasharray="{{notRunPC}} 100" fill-opacity="0.0"></circle> ' +
  '</svg>');

var stepImplUsageRowTemplate = Handlebars.compile('  <div class="row-fluid">' +
  '<div class="col-md-2">{{{result}}}</div>' +
'<div class="col-md-7">{{line}}</div>' +
'<div class="col-md-3">{{file}}:{{lineNumber}}</div>' +
'</div>');


var stepImplUsageDetailTemplate = Handlebars.compile('  <div class="row-fluid">' +
  '<h4>Usages</h4>' +
  '</div>' +
  '<div class="row-fluid">' +
  '<p style="word-break: break-all;">{{method}}</p>' +
'</div>' +
'{{{usageRows}}}');


$(document).ready(function() {

  $("#step-impls-usage-tree").jstree({
    "core":{
      "data":stepImplUsageTreeData,
      "progressive_render":true,
      "themes" : {
        "dots" : true,
        "icons" : true
      },
      "multiple" : false
    }
  });

  $("#substep-usage-tree").jstree({
    "core":{
      "data":substepDefUsageTreeData,
      "progressive_render":true,
      "themes" : {
        "dots" : true,
        "icons" : true
      },
      "multiple" : false
    }
  });

  $('#uncalled-step-impls-table').DataTable({
    paging: false,
    searching: false,
    "data": uncalledStepImplementations,
    "columns": [
      {data: 'value', title: "Value"},
      {data: 'implementedIn', title: "implemented in"},
      {data: 'method', title: "method"},
      {data: 'keyword', title: "Keyword"}
    ],
    "dom" : "<'row-fluid'<'col-sm-6'l><'col-sm-6'f>>" +
    "<'row-fluid'<'col-sm-12'tr>>" +
    "<'row-fluid'<'col-sm-5'i><'col-sm-7'p>>"
  });


  $('#uncalled-substeps-table').DataTable({
    paging: false,
    searching: false,
    "data": uncalledStepDefs,
    "columns": [
      {data: 'line', title: "Line"},
      {data: 'source', title: "source"},
      {data: 'lineNumber', title: "number"}
    ],
    "dom" : "<'row-fluid'<'col-sm-6'l><'col-sm-6'f>>" +
    "<'row-fluid'<'col-sm-12'tr>>" +
    "<'row-fluid'<'col-sm-5'i><'col-sm-7'p>>"
  });


});

$(function () {
  $('#substep-usage-tree')
    .on('select_node.jstree', function (e, data) {

      var liattr = data.node.li_attr["data-substep-def-call"]
      if (typeof liattr === "undefined"){
        $("#substep-usage-detail").html("");
      }
      else {

        var substepDefDetail = "<p>" + data.node.li_attr["data-substep-def-call"] +"</p>";

        $("#substep-usage-detail").html(substepDefDetail);

      }

    }).jstree();
});

$(function () {
  $('#step-impls-usage-tree')
    .on('select_node.jstree', function (e, data) {

      var liattr = data.node.li_attr["data-stepimpl-method"];

      if (typeof liattr === "undefined"){
        $("#step-impl-usage-detail").html("");
      }
      else
      {
        var nodeIds = data.node.li_attr["data-stepimpl-node-ids"].split(",");

        var context = {method: data.node.li_attr["data-stepimpl-method"], usageRows : nodeIds.map(getStepImplUsageDetail).join(" ")};
        var detailhtml    = stepImplUsageDetailTemplate(context);

        $("#step-impl-usage-detail").html(detailhtml);

        // get the offset of where we should be?
        var topOffsetShouldbe = $("#step-impl-detail-div-container").offset().top;

        // get the offset of the affixed div
        var affixOffset = $("#step-impl-affix-marker").offset().top;

        // so the absolute position position, relative to the parent should be affixOffset - topOffsetShouldbe
        var absPosition = affixOffset - topOffsetShouldbe;

        if (absPosition < 0){
          absPosition = 0;
        }

        $("#step-impl-usage-detail").css("top", absPosition + 'px');

        $("#step-impl-usage-detail").css("width", ($("#step-impl-detail-div-container").width));
      }
    })
    .jstree();
});

$(function () {
  $('#step-impls-usage-tree')
    .on('ready.jstree', function (e, data) {

      $("li[data-stepimpl-method]").each(function(i, li) {

        // li/a/i - insert into there
        var treeIconPlaceholder = $(li).find("> a > i");


        var passPc = parseFloat($(li).attr("data-stepimpl-passpc"));
        var failPC = parseFloat($(li).attr("data-stepimpl-failpc"));
        var notRunPC = parseFloat($(li).attr("data-stepimpl-notrunpc"));

        var context = {passPC: passPc, notRunPC : notRunPC, failPC : failPC};
        var html    = svgTemplate(context);

        $(treeIconPlaceholder).html(html)
      });

    }).jstree();
});

$(function () {
  $('#substep-usage-tree')
    .on('ready.jstree', function (e, data) {

      $("li[data-substep-def]").each(function(i, li) {

        // li/a/i - insert into there
        var treeIconPlaceholder = $(li).find("> a > i");


        var passPc = parseFloat($(li).attr("data-substepdef-passpc"));
        var failPC = parseFloat($(li).attr("data-substepdef-failpc"));
        var notRunPC = parseFloat($(li).attr("data-substepdef-notrunpc"));

        var context = {passPC: passPc, notRunPC : notRunPC, failPC : failPC};
        var html    = svgTemplate(context);

        $(treeIconPlaceholder).html(html)
      });

    }).jstree();
});

$('#substep-usage-show-hide').on('click', function () {

  toggleShowHide(this);

});

$('#stepimpl-usage-show-hide').on('click', function () {

  toggleShowHide(this);

});

function toggleShowHide(elem){
  if ($(elem).text() == 'Hide') {
    $(elem).text('Show')
  }
  else {
    $(elem).text('Hide');
  }
}

function getStepImplUsageDetail(id) {

  var detailJSON = detail[id];
  var result;
  if (detailJSON.result == "PASSED"){
    result = '<span class="label label-success">Passed</span>';
  }
  else if (detailJSON.result == "NOT_RUN"){
    result = '<span class="label label-warning">Not run</span>';
  }
  else if (detailJSON.result == "FAILED"){
    result = '<span class="label label-danger">Failed</span>';
  }
  else if (detailJSON.result == "CHILD_FAILED"){
    result = '<span class="label label-info">Child failed</span>';
  }

  var context = {result:result,line:detailJSON.description,file:detailJSON.filename, lineNumber:detailJSON.lineNum};
  return stepImplUsageRowTemplate(context);
}

