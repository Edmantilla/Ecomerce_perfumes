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

        if (sectionId === 'dashboard')  loadDashboard();
        if (sectionId === 'productos')  loadProducts();
        if (sectionId === 'pedidos')    loadOrders();
        if (sectionId === 'usuarios')   loadUsers();
        if (sectionId === 'categorias') loadCategorias();
        if (sectionId === 'marcas')     loadMarcas();
        if (sectionId === 'permisos')   loadPermisos();

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
                    '<td>' + (o.fecha ? o.fecha.substring(0, 10) : '—') + '</td>' +
                    '<td>' + fmt(o.total) + '</td>' +
                    '<td>' + statusBadge(o.estado) + '</td>' +
                    '</tr>'
                ).join('');
            }
        }).catch(e => console.error('Dashboard fetch error:', e));

        // Stock bajo: cargamos productos para detectarlos
        get('SvProductos?admin=true').then(products => {
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
        get('SvProductos?admin=true').then(products => {
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

    function _fillProductSelects(product) {
        const selMarca = document.getElementById('prod-brand');
        const selCat   = document.getElementById('prod-category');
        const marcasActivas = _marcas.filter(m => m.activo);
        const catsActivas   = _categorias.filter(c => c.activo);

        selMarca.innerHTML = '<option value="">-- Selecciona una marca --</option>' +
            marcasActivas.map(m => '<option value="' + m.id + '">' + esc(m.nombre) + '</option>').join('');
        selCat.innerHTML = '<option value="">-- Selecciona una categoría --</option>' +
            catsActivas.map(c => '<option value="' + c.id + '">' + esc(c.nombre) + '</option>').join('');

        if (product) {
            const marcaMatch = marcasActivas.find(m => m.nombre === product.marca);
            if (marcaMatch) selMarca.value = marcaMatch.id;
            const catMatch = catsActivas.find(c => c.nombre === product.categoria);
            if (catMatch) selCat.value = catMatch.id;
        }
    }

    function openProductModal(product) {
        editingProductId = product ? product.id : null;
        document.getElementById('modal-product-title').textContent = product ? 'Editar Producto' : 'Agregar Producto';
        document.getElementById('prod-name').value  = product ? product.nombre : '';
        document.getElementById('prod-price').value = product ? product.precio : '';
        document.getElementById('prod-stock').value = product ? product.stock  : '';
        document.getElementById('prod-image').value = product ? (product.imagenUrl || '') : '';

        const doOpen = () => {
            _fillProductSelects(product);
            document.getElementById('modal-product').classList.add('open');
        };

        // Si los caches ya tienen datos los usamos directamente, si no los cargamos primero
        if (_marcas.length > 0 && _categorias.length > 0) {
            doOpen();
        } else {
            Promise.all([
                _marcas.length === 0     ? get('SvMarcas').then(d     => { if (Array.isArray(d)) _marcas      = d; }) : Promise.resolve(),
                _categorias.length === 0 ? get('SvCategorias').then(d => { if (Array.isArray(d)) _categorias  = d; }) : Promise.resolve()
            ]).then(doOpen).catch(e => { console.error('Error cargando listas:', e); doOpen(); });
        }
    }

    function closeProductModal() {
        document.getElementById('modal-product').classList.remove('open');
        editingProductId = null;
    }

    function saveProduct() {
        const nombre      = document.getElementById('prod-name').value.trim();
        const idMarca     = document.getElementById('prod-brand').value;
        const idCategoria = document.getElementById('prod-category').value;
        const precio      = document.getElementById('prod-price').value.trim();
        const stock       = document.getElementById('prod-stock').value.trim();

        if (!nombre || !precio || !stock) {
            alert('Por favor completa los campos requeridos: Nombre, Precio y Stock.');
            return;
        }
        if (!idMarca) {
            alert('Por favor selecciona una marca.');
            return;
        }
        if (!idCategoria) {
            alert('Por favor selecciona una categoría.');
            return;
        }

        const imagenUrl = document.getElementById('prod-image').value.trim();
        const params = {
            nombre: nombre,
            descripcion: '',
            precio: precio,
            stock: stock,
            idCategoria: idCategoria,
            idMarca: idMarca,
            imagenUrl: imagenUrl,
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
    const ESTADOS = ['PENDIENTE', 'PROCESANDO', 'ENVIADO', 'ENTREGADO', 'CANCELADO'];

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
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--admin-muted)">Sin pedidos registrados</td></tr>';
            return;
        }
        tbody.innerHTML = orders.map(o => {
            const opts = ESTADOS.map(e =>
                '<option value="' + e + '"' + (o.estado === e ? ' selected' : '') + '>' + e.charAt(0) + e.slice(1).toLowerCase() + '</option>'
            ).join('');
            return '<tr>' +
                '<td>#' + o.id + '</td>' +
                '<td>' + (o.cliente || '—') + '</td>' +
                '<td>' + fmt(o.total) + '</td>' +
                '<td>' + (o.fecha ? o.fecha.substring(0, 10) : '—') + '</td>' +
                '<td>' + statusBadge(o.estado) + '</td>' +
                '<td>' +
                  '<div style="display:flex;gap:6px;align-items:center">' +
                  '<select id="est-' + o.id + '" style="padding:5px 8px;border:1px solid var(--admin-border);border-radius:4px;font-size:13px;background:var(--admin-card);color:var(--admin-text)">' + opts + '</select>' +
                  '<button class="btn btn-secondary btn-sm" onclick="adminApp.cambiarEstado(' + o.id + ')">Guardar</button>' +
                  '</div>' +
                '</td>' +
                '<td><button class="btn btn-sm" style="background:rgba(16,185,129,.15);color:var(--admin-success)" onclick="adminApp.abrirPago(' + o.id + ')">Pago</button></td>' +
                '<td><button class="btn btn-sm" style="background:rgba(99,102,241,.15);color:var(--admin-info)" onclick="adminApp.abrirEnvio(' + o.id + ')">Env\u00edo</button></td>' +
                '<td>' +
                  '<button class="btn btn-secondary btn-sm" onclick="adminApp.verDetalle(' + o.id + ')">Detalle</button>' +
                '</td>' +
                '</tr>';
        }).join('');
    }

    function cambiarEstado(idPedido) {
        const sel = document.getElementById('est-' + idPedido);
        if (!sel) return;
        const nuevoEstado = sel.value;
        post('SvPedidos', { accion: 'cambiarEstado', idPedido: idPedido, estado: nuevoEstado })
            .then(r => {
                if (r.error) { alert('Error: ' + r.error); return; }
                // Actualizar badge en la misma fila sin recargar toda la tabla
                const badge = sel.closest('tr').querySelector('.badge');
                if (badge) {
                    const map = { 'PENDIENTE': 'badge-danger', 'PROCESANDO': 'badge-warning', 'ENVIADO': 'badge-info', 'ENTREGADO': 'badge-success', 'CANCELADO': 'badge-danger' };
                    badge.className = 'badge ' + (map[nuevoEstado] || 'badge-info');
                    badge.textContent = nuevoEstado.charAt(0) + nuevoEstado.slice(1).toLowerCase();
                }
                // Actualizar caché local
                const o = _orders.find(x => x.id === idPedido);
                if (o) o.estado = nuevoEstado;
                showToast('Pedido #' + idPedido + ' → ' + nuevoEstado.charAt(0) + nuevoEstado.slice(1).toLowerCase());
            })
            .catch(e => alert('Error de conexión: ' + e.message));
    }

    // ─── Order Detail Modal ──────────────────────────────────────────────────
    function verDetalle(idPedido) {
        const modal = document.getElementById('modal-order-detail');
        if (!modal) return;

        const o = _orders.find(x => x.id === idPedido);
        if (!o) return;

        const cliente  = o.cliente || '—';
        const estado   = o.estado  || '';
        const fecha    = o.fecha   ? o.fecha.substring(0, 10) : '—';
        const total    = o.total   || 0;
        const direccion = o.direccionCliente || '—';
        const tels = (o.telefonosCliente && o.telefonosCliente.length > 0)
            ? o.telefonosCliente.join(', ') : '—';

        document.getElementById('modal-order-title').textContent = 'Pedido #' + idPedido;
        document.getElementById('modal-order-info').innerHTML =
            '<span><strong>Cliente:</strong> ' + cliente + '</span>' +
            '<span><strong>Fecha:</strong> ' + fecha + '</span>' +
            '<span><strong>Estado:</strong> ' + statusBadge(estado) + '</span>';
        document.getElementById('modal-order-contacto').innerHTML =
            '<span><strong>&#128205; Dirección:</strong> ' + direccion + '</span>' +
            '<span><strong>&#128222; Teléfono(s):</strong> ' + tels + '</span>';
        document.getElementById('modal-order-items').innerHTML =
            '<tr><td colspan="4" style="text-align:center;color:var(--admin-muted);padding:16px">Cargando...</td></tr>';
        document.getElementById('modal-order-total').textContent = '';

        modal.classList.add('open');

        fetch(apiUrl('SvDetallesPedido') + '?idPedido=' + idPedido, { credentials: 'same-origin' })
            .then(r => {
                if (!r.ok) {
                    return r.text().then(t => { throw new Error('HTTP ' + r.status + ': ' + t.substring(0, 200)); });
                }
                return r.json();
            })
            .then(data => {
                if (data.error) {
                    document.getElementById('modal-order-items').innerHTML =
                        '<tr><td colspan="4" style="color:#c62828;text-align:center;padding:16px">' + data.error + '</td></tr>';
                    return;
                }
                if (!Array.isArray(data)) {
                    document.getElementById('modal-order-items').innerHTML =
                        '<tr><td colspan="4" style="color:#c62828;text-align:center;padding:16px">Respuesta inesperada del servidor.</td></tr>';
                    return;
                }
                const rows = data.map(d =>
                    '<tr>' +
                    '<td>' + d.producto + '</td>' +
                    '<td style="text-align:center">' + d.cantidad + '</td>' +
                    '<td style="text-align:right">' + fmt(d.precioUnitario) + '</td>' +
                    '<td style="text-align:right;font-weight:600">' + fmt(d.subtotal) + '</td>' +
                    '</tr>'
                ).join('');
                document.getElementById('modal-order-items').innerHTML = rows || '<tr><td colspan="4" style="text-align:center;color:var(--admin-muted)">Sin productos</td></tr>';
                document.getElementById('modal-order-total').textContent = 'Total: ' + fmt(total);
            })
            .catch(err => {
                console.error('SvDetallesPedido error:', err);
                document.getElementById('modal-order-items').innerHTML =
                    '<tr><td colspan="4" style="color:#c62828;text-align:center;padding:16px">Error: ' + err.message + '</td></tr>';
            });
    }

    function closeOrderDetail() {
        const modal = document.getElementById('modal-order-detail');
        if (modal) modal.classList.remove('open');
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
            tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;color:var(--admin-muted)">Sin usuarios registrados</td></tr>';
            return;
        }
        tbody.innerHTML = users.map(u => {
            const tels = (u.telefonos && u.telefonos.length > 0)
                ? u.telefonos.map(t => t.numero).join(', ')
                : '<span style="color:var(--admin-muted)">—</span>';
            const dir = u.direccion || '<span style="color:var(--admin-muted)">—</span>';
            return '<tr>' +
                '<td>#' + u.id + '</td>' +
                '<td><strong>' + (u.nombre || 'Admin') + '</strong></td>' +
                '<td>' + u.correo + '</td>' +
                '<td>' + tels + '</td>' +
                '<td style="max-width:160px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">' + dir + '</td>' +
                '<td style="text-align:center">' + (u.numPedidos || 0) + '</td>' +
                '<td>' + (u.registro ? u.registro.substring(0, 10) : '—') + '</td>' +
                '<td>' + statusBadge(u.activo ? 'Activo' : 'Inactivo') + '</td>' +
                '<td><button class="btn btn-secondary btn-sm" onclick="adminApp.verUsuario(' + u.id + ')">Ver</button></td>' +
                '</tr>';
        }).join('');
    }

    function verUsuario(idUsuario) {
        const u = _users.find(x => x.id === idUsuario);
        if (!u) return;
        const modal = document.getElementById('modal-user-detail');
        if (!modal) return;
        document.getElementById('modal-user-title').textContent = u.nombre || 'Usuario #' + u.id;

        const row = (label, value) =>
            '<div style="display:flex;justify-content:space-between;align-items:center;padding:10px 14px;background:var(--admin-card);border:1px solid var(--admin-border);border-radius:6px">' +
            '<span style="color:var(--admin-muted);font-weight:600;flex-shrink:0">' + label + '</span>' +
            '<span style="color:var(--admin-text);font-weight:500;text-align:right;max-width:280px">' + (value || '—') + '</span></div>';

        const telsHtml = (u.telefonos && u.telefonos.length > 0)
            ? u.telefonos.map(t => t.numero + ' <span style="color:#999;font-size:12px">(' + t.tipo + ')</span>').join('<br>')
            : '—';
        const correosHtml = (u.correosAdicionales && u.correosAdicionales.length > 0)
            ? u.correosAdicionales.map(c => c.correo + (c.principal ? ' <span style="color:#2e7d32;font-size:11px">(principal)</span>' : '')).join('<br>')
            : '—';

        // Construir select de roles
        const rolesDisponibles = _roles.length > 0 ? _roles : [];
        const rolSelectHtml = '<div style="display:flex;align-items:center;gap:8px;justify-content:flex-end">' +
            '<select id="user-rol-select" style="padding:6px 10px;border:1px solid var(--admin-border);border-radius:6px;font-size:13px;background:var(--admin-card);color:var(--admin-text)">' +
            rolesDisponibles.filter(r => r.activo).map(r =>
                '<option value="' + r.id + '"' + (r.id === u.idRol ? ' selected' : '') + '>' + esc(r.nombre) + '</option>'
            ).join('') +
            '</select>' +
            '<button class="btn btn-primary btn-sm" onclick="adminApp.cambiarRolUsuario(' + u.id + ')" style="white-space:nowrap">Guardar</button>' +
            '</div>';

        document.getElementById('modal-user-body').innerHTML =
            row('ID', '#' + u.id) +
            row('Nombre', u.nombre || 'Admin') +
            row('Correo', u.correo) +
            '<div style="display:flex;justify-content:space-between;align-items:center;padding:10px 14px;background:var(--admin-card);border:1px solid var(--admin-border);border-radius:6px">' +
            '<span style="color:var(--admin-muted);font-weight:600;flex-shrink:0">Rol</span>' +
            (rolesDisponibles.length > 0 ? rolSelectHtml : '<span style="color:var(--admin-text);font-weight:500">' + esc(u.rol) + '</span>') +
            '</div>' +
            row('Direcci&oacute;n', u.direccion || 'Sin especificar') +
            row('Tel&eacute;fonos', telsHtml) +
            row('Correos adicionales', correosHtml) +
            row('N&uacute;m. pedidos', u.numPedidos || 0) +
            row('Registro', u.registro ? u.registro.substring(0, 10) : '—') +
            row('Estado', u.activo ? '<span style="color:#2e7d32">Activo</span>' : '<span style="color:#c62828">Inactivo</span>');

        // Si aún no se cargaron roles, cargarlos y refrescar el modal
        if (rolesDisponibles.length === 0) {
            get('SvPermisos').then(data => {
                if (Array.isArray(data)) { _roles = data; verUsuario(idUsuario); }
            }).catch(() => {});
        }

        modal.classList.add('open');
    }

    function cambiarRolUsuario(idUsuario) {
        const sel = document.getElementById('user-rol-select');
        if (!sel) return;
        const idRol = parseInt(sel.value);
        if (!idRol) return;
        post('SvUsuarios', { accion: 'cambiarRol', id: idUsuario, idRol: idRol })
            .then(r => {
                if (r.error) { alert('Error: ' + r.error); return; }
                showToast('Rol actualizado a: ' + r.rol);
                // Actualizar caché local
                const u = _users.find(x => x.id === idUsuario);
                if (u) {
                    const rolObj = _roles.find(x => x.id === idRol);
                    u.idRol = idRol;
                    u.rol = rolObj ? rolObj.nombre : r.rol;
                }
                closeUserDetail();
                loadUsers();
            })
            .catch(e => alert('Error de conexi\u00f3n: ' + e.message));
    }

    function closeUserDetail() {
        const modal = document.getElementById('modal-user-detail');
        if (modal) modal.classList.remove('open');
    }

    // ─── Notificaciones en tiempo real (polling) ─────────────────────────────
    let _lastPedidoCount = -1;
    let _pollingInterval = null;

    function showToast(msg) {
        let container = document.getElementById('admin-toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'admin-toast-container';
            container.style.cssText = 'position:fixed;top:20px;right:20px;z-index:99999;display:flex;flex-direction:column;gap:10px';
            document.body.appendChild(container);
        }
        const toast = document.createElement('div');
        toast.style.cssText = 'background:#1a1a1a;color:#fff;padding:14px 20px;border-radius:8px;font-size:14px;max-width:320px;box-shadow:0 4px 20px rgba(0,0,0,.3);display:flex;align-items:center;gap:12px;animation:slideIn .3s ease';
        toast.innerHTML = '<span style="font-size:22px">🛍️</span><div><strong style="display:block;margin-bottom:2px">Nuevo pedido recibido</strong><span style="color:#ccc;font-size:13px">' + msg + '</span></div>';
        container.appendChild(toast);
        setTimeout(() => { toast.style.opacity = '0'; toast.style.transition = 'opacity .4s'; setTimeout(() => toast.remove(), 400); }, 5000);
    }

    function checkNewOrders() {
        get('SvDashboard').then(data => {
            if (data.error || data.pedidos === undefined) return;
            const count = parseInt(data.pedidos, 10);
            if (_lastPedidoCount === -1) {
                _lastPedidoCount = count;
                return;
            }
            if (count > _lastPedidoCount) {
                const diff = count - _lastPedidoCount;
                showToast(diff === 1 ? '1 nuevo pedido pendiente' : diff + ' nuevos pedidos pendientes');
                _lastPedidoCount = count;
                // Recargar sección activa si es dashboard o pedidos
                const active = document.querySelector('.admin-section.active');
                if (active) {
                    if (active.id === 'section-dashboard') loadDashboard();
                    if (active.id === 'section-pedidos')   loadOrders();
                }
            } else {
                _lastPedidoCount = count;
            }
        }).catch(() => {});
    }

    function startPolling() {
        checkNewOrders();
        _pollingInterval = setInterval(checkNewOrders, 30000);
    }

    // ─── Categorías ──────────────────────────────────────────────────────────
    let _categorias = [];

    function loadCategorias() {
        get('SvCategorias').then(data => {
            if (!Array.isArray(data)) return;
            _categorias = data;
            const tbody = document.getElementById('categorias-tbody');
            if (!tbody) return;
            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--admin-muted)">No hay categor&#237;as registradas</td></tr>';
                return;
            }
            tbody.innerHTML = data.map(c =>
                '<tr>' +
                '<td>' + c.id + '</td>' +
                '<td><strong>' + esc(c.nombre) + '</strong></td>' +
                '<td>' + esc(c.descripcion || '—') + '</td>' +
                '<td>' + c.productos + '</td>' +
                '<td>' + (c.activo ? '<span class="badge badge-success">Activa</span>' : '<span class="badge badge-danger">Inactiva</span>') + '</td>' +
                '<td style="display:flex;gap:8px">' +
                  '<button class="btn btn-secondary btn-sm" onclick="adminApp.editCategoria(' + c.id + ')">Editar</button>' +
                  '<button class="btn btn-sm" style="background:rgba(245,158,11,.15);color:#f59e0b" onclick="adminApp.toggleCategoria(' + c.id + ')">' + (c.activo ? 'Desactivar' : 'Activar') + '</button>' +
                  (c.productos === 0 ? '<button class="btn btn-danger btn-sm" onclick="adminApp.deleteCategoria(' + c.id + ')">Eliminar</button>' : '') +
                '</td>' +
                '</tr>'
            ).join('');
        }).catch(e => console.error('loadCategorias:', e));
    }

    function openCategoriaModal(id) {
        const title = document.getElementById('modal-categoria-title');
        document.getElementById('cat-id').value = '';
        document.getElementById('cat-nombre').value = '';
        document.getElementById('cat-descripcion').value = '';
        if (id) {
            const c = _categorias.find(x => x.id === id);
            if (c) {
                title.textContent = 'Editar Categor\u00eda';
                document.getElementById('cat-id').value = c.id;
                document.getElementById('cat-nombre').value = c.nombre;
                document.getElementById('cat-descripcion').value = c.descripcion || '';
            }
        } else {
            title.textContent = 'Nueva Categor\u00eda';
        }
        document.getElementById('modal-categoria').classList.add('open');
    }

    function saveCategoria() {
        const id     = document.getElementById('cat-id').value;
        const nombre = document.getElementById('cat-nombre').value.trim();
        const desc   = document.getElementById('cat-descripcion').value.trim();
        if (!nombre) { alert('El nombre es obligatorio'); return; }
        const params = { nombre, descripcion: desc };
        if (id) { params.accion = 'editar'; params.id = id; }
        post('SvCategorias', params).then(r => {
            if (r.error) { alert(r.error); return; }
            document.getElementById('modal-categoria').classList.remove('open');
            showToast('Categor\u00eda guardada correctamente');
            loadCategorias();
        }).catch(e => alert('Error: ' + e));
    }

    function editCategoria(id)   { openCategoriaModal(id); }

    function toggleCategoria(id) {
        post('SvCategorias', { accion: 'desactivar', id, nombre: _categorias.find(c=>c.id===id)?.nombre || '' })
            .then(r => { if (r.error) { alert(r.error); return; } loadCategorias(); })
            .catch(e => alert('Error: ' + e));
    }

    function deleteCategoria(id) {
        if (!confirm('\u00bfEliminar esta categor\u00eda?')) return;
        post('SvCategorias', { accion: 'eliminar', id })
            .then(r => { if (r.error) { alert(r.error); return; } showToast('Categor\u00eda eliminada'); loadCategorias(); })
            .catch(e => alert('Error: ' + e));
    }

    // ─── Marcas ──────────────────────────────────────────────────────────────
    let _marcas = [];

    function loadMarcas() {
        get('SvMarcas').then(data => {
            if (!Array.isArray(data)) return;
            _marcas = data;
            const tbody = document.getElementById('marcas-tbody');
            if (!tbody) return;
            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--admin-muted)">No hay marcas registradas</td></tr>';
                return;
            }
            tbody.innerHTML = data.map(m =>
                '<tr>' +
                '<td>' + m.id + '</td>' +
                '<td><strong>' + esc(m.nombre) + '</strong></td>' +
                '<td>' + esc(m.descripcion || '—') + '</td>' +
                '<td>' + m.productos + '</td>' +
                '<td>' + (m.activo ? '<span class="badge badge-success">Activa</span>' : '<span class="badge badge-danger">Inactiva</span>') + '</td>' +
                '<td style="display:flex;gap:8px">' +
                  '<button class="btn btn-secondary btn-sm" onclick="adminApp.editMarca(' + m.id + ')">Editar</button>' +
                  '<button class="btn btn-sm" style="background:rgba(245,158,11,.15);color:#f59e0b" onclick="adminApp.toggleMarca(' + m.id + ')">' + (m.activo ? 'Desactivar' : 'Activar') + '</button>' +
                  (m.productos === 0 ? '<button class="btn btn-danger btn-sm" onclick="adminApp.deleteMarca(' + m.id + ')">Eliminar</button>' : '') +
                '</td>' +
                '</tr>'
            ).join('');
        }).catch(e => console.error('loadMarcas:', e));
    }

    function openMarcaModal(id) {
        const title = document.getElementById('modal-marca-title');
        document.getElementById('marca-id').value = '';
        document.getElementById('marca-nombre').value = '';
        document.getElementById('marca-descripcion').value = '';
        document.getElementById('marca-genero').value = 'HOMBRE';
        const generoGroup = document.getElementById('marca-genero-group');
        if (id) {
            const m = _marcas.find(x => x.id === id);
            if (m) {
                title.textContent = 'Editar Marca';
                document.getElementById('marca-id').value = m.id;
                document.getElementById('marca-nombre').value = m.nombre;
                document.getElementById('marca-descripcion').value = m.descripcion || '';
                generoGroup.style.display = 'none';
            }
        } else {
            title.textContent = 'Nueva Marca';
            generoGroup.style.display = '';
        }
        document.getElementById('modal-marca').classList.add('open');
    }

    function saveMarca() {
        const id     = document.getElementById('marca-id').value;
        const nombre = document.getElementById('marca-nombre').value.trim();
        const desc   = document.getElementById('marca-descripcion').value.trim();
        if (!nombre) { alert('El nombre es obligatorio'); return; }
        const genero = document.getElementById('marca-genero').value;
        const params = { nombre, descripcion: desc };
        if (id) { params.accion = 'editar'; params.id = id; }
        else { params.genero = genero; }
        post('SvMarcas', params).then(r => {
            if (r.error) { alert(r.error); return; }
            document.getElementById('modal-marca').classList.remove('open');
            showToast('Marca guardada correctamente');
            loadMarcas();
        }).catch(e => alert('Error: ' + e));
    }

    function editMarca(id)   { openMarcaModal(id); }

    function toggleMarca(id) {
        post('SvMarcas', { accion: 'desactivar', id, nombre: _marcas.find(m=>m.id===id)?.nombre || '' })
            .then(r => { if (r.error) { alert(r.error); return; } loadMarcas(); })
            .catch(e => alert('Error: ' + e));
    }

    function deleteMarca(id) {
        if (!confirm('\u00bfEliminar esta marca?')) return;
        post('SvMarcas', { accion: 'eliminar', id })
            .then(r => { if (r.error) { alert(r.error); return; } showToast('Marca eliminada'); loadMarcas(); })
            .catch(e => alert('Error: ' + e));
    }

    function esc(s) {
        if (!s) return '';
        return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }

    // ─── Roles y Permisos (RF07/RF08) ─────────────────────────────────────────
    let _roles = [];
    let _permisosCatalogo = [];
    let _permisosActivoTab = 'roles';

    function permisosTab(tab) {
        _permisosActivoTab = tab;
        document.getElementById('permisos-panel-roles').style.display        = tab === 'roles'        ? '' : 'none';
        document.getElementById('permisos-panel-permisosList').style.display = tab === 'permisosList' ? '' : 'none';
        document.getElementById('permisos-tab-roles').className    = tab === 'roles'        ? 'btn btn-primary' : 'btn btn-secondary';
        document.getElementById('permisos-tab-permisos').className = tab === 'permisosList' ? 'btn btn-primary' : 'btn btn-secondary';
        if (tab === 'permisosList') loadPermisosCatalogo();
        else loadRoles();
    }

    function loadPermisos() {
        loadRoles();
    }

    function loadRoles() {
        get('SvPermisos').then(data => {
            if (!Array.isArray(data)) return;
            _roles = data;
            const tbody = document.getElementById('roles-tbody');
            if (!tbody) return;
            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--admin-muted)">Sin roles registrados</td></tr>';
                return;
            }
            tbody.innerHTML = data.map(r =>
                '<tr>' +
                '<td>' + r.id + '</td>' +
                '<td><strong>' + esc(r.nombre) + '</strong></td>' +
                '<td>' + esc(r.descripcion || '—') + '</td>' +
                '<td>' + (r.activo ? '<span class="badge badge-success">Activo</span>' : '<span class="badge badge-danger">Inactivo</span>') + '</td>' +
                '<td><span style="font-size:12px;color:var(--admin-muted)">' + (r.permisos ? r.permisos.length : 0) + ' permiso(s)</span> ' +
                  '<button class="btn btn-sm" style="background:rgba(99,102,241,.15);color:var(--admin-info)" onclick="adminApp.abrirAsignar(' + r.id + ')">Gestionar</button></td>' +
                '<td style="display:flex;gap:6px">' +
                  '<button class="btn btn-secondary btn-sm" onclick="adminApp.editRol(' + r.id + ')">Editar</button>' +
                  '<button class="btn btn-sm" style="background:rgba(245,158,11,.15);color:#f59e0b" onclick="adminApp.toggleRol(' + r.id + ')">' + (r.activo ? 'Desactivar' : 'Activar') + '</button>' +
                '</td>' +
                '</tr>'
            ).join('');
        }).catch(e => console.error('loadRoles:', e));
    }

    function loadPermisosCatalogo() {
        get('SvPermisos?recurso=permisos').then(data => {
            if (!Array.isArray(data)) return;
            _permisosCatalogo = data;
            const tbody = document.getElementById('permisos-tbody');
            if (!tbody) return;
            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--admin-muted)">Sin permisos registrados</td></tr>';
                return;
            }
            tbody.innerHTML = data.map(p =>
                '<tr>' +
                '<td>' + p.id + '</td>' +
                '<td><strong>' + esc(p.nombre) + '</strong></td>' +
                '<td>' + esc(p.modulo || '—') + '</td>' +
                '<td>' + esc(p.descripcion || '—') + '</td>' +
                '<td>' + (p.activo ? '<span class="badge badge-success">Activo</span>' : '<span class="badge badge-danger">Inactivo</span>') + '</td>' +
                '<td style="display:flex;gap:6px">' +
                  '<button class="btn btn-secondary btn-sm" onclick="adminApp.editPermiso(' + p.id + ')">Editar</button>' +
                  '<button class="btn btn-sm" style="background:rgba(245,158,11,.15);color:#f59e0b" onclick="adminApp.togglePermiso(' + p.id + ')">' + (p.activo ? 'Desactivar' : 'Activar') + '</button>' +
                '</td>' +
                '</tr>'
            ).join('');
        }).catch(e => console.error('loadPermisosCatalogo:', e));
    }

    // -- Rol CRUD --
    function openRolModal(id) {
        document.getElementById('rol-id').value = '';
        document.getElementById('rol-nombre').value = '';
        document.getElementById('rol-descripcion').value = '';
        if (id) {
            const r = _roles.find(x => x.id === id);
            if (r) {
                document.getElementById('modal-rol-title').textContent = 'Editar Rol';
                document.getElementById('rol-id').value = r.id;
                document.getElementById('rol-nombre').value = r.nombre;
                document.getElementById('rol-descripcion').value = r.descripcion || '';
            }
        } else {
            document.getElementById('modal-rol-title').textContent = 'Nuevo Rol';
        }
        document.getElementById('modal-rol').classList.add('open');
    }

    function saveRol() {
        const id     = document.getElementById('rol-id').value;
        const nombre = document.getElementById('rol-nombre').value.trim();
        const desc   = document.getElementById('rol-descripcion').value.trim();
        if (!nombre) { alert('El nombre es obligatorio'); return; }
        const params = id
            ? { accion: 'editarRol', id, nombre, descripcion: desc }
            : { accion: 'crearRol',  nombre, descripcion: desc };
        post('SvPermisos', params).then(r => {
            if (r.error) { alert(r.error); return; }
            document.getElementById('modal-rol').classList.remove('open');
            showToast('Rol guardado');
            loadRoles();
        }).catch(e => alert('Error: ' + e));
    }

    function editRol(id)   { openRolModal(id); }

    function toggleRol(id) {
        post('SvPermisos', { accion: 'toggleRol', id })
            .then(r => { if (r.error) { alert(r.error); return; } loadRoles(); })
            .catch(e => alert('Error: ' + e));
    }

    // -- Permiso CRUD --
    function openPermisoModal(id) {
        document.getElementById('permiso-id').value = '';
        document.getElementById('permiso-nombre').value = '';
        document.getElementById('permiso-modulo').value = '';
        document.getElementById('permiso-descripcion').value = '';
        if (id) {
            const p = _permisosCatalogo.find(x => x.id === id);
            if (p) {
                document.getElementById('modal-permiso-title').textContent = 'Editar Permiso';
                document.getElementById('permiso-id').value = p.id;
                document.getElementById('permiso-nombre').value = p.nombre;
                document.getElementById('permiso-modulo').value = p.modulo || '';
                document.getElementById('permiso-descripcion').value = p.descripcion || '';
            }
        } else {
            document.getElementById('modal-permiso-title').textContent = 'Nuevo Permiso';
        }
        document.getElementById('modal-permiso').classList.add('open');
    }

    function savePermiso() {
        const id     = document.getElementById('permiso-id').value;
        const nombre = document.getElementById('permiso-nombre').value.trim();
        const modulo = document.getElementById('permiso-modulo').value.trim();
        const desc   = document.getElementById('permiso-descripcion').value.trim();
        if (!nombre) { alert('El nombre es obligatorio'); return; }
        const params = id
            ? { accion: 'editarPermiso', id, nombre, modulo, descripcion: desc }
            : { accion: 'crearPermiso',  nombre, modulo, descripcion: desc };
        post('SvPermisos', params).then(r => {
            if (r.error) { alert(r.error); return; }
            document.getElementById('modal-permiso').classList.remove('open');
            showToast('Permiso guardado');
            loadPermisosCatalogo();
        }).catch(e => alert('Error: ' + e));
    }

    function editPermiso(id)   { openPermisoModal(id); }

    function togglePermiso(id) {
        post('SvPermisos', { accion: 'togglePermiso', id })
            .then(r => { if (r.error) { alert(r.error); return; } loadPermisosCatalogo(); })
            .catch(e => alert('Error: ' + e));
    }

    // -- Asignar permisos a rol --
    function abrirAsignar(idRol) {
        document.getElementById('asignar-id-rol').value = idRol;
        const rol = _roles.find(r => r.id === idRol);
        document.getElementById('modal-asignar-title').textContent =
            'Permisos del Rol: ' + (rol ? rol.nombre : idRol);
        // Llenar select con todos los permisos activos
        get('SvPermisos?recurso=permisos').then(todos => {
            _permisosCatalogo = todos;
            const sel = document.getElementById('asignar-permiso-sel');
            const asignados = rol ? (rol.permisos || []).map(p => p.idPermiso) : [];
            const disponibles = todos.filter(p => p.activo && !asignados.includes(p.id));
            sel.innerHTML = disponibles.length === 0
                ? '<option value="">-- Todos los permisos ya asignados --</option>'
                : disponibles.map(p => '<option value="' + p.id + '">[' + esc(p.modulo || '') + '] ' + esc(p.nombre) + '</option>').join('');
        }).catch(() => {});
        // Mostrar permisos actuales
        _renderPermisoActuales(idRol);
        document.getElementById('modal-asignar-permisos').classList.add('open');
    }

    function _renderPermisoActuales(idRol) {
        const rol = _roles.find(r => r.id === idRol);
        const el  = document.getElementById('asignar-permisos-actuales');
        if (!rol || !rol.permisos || rol.permisos.length === 0) {
            el.innerHTML = '<p style="color:var(--admin-muted);font-size:13px">Sin permisos asignados aún.</p>';
            return;
        }
        el.innerHTML = '<p style="font-size:12px;font-weight:600;text-transform:uppercase;color:var(--admin-muted);margin-bottom:8px">Permisos actuales</p>' +
            rol.permisos.map(p =>
                '<div style="display:flex;justify-content:space-between;align-items:center;padding:7px 12px;background:rgba(99,102,241,.08);border-radius:6px;margin-bottom:5px;font-size:13px">' +
                '<span>' + (p.modulo ? '<span style="color:var(--admin-muted);margin-right:6px">[' + esc(p.modulo) + ']</span>' : '') + esc(p.nombre) + '</span>' +
                '<button onclick="adminApp.revocarPermiso(' + p.idRolPermiso + ',' + idRol + ')" style="background:none;border:none;color:var(--admin-danger);cursor:pointer;font-size:16px;padding:0 4px" title="Revocar">&#10005;</button>' +
                '</div>'
            ).join('');
    }

    function asignarPermiso() {
        const idRol     = parseInt(document.getElementById('asignar-id-rol').value);
        const idPermiso = parseInt(document.getElementById('asignar-permiso-sel').value);
        if (!idPermiso) return;
        post('SvPermisos', { accion: 'asignar', idRol, idPermiso })
            .then(r => {
                if (r.error) { alert(r.error); return; }
                showToast('Permiso asignado');
                loadRoles();
                // Refrescar modal
                setTimeout(() => abrirAsignar(idRol), 400);
            }).catch(e => alert('Error: ' + e));
    }

    function revocarPermiso(idRolPermiso, idRol) {
        if (!confirm('\u00bfRevocar este permiso?')) return;
        post('SvPermisos', { accion: 'revocar', idRolPermiso })
            .then(r => {
                if (r.error) { alert(r.error); return; }
                showToast('Permiso revocado');
                loadRoles();
                setTimeout(() => abrirAsignar(idRol), 400);
            }).catch(e => alert('Error: ' + e));
    }

    // ─── Pagos (RF020-RF022) ─────────────────────────────────────────────────
    function abrirPago(idPedido) {
        document.getElementById('pago-id-pedido').value = idPedido;
        document.getElementById('pago-monto').value = '';
        document.getElementById('pago-referencia').value = '';
        document.getElementById('pago-lista').innerHTML = '<p style="color:var(--admin-muted);font-size:13px">Cargando pagos...</p>';
        document.getElementById('pago-resumen').innerHTML = '';
        document.getElementById('modal-pago').classList.add('open');
        get('SvPagos?idPedido=' + idPedido).then(data => {
            const res = document.getElementById('pago-resumen');
            const lista = document.getElementById('pago-lista');
            res.innerHTML =
                '<span><strong>Total pedido:</strong> ' + fmt(data.totalPedido) + '</span>' +
                '<span><strong>Pagado:</strong> ' + fmt(data.sumaPagada) + '</span>' +
                '<span style="color:' + (parseFloat(data.pendiente) > 0 ? 'var(--admin-warning)' : 'var(--admin-success)') + '"><strong>Pendiente:</strong> ' + fmt(data.pendiente) + '</span>';
            document.getElementById('pago-monto').value = parseFloat(data.pendiente) > 0 ? parseFloat(data.pendiente).toFixed(0) : '';
            if (!data.pagos || data.pagos.length === 0) {
                lista.innerHTML = '<p style="color:var(--admin-muted);font-size:13px">Sin pagos registrados</p>';
            } else {
                lista.innerHTML = '<p style="font-size:12px;font-weight:600;text-transform:uppercase;color:var(--admin-muted);margin-bottom:6px">Pagos registrados</p>' +
                    '<table class="admin-table" style="font-size:12px">' +
                    '<thead><tr><th>M&eacute;todo</th><th>Monto</th><th>Estado</th><th>Fecha</th><th>Ref.</th></tr></thead><tbody>' +
                    data.pagos.map(p =>
                        '<tr><td>' + esc(p.metodo) + '</td><td>' + fmt(p.monto) + '</td>' +
                        '<td>' + p.estado + '</td>' +
                        '<td>' + (p.fecha ? p.fecha.substring(0,10) : '—') + '</td>' +
                        '<td>' + esc(p.referencia || '—') + '</td></tr>'
                    ).join('') + '</tbody></table>';
            }
        }).catch(e => { document.getElementById('pago-lista').innerHTML = '<p style="color:var(--admin-danger)">Error cargando pagos</p>'; });
    }

    function savePago() {
        const idPedido = document.getElementById('pago-id-pedido').value;
        const metodo   = document.getElementById('pago-metodo').value;
        const monto    = document.getElementById('pago-monto').value;
        const ref      = document.getElementById('pago-referencia').value;
        if (!monto || parseFloat(monto) <= 0) { alert('Ingresa un monto válido'); return; }
        post('SvPagos', { idPedido, metodo, monto, referencia: ref })
            .then(r => {
                if (r.error) { alert(r.error); return; }
                showToast('Pago registrado correctamente' + (r.estadoActualizado ? ' — Pedido marcado como PAGO' : ''));
                document.getElementById('modal-pago').classList.remove('open');
                loadOrders();
            }).catch(e => alert('Error: ' + e));
    }

    function closePagoModal() { document.getElementById('modal-pago').classList.remove('open'); }

    // ─── Envíos (RF023-RF025) ────────────────────────────────────────────────
    function abrirEnvio(idPedido) {
        document.getElementById('envio-id-pedido').value = idPedido;
        document.getElementById('envio-id-envio').value = '';
        document.getElementById('envio-direccion').value = '';
        document.getElementById('envio-transportadora').value = '';
        document.getElementById('envio-guia').value = '';
        document.getElementById('envio-fecha-est').value = '';
        document.getElementById('envio-estado-group').style.display = 'none';
        document.getElementById('modal-envio-title').textContent = 'Registrar Envío';
        document.getElementById('save-envio-btn').textContent = 'Crear Envío';
        get('SvEnvios?idPedido=' + idPedido).then(data => {
            if (data.envio) {
                const e = data.envio;
                document.getElementById('modal-envio-title').textContent = 'Actualizar Envío #' + e.id;
                document.getElementById('save-envio-btn').textContent = 'Guardar Cambios';
                document.getElementById('envio-id-envio').value = e.id;
                document.getElementById('envio-direccion').value = e.direccion || '';
                document.getElementById('envio-transportadora').value = e.transportadora || '';
                document.getElementById('envio-guia').value = e.guia || '';
                document.getElementById('envio-estado-group').style.display = 'block';
                const selEst = document.getElementById('envio-estado');
                if (selEst) selEst.value = e.estado || 'PREPARANDO';
            }
        }).catch(() => {});
        document.getElementById('modal-envio').classList.add('open');
    }

    function saveEnvio() {
        const idPedido   = document.getElementById('envio-id-pedido').value;
        const idEnvio    = document.getElementById('envio-id-envio').value;
        const direccion  = document.getElementById('envio-direccion').value.trim();
        const transport  = document.getElementById('envio-transportadora').value.trim();
        const guia       = document.getElementById('envio-guia').value.trim();
        const fechaEst   = document.getElementById('envio-fecha-est').value;
        const estado     = document.getElementById('envio-estado').value;
        if (!direccion) { alert('La dirección de envío es obligatoria'); return; }
        const params = { idPedido, direccion, transportadora: transport, guia, fechaEstimada: fechaEst };
        if (idEnvio) { params.accion = 'actualizar'; params.idEnvio = idEnvio; params.estado = estado; }
        post('SvEnvios', params)
            .then(r => {
                if (r.error) { alert(r.error); return; }
                showToast(idEnvio ? 'Envío actualizado' : 'Envío registrado — Pedido marcado como ENVIADO');
                document.getElementById('modal-envio').classList.remove('open');
                loadOrders();
            }).catch(e => alert('Error: ' + e));
    }

    function closeEnvioModal() { document.getElementById('modal-envio').classList.remove('open'); }

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

        // Roles y Permisos
        document.getElementById('btn-add-rol')?.addEventListener('click', () => openRolModal(null));
        document.getElementById('save-rol-btn')?.addEventListener('click', saveRol);
        document.getElementById('modal-rol')?.addEventListener('click', e => {
            if (e.target === document.getElementById('modal-rol')) document.getElementById('modal-rol').classList.remove('open');
        });
        document.getElementById('btn-add-permiso')?.addEventListener('click', () => openPermisoModal(null));
        document.getElementById('save-permiso-btn')?.addEventListener('click', savePermiso);
        document.getElementById('modal-permiso')?.addEventListener('click', e => {
            if (e.target === document.getElementById('modal-permiso')) document.getElementById('modal-permiso').classList.remove('open');
        });
        document.getElementById('btn-asignar-permiso')?.addEventListener('click', asignarPermiso);
        document.getElementById('modal-asignar-permisos')?.addEventListener('click', e => {
            if (e.target === document.getElementById('modal-asignar-permisos')) document.getElementById('modal-asignar-permisos').classList.remove('open');
        });

        document.getElementById('save-pago-btn')?.addEventListener('click', savePago);
        document.getElementById('modal-pago')?.addEventListener('click', e => {
            if (e.target === document.getElementById('modal-pago')) closePagoModal();
        });
        document.getElementById('save-envio-btn')?.addEventListener('click', saveEnvio);
        document.getElementById('modal-envio')?.addEventListener('click', e => {
            if (e.target === document.getElementById('modal-envio')) closeEnvioModal();
        });

        document.getElementById('modal-user-detail')?.addEventListener('click', e => {
            if (e.target === document.getElementById('modal-user-detail')) closeUserDetail();
        });

        document.getElementById('modal-order-detail')?.addEventListener('click', e => {
            if (e.target === document.getElementById('modal-order-detail')) closeOrderDetail();
        });

        document.getElementById('btn-add-categoria')?.addEventListener('click', () => openCategoriaModal(null));
        document.getElementById('save-categoria-btn')?.addEventListener('click', saveCategoria);
        document.getElementById('modal-categoria')?.addEventListener('click', e => {
            if (e.target === document.getElementById('modal-categoria')) document.getElementById('modal-categoria').classList.remove('open');
        });

        document.getElementById('btn-add-marca')?.addEventListener('click', () => openMarcaModal(null));
        document.getElementById('save-marca-btn')?.addEventListener('click', saveMarca);
        document.getElementById('modal-marca')?.addEventListener('click', e => {
            if (e.target === document.getElementById('modal-marca')) document.getElementById('modal-marca').classList.remove('open');
        });

        navigate('dashboard');
        startPolling();
    }

    // ─── Public API ──────────────────────────────────────────────────────────
    window.adminApp = { editProduct, deleteProduct, cambiarEstado, verDetalle, closeOrderDetail, verUsuario, closeUserDetail, cambiarRolUsuario, editCategoria, toggleCategoria, deleteCategoria, editMarca, toggleMarca, deleteMarca, abrirPago, closePagoModal, abrirEnvio, closeEnvioModal, permisosTab, editRol, toggleRol, editPermiso, togglePermiso, abrirAsignar, revocarPermiso };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
