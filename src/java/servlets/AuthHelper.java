package servlets; // Este archivo pertenece al paquete "servlets"

/**
 * AuthHelper — Clase utilitaria de seguridad.
 * Contiene métodos estáticos para verificar si un usuario tiene sesión activa,
 * si es administrador, y si posee un permiso específico.
 * Todos los servlets del panel admin la usan antes de ejecutar cualquier acción.
 * Los permisos se cargan en sesión durante el login (SvLogin) y se consultan aquí.
 */
import java.util.List; // Para recorrer la lista de permisos almacenada en la sesión
import javax.servlet.http.HttpServletRequest; // Para obtener la sesión HTTP desde el request
import javax.servlet.http.HttpSession; // Representa la sesión HTTP del usuario logueado

public class AuthHelper { // Clase de utilidad: todos sus métodos son estáticos, no se instancia

    // Clave del atributo de sesión que contiene la lista de nombres de permisos del usuario logueado
    // Se define como constante para evitar errores de typo al usarla en múltiples servlets
    public static final String SESS_PERMISOS = "permisosUsuario";

    /**
     * Verifica si el usuario logueado es administrador puro (sin cliente asociado)
     * o tiene el permiso VER_DASHBOARD. Ambos casos dan acceso al panel admin.
     */
    public static boolean esAdmin(HttpServletRequest request) {
        HttpSession s = request.getSession(false); // false = no crear sesión nueva si no existe
        if (s == null) return false; // Sin sesión activa, no es admin
        if (Boolean.TRUE.equals(s.getAttribute("esAdmin"))) return true; // Admin puro (sin cliente asociado)
        return tienePermiso(request, "VER_DASHBOARD"); // Vendedor con permiso de dashboard también accede al panel
    }

    /**
     * Verifica si el usuario logueado tiene un permiso específico.
     * Los permisos se leen de la lista cargada en sesión durante el login.
     * Ejemplos: "VER_PEDIDOS", "GESTIONAR_ENVIOS", "VER_DASHBOARD".
     * El admin puro siempre retorna true sin importar el permiso pedido.
     */
    @SuppressWarnings("unchecked") // Suprime el warning del cast sin verificar a List<String>
    public static boolean tienePermiso(HttpServletRequest request, String nombrePermiso) {
        HttpSession s = request.getSession(false); // false = no crear sesión nueva si no existe
        if (s == null) return false; // Sin sesión activa, no tiene ningún permiso
        // El admin puro tiene todos los permisos del sistema sin necesidad de verificar la lista
        if (Boolean.TRUE.equals(s.getAttribute("esAdmin"))) return true;
        List<String> permisos = (List<String>) s.getAttribute(SESS_PERMISOS); // Obtiene la lista de permisos de la sesión
        if (permisos == null) return false; // Si la lista no existe en sesión, no tiene permisos
        for (String p : permisos) {
            if (p.equalsIgnoreCase(nombrePermiso)) return true; // Comparación insensible a mayúsculas
        }
        return false; // El permiso solicitado no está en la lista del usuario
    }

    /**
     * Verifica si hay una sesión activa con un usuario logueado.
     * Retorna true si la sesión existe y tiene el atributo "usuario" cargado.
     */
    public static boolean estaLogueado(HttpServletRequest request) {
        HttpSession s = request.getSession(false); // false = no crear sesión nueva si no existe
        return s != null && s.getAttribute("usuario") != null; // true solo si hay sesión Y el usuario está cargado
    }
}
