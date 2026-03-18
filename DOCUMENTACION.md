# DOCUMENTACIÓN TÉCNICA — ANDREYLPZ E-Commerce de Perfumes

> Documento generado para explicar el funcionamiento completo del proyecto:
> arquitectura, base de datos, servlets, entidades JPA, vistas JSP y scripts JavaScript.

---

## ÍNDICE

1. [Stack tecnológico](#1-stack-tecnológico)
2. [Estructura del proyecto](#2-estructura-del-proyecto)
3. [Base de datos — tablas y relaciones](#3-base-de-datos--tablas-y-relaciones)
4. [Capa de persistencia JPA](#4-capa-de-persistencia-jpa)
5. [Entidades JPA (logica/)](#5-entidades-jpa-logica)
6. [Seguridad y sesiones (AuthHelper)](#6-seguridad-y-sesiones-authhelper)
7. [Servlets — explicación línea por línea](#7-servlets--explicación-línea-por-línea)
8. [Vistas JSP y flujo de navegación](#8-vistas-jsp-y-flujo-de-navegación)
9. [Scripts JavaScript](#9-scripts-javascript)
10. [Flujos completos de usuario](#10-flujos-completos-de-usuario)

---

## 1. Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Servidor de aplicaciones | GlassFish / Tomcat (vía NetBeans) |
| Lenguaje backend | Java EE (Jakarta EE) |
| Persistencia | JPA con Hibernate como proveedor |
| Base de datos | MySQL — BD: `Perfumeria_andreylpz` |
| Vistas servidor | JSP (Java Server Pages) |
| Frontend | HTML + CSS + JavaScript vanilla (sin frameworks) |
| Comunicación AJAX | `fetch()` con JSON |

---

## 2. Estructura del proyecto

```
Proyecto/
├── src/java/
│   ├── enums/             ← Enumerados: EstadoPedido, EstadoEntrega, TipoTelefono
│   ├── exceptions/        ← Excepciones personalizadas: StockInsuficienteException, ValidacionException
│   ├── logica/            ← Entidades JPA (mapean tablas de BD)
│   ├── persistencias/     ← Controladores JPA (CRUD por entidad)
│   └── servlets/          ← Servlets HTTP (lógica de negocio y API REST)
├── web/
│   ├── assets/
│   │   ├── estilos/       ← Archivos CSS (style.css, cart.css, megamenu-search.css, etc.)
│   │   ├── imagenes/      ← Imágenes estáticas del sitio
│   │   └── scripts/       ← JavaScript frontend (cart.js, admin.js)
│   └── vistas/            ← Páginas JSP del sitio
│       ├── _navbar.jsp    ← Barra de navegación compartida (include)
│       ├── _footer.jsp    ← Pie de página compartido (include)
│       ├── admin.jsp      ← Panel de administración completo
│       ├── perfil.jsp     ← Perfil de usuario / login
│       ├── registro.jsp   ← Formulario de registro
│       ├── detalle.jsp    ← Detalle de un producto
│       ├── Chanel.jsp     ← Página de marca Chanel (generada dinámicamente)
│       ├── cartas.jsp     ← Página de marca Xerjoff
│       └── ...            ← Demás páginas de marcas y estáticas
└── DOCUMENTACION.md       ← Este archivo
```

---

## 3. Base de datos — tablas y relaciones

### Diagrama de relaciones

```
usuario ──────────── cliente (1:1)
   │                    │
   │ (id_rol)           ├── telefonocliente (1:N)
   ▼                    ├── correocliente (1:N)
  rol                   └── pedido (1:N)
   │                            │
   │ (rolpermiso)               ├── detallepedido (1:N)
   ▼                            │       └── producto (N:1)
 permiso                        ├── pago (1:1)
                                └── envio (1:1)

producto ──── categoria (N:1)
         └─── marca (N:1)
```

### Tablas principales

| Tabla | Descripción |
|-------|-------------|
| `usuario` | Cuenta de acceso al sistema. Tiene correo, contraseña (texto plano), rol y opcionalmente un cliente. Si no tiene cliente, es admin puro. |
| `cliente` | Datos personales del comprador: nombre completo, dirección. Un usuario cliente siempre tiene un registro en esta tabla. |
| `rol` | Tipo de usuario: ADMIN, CLIENTE, SUPERVISOR, etc. |
| `permiso` | Acción específica del sistema: VER_DASHBOARD, EDITAR_PRODUCTOS, etc. |
| `rolpermiso` | Tabla de unión entre rol y permiso. Indica qué permisos tiene cada rol. |
| `producto` | Catálogo de perfumes: nombre, precio, stock, imagen, categoría, marca, activo. |
| `categoria` | Clasificación de productos: Mujer, Hombre, Unisex, etc. |
| `marca` | Marca del perfume: Chanel, Dior, Xerjoff, etc. Tiene paginaUrl con el JSP de su página. |
| `pedido` | Orden de compra: cliente, fecha, estado (PENDIENTE/PAGO/ENVIADO/ENTREGADO/CANCELADO), total. |
| `detallepedido` | Línea de un pedido: qué producto, cuántos, a qué precio unitario. |
| `pago` | Información de pago de un pedido: método, referencia, fecha, estado. |
| `envio` | Información de envío: transportadora, guía, estado, fechas. |
| `telefonocliente` | Teléfonos del cliente (puede tener varios). |
| `correocliente` | Correos adicionales del cliente. |

### Contraseñas
Las contraseñas se guardan en **texto plano** en la columna `contrasena` de la tabla `usuario`. El login compara directamente con `.equals()`. No hay hashing.

---

## 4. Capa de persistencia JPA

### JpaProvider.java
```java
// Singleton que crea UNA SOLA EntityManagerFactory para toda la app.
// El nombre "ProyectoPU" debe coincidir con el persistence-unit en persistence.xml
private static final EntityManagerFactory EMF =
    Persistence.createEntityManagerFactory("ProyectoPU");
```
- `EntityManagerFactory` es costosa de crear (abre pool de conexiones a la BD).
- Se crea una sola vez al iniciar la app y se reutiliza.
- Cada servlet llama `JpaProvider.getEntityManagerFactory().createEntityManager()` para obtener un `EntityManager` por operación.
- El `EntityManager` se cierra en el bloque `finally` de cada servlet.

### Controladores JPA (XxxJpaController)
Cada entidad tiene su propio controlador (ej: `ProductoJpaController`, `PedidoJpaController`).
Todos exponen los mismos métodos básicos:

| Método | SQL equivalente | Descripción |
|--------|----------------|-------------|
| `create(entity)` | `INSERT` | Inserta un registro nuevo |
| `edit(entity)` | `UPDATE` | Actualiza un registro existente |
| `destroy(id)` | `DELETE` | Elimina un registro por ID |
| `findXxx(id)` | `SELECT WHERE id = ?` | Busca por clave primaria |
| `findXxxEntities()` | `SELECT * FROM tabla` | Trae todos los registros |
| `getXxxCount()` | `SELECT COUNT(*)` | Cuenta registros |

---

## 5. Entidades JPA (logica/)

### Producto.java
Mapea la tabla `producto`. Campos principales:

```java
@Column(name = "id_producto")     int idProducto;      // Clave primaria AUTO_INCREMENT
@ManyToOne @JoinColumn("id_categoria") Categoria categoria; // Relación N:1 con categoría
@ManyToOne @JoinColumn("id_marca")     Marca marca;         // Relación N:1 con marca
@Column("nombre_producto")        String nombreProducto; // Nombre del perfume
@Column("precio")                 BigDecimal precio;     // Precio con 2 decimales
@Column("stock")                  int stock;             // Unidades disponibles
@Column("activo")                 boolean activo;        // true=visible en tienda
@Column("imagen_url")             String imagenUrl;      // URL de la imagen
@Column("descripcion")            String descripcion;    // Descripción del producto
```

**Métodos de negocio importantes:**
- `reducirStock(int cantidad)` — descuenta stock. Lanza `StockInsuficienteException` si no hay suficiente.
- `aumentarStock(int cantidad)` — suma stock (para devoluciones o reposición).
- `isDisponible()` — retorna `true` si está activo Y tiene stock > 0.
- `setNombreProducto(String)` — valida que no sea vacío ni supere 200 caracteres.
- `setPrecio(BigDecimal)` — valida que sea mayor a 0.
- `setStock(int)` — valida que no sea negativo.

### Usuario.java
Mapea la tabla `usuario`. Campos principales:

```java
@OneToOne @JoinColumn("id_cliente") Cliente cliente;   // null si es admin puro
@ManyToOne @JoinColumn("id_rol")    Rol rol;           // rol asignado
@Column("correo_usuario")           String correoUsuario; // único en BD
@Column("contrasena")               String contrasena;    // texto plano
@Column("activo")                   boolean activo;       // false = cuenta bloqueada
@Column("ultimo_acceso")            LocalDateTime ultimoAcceso; // última vez que inició sesión
```

**Métodos de negocio importantes:**
- `esAdmin()` — retorna `true` si `cliente == null` (es usuario admin del sistema).
- `registrarAcceso()` — actualiza `ultimoAcceso` y `updatedAt` al momento actual.
- `tienePermiso(String)` — delega la verificación al objeto `Rol`.

### Cliente.java
Mapea la tabla `cliente`. Datos personales del comprador:

```java
@Column("nombre_completo")          String nombreCompleto; // nombre y apellido juntos
@Column("direccion")                String direccion;      // dirección de entrega
@OneToMany(mappedBy = "cliente")    List<Telefonocliente> telefonos; // teléfonos (puede tener varios)
@OneToMany(mappedBy = "cliente")    List<Correocliente> correos;     // correos adicionales
@OneToMany(mappedBy = "cliente")    List<Pedido> pedidos;            // historial de pedidos
```

### Pedido.java
Mapea la tabla `pedido`:

```java
@ManyToOne @JoinColumn("id_cliente") Cliente cliente;    // quién hizo el pedido
@Column("estado") @Enumerated        EstadoPedido estado; // PENDIENTE, PAGO, ENVIADO, ENTREGADO, CANCELADO
@Column("total")                     BigDecimal total;    // suma de todos los ítems
@OneToMany(mappedBy = "pedido")      List<Detallepedido> detalles; // productos del pedido
@OneToOne(mappedBy = "pedido")       Pago pago;          // información de pago
@OneToOne(mappedBy = "pedido")       Envio envio;        // información de envío
```

**Ciclo de vida del estado:**
```
PENDIENTE → PAGO → ENVIADO → ENTREGADO
                ↘ CANCELADO
```

### Rol.java y Permiso.java
- `Rol` — tipo de usuario (ADMIN, CLIENTE, SUPERVISOR...). Tiene una lista de `Rolpermiso`.
- `Permiso` — acción específica (VER_DASHBOARD, EDITAR_PRODUCTOS, ELIMINAR_PRODUCTOS...).
- `Rolpermiso` — tabla de unión: un rol puede tener muchos permisos; un permiso puede estar en muchos roles.

---

## 6. Seguridad y sesiones (AuthHelper)

### Cómo funciona el sistema de permisos

**Al hacer login (SvLogin):**
1. Se busca el usuario en BD por correo.
2. Se compara la contraseña con `.equals()` (texto plano).
3. Se cargan todos los permisos del rol del usuario con JPQL:
   ```java
   SELECT rp FROM Rolpermiso rp JOIN FETCH rp.permiso WHERE rp.rol.idRol = :id
   ```
4. Se guarda en sesión HTTP:
   - `"usuario"` → el objeto `Usuario` completo
   - `"esAdmin"` → `true` si no tiene cliente asociado
   - `"permisosUsuario"` → lista de Strings con nombres de permisos activos

**En cada request posterior (AuthHelper):**
```java
// Verificar si tiene un permiso específico
public static boolean tienePermiso(HttpServletRequest request, String nombrePermiso) {
    HttpSession s = request.getSession(false);    // no crear sesión nueva si no existe
    if (s == null) return false;                  // sin sesión = no autenticado
    if (Boolean.TRUE.equals(s.getAttribute("esAdmin"))) return true; // admin tiene todo
    List<String> permisos = (List<String>) s.getAttribute("permisosUsuario");
    // Recorrer la lista y comparar ignorando mayúsculas
    for (String p : permisos) {
        if (p.equalsIgnoreCase(nombrePermiso)) return true;
    }
    return false;
}
```

**Permisos del sistema usados:**

| Permiso | Quién lo usa |
|---------|-------------|
| `VER_DASHBOARD` | SvDashboard, acceso al panel admin |
| `VER_PRODUCTOS` | SvProductos (modo admin) |
| `EDITAR_PRODUCTOS` | SvProductos (crear, editar, toggleActivo) |
| `ELIMINAR_PRODUCTOS` | SvProductos (eliminar físico) |
| `VER_PEDIDOS` | SvPedidos, SvDetallesPedido |
| `GESTIONAR_PEDIDOS` | SvPedidos (cambiar estado) |
| `GESTIONAR_ENVIOS` | SvEnvios |
| `GESTIONAR_MARCAS` | SvMarcas |
| `GESTIONAR_CATEGORIAS` | SvCategorias |
| `VER_USUARIOS` | SvUsuarios (GET) |
| `EDITAR_USUARIOS` | SvUsuarios (desactivar, cambiarRol) |
| `GESTIONAR_ROLES` | SvPermisos |

---

## 7. Servlets — explicación línea por línea

### SvLogin.java — Autenticación

**Ruta:** `POST /SvLogin`

```java
// 1. Leer correo y contraseña del formulario HTML
String correo     = request.getParameter("correo_electronico");
String contrasena = request.getParameter("contrasena");

// 2. Validar que no vengan vacíos
if (correo == null || contrasena == null || ...)
    response.sendRedirect(".../perfil.jsp?error=...");

// 3. Abrir conexión a BD y buscar usuario activo con ese correo
EntityManager em = JpaProvider.getEntityManagerFactory().createEntityManager();
TypedQuery<Usuario> q = em.createQuery(
    "SELECT u FROM Usuario u WHERE u.correoUsuario = :correo AND u.activo = true", Usuario.class);
// La consulta usa JPQL (Java Persistence Query Language), no SQL directo.
// Hibernate la traduce a: SELECT * FROM usuario WHERE correo_usuario = ? AND activo = 1

// 4. Comparar contraseña en texto plano
if (!usuario.getContrasena().equals(contrasena))
    // → redirigir con error "Correo o contraseña incorrectos"

// 5. Registrar fecha del último acceso en BD
em.getTransaction().begin();
usuario.registrarAcceso();  // actualiza ultimoAcceso = NOW()
em.merge(usuario);          // UPDATE usuario SET ultimo_acceso = ? WHERE id = ?
em.getTransaction().commit();

// 6. Forzar carga de datos del cliente (JPA carga relaciones lazy por defecto)
// Si no se accede aquí, cliente.getNombreCompleto() fuera del EntityManager daría LazyInitializationException
if (usuario.getCliente() != null) {
    usuario.getCliente().getNombreCompleto(); // fuerza carga desde BD
}

// 7. Cargar permisos del rol del usuario con JOIN FETCH (evita N+1 queries)
SELECT rp FROM Rolpermiso rp JOIN FETCH rp.permiso WHERE rp.rol.idRol = :id
// → Una sola query que trae todos los rol_permiso + permisos en un JOIN

// 8. Guardar todo en sesión HTTP (dura hasta que el usuario cierre el navegador o se haga logout)
HttpSession session = request.getSession(true); // crear sesión nueva
session.setAttribute("usuario", usuario);
session.setAttribute("esAdmin", usuario.esAdmin());     // true si sin cliente
session.setAttribute("permisosUsuario", nombresPermisos); // lista de strings

// 9. Redirigir según tipo de usuario
if (puedeVerAdmin) → /vistas/admin.jsp
else               → /vistas/perfil.jsp
```

---

### SvRegistro.java — Registro de nuevos usuarios

**Ruta:** `POST /SvRegistro`

Proceso de validaciones en orden:
1. Campos obligatorios no vacíos (nombre, apellido, correo, contraseña, dirección, fecha nacimiento)
2. Nombre y apellido: solo letras y espacios, mínimo 2 caracteres
3. Correo: formato válido con regex `^[a-zA-Z0-9._%+\-]+@...`
4. Contraseña: entre 8 y 20 caracteres, al menos 1 letra y 1 número
5. Contraseñas coinciden
6. Fecha nacimiento: válida, no futura, edad mínima 18 años
7. Dirección: mínimo 10 caracteres
8. Correo no duplicado en BD

Si pasa todas las validaciones:
```java
// Crear Cliente (datos personales)
Cliente cliente = new Cliente();
cliente.setNombreCompleto(nombre + " " + apellido);
cliente.setDireccion(direccion);
clienteCtrl.create(cliente);  // INSERT INTO cliente(...)

// Buscar rol CLIENTE en BD
Rol rolCliente = ... (buscar por nombre "CLIENTE");

// Crear Usuario (cuenta de acceso)
Usuario usuario = new Usuario();
usuario.setCorreoUsuario(correo);
usuario.setContrasena(contrasena);  // texto plano, sin hash
usuario.setCliente(cliente);        // vincular con sus datos personales
usuario.setRol(rolCliente);         // asignar rol CLIENTE
usuarioCtrl.create(usuario);        // INSERT INTO usuario(...)

// Guardar mensaje de éxito en sesión y redirigir a perfil.jsp
session.setAttribute("registroExitoso", "¡Cuenta creada!");
response.sendRedirect(".../perfil.jsp");
```

---

### SvProductos.java — Catálogo de productos

**Ruta:** `GET /SvProductos` y `POST /SvProductos`

#### GET — Listar productos
```java
ProductoJpaController ctrl = new ProductoJpaController();
List<Producto> productos = ctrl.findProductoEntities(); // SELECT * FROM producto

// Si es llamada pública (sin ?admin=true): filtrar solo activos
if (!esAdminCall) {
    productos = productos.stream()
        .filter(Producto::isActivo)   // solo los que tienen activo = true
        .collect(Collectors.toList());
}
// Si es llamada admin (?admin=true): verificar permiso VER_PRODUCTOS y retornar todos

// Construir JSON manualmente (sin Gson/Jackson)
StringBuilder sb = new StringBuilder("[");
for (Producto p : productos) {
    sb.append("{\"id\":").append(p.getIdProducto())
      .append(",\"nombre\":\"").append(escapeJson(p.getNombreProducto()))
      .append("\",...}");
}
sb.append("]");
// → respuesta: [{"id":1,"nombre":"Chanel No.5","precio":250000,...}, ...]
```

#### POST — Crear, editar, eliminar, activar/desactivar
```java
String accion = request.getParameter("accion"); // "crear", "editar", "eliminar", "toggleActivo"

// ELIMINAR: requiere permiso ELIMINAR_PRODUCTOS
new ProductoJpaController().destroy(id); // DELETE FROM producto WHERE id_producto = ?

// TOGGLE ACTIVO: activar o desactivar sin eliminar
p.setActivo(!p.isActivo());     // invierte el valor booleano
p.setUpdatedAt(LocalDateTime.now());
prodCtrl.edit(p);               // UPDATE producto SET activo = ?, updated_at = ? WHERE id = ?

// CREAR:
Producto p = new Producto();
p.setActivo(true);              // siempre activo al crear
p.setCreatedAt(LocalDateTime.now());
prodCtrl.create(p);             // INSERT INTO producto(...)

// EDITAR:
Producto p = prodCtrl.findProducto(id); // SELECT WHERE id = ?
p.setNombreProducto(nombre);
prodCtrl.edit(p);               // UPDATE producto SET ... WHERE id = ?
```

---

### SvCompra.java — Procesamiento de compras

**Ruta:** `POST /SvCompra`

Este es el servlet más crítico. Procesa el carrito y crea la orden en BD.

```java
// 1. Verificar sesión activa
Usuario usuario = (Usuario) request.getSession(false).getAttribute("usuario");
// Si no hay sesión → error "Debes iniciar sesión"

// 2. Recargar usuario desde BD (el objeto de sesión está "detached" de JPA)
// Si se usara el objeto de sesión directamente para acceder a relaciones lazy, daría error
em = JpaProvider.getEntityManagerFactory().createEntityManager();
Usuario usuarioFresh = em.find(Usuario.class, usuario.getIdUsuario());

// 3. Verificar que tiene cliente asociado (los admins no pueden comprar)
Cliente cliente = usuarioFresh.getCliente();
// Si cliente == null → error "Tu cuenta no tiene cliente asociado"

// 4. Leer el número de ítems del carrito
// El frontend (cart.js) envía: itemCount=2, item_name_0=Chanel No.5, item_price_0=250000, item_qty_0=1, etc.
int itemCount = Integer.parseInt(request.getParameter("itemCount"));

// 5. Iniciar TRANSACCIÓN (todo o nada)
em.getTransaction().begin();

// 6. Crear el pedido base con total=0
Pedido pedido = new Pedido();
pedido.setCliente(em.getReference(Cliente.class, cliente.getIdCliente()));
// em.getReference() crea un proxy lazy sin hacer SELECT (más eficiente si solo necesitamos el ID)
pedido.setEstado(EstadoPedido.PENDIENTE);
pedido.setTotal(BigDecimal.ZERO);
em.persist(pedido);
em.flush(); // forzar INSERT para obtener el ID generado por MySQL (AUTO_INCREMENT)

// 7. Procesar cada ítem
for (int i = 0; i < itemCount; i++) {
    String nombre = request.getParameter("item_name_" + i);

    // Buscar producto en BD por nombre; si no existe, crearlo automáticamente
    Producto producto = buscarOCrearProducto(em, nombre, brand, price);

    // Descontar stock — lanza StockInsuficienteException si stock < cantidad pedida
    producto.reducirStock(qty);
    em.merge(producto); // UPDATE producto SET stock = ? WHERE id = ?

    // Crear línea de detalle
    Detallepedido det = new Detallepedido();
    det.setPedido(pedido);
    det.setProducto(producto);
    det.setCantidad(qty);
    det.setPrecioUnitario(BigDecimal.valueOf(price));
    em.persist(det); // INSERT INTO detallepedido(...)
}

// 8. Actualizar total y confirmar transacción
pedido.setTotal(total);
em.merge(pedido); // UPDATE pedido SET total = ? WHERE id = ?
em.getTransaction().commit(); // COMMIT — todos los cambios se guardan en BD

// 9. Si hubo error de stock: ROLLBACK (ningún cambio queda en BD)
} catch (StockInsuficienteException e) {
    em.getTransaction().rollback(); // revierte TODO: pedido, detalles, stock
    out.print("{\"error\":\"" + e.getMessage() + "\"}");
}
```

---

### SvPedidos.java — Gestión de pedidos (admin)

**Ruta:** `GET /SvPedidos` (requiere VER_PEDIDOS) y `POST /SvPedidos` (requiere GESTIONAR_PEDIDOS)

```java
// GET: lista todos los pedidos con datos del cliente
List<Pedido> pedidos = ctrl.findPedidoEntities(); // SELECT * FROM pedido
// Para cada pedido, consulta los teléfonos del cliente con JPQL:
SELECT t FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.activo = true

// POST accion=cambiarEstado:
EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoStr.toUpperCase()); // valida el enum
pedido.setEstado(nuevoEstado);
pedido.setUpdatedAt(LocalDateTime.now());
em.merge(pedido); // UPDATE pedido SET estado = ?, updated_at = ? WHERE id = ?
```

---

### SvMarcas.java — Gestión de marcas

**Ruta:** `GET /SvMarcas` y `POST /SvMarcas` (requiere GESTIONAR_MARCAS)

**Funcionalidad especial — generación automática de JSP:**
```java
// Al crear una marca nueva, genera automáticamente un archivo JSP físico en el servidor
// Ejemplo: nombre="Dior" → genera "dior.jsp" en web/vistas/

String nombreJsp = nombre.trim().toLowerCase()
    .replace(" ", "_")
    .replaceAll("[^a-z0-9_]", "") + ".jsp"; // "Paco Rabanne" → "paco_rabanne.jsp"

String rutaVistas = request.getServletContext().getRealPath("/vistas/");
// getRealPath convierte la ruta relativa de la app a la ruta absoluta en el disco del servidor

File jspFile = new File(rutaVistas, nombreJsp);
if (!jspFile.exists()) {
    generarJspMarca(jspFile, nombre, descripcion);
    // Escribe el contenido del JSP con un script que carga productos por marca desde SvProductos
}
```

El JSP generado contiene:
- Include de `_navbar.jsp` y `_footer.jsp`
- Script JavaScript que hace `fetch('/SvProductos')` y filtra por nombre de marca
- Cards de productos dinámicos vinculados a `detalle.jsp`

---

### SvDashboard.java — Métricas del panel admin

**Ruta:** `GET /SvDashboard` (requiere VER_DASHBOARD)

```java
// Contar totales con los métodos count() de cada controlador JPA
long totalProductos = new ProductoJpaController().getProductoCount(); // SELECT COUNT(*) FROM producto
long totalUsuarios  = new UsuarioJpaController().getUsuarioCount();
long totalPedidos   = new PedidoJpaController().getPedidoCount();

// Calcular ventas totales (suma de todos los pedidos NO cancelados)
SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.estado <> EstadoPedido.CANCELADO
// COALESCE: si no hay pedidos retorna 0 en vez de null

// Traer los 5 pedidos más recientes para la tabla del dashboard
SELECT p FROM Pedido p ORDER BY COALESCE(p.fechaPedido, p.createdAt) DESC
// COALESCE: usa fechaPedido si existe, si no usa createdAt como respaldo
qRecientes.setMaxResults(5); // LIMIT 5
```

---

### SvEnvios.java — Gestión de envíos

**Ruta:** `GET /SvEnvios?idPedido=X` y `POST /SvEnvios` (requiere GESTIONAR_ENVIOS)

```java
// GET: buscar envío de un pedido específico
SELECT e FROM Envio e WHERE e.pedido.idPedido = :id

// POST sin accion = CREAR envío nuevo
// Verificar que el pedido no tenga ya un envío
SELECT COUNT(e) FROM Envio e WHERE e.pedido.idPedido = :id
// Si > 0 → error "Este pedido ya tiene un envío registrado"

envio.setEstadoEntrega(EstadoEntrega.PREPARANDO); // estado inicial
// Al crear el envío, el pedido cambia automáticamente a ENVIADO:
pedidoMerge.setEstado(EstadoPedido.ENVIADO);

// POST accion=actualizar = cambiar estado del envío
// Si el nuevo estado es ENTREGADO, el pedido también pasa a ENTREGADO automáticamente:
if (nuevoEstado == EstadoEntrega.ENTREGADO) {
    p.setEstado(EstadoPedido.ENTREGADO); // sincronización automática
}
```

---

### SvPermisos.java — Gestión de roles y permisos

**Ruta:** `GET /SvPermisos` y `POST /SvPermisos` (requiere GESTIONAR_ROLES)

Acciones disponibles:

| Acción POST | Descripción |
|-------------|-------------|
| `crearPermiso` | Nuevo permiso (nombre, descripción, módulo) |
| `editarPermiso` | Modifica un permiso existente |
| `togglePermiso` | Activa/desactiva un permiso |
| `crearRol` | Nuevo rol (nombre, descripción) |
| `editarRol` | Modifica un rol existente |
| `toggleRol` | Activa/desactiva un rol (bloquea si tiene usuarios activos) |
| `asignar` | Asigna un permiso a un rol (INSERT en rolpermiso) |
| `revocar` | Quita un permiso de un rol (DELETE de rolpermiso) |

---

### SvMisPedidos.java — Pedidos del cliente logueado

**Ruta:** `GET /SvMisPedidos` (requiere sesión activa)

```java
// Obtener ID del cliente del usuario en sesión
// Consultar pedidos ordenados del más reciente
SELECT p FROM Pedido p WHERE p.cliente.idCliente = :cid ORDER BY p.fechaPedido DESC

// Para cada pedido, cargar los detalles (productos comprados)
SELECT d FROM Detallepedido d WHERE d.pedido.idPedido = :pid

// Y el envío si existe
SELECT e FROM Envio e WHERE e.pedido.idPedido = :pid
```

---

### SvCategorias.java — Gestión de categorías

**Ruta:** `GET /SvCategorias` y `POST /SvCategorias` (requiere GESTIONAR_CATEGORIAS)

Validaciones:
- Nombre obligatorio
- Descripción máximo 120 caracteres
- Nombre no duplicado (case-insensitive)
- No se puede eliminar si tiene productos vinculados

---

### SvUsuarios.java — Gestión de usuarios (admin)

**Ruta:** `GET /SvUsuarios` (requiere VER_USUARIOS) y `POST /SvUsuarios` (requiere EDITAR_USUARIOS)

```java
// GET: para cada usuario, consulta sus teléfonos, correos adicionales y número de pedidos
SELECT t FROM Telefonocliente t WHERE t.cliente.idCliente = :id AND t.activo = true
SELECT c FROM Correocliente c WHERE c.cliente.idCliente = :id AND c.activo = true
SELECT COUNT(p) FROM Pedido p WHERE p.cliente.idCliente = :id

// POST accion=cambiarRol:
u.setRol(nuevoRol); // cambiar el rol cambia automáticamente los permisos en el próximo login
em.merge(u);        // UPDATE usuario SET id_rol = ? WHERE id_usuario = ?
```

---

## 8. Vistas JSP y flujo de navegación

### _navbar.jsp — Barra de navegación compartida
- Se incluye en todas las páginas con `<%@ include file="_navbar.jsp" %>`.
- Carga las marcas desde `SvMarcas` con JavaScript al cargar la página.
- Separa las marcas por género (HOMBRE/MUJER) para el menú desplegable.
- Incluye el buscador en tiempo real (llama a `SvProductos` con `fetch`).
- Muestra el ícono del carrito con badge de cantidad (leído de `localStorage`).

### _footer.jsp — Pie de página compartido
- Links a Historia, Filosofía, Quiénes Somos, Colombia, Venezuela.
- "Contáctanos" enlaza a `perfil.jsp`.

### perfil.jsp — Perfil de usuario y login
- **Sin sesión:** muestra formulario de login y enlace a registro.
- **Con sesión:** muestra datos del usuario, pedidos, teléfonos, correos adicionales, cambiar contraseña.
- Los pedidos se cargan con `fetch('/SvMisPedidos')` en JavaScript al abrir la sección.

### registro.jsp — Formulario de registro
- Formulario HTML que hace `POST` a `SvRegistro`.
- Incluye validación JavaScript del lado cliente (frontend) como primera línea de defensa.
- El servidor (SvRegistro) hace su propia validación independiente.

### admin.jsp — Panel de administración
- Página SPA (Single Page Application) dentro de una página JSP.
- Toda la lógica está en `admin.js`.
- Secciones: Dashboard, Productos, Pedidos, Usuarios, Categorías, Marcas, Roles/Permisos, Mensajes.
- Cada sección carga sus datos con `fetch` al servidor cuando se navega a ella.

### detalle.jsp — Detalle de producto
- Recibe el nombre del producto por URL: `detalle.jsp?nombre=Chanel+No.5`
- Carga los datos del producto desde `SvProductos` con `fetch`.
- Filtra por nombre y `activo = true`.
- Botón "AGREGAR AL CARRITO" guarda en `localStorage` y actualiza el panel del carrito.

### Páginas de marca (Chanel.jsp, cartas.jsp, etc.)
- Generadas automáticamente por `SvMarcas` al crear una marca nueva.
- Cargan productos desde `SvProductos` y filtran por nombre de marca.
- Los productos inactivos no aparecen porque `SvProductos` sin `?admin=true` solo retorna activos.

---

## 9. Scripts JavaScript

### cart.js — Sistema del carrito de compras

**Almacenamiento:** `localStorage` con clave `andreylpz_cart`. El carrito persiste aunque se cierre el navegador.

```javascript
// Estructura de un ítem en el carrito (guardado en localStorage como JSON):
{
    id: "chanel_chanel_no_5",    // nombre_marca + nombre producto en minúsculas sin caracteres especiales
    name: "Chanel No.5",
    brand: "Chanel",
    price: 250000,               // número, en COP
    image: "https://...",
    qty: 2                       // cantidad
}
```

**Funciones principales:**

| Función | Descripción |
|---------|-------------|
| `getCart()` | Lee el array de ítems desde `localStorage` |
| `saveCart(cart)` | Guarda el array en `localStorage` como JSON |
| `addProduct(product)` | Agrega un producto o incrementa su cantidad |
| `renderCart()` | Dibuja los ítems del carrito en el panel lateral |
| `updateBadge()` | Actualiza el número sobre el ícono del carrito |
| `openCart()` / `closeCart()` | Muestra/oculta el panel lateral del carrito |
| `handleCartBodyClick(e)` | Procesa clics en botones − y + y ✕ del carrito |
| `handleCartBodyChange(e)` | Procesa cuando el usuario escribe directamente la cantidad en el input |
| `handleCheckout()` | Envía el carrito a `SvCompra` por POST y procesa la respuesta |
| `initSearch()` | Inicializa el buscador del navbar |

**Cómo se envía el carrito al servidor (checkout):**
```javascript
// Se construye un FormData con los ítems numerados
body.append('itemCount', cart.length);     // cuántos ítems
body.append('item_name_0', item.name);     // nombre del ítem 0
body.append('item_price_0', item.price);   // precio del ítem 0
body.append('item_qty_0', item.qty);       // cantidad del ítem 0
body.append('item_brand_0', item.brand);   // marca del ítem 0
// ... y así para cada ítem (item_name_1, item_price_1, etc.)
```

**Input de cantidad editable:**
- En el panel del carrito, la cantidad se muestra como `<input type="number">` editable.
- El usuario puede hacer clic en `−`/`+` (botones) o escribir directamente el número.
- Al perder el foco o presionar Enter, el evento `change` dispara `handleCartBodyChange`.

### admin.js — Panel de administración

**Estructura general:**
```javascript
(function() {
    'use strict';
    // Caché en memoria (evita recargar desde el servidor constantemente)
    let _products = [];
    let _orders   = [];
    let _users    = [];
    // ...

    // Funciones de comunicación
    function get(servlet)        { return fetch(apiUrl(servlet)).then(r => r.json()); }
    function post(servlet, data) { return fetch(apiUrl(servlet), { method: 'POST', body: new URLSearchParams(data) }); }
})();
```

**Secciones y sus funciones:**

| Sección | Carga con | Función render |
|---------|-----------|----------------|
| Dashboard | `SvDashboard` | Actualiza tarjetas de métricas |
| Productos | `SvProductos?admin=true` | `renderProducts()` |
| Pedidos | `SvPedidos` | `renderOrders()` |
| Usuarios | `SvUsuarios` | `renderUsers()` |
| Categorías | `SvCategorias` | tabla inline en `loadCategorias()` |
| Marcas | `SvMarcas` | tabla inline en `loadMarcas()` |
| Roles/Permisos | `SvPermisos` | `loadRoles()` / `loadPermisosCatalogo()` |

**Polling automático:**
```javascript
// Cada 30 segundos verifica si hay nuevos pedidos
_pollingInterval = setInterval(checkNewOrders, 30000);
// Si hay más pedidos que antes, muestra una notificación toast
```

---

## 10. Flujos completos de usuario

### Flujo 1: Un cliente compra un perfume

```
1. Cliente abre Chanel.jsp
   → JS hace fetch('/SvProductos') → BD retorna productos activos de Chanel
   → Se muestran los cards de productos

2. Cliente hace clic en un producto
   → Redirige a detalle.jsp?nombre=Chanel+No.5
   → JS hace fetch('/SvProductos') → BD retorna todos los activos
   → Filtra por nombre → muestra datos del producto

3. Cliente hace clic en "AGREGAR AL CARRITO"
   → JS guarda en localStorage: { id: "chanel_chanel_no5", name: "...", price: 250000, qty: 1 }
   → Actualiza badge del carrito
   → Abre el panel lateral del carrito
   → renderCart() dibuja el ítem con input editable de cantidad

4. Cliente ajusta la cantidad
   → Clic en + aumenta qty en localStorage y vuelve a renderCart()
   → O escribe el número directamente en el input → evento change → handleCartBodyChange()

5. Cliente hace clic en "FINALIZAR COMPRA"
   → handleCheckout() verifica que haya sesión (fetch('/SvCompra') → si no hay sesión, error)
   → Construye FormData con todos los ítems numerados
   → POST /SvCompra con los datos del carrito

6. SvCompra procesa:
   → Verifica sesión y cliente
   → Abre transacción JPA
   → INSERT INTO pedido (estado=PENDIENTE, total=0)
   → Para cada ítem: busca producto, reducirStock(), INSERT INTO detallepedido
   → UPDATE pedido SET total = suma_total
   → COMMIT (todos los cambios guardados)
   → Retorna: {"ok":true, "idPedido":42, "total":250000}

7. JS limpia el carrito (localStorage vacío) y muestra mensaje de éxito con el número de pedido
```

---

### Flujo 2: Admin activa/desactiva un producto

```
1. Admin accede a admin.jsp (requiere sesión con VER_DASHBOARD)
2. Navega a sección Productos
   → loadProducts() hace fetch('/SvProductos?admin=true')
   → BD retorna TODOS los productos (activos e inactivos)
   → renderProducts() dibuja la tabla con badge "Activo"/"Inactivo" y botón "Activar"/"Desactivar"

3. Admin hace clic en "Desactivar"
   → toggleProduct(id) hace POST /SvProductos con accion=toggleActivo&id=5
   → SvProductos verifica permiso EDITAR_PRODUCTOS
   → Busca el producto: SELECT WHERE id = 5
   → p.setActivo(!p.isActivo())  ← invierte el valor
   → UPDATE producto SET activo = false, updated_at = NOW() WHERE id = 5
   → Retorna {"ok":true, "activo":false}

4. JS muestra toast "Producto desactivado correctamente"
   → loadProducts() recarga la tabla con el nuevo estado

5. En la tienda: fetch('/SvProductos') sin ?admin=true filtra los inactivos
   → El producto ya no aparece para los clientes
```

---

### Flujo 3: Admin crea una marca nueva

```
1. Admin hace clic en "Nueva Marca" en admin.jsp
2. Completa el formulario: nombre="Giorgio Armani", género=HOMBRE
3. saveMarca() hace POST /SvMarcas con nombre y genero

4. SvMarcas:
   → Verifica permiso GESTIONAR_MARCAS
   → Genera nombre del JSP: "giorgio_armani.jsp"
   → INSERT INTO marca (nombre_marca, genero, pagina_url, activo=true)
   → getRealPath("/vistas/") → obtiene ruta absoluta en disco
   → Genera el archivo giorgio_armani.jsp físicamente en el servidor
   → El JSP contiene un script que carga productos de esta marca desde SvProductos
   → Retorna {"ok":true}

5. JS recarga la tabla de marcas
6. La marca aparece en el menú de navegación en el próximo request a _navbar.jsp
7. La URL /vistas/giorgio_armani.jsp ya es accesible para los clientes
```

---

### Flujo 4: Sistema de sesión y permisos

```
1. Usuario hace login con correo y contraseña
   → SvLogin busca en BD: SELECT WHERE correo = ? AND activo = true
   → Compara contraseña con .equals() (texto plano)
   → Carga permisos del rol con JOIN FETCH (una sola query)
   → Guarda en sesión HTTP: usuario, esAdmin, permisosUsuario=[lista de strings]

2. Usuario navega a cualquier página protegida
   → El servlet llama AuthHelper.tienePermiso(request, "VER_DASHBOARD")
   → AuthHelper lee la sesión: request.getSession(false)
   → Busca el nombre del permiso en la lista guardada en sesión
   → Si está → continúa; si no → HTTP 403 con {"error":"Sin permiso: ..."}

3. Usuario cierra sesión
   → SvLogout invalida la sesión: request.getSession().invalidate()
   → Redirect a perfil.jsp
   → Sin sesión, AuthHelper retorna false para cualquier permiso
```

---

*Fin de la documentación. Generada con base en análisis completo del código fuente del proyecto.*
