package com.example.quiz.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/admin/cancelDraft")
public class CancelDraftServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("pendingQuizTitle");
            session.removeAttribute("pendingQuizImageUrl");
            session.removeAttribute("pendingQuizPin");
            session.removeAttribute("pendingQuizCreatedBy");
            session.removeAttribute("pendingQuestions");
            session.removeAttribute("finalizeError");
        }

        response.sendRedirect(request.getContextPath() + "/admin/quizzes");
    }
}
