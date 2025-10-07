<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>QuizMaster-Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css?v=1">
</head>
<body>
    <div class="container">
        <div class="auth-card">
            <div class="header">
                <h1>QuizMaster</h1>
                <p>Welcome back! Ready for your next challenge?</p>
            </div>

            <script>
                window.addEventListener("load", function () {
                    const alert = document.getElementById("alert");
                    if (alert) {
                        setTimeout(() => alert.style.display = 'none', 5000);
                    }
                });
            </script>


            <form class="auth-form" method="post" action="${pageContext.request.contextPath}/login">
                <div class="form-group">
                    <label for="name">Username</label>
                    <input type="text" id="name" name="name" placeholder="Enter username" required>
                </div>
                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" placeholder="Enter password" required>
                </div>

                <c:if test="${not empty error}">
                    <div id="alert" class="error-message">${error}</div>
                </c:if>
                
                <button type="submit" class="btn-primary">Login</button>

                
            </form>

            <div class="auth-footer">
                <p>Don't have an account? <a href="${pageContext.request.contextPath}/signup">Sign up here</a>.</p>
            </div>
        </div>
    </div>
</body>
</html>
