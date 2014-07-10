<html>
<head>
<%@include file="includes/head.html" %>
<%@ page import="java.util.Map" %>
<%@ page import="region.RegionMap" %>
<jsp:useBean id="pdp" scope="application" class="analysis.densityANDflows.density.PopulationDensityPlaces"/>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization"></script>
<script>


<% 
String placesFile= request.getParameter("places_file");
String regionMap= request.getParameter("region_map");
String kop= request.getParameter("kop");
String exclude_kop= request.getParameter("exclude_kop");

if(exclude_kop.equals("null")) exclude_kop = null;

String constraints= request.getParameter("constraints");

double lat1 = Double.parseDouble(request.getParameter("lat1"));
double lon1 = Double.parseDouble(request.getParameter("lon1"));
double lat2 = Double.parseDouble(request.getParameter("lat2"));
double lon2 = Double.parseDouble(request.getParameter("lon2"));

String jsData = pdp.runAll(placesFile,regionMap,kop,exclude_kop,constraints,lat1,lon1,lat2,lon2);
out.println(jsData);


String kml_file = (regionMap.startsWith("grid") ? "grid" : regionMap.substring("FIX_".length(), regionMap.lastIndexOf(".")))+"-"+kop+"-"+exclude_kop;

%>
var map;
var cp = new google.maps.LatLng(<%=(lat1+lat2)/2+","+(lon1+lon2)/2%>);
function initialize() {
map = new google.maps.Map(document.getElementById('map-canvas'), {
center: cp,
zoom: 9,
mapTypeId: google.maps.MapTypeId.NORMAL
});
var heatmap = new google.maps.visualization.HeatmapLayer({
data: heatMapData,
radius: 100.0,
opacity: 0.6
});
heatmap.setMap(map);
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
		<h2>Population Density Places</h2>
		</header>
		
		<h3>Request:</h3>
		<%
		out.println("Places File: "+placesFile+"<br>");
		out.println("Kind Of Place: "+kop+", Exclude: "+exclude_kop+"<br>");
		out.println("Area: ("+lat1+","+lon1+") ("+lat2+","+lon2+")<br>");	
		out.println("Region Map: "+regionMap+"<br>");
		if(constraints.contains("="))
			out.println("Constraints: "+constraints+"<br>");
		if(jsData == null) {
			out.println("<h3>Response:</h3>");
			out.println("The requested area/time is outside the PLS coverage");
		}
		%>
		
	</article>
	<div style="height: 600px" id="map-canvas"></div>
	<a href="kml/<%=kml_file%>.kml">Download the KML file!</a>
</div>
</div>

</body>
</html>