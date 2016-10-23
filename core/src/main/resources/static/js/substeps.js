$(document).ready(function() {

// cellpadding="0" cellspacing="0" border="0" 
	$('#feature-stats-div').html('<table class="table table-striped table-bordered display" id="feature-stats-table"></table>');

	$('#feature-stats-table').DataTable({
		paging: false,
		searching: false,
		"data": featureStatsData,
		"columns": [
			{data: 'tag', title: "Tag"},
			{data: 'total', title: "total"},
			{data: 'run', title: "run"},
			{data: 'passed', title: "passed"},
			{data: 'failedPC', title: "failed"},
			{data: 'successPC', title: "success"}
		],

		"columnDefs": [

			{
				// The `data` parameter refers to the data for the cell (defined by the
				// `data` option, which defaults to the column being worked with, in
				// this case `data: 0`.
				"render": function (data, type, row) {
					return data + ' %';
				},
				"targets": [4, 5]
			}
		]
	});

	$('#scenario-stats-div').html('<table class="table table-striped table-bordered display" id="scenario-stats-table"></table>');

	$('#scenario-stats-table').DataTable({
		paging: false,
		searching: false,
		"data": scenarioStatsData,
		"columns": [
			{data: 'tag', title: "Tag"},
			{data: 'total', title: "total"},
			{data: 'run', title: "run"},
			{data: 'passed', title: "passed"},
			{data: 'failedPC', title: "failed"},
			{data: 'successPC', title: "success"}
		],

		"columnDefs": [
			{
				"render": function (data, type, row) {
					return data + ' %';
				},
				"targets": [4, 5]
			}
		]
	});


});




$(document).ready(function() {
	$("#feature-tree").jstree({
		"core":{
			"data":treeData,
			"progressive_render":true,
			"themes" : {
				"dots" : true,
				"icons" : true
			},
			"multiple" : false
		}
	});

	$("#hide-not-run-chk").bind("click", function(event){
		var result = document.evaluate('//li[a/i[contains(@class, "NOT_RUN")]]', document.documentElement, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null)
		for (var i = 0; i < result.snapshotLength; i++) {

			if (event.target.checked) {
				result.snapshotItem(i).style.display = 'none'
			}
			else {
				result.snapshotItem(i).style.display = 'block'
			}
		}

	});
});

function formatStackElement(item, index){
	return "<li>" + item + "</li>"
}


	function toggle(id){

		elem = document.getElementById(id);
		if (elem.style.display == 'none') {
			elem.style.display = 'block';

		} else {
			elem.style.display = 'none';
		}
	}





$(function () {
	$('#feature-tree')
		.on('select_node.jstree', function (e, data) {

			// console.log("selected!: " + data.selected + " " + data.node.text);

			var id = data.node.id

			var detailJSON = detail[id];
			if (detailJSON ) {

				var detailhtml = "<p>" + detailJSON.result +"</p>"

				if (detailJSON.filename.length >0){
					detailhtml += "<p>File: " +  detailJSON.filename + "</p>";
				}

				detailhtml += "<p>" + detailJSON.nodetype + ": " + detailJSON.description + "</p>";

				if (detailJSON.method > 0){
					detailhtml = detailhtml + "<p>Method: " + detailJSON.method + "</p>";
				}

				if(detailJSON.screenshot) {
					detailhtml = detailhtml + "<p><a href='" + detailJSON.screenshot + "'><img style='border: 2px solid red;' width='400px;' src='" + detailJSON.screenshot + "' alt='screenshot of failure' /></a>";
				}

				if (detailJSON.emessage != null && detailJSON.emessage.length > 0){
					detailhtml = detailhtml + "<p>" + detailJSON.emessage + "</p><div class=\"stacktrace\"><pre class=\"stacktracepre\"><ul style=\"list-style-type: none;\">" +
						detailJSON.stacktrace.map(formatStackElement).join('') + "</ul></div></pre>";
				}

				detailhtml = detailhtml + "<p>Duration: " + detailJSON.runningDurationString + "</p>"

				if (detailJSON.children && detailJSON.children.length > 0){

					detailhtml = detailhtml + '<table class="table table-bordered table-condensed"><tbody>';

					for (i=0;i<detailJSON.children.length;i++){

						detailhtml = detailhtml + "<tr";
						if (detailJSON.children[i].result == 'PASSED'){
							detailhtml = detailhtml +' class="success"'
						}
						else if (detailJSON.children[i].result == 'FAILED'){
							detailhtml = detailhtml +' class="error"'
						}

						detailhtml = detailhtml + "><td>" + detailJSON.children[i].description + "</td></tr>";
					}
					detailhtml = detailhtml + "</tbody></table>";
				}

				$("#feature-detail").html(detailhtml);

				// get the offset of where we should be?
				var topOffsetShouldbe = $("#detail-div-container").offset().top;

				// get the offset of the affixed div
				var affixOffset = $("#affix-marker").offset().top;

				// so the absolute position position, relative to the parent should be affixOffset - topOffsetShouldbe
				var absPosition = affixOffset - topOffsetShouldbe;

				if (absPosition < 0){
					absPosition = 0;
				}

				$("#feature-detail").css("top", absPosition + 'px');

			}


		})
		.jstree();
});

