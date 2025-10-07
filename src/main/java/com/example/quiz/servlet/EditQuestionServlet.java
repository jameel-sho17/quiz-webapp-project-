package com.example.quiz.servlet;

import com.example.quiz.model.*;
import com.example.quiz.repository.QuestionRepository;
import com.example.quiz.service.QuestionService;
import com.example.quiz.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin/quizzes/questions/edit")
public class EditQuestionServlet extends HttpServlet {
    private final QuestionService questionService = new QuestionService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long qid = null;
        try { qid = Long.valueOf(req.getParameter("id")); } catch (Exception ignored) {}
        if (qid == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST); return; }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            QuestionRepository repo = new QuestionRepository(em);
            Question q = repo.findByIdWithQuizAndAnswers(qid);
            if (q == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
            req.setAttribute("question", q);
            req.getRequestDispatcher("/WEB-INF/views/admin/editQuestion.jsp").forward(req, resp);
        } finally { em.close(); }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        Long questionId = null;
        try { questionId = Long.valueOf(req.getParameter("id")); } catch (Exception ignored) {}

        String text = req.getParameter("questionText");
        int duration = parseInt(req.getParameter("durationSeconds"));
        int points   = parseInt(req.getParameter("points"));

        String[] answerTexts = req.getParameterValues("answerText");
        String[] correctIdxs = req.getParameterValues("isCorrect");

        List<Answer> answers = new ArrayList<>();
        if (answerTexts != null) {
            for (int i=0;i<answerTexts.length;i++) {
                Answer a = new Answer();
                a.setAnswerText(answerTexts[i]);
                a.setCorrect(false);
                if (correctIdxs != null) {
                    for (String ci : correctIdxs) {
                        try { if (Integer.parseInt(ci) == i) { a.setCorrect(true); break; } }
                        catch (NumberFormatException ignored) {}
                    }
                }
                answers.add(a);
            }
        }

        boolean ok = questionService.updateQuestion(me, questionId, text, duration, points, answers);

        Long quizId = null;
        try {
            EntityManager em = JPAUtil.getEntityManager();
            try { quizId = new QuestionRepository(em).findById(questionId).getQuiz().getId(); }
            finally { em.close(); }
        } catch (Exception ignored) {}

        String redirect = req.getContextPath()+"/admin/quizzes/edit?id="+(quizId==null?"":quizId);
        if (!ok) {
            req.getSession().setAttribute("editError", "Failed to update question (check fields / at least one correct).");
        }
        resp.sendRedirect(redirect);
    }

    private int parseInt(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }
}