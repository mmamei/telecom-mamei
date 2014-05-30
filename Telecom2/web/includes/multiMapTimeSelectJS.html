<script src="longRun.js"></script>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization"></script>
<script type="text/javascript">
var map;


var rectangle = new Array();
var ccont = 0; // number of calendar / rectanbkes being added

var colors=new Array("#dd0000", "#00dd00","#0000dd","#dddd00","#00dddd","#dd00dd",
					 "#550000", "#005500","#000055","#555500","#005555","#550055");

var geocoder;
var cp = new google.maps.LatLng(43.77277998225912, 11.249104393861822);
function initialize() {
	map = new google.maps.Map(document.getElementById('map-canvas'), {
	center: cp,
	zoom: 8,
	mapTypeId: google.maps.MapTypeId.NORMAL
	});
	
	var input = document.getElementById('search_address');
	map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);
	geocoder = new google.maps.Geocoder();
    	
	google.maps.event.addListener(map, 'click', function(event) {
		var lat = event.latLng.lat();
		var lng = event.latLng.lng();
		drawRectangle(lat,lng);	
	});
}
google.maps.event.addDomListener(window, 'load', initialize);


function showGenTimeFrame() {
	var x = document.getElementById("timeframe");
	x.style.display="block";
}


function drawRectangle(lat,lng) {

	var cals = document.getElementById("calendars");
	
	var newcal = document.createElement("div");
	newcal.setAttribute("id","cal"+ccont);
	newcal.innerHTML = 	"<input class='button' style='margin-right:10; padding: 0em 1em 0em 1em; background-color:"+colors[ccont]+"' type='button' value='X' onclick='removeArea("+ccont+")'>"+
						"Start Date <input type='date' id='start_day"+ccont+"'>"+
						"Start Time <input style='margin-right:10; padding-left:10; width:100; color:gray' type='text' id='start_time"+ccont+"' value='hh:mm'>"+
						"End Date <input type='date' id='end_day"+ccont+"'>"+
						"End Time <input style='margin-right:10; padding-left:10; width:100; color:gray' type='text' id='end_time"+ccont+"' value='hh:mm'>"+
						"Weekly Repeat <input onclick='showGenTimeFrame();' type='checkbox' style='width:2em;height:2em;vertical-align:middle'name='wr' value='wr' id='wr"+ccont+"'>";
	cals.appendChild(newcal);
	
	
	rectangle[ccont] = new google.maps.Rectangle({
		strokeColor: colors[ccont],
	    strokeOpacity: 0.8,
	    strokeWeight: 2,
	    fillColor: colors[ccont],
	    fillOpacity: 0.35,
	    bounds: new google.maps.LatLngBounds(new google.maps.LatLng(lat-0.001, lng-0.001),new google.maps.LatLng(lat+0.001, lng+0.001)),
		map: map,
		editable: true
	});
	
	
	ccont ++;
}


function removeArea(id) {
	var x = document.getElementById("cal"+id);
	x.remove();
	rectangle[id].setMap(null);
}


// This function geo-codes the address entered in the search bar. 
// Then in centers the map to that location
// Finally it draws a rectangle to select the event in that area
function go2Address() {
	var addressField = document.getElementById('search_address');
	geocoder.geocode(
        {'address': addressField.value}, 
        function(results, status) { 
            if (status == google.maps.GeocoderStatus.OK) { 
                var loc = results[0].geometry.location;
                map.setCenter(loc);
				drawRectangle(loc.lat(),loc.lng());	
            } 
            else {
                alert("Not found: " + status); 
            } 
        }
    );
}

var jspLocation;
var longRun = false;

function process() {
	
	var params = new Array();
	
	var gs = document.getElementById("general_start").value;
	var ge = document.getElementById("general_end").value;
	
	params.push(gs+";");
	params.push(ge+";");
	
	for(var i=0; i<ccont;i++) {
		var c = document.getElementById("cal"+i);
		if(c != null) {
			var sd = document.getElementById("start_day"+i).value;
			var st = document.getElementById("start_time"+i).value;
			var ed = document.getElementById("end_day"+i).value;
			var et = document.getElementById("end_time"+i).value;
			var wr = document.getElementById("wr"+i).checked;
			var b = rectangle[i].getBounds();
			params.push(sd+";"+st+";"+ed+";"+et+";"+wr+";"+b+";"); // add
		} 
	}
	
	
	var url = jspLocation+"?q="+params.join('');
	
	if(url.length > 2000) alert("Very long url, may have problems with http get");
	
	if(longRun) longRunF(url);
	else window.open(url);
}
</script>