package servlets;                                        // declara que esta clase pertenece al paquete "servlets"

import java.io.File;                                     // representa un archivo en el sistema de archivos
import java.io.FileOutputStream;                         // flujo de bytes para escribir archivos en disco
import java.io.IOException;                              // excepción de entrada/salida
import java.io.OutputStreamWriter;                       // convierte un flujo de bytes en un escritor de caracteres con un charset específico
import java.io.PrintWriter;                              // escribe texto en la respuesta HTTP
import java.nio.charset.StandardCharsets;                // constantes para charsets estándar (UTF-8, ISO-8859-1, etc.)
import java.util.List;                                   // lista de marcas
import javax.persistence.EntityManager;                  // gestiona la sesión activa con la BD
import javax.persistence.TypedQuery;                     // consulta JPQL con tipo de retorno definido
import javax.servlet.ServletException;                   // excepción propia de los servlets
import javax.servlet.http.HttpServlet;                   // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;            // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;           // representa la respuesta HTTP saliente
import logica.Marca;                                     // entidad que representa una marca de perfumes
import persistencias.JpaProvider;                        // Singleton que provee la EntityManagerFactory
import persistencias.MarcaJpaController;                 // controlador para CRUD en la tabla marca

/**
 * SvMarcas — Servlet de gestión de marcas de productos.
 * GET: retorna la lista de todas las marcas con id, nombre, género, páginaUrl, activo y conteo de productos.
 * POST accion=eliminar: eliminación física. Bloqueada si la marca tiene productos vinculados.
 * POST accion=editar: actualiza nombre y descripción de una marca existente.
 * POST accion=desactivar: activa/desactiva una marca (toggle). Bloqueado si tiene productos activos.
 * POST sin accion: crea la marca en BD y genera automáticamente un archivo JSP físico en web/vistas/.
 * Todas las operaciones POST requieren el permiso GESTIONAR_MARCAS.
 */
public class SvMarcas extends HttpServlet {              // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando admin.js hace GET a /SvMarcas
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8"); // respuesta JSON en UTF-8
        PrintWriter out = response.getWriter();          // escritor de la respuesta

