package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * SvLogout — Servlet de cierre de sesión.
 * Invalida la sesión HTTP activa (borra todos los atributos: usuario, permisos, etc.)
 * y redirige al usuario a la página de perfil/login.
 */
@WebServlet(name = "SvLogout", urlPatterns = {"/SvLogout"})
public class SvLogout extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener la sesión actual sin crear una nueva (false = no crear si no existe)
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Destruir la sesión: elimina todos los atributos guardados (usuario, permisos, etc.)
            session.invalidate();
        }
        // Redirigir al login/perfil tras cerrar la sesión
        response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Acepta tanto GET como POST para mayor compatibilidad con los botones del frontend
        doGet(request, response);
    }
}
