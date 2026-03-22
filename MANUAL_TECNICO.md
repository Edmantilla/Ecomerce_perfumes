# Manual Técnico — Andreylpz Perfumería (Java EE / JSP / Servlets / JPA / MySQL)

## 1. Información general

- **Nombre del sistema**: Andreylpz Perfumería (E-Commerce de Perfumes)
- **Tipo**: Aplicación web (Java EE)
- **Patrón predominante**: MVC (JSP como Vista, Servlets como Controlador, JPA/Entidades como Modelo)
- **Persistencia**: JPA (EclipseLink) con MySQL
- **Frontend**: JSP + HTML/CSS + JavaScript (vanilla)
- **Autenticación/Autorización**: Sesión HTTP + Roles/Permisos

## 2. Objetivo del sistema

Permitir a clientes navegar el catálogo de perfumes, agregar productos al carrito y registrar pedidos. Adicionalmente, incluir un panel de administración para gestionar catálogo (productos, categorías, marcas), pedidos, usuarios, envíos y seguridad (roles/permisos).

## 3. Alcance

- **Incluye**:
  - Catálogo público con productos activos.
  - Carrito de compras en el navegador (localStorage).
  - Checkout que crea pedidos en BD.
  - Panel de administración (dashboard, CRUD, gestión de permisos).
  - Gestión de envíos y estado de pedidos.
- **No incluye** (en el estado actual):
  - Integración real con pasarela de pagos (Pago es entidad/modelo, pero no hay gateway).
  - Hashing de contraseñas (en BD se guarda texto plano).

## 4. Stack tecnológico

- **Lenguaje**: Java
- **Web**: Java EE (Servlets + JSP)
- **ORM**: JPA 2.2 con **EclipseLink**
- **Base de datos**: MySQL
- **Build**: Ant (proyecto NetBeans)
- **Servidor**: GlassFish o Tomcat (según configuración de tu entorno NetBeans)

## 5. Requisitos de instalación

### 5.1 Software

- Java JDK (compatible con tu servidor/NetBeans)
- NetBeans (recomendado, por estructura Ant)
- Servidor Java EE:
  - GlassFish (típico en NetBeans para Java EE)
  - o Tomcat (si tu proyecto está configurado para ello)
- MySQL Server
- Driver MySQL: `com.mysql.cj.jdbc.Driver`

### 5.2 Hardware (referencia)

- CPU: 2 núcleos o más
- RAM: 4 GB mínimo (8 GB recomendado)
- Disco: 2 GB libres (más para BD)

## 6. Estructura del proyecto

Rutas principales (según workspace):

- **Servlets**: `src/java/servlets/`
- **Entidades JPA**: `src/java/logica/`
- **Persistencia/EMF**: `src/java/persistencias/`
- **Config**: `src/java/config/` (por ejemplo `AppInitializer.java`)
- **Vistas (JSP)**: `web/vistas/`
- **Recursos**:
  - CSS: `web/assets/estilos/`
  - JS: `web/assets/scripts/`
- **SQL**: `database/perfumeria_andreylpz.sql`

## 7. Base de datos

### 7.1 Nombre y conexión

La conexión está definida en `src/conf/persistence.xml`:

- **Persistence Unit**: `ProyectoPU`
- **URL**: `jdbc:mysql://localhost:3306/Perfumeria_andreylpz?serverTimezone=UTC`
- **Usuario**: `root`
- **Password**: `123456789`

Recomendación técnica:

- Cambiar credenciales en `persistence.xml` para tu ambiente.
- No versionar contraseñas reales.

### 7.2 Script de creación

Archivo:

- `database/perfumeria_andreylpz.sql`

Contiene:

- `DROP DATABASE IF EXISTS Perfumeria_andreylpz;`
- `CREATE DATABASE Perfumeria_andreylpz ...`
- Creación de tablas y claves foráneas.
- Datos semilla (roles, permisos, etc.).

### 7.3 Modelo relacional (resumen)

Tablas principales:

- **categoria** 1:N **producto**
- **marca** 1:N **producto**
- **cliente** 1:N **pedido**
- **pedido** 1:N **detalle_pedido**
- **pedido** 1:1 **pago**
- **pedido** 1:1 **envio**
- **rol** 1:N **usuario**
- **rol** N:M **permiso** (vía **rol_permiso**)

## 8. Persistencia (JPA / EclipseLink)

### 8.1 Persistence Unit

En `persistence.xml` se declara el provider:

- `org.eclipse.persistence.jpa.PersistenceProvider`

Y se listan las entidades (ej: `logica.Producto`, `logica.Pedido`, `logica.Usuario`, etc.).

### 8.2 EntityManagerFactory (singleton)

La clase `persistencias.JpaProvider` centraliza una sola instancia de `EntityManagerFactory` para toda la app.

### 8.3 Inicialización y cierre (AppInitializer)

`config.AppInitializer` (listener) ejecuta:

- En arranque: `JpaProvider.getEntityManagerFactory();`
- En apagado: `JpaProvider.getEntityManagerFactory().close();`

Objetivo:

- Inicializar el pool/conexión al iniciar la app.
- Liberar recursos al detener la aplicación.

## 9. Arquitectura de la aplicación

### 9.1 Capa de presentación

