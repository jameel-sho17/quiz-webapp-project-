package com.example.quiz.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.InputStream;

@WebServlet("/css/login.css")
public class StaticResourceServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/css");
        try (InputStream in = getServletContext().getResourceAsStream("/css/login.css")) {
            if (in != null) {
                in.transferTo(resp.getOutputStream());
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
}
