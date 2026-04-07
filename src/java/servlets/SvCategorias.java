package servlets;                                        // declara que esta clase pertenece al paquete "servlets"

import java.io.IOException;                              // excepción de entrada/salida (requerida por doGet/doPost)
import java.io.PrintWriter;                              // escribe texto en la respuesta HTTP
import java.util.List;                                   // lista de categorías
import javax.persistence.EntityManager;                  // gestiona la sesión activa con la BD
import javax.persistence.TypedQuery;                     // consulta JPQL con tipo de retorno definido
import javax.servlet.ServletException;                   // excepción propia de los servlets
import javax.servlet.http.HttpServlet;                   // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;            // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;           // representa la respuesta HTTP saliente
import logica.Categoria;                                 // entidad que representa una categoría de productos
import persistencias.CategoriaJpaController;             // controlador para CRUD en la tabla categoria
import persistencias.JpaProvider;                        // Singleton que provee la EntityManagerFactory

/**
 * SvCategorias — Servlet de gestión de categorías de productos.
 * GET: retorna la lista de todas las categorías en formato JSON con el conteo de productos vinculados.
 * POST accion=eliminar: eliminación física. Bloqueada si la categoría tiene productos.
 * POST accion=editar: actualiza nombre y descripción de una categoría existente.
 * POST accion=desactivar: activa o desactiva una categoría (toggle).
 * POST sin accion: crea una categoría nueva.
 * Todas las operaciones POST requieren el permiso GESTIONAR_CATEGORIAS.
 */
public class SvCategorias extends HttpServlet {          // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando admin.js hace GET a /SvCategorias
            throws ServletException, IOException {       // declara las excepciones que puede lanzar

        response.setContentType("application/json;charset=UTF-8"); // indica al navegador que la respuesta es JSON en UTF-8
        PrintWriter out = response.getWriter();          // obtiene el escritor de la respuesta

        try {
            List<Categoria> lista = new CategoriaJpaController().findCategoriaEntities(); // SELECT * FROM categoria ORDER BY id_categoria ASC
            StringBuilder sb = new StringBuilder("[");   // inicia el array JSON de categorías
            for (int i = 0; i < lista.size(); i++) {     // iterar sobre cada categoría
                Categoria c = lista.get(i);              // categoría actual
                if (i > 0) sb.append(",");               // separador entre objetos del array (no antes del primero)
                sb.append("{");
                sb.append("\"id\":").append(c.getIdCategoria()).append(",");                      // ID de la categoría
                sb.append("\"nombre\":\"").append(escapeJson(c.getNombreCategoria())).append("\","); // nombre de la categoría
                sb.append("\"descripcion\":\"").append(escapeJson(c.getDescripcion())).append("\","); // descripción opcional
                sb.append("\"activo\":").append(c.isActivo()).append(",");                        // true/false si está activa
                // Contar cuántos productos están vinculados a esta categoría
                // Se usa para bloquear la eliminación si tiene productos (integridad referencial)
                sb.append("\"productos\":").append(c.getProductos() != null ? c.getProductos().size() : 0); // cantidad de productos de esta categoría
                sb.append("}");                          // cierra el objeto de esta categoría
            }
            sb.append("]");                              // cierra el array JSON
            out.print(sb.toString());                    // envía el JSON al navegador

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // HTTP 500
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");  // error genérico
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando admin.js hace POST a /SvCategorias
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");           // fuerza UTF-8 para leer caracteres especiales del formulario
        response.setContentType("application/json;charset=UTF-8"); // respuesta JSON
        PrintWriter out = response.getWriter();          // escritor de la respuesta

        EntityManager em = null;                         // declara em en null para cerrarlo en el finally si se abrió
        try {
            // Verificar permiso antes de realizar cualquier operación de escritura
            if (!AuthHelper.tienePermiso(request, "GESTIONAR_CATEGORIAS")) { // lee los permisos de la sesión HTTP
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);        // HTTP 403: acceso denegado
                out.print("{\"error\":\"Sin permiso: GESTIONAR_CATEGORIAS\"}");
                return;
            }

            String accion = request.getParameter("accion"); // "eliminar", "editar", "desactivar" o null (crear)
            CategoriaJpaController ctrl = new CategoriaJpaController(); // controlador para CRUD de categorías

            if ("eliminar".equals(accion)) {
                // ELIMINAR: verificar primero que no haya productos vinculados a esta categoría
                // Si los hay, la eliminación rompería la FK id_categoria en la tabla producto
                int id = Integer.parseInt(request.getParameter("id")); // ID de la categoría a eliminar
                em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión para el COUNT
                TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(p) FROM Producto p WHERE p.categoria.idCategoria = :id", Long.class); // cuenta productos de esta categoría
                q.setParameter("id", id);                // reemplaza :id con el ID real
                long count = q.getSingleResult();        // número de productos vinculados
                // Bloquear si hay productos: eliminar la categoría rompería la integridad referencial
                if (count > 0) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT); // HTTP 409: conflicto de estado
                    out.print("{\"error\":\"No se puede eliminar: tiene " + count + " producto(s) vinculado(s)\"}");
                    return;
                }
                ctrl.destroy(id);                        // DELETE FROM categoria WHERE id_categoria = ? (solo si no hay productos)
                out.print("{\"ok\":true}");              // respuesta de éxito al frontend
                return;
            }

