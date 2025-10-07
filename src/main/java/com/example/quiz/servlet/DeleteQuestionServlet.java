package com.example.quiz.servlet;

import com.example.quiz.model.User;
import com.example.quiz.service.QuestionService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;

@WebServlet("/admin/quizzes/questions/delete")
public class DeleteQuestionServlet extends HttpServlet {
    private final QuestionService questionService = new QuestionService();

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User me = (User) req.getSession().getAttribute("user");
        Long questionId = null, quizId = null;
        try { questionId = Long.valueOf(req.getParameter("questionId")); } catch (Exception ignored) {}
        try { quizId = Long.valueOf(req.getParameter("quizId")); } catch (Exception ignored) {}

        boolean ok = questionService.deleteQuestionIfAllowed(me, questionId);
        String redirect = req.getContextPath()+"/admin/quizzes/edit?id="+quizId;
        if (!ok) {
            req.getSession().setAttribute("editError", "Cannot delete. A quiz must have at least 5 questions.");
        }
        resp.sendRedirect(redirect);
    }
}