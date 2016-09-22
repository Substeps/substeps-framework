package org.substeps.report

/**
  * Created by ian on 16/09/16.
  */
trait UsageTreeTemplate {

  def buildUsageTree() =
  """<!DOCTYPE html>
    |<html lang="en">
    |
    |<head>
    |    <title>Substeps usage report</title>
    |    <meta charset="UTF-8">
    |    <link href="css/bootstrap.min.css" rel="stylesheet"/>
    |    <link href="css/bootstrap-responsive.min.css" rel="stylesheet"/>
    |    <link href="css/substeps.css" rel="stylesheet"/>
    |
    |    <link rel="stylesheet" href="css/jstree/style.min.css" />
    |
    |    <style>
    |        circle {
    |            fill: #C02D05;
    |            stroke: #59801A;
    |            stroke-width: 32;
    |        }
    |
    |        svg {
    |            width: 22px; height: 22px;
    |            transform: rotate(-90deg);
    |            background:#C02D05;
    |            border-radius: 50%;
    |        }
    |
    |    </style>
    |
    |    <script id="pie-svg-template" type="text/x-handlebars-template">
    |        <svg viewBox="0 0 32 32" style="margin-right:4px"><title>{{pc}}%</title><circle r="16" cx="16" cy="16" stroke-dasharray="{{pc}} 100"></circle></svg>
    |    </script>
    |
    |</head>
    |
    |<body>

    |<nav class="navbar navbar-default navbar-fixed-top">
    |    <div class="container-fluid">
    |
    |        <div class="navbar-header">
    |            <span class="navbar-brand" href="#">title</span>
    |            <span class="navbar-brand" >dateTime</span>
    |
    |        </div>
    |
    |        <div class="collapse navbar-collapse">
    |
    |            <ul class="nav navbar-nav navbar-right">
    |                <li class="active"><a href="report_frame.html">Results Summary</a></li>
    |
    |            </ul>
    |        </div>
    |    </div>
    |</nav>
    |
    |
    |<div class="container-fluid">
    |
    |
    |
    |        <header>
    |            <h2>Step Implementation Usage Tree</h2>
    |        </header>
    |
    |
    |    <noscript>
    |        <h3>Please enable Javascript to view Test details</h3>
    |        <p>Non java script variants of this report were not viable, sorry.  We found that there was simply too much data to display and page load times were approaching unacceptable.</p>
    |        <p>Please enable javascript and reload this page</p>
    |    </noscript>
    |
    |    <div id="test-detail" class="row-fluid">
    |        <div id="step-impls-usage-tree" class="col-md-6"></div>
    |
    |
    |        <div class="col-md-6" id="detail-div-container">
    |            <div id="affix-marker" data-spy="affix" data-offset-top="200"></div>
    |            <div id="usage-detail" class="detail-div"></div>
    |        </div>
    |    </div>
    |
    |</div>
    |
    |<script type="text/javascript" src="js/jquery.min.js"></script>
    |<script type="text/javascript" src="js/bootstrap.min.js"></script>
    |<script type="text/javascript" src="js/datatables.min.js"></script>
    |<script type="text/javascript" src="js/jstree.min.js"></script>
    |<script type="text/javascript" src="js/handlebars-v4.0.5.js"></script>
    |<!--<script type="text/javascript" src="js/substeps.js"></script>-->
    |<script type="text/javascript" src="substeps-results-tree.js"></script>
    |<script type="text/javascript" src="detail_data.js"></script>
    |<script type="text/javascript" src="substeps-usage-tree.js"></script>
    |
    |<script type="text/javascript">
    |
    |    var source   = $("#pie-svg-template").html();
    |    var svgTemplate = Handlebars.compile(source);
    |
    |    $(document).ready(function() {
    |
    |        console.log("doc ready rendering usage tree")
    |
    |        $("#step-impls-usage-tree").jstree({
    |            "core":{
    |                "data":stepImplUsageTreeData,
    |                "progressive_render":true,
    |                "themes" : {
    |                    "dots" : true,
    |                    "icons" : true
    |                },
    |                "multiple" : false
    |            }
    |        });
    |    });
    |
    |    function formatStackElement(item, index){
    |        return "<li>" + item + "</li>"
    |    }
    |
    |    $(function () {
    |        $('#step-impls-usage-tree')
    |                .on('select_node.jstree', function (e, data) {
    |
    |                  var liattr = data.node.li_attr["data-stepimpl-method"]
    |
    |                    if (typeof liattr === "undefined"){
    |                        $("#usage-detail").html("");
    |                    }
    |                    else
    |                    {
    |                        var detailhtml = "<p>" + data.node.li_attr["data-stepimpl-method"] +"</p>"
    |
    |                        $("#usage-detail").html(detailhtml);
    |
    |                        // get the offset of where we should be?
    |                        var topOffsetShouldbe = $("#detail-div-container").offset().top;
    |
    |                        // get the offset of the affixed div
    |                        var affixOffset = $("#affix-marker").offset().top;
    |
    |                        // so the absolute position position, relative to the parent should be affixOffset - topOffsetShouldbe
    |                        var absPosition = affixOffset - topOffsetShouldbe;
    |
    |                        if (absPosition < 0){
    |                            absPosition = 0;
    |                        }
    |
    |                        $("#usage-detail").css("top", absPosition + 'px');
    |                    }
    |                })
    |                .jstree();
    |    });
    |
    |    $(function () {
    |        $('#step-impls-usage-tree')
    |                .on('ready.jstree', function (e, data) {
    |
    |                    $("li[data-stepimpl-method]").each(function(i, li) {
    |
    |                        // li/a/i - insert into there
    |                        var treeIconPlaceholder = $(li).find("> a > i")
    |
    |
    |                        var passPc = parseFloat($(li).attr("data-stepimpl-passpc"))
    |                        var failPC = parseFloat($(li).attr("data-stepimpl-failpc"))
    |                        var notRunPC = parseFloat($(li).attr("data-stepimpl-notrunpc"))
    |
    |                        var context = {pc: passPc};
    |                        var html    = svgTemplate(context);
    |
    |                        $(treeIconPlaceholder).html(html)
    |                    });
    |
    |                }).jstree();
    |    });
    |
    |</script>
    |
    |</body>
    |</html>
    |""".stripMargin
}
