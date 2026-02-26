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

@WebServlet(name = "SvRegistro", urlPatterns = {"/SvRegistro"})
public class SvRegistro extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String nombre      = request.getParameter("nombre");
        String apellido    = request.getParameter("apellido");
        String correo      = request.getParameter("correo_electronico");
        String contrasena  = request.getParameter("contrasena");
        String confirmar   = request.getParameter("confirmar_contrasena");
        String direccion   = request.getParameter("direccion");

        if (direccion == null || direccion.isBlank()) {
            direccion = "Sin especificar";
        }

        if (nombre == null || correo == null || contrasena == null ||
                nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            request.setAttribute("error", "Todos los campos son obligatorios.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        if (!contrasena.equals(confirmar)) {
            request.setAttribute("error", "Las contraseñas no coinciden.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        try {
            UsuarioJpaController usuarioCtrl = new UsuarioJpaController();
            ClienteJpaController clienteCtrl = new ClienteJpaController();
            RolJpaController rolCtrl         = new RolJpaController();

            for (logica.Usuario u : usuarioCtrl.findUsuarioEntities()) {
                if (u.getCorreoUsuario().equalsIgnoreCase(correo.trim())) {
                    request.setAttribute("error", "Ya existe una cuenta con ese correo electrónico.");
                    request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
                    return;
                }
            }

            String nombreCompleto = nombre.trim() + " " + (apellido != null ? apellido.trim() : "");

            Cliente cliente = new Cliente();
            cliente.setNombreCompleto(nombreCompleto.trim());
            cliente.setDireccion(direccion);
            cliente.setActivo(true);
            cliente.setCreatedAt(LocalDateTime.now());
            cliente.setUpdatedAt(LocalDateTime.now());
            clienteCtrl.create(cliente);

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

            Usuario usuario = new Usuario();
            usuario.setCorreoUsuario(correo);
            usuario.setContrasena(contrasena);
            usuario.setCliente(cliente);
            usuario.setRol(rolCliente);
            usuario.setActivo(true);
            usuario.setCreatedAt(LocalDateTime.now());
            usuario.setUpdatedAt(LocalDateTime.now());
            usuarioCtrl.create(usuario);

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
