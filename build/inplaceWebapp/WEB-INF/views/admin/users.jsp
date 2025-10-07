<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>QuizMaster-Manage Admins</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/edit.css" />
</head>
<body>
<div class="quiz-page">
    <h1 class="quiz-page-title">Admins</h1>

    <c:if test="${not empty sessionScope.userError}">
        <div style="color:#ff6b6b;margin-bottom:10px;">${sessionScope.userError}</div>
        <c:remove var="userError" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.userInfo}">
        <div style="color:#38a169;margin-bottom:10px;">${sessionScope.userInfo}</div>
        <c:remove var="userInfo" scope="session"/>
    </c:if>

    <table style="width:100%;background:white;border-radius:12px;padding:12px;border-collapse: collapse;">
        <thead>
        <tr>
            <th style="text-align:left;padding:12px;">ID</th>
            <th style="text-align:left;padding:12px;">Username</th>
            <th style="text-align:left;padding:12px;">Role</th>
            <th style="text-align:left;padding:12px;">Actions</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="u" items="${admins}">
            <tr>
            <td style="padding:8px;">${u.id}</td>
            <td style="padding:8px;">${u.name}</td>
            <td style="padding:8px;">
                <c:choose>
                <c:when test="${u.superadmin}">Super Admin</c:when>
                <c:otherwise>Editor</c:otherwise>
                </c:choose>
            </td>
            <td style="padding:8px;">
                <form action="${pageContext.request.contextPath}/admin/users/edit" method="get" style="display:inline;">
                <input type="hidden" name="id" value="${u.id}"/>
                <button class="btn-small" <c:if test="${u.superadmin}">disabled</c:if>>Edit</button>
                </form>
                <form action="${pageContext.request.contextPath}/admin/users/delete" method="post" style="display:inline;margin-left:6px;">
                <input type="hidden" name="id" value="${u.id}"/>
                <button class="btn-small delete"
                        onclick="return confirm('Delete user ${u.name}?');"
                        <c:if test="${u.superadmin}">disabled</c:if>>
                    Delete
                </button>
                </form>
            </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <div style="margin-top:16px;">
        <a href="${pageContext.request.contextPath}/admin/quizzes" class="btn-small" style="text-decoration:none;">Back to Quizzes</a>
    </div>
</div>
</body>
</html>