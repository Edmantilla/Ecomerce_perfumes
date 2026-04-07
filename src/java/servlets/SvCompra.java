package servlets;                                        // declara que esta clase pertenece al paquete "servlets"

import enums.EstadoPedido;                               // enum con los posibles estados de un pedido: PENDIENTE, ENVIADO, ENTREGADO, CANCELADO, PAGO
import exceptions.StockInsuficienteException;            // excepción personalizada que se lanza cuando un producto no tiene stock suficiente
import java.io.IOException;                              // excepción de entrada/salida
import java.io.PrintWriter;                              // escribe texto en la respuesta HTTP
import java.math.BigDecimal;                             // tipo exacto para cálculos monetarios (precio × cantidad, total)
import java.time.LocalDateTime;                          // fecha+hora actual para created_at y updated_at
import java.util.List;                                   // lista de resultados de consultas JPQL
import javax.persistence.EntityManager;                  // gestiona la sesión activa con la BD y las transacciones
import javax.persistence.TypedQuery;                     // consulta JPQL con tipo de retorno definido
import javax.servlet.ServletException;                   // excepción propia de los servlets
import javax.servlet.http.HttpServlet;                   // clase base de todos los servlets HTTP
import javax.servlet.http.HttpServletRequest;            // representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse;           // representa la respuesta HTTP saliente
import logica.Cliente;                                   // entidad del cliente (datos personales del comprador)
import logica.Detallepedido;                             // entidad que representa una línea del pedido (un ítem del carrito)
import logica.Pedido;                                    // entidad que representa el pedido completo
import logica.Producto;                                  // entidad del producto para verificar y descontar stock
import logica.Usuario;                                   // entidad del usuario para verificar la sesión
import logica.Categoria;                                 // entidad de categoría usada al crear productos nuevos automáticamente
import logica.Marca;                                     // entidad de marca usada al crear marcas nuevas automáticamente
import persistencias.JpaProvider;                        // Singleton que provee la EntityManagerFactory

/**
 * SvCompra — Servlet de procesamiento de compras.
 * Recibe los ítems del carrito por POST, crea el Pedido y sus Detallepedido en BD,
 * descuenta el stock de cada producto y retorna el ID del pedido creado.
 * Si un producto no tiene suficiente stock, lanza StockInsuficienteException
 * y revierte toda la transacción (ninguna compra queda a medias).
 */
