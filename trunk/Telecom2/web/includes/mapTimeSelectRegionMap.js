<script src="longRun.js"></script>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization"></script>
<script type="text/javascript">
var map;
var marker1;
var marker2;
var rectangle;
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
	
	// init the two calendars to today
	document.getElementById('start_day').value = new Date();
	document.getElementById('end_day').value = new Date();
}
google.maps.event.addDomListener(window, 'load', initialize);


function drawRectangle(lat,lng) {
	//alert(lat+","+lng);
	if(rectangle !=null) {
		marker1.setMap(null);
		marker2.setMap(null);
		rectangle.setMap(null)
	}
	// alert("Lat=" + lat + "; Lng=" + lng);
	// Plot two markers to represent the Rectangle's bounds.
	marker1 = new google.maps.Marker({
		map: map,
		position: new google.maps.LatLng(lat, lng),
		icon: {path: google.maps.SymbolPath.CIRCLE,scale: 3},
		draggable: true,
		title: 'Drag me!'
	});
	marker2 = new google.maps.Marker({
		map: map,
		position: new google.maps.LatLng(lat, lng),
		icon: {path: google.maps.SymbolPath.CIRCLE,scale: 3},
		draggable: true,
		title: 'Drag me!'
	});
	// Allow user to drag each marker to resize the size of the Rectangle.
	google.maps.event.addListener(marker1, 'drag', redraw);
	google.maps.event.addListener(marker2, 'drag', redraw);
       
	// Create a new Rectangle overlay and place it on the map.  Size
	// will be determined by the LatLngBounds based on the two Marker
	// positions.
	rectangle = new google.maps.Rectangle({
		map: map
	});
	redraw();
}

function redraw() {
    var latLngBounds = new google.maps.LatLngBounds(
		marker1.getPosition(),
        marker2.getPosition()
    );
    rectangle.setBounds(latLngBounds);
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
	var sd = document.getElementById("start_day").value;
	var st = document.getElementById("start_time").value;
	var ed = document.getElementById("end_day").value;
	var et = document.getElementById("end_time").value;
	var rm = document.getElementById("region_map").value;
	var ueps = document.getElementById("users_event_probscores").value;

	if(!marker1) {
		alert("Select the target area!");
		return;
	}
	
	var constraints = "";
	var ao = document.getElementById("adv_op");
	if(ao.style.display=="block") {
		constraints = document.getElementById("constraints").value;
		constraints = constraints+";users_event_probscores="+ueps;
	}
	
	var p1 = marker1.getPosition();
	var p2 = marker2.getPosition();
	
	var url = jspLocation+"?sd="+sd+"&st="+st+"&ed="+ed+"&et="+et+
			  "&lat1="+p1.lat().toFixed(4)+"&lon1="+p1.lng().toFixed(4)+
			  "&lat2="+p2.lat().toFixed(4)+"&lon2="+p2.lng().toFixed(4)+
			  "&region_map="+rm+"&constraints="+constraints;
	if(longRun) longRunF(url)
	else window.open(url);
}
</script>