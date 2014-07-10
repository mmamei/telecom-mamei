<html>
<head>
<%@include file="includes/head.html" %>
<%@ page import="java.util.Map" %>
<%@ page import="region.RegionMap" %>
<jsp:useBean id="odhw" scope="application" class="analysis.densityANDflows.flows.ODMatrixHW"/>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization"></script>
<script>

<% 


String placesFile= request.getParameter("places_file");
String regionMap= request.getParameter("region_map");

String constraints= request.getParameter("constraints");

double lat1 = Double.parseDouble(request.getParameter("lat1"));
double lon1 = Double.parseDouble(request.getParameter("lon1"));
double lat2 = Double.parseDouble(request.getParameter("lat2"));
double lon2 = Double.parseDouble(request.getParameter("lon2"));

String jsData = odhw.runAll(placesFile,regionMap,constraints,lat1,lon1,lat2,lon2);
out.println(jsData);


String kml_file = "tmp_"+ (regionMap.startsWith("grid") ? "grid" : regionMap.substring("FIX_".length(), regionMap.lastIndexOf(".")));

%>
var map;
var cp = new google.maps.LatLng(<%=(lat1+lat2)/2+","+(lon1+lon2)/2%>);
function initialize() {
map = new google.maps.Map(document.getElementById('map-canvas'), {
center: cp,
zoom: 11,
mapTypeId: google.maps.MapTypeId.NORMAL
});

var lineSymbol = {path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW};


<%=jsData%>

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
		<h2>Population Flow (OD Matrix) Home-Work Commute</h2>
		</header>
		
		<h3>Request:</h3>
		<%
		out.println("Places File: "+placesFile+"<br>");
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
	<a href="kml/od_tmp.kml">Download the KML file!</a>
</div>
</div>

</body>
</html>