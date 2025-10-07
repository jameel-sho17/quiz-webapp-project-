<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>QuizMaster-Join Quiz</title>
    <link rel="stylesheet" type="text/css" href="css/login.css" />
</head>

<body>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="home-hero">
    <h1>Join a Quiz</h1>
    <p>Enter the quiz code provided by the administrator and your name to participate.</p>

    <c:if test="${not empty error}"><div class="error" style="color:#ef4444; margin-bottom:8px; font-weight:600;">${error}</div></c:if>
        <form action="start-quiz" method="post" class="join-form">
        <div class="join-group">
            <label for="pin">Quiz Code</label>
            <input type="text" id="pin" name="pin" placeholder="Enter Quiz Code" required />
        </div>
        <div class="join-group">
            <label for="name">Full Name</label>
            <input type="text" id="name" name="name" placeholder="Enter Full Name" required />
        </div>
        <button type="submit" class="btn-primary">Join Quiz</button>
    </form>
</div>


</body>
</html>