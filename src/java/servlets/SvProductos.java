package servlets;                                        // declara que esta clase pertenece al paquete "servlets"

import java.io.IOException;                              // excepción de entrada/salida
import java.io.PrintWriter;                              // escribe texto en la respuesta HTTP
import java.math.BigDecimal;                             // tipo exacto para precios (evita errores de punto flotante)
import java.time.LocalDateTime;                          // fecha+hora para updated_at
import java.util.List;                                   // lista de productos
import javax.servlet.ServletException;                   // excepción propia de los servlets
import javax.servlet.annotation.WebServlet;              // anotación que mapea la URL al servlet
import javax.servlet.http.HttpServlet;                   // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;            // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;           // representa la respuesta HTTP saliente
import logica.Categoria;                                 // entidad que representa una categoría de productos
import logica.Marca;                                     // entidad que representa una marca de perfumes
import logica.Producto;                                  // entidad que representa un producto del catálogo
import javax.persistence.EntityManager;                  // gestiona la sesión activa con la BD
import javax.persistence.TypedQuery;                     // consulta JPQL con tipo de retorno definido
import persistencias.CategoriaJpaController;             // controlador para CRUD en la tabla categoria
import persistencias.JpaProvider;                        // Singleton que provee la EntityManagerFactory
import persistencias.MarcaJpaController;                 // controlador para CRUD en la tabla marca
import persistencias.ProductoJpaController;              // controlador para CRUD en la tabla producto

/**
 * SvProductos — Servlet de gestión del catálogo de productos.
 * GET: devuelve la lista de productos en formato JSON.
 *   - Sin parámetros: solo productos activos (para el frontend público).
 *   - Con ?admin=true: todos los productos incluyendo inactivos (requiere VER_PRODUCTOS).
 * POST accion=crear: crea un nuevo producto (requiere EDITAR_PRODUCTOS).
 * POST accion=editar: modifica un producto existente (requiere EDITAR_PRODUCTOS).
 * POST accion=eliminar: eliminación física del producto (requiere ELIMINAR_PRODUCTOS).
 */
@WebServlet(name = "SvProductos", urlPatterns = {"/SvProductos"}) // cuando llegue una petición a /SvProductos, Tomcat ejecuta esta clase
public class SvProductos extends HttpServlet {           // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando alguien hace GET a /SvProductos
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8"); // respuesta JSON en UTF-8
        PrintWriter out = response.getWriter();          // escritor de la respuesta

