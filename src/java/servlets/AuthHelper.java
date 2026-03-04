package servlets;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Utilidad centralizada para verificar autorizacion en servlets.
 * Lee los permisos cargados en sesion por SvLogin.
 */
public class AuthHelper {

    /** Atributo de sesion que contiene la lista de nombres de permiso del usuario. */
    public static final String SESS_PERMISOS = "permisosUsuario";

    /**
     * Retorna true si el usuario en sesion es administrador puro
     * (sin cliente asociado) O tiene el permiso VER_DASHBOARD.
     */
    public static boolean esAdmin(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s == null) return false;
        if (Boolean.TRUE.equals(s.getAttribute("esAdmin"))) return true;
        return tienePermiso(request, "VER_DASHBOARD");
    }

    /**
     * Retorna true si el usuario en sesion tiene el permiso indicado.
     * Compara sin importar mayusculas/minusculas.
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

    /** Retorna true si hay sesion activa (cualquier usuario logueado). */
    public static boolean estaLogueado(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        return s != null && s.getAttribute("usuario") != null;
    }
}
