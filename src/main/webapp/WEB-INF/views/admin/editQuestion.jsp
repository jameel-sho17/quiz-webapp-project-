<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
  <title>QuizMaster-Edit Question</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/edit.css" />
  <style>.wrap{max-width:900px;margin:40px auto;background:rgba(255,255,255,.95);padding:24px;border-radius:16px}</style>
</head>
<body>
<div class="wrap">
  <a class="btn-small" href="${pageContext.request.contextPath}/admin/quizzes/edit?id=${question.quiz.id}">← Back to quiz</a>
  <h2 style="margin:12px 0;">Edit Question</h2>

  <form method="post" action="${pageContext.request.contextPath}/admin/quizzes/questions/edit">
    <input type="hidden" name="id" value="${question.id}"/>

    <label>Question Text</label>
    <textarea class="form-input" name="questionText" rows="3" required><c:out value="${question.questionText}"/></textarea>

    <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
      <label>Duration (s)
        <input class="form-input" type="number" name="durationSeconds" value="${question.durationSeconds}" required>
      </label>
      <label>Points
        <input class="form-input" type="number" name="points" value="${question.points}" required>
      </label>
    </div>

    <h4 style="margin-top:12px;">Answers</h4>
    <div>
      <c:forEach var="a" items="${question.answers}" varStatus="st">
        <div class="form-group">
          <label>Answer ${st.index + 1}</label>
          <input class="form-input" type="text" name="answerText" value="${a.answerText}" required>
          <label style="display:flex;align-items:center;gap:6px;margin-top:4px;">
            <input type="checkbox" name="isCorrect" value="${st.index}" ${a.correct ? "checked" : ""}> Correct
          </label>
        </div>
      </c:forEach>
    </div>

    <button type="submit" class="btn-primary" style="width:auto;">Save question</button>
  </form>
</div>
</body>
</html>
