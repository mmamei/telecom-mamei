<html>
<head>
<%@include file="includes/head.html" %>
<%@include file="includes/mapTimeSelect.js" %>

<script type="text/javascript">
jspLocation = "user_places_res.jsp";
longRun = true;


function process2() {
	var sd = document.getElementById("start_day").value;
	var ed = document.getElementById("end_day").value;
	var user = document.getElementById("user").value;
	var p1 = marker1.getPosition();
	var p2 = marker2.getPosition();
	var url = jspLocation+"?sd="+sd+"&ed="+ed+"&user="+user+
			  "&lat1="+p1.lat().toFixed(4)+"&lon1="+p1.lng().toFixed(4)+
			  "&lat2="+p2.lat().toFixed(4)+"&lon2="+p2.lng().toFixed(4);
	if(longRun) longRunF(url)
	else window.open(url);
}
</script>

</head>
<body class="no-sidebar">
<%@include file="includes/header.html" %>
<div class="wrapper style1">
<div class="container">
<h2>Find User Places</h2>

Start Date <input type="date" id="start_day">
End Date <input type="date" id="end_day">
User Hash <input type="text" size="60" id="user">
<input class="button" style="padding: 0em 1em 0em 1em" type="button" value="Go!" onclick="process2()">


<input style="margin-top: 25px; padding: 0 11px 0 13px; width: 400px; font-family: Roboto; font-size: 15px; font-weight: 300; text-overflow: ellipsis;" id="search_address" type="text" onChange="go2Address()">
<div style="height: 600px" id="map-canvas"></div>


</div>
</div>
</body>
</html>
