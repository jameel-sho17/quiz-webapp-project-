package com.example.quiz.servlet;

import com.example.quiz.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/admin/addQuizPage")
public class AddQuizPageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        HttpSession session = request.getSession();
        session.removeAttribute("pendingQuizTitle");
        session.removeAttribute("pendingQuizImageUrl");
        session.removeAttribute("pendingQuizPin");
        session.removeAttribute("pendingQuizCreatedBy");
        session.removeAttribute("pendingQuestions");
        session.removeAttribute("finalizeError");

        request.getRequestDispatcher("/WEB-INF/views/admin/addQuiz.jsp").forward(request, response);
    }
}