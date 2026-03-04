package servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Marca;
import persistencias.JpaProvider;
import persistencias.MarcaJpaController;

public class SvMarcas extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            List<Marca> lista = new MarcaJpaController().findMarcaEntities();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Marca m = lista.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                String paginaUrl = m.getPaginaUrl() != null && !m.getPaginaUrl().isEmpty()
                    ? m.getPaginaUrl()
                    : m.getNombreMarca().trim().toLowerCase().replace(" ", "_").replaceAll("[^a-z0-9_]", "") + ".jsp";
                sb.append("\"id\":").append(m.getIdMarca()).append(",");
                sb.append("\"nombre\":\"").append(escapeJson(m.getNombreMarca())).append("\",");
                sb.append("\"descripcion\":\"").append(escapeJson(m.getDescripcion())).append("\",");
                sb.append("\"genero\":\"").append(escapeJson(m.getGenero())).append("\",");
                sb.append("\"pagina\":\"").append(escapeJson(paginaUrl)).append("\",");
                sb.append("\"activo\":").append(m.isActivo()).append(",");
                sb.append("\"productos\":").append(m.getProductos() != null ? m.getProductos().size() : 0);
                sb.append("}");
            }
            sb.append("]");
            out.print(sb.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        EntityManager em = null;
        try {
            String accion = request.getParameter("accion");
            MarcaJpaController ctrl = new MarcaJpaController();

            if ("eliminar".equals(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                em = JpaProvider.getEntityManagerFactory().createEntityManager();
                TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(p) FROM Producto p WHERE p.marca.idMarca = :id", Long.class);
                q.setParameter("id", id);
                long count = q.getSingleResult();
                if (count > 0) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"error\":\"No se puede eliminar: tiene " + count + " producto(s) vinculado(s)\"}");
                    return;
                }
                ctrl.destroy(id);
                out.print("{\"ok\":true}");
                return;
            }

            String nombre      = request.getParameter("nombre");
            String descripcion = request.getParameter("descripcion");
            String idStr       = request.getParameter("id");

            if (nombre == null || nombre.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"El nombre es obligatorio\"}");
                return;
            }

            em = JpaProvider.getEntityManagerFactory().createEntityManager();
            TypedQuery<Long> dupQ = em.createQuery(
                "SELECT COUNT(m) FROM Marca m WHERE LOWER(m.nombreMarca) = LOWER(:n)" +
                (idStr != null ? " AND m.idMarca <> :id" : ""), Long.class);
            dupQ.setParameter("n", nombre.trim());
            if (idStr != null) dupQ.setParameter("id", Integer.parseInt(idStr));
            if (dupQ.getSingleResult() > 0) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"error\":\"Ya existe una marca con ese nombre\"}");
                return;
            }

            if ("editar".equals(accion) && idStr != null) {
                Marca marca = ctrl.findMarca(Integer.parseInt(idStr));
                marca.setNombreMarca(nombre);
                marca.setDescripcion(descripcion);
                ctrl.edit(marca);
            } else if ("desactivar".equals(accion) && idStr != null) {
                Marca marca = ctrl.findMarca(Integer.parseInt(idStr));
                em.close(); em = null;
                em = JpaProvider.getEntityManagerFactory().createEntityManager();
                TypedQuery<Long> qp = em.createQuery(
                    "SELECT COUNT(p) FROM Producto p WHERE p.marca.idMarca = :id AND p.activo = true", Long.class);
                qp.setParameter("id", Integer.parseInt(idStr));
                if (marca.isActivo() && qp.getSingleResult() > 0) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"error\":\"No se puede desactivar: tiene productos activos vinculados\"}");
                    return;
                }
                marca.setActivo(!marca.isActivo());
                ctrl.edit(marca);
            } else {
                String genero = request.getParameter("genero");
                if (genero == null || genero.isBlank()) genero = "HOMBRE";
                String nombreJsp = nombre.trim().toLowerCase()
                    .replace(" ", "_")
                    .replaceAll("[^a-z0-9_]", "") + ".jsp";
                Marca marca = new Marca();
                marca.setNombreMarca(nombre);
                marca.setDescripcion(descripcion);
                marca.setGenero(genero);
                marca.setPaginaUrl(nombreJsp);
                marca.setActivo(true);
                ctrl.create(marca);

                // Generar JSP físico para la nueva marca
                String rutaVistas = request.getServletContext().getRealPath("/vistas/");
                File jspFile = new File(rutaVistas, nombreJsp);
                if (!jspFile.exists()) {
                    generarJspMarca(jspFile, nombre, descripcion != null ? descripcion : "");
                }
            }
            out.print("{\"ok\":true}");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    private void generarJspMarca(File jspFile, String nombre, String descripcion) throws Exception {
        String tituloUpper = nombre.toUpperCase();
        StringBuilder sb = new StringBuilder();
        sb.append("<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\"%>\n");
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("    <link rel=\"stylesheet\" href=\"../assets/estilos/style.css\">\n");
        sb.append("    <link rel=\"stylesheet\" href=\"../assets/estilos/cart.css\">\n");
        sb.append("    <title>").append(nombre).append("</title>\n</head>\n<body>\n");
        sb.append("    <%@ include file=\"_navbar.jsp\" %>\n");
        sb.append("    <main class=\"main-losion\">\n");
        sb.append("        <section class=\"section-losion\">\n");
        sb.append("            <h2 class=\"section-losion__title\">").append(tituloUpper).append("</h2>\n");
        sb.append("            <img class=\"section-losion__img\" src=\"../assets/imagenes/Imagen de la losion.webp\" alt=\"").append(nombre).append("\">\n");
        sb.append("            <div class=\"section-losion__description\">\n");
        sb.append("                <h2 class=\"section-losion__description__title\">").append(descripcion.isEmpty() ? nombre : descripcion).append("</h2>\n");
        sb.append("            </div>\n        </section>\n");
        sb.append("        <section class=\"cards-lociones\" id=\"marca-cards\">\n");
        sb.append("        </section>\n    </main>\n");
        // Script que carga los productos de esta marca dinámicamente
        sb.append("    <script>\n");
        sb.append("    (function() {\n");
        sb.append("        var MARCA_NOMBRE = '").append(nombre.replace("'", "\\'")).append("';\n");
        sb.append("        var ctx = (function() { var p = window.location.pathname.split('/'); return '/' + p[1]; })();\n");
        sb.append("        function fmt(n) { return parseFloat(n).toLocaleString('es-CO') + ' COP'; }\n");
        sb.append("        fetch(ctx + '/SvProductos', { credentials: 'same-origin' })\n");
        sb.append("            .then(function(r) { return r.json(); })\n");
        sb.append("            .then(function(productos) {\n");
        sb.append("                if (!Array.isArray(productos)) return;\n");
        sb.append("                var filtrados = productos.filter(function(p) {\n");
        sb.append("                    return p.activo && p.marca && p.marca.toUpperCase() === MARCA_NOMBRE.toUpperCase();\n");
        sb.append("                });\n");
        sb.append("                if (filtrados.length === 0) return;\n");
        sb.append("                var section = document.getElementById('marca-cards');\n");
        sb.append("                filtrados.forEach(function(p) {\n");
        sb.append("                    var imgSrc = p.imagenUrl && p.imagenUrl.trim() !== ''\n");
        sb.append("                        ? p.imagenUrl : ctx + '/assets/imagenes/Imagen de la losion.webp';\n");
        sb.append("                    var art = document.createElement('article');\n");
        sb.append("                    art.className = 'card';\n");
        sb.append("                    art.innerHTML =\n");
        sb.append("                        '<a href=\"detalle.jsp?nombre=' + encodeURIComponent(p.nombre) + '\">' +\n");
        sb.append("                            '<img class=\"card__img\" src=\"' + imgSrc + '\" alt=\"' + p.nombre + '\">' +\n");
        sb.append("                        '</a>' +\n");
        sb.append("                        '<div class=\"card__content\">' +\n");
        sb.append("                            '<h2 class=\"card__title\">' + p.nombre.toUpperCase() + '</h2>' +\n");
        sb.append("                            '<h3 class=\"card__subtitle\">Perfume</h3>' +\n");
        sb.append("                            '<p class=\"card__description\">' + (p.descripcion || '') + '</p>' +\n");
        sb.append("                            '<p class=\"card__price\">' + fmt(p.precio) + '</p>' +\n");
        sb.append("                        '</div>';\n");
        sb.append("                    section.appendChild(art);\n");
        sb.append("                });\n");
        sb.append("            })\n");
        sb.append("            .catch(function() {});\n");
        sb.append("    })();\n");
        sb.append("    </script>\n");
        sb.append("    <%@ include file=\"_footer.jsp\" %>\n");
        sb.append("    <script src=\"../assets/scripts/cart.js\"></script>\n");
        sb.append("</body>\n</html>\n");

        jspFile.getParentFile().mkdirs();
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(jspFile), StandardCharsets.UTF_8)) {
            w.write(sb.toString());
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