        EntityManager em = null;                         // declara em en null para cerrarlo en el finally
        try {
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión con la BD para contar productos por marca
            List<Marca> lista = new MarcaJpaController().findMarcaEntities(); // SELECT * FROM marca ORDER BY id_marca ASC
            StringBuilder sb = new StringBuilder("[");   // inicia el array JSON
            for (int i = 0; i < lista.size(); i++) {     // iterar sobre cada marca
                Marca m = lista.get(i);                  // marca actual
                if (i > 0) sb.append(",");               // separador entre objetos del array
                sb.append("{");
                // Si la marca tiene paginaUrl guardada se usa directamente
                // Si no, se genera dinámicamente desde el nombre: ej. "Paco Rabanne" → "paco_rabanne.jsp"
                String paginaUrl = m.getPaginaUrl() != null && !m.getPaginaUrl().isEmpty()
                    ? m.getPaginaUrl()                   // usar la URL guardada en BD
                    : m.getNombreMarca().trim().toLowerCase().replace(" ", "_").replaceAll("[^a-z0-9_]", "") + ".jsp"; // generar desde el nombre
                // Contar productos con JPQL para evitar que la carga lazy de colecciones cause errores
                long numProductos = em.createQuery(
                    "SELECT COUNT(p) FROM Producto p WHERE p.marca.idMarca = :id", Long.class) // cuenta productos de esta marca
                    .setParameter("id", m.getIdMarca()).getSingleResult(); // filtra por el ID de esta marca
                sb.append("\"id\":").append(m.getIdMarca()).append(",");                          // ID de la marca
                sb.append("\"nombre\":\"").append(escapeJson(m.getNombreMarca())).append("\","); // nombre de la marca
                sb.append("\"descripcion\":\"").append(escapeJson(m.getDescripcion())).append("\","); // descripción opcional
                sb.append("\"genero\":\"").append(escapeJson(m.getGenero())).append("\",");      // "HOMBRE" o "MUJER"
                sb.append("\"pagina\":\"").append(escapeJson(paginaUrl)).append("\",");          // ruta del JSP de esta marca
                sb.append("\"activo\":").append(m.isActivo()).append(",");                        // true/false si está activa
                sb.append("\"productos\":").append(numProductos);                                // cantidad de productos vinculados
                sb.append("}");
            }
            sb.append("]");                              // cierra el array JSON
            out.print(sb.toString());                    // envía el JSON al navegador

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();   // SIEMPRE cierra el EntityManager
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando admin.js hace POST a /SvMarcas
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");           // fuerza UTF-8 para leer caracteres especiales
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        EntityManager em = null;
        try {
            // Verificar permiso antes de cualquier operación de escritura
            if (!AuthHelper.tienePermiso(request, "GESTIONAR_MARCAS")) { // consulta la sesión HTTP
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);    // HTTP 403
                out.print("{\"error\":\"Sin permiso: GESTIONAR_MARCAS\"}");
                return;
            }

            String accion = request.getParameter("accion"); // "eliminar", "editar", "desactivar" o null (crear)
            MarcaJpaController ctrl = new MarcaJpaController(); // controlador para CRUD de marcas

            if ("eliminar".equals(accion)) {
                // ELIMINAR: bloquear si la marca tiene productos vinculados
                // No se puede eliminar una marca que tenga productos activos o inactivos
                int id = Integer.parseInt(request.getParameter("id")); // ID de la marca a eliminar
                em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión para el COUNT
                TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(p) FROM Producto p WHERE p.marca.idMarca = :id", Long.class); // cuenta productos de esta marca
                q.setParameter("id", id);
                long count = q.getSingleResult();        // número de productos vinculados (activos e inactivos)
                if (count > 0) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT); // HTTP 409: conflicto
                    out.print("{\"error\":\"No se puede eliminar: tiene " + count + " producto(s) vinculado(s)\"}");
                    return;
                }
                ctrl.destroy(id);                        // DELETE FROM marca WHERE id_marca = ? (solo si no hay productos)
                out.print("{\"ok\":true}");
                return;
            }

            String nombre      = request.getParameter("nombre");      // nombre de la marca (obligatorio)
            String descripcion = request.getParameter("descripcion"); // descripción opcional (máx 120 chars)
            String idStr       = request.getParameter("id");          // ID para editar o desactivar

            // Validar campo obligatorio
            if (nombre == null || nombre.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"El nombre es obligatorio\"}");
                return;
            }
            // Validar longitud de la descripción
            if (descripcion != null && descripcion.length() > 120) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"La descripci\\u00f3n no puede superar los 120 caracteres\"}");
                return;
            }

            // Verificar que no exista otra marca con el mismo nombre (al editar se excluye la propia)
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión para chequeo de duplicados
            TypedQuery<Long> dupQ = em.createQuery(
                "SELECT COUNT(m) FROM Marca m WHERE LOWER(m.nombreMarca) = LOWER(:n)" +
                (idStr != null ? " AND m.idMarca <> :id" : ""), Long.class); // excluir la propia marca al editar
            dupQ.setParameter("n", nombre.trim());
            if (idStr != null) dupQ.setParameter("id", Integer.parseInt(idStr)); // ID de la marca actual (para excluirla del chequeo)
            if (dupQ.getSingleResult() > 0) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"error\":\"Ya existe una marca con ese nombre\"}");
                return;
            }

            if ("editar".equals(accion) && idStr != null) {
                // EDITAR: actualizar nombre y descripción (no se cambia el JSP físico ya generado)
                Marca marca = ctrl.findMarca(Integer.parseInt(idStr)); // SELECT * FROM marca WHERE id_marca = ?
                marca.setNombreMarca(nombre);            // nuevo nombre
                marca.setDescripcion(descripcion);       // nueva descripción
                ctrl.edit(marca);                        // UPDATE marca SET nombre_marca=?, descripcion=? WHERE id_marca=?
            } else if ("desactivar".equals(accion) && idStr != null) {
                // DESACTIVAR: no permitir si la marca tiene productos activos (rompería el catálogo visible)
                Marca marca = ctrl.findMarca(Integer.parseInt(idStr)); // cargar la marca a desactivar
                em.close(); em = null;                   // cerrar el em de duplicados y abrir uno nuevo para la consulta
                em = JpaProvider.getEntityManagerFactory().createEntityManager();
                TypedQuery<Long> qp = em.createQuery(
                    "SELECT COUNT(p) FROM Producto p WHERE p.marca.idMarca = :id AND p.activo = true", Long.class); // cuenta solo productos activos
                qp.setParameter("id", Integer.parseInt(idStr));
                if (marca.isActivo() && qp.getSingleResult() > 0) { // solo bloquear si SE INTENTA DESACTIVAR una marca con productos activos
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"error\":\"No se puede desactivar: tiene productos activos vinculados\"}");
                    return;
                }
                marca.setActivo(!marca.isActivo());      // toggle: activa→inactiva o inactiva→activa
                ctrl.edit(marca);                        // UPDATE marca SET activo=? WHERE id_marca=?
            } else {
                // CREAR: registrar la marca en BD y generar automáticamente su archivo JSP en disco
                String genero = request.getParameter("genero"); // "HOMBRE" o "MUJER" (obligatorio para las páginas de marcas)
                if (genero == null || genero.isBlank()) genero = "HOMBRE"; // valor por defecto si no se especifica

                // Generar el nombre del archivo JSP a partir del nombre de la marca
                // Se eliminan acentos, se convierten espacios en _ y se quitan caracteres no alfanuméricos
                // Ejemplo: "Paco Rabanne" → "paco_rabanne.jsp"
                String nombreJsp = nombre.trim().toLowerCase()
                    .replace(" ", "_")                   // espacios → guiones bajos
                    .replaceAll("[^a-z0-9_]", "") + ".jsp"; // quitar caracteres no permitidos en nombres de archivo

                Marca marca = new Marca();               // objeto nuevo para la nueva marca
                marca.setNombreMarca(nombre);            // nombre que ingresó el admin
                marca.setDescripcion(descripcion);       // descripción opcional
                marca.setGenero(genero);                 // "HOMBRE" o "MUJER"
                marca.setPaginaUrl(nombreJsp);           // ruta relativa del JSP dentro de web/vistas/
                marca.setActivo(true);                   // la nueva marca se crea activa
                ctrl.create(marca);                      // INSERT INTO marca (nombre_marca, descripcion, genero, pagina_url, activo) VALUES (...)

                // Generar el archivo JSP físico en el servidor si no existe ya
                // getRealPath convierte la ruta relativa "/vistas/" a la ruta absoluta en disco
                String rutaVistas = request.getServletContext().getRealPath("/vistas/"); // ruta absoluta de la carpeta vistas en el servidor
                File jspFile = new File(rutaVistas, nombreJsp); // construye la ruta completa del archivo JSP a crear
                if (!jspFile.exists()) {                 // solo crear si no existe ya (para no sobreescribir ediciones manuales)
                    generarJspMarca(jspFile, nombre, descripcion != null ? descripcion : ""); // genera el contenido HTML del JSP
                }
            }
            out.print("{\"ok\":true}");                  // respuesta de éxito al frontend

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();   // SIEMPRE cierra el EntityManager
        }
    }

    // Genera el contenido HTML del archivo JSP para la página de una marca
    // El JSP resultante muestra los productos de esa marca cargados dinámicamente con fetch()
    private void generarJspMarca(File jspFile, String nombre, String descripcion) throws Exception {
        String tituloUpper = nombre.toUpperCase();       // título de la marca en mayúsculas para el encabezado
        StringBuilder sb = new StringBuilder();
        sb.append("<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\"%>\n"); // directiva JSP de codificación
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("    <link rel=\"stylesheet\" href=\"../assets/estilos/style.css\">\n");  // hoja de estilos principal
        sb.append("    <link rel=\"stylesheet\" href=\"../assets/estilos/cart.css\">\n");   // estilos del carrito de compras
        sb.append("    <title>").append(nombre).append("</title>\n</head>\n<body>\n");
        sb.append("    <%@ include file=\"_navbar.jsp\" %>\n"); // barra de navegación compartida
        sb.append("    <main class=\"main-losion\">\n");
        sb.append("        <section class=\"section-losion\">\n");
        sb.append("            <h2 class=\"section-losion__title\">").append(tituloUpper).append("</h2>\n"); // nombre de la marca en grande
        sb.append("            <img class=\"section-losion__img\" src=\"../assets/imagenes/Imagen de la losion.webp\" alt=\"").append(nombre).append("\">\n"); // imagen representativa
        sb.append("            <div class=\"section-losion__description\">\n");
        sb.append("                <h2 class=\"section-losion__description__title\">").append(descripcion.isEmpty() ? nombre : descripcion).append("</h2>\n"); // descripción o nombre si no hay descripción
        sb.append("            </div>\n        </section>\n");
        sb.append("        <section class=\"cards-lociones\" id=\"marca-cards\">\n"); // contenedor donde el JS insertará las tarjetas de productos
        sb.append("        </section>\n    </main>\n");
        // Script que carga los productos de esta marca dinámicamente desde SvProductos
        sb.append("    <script>\n");
        sb.append("    (function() {\n");               // IIFE para no contaminar el scope global
        sb.append("        var MARCA_NOMBRE = '").append(nombre.replace("'", "\\'")).append("';\n"); // nombre de la marca como constante JS
        sb.append("        var ctx = (function() { var p = window.location.pathname.split('/'); return '/' + p[1]; })();\n"); // extrae el context path ("/Proyecto")
        sb.append("        function fmt(n) { return parseFloat(n).toLocaleString('es-CO') + ' COP'; }\n"); // formatea precio en pesos colombianos
        sb.append("        fetch(ctx + '/SvProductos', { credentials: 'same-origin' })\n"); // pide todos los productos activos al servidor
        sb.append("            .then(function(r) { return r.json(); })\n");
        sb.append("            .then(function(productos) {\n");
        sb.append("                if (!Array.isArray(productos)) return;\n"); // si la respuesta no es un array, no hacer nada
        sb.append("                var filtrados = productos.filter(function(p) {\n"); // filtrar solo los productos de esta marca
        sb.append("                    return p.activo && p.marca && p.marca.toUpperCase() === MARCA_NOMBRE.toUpperCase();\n"); // comparación insensible a mayúsculas
        sb.append("                });\n");
        sb.append("                if (filtrados.length === 0) return;\n"); // si no hay productos, no crear tarjetas
        sb.append("                var section = document.getElementById('marca-cards');\n"); // contenedor HTML donde se insertan las tarjetas
        sb.append("                filtrados.forEach(function(p) {\n"); // crear una tarjeta HTML por cada producto
        sb.append("                    var imgSrc = p.imagenUrl && p.imagenUrl.trim() !== ''\n");
        sb.append("                        ? p.imagenUrl : ctx + '/assets/imagenes/Imagen de la losion.webp';\n"); // imagen del producto o la imagen por defecto
        sb.append("                    var art = document.createElement('article');\n");
        sb.append("                    art.className = 'card';\n");
        sb.append("                    art.innerHTML =\n");
        sb.append("                        '<a href=\"detalle.jsp?nombre=' + encodeURIComponent(p.nombre) + '\">' +\n"); // enlace a la página de detalle del producto
        sb.append("                            '<img class=\"card__img\" src=\"' + imgSrc + '\" alt=\"' + p.nombre + '\">' +\n");
        sb.append("                        '</a>' +\n");
        sb.append("                        '<div class=\"card__content\">' +\n");
        sb.append("                            '<h2 class=\"card__title\">' + p.nombre.toUpperCase() + '</h2>' +\n");
        sb.append("                            '<h3 class=\"card__subtitle\">Perfume</h3>' +\n");
        sb.append("                            '<p class=\"card__description\">' + (p.descripcion || '') + '</p>' +\n");
        sb.append("                            '<p class=\"card__price\">' + fmt(p.precio) + '</p>' +\n");
        sb.append("                        '</div>';\n");
        sb.append("                    section.appendChild(art);\n"); // insertar la tarjeta en la sección
        sb.append("                });\n");
        sb.append("            })\n");
        sb.append("            .catch(function() {});\n"); // captura errores silenciosamente (el usuario no ve error de JS)
        sb.append("    })();\n");
        sb.append("    </script>\n");
        sb.append("    <%@ include file=\"_footer.jsp\" %>\n"); // pie de página compartido
        sb.append("    <script src=\"../assets/scripts/cart.js\"></script>\n"); // script del carrito de compras
        sb.append("</body>\n</html>\n");

        jspFile.getParentFile().mkdirs();                // crea las carpetas intermedias si no existen (por si vistas/ no existe)
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(jspFile), StandardCharsets.UTF_8)) { // escribe el archivo en UTF-8
            w.write(sb.toString());                      // escribe el contenido HTML generado en el archivo .jsp
        }
    }

    // Método auxiliar que escapa caracteres especiales para JSON
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
