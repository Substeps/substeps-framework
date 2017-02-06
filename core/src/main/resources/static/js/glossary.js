$(document).ready(function() {

  var glossaryTable = $('#glossary-table').DataTable({
    paging: false,
    searching: true,
    "data": glossary,
    "columns": [
      {data: 'section', title: "Section"},
      {data: 'expression', title: "Expression"},
      {data: 'description', title: "Description"},
      {data: 'example', title: "Example"}
    ],
    "dom" : "<'row-fluid'<'col-sm-6'l><'col-sm-6'f>>" +
    "<'row-fluid'<'col-sm-12'tr>>" +
    "<'row-fluid'<'col-sm-5'i><'col-sm-7'p>>"
    // ,
    // sDom: '<"search-box"r><"H"lf>t<"F"ip>'
  });

$('#glossarySearchField').keyup(function(){
      glossaryTable.search($(this).val()).draw() ;
})




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



