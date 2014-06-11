<html>


<%

String dataset = request.getParameter("dataset");
if(dataset !=null) {
	utils.Config conf = utils.Config.getInstance();
	conf.changeDataset(dataset);
}

%>

<head>
<%@include file="includes/head.html" %>
</head>
<body class="no-sidebar">
<%@include file="includes/header.html" %>
<!-- Main -->
<div class="wrapper style1">
<div class="container">
	<article id="main" class="special">
		
		
		<% if(dataset == null) {%>
		<header>
		<h2>Change Dataset</h2>
		</header>
		<header>
		<form>
		<select name="dataset">
		<option value="italy">Italy</option>
		<option value="ivory-set2">Ivory Coast - SET2</option>
		<option value="ivory-set3">Ivory Coast - SET3</option>
		</select>
		<br>
		<input style="padding: 0em 2em 0em 2em" class="button" type="submit" value="Change!">
		</form>
		
		<% } else {%>
		<header>
		<h2>Dataset Changed</h2>
		</header>
		<header>
		<h2 style="color:#a00"><%= dataset %></h2>
		
		<% }%>
		
		</header>
		
	</article>
</div>
</div>

<%@include file="includes/footer.html" %>
</body>
</html>