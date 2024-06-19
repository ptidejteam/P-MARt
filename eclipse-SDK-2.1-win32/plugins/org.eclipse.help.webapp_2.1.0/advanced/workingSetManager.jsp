<%--
 Copyright (c) 2000, 2003 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>

<% 
	WorkingSetManagerData data = new WorkingSetManagerData(application, request);
	WebappPreferences prefs = data.getPrefs();
%>


<html>
<head>
<title><%=ServletResources.getString("SelectWorkingSet", request)%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">


<style type="text/css">
<%@ include file="list.css"%>
</style>

<style type="text/css">
BODY {
	background-color: <%=prefs.getToolbarBackground()%>;
}

TABLE {
	width:auto;
}

TD, TR {
	margin:0px;
	padding:0px;
	border:0px;
}

INPUT {
	font:<%=prefs.getViewFont()%>;
}

#workingSetContainer {
	background:Window;
	border: 2px inset ThreeDHighlight;
	margin:0px 5px;
	padding:5px;
	overflow:auto;
}

</style>

<script language="JavaScript" src="list.js"></script>
<script language="JavaScript">

function highlightHandler()
{
	document.getElementById('selectws').checked = true;
	enableButtons();
}

// register handler
_highlightHandler = highlightHandler;

function onloadHandler() {
	sizeButtons();
	enableButtons();
	document.getElementById("alldocs").focus();
}

function sizeButtons() {
	var minWidth=60;

	if(document.getElementById("ok").offsetWidth < minWidth){
		document.getElementById("ok").style.width = minWidth+"px";
	}
	if(document.getElementById("cancel").offsetWidth < minWidth){
		document.getElementById("cancel").style.width = minWidth+"px";
	}
	if(document.getElementById("edit").offsetWidth < minWidth){
		document.getElementById("edit").style.width = minWidth+"px";
	}
	if(document.getElementById("remove").offsetWidth < minWidth){
		document.getElementById("remove").style.width = minWidth+"px";
	}
	if(document.getElementById("new").offsetWidth < minWidth){
		document.getElementById("new").style.width = minWidth+"px";
	}
}

function enableButtons() {
	if (document.getElementById('selectws').checked){
		document.getElementById("edit").disabled = (active == null);
		document.getElementById("remove").disabled = (active == null);
		document.getElementById("ok").disabled = (active == null);	
	} else {
		document.getElementById("edit").disabled = true;
		document.getElementById("remove").disabled = true;
		document.getElementById("ok").disabled = false;
	}
}

function getWorkingSet()
{
	if (active != null && document.getElementById("selectws").checked)
		return active.title;
	else
		return "";
}


function selectWorkingSet() {
	var workingSet = getWorkingSet();

	var search = window.opener.location.search;
	if (search && search.length > 0) {
		var i = search.indexOf("workingSet=");
		if (i >= 0)
			search = search.substring(0, i);
		else
			search += "&";
	} else {
		search = "?";
	}

	search += "workingSet=" + escape(workingSet)+"&encoding=js";
	var searchWord = window.opener.document.forms["searchForm"].searchWord.value;
	if (searchWord)
		search += "&searchWord="+escape(searchWord);
		
	window.opener.location.replace(
		window.opener.location.protocol +
		"//" +
		window.opener.location.host + 
		window.opener.location.pathname +
		search);

 	window.close();
}

function removeWorkingSet() {
	window.location.replace("workingSetManager.jsp?operation=remove&workingSet="+escape(getWorkingSet())+"&encoding=js");
}

var workingSetDialog;
var w = 300;
var h = 500;

function newWorkingSet() { 	
	<%
	if (data.isIE()){
	%>
		var l = top.screenLeft + (top.document.body.clientWidth - w) / 2;
		var t = top.screenTop + (top.document.body.clientHeight - h) / 2;
	<%
	} else {
	%>
		var l = top.screenX + (top.innerWidth - w) / 2;
		var t = top.screenY + (top.innerHeight - h) / 2;
	<%
	}
	%>
	// move the dialog just a bit higher than the middle
	if (t-50 > 0) t = t-50;
		
	workingSetDialog = window.open("workingSet.jsp?operation=add&workingSet="+escape(getWorkingSet())+"&encoding=js", "workingSetDialog", "resizeable=no,height="+h+",width="+w +",left="+l+",top="+t);
	workingSetDialog.focus(); 
}

