package org.substeps.report

/**
  * Created by ian on 06/02/17.
  */
trait GlossaryTemplate {

  def buildGlossaryReport(dateTimeString :String) ={

    s"""
<!DOCTYPE html>
<html lang="en">

<head>
    <title>Substeps Glossary</title>
    <meta charset="UTF-8">
    <link href="css/bootstrap.min.css" rel="stylesheet"/>
    <link href="css/bootstrap-responsive.min.css" rel="stylesheet"/>
    <link href="css/datatables.css" rel="stylesheet"/>
    <link href="css/substeps.css" rel="stylesheet"/>
     <link href="css/glossary.css" rel="stylesheet"/>

</head>

<body>

<nav class="navbar navbar-default navbar-fixed-top">
    <div class="container-fluid">

        <div class="navbar-header">
            <span class="navbar-brand" href="#">Stepimplementations in scope</span>
            <span class="navbar-brand" >${dateTimeString}</span>
        </div>

        <div class="collapse navbar-collapse">

            <ul class="nav navbar-nav navbar-right">
                <li class="active"><a href="report_frame.html">Results Summary</a></li>
                <li><a href="usage-tree.html">Usage <span class="label label-warning">Beta</span></a></li>
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
                <div class="col-md-3">
                     <h3 class="panel-title">Glossary</h3>
                </div>
                <div class="col-md-9">
                     <div class="row-fluid">
                        <div class="col-sm-2">
                            <label for="glossarySearchField">Search</label>
                        </div>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="glossarySearchField" placeholder="search glossary">

                        </div>
                     </div>
                </div>
            </div> 
        </div>

        <div class="panel-body">

                <table class="table table-striped table-bordered display" id="glossary-table">
                    <thead>
                    <tr>
                        <th>Section</th>
                        <th>Expression</th>
                        <th>Description</th>
                        <th>Example</th>
                    </tr>
                    </thead>
                </table>
        </div>
    </div>
</div>

<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery-ui.min.js"></script>
<script type="text/javascript" src="js/bootstrap.min.js"></script>
<script type="text/javascript" src="js/datatables.min.js"></script>
<script type="text/javascript" src="glossary-data.js"></script>
<script type="text/javascript" src="js/glossary.js"></script>

</body>
</html>
     """

  }
}
