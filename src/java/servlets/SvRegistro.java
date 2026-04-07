package servlets;                                        // declara que esta clase pertenece al paquete "servlets"

import java.io.IOException;                              // excepción de entrada/salida (requerida por doPost/doGet)
import java.time.LocalDate;                              // tipo de fecha sin hora; se usa para validar la fecha de nacimiento
import java.time.LocalDateTime;                          // tipo de fecha+hora; se asigna a createdAt/updatedAt
import java.time.Period;                                 // calcula diferencias entre fechas (ej: cuántos años tiene el usuario)
import java.time.format.DateTimeParseException;          // excepción que se lanza si la fecha no tiene formato YYYY-MM-DD
import java.util.regex.Pattern;                          // permite compilar y usar expresiones regulares (validación de correo)
import javax.servlet.ServletException;                   // excepción propia de los servlets
import javax.servlet.annotation.WebServlet;              // anotación que mapea la URL al servlet
import javax.servlet.http.HttpServlet;                   // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;            // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;           // representa la respuesta HTTP saliente
import javax.servlet.http.HttpSession;                   // maneja la sesión HTTP del usuario
import logica.Cliente;                                   // entidad que representa los datos personales del comprador
import logica.Rol;                                       // entidad que representa un rol del sistema (ej: CLIENTE, ADMIN)
import logica.Usuario;                                   // entidad que representa la cuenta de acceso del usuario
import persistencias.ClienteJpaController;               // controlador para INSERT/SELECT en la tabla cliente
import persistencias.RolJpaController;                   // controlador para SELECT en la tabla rol
import persistencias.UsuarioJpaController;               // controlador para INSERT/SELECT en la tabla usuario

/**
 * SvRegistro — Servlet de registro de nuevos usuarios.
 * POST: recibe los datos del formulario, crea un Cliente y un Usuario en BD,
 *       asigna el rol CLIENTE y redirige al perfil con mensaje de éxito.
 * GET: redirige directamente a la página de registro (registro.jsp).
 */
@WebServlet(name = "SvRegistro", urlPatterns = {"/SvRegistro"}) // cuando llegue una petición a /SvRegistro, Tomcat ejecuta esta clase
public class SvRegistro extends HttpServlet {            // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando el formulario de registro envía POST
            throws ServletException, IOException {       // declara las excepciones que puede lanzar

        request.setCharacterEncoding("UTF-8");           // fuerza UTF-8 para leer correctamente tildes y ñ del formulario

        // Leer todos los campos del formulario de registro
        String nombre      = request.getParameter("nombre");               // primer nombre del usuario
        String apellido    = request.getParameter("apellido");             // apellido del usuario
        String correo      = request.getParameter("correo_electronico");   // correo que usará para iniciar sesión
        String contrasena  = request.getParameter("contrasena");           // contraseña elegida
        String confirmar   = request.getParameter("confirmar_contrasena"); // repetición de la contraseña para verificar
        String direccion   = request.getParameter("direccion");            // dirección de entrega del cliente
        String fechaNac    = request.getParameter("fecha_nacimiento");     // fecha en formato YYYY-MM-DD

        // Normalizar: quitar espacios al inicio/fin y convertir correo a minúsculas
        if (nombre   != null) nombre   = nombre.trim();                    // elimina espacios sobrantes del nombre
        if (apellido != null) apellido = apellido.trim();                  // elimina espacios sobrantes del apellido
        if (correo   != null) correo   = correo.trim().toLowerCase();      // normaliza el correo para evitar duplicados por mayúsculas
        if (direccion != null) direccion = direccion.trim();               // elimina espacios sobrantes de la dirección

        // 1. Validación: campos obligatorios vacíos
        // Si cualquiera de los 7 campos está null o en blanco, rechazar y mostrar error en el JSP
        if (nombre == null || nombre.isBlank() ||
            apellido == null || apellido.isBlank() ||
            correo == null || correo.isBlank() ||
            contrasena == null || contrasena.isBlank() ||
            confirmar == null || confirmar.isBlank() ||
            direccion == null || direccion.isBlank() ||
            fechaNac == null || fechaNac.isBlank()) {
            request.setAttribute("error", "Todos los campos son obligatorios."); // guarda el mensaje de error como atributo
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response); // reenvía al JSP mostrando el error
            return;                                      // detiene la ejecución; la respuesta ya fue enviada
        }

