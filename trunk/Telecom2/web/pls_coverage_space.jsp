<html>
<head>
<%@include file="includes/head.html" %>
<%@ page import="java.util.Map" %>
<%@ page import="region.RegionMap" %>
<jsp:useBean id="apbbox" scope="application" class="pls_parser.AnalyzePLSCoverageSpaceDB"/>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization"></script>
<script>

<% 
Map<String,RegionMap> map = apbbox.getPlsCoverage();
out.println(apbbox.getJSMap(map)); 
%>
  
function initialize() {
  // Create the map.
  var mapOptions = {
    zoom: 4,
    center: new google.maps.LatLng(<%=apbbox.getJSMapCenterLatLng(map)%>),
    mapTypeId: google.maps.MapTypeId.TERRAIN
  };

  var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
  
  for(var i=0; i<heatmaps.length;i++)
	  heatmaps[i].setMap(map);
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
	<div style="height: 600px" id="map-canvas"></div>
</div>
</div>

</body>
</html>