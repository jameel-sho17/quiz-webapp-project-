<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>QuizMaster-Edit Quiz</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/edit.css" />
    <style>
        .edit-wrap{max-width:1000px;margin:40px auto;background:rgba(255,255,255,.92);padding:24px;border-radius:16px}
        .meta-grid{display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px}
        .q-list{list-style:none;padding:0;margin:0;display:flex;flex-direction:column;gap:10px}
        .q-item{padding:12px 14px;border:1px solid #e2e8f0;border-radius:10px;background:#fff;display:flex;align-items:center;gap:12px;cursor:grab}
        .q-item.dragging{opacity:.6}
        .q-handle{font-weight:bold;color:#667eea}
        .q-meta{color:#4a5568;font-size:.9rem}
        .btn-row{display:flex;gap:10px;margin-top:12px}
        .hr{height:1px;background:#e2e8f0;margin:20px 0}
    </style>
</head>
<body>
<div class="edit-wrap">

    <a class="btn-small" href="${pageContext.request.contextPath}/admin/quizzes">← Back</a>
    <h2 style="margin:12px 0;">Edit: <c:out value="${quiz.title}"/></h2>

    <c:if test="${not empty sessionScope.editError}">
        <div style="color:#e53e3e;margin:12px 0;"><c:out value="${sessionScope.editError}"/></div>
        <c:remove var="editError" scope="session"/>
    </c:if>

    <form method="post"
        action="${pageContext.request.contextPath}/admin/quizzes/edit"
        enctype="multipart/form-data">
    <input type="hidden" name="id" value="${quiz.id}"/>

    <div class="meta-grid">
        <label>Title
        <input class="form-input" type="text" name="title" value="${quiz.title}" required>
        </label>
    </div>

    <div class="form-group">
        <label>Current Image</label>
        <img src="${pageContext.request.contextPath}/${quiz.imageUrl}"
            alt="Current Quiz Image"
            style="max-width:200px;display:block;margin-bottom:10px;border-radius:8px;"/>

        <label for="imageFile">Change Image</label>
        <input type="file" name="imageFile" id="imageFile" accept="image/*"/>
    </div>

    <div class="btn-row">
        <button type="submit" class="btn-primary" style="width:auto;">Save</button>
    </div>
    </form>


    <div class="hr"></div>

    <h3>Questions order</h3>
    <p class="q-meta">Drag and drop to reorder</p>

    <ul id="qList" class="q-list">
        <c:forEach var="q" items="${quiz.questions}" varStatus="st">
        <li class="q-item" draggable="true" data-id="${q.id}">
            <span class="q-handle">≡</span>
            <div style="flex:1 1 auto;">
            <div><b>Q${st.index + 1}.</b> <c:out value="${q.questionText}"/></div>
            <div class="q-meta">Time: ${q.durationSeconds}s • Points: ${q.points}</div>
            </div>

            <div style="margin-left:auto; display:flex; gap:8px;">
            <form action="${pageContext.request.contextPath}/admin/quizzes/questions/edit" method="get" style="display:inline;">
                <input type="hidden" name="id" value="${q.id}"/>
                <button type="submit" class="btn-small">Edit</button>
            </form>

            <form action="${pageContext.request.contextPath}/admin/quizzes/questions/delete" method="post" style="display:inline;"
                    onsubmit="return confirm('Delete this question? Quiz must keep at least 5 questions.');">
                <input type="hidden" name="questionId" value="${q.id}"/>
                <input type="hidden" name="quizId" value="${quiz.id}"/>
                <button type="submit" class="btn-small delete">Delete</button>
            </form>
            </div>
        </li>
        </c:forEach>
    </ul>

    <form id="reorderForm" method="post" action="${pageContext.request.contextPath}/admin/quizzes/reorder" class="btn-row">
        <input type="hidden" name="quizId" value="${quiz.id}">
        <input type="hidden" name="order" id="orderInput">
        <button type="submit" class="btn-secondary" style="width:auto;">Save order</button>
    </form>

    <div class="hr"></div>

    <h3 style="margin-top:4px;">Add new question</h3>
    <form method="post" action="${pageContext.request.contextPath}/admin/quizzes/questions/add">
        <input type="hidden" name="quizId" value="${quiz.id}"/>

        <label>Question Text</label>
        <textarea class="form-input" name="questionText" rows="3" required></textarea>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
        <label>Duration (s)
            <input class="form-input" type="number" min="5" name="durationSeconds" value="30" required>
        </label>
        <label>Points
            <input class="form-input" type="number" min="1" name="points" value="1" required>
        </label>
        </div>

        <div style="margin-top:8px;">
        <c:forEach var="i" begin="0" end="3">
            <div class="form-group">
            <label>Answer ${i + 1}</label>
            <input class="form-input" type="text" name="answerText" required>
            <label style="display:flex;align-items:center;gap:6px;margin-top:4px;">
                <input type="checkbox" name="isCorrect" value="${i}"> Correct
            </label>
            </div>
        </c:forEach>
        </div>

        <button type="submit" class="btn-primary" style="width:auto;">Add question</button>
    </form>
</div>

<script>
(function(){
    const list = document.getElementById('qList');
    const orderInput = document.getElementById('orderInput');
    const form = document.getElementById('reorderForm');
    let dragEl = null;

    if (list) {
        list.addEventListener('dragstart', e=>{
        dragEl = e.target.closest('.q-item');
        if (dragEl) dragEl.classList.add('dragging');
        });
        list.addEventListener('dragend', e=>{
        if (dragEl) dragEl.classList.remove('dragging');
        dragEl = null;
        });
        list.addEventListener('dragover', e=>{
        e.preventDefault();
        const after = getDragAfterElement(list, e.clientY);
        const cur = document.querySelector('.q-item.dragging');
        if (!cur) return;
        if (after == null) list.appendChild(cur);
        else list.insertBefore(cur, after);
        });
    }

    function getDragAfterElement(container, y){
        const els = [...container.querySelectorAll('.q-item:not(.dragging)')];
        return els.reduce((closest, child)=>{
        const box = child.getBoundingClientRect();
        const offset = y - box.top - box.height/2;
        if (offset < 0 && offset > closest.offset) return { offset, element: child };
        else return closest;
        }, { offset: Number.NEGATIVE_INFINITY }).element;
    }

    if (form) {
        form.addEventListener('submit', ()=>{
        const ids = [...list.querySelectorAll('.q-item')].map(li=>li.dataset.id);
        orderInput.value = ids.join(',');
        });
    }
})();
</script>
</body>
</html>
