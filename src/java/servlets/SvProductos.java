package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Categoria;
import logica.Marca;
import logica.Producto;
import persistencias.CategoriaJpaController;
import persistencias.MarcaJpaController;
import persistencias.ProductoJpaController;

/**
 * SvProductos — Servlet de gestión del catálogo de productos.
 * GET: devuelve la lista de productos en formato JSON.
 *   - Sin parámetros: solo productos activos (para el frontend público).
 *   - Con ?admin=true: todos los productos incluyendo inactivos (requiere VER_PRODUCTOS).
 * POST accion=crear: crea un nuevo producto (requiere EDITAR_PRODUCTOS).
 * POST accion=editar: modifica un producto existente (requiere EDITAR_PRODUCTOS).
 * POST accion=eliminar: eliminación física del producto (requiere ELIMINAR_PRODUCTOS).
 */
@WebServlet(name = "SvProductos", urlPatterns = {"/SvProductos"})
public class SvProductos extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            ProductoJpaController ctrl = new ProductoJpaController();
            List<Producto> productos = ctrl.findProductoEntities(); // trae todos desde BD

            // Determinar si es una llamada desde el panel admin (?admin=true)
            boolean esAdminCall = "true".equals(request.getParameter("admin"));
            // Si es llamada admin, verificar que el usuario tenga el permiso VER_PRODUCTOS
            if (esAdminCall && !AuthHelper.tienePermiso(request, "VER_PRODUCTOS")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Sin permiso: VER_PRODUCTOS\"}");
                return;
            }
            // Si no es admin, filtrar solo los productos activos (los inactivos no se muestran en la tienda)
            if (!esAdminCall) {
                productos = productos.stream().filter(Producto::isActivo).collect(java.util.stream.Collectors.toList());
            }

            // Construir la respuesta JSON manualmente (sin librerías externas como Gson/Jackson)
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < productos.size(); i++) {
                Producto p = productos.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(p.getIdProducto()).append(",");
                sb.append("\"nombre\":\"").append(escapeJson(p.getNombreProducto())).append("\",");
                sb.append("\"descripcion\":\"").append(escapeJson(p.getDescripcion())).append("\",");
                sb.append("\"precio\":").append(p.getPrecio()).append(",");
                sb.append("\"stock\":").append(p.getStock()).append(",");
                sb.append("\"activo\":").append(p.isActivo()).append(",");
                sb.append("\"categoria\":\"").append(p.getCategoria() != null ? escapeJson(p.getCategoria().getNombreCategoria()) : "").append("\",");
                sb.append("\"idCategoria\":").append(p.getCategoria() != null ? p.getCategoria().getIdCategoria() : 0).append(",");
                sb.append("\"marca\":\"").append(p.getMarca() != null ? escapeJson(p.getMarca().getNombreMarca()) : "").append("\",");
                sb.append("\"idMarca\":").append(p.getMarca() != null ? p.getMarca().getIdMarca() : 0).append(",");
                sb.append("\"imagenUrl\":\"").append(escapeJson(p.getImagenUrl())).append("\"");
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

        try {
            String accion = request.getParameter("accion"); // "crear", "editar" o "eliminar"

            // Verificar que el usuario tenga permiso para crear o editar productos
            if (!AuthHelper.tienePermiso(request, "EDITAR_PRODUCTOS")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Sin permiso: EDITAR_PRODUCTOS\"}");
                return;
            }

            // La eliminación requiere un permiso adicional más restrictivo
            if ("eliminar".equals(accion)) {
                if (!AuthHelper.tienePermiso(request, "ELIMINAR_PRODUCTOS")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Sin permiso: ELIMINAR_PRODUCTOS\"}");
                    return;
                }
                int id = Integer.parseInt(request.getParameter("id"));
                new ProductoJpaController().destroy(id); // eliminación física de la BD
                out.print("{\"ok\":true}");
                return;
            }

            // Activar / desactivar producto (toggle activo)
            if ("toggleActivo".equals(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                ProductoJpaController prodCtrlT = new ProductoJpaController();
                Producto p = prodCtrlT.findProducto(id);
                if (p == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Producto no encontrado\"}");
                    return;
                }
                p.setActivo(!p.isActivo());
                p.setUpdatedAt(LocalDateTime.now());
                prodCtrlT.edit(p);
                out.print("{\"ok\":true,\"activo\":" + p.isActivo() + "}");
                return;
            }

            String nombre        = request.getParameter("nombre");
            String descripcion   = request.getParameter("descripcion");
            String precioStr     = request.getParameter("precio");
            String stockStr      = request.getParameter("stock");
            String idCatStr      = request.getParameter("idCategoria");
            String idMarcaStr    = request.getParameter("idMarca");
            String idStr         = request.getParameter("id");
            String imagenUrl     = request.getParameter("imagenUrl");

            BigDecimal precio = new BigDecimal(precioStr);
            int stock         = Integer.parseInt(stockStr);

            CategoriaJpaController catCtrl   = new CategoriaJpaController();
            MarcaJpaController marcaCtrl     = new MarcaJpaController();
            ProductoJpaController prodCtrl   = new ProductoJpaController();

            // Resolver la categoría: si viene un número, buscar por ID; si viene un texto, buscar o crear por nombre
            Categoria cat;
            Marca marca;
            try {
                int idCat = Integer.parseInt(idCatStr); // intentar como ID numérico
                cat = catCtrl.findCategoria(idCat);
                if (cat == null) throw new Exception("Categoría con ID " + idCat + " no encontrada.");
            } catch (NumberFormatException e) {
                // Si no es un número, buscar por nombre o crearla si no existe
                cat = buscarOCrearCategoria(catCtrl, idCatStr != null && !idCatStr.isBlank() ? idCatStr : "General");
            }
            // Resolver la marca: misma lógica que la categoría
            try {
                int idMarca = Integer.parseInt(idMarcaStr); // intentar como ID numérico
                marca = marcaCtrl.findMarca(idMarca);
                if (marca == null) throw new Exception("Marca con ID " + idMarca + " no encontrada.");
            } catch (NumberFormatException e) {
                // Si no es un número, buscar por nombre o crearla si no existe
                marca = buscarOCrearMarca(marcaCtrl, idMarcaStr != null && !idMarcaStr.isBlank() ? idMarcaStr : "Sin Marca");
            }

            if ("editar".equals(accion) && idStr != null) {
                // EDITAR: cargar el producto existente y actualizar sus campos
                Producto p = prodCtrl.findProducto(Integer.parseInt(idStr));
                p.setNombreProducto(nombre);
                p.setDescripcion(descripcion);
                p.setPrecio(precio);
                p.setStock(stock);
                p.setCategoria(cat);
                p.setMarca(marca);
                // Solo actualizar la imagen si se envió una nueva URL; si no, mantener la anterior
                p.setImagenUrl(imagenUrl != null && !imagenUrl.isBlank() ? imagenUrl.trim() : p.getImagenUrl());
                p.setUpdatedAt(LocalDateTime.now());
                prodCtrl.edit(p); // UPDATE en BD
            } else {
                // CREAR: construir un nuevo producto y persistirlo
                Producto p = new Producto();
                p.setNombreProducto(nombre);
                p.setDescripcion(descripcion);
                p.setPrecio(precio);
                p.setStock(stock);
                p.setCategoria(cat);
                p.setMarca(marca);
                p.setImagenUrl(imagenUrl != null && !imagenUrl.isBlank() ? imagenUrl.trim() : null);
                p.setActivo(true); // los productos nuevos se crean activos por defecto
                p.setCreatedAt(LocalDateTime.now());
                p.setUpdatedAt(LocalDateTime.now());
                prodCtrl.create(p); // INSERT en BD
            }
            out.print("{\"ok\":true}");

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
        List<Categoria> todas = ctrl.findCategoriaEntities();
        for (Categoria c : todas) {
            if (c.getNombreCategoria().equalsIgnoreCase(nombre)) return c; // encontrada
        }
        // No existe: crear la categoría nueva
        Categoria nueva = new Categoria();
        nueva.setNombreCategoria(nombre);
        nueva.setActivo(true);
        ctrl.create(nueva);
        // Volver a buscar para obtener el objeto con el ID asignado por la BD
        for (Categoria c : ctrl.findCategoriaEntities()) {
            if (c.getNombreCategoria().equalsIgnoreCase(nombre)) return c;
        }
        return null;
    }

    /**
     * Busca una marca por nombre (sin distinción de mayúsculas).
     * Si no existe, la crea automáticamente con género HOMBRE por defecto.
     */
    private Marca buscarOCrearMarca(MarcaJpaController ctrl, String nombre) throws Exception {
        List<Marca> todas = ctrl.findMarcaEntities();
        for (Marca m : todas) {
            if (m.getNombreMarca().equalsIgnoreCase(nombre)) return m; // encontrada
        }
        // No existe: crear la marca nueva
        Marca nueva = new Marca();
        nueva.setNombreMarca(nombre);
        nueva.setGenero("HOMBRE"); // género por defecto al crear desde aquí
        nueva.setActivo(true);
        ctrl.create(nueva);
        // Volver a buscar para obtener el objeto con el ID asignado por la BD
        for (Marca m : ctrl.findMarcaEntities()) {
            if (m.getNombreMarca().equalsIgnoreCase(nombre)) return m;
        }
        return null;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
