# Flujo completo de Pedidos — Andreylpz Perfumería

Este documento explica paso a paso cómo viaja un pedido desde que el usuario
hace clic en "Finalizar Compra" hasta que el administrador lo gestiona en el panel,
con la línea de código exacta de cada paso.

---

## DIAGRAMA GENERAL

```
[Usuario en JSP]
      |
      | clic "Finalizar Compra"
      v
[cart.js → handleCheckout()]           ← serializa el carrito y hace POST
      |
      | POST /SvCompra (HTTP)
      v
[SvCompra.java → doPost()]             ← crea Pedido + Detallepedido en BD
      |
      | guarda en MySQL via em.persist()
      v
[MySQL tabla: pedido + detalle_pedido]
      |
      | responde JSON { ok:true, idPedido: X }
      v
[cart.js → showPaymentModal()]         ← muestra modal de método de pago
      |
      | POST /SvPagos
      v
[SvPagos.java → doPost()]              ← registra el pago

========= LADO ADMIN =========

[admin.js → loadOrders()]              ← hace GET al abrir sección Pedidos
      |
      | GET /SvPedidos
      v
[SvPedidos.java → doGet()]             ← llama a PedidoJpaController.findPedidoEntities()
      |
      v
[PedidoJpaController]                  ← ejecuta SELECT * FROM pedido con native query
      |
      | retorna List<Pedido>
      v
[SvPedidos.java]                       ← construye JSON y responde
      |
      v
[admin.js → renderOrders()]            ← pinta la tabla en el HTML

[Admin cambia estado]
      |
      | POST /SvPedidos accion=cambiarEstado
      v
[SvPedidos.java → doPost()]            ← actualiza estado en BD
```

---

## PASO 1 — El usuario llena el carrito (cart.js)

El carrito vive en el **navegador**, guardado en `localStorage`.

```js
// cart.js línea 17
const STORAGE_KEY = 'andreylpz_cart';
```

Cada vez que el usuario hace clic en "Agregar al carrito" en una JSP (ej. Chanel.jsp),
`cart.js` llama a `addProduct()`:

```js
// cart.js → función addProduct()
// Busca si el producto ya está en el array del carrito
const existing = cart.find(i => i.id === product.id);

if (existing) {
    existing.qty++;          // si ya existe, solo aumenta la cantidad
} else {
    cart.push({              // si es nuevo, agrega el objeto completo
        id:    product.id,
        name:  product.name,
        brand: product.brand,
        price: product.price,
        image: product.image,
        qty:   1
    });
}
saveCart(cart);              // guarda el array actualizado en localStorage como JSON
```

`saveCart()` serializa el array a texto y lo almacena:
```js
// cart.js línea 29-31
function saveCart(cart) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(cart));
    // localStorage guarda: '[{"id":5,"name":"Chanel N5","price":250000,"qty":2}, ...]'
}
```

El carrito persiste aunque el usuario recargue la página, porque está en `localStorage`
(no en `sessionStorage` ni en memoria).

---

## PASO 2 — El usuario hace clic en "Finalizar Compra" (cart.js)

El botón del panel lateral dispara `handleCheckout()`:

```js
// cart.js → función handleCheckout()
function handleCheckout() {
    const cart = getCart();          // lee el array del localStorage

    if (cart.length === 0) {
        showCartError('Tu carrito está vacío.');
        return;
    }

    // Construye el FormData que se enviará al servidor
    // El servidor espera: itemCount, item_name_0, item_price_0, item_qty_0, item_brand_0, ...
    const params = new URLSearchParams();
    params.append('itemCount', cart.length);   // cuántos ítems hay en el carrito

    cart.forEach((item, i) => {
        params.append('item_name_'  + i, item.name);   // nombre del producto
        params.append('item_price_' + i, item.price);  // precio unitario
        params.append('item_qty_'   + i, item.qty);    // cantidad
        params.append('item_brand_' + i, item.brand);  // marca (para crear el producto si no existe)
    });

    // Envía POST al servlet SvCompra
    fetch(ctx + '/SvCompra', {
        method: 'POST',
        credentials: 'same-origin',                    // envía la cookie de sesión para que el servidor sepa quién es el usuario
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
        // Ejemplo del body enviado:
        // itemCount=2&item_name_0=Chanel+N5&item_price_0=250000&item_qty_0=2&item_brand_0=Chanel
        // &item_name_1=Dior+Sauvage&item_price_1=180000&item_qty_1=1&item_brand_1=Dior
    })
    .then(r => r.json())
    .then(data => {
        if (data.error) {
            showCartError(data.error);   // ej: "Stock insuficiente para Chanel N5"
            return;
        }
        // Compra exitosa: vaciar el carrito y mostrar modal de pago
        saveCart([]);                    // borra el localStorage del carrito
        updateBadge();                   // actualiza el contador del ícono a 0
        showPaymentModal(data.idPedido, data.total);  // abre modal con el ID del pedido recién creado
    });
}
```

