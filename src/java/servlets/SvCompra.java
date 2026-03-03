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
            javax.servlet.http.HttpSession sess = request.getSession(false);
            Usuario usuario = (sess != null) ? (Usuario) sess.getAttribute("usuario") : null;

            if (usuario == null) {
                out.print("{\"error\":\"Debes iniciar sesión para realizar una compra.\"}");
                return;
            }

            // Recargar usuario desde BD para evitar LazyInitializationException
            em = JpaProvider.getEntityManagerFactory().createEntityManager();
            Usuario usuarioFresh = em.find(Usuario.class, usuario.getIdUsuario());
            if (usuarioFresh == null) {
                out.print("{\"error\":\"Usuario no encontrado. Cierra sesión e inicia de nuevo.\"}");
                return;
            }

            Cliente cliente = usuarioFresh.getCliente();
            if (cliente == null) {
                out.print("{\"error\":\"Tu cuenta no tiene cliente asociado.\"}");
                return;
            }

            String itemCountStr = request.getParameter("itemCount");
            int itemCount = 0;
            try { itemCount = Integer.parseInt(itemCountStr); } catch (Exception ignored2) {}

            if (itemCount <= 0) {
                out.print("{\"error\":\"El carrito está vacío.\"}");
                return;
            }

            em.getTransaction().begin();

            Pedido pedido = new Pedido();
            pedido.setCliente(em.getReference(Cliente.class, cliente.getIdCliente()));
            pedido.setEstado(EstadoPedido.PENDIENTE);
            pedido.setFechaPedido(LocalDateTime.now());
            pedido.setCreatedAt(LocalDateTime.now());
            pedido.setUpdatedAt(LocalDateTime.now());
            pedido.setActivo(true);
            pedido.setTotal(BigDecimal.ZERO);
            em.persist(pedido);
            em.flush();

            BigDecimal total = BigDecimal.ZERO;

            for (int i = 0; i < itemCount; i++) {
                String nombre = request.getParameter("item_name_"  + i);
                String priceS = request.getParameter("item_price_" + i);
                String qtyS   = request.getParameter("item_qty_"   + i);

                if (nombre == null || nombre.isBlank()) continue;

                double price = 0;
                int qty = 1;
                try { price = Double.parseDouble(priceS); } catch (Exception ignored) {}
                try { qty   = Integer.parseInt(qtyS);     } catch (Exception ignored) {}
                if (qty <= 0) continue;
                if (price <= 0) price = 1;

                Producto producto = buscarOCrearProducto(em, nombre, request.getParameter("item_brand_" + i), price);
                if (producto == null) continue;

                // Descontar stock (lanza StockInsuficienteException si no hay suficiente)
                producto.reducirStock(qty);
                em.merge(producto);

                Detallepedido det = new Detallepedido();
                det.setPedido(pedido);
                det.setProducto(producto);
                det.setCantidad(qty);
                det.setPrecioUnitario(BigDecimal.valueOf(price));
                det.setActivo(true);
                em.persist(det);

                total = total.add(BigDecimal.valueOf(price * qty));
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

    private Producto buscarOCrearProducto(EntityManager em, String nombre, String brand, double price) {
        TypedQuery<Producto> q = em.createQuery(
            "SELECT p FROM Producto p WHERE LOWER(p.nombreProducto) = LOWER(:n)", Producto.class);
        q.setParameter("n", nombre.trim());
        q.setMaxResults(1);
        List<Producto> r = q.getResultList();
        if (!r.isEmpty()) return r.get(0);

        String marcaNombre = (brand != null && !brand.isBlank()) ? brand.trim() : "General";

        // Buscar o crear Marca dentro del mismo em
        Marca marca = null;
        List<Marca> marcas = em.createQuery("SELECT m FROM Marca m WHERE LOWER(m.nombreMarca) = LOWER(:n)", Marca.class)
            .setParameter("n", marcaNombre).setMaxResults(1).getResultList();
        if (!marcas.isEmpty()) {
            marca = marcas.get(0);
        } else {
            marca = new Marca();
            marca.setNombreMarca(marcaNombre);
            marca.setGenero("HOMBRE");
            marca.setActivo(true);
            em.persist(marca);
            em.flush();
        }

        // Buscar o crear Categoria dentro del mismo em
        Categoria cat = null;
        List<Categoria> cats = em.createQuery("SELECT c FROM Categoria c", Categoria.class)
            .setMaxResults(1).getResultList();
        if (!cats.isEmpty()) {
            cat = cats.get(0);
        } else {
            cat = new Categoria();
            cat.setNombreCategoria("General");
            cat.setActivo(true);
            em.persist(cat);
            em.flush();
        }

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