- **JSP** generan HTML.
- `web/vistas/_navbar.jsp` y `web/vistas/_footer.jsp` funcionan como includes compartidos.
- Las páginas de marca son JSP físicas (algunas generadas automáticamente) que consultan el catálogo por marca.

### 9.2 Capa de control (Servlets)

Los servlets exponen endpoints (muchos retornan JSON) y ejecutan reglas de negocio. Ejemplos:

- `SvLogin` (autenticación)
- `SvRegistro` (registro)
- `SvProductos`, `SvCategorias`, `SvMarcas` (CRUD admin)
- `SvCompra` (checkout)
- `SvPedidos`, `SvDetallesPedido` (gestión/consulta)
- `SvEnvios` (envíos)
- `SvPermisos` (roles/permisos)

### 9.3 Capa de modelo

- Entidades JPA en `logica/`.
- Validaciones de negocio en setters (ej: nombre de categoría, formato teléfono, etc.).

## 10. Configuración de la aplicación

### 10.1 Sesión

En `web/WEB-INF/web.xml`:

- `session-timeout`: **30** minutos.

### 10.2 Página de inicio

En `web.xml`:

- `welcome-file`: `index.jsp`

### 10.3 Mapeo de servlets

En `web.xml` se encuentran mapeados los principales endpoints, por ejemplo:

- `/SvLogin`
- `/SvRegistro`
- `/SvProductos`
- `/SvCompra`
- `/SvPermisos`
- `/SvEnvios`

## 11. Seguridad

### 11.1 Autenticación

- Basada en sesión HTTP.
- `SvLogin` valida credenciales contra tabla `usuario`.

Nota técnica importante:

- En `usuario.contrasena` se guarda texto plano. Esto es un riesgo.
- Recomendación: migrar a hash (BCrypt/Argon2) y forzar cambio de contraseñas.

### 11.2 Autorización (roles/permisos)

- Un usuario tiene un `Rol`.
- Un `Rol` tiene permisos a través de `Rolpermiso`.
- `AuthHelper.tienePermiso(request, "...")` bloquea acciones si no hay permiso.

## 12. Flujo técnico del carrito y compra

### 12.1 Carrito (cliente)

- Se maneja con JavaScript en el navegador.
- Se persiste en `localStorage`.

### 12.2 Checkout (servidor)

- `SvCompra` recibe ítems del carrito.
- Valida stock/disponibilidad.
- Crea:
  - `Pedido`
  - `Detallepedido` por producto
- Descuenta stock.
- Maneja transacciones (commit/rollback ante error).

## 13. Instalación y puesta en marcha (paso a paso)

### 13.1 Preparar base de datos

1. Abrir MySQL (Workbench o consola).
2. Ejecutar el script:
   - `database/perfumeria_andreylpz.sql`

### 13.2 Configurar credenciales JPA

1. Abrir `src/conf/persistence.xml`.
2. Ajustar:
   - `javax.persistence.jdbc.user`
   - `javax.persistence.jdbc.password`
   - (si aplica) host/puerto de MySQL

### 13.3 Ejecutar en NetBeans

1. Abrir el proyecto `Proyecto`.
2. Configurar servidor (GlassFish/Tomcat) en NetBeans.
3. Ejecutar el proyecto.
4. Navegar a:
   - `http://localhost:<puerto>/<contexto>/index.jsp`

## 14. Datos de acceso (ambiente local)

Si el script SQL incluye usuario admin, típicamente:

- **Correo admin**: `admin@andreylpz.com`
- **Contraseña**: `admin123`

(Verifica en tu tabla `usuario` y/o en la sección de datos semilla del SQL.)

## 15. Operación y mantenimiento

### 15.1 Respaldos

- Respaldar BD:
  - `mysqldump -u root -p Perfumeria_andreylpz > backup.sql`
- Respaldar proyecto:
  - repositorio + carpeta `web/vistas/` (incluye JSPs generados por marcas)

### 15.2 Logs

- Revisar logs del servidor (GlassFish/Tomcat) para:
  - errores de conexión a BD
  - errores de servlets
  - excepciones en transacciones

### 15.3 Limpieza de recursos

- `AppInitializer` cierra el `EntityManagerFactory` al detener la aplicación.

## 16. Pruebas (guía técnica mínima)

- **Registro**:
  - Crear usuario cliente.
  - Verificar inserciones en `cliente` y `usuario`.
- **Login**:
  - Verificar sesión y redirecciones.
- **Admin**:
  - Crear categoría/marca/producto.
  - Activar/desactivar productos.
- **Compra**:
  - Agregar productos al carrito.
  - Ejecutar checkout.
  - Verificar `pedido` + `detalle_pedido` + descuento de `stock`.
- **Envíos**:
  - Crear envío, cambiar estado a ENTREGADO y verificar sincronización con estado de pedido.

## 17. Riesgos técnicos y recomendaciones

- **Contraseñas en texto plano**:
  - Migrar a hashing.
- **Credenciales en `persistence.xml`**:
  - Usar variables de entorno o configuración externa.
- **Generación de JSP por marcas**:
  - Asegurar permisos de escritura en `web/vistas/`.
  - Controlar nombres para evitar rutas inválidas.

---

**Fin del Manual Técnico**
