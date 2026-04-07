# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Andreylpz Perfumería** — a Java EE e-commerce web application for selling perfumes. Built with NetBeans, deployed to Apache Tomcat, using JPA/EclipseLink with MySQL.

## Build & Run

This project is built and deployed through **NetBeans IDE** using Ant. There is no standalone CLI build command intended for direct use outside the IDE. The build output goes to `C:/ProyectoBuild/` (configured in `nbproject/project.properties`).

- **Server**: Apache Tomcat (Java EE 8 Web Profile)
- **Java**: 21
- **WAR name**: `Proyecto.war`
- **Database**: MySQL at `localhost:3306`, database `Perfumeria_andreylpz`, user `root`, password `123456789`
- **DB schema**: `database/perfumeria_andreylpz.sql`

To deploy manually with Ant:
```
ant -f build.xml
```

There are no automated tests in the `test/` directory.

## Architecture

The app follows a classic MVC pattern without a framework:

### Layer breakdown

**`src/java/logica/`** — JPA entities (model layer). Each class maps 1:1 to a DB table. Key entities: `Usuario`, `Cliente`, `Producto`, `Pedido`, `Detallepedido`, `Pago`, `Envio`, `Rol`, `Permiso`, `Rolpermiso`.

**`src/java/persistencias/`** — JPA controllers (one per entity, e.g. `ProductoJpaController`). All expose `create`, `edit`, `destroy`, `findXxx`, `findXxxEntities`, `getXxxCount`. The singleton `JpaProvider` holds the single `EntityManagerFactory` for the persistence unit `ProyectoPU`. `ControladoraPersistencia` aggregates all JPA controllers.

**`src/java/servlets/`** — HTTP Servlets (controller layer). Each servlet handles one resource area. Most return JSON via `response.setContentType("application/json")` for AJAX calls from the admin panel. Some do HTTP redirects for form submissions.

**`web/vistas/`** — JSP views. `_navbar.jsp` and `_footer.jsp` are shared includes. `admin.jsp` is the full admin SPA. Brand pages (`Chanel.jsp`, `Cristian_dior.jsp`, etc.) are individual JSPs per brand.

**`web/assets/scripts/`** — Vanilla JS frontend. `cart.js` manages the shopping cart (stored in `sessionStorage`). `admin.js` handles all admin panel AJAX calls.

### Session attributes (set by `SvLogin`)

| Attribute | Type | Description |
|---|---|---|
| `usuario` | `String` | Username of logged-in user |
| `idUsuario` | `int` | User's DB ID |
| `esAdmin` | `Boolean` | True if admin with no associated client |
| `permisosUsuario` | `List<String>` | Permission names for role-based access |
| `idCliente` | `int` | Client ID (if user is a customer) |

### Authorization pattern

Every admin servlet checks `AuthHelper.esAdmin(request)` or `AuthHelper.tienePermiso(request, "PERMISSION_NAME")` at the top of `doGet`/`doPost`. Permission names are strings like `"VER_DASHBOARD"`, `"EDITAR_PRODUCTOS"`, `"GESTIONAR_ENVIOS"`. Admin users (no associated client) bypass all permission checks.

### Servlet URL mapping

All servlets map to `/<ClassName>` (e.g. `/SvLogin`, `/SvProductos`). Mappings are defined in `web/WEB-INF/web.xml`.

### Order state machine

`EstadoPedido` enum: `PENDIENTE → PAGO → ENVIADO → ENTREGADO / CANCELADO`

`EstadoEntrega` enum tracks shipment state separately.

### Key design constraints

- Passwords are stored and compared as **plain text** (no hashing). This is intentional per current design.
- JPA transactions are `RESOURCE_LOCAL` — managed manually (`em.getTransaction().begin()` / `commit()` / `rollback()`). Always close `EntityManager` in a `finally` block.
- The shopping cart lives entirely in the browser's `sessionStorage` as a JSON array of `itemCarrito`-like objects. It is only persisted to DB when `SvCompra` processes the checkout.
- New entities added to `logica/` must be registered in `src/conf/persistence.xml` under `<class>`.
