<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Upload Result</title>
<%
    String json = request.getAttribute("jsonLDContext");
    String id = request.getAttribute("id");
%>
</head>
<body>
    <h3>URL of the JSON-LD Context:</h3>
    <p><%= id %></p>

    <h3>JSON-LD Context text</h3>
    <p><%= json %></p>

</body>
</html>