public class SvCompra extends HttpServlet {              // extiende HttpServlet para manejar peticiones HTTP

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) // GET de prueba: confirma que el servlet está activo
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8"); // respuesta JSON
        response.getWriter().print("{\"status\":\"SvCompra OK\"}"); // mensaje de estado para pruebas
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) // se ejecuta cuando cart.js envía el carrito al servidor
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8"); // respuesta JSON en UTF-8
        response.setCharacterEncoding("UTF-8");          // codificación de la respuesta
        PrintWriter out = response.getWriter();          // escritor de la respuesta
        EntityManager em = null;                         // declara em en null para cerrarlo en el finally

        try {
            // Verificar que el usuario esté logueado antes de procesar la compra
            // La sesión puede ser null si expiró o si el usuario nunca inició sesión
            javax.servlet.http.HttpSession sess = request.getSession(false); // false: no crear sesión nueva
            Usuario usuario = (sess != null) ? (Usuario) sess.getAttribute("usuario") : null; // obtener el usuario de sesión

            if (usuario == null) {                       // si no hay usuario en sesión...
                out.print("{\"error\":\"Debes iniciar sesión para realizar una compra.\"}");
                return;                                  // no procesar la compra sin usuario logueado
            }

            // Recargar el usuario desde BD con un EntityManager fresco
            // El objeto de sesión puede estar "detached" (desconectado de JPA) y causar LazyInitializationException
            em = JpaProvider.getEntityManagerFactory().createEntityManager(); // abre sesión con la BD
            Usuario usuarioFresh = em.find(Usuario.class, usuario.getIdUsuario()); // SELECT * FROM usuario WHERE id_usuario = ?
            if (usuarioFresh == null) {                  // el usuario fue eliminado de BD mientras tenía sesión activa
                out.print("{\"error\":\"Usuario no encontrado. Cierra sesión e inicia de nuevo.\"}");
                return;
            }

            // Verificar que el usuario tiene un cliente asociado
            // Los usuarios admin puro no tienen cliente y no pueden realizar compras
            Cliente cliente = usuarioFresh.getCliente(); // obtiene el cliente asociado al usuario (puede ser null para admins)
            if (cliente == null) {
                out.print("{\"error\":\"Tu cuenta no tiene cliente asociado.\"}");
                return;
            }

            // Leer cuántos ítems trae el carrito
            // El frontend los envía como: item_name_0, item_price_0, item_qty_0, item_name_1, item_price_1, etc.
            String itemCountStr = request.getParameter("itemCount"); // número total de ítems en el carrito
            int itemCount = 0;
            try { itemCount = Integer.parseInt(itemCountStr); } catch (Exception ignored2) {} // si no es número, queda 0

            if (itemCount <= 0) {                        // carrito vacío: no procesar
                out.print("{\"error\":\"El carrito está vacío.\"}");
                return;
            }

            em.getTransaction().begin();                 // inicia la transacción: todo el pedido se guarda o nada

            // Crear el pedido en BD antes de procesar los ítems
            // Se crea con total=0 y se actualiza al final con el total real calculado
            Pedido pedido = new Pedido();                // nuevo pedido
            pedido.setCliente(em.getReference(Cliente.class, cliente.getIdCliente())); // referencia lazy al cliente (evita SELECT extra)
            pedido.setEstado(EstadoPedido.PENDIENTE);    // estado inicial: recién creado, aún sin procesar
            pedido.setFechaPedido(LocalDateTime.now());  // fecha y hora en que se realizó la compra
            pedido.setCreatedAt(LocalDateTime.now());    // fecha de creación en BD
            pedido.setUpdatedAt(LocalDateTime.now());    // fecha de última modificación
            pedido.setActivo(true);                      // pedido activo por defecto
            pedido.setTotal(BigDecimal.ZERO);            // total provisional en 0 (se actualiza al final)
            em.persist(pedido);                          // INSERT INTO pedido (...) → MySQL asigna el id_pedido
            em.flush();                                  // forzar el INSERT ahora para obtener el ID del pedido antes de los detalles

            BigDecimal total = BigDecimal.ZERO;          // acumulador del total de la compra

            // Procesar cada ítem del carrito uno por uno
            for (int i = 0; i < itemCount; i++) {        // recorrer cada ítem del 0 hasta itemCount-1
                // Leer los datos de cada ítem enviados por cart.js con índice numérico
                String nombre = request.getParameter("item_name_"  + i); // nombre del producto
                String priceS = request.getParameter("item_price_" + i); // precio unitario del producto
                String qtyS   = request.getParameter("item_qty_"   + i); // cantidad del ítem en el carrito

                if (nombre == null || nombre.isBlank()) continue; // ítem inválido (sin nombre), saltar al siguiente

                double price = 0;
                int qty = 1;
                try { price = Double.parseDouble(priceS); } catch (Exception ignored) {} // parsear precio; si falla, queda 0
                try { qty   = Integer.parseInt(qtyS);     } catch (Exception ignored) {} // parsear cantidad; si falla, queda 1
                if (qty <= 0) continue;                  // cantidad 0 o negativa: ítem inválido, saltar
                if (price <= 0) price = 1;               // precio 0 o negativo no tiene sentido, usar mínimo 1

                // Buscar el producto en BD por nombre; si no existe, crearlo automáticamente
                // Esto permite procesar compras de productos que el admin añadió fuera del sistema
                Producto producto = buscarOCrearProducto(em, nombre, request.getParameter("item_brand_" + i), price);
                if (producto == null) continue;          // si no se pudo crear, saltar este ítem

                // Descontar el stock del producto
                // Si no hay suficiente stock, reducirStock() lanza StockInsuficienteException
                // El catch de abajo hace rollback de toda la transacción para que nada quede a medias
                producto.reducirStock(qty);              // stock -= qty; lanza excepción si stock < qty
                em.merge(producto);                      // UPDATE producto SET stock=? WHERE id_producto=?

                // Crear la línea de detalle del pedido (registra qué se compró, cuántos y a qué precio)
                Detallepedido det = new Detallepedido(); // nueva línea de detalle
                det.setPedido(pedido);                   // FK id_pedido → vincula con el pedido
                det.setProducto(producto);               // FK id_producto → vincula con el producto comprado
                det.setCantidad(qty);                    // cuántas unidades de este producto
                det.setPrecioUnitario(BigDecimal.valueOf(price)); // precio al que se compró (puede diferir del precio actual)
                det.setActivo(true);                     // detalle activo
                em.persist(det);                         // INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario, activo)

                total = total.add(BigDecimal.valueOf(price * qty)); // acumular: total += precio × cantidad
            }

            pedido.setTotal(total);                      // actualizar el total del pedido con la suma real calculada
            em.merge(pedido);                            // UPDATE pedido SET total=? WHERE id_pedido=?
            em.getTransaction().commit();                // confirmar: pedido, detalles y stock quedan guardados en MySQL

            out.print("{\"ok\":true,\"idPedido\":" + pedido.getIdPedido() + ",\"total\":" + total + "}"); // retorna el ID del pedido creado

        } catch (StockInsuficienteException e) {         // captura específica: un producto no tenía suficiente stock
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // revertir TODA la compra
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}"); // informar al usuario qué producto falló
        } catch (Exception e) {                          // captura general: error de BD u otro error inesperado
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback(); // revertir para no dejar datos a medias
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // HTTP 500
            // Capturar el stack trace completo para facilitar el diagnóstico del error
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            String full = sw.toString();
            String msg = full.length() > 400 ? full.substring(0, 400) : full; // limitar a 400 chars para no saturar la respuesta
            out.print("{\"error\":\"" + escapeJson(msg) + "\"}");
        } finally {
            if (em != null && em.isOpen()) em.close();   // SIEMPRE cerrar el EntityManager para devolver la conexión al pool
        }
    }

    /**
     * Busca un producto por nombre exacto (insensible a mayúsculas).
     * Si no existe en BD, lo crea junto con su marca y categoría si tampoco existen.
     * Esto permite procesar compras de productos que no están aún en el catálogo formal.
     */
    private Producto buscarOCrearProducto(EntityManager em, String nombre, String brand, double price) {
        // Intentar encontrar el producto por nombre usando JPQL
        TypedQuery<Producto> q = em.createQuery(
            "SELECT p FROM Producto p WHERE LOWER(p.nombreProducto) = LOWER(:n)", Producto.class); // búsqueda insensible a mayúsculas
        q.setParameter("n", nombre.trim());              // :n = nombre del producto normalizado
        q.setMaxResults(1);                              // solo necesitamos uno
        List<Producto> r = q.getResultList();            // ejecutar el SELECT
        if (!r.isEmpty()) return r.get(0);               // producto encontrado: retornar sin crear

        // Producto no encontrado: crearlo. Primero resolver la marca
        String marcaNombre = (brand != null && !brand.isBlank()) ? brand.trim() : "General"; // usar la marca del carrito o "General"

        // Buscar la marca por nombre; si no existe en BD, crearla automáticamente
        Marca marca = null;
        List<Marca> marcas = em.createQuery("SELECT m FROM Marca m WHERE LOWER(m.nombreMarca) = LOWER(:n)", Marca.class)
            .setParameter("n", marcaNombre).setMaxResults(1).getResultList(); // buscar la marca
        if (!marcas.isEmpty()) {
            marca = marcas.get(0);                       // marca encontrada en BD
        } else {
            // Crear la marca nueva con género HOMBRE por defecto (el admin puede editarlo después)
            marca = new Marca();
            marca.setNombreMarca(marcaNombre);           // nombre de la marca
            marca.setGenero("HOMBRE");                   // género por defecto
            marca.setActivo(true);                       // activa por defecto
            em.persist(marca);                           // INSERT INTO marca
            em.flush();                                  // forzar INSERT para que la marca tenga ID antes de asignarla al producto
        }

        // Buscar cualquier categoría existente para asignarla al nuevo producto
        // Si no hay ninguna, crear la categoría "General"
        Categoria cat = null;
        List<Categoria> cats = em.createQuery("SELECT c FROM Categoria c", Categoria.class)
            .setMaxResults(1).getResultList();           // buscar cualquier categoría disponible
        if (!cats.isEmpty()) {
            cat = cats.get(0);                           // usar la primera categoría disponible
        } else {
            cat = new Categoria();                       // no hay categorías: crear "General"
            cat.setNombreCategoria("General");
            cat.setActivo(true);
            em.persist(cat);                             // INSERT INTO categoria
            em.flush();                                  // forzar INSERT para tener el ID
        }

        // Crear el producto nuevo en BD
        // El stock se inicializa en 0 porque el admin debe gestionarlo desde el panel
        Producto nuevo = new Producto();
        nuevo.setNombreProducto(nombre.trim());          // nombre tal como vino del carrito
        nuevo.setMarca(marca);                           // FK a la marca resuelta arriba
        nuevo.setCategoria(cat);                         // FK a la categoría resuelta arriba
        nuevo.setPrecio(BigDecimal.valueOf(price > 0 ? price : 1)); // precio del carrito (mínimo 1 para evitar cero)
        nuevo.setStock(0);                               // stock 0 inicial; el admin lo ajusta desde el panel
        nuevo.setActivo(true);                           // activo por defecto
        nuevo.setCreatedAt(LocalDateTime.now());         // fecha de creación
        nuevo.setUpdatedAt(LocalDateTime.now());         // fecha de última modificación
        em.persist(nuevo);                               // INSERT INTO producto
        em.flush();                                      // forzar INSERT para obtener el ID del producto
        return nuevo;                                    // retornar el producto recién creado
    }

    // Método auxiliar que escapa caracteres especiales para JSON
    // Más completo que otros: maneja tabulaciones y caracteres de control
    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {                 // recorre el string carácter por carácter
            if      (c == '"')  sb.append("\\\"");       // escapa comilla doble
            else if (c == '\\') sb.append("\\\\");       // escapa barra invertida
            else if (c == '\n') sb.append("\\n");        // escapa salto de línea
            else if (c == '\r') sb.append("\\r");        // escapa retorno de carro
            else if (c == '\t') sb.append("\\t");        // escapa tabulación
            else if (c < 0x20)  sb.append(String.format("\\u%04x", (int) c)); // escapa cualquier otro carácter de control
            else                sb.append(c);            // carácter normal: agregar directamente
        }
        return sb.toString();
    }
}