function editWorkingSet() {
	 	
	<%
	if (data.isIE()){
	%>
		var l = top.screenLeft + (top.document.body.clientWidth - w) / 2;
		var t = top.screenTop + (top.document.body.clientHeight - h) / 2;
	<%
	} else {
	%>
		var l = top.screenX + (top.innerWidth - w) / 2;
		var t = top.screenY + (top.innerHeight - h) / 2;
	<%
	}
	%>
	// move the dialog just a bit higher than the middle
	if (t-50 > 0) t = t-50;
		
	workingSetDialog = window.open("workingSet.jsp?operation=edit&workingSet="+escape(getWorkingSet())+"&encoding=js", "workingSetDialog", "resizeable=no,height="+h+",width="+w+",left="+l+",top="+t );
	workingSetDialog.focus(); 
}

function closeWorkingSetDialog()
{
	try {
		if (workingSetDialog)
			workingSetDialog.close();
	}
	catch(e) {}
}

</script>

</head>

<body onload="onloadHandler()" onunload="closeWorkingSetDialog()">
<form>
<div style="overflow:auto;height:250px;width:100%;">
  	<table id="filterTable" cellspacing=0 cellpading=0 border=0 align=center  style="background:<%=prefs.getToolbarBackground()%>; font:<%=prefs.getToolbarFont()%>;margin-top:5px;width:100%;">
		<tr><td>
			<input id="alldocs" type="radio" name="workingSet" onclick="enableButtons()"><label for="alldocs"><%=ServletResources.getString("selectAll", request)%></label>
		</td></tr>
		<tr><td>
			<input id="selectws" type="radio" name="workingSet"  onclick="enableButtons()"><label for="selectws"><%=ServletResources.getString("selectWorkingSet", request)%>:</label>	
		</td></tr>
		<tr><td>
			<div id="workingSetContainer" style="overflow:auto; height:150px; background:<%=prefs.getViewBackground()%>;">

<table id='list'  cellspacing='0' style="width:100%;">
<% 
String[] wsets = data.getWorkingSets();
String workingSetId = "";
for (int i=0; i<wsets.length; i++)
{
	if (data.isCurrentWorkingSet(i))
		workingSetId = "a" + i;
%>
<tr class='list' id='r<%=i%>' style="width:100%;">
	<td align='left' class='label' nowrap style="width:100%; padding-left:5px;">
		<a id='a<%=i%>' 
		   href='#' 
		   onclick="active=this;highlightHandler()"
   		   ondblclick="selectWorkingSet()"
		   title="<%=wsets[i]%>">
		   <%=wsets[i]%>
		 </a>
	</td>
</tr>

<%
}		
%>

</table>
			</div>
		</td></tr>
		<tr id="actionsTable" valign="bottom"><td>
  			<table cellspacing=10 cellpading=0 border=0 style="background:transparent;">
				<tr>
					<td>
						<input type="button" onclick="newWorkingSet()" value='<%=ServletResources.getString("NewWorkingSetButton", request)%>...'  id="new" alt='<%=ServletResources.getString("NewWorkingSetButton", request)%>'>
					</td>
					<td>
					  	<input type="button" onclick="editWorkingSet()" value='<%=ServletResources.getString("EditWorkingSetButton", request)%>...'  id="edit" disabled='<%=data.getWorkingSet() == null ?"true":"false"%>' alt='<%=ServletResources.getString("EditWorkingSetButton", request)%>'>
					</td>
					<td>
					  	<input type="button" onclick="removeWorkingSet()" value='<%=ServletResources.getString("RemoveWorkingSetButton", request)%>'  id="remove" disabled='<%=data.getWorkingSet() == null ?"true":"false"%>' alt='<%=ServletResources.getString("RemoveWorkingSetButton", request)%>'>
					</td>
				</tr>
  			</table>
		</td></tr>
	</table>
</div>
<div style="height:50px;">
	<table valign="bottom" align="right" style="background:<%=prefs.getToolbarBackground()%>">
		<tr id="buttonsTable" valign="bottom"><td valign="bottom" align="right">
  			<table cellspacing=10 cellpading=0 border=0 style="background:transparent;">
				<tr>
					<td>
						<input type="button" onclick="selectWorkingSet()" value='<%=ServletResources.getString("OK", request)%>' id="ok" alt='<%=ServletResources.getString("OK", request)%>'>
					</td>
					<td>
					  	<input type="button" onclick="window.close()" value='<%=ServletResources.getString("Cancel", request)%>' id="cancel" alt='<%=ServletResources.getString("Cancel", request)%>'>
					</td>
				</tr>
  			</table>
		</td></tr>
	</table>
</div>
</form>
<script language="JavaScript">
	var selected = selectTopicById('<%=workingSetId%>');
	if (!selected)
		document.getElementById("alldocs").checked = true;
	else
		document.getElementById("selectws").checked = true;
		
</script>

</body>
</html>
