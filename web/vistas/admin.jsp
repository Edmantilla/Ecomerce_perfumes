<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%
    // Proteger acceso: solo usuarios con VER_DASHBOARD o esAdmin
    Object usuarioSess = session.getAttribute("usuario");
    if (usuarioSess == null) {
        response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp");
        return;
    }
    boolean esAdminPuro = Boolean.TRUE.equals(session.getAttribute("esAdmin"));
    @SuppressWarnings("unchecked")
    List<String> permisosUsuario = (List<String>) session.getAttribute("permisosUsuario");
    boolean puedeAcceder = esAdminPuro ||
        (permisosUsuario != null && permisosUsuario.stream().anyMatch(p -> p.equalsIgnoreCase("VER_DASHBOARD")));
    if (!puedeAcceder) {
        response.sendRedirect(request.getContextPath() + "/vistas/perfil.jsp?error=Sin+acceso+al+panel");
        return;
    }
    // Helper lambda para chequear permisos
    java.util.function.Predicate<String> perm = p -> esAdminPuro ||
        (permisosUsuario != null && permisosUsuario.stream().anyMatch(x -> x.equalsIgnoreCase(p)));
    String nombreUsuario = ((logica.Usuario) usuarioSess).getCliente() != null
        ? ((logica.Usuario) usuarioSess).getCliente().getNombreCompleto()
        : "Admin";
    String rolUsuario = ((logica.Usuario) usuarioSess).getRol() != null
        ? ((logica.Usuario) usuarioSess).getRol().getNombreRol()
        : "Administrador";
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Admin – ANDREYLPZ</title>
    <link rel="stylesheet" href="../assets/estilos/admin.css">
</head>

