package com.gastofacil.filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/*")
public class AutenticacionFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        boolean esRutaApi = path.startsWith("/api/");

        boolean esPaginaPublica = path.endsWith("login.jsp")
                || path.endsWith("index.html")
                || path.endsWith("index.jsp")
                || path.endsWith("registro.html")
                || path.endsWith("registro.jsp")
                || path.contains("AuthServlet")
                || path.contains("RegistroServlet");

        boolean esRecursoEstatico = path.contains("/css/")
                || path.contains("/js/")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".ico");

        HttpSession session = httpRequest.getSession(false);
        boolean usuarioLogueado = (session != null && (session.getAttribute("usuarioLogueado") != null
                || session.getAttribute("nombreUsuario") != null
                || session.getAttribute("idTienda") != null));

        if (esRutaApi || usuarioLogueado || esPaginaPublica || esRecursoEstatico) {
            if (!esRecursoEstatico) {
                httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                httpResponse.setHeader("Pragma", "no-cache");
                httpResponse.setDateHeader("Expires", 0);
            }
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
        }
    }

    @Override
    public void destroy() {
    }
}