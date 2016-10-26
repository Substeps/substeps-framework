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
    <link href="css/usage.css" rel="stylesheet"/>

    <link rel="stylesheet" href="css/jstree/style.min.css" />


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

<script type="text/javascript" src="js/usage.js"></script>

</body>
</html>
"""
}