<body class="admin-body">

    <!-- ======================================
         SIDEBAR
    ====================================== -->
    <aside class="admin-sidebar" id="adminSidebar">
        <div class="admin-sidebar__logo">
            <div class="admin-sidebar__logo-text">ANDREYLPZ</div>
            <div class="admin-sidebar__logo-sub">Panel de Administraci&#243;n</div>
        </div>

        <nav class="admin-nav">
            <!-- Dashboard -->
            <div class="admin-nav__item" data-section="dashboard" data-label="Dashboard">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <rect x="3" y="3" width="7" height="7" rx="1" />
                    <rect x="14" y="3" width="7" height="7" rx="1" />
                    <rect x="3" y="14" width="7" height="7" rx="1" />
                    <rect x="14" y="14" width="7" height="7" rx="1" />
                </svg>
                Dashboard
            </div>

            <!-- Productos -->
            <% if (perm.test("VER_PRODUCTOS")) { %>
            <div class="admin-nav__item" data-section="productos" data-label="Productos">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
                    <line x1="3" y1="6" x2="21" y2="6" />
                    <path d="M16 10a4 4 0 0 1-8 0" />
                </svg>
                Productos
            </div>
            <% } %>

            <!-- Pedidos -->
            <% if (perm.test("VER_PEDIDOS")) { %>
            <div class="admin-nav__item" data-section="pedidos" data-label="Pedidos">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <path d="M9 17H5a2 2 0 0 0-2 2" />
                    <path d="M15 17h4a2 2 0 0 1 2 2" />
                    <path d="M12 3v14" />
                    <path d="M12 3 8 7m4-4 4 4" />
                    <rect x="5" y="19" width="14" height="3" rx="1" />
                </svg>
                Pedidos
            </div>
            <% } %>

            <!-- Usuarios -->
            <% if (perm.test("VER_USUARIOS")) { %>
            <div class="admin-nav__item" data-section="usuarios" data-label="Usuarios">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                    <circle cx="9" cy="7" r="4" />
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </svg>
                Usuarios
            </div>
            <% } %>

            <!-- Categorías -->
            <% if (perm.test("GESTIONAR_CATEGORIAS")) { %>
            <div class="admin-nav__item" data-section="categorias" data-label="Categor&#237;as">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <path d="M3 3h7v7H3z" /><path d="M14 3h7v7h-7z" /><path d="M3 14h7v7H3z" />
                    <path d="M17.5 17.5m-3 0a3 3 0 1 0 6 0a3 3 0 1 0 -6 0" />
                </svg>
                Categor&#237;as
            </div>
            <% } %>

            <!-- Marcas -->
            <% if (perm.test("GESTIONAR_MARCAS")) { %>
            <div class="admin-nav__item" data-section="marcas" data-label="Marcas">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01z" />
                </svg>
                Marcas
            </div>
            <% } %>

            <!-- Roles y Permisos -->
            <% if (perm.test("GESTIONAR_ROLES")) { %>
            <div class="admin-nav__item" data-section="permisos" data-label="Roles y Permisos">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
                Roles y Permisos
            </div>
            <% } %>

            <div class="admin-nav__separator"></div>

            <!-- Configuraci&#243;n -->
            <div class="admin-nav__item" data-section="configuracion" data-label="Configuraci&#243;n">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <circle cx="12" cy="12" r="3" />
                    <path
                        d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
                </svg>
                Configuraci&#243;n
            </div>

            <!-- Cerrar sesion -->
            <div class="admin-nav__item" data-section="logout" data-label="Cerrar Sesi&#243;n"
                onclick="window.location='<%=request.getContextPath()%>/SvLogout'">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                    <polyline points="16 17 21 12 16 7" />
                    <line x1="21" y1="12" x2="9" y2="12" />
                </svg>
                Cerrar Sesi&#243;n
            </div>
        </nav>

        <div class="admin-sidebar__footer">
            <div class="admin-sidebar__user">
                <div class="admin-sidebar__avatar">A</div>
                <div>
                    <div class="admin-sidebar__username"><%=nombreUsuario%></div>
                    <div class="admin-sidebar__role"><%=rolUsuario%></div>
                </div>
            </div>
        </div>
    </aside>

    <!-- ======================================
         MAIN AREA
    ====================================== -->
    <div class="admin-main">

        <!-- Topbar -->
        <header class="admin-topbar">
            <div style="display:flex;align-items:center;gap:16px">
                <button class="admin-hamburger" id="adminHamburger" aria-label="Men&#250;">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="3" y1="6" x2="21" y2="6" />
                        <line x1="3" y1="12" x2="21" y2="12" />
                        <line x1="3" y1="18" x2="21" y2="18" />
                    </svg>
                </button>
                <h1 class="admin-topbar__title" id="topbar-title">Dashboard</h1>
            </div>
            <div class="admin-topbar__actions">
                <a href="../index.jsp" class="btn btn-secondary btn-sm">&larr; Ver Tienda</a>
            </div>
        </header>

        <div class="admin-content">

            <!-- SECTION: DASHBOARD -->
            <section class="admin-section" id="section-dashboard">

                <!-- Stats -->
                <div class="admin-stats-grid">
                    <div class="stat-card">
                        <div class="stat-card__icon stat-card__icon--gold">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="1.8">
                                <line x1="12" y1="1" x2="12" y2="23" />
                                <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
                            </svg>
                        </div>
                        <div>
                            <div class="stat-card__value" id="stat-revenue">–</div>
                            <div class="stat-card__label">Ventas Totales</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-card__icon stat-card__icon--green">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="1.8">
                                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
                                <line x1="3" y1="6" x2="21" y2="6" />
                            </svg>
                        </div>
                        <div>
                            <div class="stat-card__value" id="stat-products">–</div>
                            <div class="stat-card__label">Productos</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-card__icon stat-card__icon--purple">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="1.8">
                                <path d="M9 17H5a2 2 0 0 0-2 2" />
                                <path d="M15 17h4a2 2 0 0 1 2 2" />
                                <path d="M12 3v14" />
                                <path d="M12 3 8 7m4-4 4 4" />
                                <rect x="5" y="19" width="14" height="3" rx="1" />
                            </svg>
                        </div>
                        <div>
                            <div class="stat-card__value" id="stat-orders">–</div>
                            <div class="stat-card__label">Pedidos</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-card__icon stat-card__icon--red">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="1.8">
                                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                                <circle cx="9" cy="7" r="4" />
                                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                            </svg>
                        </div>
                        <div>
                            <div class="stat-card__value" id="stat-users">–</div>
                            <div class="stat-card__label">Usuarios</div>
                        </div>
                    </div>
                </div>

                <!-- Bottom Grid -->
                <div class="recent-orders-grid">
                    <div class="admin-card">
                        <div class="admin-card__title">Pedidos Recientes</div>
                        <div class="admin-table-wrapper" style="border:none;border-radius:0">
                            <table class="admin-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Cliente</th>
                                        <th>Fecha</th>
                                        <th>Total</th>
                                        <th>Estado</th>
                                    </tr>
                                </thead>
                                <tbody id="recent-orders-tbody"></tbody>
                            </table>
                        </div>
                    </div>
                    <div class="admin-card">
                        <div class="admin-card__title">&#9888; Stock Bajo</div>
                        <div id="low-stock-list"></div>
                    </div>
                </div>
            </section>

            <!-- ==============================
                 SECTION: PRODUCTOS ============================== -->
            <section class="admin-section" id="section-productos">
                <div class="admin-section__header">
                    <h2>Gesti&#243;n de Productos</h2>
                    <button class="btn btn-primary" id="btn-add-product">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                            stroke-width="2.5">
                            <line x1="12" y1="5" x2="12" y2="19" />
                            <line x1="5" y1="12" x2="19" y2="12" />
                        </svg>
                        Agregar Producto
                    </button>
                </div>
                <div class="admin-table-wrapper">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>Img</th>
                                <th>Nombre</th>
                                <th>Marca</th>
                                <th>Categor&#237;a</th>
                                <th>Precio</th>
                                <th>Stock</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody id="products-tbody"></tbody>
                    </table>
                </div>
            </section>

            <!-- ==============================
                 SECTION: PEDIDOS ============================== -->
            <section class="admin-section" id="section-pedidos">
                <div class="admin-section__header">
                    <h2>Gesti&#243;n de Pedidos</h2>
                </div>
                <div class="admin-table-wrapper">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Cliente</th>
                                <th>Total</th>
                                <th>Fecha</th>
                                <th>Estado actual</th>
                                <th>Cambiar estado</th>
                                <th>Pago</th>
                                <th>Env&#237;o</th>
                                <th>Detalle</th>
                            </tr>
                        </thead>
                        <tbody id="orders-tbody"></tbody>
                    </table>
                </div>
            </section>

            <!-- ==============================
                 SECTION: USUARIOS ============================== -->
            <section class="admin-section" id="section-usuarios">
                <div class="admin-section__header">
                    <h2>Usuarios Registrados</h2>
                </div>
                <div class="admin-table-wrapper">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nombre</th>
                                <th>Correo</th>
                                <th>Tel&#233;fonos</th>
                                <th>Direcci&#243;n</th>
                                <th>Pedidos</th>
                                <th>Registro</th>
                                <th>Estado</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody id="users-tbody"></tbody>
                    </table>
                </div>
            </section>

            <!-- SECTION: CATEGORIAS -->
            <section class="admin-section" id="section-categorias">
                <div class="admin-section__header">
                    <h2>Gesti&#243;n de Categor&#237;as</h2>
                    <button class="btn btn-primary" id="btn-add-categoria">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                            <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
                        </svg>
                        Nueva Categor&#237;a
                    </button>
                </div>
                <div class="admin-table-wrapper">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nombre</th>
                                <th>Descripci&#243;n</th>
                                <th>Productos</th>
                                <th>Estado</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody id="categorias-tbody"></tbody>
                    </table>
                </div>
            </section>

            <!-- SECTION: MARCAS -->
            <section class="admin-section" id="section-marcas">
                <div class="admin-section__header">
                    <h2>Gesti&#243;n de Marcas</h2>
                    <button class="btn btn-primary" id="btn-add-marca">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                            <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
                        </svg>
                        Nueva Marca
                    </button>
                </div>
                <div class="admin-table-wrapper">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nombre</th>
                                <th>Descripci&#243;n</th>
                                <th>Productos</th>
                                <th>Estado</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody id="marcas-tbody"></tbody>
                    </table>
                </div>
            </section>

            <!-- SECTION: ROLES Y PERMISOS -->
            <section class="admin-section" id="section-permisos">
                <div class="admin-section__header">
                    <h2>Roles y Permisos</h2>
                </div>

                <!-- Sub-tabs -->
                <div style="display:flex;gap:8px;margin-bottom:20px">
                    <button class="btn btn-primary" id="permisos-tab-roles" onclick="adminApp.permisosTab('roles')">Roles</button>
                    <button class="btn btn-secondary" id="permisos-tab-permisos" onclick="adminApp.permisosTab('permisosList')">Permisos</button>
                </div>

                <!-- Panel: Roles -->
                <div id="permisos-panel-roles">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
                        <h3 style="font-size:15px;font-weight:600;color:var(--admin-text)">Roles del Sistema</h3>
                        <button class="btn btn-primary btn-sm" id="btn-add-rol">+ Nuevo Rol</button>
                    </div>
                    <div class="admin-table-wrapper">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>Descripci&#243;n</th>
                                    <th>Estado</th>
                                    <th>Permisos asignados</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody id="roles-tbody"></tbody>
                        </table>
                    </div>
                </div>

                <!-- Panel: Permisos -->
                <div id="permisos-panel-permisosList" style="display:none">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
                        <h3 style="font-size:15px;font-weight:600;color:var(--admin-text)">Cat&#225;logo de Permisos</h3>
                        <button class="btn btn-primary btn-sm" id="btn-add-permiso">+ Nuevo Permiso</button>
                    </div>
                    <div class="admin-table-wrapper">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>M&#243;dulo</th>
                                    <th>Descripci&#243;n</th>
                                    <th>Estado</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody id="permisos-tbody"></tbody>
                        </table>
                    </div>
                </div>
            </section>

            <!-- ==============================
                 SECTION: CONFIGURACIÓN ============================== -->
            <section class="admin-section" id="section-configuracion">
                <div class="admin-section__header">
                    <h2>Configuraci&#243;n</h2>
                </div>
                <div class="admin-card" style="max-width:600px">
                    <div class="admin-card__title">Informaci&#243;n de la Tienda</div>
                    <div class="form-group">
                        <label class="form-label">Nombre de la Tienda</label>
                        <input class="form-input" type="text" value="ANDREYLPZ" readonly>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Correo de Contacto</label>
                        <input class="form-input" type="email" placeholder="admin@andreylpz.com">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Moneda</label>
                        <select class="form-select">
                            <option selected>COP - Peso Colombiano</option>
                            <option>USD - D&#243;lar Americano</option>
                        </select>
                    </div>
                    <button class="btn btn-primary" style="margin-top:8px">Guardar Cambios</button>
                </div>
            </section>

        </div>
    </div>

            <!-- ==============================
         MODAL: AGREGAR / EDITAR PRODUCTO ============================== -->
    <div class="admin-modal-overlay" id="modal-product">
        <div class="admin-modal">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title" id="modal-product-title">Agregar Producto</h2>
                <button class="admin-modal__close" id="close-product-modal">&#10005;</button>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">Nombre *</label>
                    <input class="form-input" id="prod-name" type="text" placeholder="Ej: J'adore">
                </div>
                <div class="form-group">
                    <label class="form-label">Marca *</label>
                    <select class="form-select" id="prod-brand"><option value="">Cargando...</option></select>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">Categor&#237;a</label>
                    <select class="form-select" id="prod-category"><option value="">Cargando...</option></select>
                </div>
                <div class="form-group">
                    <label class="form-label">Precio (COP) *</label>
                    <input class="form-input" id="prod-price" type="number" placeholder="450000">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">Stock *</label>
                    <input class="form-input" id="prod-stock" type="number" placeholder="10">
                </div>
                <div class="form-group">
                    <label class="form-label">URL Imagen</label>
                    <input class="form-input" id="prod-image" type="text" placeholder="../assets/imagenes/...">
                </div>
            </div>
            <div style="display:flex;gap:12px;justify-content:flex-end;margin-top:8px">
                <button class="btn btn-secondary" id="close-product-modal2"
                    onclick="document.getElementById('modal-product').classList.remove('open')">Cancelar</button>
                <button class="btn btn-primary" id="save-product-btn">Guardar</button>
            </div>
        </div>
    </div>

            <!-- ==============================
         MODAL: DETALLE DE PEDIDO ============================== -->
    <div class="admin-modal-overlay" id="modal-order-detail">
        <div class="admin-modal" style="max-width:580px;width:95%">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title" id="modal-order-title">Detalle del Pedido</h2>
                <button class="admin-modal__close" onclick="adminApp.closeOrderDetail()">&#10005;</button>
            </div>
            <div id="modal-order-info" style="margin-bottom:8px;display:flex;gap:24px;flex-wrap:wrap;font-size:14px;color:var(--admin-text)"></div>
            <div id="modal-order-contacto" style="margin-bottom:16px;display:flex;gap:16px;flex-wrap:wrap;font-size:13px;color:var(--admin-text);background:var(--admin-card);border:1px solid var(--admin-border);border-radius:6px;padding:10px 14px"></div>
            <div class="admin-table-wrapper" style="max-height:320px;overflow-y:auto">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>Producto</th>
                            <th style="text-align:center">Cant.</th>
                            <th style="text-align:right">Precio unit.</th>
                            <th style="text-align:right">Subtotal</th>
                        </tr>
                    </thead>
                    <tbody id="modal-order-items"></tbody>
                </table>
            </div>
            <div id="modal-order-total" style="text-align:right;font-weight:700;font-size:15px;margin-top:14px;padding-top:12px;border-top:1px solid var(--admin-border)"></div>
            <div id="modal-order-envio" style="display:none;margin-top:14px;padding:12px 14px;background:var(--admin-card);border:1px solid var(--admin-border);border-radius:6px"></div>
            <div style="display:flex;justify-content:flex-end;margin-top:16px">
                <button class="btn btn-secondary" onclick="adminApp.closeOrderDetail()">Cerrar</button>
            </div>
        </div>
    </div>

    <!-- MODAL: DETALLE DE USUARIO -->
    <div class="admin-modal-overlay" id="modal-user-detail">
        <div class="admin-modal" style="max-width:520px;width:95%">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title" id="modal-user-title">Detalle del Usuario</h2>
                <button class="admin-modal__close" onclick="adminApp.closeUserDetail()">&#10005;</button>
            </div>
            <div id="modal-user-body" style="display:flex;flex-direction:column;gap:10px;font-size:14px"></div>
            <div style="display:flex;justify-content:flex-end;margin-top:16px">
                <button class="btn btn-secondary" onclick="adminApp.closeUserDetail()">Cerrar</button>
            </div>
        </div>
    </div>

    <!-- MODAL: CATEGORIA -->
    <div class="admin-modal-overlay" id="modal-categoria">
        <div class="admin-modal" style="max-width:440px">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title" id="modal-categoria-title">Nueva Categor&#237;a</h2>
                <button class="admin-modal__close" onclick="document.getElementById('modal-categoria').classList.remove('open')">&#10005;</button>
            </div>
            <input type="hidden" id="cat-id">
            <div class="form-group">
                <label class="form-label">Nombre *</label>
                <input class="form-input" id="cat-nombre" type="text" placeholder="Ej: Mujer">
            </div>
            <div class="form-group">
                <label class="form-label">Descripci&#243;n</label>
                <input class="form-input" id="cat-descripcion" type="text" placeholder="Descripci&#243;n opcional">
            </div>
            <div style="display:flex;gap:12px;justify-content:flex-end;margin-top:12px">
                <button class="btn btn-secondary" onclick="document.getElementById('modal-categoria').classList.remove('open')">Cancelar</button>
                <button class="btn btn-primary" id="save-categoria-btn">Guardar</button>
            </div>
        </div>
    </div>

    <!-- MODAL: MARCA -->
    <div class="admin-modal-overlay" id="modal-marca">
        <div class="admin-modal" style="max-width:440px">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title" id="modal-marca-title">Nueva Marca</h2>
                <button class="admin-modal__close" onclick="document.getElementById('modal-marca').classList.remove('open')">&#10005;</button>
            </div>
            <input type="hidden" id="marca-id">
            <div class="form-group">
                <label class="form-label">Nombre *</label>
                <input class="form-input" id="marca-nombre" type="text" placeholder="Ej: Xerjoff">
            </div>
            <div class="form-group">
                <label class="form-label">Descripci&#243;n</label>
                <input class="form-input" id="marca-descripcion" type="text" placeholder="Descripci&#243;n opcional">
            </div>
            <div class="form-group" id="marca-genero-group">
                <label class="form-label">G&#233;nero *</label>
                <select class="form-input" id="marca-genero">
                    <option value="HOMBRE">Hombre</option>
                    <option value="MUJER">Mujer</option>
                </select>
            </div>
            <div style="display:flex;gap:12px;justify-content:flex-end;margin-top:12px">
                <button class="btn btn-secondary" onclick="document.getElementById('modal-marca').classList.remove('open')">Cancelar</button>
                <button class="btn btn-primary" id="save-marca-btn">Guardar</button>
            </div>
        </div>
    </div>

    <!-- MODAL: REGISTRAR PAGO -->
    <div class="admin-modal-overlay" id="modal-pago">
        <div class="admin-modal" style="max-width:500px">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title">Registrar Pago</h2>
                <button class="admin-modal__close" onclick="adminApp.closePagoModal()">&#10005;</button>
            </div>
            <input type="hidden" id="pago-id-pedido">
            <div id="pago-resumen" style="background:rgba(201,168,76,.08);border:1px solid var(--admin-border);border-radius:8px;padding:12px;margin-bottom:16px;font-size:13px;display:flex;gap:24px;flex-wrap:wrap"></div>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">M&#233;todo de Pago *</label>
                    <select class="form-select" id="pago-metodo">
                        <option value="EFECTIVO">Efectivo</option>
                        <option value="TARJETA_CREDITO">Tarjeta de Cr&#233;dito</option>
                        <option value="TARJETA_DEBITO">Tarjeta de D&#233;bito</option>
                        <option value="TRANSFERENCIA">Transferencia</option>
                        <option value="PSE">PSE</option>
                        <option value="NEQUI">Nequi</option>
                        <option value="DAVIPLATA">Daviplata</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label">Monto (COP) *</label>
                    <input class="form-input" id="pago-monto" type="number" min="1" placeholder="0">
                </div>
            </div>
            <div class="form-group">
                <label class="form-label">Referencia / N&#250;mero de transacci&#243;n</label>
                <input class="form-input" id="pago-referencia" type="text" placeholder="Opcional">
            </div>
            <div id="pago-lista" style="margin-top:12px"></div>
            <div style="display:flex;gap:12px;justify-content:flex-end;margin-top:12px">
                <button class="btn btn-secondary" onclick="adminApp.closePagoModal()">Cancelar</button>
                <button class="btn btn-primary" id="save-pago-btn">Registrar Pago</button>
            </div>
        </div>
    </div>

    <!-- MODAL: REGISTRAR ENV&#205;O -->
    <div class="admin-modal-overlay" id="modal-envio">
        <div class="admin-modal" style="max-width:520px">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title" id="modal-envio-title">Registrar Env&#237;o</h2>
                <button class="admin-modal__close" onclick="adminApp.closeEnvioModal()">&#10005;</button>
            </div>
            <input type="hidden" id="envio-id-pedido">
            <input type="hidden" id="envio-id-envio">
            <div class="form-group">
                <label class="form-label">Direcci&#243;n de Env&#237;o *</label>
                <input class="form-input" id="envio-direccion" type="text" placeholder="Calle, ciudad, departamento">
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">Transportadora</label>
                    <input class="form-input" id="envio-transportadora" type="text" placeholder="Ej: Servientrega">
                </div>
                <div class="form-group">
                    <label class="form-label">N&#250;mero de Gu&#237;a</label>
                    <input class="form-input" id="envio-guia" type="text" placeholder="Opcional">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">Fecha Estimada Entrega</label>
                    <input class="form-input" id="envio-fecha-est" type="date">
                </div>
                <div class="form-group" id="envio-estado-group" style="display:none">
                    <label class="form-label">Estado de Entrega</label>
                    <select class="form-select" id="envio-estado">
                        <option value="PREPARANDO">Preparando</option>
                        <option value="EN_TRANSITO">En tr&#225;nsito</option>
                        <option value="ENTREGADO">Entregado</option>
                        <option value="DEVUELTO">Devuelto</option>
                    </select>
                </div>
            </div>
            <div style="display:flex;gap:12px;justify-content:flex-end;margin-top:12px">
                <button class="btn btn-secondary" onclick="adminApp.closeEnvioModal()">Cancelar</button>
                <button class="btn btn-primary" id="save-envio-btn">Guardar Env&#237;o</button>
            </div>
        </div>
    </div>

    <!-- MODAL: ROL -->
    <div class="admin-modal-overlay" id="modal-rol">
        <div class="admin-modal" style="max-width:440px">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title" id="modal-rol-title">Nuevo Rol</h2>
                <button class="admin-modal__close" onclick="document.getElementById('modal-rol').classList.remove('open')">&#10005;</button>
            </div>
            <input type="hidden" id="rol-id">
            <div class="form-group">
                <label class="form-label">Nombre *</label>
                <input class="form-input" id="rol-nombre" type="text" placeholder="Ej: Vendedor">
            </div>
            <div class="form-group">
                <label class="form-label">Descripci&#243;n</label>
                <input class="form-input" id="rol-descripcion" type="text" placeholder="Descripci&#243;n del rol">
            </div>
            <div style="display:flex;gap:12px;justify-content:flex-end;margin-top:12px">
                <button class="btn btn-secondary" onclick="document.getElementById('modal-rol').classList.remove('open')">Cancelar</button>
                <button class="btn btn-primary" id="save-rol-btn">Guardar</button>
            </div>
        </div>
    </div>

    <!-- MODAL: PERMISO -->
    <div class="admin-modal-overlay" id="modal-permiso">
        <div class="admin-modal" style="max-width:480px">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title" id="modal-permiso-title">Nuevo Permiso</h2>
                <button class="admin-modal__close" onclick="document.getElementById('modal-permiso').classList.remove('open')">&#10005;</button>
            </div>
            <input type="hidden" id="permiso-id">
            <div class="form-group">
                <label class="form-label">Nombre *</label>
                <input class="form-input" id="permiso-nombre" type="text" placeholder="Ej: VER_PEDIDOS">
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">M&#243;dulo</label>
                    <input class="form-input" id="permiso-modulo" type="text" placeholder="Ej: PEDIDOS">
                </div>
                <div class="form-group">
                    <label class="form-label">Descripci&#243;n</label>
                    <input class="form-input" id="permiso-descripcion" type="text" placeholder="Descripci&#243;n">
                </div>
            </div>
            <div style="display:flex;gap:12px;justify-content:flex-end;margin-top:12px">
                <button class="btn btn-secondary" onclick="document.getElementById('modal-permiso').classList.remove('open')">Cancelar</button>
                <button class="btn btn-primary" id="save-permiso-btn">Guardar</button>
            </div>
        </div>
    </div>

    <!-- MODAL: ASIGNAR PERMISOS A ROL -->
    <div class="admin-modal-overlay" id="modal-asignar-permisos">
        <div class="admin-modal" style="max-width:560px">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title" id="modal-asignar-title">Gestionar Permisos del Rol</h2>
                <button class="admin-modal__close" onclick="document.getElementById('modal-asignar-permisos').classList.remove('open')">&#10005;</button>
            </div>
            <input type="hidden" id="asignar-id-rol">
            <div id="asignar-permisos-actuales" style="margin-bottom:16px"></div>
            <div style="border-top:1px solid var(--admin-border);padding-top:14px">
                <p style="font-size:13px;font-weight:600;text-transform:uppercase;color:var(--admin-muted);margin-bottom:10px">Agregar permiso</p>
                <div style="display:flex;gap:8px">
                    <select class="form-select" id="asignar-permiso-sel" style="flex:1"></select>
                    <button class="btn btn-primary" id="btn-asignar-permiso">Asignar</button>
                </div>
            </div>
            <div style="display:flex;justify-content:flex-end;margin-top:16px">
                <button class="btn btn-secondary" onclick="document.getElementById('modal-asignar-permisos').classList.remove('open')">Cerrar</button>
            </div>
        </div>
    </div>

    <script src="../assets/scripts/admin.js"></script>
</body>

</html>
