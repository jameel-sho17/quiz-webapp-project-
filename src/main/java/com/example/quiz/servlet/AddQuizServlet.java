package com.example.quiz.servlet;

import com.example.quiz.model.Question;
import com.example.quiz.model.Quiz;
import com.example.quiz.model.User;
import com.example.quiz.service.QuizService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

@WebServlet("/admin/addQuiz")
@MultipartConfig(fileSizeThreshold = 1024 * 1024,     
                 maxFileSize = 15 * 1024 * 1024,        
                 maxRequestSize = 20 * 1024 * 1024)    
public class AddQuizServlet extends HttpServlet {

    private final QuizService quizService = new QuizService();

    private String generateUniquePin() {
        Random random = new Random();
        String pin;
        do {
            pin = String.format("%08d", random.nextInt(100_000_000));
        } while (quizService.isPinTaken(pin));
        return pin;
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null || !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String title = request.getParameter("title");
        String pin = generateUniquePin();

        Part imagePart = request.getPart("imageFile");

        String fileName = Paths.get(imagePart.getSubmittedFileName()).getFileName().toString();
        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;

        String imagePath = "img/" + uniqueFileName;

        String uploadPath = System.getProperty("user.dir") + "/src/main/webapp/img";


        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File imageFile = new File(uploadDir, uniqueFileName);
        imagePart.write(imageFile.getAbsolutePath());


        session.setAttribute("pendingQuizTitle", title);
        session.setAttribute("pendingQuizImageUrl", imagePath);
        session.setAttribute("pendingQuizPin", pin);
        session.setAttribute("pendingQuizCreatedBy", user.getId());

        @SuppressWarnings("unchecked")
        List<Question> pendingQuestions = (List<Question>) session.getAttribute("pendingQuestions");
        if (pendingQuestions == null) {
            pendingQuestions = new ArrayList<>();
            session.setAttribute("pendingQuestions", pendingQuestions);
        }

        // redirekcija na dodavanje pitanja
        response.sendRedirect(request.getContextPath() + "/admin/questions");

    }
}
