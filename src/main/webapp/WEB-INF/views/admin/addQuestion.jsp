<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<html>
<head>
    <title>QuizMaster-Add Question</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/question.css">
</head>
<body>

<div class="container">
    <h1 class="add-question-title">Add a Question to the Quiz: ${quizTitle}</h1>
    <p style="color:black; font-size:1.1rem;">
        Add at least 5 questions to add a quiz. You have added <strong>${questionCount}</strong> questions.
    </p>
    


    <form id="addQuestionForm" method="post">
        <div class="form-group">
            <label>Question Text</label><br>
            <textarea name="questionText" rows="3" cols="80" required><c:out value="${fn:trim(draftQuestionText)}" /></textarea>


        </div>

        <div class="form-group">
            <label>Duration (max 60s)</label>
            <input type="number" name="durationSeconds" min="5" max="60" required placeholder="e.g. 30"
              value="${draftDurationSeconds != null ? draftDurationSeconds : ''}">

        </div>

        <div class="form-group">
            <label>Points</label>
            <input type="number" name="points" min="1" max="100" required placeholder="e.g. 5"
              value="${draftPoints != null ? draftPoints : ''}">

        </div>

        <hr>

        <h3>Answers</h3>
        <c:forEach var="i" begin="0" end="3">
          <c:set var="ans" value="${draftAnswers != null && i < draftAnswers.size() ? draftAnswers[i] : null}" />

          <div class="form-group">
              <label>Answer ${i + 1}</label>
              <input type="text" name="answerText" required
                    value="${ans != null ? ans.answerText : ''}">
              <label>
                  <input type="checkbox" name="isCorrect" value="${i}"
                        <c:if test="${ans != null && ans.correct}">checked</c:if>>
                  Correct
              </label>
          </div>
        </c:forEach>


        <c:if test="${not empty error}">
          <div style="color:#d33;margin:8px 0;">${error}</div>
        </c:if>



        <div class="form-group">
            <button id="addQuestionBtn" type="submit">Add Question</button>
        </div>

        


    </form>
    <c:if test="${questionCount >= 5}">
            <form id="finalizeForm" action="${pageContext.request.contextPath}/admin/finalizeQuiz" method="post">
                <button type="submit" class="btn-primary">Add Quiz</button>
            </form>
    </c:if>

    <hr>
    <a href="${pageContext.request.contextPath}/admin/cancelDraft">Back to Quizzes</a>


</div>

<script>
document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("addQuestionForm"); 
  const checkboxes = document.querySelectorAll('input[name="isCorrect"]');
  const addBtn = document.getElementById("addQuestionBtn"); 
  const warn = document.getElementById("correctWarn");

  function validate() {
    const anyChecked = Array.from(checkboxes).some(cb => cb.checked);
    if (!anyChecked) {
      addBtn.disabled = true;
      if (warn) warn.style.display = 'block';
    } else {
      addBtn.disabled = false;
      if (warn) warn.style.display = 'none';
    }
  }

  checkboxes.forEach(cb => cb.addEventListener('change', validate));
  validate(); 
});
</script>


</body>
</html>