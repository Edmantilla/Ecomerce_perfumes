<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Admin – ANDREYLPZ</title>
    <link rel="stylesheet" href="../assets/estilos/admin.css">
</head>

<body class="admin-body">

    <!-- â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
         SIDEBAR
    â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• -->
    <aside class="admin-sidebar" id="adminSidebar">
        <div class="admin-sidebar__logo">
            <div class="admin-sidebar__logo-text">ANDREYLPZ</div>
            <div class="admin-sidebar__logo-sub">Panel de Administración</div>
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
            <div class="admin-nav__item" data-section="productos" data-label="Productos">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
                    <line x1="3" y1="6" x2="21" y2="6" />
                    <path d="M16 10a4 4 0 0 1-8 0" />
                </svg>
                Productos
            </div>

            <!-- Pedidos -->
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

            <!-- Usuarios -->
            <div class="admin-nav__item" data-section="usuarios" data-label="Usuarios">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                    <circle cx="9" cy="7" r="4" />
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </svg>
                Usuarios
            </div>

            <div class="admin-nav__separator"></div>

            <!-- Configuración -->
            <div class="admin-nav__item" data-section="configuracion" data-label="Configuración">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <circle cx="12" cy="12" r="3" />
                    <path
                        d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
                </svg>
                Configuración
            </div>

            <!-- Cerrar sesión -->
            <div class="admin-nav__item" data-section="logout" data-label="Cerrar Sesión"
                onclick="window.location='<%=request.getContextPath()%>/SvLogout'">
                <svg class="admin-nav__icon" viewBox="0 0 24 24">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                    <polyline points="16 17 21 12 16 7" />
                    <line x1="21" y1="12" x2="9" y2="12" />
                </svg>
                Cerrar Sesión
            </div>
        </nav>

        <div class="admin-sidebar__footer">
            <div class="admin-sidebar__user">
                <div class="admin-sidebar__avatar">A</div>
                <div>
                    <div class="admin-sidebar__username">Admin</div>
                    <div class="admin-sidebar__role">Administrador</div>
                </div>
            </div>
        </div>
    </aside>

    <!-- â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
         MAIN AREA
    â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• -->
    <div class="admin-main">

        <!-- Topbar -->
        <header class="admin-topbar">
            <div style="display:flex;align-items:center;gap:16px">
                <button class="admin-hamburger" id="adminHamburger" aria-label="Menú">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="3" y1="6" x2="21" y2="6" />
                        <line x1="3" y1="12" x2="21" y2="12" />
                        <line x1="3" y1="18" x2="21" y2="18" />
                    </svg>
                </button>
                <h1 class="admin-topbar__title" id="topbar-title">Dashboard</h1>
            </div>
            <div class="admin-topbar__actions">
                <a href="../index.jsp" class="btn btn-secondary btn-sm">â† Ver Tienda</a>
            </div>
        </header>

        <div class="admin-content">

            <!-- â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                 SECTION: DASHBOARD
            â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ -->
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
                                        <th>Producto</th>
                                        <th>Total</th>
                                        <th>Estado</th>
                                    </tr>
                                </thead>
                                <tbody id="recent-orders-tbody"></tbody>
                            </table>
                        </div>
                    </div>
                    <div class="admin-card">
                        <div class="admin-card__title">âš  Stock Bajo</div>
                        <div id="low-stock-list"></div>
                    </div>
                </div>
            </section>

            <!-- â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                 SECTION: PRODUCTOS
            â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ -->
            <section class="admin-section" id="section-productos">
                <div class="admin-section__header">
                    <h2>Gestión de Productos</h2>
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
                                <th>Categoría</th>
                                <th>Precio</th>
                                <th>Stock</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody id="products-tbody"></tbody>
                    </table>
                </div>
            </section>

            <!-- â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                 SECTION: PEDIDOS
            â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ -->
            <section class="admin-section" id="section-pedidos">
                <div class="admin-section__header">
                    <h2>Gestión de Pedidos</h2>
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
                                <th>Detalle</th>
                            </tr>
                        </thead>
                        <tbody id="orders-tbody"></tbody>
                    </table>
                </div>
            </section>

            <!-- â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                 SECTION: USUARIOS
            â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ -->
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
                                <th>Pedidos</th>
                                <th>Registro</th>
                                <th>Estado</th>
                            </tr>
                        </thead>
                        <tbody id="users-tbody"></tbody>
                    </table>
                </div>
            </section>

            <!-- â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                 SECTION: CONFIGURACIÓN
            â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ -->
            <section class="admin-section" id="section-configuracion">
                <div class="admin-section__header">
                    <h2>Configuración</h2>
                </div>
                <div class="admin-card" style="max-width:600px">
                    <div class="admin-card__title">Información de la Tienda</div>
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
                            <option>USD - Dólar Americano</option>
                        </select>
                    </div>
                    <button class="btn btn-primary" style="margin-top:8px">Guardar Cambios</button>
                </div>
            </section>

        </div>
    </div>

    <!-- â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
         MODAL: AGREGAR / EDITAR PRODUCTO
    â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• -->
    <div class="admin-modal-overlay" id="modal-product">
        <div class="admin-modal">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title" id="modal-product-title">Agregar Producto</h2>
                <button class="admin-modal__close" id="close-product-modal">âœ•</button>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">Nombre *</label>
                    <input class="form-input" id="prod-name" type="text" placeholder="Ej: J'adore">
                </div>
                <div class="form-group">
                    <label class="form-label">Marca *</label>
                    <input class="form-input" id="prod-brand" type="text" placeholder="Ej: Dior">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">Categoría</label>
                    <input class="form-input" id="prod-category" type="text" placeholder="Ej: Mujer">
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

    <!-- ══════════════════════════════════════
         MODAL: DETALLE DE PEDIDO
    ══════════════════════════════════════ -->
    <div class="admin-modal-overlay" id="modal-order-detail" style="display:none;position:fixed;inset:0;background:rgba(0,0,0,.55);z-index:9998;align-items:center;justify-content:center">
        <div class="admin-modal" style="max-width:580px;width:95%">
            <div class="admin-modal__header">
                <h2 class="admin-modal__title" id="modal-order-title">Detalle del Pedido</h2>
                <button class="admin-modal__close" onclick="adminApp.closeOrderDetail()">✕</button>
            </div>
            <div id="modal-order-info" style="margin-bottom:16px;display:flex;gap:24px;flex-wrap:wrap;font-size:14px;color:#555"></div>
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
            <div style="display:flex;justify-content:flex-end;margin-top:16px">
                <button class="btn btn-secondary" onclick="adminApp.closeOrderDetail()">Cerrar</button>
            </div>
        </div>
    </div>

    <script src="../assets/scripts/admin.js"></script>
</body>

</html>