            String nombre      = request.getParameter("nombre");      // nombre de la categoría (obligatorio)
            String descripcion = request.getParameter("descripcion"); // descripción opcional (máx 120 chars)
            String idStr       = request.getParameter("id");          // ID de la categoría al editar o desactivar

            // Validar que el nombre no esté vacío (es el campo principal e indispensable)
            if (nombre == null || nombre.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // HTTP 400: petición inválida
                out.print("{\"error\":\"El nombre es obligatorio\"}");
                return;
            }
            // Validar que la descripción no exceda los 120 caracteres si se proporcionó
            if (descripcion != null && descripcion.length() > 120) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"La descripci\\u00f3n no puede superar los 120 caracteres\"}"); // ó = \u00f3 (unicode)
                return;
            }

            // Verificar que no exista otra categoría con el mismo nombre (insensible a mayúsculas)
            // Al editar, se excluye la propia categoría del chequeo de duplicados usando " AND c.idCategoria <> :id"
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión para la consulta de duplicado
            TypedQuery<Long> dupQ = em.createQuery(
                "SELECT COUNT(c) FROM Categoria c WHERE LOWER(c.nombreCategoria) = LOWER(:n)" +
                (idStr != null ? " AND c.idCategoria <> :id" : ""), Long.class); // excluir la propia categoría al editar
            dupQ.setParameter("n", nombre.trim());       // :n = nombre sin espacios sobrantes
            if (idStr != null) dupQ.setParameter("id", Integer.parseInt(idStr)); // :id = ID de la categoría que se está editando
            if (dupQ.getSingleResult() > 0) {            // si ya existe otra categoría con ese nombre...
                response.setStatus(HttpServletResponse.SC_CONFLICT); // HTTP 409
                out.print("{\"error\":\"Ya existe una categor&#237;a con ese nombre\"}"); // &#237; = í en HTML entity
                return;
            }

            if ("editar".equals(accion) && idStr != null) {
                // EDITAR: cargar la categoría existente, cambiar sus campos y guardar en BD
                Categoria cat = ctrl.findCategoria(Integer.parseInt(idStr)); // SELECT * FROM categoria WHERE id_categoria = ?
                cat.setNombreCategoria(nombre);          // actualiza el nombre
                cat.setDescripcion(descripcion);         // actualiza la descripción
                ctrl.edit(cat);                          // UPDATE categoria SET nombre_categoria=?, descripcion=? WHERE id_categoria=?
            } else if ("desactivar".equals(accion) && idStr != null) {
                // DESACTIVAR/ACTIVAR: alternancia del estado activo (toggle)
                // Si estaba activa pasa a inactiva y viceversa
                Categoria cat = ctrl.findCategoria(Integer.parseInt(idStr)); // cargar la categoría de BD
                cat.setActivo(!cat.isActivo());          // invertir el estado: true→false o false→true
                ctrl.edit(cat);                          // UPDATE categoria SET activo=? WHERE id_categoria=?
            } else {
                // CREAR: insertar una categoría nueva activa por defecto
                Categoria cat = new Categoria();         // objeto nuevo para la nueva categoría
                cat.setNombreCategoria(nombre);          // nombre que proporcionó el admin
                cat.setDescripcion(descripcion);         // descripción opcional (puede ser null)
                cat.setActivo(true);                     // las nuevas categorías se crean activas
                ctrl.create(cat);                        // INSERT INTO categoria (nombre_categoria, descripcion, activo) VALUES (?, ?, ?)
            }
            out.print("{\"ok\":true}");                  // respuesta de éxito al frontend para cualquiera de las acciones anteriores

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // HTTP 500
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");  // error genérico
        } finally {
            if (em != null && em.isOpen()) em.close();   // SIEMPRE cierra el EntityManager para devolver la conexión al pool
        }
    }

    // Método auxiliar que escapa caracteres especiales para que el string sea válido dentro de un JSON
    private String escapeJson(String s) {
        if (s == null) return "";                        // null → cadena vacía (evita escribir la palabra "null" en el JSON)
        return s.replace("\\", "\\\\").replace("\"", "\\\"") // escapa barras invertidas y comillas dobles
                .replace("\n", "\\n").replace("\r", "\\r");  // escapa saltos de línea y retornos de carro
    }
}
