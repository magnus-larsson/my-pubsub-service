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
		<h2>Pollande publicerare</h2>
		
		<form action='' method="post">
			<p><label for="url">URL</label><input name="url" value="${publisher.url}" /></p>
			<input type="hidden" name="id" value="${publisher.id}" /></p>
			
			<p><input type='submit' value='Spara'> <c:if test="${not empty publisher}"><input type='submit' name='delete' value='Ta bort publicerare'></c:if></p>
		</form>	
	</body>
</html>
