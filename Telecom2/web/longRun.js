
function longRunF(page) {
	$.get('waiting.html',function(response){
		$('.container').replaceWith(response);
	});
	$.get(page,function(response){
		var newDoc = document.open("text/html", "replace");
		newDoc.write(response);
		newDoc.close();
	}).fail(function() {
	    alert("Error");
	});
}