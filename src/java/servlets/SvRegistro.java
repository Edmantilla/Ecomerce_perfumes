package servlets;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
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
        String fechaNac    = request.getParameter("fecha_nacimiento");

        // Normalizar
        if (nombre   != null) nombre   = nombre.trim();
        if (apellido != null) apellido = apellido.trim();
        if (correo   != null) correo   = correo.trim().toLowerCase();
        if (direccion != null) direccion = direccion.trim();

        // 1. Campos obligatorios vacíos
        if (nombre == null || nombre.isBlank() ||
            apellido == null || apellido.isBlank() ||
            correo == null || correo.isBlank() ||
            contrasena == null || contrasena.isBlank() ||
            confirmar == null || confirmar.isBlank() ||
            direccion == null || direccion.isBlank() ||
            fechaNac == null || fechaNac.isBlank()) {
            request.setAttribute("error", "Todos los campos son obligatorios.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 2. Nombre solo letras y espacios (mín 2 chars)
        if (!nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]{2,50}")) {
            request.setAttribute("error", "El nombre solo puede contener letras (mínimo 2 caracteres).");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 3. Apellido solo letras y espacios (mín 2 chars)
        if (!apellido.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]{2,50}")) {
            request.setAttribute("error", "El apellido solo puede contener letras (mínimo 2 caracteres).");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 4. Formato de correo electrónico
        if (!Pattern.matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$", correo)) {
            request.setAttribute("error", "El correo electrónico no tiene un formato válido.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 5. Longitud de contraseña: mínimo 8, máximo 20
        if (contrasena.length() < 8 || contrasena.length() > 20) {
            request.setAttribute("error", "La contraseña debe tener entre 8 y 20 caracteres.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 6. Complejidad de contraseña: al menos 1 letra y 1 número
        if (!contrasena.matches(".*[a-zA-Z].*") || !contrasena.matches(".*[0-9].*")) {
            request.setAttribute("error", "La contraseña debe contener al menos una letra y un número.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 7. Las contraseñas coinciden
        if (!contrasena.equals(confirmar)) {
            request.setAttribute("error", "Las contraseñas no coinciden.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 8. Fecha de nacimiento válida y edad mínima 18 años
        try {
            LocalDate nacimiento = LocalDate.parse(fechaNac);
            if (nacimiento.isAfter(LocalDate.now())) {
                request.setAttribute("error", "La fecha de nacimiento no puede ser una fecha futura.");
                request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
                return;
            }
            int edad = Period.between(nacimiento, LocalDate.now()).getYears();
            if (edad < 18) {
                request.setAttribute("error", "Debes tener al menos 18 años para crear una cuenta.");
                request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
                return;
            }
            if (edad > 120) {
                request.setAttribute("error", "La fecha de nacimiento ingresada no es válida.");
                request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
                return;
            }
        } catch (DateTimeParseException e) {
            request.setAttribute("error", "La fecha de nacimiento no tiene un formato válido.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 9. Dirección mínimo 10 caracteres
        if (direccion.length() < 10) {
            request.setAttribute("error", "La dirección debe ser más específica (mínimo 10 caracteres).");
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
            request.setAttribute("error", "Ocurrió un error al crear la cuenta. Intenta de nuevo.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
    }
}