        try {
            ProductoJpaController ctrl = new ProductoJpaController(); // controlador para acceder a la tabla producto
            List<Producto> productos = ctrl.findProductoEntities();   // SELECT * FROM producto ORDER BY id_producto ASC

            // Determinar si es una llamada desde el panel admin (?admin=true en la URL)
            boolean esAdminCall = "true".equals(request.getParameter("admin")); // true si viene el parámetro admin=true
            // Si es llamada admin, verificar que el usuario tenga el permiso VER_PRODUCTOS
            if (esAdminCall && !AuthHelper.tienePermiso(request, "VER_PRODUCTOS")) { // consulta la sesión HTTP
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 403
                out.print("{\"error\":\"Sin permiso: VER_PRODUCTOS\"}");
                return;
            }
            // Si no es llamada admin, filtrar para mostrar solo los productos activos
            // Los productos inactivos no deben aparecer en la tienda pública
            if (!esAdminCall) {
                productos = productos.stream().filter(Producto::isActivo).collect(java.util.stream.Collectors.toList()); // filtra solo activos
            }

            // Construir la respuesta JSON manualmente (sin librerías externas como Gson/Jackson)
            StringBuilder sb = new StringBuilder("[");   // inicia el array JSON
            for (int i = 0; i < productos.size(); i++) { // iterar sobre cada producto
                Producto p = productos.get(i);           // producto actual
                if (i > 0) sb.append(",");               // separador entre objetos del array
                sb.append("{");
                sb.append("\"id\":").append(p.getIdProducto()).append(",");                            // ID del producto
                sb.append("\"nombre\":\"").append(escapeJson(p.getNombreProducto())).append("\",");   // nombre del perfume
                sb.append("\"descripcion\":\"").append(escapeJson(p.getDescripcion())).append("\","); // descripción opcional
                sb.append("\"precio\":").append(p.getPrecio()).append(",");                           // precio BigDecimal
                sb.append("\"stock\":").append(p.getStock()).append(",");                             // unidades disponibles
                sb.append("\"activo\":").append(p.isActivo()).append(",");                            // true/false si está visible en tienda
                sb.append("\"categoria\":\"").append(p.getCategoria() != null ? escapeJson(p.getCategoria().getNombreCategoria()) : "").append("\","); // nombre de la categoría
                sb.append("\"idCategoria\":").append(p.getCategoria() != null ? p.getCategoria().getIdCategoria() : 0).append(","); // ID de la categoría
                sb.append("\"marca\":\"").append(p.getMarca() != null ? escapeJson(p.getMarca().getNombreMarca()) : "").append("\","); // nombre de la marca
                sb.append("\"idMarca\":").append(p.getMarca() != null ? p.getMarca().getIdMarca() : 0).append(","); // ID de la marca
                sb.append("\"imagenUrl\":\"").append(escapeJson(p.getImagenUrl())).append("\"");     // URL de la imagen del producto
                sb.append("}");
            }
            sb.append("]");                              // cierra el array JSON
            out.print(sb.toString());                    // envía el JSON al navegador

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando admin.js hace POST a /SvProductos
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");           // fuerza UTF-8 para leer caracteres especiales del formulario
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String accion = request.getParameter("accion"); // "crear", "editar", "eliminar" o "toggleActivo"

            // Verificar que el usuario tiene permiso para crear o editar productos
            if (!AuthHelper.tienePermiso(request, "EDITAR_PRODUCTOS")) { // permiso básico requerido
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Sin permiso: EDITAR_PRODUCTOS\"}");
                return;
            }

