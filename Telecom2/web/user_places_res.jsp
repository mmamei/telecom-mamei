<html>
<head>
<%@include file="includes/head.html" %>
<jsp:useBean id="pr" scope="application" class="analysis.user_place_recognizer.PlaceRecognizer"/>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>
<script type="text/javascript" src="js/geoxmlfull_v3.js"></script>
<script>
// This example creates circles on the map, representing
// populations in the United States.

// First, create an object containing LatLng and population for each region map.

<% 
String sd = request.getParameter("sd");
String ed = request.getParameter("ed");
String user = request.getParameter("user");
double lat1 = Double.parseDouble(request.getParameter("lat1"));
double lon1 = Double.parseDouble(request.getParameter("lon1"));
double lat2 = Double.parseDouble(request.getParameter("lat2"));
double lon2 = Double.parseDouble(request.getParameter("lon2"));
pr.runSingle(sd,ed, user, lon1, lat1,  lon2, lat2);
%>


var map;
var cta_layer;
var gxml; 
function initialize() {
//    var kmlUrl = 'http://earth-api-samples.googlecode.com/svn/trunk/examples/static/red.kml';
    var myOptions = {
        mapTypeId: google.maps.MapTypeId.ROADMAP,
		zoom: 8,
		center: new google.maps.LatLng(42,0.7)
		}
    var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	gxml = new GeoXml("gxml", map, "kml/<%=user%>.kml", {
		sidebarid:"the_side_bar",
		quiet:true,
		iconFromDescription:false
	}); 
	gxml.parse();
}

google.maps.event.addDomListener(window, 'load', initialize);
</script>
<link type="text/css" rel="stylesheet" href="css/gmap.css" />
</head>

<body class="no-sidebar">
<%@include file="includes/header.html" %>
<!-- Main -->
<div class="wrapper style1">
<div class="container">
	<article id="main" class="special">
		<header>
		<h2><%=user.substring(0,10)+"..."%></h2>
		</header>
	</article>
	<div id="map-container">
	<div id="the_side_bar" style="font-size:11;float: right;"></div>
	<div style="height: 600px" id="map_canvas"></div>
	</div>
	<a href="kml/<%=user%>.kml">Download the KML file!</a>
</div>
</div>





</body>
</html>