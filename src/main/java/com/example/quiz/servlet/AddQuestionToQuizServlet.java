package com.example.quiz.servlet;

import com.example.quiz.model.Answer;
import com.example.quiz.model.User;
import com.example.quiz.service.QuestionService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin/quizzes/questions/add")
public class AddQuestionToQuizServlet extends HttpServlet {
    private final QuestionService questionService = new QuestionService();

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User me = (User) req.getSession().getAttribute("user");
        Long quizId = null;
        try { quizId = Long.valueOf(req.getParameter("quizId")); } catch (Exception ignored) {}

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

        boolean ok = questionService.addQuestionToQuiz(me, quizId, text, duration, points, answers);
        String redirect = req.getContextPath()+"/admin/quizzes/edit?id="+quizId;
        if (!ok) {
            req.getSession().setAttribute("editError", "Failed to add question (check required fields and at least one correct answer).");
        }
        resp.sendRedirect(redirect);
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}