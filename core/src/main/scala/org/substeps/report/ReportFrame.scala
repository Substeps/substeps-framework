package org.substeps.report

/**
  * Created by ian on 18/08/16.
  */


trait ReportFrameTemplate {



  def buildReportFrame(reportTitle: String, dateTimeStr : String, stats : ExecutionStats) = {

    s"""
       |<!DOCTYPE html>
       |<!-- Copyright Technophobia Ltd 2012 -->
       |<html lang="en">
       |
 |<head>
       |    <title>SubSteps report</title>
       |    <meta charset="UTF-8">
       |    <link href="css/bootstrap.min.css" rel="stylesheet"/>
       |    <link href="css/bootstrap-responsive.min.css" rel="stylesheet"/>
       |    <link href="css/substeps.css" rel="stylesheet"/>
       |
       |    <link rel="stylesheet" href="css/jstree/style.min.css" />
       |
       |<script type="text/javascript">
       |//<!--
       |function toggle(id){
       |
 |    elem = document.getElementById(id);
       |    if (elem.style.display == 'none') {
       |        elem.style.display = 'block';
       |
       |    } else {
       |        elem.style.display = 'none';
       |    }
       |}
       |
 | function hideNotRun(chkBox){
 |    var result = document.evaluate('//li[a/i[contains(@style, "NOT_RUN")]]', document.documentElement, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null)
 |    for (var i = 0; i < result.snapshotLength; i++) {
 |        if (chkBox.checked) {
 |            result.snapshotItem(i).style.display = 'none'
       |        }
       |        else {
       |            result.snapshotItem(i).style.display = 'block'
       |        }
       |    }
       |}
 |
 |//-->
       |
 |
 |</script>
       |
       |
       |</head>
       |
 |<body>
       |
 |<div class="container-fluid">
       |    <header class="row-fluid">
       |        <div id="navbar" class="navbar navbar-fixed-top">
       |            <div class="navbar-inner">
       |                <span class="brand" href="#" style="margin-left:5px">${reportTitle}</span> <span class="brand" style="margin-left:5px">${dateTimeStr}</span>
       |                <ul class="nav" style="float:right">
       |                    <li class="active"><a href="#summary">Summary</a></li>
       |                    <li><a href="#feature-tag-summary" onclick="javascript:toggle('feature-tag-summary')">Features by tag</a></li>
       |                    <li><a href="#scenario-tag-summary" onclick="javascript:toggle('scenario-tag-summary')">Scenario tag summary</a></li>
       |                    <li><a href="#test-detail">Test detail</a></li>
       |
       |                </ul>
       |            </div>
       |        </div>
       |    </header>
       |
 |    <div id="summary" class="row-fluid">
       |
       |        <div class="progress">
       |            <div class="bar bar-success" style="width: ${stats.featuresCounter.successPC}%;"></div>
       |
       |            <div class="bar bar-danger" style="width: ${stats.featuresCounter.failedPC}%;"></div>
       |        </div>
       |
       |        <table class="table table-striped table-bordered">
       |            <thead>
       |            <tr>
       |                <th><h4>Summary</h4></th>
       |                <th>Number</th>
       |                <th>Run</th>
       |                <th>Passed</th>
       |                <th>Failed</th>
       |                <th>Skipped</th>
       |                <th>Success %</th>
       |            </tr>
       |            </thead>
       |            <tbody>
       |            <tr>
       |                <td>Features</td>
       |                <td>${stats.featuresCounter.total}</td>
       |                <td>${stats.featuresCounter.run}</td>
       |                <td>${stats.featuresCounter.passed}</td>
       |                <td>${stats.featuresCounter.failed}</td>
       |                <td>${stats.featuresCounter.skipped}</td>
       |                <td>${stats.featuresCounter.successPC} %</td>
       |            </tr>
       |
 |            <tr>
       |                <td>Scenarios</td>
       |                <td>${stats.scenarioCounters.total}</td>
       |                <td>${stats.scenarioCounters.run}</td>
       |                <td>${stats.scenarioCounters.passed}</td>
       |                <td>${stats.scenarioCounters.failed}</td>
       |                <td>${stats.scenarioCounters.skipped}</td>
       |                <td>${stats.scenarioCounters.successPC} %</td>
       |            </tr>
       |
 |            <tr>
       |                <td>Scenario steps</td>
       |                <td>${stats.stepCounters.total}</td>
       |                <td>${stats.stepCounters.run}</td>
       |                <td>${stats.stepCounters.passed}</td>
       |                <td>${stats.stepCounters.failed}</td>
       |                <td>${stats.stepCounters.skipped}</td>
       |                <td>${stats.stepCounters.successPC} %</td>
       |            </tr>
       |            </tbody>
       |        </table>
       |    </div>
       |
       |    <div class="container">
       |    <div id="feature-tag-summary" class="row" style="display:none;">
       |        <header>
       |            <h4>Feature breakdown by tag</h4>
       |        </header>
       |
       |        <div id="feature-stats-div"></div>
       |
       |    </div>
       |    </div>

       |    <div class="container">
       |    <div id="scenario-tag-summary" class="row" style="display:none;">
       |        <header>
       |            <h4>Scenario breakdown by tag</h4>
       |        </header>
       |
       |         <div id="scenario-stats-div"></div>
       |
       |    </div>
       |    </div>
 |
 |
 |        <header>
       |            <h2>Test details</h2>
       |        </header>
       |    <div>
       |        <input type="checkbox" onclick="javascript:hideNotRun(this)"/>Hide not run
       |    </div>
       |
       |
       |    <noscript>
       |        <h3>Please enable Javascript to view Test details</h3>
       |        <p>Non java script variants of this report were not viable, sorry.  We found that there was simply too much data to display and page load times were approaching unacceptable.</p>
       |        <p>Please enable javascript and reload this page</p>
       |    </noscript>
       |
       |    <div id="test-detail" class="row-fluid">
       |
 |        <div id="feature-tree" class="span7">
       |
 |        </div>
       |
 |
 |        <div class="span5" id="detail-div-container">
       |            <div id="affix-marker" data-spy="affix" data-offset-top="200"></div>
       |            <div id="feature-detail" class="detail-div"></div>
       |        </div>
       |    </div>
       |</div>
       |
       |<script type="text/javascript" src="js/jquery.min.js"></script>
       |<script type="text/javascript" src="js/bootstrap.min.js"></script>
       |<script type="text/javascript" src="js/datatables.min.js"></script>
       |<script type="text/javascript" src="js/jstree.min.js"></script>
       |<script type="text/javascript" src="js/substeps.js"></script>
       |<script type="text/javascript" src="substeps-results-tree.js"></script>
       |<script type="text/javascript" src="detail_data.js"></script>
       |<script type="text/javascript" src="substeps-stats-by-tag.js"></script>
       |
       |</body>
       |</html>
     """.stripMargin
  }

}
