/* admin.js - Admin Panel Logic for ANDREYLPZ — conectado al backend */
(function () {
    'use strict';

    // ─── Base URL de servlets (relativa al contexto de la app) ───────────────
    const BASE = (function () {
        const p = window.location.pathname.split('/');
        return '/' + p[1];
    })();

    function apiUrl(servlet) { return BASE + '/' + servlet; }

    // ─── Helpers HTTP ────────────────────────────────────────────────────────
    function get(servlet) {
        return fetch(apiUrl(servlet), { credentials: 'same-origin' })
            .then(r => r.json());
    }

    function post(servlet, params) {
        const body = new URLSearchParams(params);
        return fetch(apiUrl(servlet), {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body.toString()
        }).then(r => r.json());
    }

    // ─── Caché en memoria ────────────────────────────────────────────────────
    let _products = [];
    let _orders   = [];
    let _users    = [];

    // ─── Section Navigation ──────────────────────────────────────────────────
    function navigate(sectionId) {
        document.querySelectorAll('.admin-section').forEach(s => s.classList.remove('active'));
        document.querySelectorAll('.admin-nav__item').forEach(i => i.classList.remove('active'));

        const section = document.getElementById('section-' + sectionId);
        const navItem = document.querySelector('[data-section="' + sectionId + '"]');
        if (section) section.classList.add('active');
        if (navItem) navItem.classList.add('active');

        const titleEl = document.getElementById('topbar-title');
        if (titleEl) titleEl.textContent = navItem ? navItem.dataset.label : sectionId;

        if (sectionId === 'dashboard') loadDashboard();
        if (sectionId === 'productos') loadProducts();
        if (sectionId === 'pedidos')   loadOrders();
        if (sectionId === 'usuarios')  loadUsers();

        document.getElementById('adminSidebar')?.classList.remove('open');
    }

    // ─── Format helpers ──────────────────────────────────────────────────────
    function fmt(n) {
        const num = parseFloat(n) || 0;
        return num.toLocaleString('es-CO') + ' COP';
    }

    function statusBadge(status) {
        const map = {
            'ENTREGADO': 'badge-success', 'Entregado': 'badge-success',
            'ENVIADO':   'badge-info',    'Enviado':   'badge-info',
            'PAGO':      'badge-warning', 'Procesando':'badge-warning',
            'PENDIENTE': 'badge-danger',  'Pendiente': 'badge-danger',
            'CANCELADO': 'badge-danger',  'Cancelado': 'badge-danger',
            'true':      'badge-success', 'Activo':    'badge-success',
            'false':     'badge-danger',  'Inactivo':  'badge-danger',
        };
        const label = String(status);
        return '<span class="badge ' + (map[label] || 'badge-info') + '">' + label + '</span>';
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────
    function loadDashboard() {
        get('SvDashboard').then(data => {
            if (data.error) { console.error('Dashboard error:', data.error); return; }

            document.getElementById('stat-revenue').textContent  = fmt(data.ventas);
            document.getElementById('stat-products').textContent = data.productos;
            document.getElementById('stat-orders').textContent   = data.pedidos;
            document.getElementById('stat-users').textContent    = data.usuarios;

            const recentTbody = document.getElementById('recent-orders-tbody');
            if (recentTbody && Array.isArray(data.pedidosRecientes)) {
                recentTbody.innerHTML = data.pedidosRecientes.map(o =>
                    '<tr>' +
                    '<td>#' + o.id + '</td>' +
                    '<td>' + (o.cliente || '—') + '</td>' +
                    '<td>—</td>' +
                    '<td>' + fmt(o.total) + '</td>' +
                    '<td>' + statusBadge(o.estado) + '</td>' +
                    '</tr>'
                ).join('');
            }
        }).catch(e => console.error('Dashboard fetch error:', e));

        // Stock bajo: cargamos productos para detectarlos
        get('SvProductos').then(products => {
            if (!Array.isArray(products)) return;
            _products = products;
            const lowStock = products.filter(p => p.stock <= 5 && p.activo);
            const lowEl = document.getElementById('low-stock-list');
            if (!lowEl) return;
            lowEl.innerHTML = lowStock.length === 0
                ? '<p style="color:var(--admin-muted);font-size:0.85rem;">Sin alertas de stock</p>'
                : lowStock.map(p =>
                    '<div style="display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid var(--admin-border)">' +
                    '<span>' + p.nombre + ' – ' + p.marca + '</span>' +
                    '<span class="badge ' + (p.stock === 0 ? 'badge-danger' : 'badge-warning') + '">' + p.stock + ' uds</span>' +
                    '</div>'
                ).join('');
        }).catch(e => console.error('Products (stock) fetch error:', e));
    }

    // ─── Products ────────────────────────────────────────────────────────────
    function loadProducts() {
        get('SvProductos').then(products => {
            if (!Array.isArray(products)) { console.error('Productos error:', products); return; }
            _products = products;
            renderProducts(products);
        }).catch(e => console.error('Products fetch error:', e));
    }

    function renderProducts(products) {
        const tbody = document.getElementById('products-tbody');
        if (!tbody) return;
        if (products.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--admin-muted)">Sin productos registrados</td></tr>';
            return;
        }
        tbody.innerHTML = products.map(p =>
            '<tr>' +
            '<td><img class="product-img-sm" src="" alt="" style="display:none"></td>' +
            '<td><strong>' + p.nombre + '</strong></td>' +
            '<td>' + (p.marca || '—') + '</td>' +
            '<td>' + (p.categoria || '—') + '</td>' +
            '<td>' + fmt(p.precio) + '</td>' +
            '<td><span class="badge ' + (p.stock <= 5 ? 'badge-warning' : 'badge-success') + '">' + p.stock + '</span></td>' +
            '<td style="display:flex;gap:8px;padding-top:18px">' +
            '<button class="btn btn-secondary btn-sm" onclick="adminApp.editProduct(' + p.id + ')">Editar</button>' +
            '<button class="btn btn-danger btn-sm" onclick="adminApp.deleteProduct(' + p.id + ')">Eliminar</button>' +
            '</td>' +
            '</tr>'
        ).join('');
    }

    // ─── Product Modal ───────────────────────────────────────────────────────
    let editingProductId = null;

    function openProductModal(product) {
        editingProductId = product ? product.id : null;
        document.getElementById('modal-product-title').textContent = product ? 'Editar Producto' : 'Agregar Producto';
        document.getElementById('prod-name').value     = product ? product.nombre    : '';
        document.getElementById('prod-brand').value    = product ? product.idMarca   : '';
        document.getElementById('prod-category').value = product ? product.idCategoria : '';
        document.getElementById('prod-price').value    = product ? product.precio    : '';
        document.getElementById('prod-stock').value    = product ? product.stock     : '';
        document.getElementById('prod-image').value    = '';
        document.getElementById('modal-product').classList.add('open');
    }

    function closeProductModal() {
        document.getElementById('modal-product').classList.remove('open');
        editingProductId = null;
    }

    function saveProduct() {
        const nombre      = document.getElementById('prod-name').value.trim();
        const idMarca     = document.getElementById('prod-brand').value.trim();
        const idCategoria = document.getElementById('prod-category').value.trim();
        const precio      = document.getElementById('prod-price').value.trim();
        const stock       = document.getElementById('prod-stock').value.trim();

        if (!nombre || !precio || !stock) {
            alert('Por favor completa los campos requeridos: Nombre, Precio y Stock.');
            return;
        }

        const params = {
            nombre: nombre,
            descripcion: '',
            precio: precio,
            stock: stock,
            idCategoria: idCategoria || '1',
            idMarca: idMarca || '1',
            accion: editingProductId ? 'editar' : 'crear'
        };
        if (editingProductId) params.id = editingProductId;

        post('SvProductos', params).then(r => {
            if (r.error) { alert('Error: ' + r.error); return; }
            closeProductModal();
            loadProducts();
            loadDashboard();
        }).catch(e => { alert('Error de conexión: ' + e.message); });
    }

    function deleteProduct(id) {
        if (!confirm('¿Eliminar este producto?')) return;
        post('SvProductos', { accion: 'eliminar', id: id }).then(r => {
            if (r.error) { alert('Error: ' + r.error); return; }
            loadProducts();
            loadDashboard();
        }).catch(e => alert('Error de conexión: ' + e.message));
    }

    function editProduct(id) {
        const product = _products.find(p => p.id === id);
        if (product) openProductModal(product);
    }

    // ─── Orders ──────────────────────────────────────────────────────────────
    function loadOrders() {
        get('SvPedidos').then(orders => {
            if (!Array.isArray(orders)) { console.error('Pedidos error:', orders); return; }
            _orders = orders;
            renderOrders(orders);
        }).catch(e => console.error('Orders fetch error:', e));
    }

    function renderOrders(orders) {
        const tbody = document.getElementById('orders-tbody');
        if (!tbody) return;
        if (orders.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--admin-muted)">Sin pedidos registrados</td></tr>';
            return;
        }
        tbody.innerHTML = orders.map(o =>
            '<tr>' +
            '<td>#' + o.id + '</td>' +
            '<td>' + (o.cliente || '—') + '</td>' +
            '<td>—</td>' +
            '<td>' + fmt(o.total) + '</td>' +
            '<td>' + (o.fecha ? o.fecha.substring(0, 10) : '—') + '</td>' +
            '<td>' + statusBadge(o.estado) + '</td>' +
            '</tr>'
        ).join('');
    }

    // ─── Users ───────────────────────────────────────────────────────────────
    function loadUsers() {
        get('SvUsuarios').then(users => {
            if (!Array.isArray(users)) { console.error('Usuarios error:', users); return; }
            _users = users;
            renderUsers(users);
        }).catch(e => console.error('Users fetch error:', e));
    }

    function renderUsers(users) {
        const tbody = document.getElementById('users-tbody');
        if (!tbody) return;
        if (users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--admin-muted)">Sin usuarios registrados</td></tr>';
            return;
        }
        tbody.innerHTML = users.map(u =>
            '<tr>' +
            '<td>#' + u.id + '</td>' +
            '<td><strong>' + (u.nombre || 'Admin') + '</strong></td>' +
            '<td>' + u.correo + '</td>' +
            '<td>—</td>' +
            '<td>' + (u.registro ? u.registro.substring(0, 10) : '—') + '</td>' +
            '<td>' + statusBadge(u.activo ? 'Activo' : 'Inactivo') + '</td>' +
            '</tr>'
        ).join('');
    }

    // ─── Event binding ───────────────────────────────────────────────────────
    function init() {
        document.querySelectorAll('.admin-nav__item').forEach(item => {
            item.addEventListener('click', () => navigate(item.dataset.section));
        });

        document.getElementById('adminHamburger')?.addEventListener('click', () => {
            document.getElementById('adminSidebar')?.classList.toggle('open');
        });

        document.getElementById('btn-add-product')?.addEventListener('click', () => openProductModal(null));
        document.getElementById('close-product-modal')?.addEventListener('click', closeProductModal);
        document.getElementById('modal-product')?.addEventListener('click', e => {
            if (e.target === document.getElementById('modal-product')) closeProductModal();
        });
        document.getElementById('save-product-btn')?.addEventListener('click', saveProduct);

        navigate('dashboard');
    }

    // ─── Public API ──────────────────────────────────────────────────────────
    window.adminApp = { editProduct, deleteProduct };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
