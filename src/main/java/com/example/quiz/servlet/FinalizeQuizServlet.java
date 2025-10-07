package com.example.quiz.servlet;

import com.example.quiz.model.Answer;
import com.example.quiz.model.Question;
import com.example.quiz.model.Quiz;
import com.example.quiz.model.User;
import com.example.quiz.service.AnswerService;
import com.example.quiz.service.QuestionService;
import com.example.quiz.service.QuizService;
import com.example.quiz.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/finalizeQuiz")
public class FinalizeQuizServlet extends HttpServlet {

    private final QuizService quizService = new QuizService();
    private final UserService userService = new UserService();
    private final QuestionService questionService = new QuestionService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/admin/quizzes");
            return;
        }

        

        String title = (String) session.getAttribute("pendingQuizTitle");
        String imageUrl = (String) session.getAttribute("pendingQuizImageUrl");
        String pin = (String) session.getAttribute("pendingQuizPin");
        Long createdById = (Long) session.getAttribute("pendingQuizCreatedBy");

        @SuppressWarnings("unchecked")
        List<Question> pendingQuestions = (List<Question>) session.getAttribute("pendingQuestions");

        System.out.println("FinalizeQuizServlet: ENTER doPost");
        System.out.println("FinalizeQuizServlet: title=" + title + ", imageUrl=" + imageUrl + ", pin=" + pin 
            + ", createdById=" + createdById 
            + ", pendingQuestions.size=" + (pendingQuestions == null ? "null" : pendingQuestions.size()));


        if (title == null || imageUrl == null || pin == null || createdById == null
                || pendingQuestions == null || pendingQuestions.size() < 5) {
            session.setAttribute("finalizeError", "Validation failed: need >=5 questions and all pending* set.");
            System.out.println("FinalizeQuizServlet: validation FAIL -> redirect /admin/questions");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
            return;
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setImageUrl(imageUrl);
        quiz.setPin(pin);

        User creator = userService.getUserById(createdById).orElse(null);
        if (creator != null) {
            quiz.setCreatedBy(creator);
        }

        quizService.createQuiz(quiz); 

        for (Question q : pendingQuestions) {
            q.setQuiz(quiz); 

            if (q.getAnswers() != null) {
                for (Answer a : q.getAnswers()) {
                    a.setQuestion(q);
                }
            }

            questionService.addQuestion(q);

            
        }

        session.removeAttribute("pendingQuizTitle");
        session.removeAttribute("pendingQuizImageUrl");
        session.removeAttribute("pendingQuizPin");
        session.removeAttribute("pendingQuizCreatedBy");
        session.removeAttribute("pendingQuestions");


        System.out.println("FinalizeQuizServlet: ENTER doPost");
        System.out.println("FinalizeQuizServlet: title=" + title + ", imageUrl=" + imageUrl + ", pin=" + pin 
            + ", createdById=" + createdById 
            + ", pendingQuestions.size=" + (pendingQuestions == null ? "null" : pendingQuestions.size()));

        response.sendRedirect(request.getContextPath() + "/admin/quizzes");

        System.out.println("FinalizeQuizServlet: SUCCESS -> redirect /admin/quizzes");

    }
}
