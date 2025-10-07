<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.quiz.model.Quiz" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>


<!DOCTYPE html>
<html>
<head>
    <title>QuizMaster-All Quizzes</title>
    <link rel="stylesheet" href="../../css/admin.css" />
</head>
<body>
<%
    com.example.quiz.model.User user = (com.example.quiz.model.User) session.getAttribute("user");
    if (user != null) {
%>
    <div style="position: absolute; top: 20px; left: 20px; background-color: rgba(255,255,255,0.1); padding: 12px 20px; border-radius: 12px; color: white; font-size: 1rem;">
        👤 Admin: <strong><%= user.getName() %></strong>
        <c:if test="${sessionScope.user ne null and sessionScope.user.superadmin == true}">
            <form action="${pageContext.request.contextPath}/admin/users" method="get" style="margin-top: 10px;">
                <button type="submit" class="btn-small manage" style="font-size: 0.85rem;">Manage Admins</button>
            </form>
        </c:if>
        <form action="<%= request.getContextPath() %>/logout" method="post" style="margin-top: 10px;">
            <button type="submit" class="btn-small delete" style="font-size: 0.85rem;">Logout</button>
        </form>
    </div>
<%
    }
%>

<div class="quiz-page">
<h1 class="quiz-page-title">All Available Quizzes</h1>

<div class="carousel-wrapper">
    <button class="carousel-btn left" id="left-arrow">&#10094;</button>

    <div class="carousel-container" id="carousel-container">
        <div class="carousel-track" id="quiz-carousel">
            <%
                List<Quiz> quizzes = (List<Quiz>) request.getAttribute("quizzes");
                for (Quiz q : quizzes) {
            %>
            <div class="quiz-card">
                <img src="<%= request.getContextPath() + "/" + q.getImageUrl() %>" alt="Quiz Image" class="quiz-image"/>
                <h3><%= q.getTitle() %></h3>
                <!--<p>Status: <%= q.getIsActive() ? "Active" : "Inactive" %></p>-->

                <% if (user != null && user.isSuperadmin()) { %>
                    <p class="muted" style="opacity:.8;">
                        Owner: <%= (q.getCreatedBy() != null ? q.getCreatedBy().getName() : "N/A") %>
                    </p>
                <% } %>

                <div class="button-group-small">
                    <form action="${pageContext.request.contextPath}/admin/quizzes/details" method="get" style="display:inline;">
                        <input type="hidden" name="id" value="<%= q.getId() %>"/>
                        <button type="submit" class="btn-small">Details</button>
                    </form>
                    <form action="${pageContext.request.contextPath}/admin/quizzes/edit" method="get" style="display:inline;">
                        <input type="hidden" name="id" value="<%= q.getId() %>"/>
                        <button type="submit" class="btn-small">Edit</button>
                    </form>

                    <form action="${pageContext.request.contextPath}/admin/quizzes/delete" method="post" style="display:inline;"
                        onsubmit="return confirm('Delete this quiz permanently?')">
                        <input type="hidden" name="id" value="<%= q.getId() %>"/>
                        <button type="submit" class="btn-small delete">Delete</button>
                    </form>
                </div>
                <form action="${pageContext.request.contextPath}/admin/quizzes/start" method="post" style="margin-top: 12px;">
                    <input type="hidden" name="quizId" value="<%= q.getId() %>"/>
                    <button type="submit" class="btn-primary start-btn">Start Quiz</button>
                </form>

            </div>
            <%
                }
            %>
        </div>
    </div>

    <button class="carousel-btn right" id="right-arrow">&#10095;</button>
</div>
<form action="${pageContext.request.contextPath}/admin/addQuizPage" method="get" style="display: flex; justify-content: center; margin-top: 100px; margin-bottom: 60px;">
    <button type="submit" class="btn-primary" style="font-size: 1.5rem; padding: 20px 40px; width: auto;">Add New Quiz</button>
</form>

</div>


<script>
    document.addEventListener("DOMContentLoaded", function () {
        const track = document.querySelector(".carousel-track");
        const btnLeft = document.querySelector(".carousel-btn.left");
        const btnRight = document.querySelector(".carousel-btn.right");
        const card = document.querySelector(".quiz-card");

        //3 dodatne linije
        const viewport = document.querySelector('.carousel-container');
        const fitsAll = track.scrollWidth <= viewport.clientWidth;
        track.style.justifyContent = fitsAll ? 'center' : 'flex-start';

        const scrollAmount = card.offsetWidth + 16; 

        function updateButtons() {
            btnLeft.style.display = track.scrollLeft > 0 ? "block" : "none";
            const maxScroll = track.scrollWidth - track.clientWidth;
            btnRight.style.display = track.scrollLeft < maxScroll ? "block" : "none";
        }

        btnLeft.addEventListener("click", () => {
            track.scrollBy({ left: -scrollAmount * 2, behavior: "smooth" });
        });

        btnRight.addEventListener("click", () => {
            track.scrollBy({ left: scrollAmount * 2, behavior: "smooth" });
        });

        track.addEventListener("scroll", updateButtons);

        updateButtons();
    });
</script>

</body>
</html>