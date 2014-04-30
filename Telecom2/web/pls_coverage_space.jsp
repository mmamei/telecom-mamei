<html>
<head>
<%@include file="includes/head.html" %>
<%@ page import="java.util.Map" %>
<%@ page import="region.RegionMap" %>
<jsp:useBean id="apbbox" scope="application" class="pls_parser.AnalyzePLSCoverageSpace"/>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>
<script>
// This example creates circles on the map, representing
// populations in the United States.

// First, create an object containing LatLng and population for each region map.

<% 
Map<String,RegionMap> map = apbbox.getPlsCoverage();
out.println(apbbox.getJSMap(map)); 
%>
  
var cityCircle;

function initialize() {
  // Create the map.
  var mapOptions = {
    zoom: 4,
    center: new google.maps.LatLng(<%=apbbox.getJSMapCenterLatLng(map)%>),
    mapTypeId: google.maps.MapTypeId.TERRAIN
  };

  var map = new google.maps.Map(document.getElementById('map-canvas'),
      mapOptions);

	  
  
  var colors = ['#ff0000','#00ff00','#0000ff','#ffff00','#00ffff','#ff00ff'];
  
  // Construct the circle for each value in citymap.
  var color_index = 0;
  for (var city in citymap) {
	for(var i=0; i<citymap[city].length;i++) {
		var opt = {
		  strokeColor: colors[color_index],
		  strokeOpacity: 0.8,
		  strokeWeight: 2,
		  fillColor: '#FF0000',
		  fillOpacity: 0.35,
		  map: map,
		  center: citymap[city][i].center,
		  radius: citymap[city][i].radius
		};
		// Add the circle for this city to the map.
		cityCircle = new google.maps.Circle(opt);
	  }
	color_index++;
  }
}

google.maps.event.addDomListener(window, 'load', initialize);
</script>


</head>
<body class="no-sidebar">
<%@include file="includes/header.html" %>
<!-- Main -->
<div class="wrapper style1">
<div class="container">
	<article id="main" class="special">
		<header>
		<h2>PLS Coverage in Space</h2>
		</header>
	</article>
	<div style="height: 600px" id="map-canvas"/>
</div>
</div>

</body>
</html>