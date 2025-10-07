package com.example.quiz.servlet;

import com.example.quiz.model.Quiz;
import com.example.quiz.model.User;
import com.example.quiz.service.QuizService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;

@WebServlet("/admin/quizzes/details")
public class QuizDetailsServlet extends HttpServlet {
    private final QuizService quizService = new QuizService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User me = (User) req.getSession().getAttribute("user");
        Long id = null;
        try { id = Long.valueOf(req.getParameter("id")); } catch (Exception ignored) {}

        if (id == null) { resp.sendRedirect(req.getContextPath()+"/admin/quizzes"); return; }

        Quiz quiz = quizService.getQuizWithChildrenIfAllowed(me, id);
        if (quiz == null) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        boolean showAllAnswers = "1".equals(req.getParameter("showAll"));

        req.setAttribute("quiz", quiz);
        req.setAttribute("showAllAnswers", showAllAnswers);
        req.getRequestDispatcher("/WEB-INF/views/admin/quizDetails.jsp").forward(req, resp);
    }
}