---

## PASO 3 — El servidor recibe la compra (SvCompra.java)

`SvCompra.doPost()` recibe los parámetros y crea el pedido en la base de datos.

### 3.1 — Verifica que el usuario tenga sesión

```java
// SvCompra.java línea 53-58
javax.servlet.http.HttpSession sess = request.getSession(false); // false = no crear sesión nueva si no existe
Usuario usuario = (sess != null) ? (Usuario) sess.getAttribute("usuario") : null;
// La sesión fue creada por SvLogin al iniciar sesión; contiene el objeto Usuario

if (usuario == null) {
    out.print("{\"error\":\"Debes iniciar sesión para realizar una compra.\"}");
    return;   // corta la ejecución aquí; no procesa la compra
}
```

### 3.2 — Recarga el usuario desde BD y verifica que tenga cliente

```java
// SvCompra.java línea 64-76
em = JpaProvider.getEntityManagerFactory().createEntityManager();
Usuario usuarioFresh = em.find(Usuario.class, usuario.getIdUsuario());
// SELECT * FROM usuario WHERE id_usuario = ? (el objeto de sesión puede estar "desconectado" de JPA)

Cliente cliente = usuarioFresh.getCliente();
// Relación @OneToOne en Usuario → Cliente
// Los admins puros no tienen cliente, por eso se verifica:
if (cliente == null) {
    out.print("{\"error\":\"Tu cuenta no tiene cliente asociado.\"}");
    return;
}
```

### 3.3 — Lee cuántos ítems trae el carrito

```java
// SvCompra.java línea 80-87
String itemCountStr = request.getParameter("itemCount");  // lee "2" del POST
int itemCount = Integer.parseInt(itemCountStr);           // convierte a entero: 2

if (itemCount <= 0) {
    out.print("{\"error\":\"El carrito está vacío.\"}");
    return;
}
```

### 3.4 — Crea el Pedido en BD

```java
// SvCompra.java línea 89-102
em.getTransaction().begin();    // inicia la transacción: todo se guarda o nada

Pedido pedido = new Pedido();
pedido.setCliente(em.getReference(Cliente.class, cliente.getIdCliente()));
// getReference() crea un proxy lazy del Cliente sin hacer SELECT extra;
// solo guarda el ID para la FK en la tabla pedido

pedido.setEstado(EstadoPedido.PENDIENTE);   // estado inicial siempre PENDIENTE
pedido.setFechaPedido(LocalDateTime.now()); // fecha y hora actual
pedido.setCreatedAt(LocalDateTime.now());
pedido.setUpdatedAt(LocalDateTime.now());
pedido.setActivo(true);
pedido.setTotal(BigDecimal.ZERO);           // total provisional en 0, se calcula después

em.persist(pedido);   // INSERT INTO pedido (id_cliente, estado, fecha_pedido, ...) VALUES (...)
em.flush();           // fuerza el INSERT ahora para que MySQL asigne el id_pedido
                      // sin flush(), el INSERT podría quedar pendiente y los detalles
                      // no tendrían id_pedido para su FK
```

### 3.5 — Procesa cada ítem del carrito y crea los Detallepedido

