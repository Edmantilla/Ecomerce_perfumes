package servlets;

import java.io.IOException;
import java.time.LocalDateTime;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import logica.Cliente;
import logica.Rol;
import logica.Usuario;
import persistencias.ClienteJpaController;
import persistencias.RolJpaController;
import persistencias.UsuarioJpaController;

/**
 * SvRegistro — Servlet de registro de nuevos usuarios.
 * POST: recibe los datos del formulario, crea un Cliente y un Usuario en BD,
 *       asigna el rol CLIENTE y redirige al perfil con mensaje de éxito.
 * GET: redirige directamente a la página de registro (registro.jsp).
 */
@WebServlet(name = "SvRegistro", urlPatterns = {"/SvRegistro"})
public class SvRegistro extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Leer todos los campos del formulario de registro
        String nombre      = request.getParameter("nombre");
        String apellido    = request.getParameter("apellido");
        String correo      = request.getParameter("correo_electronico");
        String contrasena  = request.getParameter("contrasena");
        String confirmar   = request.getParameter("confirmar_contrasena");
        String direccion   = request.getParameter("direccion");

        // Validar que los campos obligatorios no estén vacíos
        if (nombre == null || correo == null || contrasena == null || direccion == null ||
                nombre.isBlank() || correo.isBlank() || contrasena.isBlank() || direccion.isBlank()) {
            request.setAttribute("error", "Todos los campos son obligatorios, incluyendo la dirección.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // Validar que las dos contraseñas escritas coincidan
        if (!contrasena.equals(confirmar)) {
            request.setAttribute("error", "Las contraseñas no coinciden.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        try {
            UsuarioJpaController usuarioCtrl = new UsuarioJpaController();
            ClienteJpaController clienteCtrl = new ClienteJpaController();
            RolJpaController rolCtrl         = new RolJpaController();

            // Verificar que el correo no esté registrado ya (sin importar mayúsculas)
            for (logica.Usuario u : usuarioCtrl.findUsuarioEntities()) {
                if (u.getCorreoUsuario().equalsIgnoreCase(correo.trim())) {
                    request.setAttribute("error", "Ya existe una cuenta con ese correo electrónico.");
                    request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
                    return;
                }
            }

            // Unir nombre y apellido en un solo campo nombre completo
            String nombreCompleto = nombre.trim() + " " + (apellido != null ? apellido.trim() : "");

            // Crear el registro de Cliente (datos personales del comprador)
            Cliente cliente = new Cliente();
            cliente.setNombreCompleto(nombreCompleto.trim());
            cliente.setDireccion(direccion);
            cliente.setActivo(true);
            cliente.setCreatedAt(LocalDateTime.now());
            cliente.setUpdatedAt(LocalDateTime.now());
            clienteCtrl.create(cliente); // INSERT en tabla cliente

            // Buscar el rol CLIENTE en BD; si no existe, crearlo
            // Normalmente ya existe como dato semilla en el SQL inicial
            Rol rolCliente = null;
            for (Rol r : rolCtrl.findRolEntities()) {
                if ("CLIENTE".equalsIgnoreCase(r.getNombreRol())) {
                    rolCliente = r;
                    break;
                }
            }
            if (rolCliente == null) {
                rolCliente = new Rol();
                rolCliente.setNombreRol("CLIENTE");
                rolCliente.setActivo(true);
                rolCtrl.create(rolCliente);
            }

            // Crear el Usuario (cuenta de acceso) vinculado al cliente y al rol
            Usuario usuario = new Usuario();
            usuario.setCorreoUsuario(correo);
            usuario.setContrasena(contrasena); // contraseña en texto plano
            usuario.setCliente(cliente);       // relación con sus datos personales
            usuario.setRol(rolCliente);        // rol CLIENTE (sin permisos de admin)
            usuario.setActivo(true);
            usuario.setCreatedAt(LocalDateTime.now());
            usuario.setUpdatedAt(LocalDateTime.now());
            usuarioCtrl.create(usuario); // INSERT en tabla usuario

            // Guardar mensaje de éxito en sesión para mostrarlo en perfil.jsp tras el redirect
            HttpSession session = request.getSession();
            session.setAttribute("registroExitoso", "¡Cuenta creada exitosamente! Ahora inicia sesión.");

            response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp");

        } catch (Exception e) {
            request.setAttribute("error", "Error al registrar: " + e.getMessage());
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
    }
}