            // La eliminación requiere un permiso adicional más restrictivo que el de edición
            if ("eliminar".equals(accion)) {
                if (!AuthHelper.tienePermiso(request, "ELIMINAR_PRODUCTOS")) { // permiso extra para eliminar
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Sin permiso: ELIMINAR_PRODUCTOS\"}");
                    return;
                }
                int id = Integer.parseInt(request.getParameter("id")); // ID del producto a eliminar
                // Verificar si el producto tiene pedidos asociados en detalle_pedido
                // Si los tiene, no se puede eliminar físicamente porque rompería el historial de compras
                EntityManager emCheck = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión para el COUNT
                try {
                    TypedQuery<Long> qCheck = emCheck.createQuery(
                        "SELECT COUNT(d) FROM Detallepedido d WHERE d.producto.idProducto = :id", Long.class); // cuenta líneas de pedido de este producto
                    qCheck.setParameter("id", id);
                    long pedidosVinculados = qCheck.getSingleResult(); // cuántos pedidos tienen este producto
                    if (pedidosVinculados > 0) {
                        // Hay pedidos que referencian este producto: solo se puede desactivar, no eliminar
                        response.setStatus(HttpServletResponse.SC_CONFLICT); // HTTP 409
                        out.print("{\"error\":\"Imposible realizar esta accion por que el producto ya tiene una venta\"}");
                        return;
                    }
                } finally {
                    emCheck.close();                     // SIEMPRE cerrar este EntityManager
                }
                new ProductoJpaController().destroy(id); // DELETE FROM producto WHERE id_producto = ? (solo si no tiene pedidos)
                out.print("{\"ok\":true}");
                return;
            }

            // Activar o desactivar un producto sin cambiar ningún otro dato (toggle activo)
            if ("toggleActivo".equals(accion)) {
                int id = Integer.parseInt(request.getParameter("id")); // ID del producto a toggle
                ProductoJpaController prodCtrlT = new ProductoJpaController(); // controlador para la actualización
                Producto p = prodCtrlT.findProducto(id); // SELECT * FROM producto WHERE id_producto = ?
                if (p == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND); // HTTP 404
                    out.print("{\"error\":\"Producto no encontrado\"}");
                    return;
                }
                p.setActivo(!p.isActivo());              // invertir el estado: activo→inactivo o inactivo→activo
                p.setUpdatedAt(LocalDateTime.now());     // actualizar la fecha de modificación
                prodCtrlT.edit(p);                       // UPDATE producto SET activo=?, updated_at=? WHERE id_producto=?
                out.print("{\"ok\":true,\"activo\":" + p.isActivo() + "}"); // retorna el nuevo estado para actualizar la UI
                return;
            }

            // Leer los campos del formulario para crear o editar un producto
            String nombre        = request.getParameter("nombre");       // nombre del perfume
            String descripcion   = request.getParameter("descripcion");  // descripción
            String precioStr     = request.getParameter("precio");       // precio como texto (se convierte a BigDecimal)
            String stockStr      = request.getParameter("stock");        // stock como texto (se convierte a int)
            String idCatStr      = request.getParameter("idCategoria");  // ID o nombre de la categoría
            String idMarcaStr    = request.getParameter("idMarca");      // ID o nombre de la marca
            String idStr         = request.getParameter("id");           // ID del producto al editar
            String imagenUrl     = request.getParameter("imagenUrl");    // URL de la imagen del producto

            BigDecimal precio = new BigDecimal(precioStr); // convierte el texto del precio a BigDecimal exacto
            int stock         = Integer.parseInt(stockStr); // convierte el texto del stock a entero

            CategoriaJpaController catCtrl   = new CategoriaJpaController(); // controlador para buscar categorías
            MarcaJpaController marcaCtrl     = new MarcaJpaController();     // controlador para buscar marcas
            ProductoJpaController prodCtrl   = new ProductoJpaController();  // controlador para crear/editar productos

            // Resolver la categoría: si el valor es un número, buscar por ID; si es texto, buscar o crear por nombre
            Categoria cat;
            Marca marca;
            try {
                int idCat = Integer.parseInt(idCatStr);  // intentar parsear como ID numérico
                cat = catCtrl.findCategoria(idCat);      // SELECT * FROM categoria WHERE id_categoria = ?
                if (cat == null) throw new Exception("Categoría con ID " + idCat + " no encontrada.");
            } catch (NumberFormatException e) {
                // Si no es un número, buscar por nombre o crear la categoría si no existe en BD
                cat = buscarOCrearCategoria(catCtrl, idCatStr != null && !idCatStr.isBlank() ? idCatStr : "General");
            }
            // Misma lógica para la marca: ID numérico o nombre de texto
            try {
                int idMarca = Integer.parseInt(idMarcaStr); // intentar parsear como ID numérico
                marca = marcaCtrl.findMarca(idMarca);    // SELECT * FROM marca WHERE id_marca = ?
                if (marca == null) throw new Exception("Marca con ID " + idMarca + " no encontrada.");
            } catch (NumberFormatException e) {
                // Si no es un número, buscar por nombre o crear la marca si no existe en BD
                marca = buscarOCrearMarca(marcaCtrl, idMarcaStr != null && !idMarcaStr.isBlank() ? idMarcaStr : "Sin Marca");
            }

            if ("editar".equals(accion) && idStr != null) {
                // EDITAR: cargar el producto existente y actualizar sus campos con los valores nuevos
                Producto p = prodCtrl.findProducto(Integer.parseInt(idStr)); // SELECT * FROM producto WHERE id_producto = ?
                p.setNombreProducto(nombre);             // actualiza el nombre
                p.setDescripcion(descripcion);           // actualiza la descripción
                p.setPrecio(precio);                     // actualiza el precio DECIMAL(10,2)
                p.setStock(stock);                       // actualiza el stock disponible
                p.setCategoria(cat);                     // actualiza la FK id_categoria
                p.setMarca(marca);                       // actualiza la FK id_marca
                // Solo actualizar la imagen si se envió una nueva URL; si no, mantener la anterior
                p.setImagenUrl(imagenUrl != null && !imagenUrl.isBlank() ? imagenUrl.trim() : p.getImagenUrl());
                p.setUpdatedAt(LocalDateTime.now());     // registra la fecha de la modificación
                prodCtrl.edit(p);                        // UPDATE producto SET ... WHERE id_producto=?
            } else {
                // CREAR: construir un nuevo producto y persistirlo en BD
                Producto p = new Producto();             // objeto nuevo para el nuevo producto
                p.setNombreProducto(nombre);             // nombre del perfume
                p.setDescripcion(descripcion);           // descripción
                p.setPrecio(precio);                     // precio en pesos colombianos
                p.setStock(stock);                       // unidades disponibles en inventario
                p.setCategoria(cat);                     // FK a la categoría
                p.setMarca(marca);                       // FK a la marca
                p.setImagenUrl(imagenUrl != null && !imagenUrl.isBlank() ? imagenUrl.trim() : null); // URL de imagen o null si no tiene
                p.setActivo(true);                       // los productos nuevos se crean activos por defecto
                p.setCreatedAt(LocalDateTime.now());     // fecha de creación en BD
                p.setUpdatedAt(LocalDateTime.now());     // fecha de última modificación
                prodCtrl.create(p);                      // INSERT INTO producto (...) VALUES (...)
            }
            out.print("{\"ok\":true}");                  // respuesta de éxito al frontend

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    /**
     * Busca una categoría por nombre (sin distinción de mayúsculas).
     * Si no existe, la crea automáticamente. Útil cuando el admin escribe un nombre nuevo.
     */
    private Categoria buscarOCrearCategoria(CategoriaJpaController ctrl, String nombre) throws Exception {
        List<Categoria> todas = ctrl.findCategoriaEntities(); // SELECT * FROM categoria
        for (Categoria c : todas) {
            if (c.getNombreCategoria().equalsIgnoreCase(nombre)) return c; // encontrada: retornar sin crear
        }
        // No existe: crear la categoría nueva automáticamente
        Categoria nueva = new Categoria();               // nueva categoría
        nueva.setNombreCategoria(nombre);                // nombre tal como lo ingresó el admin
        nueva.setActivo(true);                           // activa por defecto
        ctrl.create(nueva);                              // INSERT INTO categoria
        // Volver a buscar para obtener el objeto con el ID asignado por MySQL (AUTO_INCREMENT)
        for (Categoria c : ctrl.findCategoriaEntities()) {
            if (c.getNombreCategoria().equalsIgnoreCase(nombre)) return c; // retornar la categoría con su nuevo ID
        }
        return null;                                     // no debería llegar aquí
    }

    /**
     * Busca una marca por nombre (sin distinción de mayúsculas).
     * Si no existe, la crea automáticamente con género HOMBRE por defecto.
     */
    private Marca buscarOCrearMarca(MarcaJpaController ctrl, String nombre) throws Exception {
        List<Marca> todas = ctrl.findMarcaEntities();    // SELECT * FROM marca
        for (Marca m : todas) {
            if (m.getNombreMarca().equalsIgnoreCase(nombre)) return m; // encontrada: retornar sin crear
        }
        // No existe: crear la marca nueva automáticamente
        Marca nueva = new Marca();                       // nueva marca
        nueva.setNombreMarca(nombre);                    // nombre tal como lo ingresó el admin
        nueva.setGenero("HOMBRE");                       // género por defecto al crear desde aquí
        nueva.setActivo(true);                           // activa por defecto
        ctrl.create(nueva);                              // INSERT INTO marca
        // Volver a buscar para obtener el objeto con el ID asignado por MySQL (AUTO_INCREMENT)
        for (Marca m : ctrl.findMarcaEntities()) {
            if (m.getNombreMarca().equalsIgnoreCase(nombre)) return m; // retornar la marca con su nuevo ID
        }
        return null;                                     // no debería llegar aquí
    }

    // Método auxiliar que escapa caracteres especiales para JSON
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
