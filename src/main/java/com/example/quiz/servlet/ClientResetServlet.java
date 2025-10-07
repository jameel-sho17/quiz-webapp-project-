package com.example.quiz.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "ClientResetServlet", urlPatterns = {"/client/reset"})
public class ClientResetServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resetAndGo(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resetAndGo(req, resp);
    }

    private void resetAndGo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            httpSession.removeAttribute("playerId");
            httpSession.removeAttribute("playerName");
            httpSession.removeAttribute("sessionPin");
            httpSession.removeAttribute("sessionId");
        }
        String cancelled = req.getParameter("cancelled") != null ? "1" : null;
        String target = req.getContextPath() + "/join-quiz.jsp" + (cancelled != null ? "?cancelled=1" : "");
        resp.sendRedirect(target);
    }
}
