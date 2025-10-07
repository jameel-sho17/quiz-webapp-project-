package com.example.quiz.servlet;

import com.example.quiz.model.Answer;
import com.example.quiz.model.Question;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin/questions")
public class AddQuestionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<Question> pendingQuestions = (List<Question>) session.getAttribute("pendingQuestions");
        if (pendingQuestions == null) {
            pendingQuestions = new ArrayList<>();
            session.setAttribute("pendingQuestions", pendingQuestions);
        }

        Object quizTitle = session.getAttribute("pendingQuizTitle");
        request.setAttribute("quizTitle", quizTitle);

        request.setAttribute("questionCount", pendingQuestions.size());

        request.getRequestDispatcher("/WEB-INF/views/admin/addQuestion.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<Question> pendingQuestions = (List<Question>) session.getAttribute("pendingQuestions");
        if (pendingQuestions == null) {
            pendingQuestions = new ArrayList<>();
        }

        System.out.println("AddQuestionServlet: added question. Now count=" + pendingQuestions.size());


        String questionText = request.getParameter("questionText");
        String durationStr = request.getParameter("durationSeconds");
        String pointsStr = request.getParameter("points");
        String[] answerTexts = request.getParameterValues("answerText");
        String[] correctAnswers = request.getParameterValues("isCorrect"); 

        int durationSeconds = 0;
        int points = 0;
        try {
            durationSeconds = Integer.parseInt(durationStr);
        } catch (Exception ignored) {}
        try {
            points = Integer.parseInt(pointsStr);
        } catch (Exception ignored) {}

        Question question = new Question();
        question.setQuestionText(questionText);
        question.setDurationSeconds(durationSeconds);
        question.setPoints(points);
        question.setQuestionOrder(pendingQuestions.size() + 1);

        List<Answer> answers = new ArrayList<>();
        if (answerTexts != null) {
            for (int i = 0; i < answerTexts.length; i++) {
                Answer a = new Answer();
                a.setAnswerText(answerTexts[i]);
                a.setCorrect(false);

                if (correctAnswers != null) {
                    for (String idxStr : correctAnswers) {
                        try {
                            if (Integer.parseInt(idxStr) == i) {
                                a.setCorrect(true);
                                break;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                answers.add(a);
            }
        }

        boolean hasCorrect = answers.stream().anyMatch(Answer::isCorrect);
        if (!hasCorrect) {
            request.setAttribute("error", "At least one answer must be marked as correct.");
            request.setAttribute("questionCount", pendingQuestions.size());
            request.setAttribute("quizTitle", session.getAttribute("pendingQuizTitle"));

            request.setAttribute("draftQuestionText", questionText);
            request.setAttribute("draftDurationSeconds", durationSeconds);
            request.setAttribute("draftPoints", points);
            request.setAttribute("draftAnswers", answers);
            
            request.getRequestDispatcher("/WEB-INF/views/admin/addQuestion.jsp").forward(request, response);
            return;
        }

        question.setAnswers(answers);

        pendingQuestions.add(question);
        session.setAttribute("pendingQuestions", pendingQuestions);
        System.out.println("AddQuestionServlet: added question. Now count=" + pendingQuestions.size());

        response.sendRedirect(request.getContextPath() + "/admin/questions");
    }
}
