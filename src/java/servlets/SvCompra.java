package servlets;

import enums.EstadoPedido;
import exceptions.StockInsuficienteException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logica.Cliente;
import logica.Detallepedido;
import logica.Pedido;
import logica.Producto;
import logica.Usuario;
import logica.Categoria;
import logica.Marca;
import persistencias.JpaProvider;

/**
 * SvCompra — Servlet de procesamiento de compras.
 * Recibe los ítems del carrito por POST, crea el Pedido y sus Detallepedido en BD,
 * descuenta el stock de cada producto y retorna el ID del pedido creado.
 * Si un producto no tiene suficiente stock, lanza StockInsuficienteException
 * y revierte toda la transacción (ninguna compra queda a medias).
 */
public class SvCompra extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print("{\"status\":\"SvCompra OK\"}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        EntityManager em = null;

        try {
            // Verificar que el usuario esté logueado (la sesión puede ser null si expiró)
            javax.servlet.http.HttpSession sess = request.getSession(false);
            Usuario usuario = (sess != null) ? (Usuario) sess.getAttribute("usuario") : null;

            if (usuario == null) {
                out.print("{\"error\":\"Debes iniciar sesión para realizar una compra.\"}");
                return;
            }

            // Recargar el usuario desde BD con un EntityManager fresco
            // El objeto de sesión puede estar detached (desconectado de JPA) y causar errores lazy
            em = JpaProvider.getEntityManagerFactory().createEntityManager();
            Usuario usuarioFresh = em.find(Usuario.class, usuario.getIdUsuario());
            if (usuarioFresh == null) {
                out.print("{\"error\":\"Usuario no encontrado. Cierra sesión e inicia de nuevo.\"}");
                return;
            }

            // Verificar que el usuario tiene un cliente asociado (los admins no pueden comprar)
            Cliente cliente = usuarioFresh.getCliente();
            if (cliente == null) {
                out.print("{\"error\":\"Tu cuenta no tiene cliente asociado.\"}");
                return;
            }

            // Leer cuántos ítems trae el carrito (el frontend los envía como item_name_0, item_name_1, etc.)
            String itemCountStr = request.getParameter("itemCount");
            int itemCount = 0;
            try { itemCount = Integer.parseInt(itemCountStr); } catch (Exception ignored2) {}

            if (itemCount <= 0) {
                out.print("{\"error\":\"El carrito está vacío.\"}");
                return;
            }

            em.getTransaction().begin();

            // Crear el pedido en BD antes de procesar los ítems
            // Se crea con total=0 y se actualiza al final con el total real
            Pedido pedido = new Pedido();
            pedido.setCliente(em.getReference(Cliente.class, cliente.getIdCliente()));
            pedido.setEstado(EstadoPedido.PENDIENTE); // estado inicial
            pedido.setFechaPedido(LocalDateTime.now());
            pedido.setCreatedAt(LocalDateTime.now());
            pedido.setUpdatedAt(LocalDateTime.now());
            pedido.setActivo(true);
            pedido.setTotal(BigDecimal.ZERO);
            em.persist(pedido);
            em.flush(); // forzar INSERT para obtener el ID del pedido antes de agregar los detalles

            BigDecimal total = BigDecimal.ZERO;

            // Procesar cada ítem del carrito uno por uno
            for (int i = 0; i < itemCount; i++) {
                // Leer los datos de cada ítem enviados por el frontend (cart.js)
                String nombre = request.getParameter("item_name_"  + i);
                String priceS = request.getParameter("item_price_" + i);
                String qtyS   = request.getParameter("item_qty_"   + i);

                if (nombre == null || nombre.isBlank()) continue; // ítem inválido, saltar

                double price = 0;
                int qty = 1;
                try { price = Double.parseDouble(priceS); } catch (Exception ignored) {}
                try { qty   = Integer.parseInt(qtyS);     } catch (Exception ignored) {}
                if (qty <= 0) continue;
                if (price <= 0) price = 1;

                // Buscar el producto en BD por nombre; si no existe, crearlo automáticamente
                Producto producto = buscarOCrearProducto(em, nombre, request.getParameter("item_brand_" + i), price);
                if (producto == null) continue;

                // Descontar el stock del producto. Si no hay suficiente stock lanza StockInsuficienteException
                // y el catch de abajo hace rollback de toda la transacción
                producto.reducirStock(qty);
                em.merge(producto); // UPDATE stock en BD

                // Crear la línea de detalle del pedido (qué producto, cuántos, a qué precio)
                Detallepedido det = new Detallepedido();
                det.setPedido(pedido);
                det.setProducto(producto);
                det.setCantidad(qty);
                det.setPrecioUnitario(BigDecimal.valueOf(price));
                det.setActivo(true);
                em.persist(det); // INSERT en tabla detalle_pedido

                total = total.add(BigDecimal.valueOf(price * qty)); // acumular el total
            }

            pedido.setTotal(total);
            em.merge(pedido);
            em.getTransaction().commit();

            out.print("{\"ok\":true,\"idPedido\":" + pedido.getIdPedido() + ",\"total\":" + total + "}");

        } catch (StockInsuficienteException e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            String full = sw.toString();
            String msg = full.length() > 400 ? full.substring(0, 400) : full;
            out.print("{\"error\":\"" + escapeJson(msg) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    /**
     * Busca un producto por nombre exacto (insensible a mayúsculas).
     * Si no existe en BD, lo crea junto con su marca y categoría si tampoco existen.
     * Esto permite procesar compras de productos que no están en el catálogo aún.
     */
    private Producto buscarOCrearProducto(EntityManager em, String nombre, String brand, double price) {
        // Intentar encontrar el producto por nombre
        TypedQuery<Producto> q = em.createQuery(
            "SELECT p FROM Producto p WHERE LOWER(p.nombreProducto) = LOWER(:n)", Producto.class);
        q.setParameter("n", nombre.trim());
        q.setMaxResults(1);
        List<Producto> r = q.getResultList();
        if (!r.isEmpty()) return r.get(0); // producto encontrado, retornar

        // Producto no encontrado: crearlo. Primero resolver la marca
        String marcaNombre = (brand != null && !brand.isBlank()) ? brand.trim() : "General";

        // Buscar la marca por nombre; si no existe, crearla
        Marca marca = null;
        List<Marca> marcas = em.createQuery("SELECT m FROM Marca m WHERE LOWER(m.nombreMarca) = LOWER(:n)", Marca.class)
            .setParameter("n", marcaNombre).setMaxResults(1).getResultList();
        if (!marcas.isEmpty()) {
            marca = marcas.get(0); // marca encontrada
        } else {
            // Crear la marca nueva con género HOMBRE por defecto
            marca = new Marca();
            marca.setNombreMarca(marcaNombre);
            marca.setGenero("HOMBRE");
            marca.setActivo(true);
            em.persist(marca);
            em.flush(); // necesario para que la marca tenga ID antes de asignarla al producto
        }

        // Buscar cualquier categoría existente para asignarla; si no hay ninguna, crearla
        Categoria cat = null;
        List<Categoria> cats = em.createQuery("SELECT c FROM Categoria c", Categoria.class)
            .setMaxResults(1).getResultList();
        if (!cats.isEmpty()) {
            cat = cats.get(0); // usar la primera categoría disponible
        } else {
            cat = new Categoria();
            cat.setNombreCategoria("General");
            cat.setActivo(true);
            em.persist(cat);
            em.flush();
        }

        // Crear el producto nuevo con stock 0 (el stock se gestiona desde el panel admin)
        Producto nuevo = new Producto();
        nuevo.setNombreProducto(nombre.trim());
        nuevo.setMarca(marca);
        nuevo.setCategoria(cat);
        nuevo.setPrecio(BigDecimal.valueOf(price > 0 ? price : 1));
        nuevo.setStock(0);
        nuevo.setActivo(true);
        nuevo.setCreatedAt(LocalDateTime.now());
        nuevo.setUpdatedAt(LocalDateTime.now());
        em.persist(nuevo);
        em.flush();
        return nuevo;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if      (c == '"')  sb.append("\\\"");
            else if (c == '\\') sb.append("\\\\");
            else if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else if (c == '\t') sb.append("\\t");
            else if (c < 0x20)  sb.append(String.format("\\u%04x", (int) c));
            else                sb.append(c);
        }
        return sb.toString();
    }
}
