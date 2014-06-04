<html>
<head>
<jsp:useBean id="pbia" scope="application" class="analysis.PLSTimeDensity"/>
<%@include file="includes/head.html" %>


<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">

// Load the Visualization API and the piechart package.
google.load('visualization', '1.0', {'packages':['corechart']});

// Set a callback to run when the Google Visualization API is loaded.
google.setOnLoadCallback(drawChart);

// Callback that creates and populates a data table,
// instantiates the pie chart, passes in the data and
// draws it.
function drawChart() {

	// Create the data table.
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
	Object[] plsdata = pbia.process(sd,st,ed,et,lon1,lat1,lon2,lat2,constraints);
	if(plsdata != null)
		out.println(pbia.getJSMap(plsdata));
	%>

    // Set chart options
    var options = {'title':'PLS Behavior',
                    'width':800,
                    'height':500,
					'legend':{position: 'none'}
				};

    // Instantiate and draw our chart, passing in some options.
    var chart = new google.visualization.AreaChart(document.getElementById('chart_div_res'));
    chart.draw(data, options);
}
</script>


</head>
<body class="no-sidebar">
<%@include file="includes/header.html"%>
<!-- Main -->
<div class="wrapper style1">
<div class="container">
	<article id="main" class="special">
		<header>
		<h2>PLS Behavior In An Area</h2>
		</header>
		<h3>Request:</h3>
		<%
		out.println("Start Period: "+sd+" at "+st+"<br>");
		out.println("End Period: "+ed+" at "+et+"<br>");
		out.println("Area: ("+lat1+","+lon1+") ("+lat2+","+lon2+")<br>");	
		if(constraints.contains("="))
			out.println("Constraints: "+constraints+"<br>");
		if(plsdata == null) {
			out.println("<h3>Response:</h3>");
			out.println("The requested area/time is outside the PLS coverage");
		}
		%>
	
	<div style="width:800px; height: 600px; margin-left:auto; margin-right:auto;" id="chart_div_res"></div>
	</article>
</div>
</div>

</body>
</html>