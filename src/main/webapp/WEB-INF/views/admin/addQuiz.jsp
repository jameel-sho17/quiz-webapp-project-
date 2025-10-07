<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="jakarta.servlet.http.*,jakarta.servlet.*" %>
<%
    com.example.quiz.model.User user = (com.example.quiz.model.User) session.getAttribute("user");
    if (user == null || !user.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>QuizMaster-New Quiz</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/admin.css">
</head>
<body>
<div class="auth-card">
    <h1 class="add-quiz-title">Add New Quiz</h1>

    <form action="${pageContext.request.contextPath}/admin/addQuiz" method="post" enctype="multipart/form-data" style="max-width: 500px; margin: auto;">
        <div class="form-group">
            <label>Title</label>
            <input type="text" name="title" required class="form-input" style="width: 100%; padding: 10px; margin-bottom: 15px;">
        </div>
        <div class="form-group">
            <label>Image:</label>
            <div class="custom-file-upload">
                <label for="imageFile" class="btn-secondary file-label">Choose Image</label>
                <input type="file" id="imageFile" name="imageFile" accept="image/*" required onchange="updateFileName(); validateSize(this);" hidden>
                <p id="fileNameDisplay" class="file-name">No file chosen</p>
            </div>
        </div>
        <button type="submit" class="btn-primary">Add Questions</button>
    </form>
</div>

<script>
    function updateFileName() {
        const input = document.getElementById("imageFile");
        const fileNameDisplay = document.getElementById("fileNameDisplay");
        if (input.files.length > 0) {
            fileNameDisplay.textContent = input.files[0].name;
        } else {
            fileNameDisplay.textContent = "No file chosen";
        }
    }

    function validateSize(input) {
        const file = input.files[0];
        if (file && file.size > 15 * 1024 * 1024) {
            alert("Image is too large. Maximum allowed size is 15MB.");
            input.value = ""; 
            document.getElementById("fileNameDisplay").textContent = ""; 
        }
    }

</script>

</body>
</html>