```java
// SvCompra.java línea 104-143
BigDecimal total = BigDecimal.ZERO;

for (int i = 0; i < itemCount; i++) {
    // Lee los parámetros del POST para este ítem (índice i)
    String nombre = request.getParameter("item_name_"  + i);   // "Chanel N5"
    String priceS = request.getParameter("item_price_" + i);   // "250000"
    String qtyS   = request.getParameter("item_qty_"   + i);   // "2"

    double price = Double.parseDouble(priceS);  // 250000.0
    int qty      = Integer.parseInt(qtyS);      // 2

    // Busca el producto en BD por nombre; si no existe, lo crea automáticamente
    Producto producto = buscarOCrearProducto(em, nombre, request.getParameter("item_brand_" + i), price);

    // Descuenta el stock del producto
    producto.reducirStock(qty);   // stock -= qty; lanza StockInsuficienteException si no alcanza
    em.merge(producto);           // UPDATE producto SET stock=? WHERE id_producto=?

    // Crea la línea de detalle
    Detallepedido det = new Detallepedido();
    det.setPedido(pedido);                              // FK id_pedido
    det.setProducto(producto);                          // FK id_producto
    det.setCantidad(qty);                               // 2
    det.setPrecioUnitario(BigDecimal.valueOf(price));   // 250000.00
    det.setActivo(true);
    em.persist(det);   // INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario, activo)

    total = total.add(BigDecimal.valueOf(price * qty)); // acumula: 250000 × 2 = 500000
}

pedido.setTotal(total);   // actualiza el total real calculado: 500000 + 180000 = 680000
em.merge(pedido);         // UPDATE pedido SET total=680000 WHERE id_pedido=?
em.getTransaction().commit(); // confirma todo: pedido, detalles y stock descontado
```

### 3.6 — Responde al frontend

```java
// SvCompra.java línea 149
out.print("{\"ok\":true,\"idPedido\":" + pedido.getIdPedido() + ",\"total\":" + total + "}");
// Ejemplo: {"ok":true,"idPedido":42,"total":680000}
```

---

## PASO 4 — El usuario selecciona método de pago (cart.js)

Con el `idPedido` recibido, `cart.js` abre el modal de pago:

```js
// cart.js → función showPaymentModal(idPedido, total)
// Muestra opciones: Tarjeta / Transferencia / Efectivo

// Cuando el usuario confirma, se llama confirmarPago():
window.confirmarPago = function(idPedido, total) {
    var metodo = document.querySelector('input[name="pago-metodo"]:checked').value;
    // metodo = "TARJETA" | "TRANSFERENCIA" | "EFECTIVO"

    var params = 'idPedido=' + idPedido +
                 '&metodo=' + metodo +
                 '&monto='  + total +
                 '&referencia=' + referencia;

    fetch(ctx + '/SvPagos', {
        method: 'POST',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
        // Ejemplo: idPedido=42&metodo=TARJETA&monto=680000&referencia=TXN-98765
    })
    .then(r => r.json())
    .then(data => {
        if (data.estadoActualizado === 'PAGO') {
            // El servidor detectó que el pago cubre el total y cambió el estado a PAGO
            showOrderConfirmation(idPedido, total, metodo);  // modal de confirmación final
        }
    });
}
```

---

## PASO 5 — El admin abre la sección Pedidos (admin.js)

Cuando el administrador hace clic en "Pedidos" en el menú lateral, `admin.js` ejecuta:

```js
// admin.js línea 76
if (sectionId === 'pedidos') loadOrders();
```

`loadOrders()` hace GET a SvPedidos:

```js
// admin.js → función loadOrders()
function loadOrders() {
    get('SvPedidos')   // helper que hace fetch GET al servlet /SvPedidos
    .then(orders => {
        // orders = array JSON de pedidos que vino del servidor
        // Ejemplo: [{ id:42, estado:"PENDIENTE", total:680000, cliente:"Juan Pérez", ... }]

        _orders = orders;       // guarda en caché para no repetir el fetch en cada acción
        renderOrders(orders);   // pinta la tabla en el HTML
    });
}
```

---

## PASO 6 — SvPedidos recibe el GET y consulta la BD (SvPedidos.java)

```java
// SvPedidos.java → doGet() línea 45-46
PedidoJpaController ctrl = new PedidoJpaController();
// Crea una instancia del controlador; internamente usa JpaProvider.getEntityManagerFactory()

List<Pedido> pedidos = ctrl.findPedidoEntities();
// Llama al método público sin parámetros → delega al privado con all=true
```

---

## PASO 7 — PedidoJpaController ejecuta el SELECT (PedidoJpaController.java)

