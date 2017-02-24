package org.substeps.report

import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

/**
  * Created by ian on 18/08/16.
  */


trait ReportFrameTemplate {


  def buildStatsBlock(name: String, counters : Counters) = {

    s"""
       |    <div class="row-fluid">
       |
       |        <div class="col-md-2">${name} &nbsp;<span class="badge">${counters.total}</span></div>
       |
       |        <div class="col-md-10">
       |
       |            <div class="progress">
       |                <div class="progress-bar progress-bar-success" style="width: ${counters.successPC}%;">${counters.successPC} Success (${counters.passed})</div>
       |                <div class="progress-bar progress-bar-danger" style="width: ${counters.failedPC}%">${counters.failedPC}% Failure (${counters.failed})</div>
       |                <div class="progress-bar progress-bar-warning" style="width: ${counters.skippedPC}%">${counters.skippedPC}% Not run (${counters.skipped})</div>
       |            </div>
       |
       |        </div>
       |
       |    </div>
       |
     """.stripMargin

  }

  def buildReportFrame(rootNodeSummary: RootNodeSummary, stats : ExecutionStats, dateTimeString : String) = {

    val featureProgressBlock = buildStatsBlock("Features", stats.featuresCounter)
    val scenarioProgressBlock = buildStatsBlock("Scenarios", stats.scenarioCounters)
    val scenarioStepProgressBlock = buildStatsBlock("Scenario steps", stats.stepCounters)
    val stepImplBlock = buildStatsBlock("Step Impls", stats.stepImplCounters)

    val reportTitle = Option(rootNodeSummary.description).getOrElse("Substeps Test Report")


    // TODO pull out some of the other things from the node summary - tags, nonfatal tags and environment

    val env = rootNodeSummary.environment


    val nonFatalTagsOption =
        rootNodeSummary.nonFatalTags.map(t => s"""<p class="navbar-text navbar-left">Non Fatal Tags: ${t}</p>""" )

    val nonFatalTags = nonFatalTagsOption.getOrElse("")

    val tags = rootNodeSummary.tags.getOrElse("")



    s"""
       |<!DOCTYPE html>
       |<!-- Original Copyright Technophobia Ltd 2012, later revisions by others, see github  -->
       |<html lang="en">
       |
 |<head>
       |    <title>Substeps report</title>
       |    <meta charset="UTF-8">
       |    <link href="css/bootstrap.min.css" rel="stylesheet"/>
       |    <link href="css/bootstrap-responsive.min.css" rel="stylesheet"/>
       |    <link href="css/substeps.css" rel="stylesheet"/>
       |
       |    <link rel="stylesheet" href="css/jstree/style.min.css" />
       |    <script type="text/javascript" src="results-summary.js"></script>
       |
       |
       |
       |</head>
       |
 |<body>
 |
 |<nav class="navbar navbar-default navbar-fixed-top">
       |    <div class="container-fluid">
       |
 |        <div class="navbar-header">
       |            <span class="navbar-brand" href="#">${reportTitle}</span>
       |            <span class="navbar-brand" >${dateTimeString}</span>
       |            <p class="navbar-text navbar-left">Environment: ${env}</p>
       |            <p class="navbar-text navbar-left">Tags: ${tags}</p>
       |            ${nonFatalTags}
       |
       |
 |        </div>
       |
 |        <div class="collapse navbar-collapse">
       |
 |            <ul class="nav navbar-nav navbar-right">
       |                <li class="active"><a href="#summary">Summary</a></li>
       |                <li><a href="#feature-tag-summary" onclick="javascript:toggle('feature-tag-summary')">Features by tag</a></li>
       |                <li><a href="#scenario-tag-summary" onclick="javascript:toggle('scenario-tag-summary')">Scenario tag summary</a></li>
       |                <li><a href="#test-detail">Test detail</a></li>
       |                <li><a href="usage-tree.html">Usage <span class="label label-warning">Beta</span></a></li>
       |                <li><a href="glossary.html">Glossary</a></li>
       |
 |            </ul>
       |        </div>
       |    </div>
       |</nav>
       |
       |
       |<div class="container-fluid" style="padding-top:10px">
       |    <div class="panel panel-default">
       |        <div class="panel-heading">
       |            <h3 class="panel-title">Summary</h3>
       |        </div>
       |        <div class="panel-body">
       |
       |     ${featureProgressBlock}
       |
       |     ${scenarioProgressBlock}
       |
       |     ${scenarioStepProgressBlock}
       |
       |
       |             </div>
       |    </div>
       |
       |        <div class="panel panel-default">
       |        <div class="panel-heading">
       |            <div class="row-fluid">
       |                <div class="col-md-11">
       |                    <h3 class="panel-title">Summary table</h3>
       |                </div>
       |                <div class="col-md-1">
       |                   <a class="btn btn-primary btn-xs pull-right" role="button" data-toggle="collapse" href="#summaryTable" aria-expanded="false" aria-controls="summaryTable">Show</a>
       |                </div>
       |            </div>
       |
       |        </div>
       |        <div class="panel-body">
       |
       |    <div id="summaryTable" class="row-fluid collapse">
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
       |            </div>
       |    </div>
       |
       |    <div class="panel panel-default">
       |        <div class="panel-heading">
       |          <div class="row-fluid">
       |                <div class="col-md-6">
       |                    <h3 class="panel-title">Test details</h3>
       |                </div>
       |
       |                    <div class="col-md-1">
       |                        <label style=" margin-bottom: 0px;">Key:</label>
       |                    </div>
       |                    <div class="col-md-5">
       |
 |                        <label class="icon-key">
       |                        <img class="key-img" src="img/PASSED.png" alt="Passed"> <span>Passed</span>
       |                        </label>
       |
 |                        <label class="icon-key">
       |                        <img class="key-img" src="img/FAILED.png" alt="Failure"> <span>Failed</span>
       |                        </label>
       |
 |
       |                        <label class="icon-key">
       |                        <img class="key-img" src="img/CHILD_FAILED.png" alt="Child failed"> <span>Child failed</span>
       |                        </label>
       |
 |
 |
 |                        <label class="icon-key">
       |                        <img class="key-img" src="img/NOT_RUN2.png" alt="Not run"> <span>Not run</span>
       |                        </label>
       |
 |
 |                        <label class="icon-key">
       |                        <img class="key-img" src="img/NON_CRITICAL_FAILURE.png" alt="Non critical failure"> <span>Non critical failure</span>
       |                        </label>
       |
 |
 |                    </div>
       |          </div>
       |        </div>
       |        <div class="panel-body">
       |
       |    <div>
       |        <input id="hide-not-run-chk" type="checkbox"/>Hide not run
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
       |        <div class="span5" id="detail-div-container">
       |            <div id="affix-marker" data-spy="affix" data-offset-top="200"></div>
       |            <div id="feature-detail" class="detail-div"></div>
       |        </div>
       |    </div>
       |        </div>
       |    </div>
       |</div>
       |
       |<script type="text/javascript" src="js/jquery.min.js"></script>
       |<script type="text/javascript" src="js/jquery-ui.min.js"></script>
       |<script type="text/javascript" src="js/bootstrap.min.js"></script>
       |<script type="text/javascript" src="js/datatables.min.js"></script>
       |<script type="text/javascript" src="js/jstree.min.js"></script>
       |<script type="text/javascript" src="js/substeps.js"></script>
       |<script type="text/javascript" src="substeps-results-tree.js"></script>
       |<script type="text/javascript" src="detail_data.js"></script>
       |<script type="text/javascript" src="substeps-stats-by-tag.js"></script>
       |
       |
       |</body>
       |</html>
     """.stripMargin
  }

}
