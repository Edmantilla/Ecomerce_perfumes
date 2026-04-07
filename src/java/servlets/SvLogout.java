package servlets;                                       // declara que esta clase pertenece al paquete "servlets"

import java.io.IOException;                             // excepción de entrada/salida (requerida por doGet/doPost)
import javax.servlet.ServletException;                  // excepción propia de los servlets
import javax.servlet.annotation.WebServlet;             // anotación que mapea la URL al servlet
import javax.servlet.http.HttpServlet;                  // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;           // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;          // representa la respuesta HTTP saliente
import javax.servlet.http.HttpSession;                  // permite acceder y destruir la sesión HTTP del usuario

/**
 * SvLogout — Servlet de cierre de sesión.
 * Invalida la sesión HTTP activa (borra todos los atributos: usuario, permisos, etc.)
 * y redirige al usuario a la página de perfil/login.
 */
@WebServlet(name = "SvLogout", urlPatterns = {"/SvLogout"}) // cuando llegue una petición a /SvLogout, Tomcat ejecuta esta clase
public class SvLogout extends HttpServlet {             // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando el botón de logout hace GET a /SvLogout
            throws ServletException, IOException {      // declara las excepciones que puede lanzar

        // Obtener la sesión actual sin crear una nueva (false = no crear si no existe)
        // Si se usara getSession(true) y no hay sesión, crearía una nueva innecesariamente
        HttpSession session = request.getSession(false); // false: no crear una sesión nueva si no hay ninguna activa
        if (session != null) {                           // si había una sesión activa...
            // Destruir la sesión: elimina todos los atributos guardados (usuario, permisos, carrito, etc.)
            session.invalidate();                        // borra session.getAttribute("usuario"), "permisosUsuario", etc.
        }
        // Redirigir al login/perfil tras cerrar la sesión
        // El JSP de perfil mostrará el formulario de inicio de sesión al no encontrar "usuario" en sesión
        response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp"); // redirige a perfil.jsp después de cerrar sesión
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) // también acepta POST para mayor flexibilidad
            throws ServletException, IOException {
        // Acepta tanto GET como POST para mayor compatibilidad con los botones del frontend
        // Algunos botones de "Cerrar sesión" hacen POST por convención; se reutiliza el mismo flujo
        doGet(request, response);                        // delega al doGet para no duplicar lógica de logout
    }
}
