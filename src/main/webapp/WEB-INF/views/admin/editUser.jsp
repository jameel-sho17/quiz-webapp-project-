<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>QuizMaster-Edit Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/edit.css" />
</head>
<body>
<div class="quiz-page" style="max-width:720px;margin:0 auto;">
    <h1 class="quiz-page-title" style="font-size:2rem;">Edit Admin</h1>

    <form action="${pageContext.request.contextPath}/admin/users/edit" method="post"
        style="background:white;border-radius:12px;padding:16px;">
        <input type="hidden" name="id" value="${editUser.id}"/>

        <label>Username
        <input class="form-input" type="text" name="name" value="${editUser.name}" required/>
        </label>

        <label>New Password (optional)
        <input class="form-input" type="password" name="password" placeholder="Leave blank to keep current"/>
        </label>

        <div style="margin-top:12px;">
        <button class="btn-primary" style="width:auto;">Save</button>
        <a href="${pageContext.request.contextPath}/admin/users" class="btn-small" style="margin-left:8px;text-decoration:none;">Cancel</a>
        </div>
    </form>
</div>
</body>
</html>