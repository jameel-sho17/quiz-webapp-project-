package com.example.quiz.servlet;

import com.example.quiz.model.User;
import com.example.quiz.service.QuizService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebServlet("/admin/quizzes/reorder")
public class ReorderQuestionsServlet extends HttpServlet {
    private final QuizService quizService = new QuizService();

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        Long quizId = null;
        try { quizId = Long.valueOf(req.getParameter("quizId")); } catch (Exception ignored) {}

        String orderCsv = req.getParameter("order"); 
        if (quizId == null || orderCsv == null || orderCsv.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST); return;
        }
        List<Long> ids = Stream.of(orderCsv.split(","))
                .map(String::trim).filter(s->!s.isEmpty())
                .map(Long::valueOf).collect(Collectors.toList());

        boolean ok = quizService.reorderQuestions(me, quizId, ids);
        if (!ok) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        resp.sendRedirect(req.getContextPath()+"/admin/quizzes/edit?id="+quizId);
    }
}
