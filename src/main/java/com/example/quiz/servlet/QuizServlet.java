package com.example.quiz.servlet;

import com.example.quiz.model.Quiz;
import com.example.quiz.model.User;
import com.example.quiz.service.QuizService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "QuizServlet", urlPatterns = {"/admin/quizzes"})
public class QuizServlet extends HttpServlet {
    private QuizService quizService = new QuizService();

    @Override 
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("user");
        if (user == null || !user.isAdmin()) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<Quiz> quizzes = quizService.getQuizzesFor(user);
        
        req.setAttribute("quizzes", quizzes);
        req.getRequestDispatcher("/WEB-INF/views/admin/quizzes.jsp").forward(req, resp);
    }
}