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
		<div class="section">
			<h2>PubSubHubbub prenumeranter</h2>
			
			
			<table>
				<tr>
					<th>Topic</th>
					<th>Callback</th>
					<th></th>
				</tr>
				<c:forEach var="pushSubscriber" items="${pushSubscribers}">
					<tr>
						<td>${pushSubscriber.topic}</td>
						<td>${pushSubscriber.callback}</td>
						<td><a href="${pageContext.request.contextPath}/push/admin/push/${pushSubscriber.id}/edit">Ändra</a></td>
					</tr>
				</c:forEach>
			</table>
			<div><a href="${pageContext.request.contextPath}/push/admin/push/new"><img src="${pageContext.request.contextPath}/push/resources/img/page_add.png" /> Skapa ny prenumerant</a></div>
		</div>
		<div class="section">
			<h2>Pollande publicerare</h2>
			
			<table>
				<tr>
					<th>URL</th>
					<th></th>
				</tr>
				<c:forEach var="polledPublisher" items="${polledPublishers}">
					<tr>
						<td>${polledPublisher.url}</td>
						<td><a href="${pageContext.request.contextPath}/push/admin/polled/${polledPublisher.id}/edit">Ändra</a></td>
					</tr>
				</c:forEach>
			</table>
			<div><a href="${pageContext.request.contextPath}/push/admin/polled/new"><img src="${pageContext.request.contextPath}/push/resources/img/page_add.png" /> Skapa ny publicerare</a></div>
		</div>
	</body>
</html>
