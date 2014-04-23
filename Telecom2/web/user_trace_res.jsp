<html>
<head>
<%@include file="includes/head.html" %>
<jsp:useBean id="pu" scope="application" class="analysis.find_user.UserPlotter"/>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>
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
%>


function initialize() {
  // Create the map.
  var mapOptions = {
    zoom: 12,
    center: new google.maps.LatLng(<%=(lat1+lat2)/2%>,<%=(lon1+lon2)/2%>),
    mapTypeId: google.maps.MapTypeId.TERRAIN
  };

  var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
  //var utrace = new google.maps.KmlLayer({url: ''});
  
  var kmlLayer = new google.maps.KmlLayer('https://localhost:8443/pls/kml/feaf164623aa5fcac0512b3b4a62496c34458ac017141a808dfe306b62759f.kml', {
	    map: map
	  });

  
  //utrace.setMap(map);
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
		<h2><%=user%></h2>
		</header>
	</article>
	<div style="height: 600px" id="map-canvas"></div>
</div>
</div>

</body>
</html>