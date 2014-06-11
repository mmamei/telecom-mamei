<%@page import="dataset.EventFilesFinderI"%>
<html>
<head>
<%@include file="includes/head.html" %>
</head>
<body class="no-sidebar">
<%@include file="includes/header.html"%>
<!-- Main -->
<div class="wrapper style1">
<div class="container">
	<article id="main" class="special">
		<header>
		<h2>ECheck Event Availability</h2>
		</header>
		<h3>Request:</h3>
		<%
		String sd = request.getParameter("sd");
		String st = request.getParameter("st");
		String ed = request.getParameter("ed");
		String et = request.getParameter("et");
		double lat1 = Double.parseDouble(request.getParameter("lat1"));
		double lon1 = Double.parseDouble(request.getParameter("lon1"));
		double lat2 = Double.parseDouble(request.getParameter("lat2"));
		double lon2 = Double.parseDouble(request.getParameter("lon2"));
		out.println("Event Start: "+sd+" at "+st+"<br>");
		out.println("Event End: "+ed+" at "+et+"<br>");
		out.println("Event Bbox: ("+lat1+","+lon1+") ("+lat2+","+lon2+")<br>");
		EventFilesFinderI eff = dataset.DataFactory.getEventFilesFinder();
		String dir = eff.find(sd,st,ed,et,lon1,lat1,lon2,lat2);
		out.println("<h3>Response:</h3>");
		if(dir!=null) out.println("The event is covered in <b>"+dir+"</b>");
		else out.println("The event is not covererd by our data");
		%>
		
	</article>
</div>
</div>

</body>
</html>