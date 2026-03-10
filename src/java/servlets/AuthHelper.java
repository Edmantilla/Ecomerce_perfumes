package servlets;

/**
 * AuthHelper — Clase utilitaria de seguridad.
 * Contiene métodos estáticos para verificar si un usuario tiene sesión activa,
 * si es administrador, y si posee un permiso específico.
 * Todos los servlets del panel admin la usan antes de ejecutar cualquier acción.
 * Los permisos se cargan en sesión durante el login (SvLogin) y se consultan aquí.
 */
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AuthHelper {

    // Clave del atributo de sesión que contiene la lista de nombres de permisos del usuario logueado
    public static final String SESS_PERMISOS = "permisosUsuario";

    /**
     * Verifica si el usuario logueado es administrador puro (sin cliente asociado)
     * o tiene el permiso VER_DASHBOARD. Ambos casos dan acceso al panel admin.
     */
    public static boolean esAdmin(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s == null) return false;
        if (Boolean.TRUE.equals(s.getAttribute("esAdmin"))) return true;
        return tienePermiso(request, "VER_DASHBOARD");
    }

    /**
     * Verifica si el usuario logueado tiene un permiso específico.
     * Los permisos se leen de la lista cargada en sesión durante el login.
     * Ejemplos: "VER_PEDIDOS", "GESTIONAR_ENVIOS", "VER_DASHBOARD".
     * El admin puro siempre retorna true sin importar el permiso pedido.
     */
    @SuppressWarnings("unchecked")
    public static boolean tienePermiso(HttpServletRequest request, String nombrePermiso) {
        HttpSession s = request.getSession(false);
        if (s == null) return false;
        // Admin puro tiene todos los permisos
        if (Boolean.TRUE.equals(s.getAttribute("esAdmin"))) return true;
        List<String> permisos = (List<String>) s.getAttribute(SESS_PERMISOS);
        if (permisos == null) return false;
        for (String p : permisos) {
            if (p.equalsIgnoreCase(nombrePermiso)) return true;
        }
        return false;
    }

    /**
     * Verifica si hay una sesión activa con un usuario logueado.
     * Retorna true si la sesión existe y tiene el atributo "usuario" cargado.
     */
    public static boolean estaLogueado(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        return s != null && s.getAttribute("usuario") != null;
    }
}
