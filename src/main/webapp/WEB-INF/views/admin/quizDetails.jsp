<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<html>
<head>
  <title>QuizMaster-Quiz Details</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/edit.css" />
  <style>
    .details-wrap{max-width:1000px;margin:40px auto;background:rgba(255,255,255,.92);padding:24px;border-radius:16px}
    .q-item{margin:18px 0;padding:16px;border:1px solid #e2e8f0;border-radius:12px;background:#fff}
    .answers{margin-top:10px;display:grid;grid-template-columns:1fr 1fr;gap:8px}
    .ans{padding:10px;border-radius:8px;background:#f7fafc;border:1px solid #e2e8f0}
    .ans.correct{background:#e6fffa;border-color:#38b2ac}
    .meta{color:#4a5568;font-size:.95rem}
  </style>
</head>
<body class="details-body">
  <div class="details-wrap">
    <div style="display:flex;gap:20px;align-items:flex-start">
      <img src="${pageContext.request.contextPath}/${quiz.imageUrl}" alt="" style="width:320px;height:180px;object-fit:cover;border-radius:12px">
      <div>
        <h2 style="margin:0 0 6px 0;">${quiz.title}</h2>
        <div class="meta">Owner: <c:out value="${quiz.createdBy != null ? quiz.createdBy.name : 'N/A'}"/></div>
        <div style="margin-top:12px;">
          <a class="btn-small" href="${pageContext.request.contextPath}/admin/quizzes">← Back</a>
          <a class="btn-small" href="${pageContext.request.contextPath}/admin/quizzes/details?id=${quiz.id}&showAll=${!showAllAnswers ? 1 : 0}">
            <c:choose>
              <c:when test="${showAllAnswers}">Show only correct</c:when>
              <c:otherwise>Show all answers</c:otherwise>
            </c:choose>
          </a>
        </div>
      </div>
    </div>

    <h3 style="margin:24px 0 12px;">Questions (${quiz.questions.size()})</h3>

    <c:forEach var="q" items="${quiz.questions}" varStatus="st">
      <div class="q-item">
        <div><b>Q${st.index + 1}.</b> <c:out value="${q.questionText}"/></div>
        <div class="meta">Time: ${q.durationSeconds}s &nbsp;•&nbsp; Points: ${q.points}</div>

        <div class="answers">
          <c:forEach var="a" items="${q.answers}">
            <c:if test="${showAllAnswers or a.correct}">
              <div class="ans ${a.correct ? 'correct' : ''}">
                <c:out value="${a.answerText}"/>
                <c:if test="${a.correct}"><span style="margin-left:6px;">✅</span></c:if>
              </div>
            </c:if>
          </c:forEach>
        </div>
      </div>
    </c:forEach>
  </div>
</body>
</html>