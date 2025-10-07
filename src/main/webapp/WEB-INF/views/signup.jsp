<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
  response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
  response.setHeader("Pragma", "no-cache");
  response.setDateHeader("Expires", 0);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>QuizMaster-Sign Up</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body>
    <div class="container">
        <div class="auth-card">

            <script>
                window.addEventListener("load", function () {
                    const alert = document.getElementById("alert");
                    if (alert) {
                        setTimeout(() => alert.style.display = 'none', 5000);
                    }
                });
            </script>

            <div class="header">
                <h1>QuizMaster</h1>
                <p>Join thousands of quiz enthusiasts!</p>
            </div>
            
            <form class="auth-form" action="${pageContext.request.contextPath}/signup" method="POST">
                <div class="form-group">
                    <label for="username">Username</label>
                    <input type="text" id="name" name="name" placeholder="Enter username" required>
                </div>
                
                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" placeholder="Enter password" required>
                </div>
                <div class="form-group">
                    <label for="confirmPassword">Confirm Password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Re-enter password" required>
                </div>
                
                <c:if test="${not empty error}">
                    <div id="alert" class="error-message">${error}</div>
                </c:if>

                <button type="submit" class="btn-primary">Create Account</button>

                
            </form>
            
            <div class="auth-footer">
                <p>Already have an account? <a href="${pageContext.request.contextPath}/login">Log in here</a>.</p>
            </div>
        </div>
    </div>
</body>
</html> 
