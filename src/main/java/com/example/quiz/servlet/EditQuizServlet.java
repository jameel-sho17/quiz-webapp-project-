package com.example.quiz.servlet;

import com.example.quiz.model.Quiz;
import com.example.quiz.model.User;
import com.example.quiz.service.QuizService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@WebServlet("/admin/quizzes/edit")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      
        maxFileSize = 15 * 1024 * 1024,       
        maxRequestSize = 20 * 1024 * 1024     
)
public class EditQuizServlet extends HttpServlet {
    private final QuizService quizService = new QuizService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        Long id = null;
        try { id = Long.valueOf(req.getParameter("id")); } catch (Exception ignored) {}
        if (id == null) { resp.sendRedirect(req.getContextPath()+"/admin/quizzes"); return; }

        Quiz quiz = quizService.getEditableQuiz(me, id);
        if (quiz == null) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        req.setAttribute("quiz", quiz);
        req.getRequestDispatcher("/WEB-INF/views/admin/editQuiz.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        Long id = null;
        try { id = Long.valueOf(req.getParameter("id")); } catch (Exception ignored) {}
        if (id == null) { resp.sendRedirect(req.getContextPath()+"/admin/quizzes"); return; }

        String title = req.getParameter("title");

        Boolean active = (req.getParameter("isActive") != null) ? Boolean.TRUE : Boolean.FALSE;

        String newImageUrl = null;
        try {
            Part imagePart = req.getPart("imageFile"); 
            if (imagePart != null && imagePart.getSize() > 0) {
                String ctype = imagePart.getContentType();
                if (ctype == null || !ctype.startsWith("image/")) {
                    req.getSession().setAttribute("editError", "Only image files are allowed.");
                    resp.sendRedirect(req.getContextPath() + "/admin/quizzes/edit?id=" + id);
                    return;
                }

                String originalName = Paths.get(
                        imagePart.getSubmittedFileName() == null ? "image" : imagePart.getSubmittedFileName()
                ).getFileName().toString();

                String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
                String uniqueFileName = System.currentTimeMillis() + "_" + safeName;

                String uploadPath = System.getProperty("user.dir") + "/src/main/webapp/img";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                File imageFile = new File(uploadDir, uniqueFileName);
                imagePart.write(imageFile.getAbsolutePath());

                newImageUrl = "img/" + uniqueFileName; 
            }
        } catch (IllegalStateException ex) {
            req.getSession().setAttribute("editError", "Image too large.");
            resp.sendRedirect(req.getContextPath() + "/admin/quizzes/edit?id=" + id);
            return;
        }

        if (newImageUrl == null) {
            String manualUrl = req.getParameter("imageUrl");
            if (manualUrl != null && !manualUrl.isBlank()) {
                newImageUrl = manualUrl.trim();
            }
        }

        boolean ok = quizService.updateQuizMeta(me, id, title, newImageUrl, active);
        if (!ok) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        resp.sendRedirect(req.getContextPath()+"/admin/quizzes/edit?id="+id);
    }
}