        // 2. Validación: nombre solo letras y espacios (mín 2, máx 50 caracteres)
        // La regex admite tildes, ñ y Ñ porque los nombres en español los pueden tener
        if (!nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]{2,50}")) { // expresión regular que valida el nombre
            request.setAttribute("error", "El nombre solo puede contener letras (mínimo 2 caracteres).");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 3. Validación: apellido solo letras y espacios (mín 2, máx 50 caracteres)
        if (!apellido.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]{2,50}")) { // misma regex que el nombre
            request.setAttribute("error", "El apellido solo puede contener letras (mínimo 2 caracteres).");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 4. Validación: formato de correo electrónico con expresión regular estándar
        // Acepta: letras, números, puntos, guiones, subguiones, el @, y el dominio con TLD de 2+ letras
        if (!Pattern.matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$", correo)) { // regex de correo
            request.setAttribute("error", "El correo electrónico no tiene un formato válido.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 5. Validación: longitud de contraseña entre 8 y 20 caracteres
        if (contrasena.length() < 8 || contrasena.length() > 20) { // rango de longitud permitido
            request.setAttribute("error", "La contraseña debe tener entre 8 y 20 caracteres.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 6. Validación: la contraseña debe tener al menos 1 letra y 1 número
        // Esto previene contraseñas triviales como "12345678" o "abcdefgh"
        if (!contrasena.matches(".*[a-zA-Z].*") || !contrasena.matches(".*[0-9].*")) { // regex para verificar mezcla
            request.setAttribute("error", "La contraseña debe contener al menos una letra y un número.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 7. Validación: las dos contraseñas ingresadas deben ser idénticas
        if (!contrasena.equals(confirmar)) {             // comparación exacta (case-sensitive)
            request.setAttribute("error", "Las contraseñas no coinciden.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 8. Validación: fecha de nacimiento válida y edad mínima de 18 años
        try {
            LocalDate nacimiento = LocalDate.parse(fechaNac); // parsear YYYY-MM-DD; lanza DateTimeParseException si es inválido
            if (nacimiento.isAfter(LocalDate.now())) {   // no puede ser una fecha en el futuro
                request.setAttribute("error", "La fecha de nacimiento no puede ser una fecha futura.");
                request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
                return;
            }
            int edad = Period.between(nacimiento, LocalDate.now()).getYears(); // calcula la edad en años completos
            if (edad < 18) {                             // menor de 18 años no puede registrarse
                request.setAttribute("error", "Debes tener al menos 18 años para crear una cuenta.");
                request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
                return;
            }
            if (edad > 120) {                            // más de 120 años es una fecha irreal
                request.setAttribute("error", "La fecha de nacimiento ingresada no es válida.");
                request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
                return;
            }
        } catch (DateTimeParseException e) {             // si la fecha no tiene el formato YYYY-MM-DD esperado
            request.setAttribute("error", "La fecha de nacimiento no tiene un formato válido.");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        // 9. Validación: la dirección debe ser suficientemente descriptiva (mínimo 10 caracteres)
        if (direccion.length() < 10) {                   // direcciones muy cortas no son útiles para entregas
            request.setAttribute("error", "La dirección debe ser más específica (mínimo 10 caracteres).");
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
            return;
        }

        try {
            UsuarioJpaController usuarioCtrl = new UsuarioJpaController(); // controlador para operaciones con la tabla usuario
            ClienteJpaController clienteCtrl = new ClienteJpaController(); // controlador para operaciones con la tabla cliente
            RolJpaController rolCtrl         = new RolJpaController();     // controlador para buscar roles en la tabla rol

            // Verificar que el correo no esté registrado ya (sin importar mayúsculas/minúsculas)
            // Se carga todos los usuarios y se compara localmente porque la BD puede no tener función LOWER accesible
            for (logica.Usuario u : usuarioCtrl.findUsuarioEntities()) { // SELECT * FROM usuario
                if (u.getCorreoUsuario().equalsIgnoreCase(correo.trim())) { // comparación insensible a mayúsculas
                    request.setAttribute("error", "Ya existe una cuenta con ese correo electrónico.");
                    request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response);
                    return;
                }
            }

            // Unir nombre y apellido en un solo campo "nombre completo" como lo espera la entidad Cliente
            String nombreCompleto = nombre.trim() + " " + (apellido != null ? apellido.trim() : ""); // ej: "Juan Pérez"

            // Crear el registro de Cliente (datos personales del comprador: nombre, dirección)
            Cliente cliente = new Cliente();             // objeto nuevo que representará la fila en la tabla cliente
            cliente.setNombreCompleto(nombreCompleto.trim()); // nombre completo del comprador
            cliente.setDireccion(direccion);             // dirección de entrega
            cliente.setActivo(true);                     // el cliente queda activo por defecto
            cliente.setCreatedAt(LocalDateTime.now());   // fecha y hora de creación
            cliente.setUpdatedAt(LocalDateTime.now());   // fecha y hora de última modificación (igual a createdAt al crear)
            clienteCtrl.create(cliente);                 // INSERT INTO cliente → MySQL le asigna el id_cliente con AUTO_INCREMENT

            // Buscar el rol CLIENTE en BD; si no existe (porque no fue creado en el SQL inicial), crearlo
            // Normalmente ya existe como dato semilla en el SQL de inicialización de la BD
            Rol rolCliente = null;                       // inicializar en null para detectar si no se encontró
            for (Rol r : rolCtrl.findRolEntities()) {    // SELECT * FROM rol → buscar el rol llamado "CLIENTE"
                if ("CLIENTE".equalsIgnoreCase(r.getNombreRol())) { // comparación insensible a mayúsculas
                    rolCliente = r;                      // encontrado: guardar referencia al objeto
                    break;                               // detener la búsqueda
                }
            }
            if (rolCliente == null) {                    // si el rol CLIENTE no existe en la BD...
                rolCliente = new Rol();                  // ...crearlo para no bloquear el registro del usuario
                rolCliente.setNombreRol("CLIENTE");      // nombre estándar del rol
                rolCliente.setActivo(true);              // el rol queda activo
                rolCtrl.create(rolCliente);              // INSERT INTO rol
            }

            // Crear el Usuario (cuenta de acceso) vinculado al cliente y al rol
            Usuario usuario = new Usuario();             // objeto nuevo que representará la fila en la tabla usuario
            usuario.setCorreoUsuario(correo);            // correo normalizado (minúsculas, sin espacios)
            usuario.setContrasena(contrasena);           // contraseña en texto plano (sin hash, diseño actual del proyecto)
            usuario.setCliente(cliente);                 // FK id_cliente → vincula la cuenta con los datos personales
            usuario.setRol(rolCliente);                  // FK id_rol → rol CLIENTE (sin permisos de panel admin)
            usuario.setActivo(true);                     // la cuenta queda activa inmediatamente
            usuario.setCreatedAt(LocalDateTime.now());   // fecha de creación de la cuenta
            usuario.setUpdatedAt(LocalDateTime.now());   // fecha de última modificación
            usuarioCtrl.create(usuario);                 // INSERT INTO usuario → MySQL le asigna el id_usuario

            // Guardar mensaje de éxito en sesión para mostrarlo en perfil.jsp tras el redirect
            // Se usa la sesión porque sendRedirect inicia una nueva petición HTTP y los atributos de request se pierden
            HttpSession session = request.getSession(); // obtener o crear la sesión HTTP del usuario
            session.setAttribute("registroExitoso", "¡Cuenta creada exitosamente! Ahora inicia sesión."); // mensaje que verá el usuario

            response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp"); // redirigir al login con el mensaje de éxito

        } catch (Exception e) {
            // Si algo falla en BD (ej: correo duplicado por race condition, error de conexión)
            request.setAttribute("error", "Ocurrió un error al crear la cuenta. Intenta de nuevo."); // mensaje genérico de error
            request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response); // devolver al formulario de registro
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando el usuario entra a /SvRegistro por URL directa
            throws ServletException, IOException {
        request.getRequestDispatcher("/vistas/registro.jsp").forward(request, response); // reenvía al JSP de registro para mostrar el formulario
    }
}
