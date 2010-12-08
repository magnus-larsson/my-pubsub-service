<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>
	<head>
		<title>Administration - PubSub</title>
		
		<link rel="shortcut icon" href="http://www.vgregion.se/VGRimages/favicon.ico" type="image/x-icon" />
		
		<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/reset.css" type="text/css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/typography.css" type="text/css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/forms.css" type="text/css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pubsub.css" type="text/css" />

	</head>
	<body>
		<h2>PubSubHubbub prenumerant</h2>
		
		<form action='' method="post">
			<p><label for="topic">Topic URL</label><input name="topic" value="${topic}" /></p>
			<p><label for="callback">Callback URL</label><input name="callback" value="${callback}" /></p>
			<p><label for="leaseSeconds">Lease seconds</label><input name="leaseSeconds" value="${leaseSeconds}" /></p>
			<p><label for="verifyToken">Verify token</label><input name="verifyToken" value="${verifyToken}" /></p>
			
			<p><input type='submit' value='Spara'></p>
		</form>	
	</body>
</html>