```java
// PedidoJpaController.java → findPedidoEntities()
public List<Pedido> findPedidoEntities() {
    return findPedidoEntities(true, -1, -1);
    // all=true: trae todos los registros sin LIMIT ni OFFSET
}

// Delega al método privado:
private List<Pedido> findPedidoEntities(boolean all, int maxResults, int firstResult) {
    EntityManager em = getEntityManager();  // abre conexión a MySQL
    try {
        String sql = "SELECT * FROM pedido";
        // all=true → el if no se ejecuta, el SQL queda: "SELECT * FROM pedido"

        Query q = em.createNativeQuery(sql, Pedido.class);
        // createNativeQuery con Pedido.class: EclipseLink mapea automáticamente cada fila
        // usando las anotaciones @Column de la entidad:
        //   columna id_pedido     → campo idPedido     (@Column name="id_pedido")
        //   columna id_cliente    → objeto cliente      (@JoinColumn name="id_cliente") → carga el Cliente EAGER
        //   columna fecha_pedido  → campo fechaPedido
        //   columna estado        → enum EstadoPedido   (@Enumerated STRING: "PENDIENTE" → EstadoPedido.PENDIENTE)
        //   columna total         → campo total         (BigDecimal)
        //   columna activo        → campo activo        (boolean)

        return q.getResultList();
        // Ejecuta: SELECT * FROM pedido
        // Retorna: List<Pedido> con todos los registros mapeados a objetos Java
    } finally {
        em.close();  // cierra la conexión pase lo que pase
    }
}
```

---

## PASO 8 — SvPedidos construye el JSON y responde

```java
// SvPedidos.java línea 51-84
StringBuilder sb = new StringBuilder("[");  // inicia el array JSON

for (int i = 0; i < pedidos.size(); i++) {
    Pedido p = pedidos.get(i);
    if (i > 0) sb.append(",");

    sb.append("{");
    sb.append("\"id\":"     ).append(p.getIdPedido()).append(",");
    // Ejemplo: "id":42,

    sb.append("\"estado\":\"").append(p.getEstado().name()).append("\",");
    // p.getEstado() retorna EstadoPedido.PENDIENTE → .name() = "PENDIENTE"
    // Ejemplo: "estado":"PENDIENTE",

    sb.append("\"total\":").append(p.getTotal()).append(",");
    // p.getTotal() = BigDecimal 680000 → se serializa como número
    // Ejemplo: "total":680000,

    sb.append("\"cliente\":\"").append(p.getCliente().getNombreCompleto()).append("\",");
    // p.getCliente() funciona porque @ManyToOne es EAGER por defecto:
    // EclipseLink ya cargó el Cliente al mapear el SELECT de arriba
    // Ejemplo: "cliente":"Juan Pérez",

    // Consulta los teléfonos del cliente con JPQL separado
    List<Telefonocliente> tels = emGet.createQuery(
        "SELECT t FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.activo = true",
        Telefonocliente.class).setParameter("id", idC).getResultList();
    // Ejemplo resultado: ["3001234567", "3109876543"]
    sb.append("}");
}
sb.append("]");
out.print(sb.toString());
// JSON final enviado al navegador:
// [{"id":42,"estado":"PENDIENTE","total":680000,"cliente":"Juan Pérez",
//   "direccionCliente":"Calle 45 #12-30","telefonosCliente":["3001234567"]},
//  {"id":41,"estado":"PAGO","total":320000,...}]
```

---

## PASO 9 — admin.js pinta la tabla (admin.js)

```js
// admin.js → función renderOrders(orders)
function renderOrders(orders) {
    const tbody = document.getElementById('orders-tbody');

    tbody.innerHTML = orders.map(o => {
        return '<tr>' +
            '<td>#' + o.id + '</td>' +                   // columna ID
            '<td>' + o.cliente + '</td>' +               // columna Cliente
            '<td>' + fmt(o.total) + '</td>' +            // columna Total (formateado: $680.000)
            '<td>' + o.fecha.substring(0,10) + '</td>' + // columna Fecha (solo YYYY-MM-DD)
            '<td>' + statusBadge(o.estado) + '</td>' +   // columna Estado (badge coloreado)

            // Select para cambiar el estado del pedido
            '<td><select id="est-' + o.id + '">' +
                ESTADOS.map(e =>
                    '<option value="' + e + '"' + (e === o.estado ? ' selected' : '') + '>' + e + '</option>'
                ).join('') +
            '</select>' +
            '<button onclick="adminApp.cambiarEstado(' + o.id + ')">Guardar</button></td>' +

            // Botones de acción
            '<td>' +
            '<button onclick="adminApp.abrirPago(' + o.id + ')">Pago</button>' +
            '<button onclick="adminApp.verDetalle(' + o.id + ')">Detalle</button>' +
            '</td>' +
        '</tr>';
    }).join('');
}
```

---

## PASO 10 — El admin cambia el estado del pedido (admin.js → SvPedidos)

El admin selecciona "ENVIADO" en el `<select>` y hace clic en "Guardar":

