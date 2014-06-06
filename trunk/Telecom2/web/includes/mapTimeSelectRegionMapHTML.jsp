

<script type="text/javascript">
function showAdvOp() {
	var ao = document.getElementById("adv_op");
	if(ao.style.display=="none") ao.style.display="block";
	else ao.style.display="none";
}
</script>

Start Date <input type="date" id="start_day">
Start Time
<select id="start_time">
<option value="0">00:00</option><option value="1">01:00</option><option value="2">02:00</option> <option value="3">03:00</option>
<option value="4">04:00</option><option value="5">05:00</option><option value="6">06:00</option> <option value="7">07:00</option>
<option value="8">08:00</option><option value="9">09:00</option><option value="10">10:00</option> <option value="11">11:00</option>
<option value="12">12:00</option><option value="13">13:00</option><option value="14">14:00</option> <option value="15">15:00</option>
<option value="16">16:00</option><option value="17">17:00</option><option value="18">18:00</option> <option value="19">19:00</option>
<option value="20">20:00</option><option value="21">21:00</option><option value="22">22:00</option> <option value="23">23:00</option>
</select>

End Date <input type="date" id="end_day">
End Time
<select id="end_time">
<option value="0">00:00</option><option value="1">01:00</option><option value="2">02:00</option> <option value="3">03:00</option>
<option value="4">04:00</option><option value="5">05:00</option><option value="6">06:00</option> <option value="7">07:00</option>
<option value="8">08:00</option><option value="9">09:00</option><option value="10">10:00</option> <option value="11">11:00</option>
<option value="12">12:00</option><option value="13">13:00</option><option value="14">14:00</option> <option value="15">15:00</option>
<option value="16">16:00</option><option value="17">17:00</option><option value="18">18:00</option> <option value="19">19:00</option>
<option value="20">20:00</option><option value="21">21:00</option><option value="22">22:00</option> <option value="23">23:00</option>
</select>

Region Map
<select id="region_map">
<option value="grid5"><span>grid(5,5)</option>
<option value="grid10"><span>grid(10,10)</option>
<option value="grid20"><span>grid(20,20)</option>
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
Constraints: <input id="constraints" type="text" name="constraints" size="100" value="es. mnt=!22201;maxdays=4"> 
</div>

<input style="margin-top: 25px; padding: 0 11px 0 13px; width: 400px; font-family: Roboto; font-size: 15px; font-weight: 300; text-overflow: ellipsis;" id="search_address" type="text" onChange="go2Address()">
<div style="height: 600px" id="map-canvas"></div>