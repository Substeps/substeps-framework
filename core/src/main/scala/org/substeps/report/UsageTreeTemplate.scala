package org.substeps.report

/**
  * Created by ian on 16/09/16.
  */
trait UsageTreeTemplate {

  /*
    * SVG Pie chart refs:
    * https://css-tricks.com/using-svg/
    * https://css-tricks.com/how-to-make-charts-with-svg/
    * -> https://www.smashingmagazine.com/2015/07/designing-simple-pie-charts-with-css/
    * @return
    */

  def buildUsageTree() =
  """
<!DOCTYPE html>
<html lang="en">

<head>
    <title>Substeps usage report</title>
    <meta charset="UTF-8">
    <link href="css/bootstrap.min.css" rel="stylesheet"/>
    <link href="css/bootstrap-responsive.min.css" rel="stylesheet"/>
    <link href="css/datatables.css" rel="stylesheet"/>
    <link href="css/substeps.css" rel="stylesheet"/>

    <link rel="stylesheet" href="css/jstree/style.min.css" />

    <style>
        circle {
            fill: #C02D05;
            stroke: #59801A;
            stroke-width: 32;
        }

        circle.not-run {
           stroke: #f0ad4e;
        }

        svg {
            width: 22px; height: 22px;
            transform: rotate(-90deg);
            background:#C02D05;
            border-radius: 50%;
        }

    </style>

    <script id="pie-svg-template" type="text/x-handlebars-template">


        <svg viewBox="0 0 32 32" style="margin-right:4px">
        <title>{{passPC}}% Pass {{notRunPC}}% Not run {{failPC}}% Fail</title>
        <circle r="16" cx="16" cy="16" stroke-dasharray="{{passPC}} 100"></circle>
        <circle class="not-run" r="16" cx="16" cy="16" stroke-dashoffset="-{{passPC}}" stroke-dasharray="{{notRunPC}} 100" fill-opacity="0.0"></circle>
        </svg>
    </script>

    <script id="step-impl-usage-detail-template" type="text/x-handlebars-template">
        <div class="row-fluid">
            <h4>Usages</h4>
        </div>
        <div class="row-fluid">
            <p style="word-break: break-all;">{{method}}</p>
        </div>
        {{{usageRows}}}
    </script>

    <script id="step-impl-usage-row-template" type="text/x-handlebars-template">
        <div class="row-fluid">
            <div class="col-md-2">{{{result}}}</div>
            <div class="col-md-7">{{line}}</div>
            <div class="col-md-3">{{file}}:{{lineNumber}}</div>
        </div>
    </script>

</head>

<body>

<nav class="navbar navbar-default navbar-fixed-top">
    <div class="container-fluid">

        <div class="navbar-header">
            <span class="navbar-brand" href="#">Substeps Usage Report <span class="label label-warning">Beta</span></span>
            <span class="navbar-brand" >dateTime</span>

        </div>

        <div class="collapse navbar-collapse">

            <ul class="nav navbar-nav navbar-right">
                <li class="active"><a href="report_frame.html">Results Summary</a></li>

            </ul>
        </div>
    </div>
</nav>


    <noscript>
        <h3>Please enable Javascript to view Test details</h3>
        <p>Non java script variants of this report were not viable, sorry.  We found that there was simply too much data to display and page load times were approaching unacceptable.</p>
        <p>Please enable javascript and reload this page</p>
    </noscript>

<div class="container-fluid" style="padding-top:10px">

    <div class="panel panel-default">
       <div class="panel-heading">
            <div class="row-fluid">
                <div class="col-md-11">
                    <h3 class="panel-title">Step Implementation Usage Tree</h3>
                </div>
                <div class="col-md-1">
                   <button id="stepimpl-usage-show-hide" class="btn btn-primary btn-xs pull-right" role="button"
                     data-toggle="collapse" href="#step-impl-usage" aria-expanded="true" aria-controls="step-impl-usage">Hide</button>
                </div>
            </div>
         </div>

        <div class="panel-body">


            <div id="step-impl-usage" class="row-fluid collapse in">
                <div id="step-impls-usage-tree" class="col-md-6"></div>

                <div class="col-md-6" id="step-impl-detail-div-container">
                    <div id="step-impl-affix-marker" data-spy="affix" data-offset-top="200"></div>
                    <div id="step-impl-usage-detail" class="detail-div"></div>
                 </div>
            </div>
        </div>
    </div>

    <div class="panel panel-default">
          <div class="panel-heading">
                 <div class="row-fluid">
                    <div class="col-md-11">
                       <h3 class="panel-title">Substep Usage Tree</h3>
                    </div>
                    <div class="col-md-1">
                      <button type="button" id="substep-usage-show-hide" class="btn btn-primary btn-xs pull-right" role="button" data-toggle="collapse" href="#substep-usage" aria-expanded="true" aria-controls="substep-usage" autocomplete="off">Hide</button>
                    </div>
                </div>
            </div>

        <div class="panel-body">

            <div id="substep-usage" class="row-fluid collapse in">
                <div id="substep-usage-tree" class="col-md-6"></div>


                <div class="col-md-6" id="substep-detail-div-container">
                    <div id="affix-marker2" data-spy="affix" data-offset-top="200"></div>
                    <div id="substep-usage-detail" class="detail-div"></div>
                </div>
            </div>


        </div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">
            <div class="row-fluid">
                <div class="col-md-11">
                    <h3 class="panel-title">Uncalled Step Implementations</h3>
                </div>
                <div class="col-md-1">
                    <button type="button" id="uncalled-stepimpl-show-hide" class="btn btn-primary btn-xs pull-right" role="button"
                            data-toggle="collapse" href="#uncalled-step-impl-panel" aria-expanded="true" aria-controls="uncalled-step-impl-panel" autocomplete="off">Hide</button>
                </div>
            </div>
        </div>

        <div class="panel-body">

            <div id="uncalled-step-impl-panel" class="collapse in">

                <table class="table table-striped table-bordered display" id="uncalled-step-impls-table">
                    <thead>
                    <tr>
                        <th>Value</th>
                        <th>implemented in</th>
                        <th>method</th>
                        <th>Keyword</th>
                    </tr>
                    </thead>
                </table>
            </div>


        </div>
    </div>


    <div class="panel panel-default">
        <div class="panel-heading">
            <div class="row-fluid">
                <div class="col-md-11">
                    <h3 class="panel-title">Uncalled Substeps</h3>
                </div>
                <div class="col-md-1">
                    <button type="button" id="uncalled-substep-show-hide" class="btn btn-primary btn-xs pull-right" role="button"
                            data-toggle="collapse" href="#uncalled-substep-panel" aria-expanded="true" aria-controls="uncalled-substep-panel" autocomplete="off">Hide</button>
                </div>
            </div>
        </div>

        <div class="panel-body">

            <div id="uncalled-substep-panel" class="collapse in">

                <table class="table table-striped table-bordered display" id="uncalled-substeps-table">
                    <thead>
                    <tr>
                        <th>Line</th>
                        <th>source</th>
                        <th>line #</th>
                    </tr>
                    </thead>
                </table>
            </div>

        </div>
    </div>


</div>



<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery-ui.min.js"></script>
<script type="text/javascript" src="js/bootstrap.min.js"></script>
<script type="text/javascript" src="js/datatables.min.js"></script>
<script type="text/javascript" src="js/jstree.min.js"></script>
<script type="text/javascript" src="js/handlebars-v4.0.5.js"></script>
<!--<script type="text/javascript" src="js/substeps.js"></script>-->
<script type="text/javascript" src="substeps-results-tree.js"></script>
<script type="text/javascript" src="detail_data.js"></script>
<script type="text/javascript" src="substeps-usage-tree.js"></script>

<script type="text/javascript" src="uncalled.stepdefs.js"></script>
<script type="text/javascript" src="uncalled.stepimpls.js"></script>

<script type="text/javascript">

    var svgTemplate = Handlebars.compile($("#pie-svg-template").html());

    var stepImplUsageRowTemplate = Handlebars.compile($("#step-impl-usage-row-template").html())
    var stepImplUsageDetailTemplate = Handlebars.compile($("#step-impl-usage-detail-template").html())


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

                        var substepDefDetail = "<p>" + data.node.li_attr["data-substep-def-call"] +"</p>"

                        $("#substep-usage-detail").html(substepDefDetail);

                    }

                }).jstree();
    });

    $(function () {
        $('#step-impls-usage-tree')
                .on('select_node.jstree', function (e, data) {

                  var liattr = data.node.li_attr["data-stepimpl-method"]

                    if (typeof liattr === "undefined"){
                        $("#step-impl-usage-detail").html("");
                    }
                    else
                    {
                        var nodeIds = data.node.li_attr["data-stepimpl-node-ids"].split(",")

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

                        $("#step-impl-usage-detail").css("width", ($("#step-impl-detail-div-container").width))
                    }
                })
                .jstree();
    });

    $(function () {
        $('#step-impls-usage-tree')
                .on('ready.jstree', function (e, data) {

                    $("li[data-stepimpl-method]").each(function(i, li) {

                        // li/a/i - insert into there
                        var treeIconPlaceholder = $(li).find("> a > i")


                        var passPc = parseFloat($(li).attr("data-stepimpl-passpc"))
                        var failPC = parseFloat($(li).attr("data-stepimpl-failpc"))
                        var notRunPC = parseFloat($(li).attr("data-stepimpl-notrunpc"))

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
                        var treeIconPlaceholder = $(li).find("> a > i")


                        var passPc = parseFloat($(li).attr("data-substepdef-passpc"))
                        var failPC = parseFloat($(li).attr("data-substepdef-failpc"))
                        var notRunPC = parseFloat($(li).attr("data-substepdef-notrunpc"))

                        var context = {passPC: passPc, notRunPC : notRunPC, failPC : failPC};
                        var html    = svgTemplate(context);

                        $(treeIconPlaceholder).html(html)
                    });

                }).jstree();
    });

  $('#substep-usage-show-hide').on('click', function () {

    toggleShowHide(this)

  })

  $('#stepimpl-usage-show-hide').on('click', function () {

    toggleShowHide(this)

  })

function toggleShowHide(elem){
    if ($(elem).text() == 'Hide') {
        $(elem).text('Show')
    }
    else {
     $(elem).text('Hide')
     $(elem).button('reset');
    }
}

function getStepImplUsageDetail(id) {

    var detailJSON = detail[id];
    var result
    if (detailJSON.result == "PASSED"){
        result = '<span class="label label-success">Passed</span>'
    }
    else if (detailJSON.result == "NOT_RUN"){
        result = '<span class="label label-warning">Not run</span>'
    }
    else if (detailJSON.result == "FAILED"){
        result = '<span class="label label-danger">Failed</span>'
    }
    else if (detailJSON.result == "CHILD_FAILED"){
        result = '<span class="label label-info">Child failed</span>'
    }

    var context = {result:result,line:detailJSON.description,file:detailJSON.filename, lineNumber:detailJSON.lineNum}
    return stepImplUsageRowTemplate(context)
}

</script>

</body>
</html>
"""
}
