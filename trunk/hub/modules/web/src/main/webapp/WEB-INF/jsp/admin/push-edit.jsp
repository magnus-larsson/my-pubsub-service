<%--

    Copyright 2010 Västra Götalandsregionen

      This library is free software; you can redistribute it and/or modify
      it under the terms of version 2.1 of the GNU Lesser General Public
      License as published by the Free Software Foundation.

      This library is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU Lesser General Public License for more details.

      You should have received a copy of the GNU Lesser General Public
      License along with this library; if not, write to the
      Free Software Foundation, Inc., 59 Temple Place, Suite 330,
      Boston, MA 02111-1307  USA

--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>
	<head>
		<title>Administration - PubSub</title>
		
		<link rel="shortcut icon" href="http://www.vgregion.se/VGRimages/favicon.ico" type="image/x-icon" />
		
		<link rel="stylesheet" href="${pageContext.request.contextPath}/push/resources/css/reset.css" type="text/css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/push/resources/css/typography.css" type="text/css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/push/resources/css/forms.css" type="text/css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/push/resources/css/pubsub.css" type="text/css" />

	</head>
	<body>
		<h2>PubSubHubbub prenumerant</h2>
		
		<form action='' method="post">
			<p><label for="topic">Topic URL</label><input name="topic" value="${subscriber.topic}" /></p>
			<p><label for="callback">Callback URL</label><input name="callback" value="${subscriber.callback}" /></p>
			<p><label for="leaseSeconds">Lease seconds</label><input name="leaseSeconds" value="${subscriber.leaseSeconds}" /></p>
			<p><label for="verifyToken">Verify token</label><input name="verifyToken" value="${subscriber.verifyToken}" /></p>
			<p><label for="secret">Secret</label><input name="secret" value="${subscriber.secret}" /></p>
			<p><label for="active">Aktiv</label><input name="active" type="checkbox" ${subscriber.active ? 'checked="yes"' : ''} /></p>

			<input type="hidden" name="id" value="${subscriber.id}" /></p>
			
			<p><input type='submit' value='Spara'> <c:if test="${not empty subscriber}"><input type='submit' name='delete' value='Ta bort prenumerant'></c:if></p>
		</form>	
	</body>
</html>
