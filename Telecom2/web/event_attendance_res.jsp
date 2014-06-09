<html>
<head>
<jsp:useBean id="ra" scope="application" class="analysis.presence_at_event.RunAll"/>
<%@include file="includes/head.html" %>
</head>
<body class="no-sidebar">
<%@include file="includes/header.html"%>
<!-- Main -->
<div class="wrapper style1">
<div class="container">
	<article id="main" class="special">
		<header>
		<h2>Event Analysis</h2>
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
		String constraints= request.getParameter("constraints");
		out.println("Event Start: "+sd+" at "+st+"<br>");
		out.println("Event End: "+ed+" at "+et+"<br>");
		out.println("Event Bbox: ("+lat1+","+lon1+") ("+lat2+","+lon2+")<br>");
		if(constraints.contains("="))
			out.println("Constraints: "+constraints+"<br>");
		out.println("<h3>Response:</h3>");
		int[] rad_att = ra.radiusAndAttendance(sd,st,ed,et,lon1,lat1,constraints);
		%>
		Best Radius: <span style="margin-left:20px; font-size:24px;"><%=rad_att[0]%></span><br>
		Estimated Attendance: <span style="margin-left:20px; font-size:72px; color:#dd0000"><%=rad_att[1]%></span>
		
	</article>
</div>
</div>

</body>
</html>