```js
// admin.js → función cambiarEstado(idPedido)
function cambiarEstado(idPedido) {
    const sel = document.getElementById('est-' + idPedido);
    const nuevoEstado = sel.value;   // "ENVIADO"

    post('SvPedidos', {
        accion:    'cambiarEstado',
        idPedido:  idPedido,    // 42
        estado:    nuevoEstado  // "ENVIADO"
    })
    // POST body: accion=cambiarEstado&idPedido=42&estado=ENVIADO
    .then(r => {
        if (r.error) { showAdminAlert(r.error); return; }

        // Actualiza el badge en la fila sin recargar toda la tabla
        const badge = document.querySelector('#est-' + idPedido + ' ~ .badge');
        if (badge) badge.textContent = 'Enviado';

        // Actualiza el objeto en el caché local
        const o = _orders.find(x => x.id === idPedido);
        if (o) o.estado = nuevoEstado;

        showToast('Pedido #' + idPedido + ' → Enviado');  // notificación flotante
    });
}
```

`SvPedidos.doPost()` recibe el POST:

```java
// SvPedidos.java → doPost() línea 110-143
String accion    = request.getParameter("accion");   // "cambiarEstado"
String idStr     = request.getParameter("idPedido"); // "42"
String estadoStr = request.getParameter("estado");   // "ENVIADO"

int idPedido = Integer.parseInt(idStr);              // 42
EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoStr.toUpperCase());
// EstadoPedido.valueOf("ENVIADO") → EstadoPedido.ENVIADO

EntityManager em = JpaProvider.getEntityManagerFactory().createEntityManager();
em.getTransaction().begin();

Pedido pedido = em.find(Pedido.class, idPedido);
// SELECT * FROM pedido WHERE id_pedido = 42

pedido.setEstado(nuevoEstado);           // cambia el enum a ENVIADO
pedido.setUpdatedAt(LocalDateTime.now()); // registra cuándo se cambió el estado

em.merge(pedido);
// UPDATE pedido SET estado='ENVIADO', updated_at='2026-04-11 15:30:00' WHERE id_pedido=42

em.getTransaction().commit();

out.print("{\"ok\":true,\"idPedido\":42,\"estado\":\"ENVIADO\"}");
// El frontend recibe esto y actualiza el badge en pantalla sin recargar
```

---

## PASO 11 — Polling automático de nuevos pedidos (admin.js)

Cada 30 segundos, `admin.js` consulta el contador de pedidos en SvDashboard:

```js
// admin.js → función checkNewOrders()
function checkNewOrders() {
    get('SvDashboard').then(data => {
        const count = parseInt(data.pedidos, 10);
        // data.pedidos viene de PedidoJpaController.getPedidoCount()
        // que ejecuta: SELECT COUNT(*) FROM pedido

        if (count > _lastPedidoCount) {
            // El contador aumentó → hay pedido(s) nuevo(s)
            const diff = count - _lastPedidoCount;
            showToast(diff + ' nuevo(s) pedido(s) pendiente(s)');
            // Muestra el toast: "🛍️ Nuevo pedido recibido"

            _lastPedidoCount = count;

            // Recarga la tabla de pedidos si el admin está en esa sección
            if (active.id === 'section-pedidos') loadOrders();
        }
    });
}

// Se inicia al cargar el panel admin:
_pollingInterval = setInterval(checkNewOrders, 30000); // cada 30 segundos
```

---

## RESUMEN DE RESPONSABILIDADES

| Capa | Archivo | Qué hace |
|---|---|---|
| **Frontend cliente** | `cart.js` | Guarda el carrito en localStorage, envía el POST a SvCompra, muestra modal de pago |
| **Frontend admin** | `admin.js` | Pide la lista de pedidos con GET, pinta la tabla, envía cambios de estado con POST, hace polling cada 30s |
| **Servlet compra** | `SvCompra.java` | Recibe el carrito, crea Pedido + Detallepedido en BD dentro de una transacción, descuenta stock |
| **Servlet pedidos** | `SvPedidos.java` | GET → lista pedidos; POST → cambia estado |
| **Controlador JPA** | `PedidoJpaController.java` | Ejecuta el SQL nativo contra MySQL y mapea filas a objetos Pedido |
| **Entidad** | `Pedido.java` | Define la estructura de la tabla `pedido` con anotaciones JPA |
| **Base de datos** | MySQL tabla `pedido` | Persiste el registro con id, estado, total, FK a cliente |
