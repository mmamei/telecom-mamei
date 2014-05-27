<html>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<head>
<%@include file="includes/head.html" %>




<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">
	google.load("visualization", "1.1", {packages:["calendar"]});
    google.setOnLoadCallback(drawChart);

	function drawCalendar(dataTable,calId,calTitle,height) {
		var chart = new google.visualization.Calendar(document.getElementById(calId));
		var options = {
			title: calTitle,
			height: height,
		};
       chart.draw(dataTable, options);
	}
	  
	function drawChart() {
	<%
	dataset.PLSCoverageTimeI pct = dataset.DataFactory.getPLSCoverageTime();
	Map<String,List<String>> all =  pct.computeAll();
	out.println(pct.getJSMap(all));
	
	for(String key: all.keySet()) {
		int y = pct.getNumYears(all.get(key));
		int h = 20 + y * 150;
		out.println("drawCalendar(dataTable_"+key+",'calendar_"+key+"','"+key+"',"+h+");");
	}
	%>
	}
</script>

</head>
<body class="no-sidebar">
<%@include file="includes/header.html" %>
<!-- Main -->
<div class="wrapper style1">
<div class="container">
	<article id="main" class="special">
		<header>
		<h2>PLS Coverage</h2>
		</header>
		
		<%
		for(String key: all.keySet()) {
			int y = pct.getNumYears(all.get(key));
			int h = 20 + y * 150;
			out.println("<div id='calendar_"+key+"' style='width: 1000px; height: "+h+"px;'></div><hr>");
		}
		%>
	</article>
</div>
</div>

</body>
</html>