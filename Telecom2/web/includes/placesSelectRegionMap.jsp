

<script type="text/javascript">
function showAdvOp() {
	var ao = document.getElementById("adv_op");
	if(ao.style.display=="none") ao.style.display="block";
	else ao.style.display="none";
}
</script>


PlacesFile
<select id="places_file">
<%
analysis.user_place_recognizer.PlaceRecognizer pr = new analysis.user_place_recognizer.PlaceRecognizer();
String[] files = pr.getComputedResults();
for(String rm: files) {
	String label = rm.substring("C:/BASE/PlaceRecognizer/".length(),rm.lastIndexOf("\\"));
	out.println("<option value=\""+rm+"\"><span>"+label+"</option>");
}
%>
</select>

Kind Of Places
<select id="kop">
<option value="HOME">Home</option>
<option value="WORK">Work</option>
<option value="SATURDAY_NIGHT">Saturday Night</option>
<option value="SUNDAY">Sunday</option>
</select>


Exclude
<select id="exclude_kop">
<option value="null">null</option>
<option value="HOME">Home</option>
<option value="WORK">Work</option>
<option value="SATURDAY_NIGHT">Saturday Night</option>
<option value="SUNDAY">Sunday</option>
</select>


Region Map
<select id="region_map">
<option value="grid5">grid(5,5)</option>
<option value="grid10">grid(10,10)</option>
<option value="grid20">grid(20,20)</option>
<%
region.RegionMapFactory rmf = new region.RegionMapFactory();
String[] regionMaps = rmf.getAvailableRegions();
for(String rm: regionMaps) {
	String label = rm.substring(4,rm.indexOf("."));
	out.println("<option value=\""+rm+"\"><span>"+label+"</option>");
}
%>
</select>




<input style="padding: 0em 2em 0em 2em" class="button" type="button" value="Go!" onclick="process()">

<span onclick="showAdvOp()" style="color:gray;font-size:70%">Adv. Opts</span>

<div id="adv_op" style="display:none">
Constraints: <input id="constraints" type="text" name="constraints" value="es. mnt=!222;maxdays=4"> 
Weight By Event Attendance
<select id="users_event_probscores">
<option value="null"><span>null</option>
<%
analysis.presence_at_event.ProbScoresFinder psf = new analysis.presence_at_event.ProbScoresFinder();
String[] pscores = psf.getAvailableProbScores();
if(pscores!=null)
for(String ps: pscores) {
	out.println("<option value=\""+ps+"\"><span>"+ps.substring(ps.lastIndexOf("\\")+1,ps.lastIndexOf("."))+"</option>");
}
%>
</select>
</div>

<input style="margin-top: 25px; padding: 0 11px 0 13px; width: 400px; font-family: Roboto; font-size: 15px; font-weight: 300; text-overflow: ellipsis;" id="search_address" type="text" onChange="go2Address()">
<div style="height: 600px" id="map-canvas"></div>