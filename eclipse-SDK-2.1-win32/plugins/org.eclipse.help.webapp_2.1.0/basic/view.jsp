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
<%@ page import="org.eclipse.help.internal.webapp.data.*"%>

<% 
	LayoutData data = new LayoutData(application,request);
	View view = data.getCurrentView();
	if (view == null) return;
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=ServletResources.getString(view.getName(), request)%></title>
</head>

<frameset  rows="30,*" >
	<frame name="ToolbarFrame" src='<%=view.getURL()+view.getName()+"Toolbar.jsp"%>' frameborder="no" marginwidth="5" marginheight="3" scrolling="no">
	<frame name='ViewFrame' src='<%=view.getURL()+view.getName()+"View.jsp?"+request.getQueryString()%>#selectedItem' frameborder="no" marginwidth="5" marginheight="5">
</frameset>

</html>

