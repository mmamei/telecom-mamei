<html>
<head>
<jsp:useBean id="uf" scope="application" class="analysis.find_user.UserFinder"/>
<%@include file="includes/head.html" %>
</head>
<body class="no-sidebar">
<%@include file="includes/header.html"%>
<!-- Main -->
<div class="wrapper style1">
<div class="container">
	<article id="main" class="special">
		<header>
		<h2>Find User Analysis</h2>
		</header>
		<h3>Request:</h3>
		<%
		String q = request.getParameter("q");
		out.println(q);
		out.println("<h3>Response:</h3>");
		out.println(uf.find(q));
		%>
		
	</article>
</div>
</div>

</body>
</html>