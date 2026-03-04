/* ===========================
   CART SYSTEM - ANDREYLPZ
   Handles: localStorage, counter badge,
   side panel, add/remove/qty controls
   =========================== */

(function () {
    'use strict';

    // ─── State ───────────────────────────────────────────────────────────────
    const STORAGE_KEY = 'andreylpz_cart';

    function getCart() {
        try {
            return JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];
        } catch {
            return [];
        }
    }

    function saveCart(cart) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(cart));
    }

    // ─── Cart HTML Injection ──────────────────────────────────────────────────
    function injectCartHTML() {
        const html = `
        <!-- Cart Overlay -->
        <div class="cart-overlay" id="cartOverlay"></div>

        <!-- Cart Side Panel -->
        <aside class="cart-panel" id="cartPanel" aria-label="Carrito de compras">
            <div class="cart-panel__header">
                <h2 class="cart-panel__title">Mi Carrito</h2>
                <button class="cart-panel__close" id="cartClose" aria-label="Cerrar carrito">✕</button>
            </div>
            <div class="cart-panel__body" id="cartBody">
                <!-- Items will be rendered here -->
            </div>
            <div class="cart-panel__footer">
                <div class="cart-panel__subtotal">
                    <span class="cart-panel__subtotal-label">Subtotal</span>
                    <span class="cart-panel__subtotal-value" id="cartSubtotal">0 COP</span>
                </div>
                <button class="cart-panel__checkout-btn" id="cartCheckout">
                    Finalizar Compra
                </button>
            </div>
        </aside>
        `;
        document.body.insertAdjacentHTML('beforeend', html);
    }

    // ─── Badge Counter ────────────────────────────────────────────────────────
    function updateBadge() {
        const cart = getCart();
        const total = cart.reduce((sum, item) => sum + item.qty, 0);

        // Find all badge count elements (there might be multiple navbars conceptually)
        document.querySelectorAll('.cart-badge__count').forEach(el => {
            el.textContent = total;
            el.classList.toggle('visible', total > 0);
        });
    }

    // ─── Wrap the shopping icon with the badge wrapper ────────────────────────
    function wrapShoppingIcon() {
        // The shopping icon link: <a href="#compras"><img src="shopping.png"></a>
        const shoppingLinks = document.querySelectorAll('a[href="#compras"]');
        shoppingLinks.forEach(link => {
            if (link.querySelector('.cart-badge__count')) return; // already wrapped

            link.classList.add('cart-badge');
            link.setAttribute('href', '#');
            link.setAttribute('id', 'cartToggle');
            link.setAttribute('aria-label', 'Abrir carrito');

            const badge = document.createElement('span');
            badge.className = 'cart-badge__count';
            badge.setAttribute('aria-live', 'polite');
            link.appendChild(badge);
        });
    }

    // ─── Render Cart Items ────────────────────────────────────────────────────
    function formatPrice(value) {
        return value.toLocaleString('es-CO') + ' COP';
    }

    function parsePrice(str) {
        // "450.000 COP" → 450000
        return parseInt(str.replace(/\./g, '').replace(/[^0-9]/g, ''), 10) || 0;
    }

    function renderCart() {
        const cart = getCart();
        const body = document.getElementById('cartBody');
        const subtotalEl = document.getElementById('cartSubtotal');
        if (!body) return;

        if (cart.length === 0) {
            body.innerHTML = `
                <div class="cart-panel__empty">
                    <div class="cart-panel__empty-icon">◇</div>
                    <p>Tu carrito está vacío</p>
                </div>`;
            if (subtotalEl) subtotalEl.textContent = '0 COP';
            return;
        }

        let subtotal = 0;
        body.innerHTML = cart.map((item, index) => {
            const lineTotal = item.price * item.qty;
            subtotal += lineTotal;
            return `
            <div class="cart-item" data-index="${index}">
                <img class="cart-item__img" src="${item.image}" alt="${item.name}" onerror="this.style.display='none'">
                <div class="cart-item__info">
                    <p class="cart-item__brand">${item.brand}</p>
                    <p class="cart-item__name">${item.name}</p>
                    <p class="cart-item__price">${formatPrice(lineTotal)}</p>
                    <div class="cart-item__qty-controls">
                        <button class="cart-item__qty-btn" data-action="decrease" data-index="${index}">−</button>
                        <span class="cart-item__qty">${item.qty}</span>
                        <button class="cart-item__qty-btn" data-action="increase" data-index="${index}">+</button>
                    </div>
                </div>
                <button class="cart-item__remove" data-action="remove" data-index="${index}" aria-label="Eliminar ${item.name}">✕</button>
            </div>`;
        }).join('');

        if (subtotalEl) subtotalEl.textContent = formatPrice(subtotal);
    }

    // ─── Panel Open/Close ─────────────────────────────────────────────────────
    function openCart() {
        document.getElementById('cartPanel')?.classList.add('open');
        document.getElementById('cartOverlay')?.classList.add('open');
        document.body.style.overflow = 'hidden';
        renderCart();
    }

    function closeCart() {
        document.getElementById('cartPanel')?.classList.remove('open');
        document.getElementById('cartOverlay')?.classList.remove('open');
        document.body.style.overflow = '';
    }

    // ─── Add Product ──────────────────────────────────────────────────────────
    function addProduct(product) {
        const cart = getCart();
        const existing = cart.find(item => item.id === product.id);
        if (existing) {
            existing.qty += 1;
        } else {
            cart.push({ ...product, qty: 1 });
        }
        saveCart(cart);
        updateBadge();
        renderCart();
        openCart();
    }

    // ─── Hook into "AGREGAR AL CARRITO" buttons ───────────────────────────────
    function hookAddToCartButtons() {
        const btn = document.querySelector('.section-losion__divicion__description__button');
        if (!btn) return;
        if (btn.dataset.cartManaged) return;

        // Gather product data from the page
        const nameEl = document.querySelector('.section-losion__title');
        const priceEl = document.querySelector('.section-losion__divicion__description__precios__precios');
        const imgEl = document.querySelector('.section-losion__divicion__img img');
        const brandEl = document.querySelector('.section-losion__titulo2');

        const name = nameEl ? nameEl.textContent.trim() : document.title;
        const priceStr = priceEl ? priceEl.textContent.trim() : '0 COP';
        const price = parsePrice(priceStr);
        const image = imgEl ? imgEl.getAttribute('src') : '';
        const brand = brandEl ? brandEl.textContent.trim() : '';
        // Use name + brand as a unique ID
        const id = (brand + '_' + name).toLowerCase().replace(/[^a-z0-9]/g, '_');

        btn.addEventListener('click', () => {
            addProduct({ id, name, brand, price, image });

            // Visual feedback
            btn.classList.add('added');
            const original = btn.textContent;
            btn.textContent = '✓ AÑADIDO';
            setTimeout(() => {
                btn.textContent = original;
                btn.classList.remove('added');
            }, 2000);
        });
    }

    // ─── Cart Item Controls (qty / remove) ───────────────────────────────────
    function handleCartBodyClick(e) {
        const btn = e.target.closest('[data-action]');
        if (!btn) return;

        const action = btn.dataset.action;
        const index = parseInt(btn.dataset.index, 10);
        const cart = getCart();

        if (action === 'remove') {
            cart.splice(index, 1);
        } else if (action === 'increase') {
            cart[index].qty += 1;
        } else if (action === 'decrease') {
            cart[index].qty -= 1;
            if (cart[index].qty <= 0) cart.splice(index, 1);
        }

        saveCart(cart);
        updateBadge();
        renderCart();
    }

    // ─── Checkout Button ──────────────────────────────────────────────────────
    function handleCheckout() {
        const cart = getCart();
        if (cart.length === 0) {
            alert('Tu carrito está vacío.');
            return;
        }

        const btn = document.getElementById('cartCheckout');
        if (btn) { btn.disabled = true; btn.textContent = 'Procesando...'; }

        const ctx = (function () {
            const p = window.location.pathname.split('/');
            return '/' + p[1];
        })();

        var params = new URLSearchParams();
        params.append('itemCount', cart.length);
        cart.forEach(function(item, i) {
            params.append('item_name_' + i,  item.name  || '');
            params.append('item_brand_' + i, item.brand || '');
            params.append('item_price_' + i, item.price || 0);
            params.append('item_qty_' + i,   item.qty   || 1);
        });

        fetch(ctx + '/SvCompra', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        })
        .then(function(r) {
            return r.text().then(function(text) {
                try {
                    return { status: r.status, data: JSON.parse(text) };
                } catch(e) {
                    return { status: r.status, data: { error: 'Respuesta inesperada del servidor (status ' + r.status + ')' } };
                }
            });
        })
        .then(function(res) {
            var data = res.data;
            if (res.status === 401 || (data.error && data.error.includes('sesión'))) {
                if (btn) { btn.disabled = false; btn.textContent = 'Finalizar Compra'; }
                if (confirm('Debes iniciar sesión para comprar. ¿Ir al login?')) {
                    window.location.href = ctx + '/vistas/perfil.jsp';
                }
                return;
            }
            if (data.error) {
                alert('Error: ' + data.error);
                if (btn) { btn.disabled = false; btn.textContent = 'Finalizar Compra'; }
                return;
            }
            saveCart([]);
            updateBadge();
            closeCart();
            showOrderConfirmation(data.idPedido, data.total);
        })
        .catch(function(err) {
            alert('Error de conexión: ' + err.message);
            if (btn) { btn.disabled = false; btn.textContent = 'Finalizar Compra'; }
        });
    }

    function showOrderConfirmation(idPedido, total) {
        const existing = document.getElementById('order-confirm-modal');
        if (existing) existing.remove();

        const fmtNum = n => parseFloat(n).toLocaleString('es-CO') + ' COP';
        const ctx2 = (function() { var p = window.location.pathname.split('/'); return '/' + p[1]; })();

        const modal = document.createElement('div');
        modal.id = 'order-confirm-modal';
        modal.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,0.55);z-index:9999;display:flex;align-items:center;justify-content:center;padding:16px';

        modal.innerHTML =
            '<div id="pago-box" style="background:#fff;border-radius:12px;padding:36px 32px;max-width:460px;width:100%;box-shadow:0 8px 40px rgba(0,0,0,.25)">' +
                '<h2 style="font-size:18px;font-weight:700;color:#1a1a1a;margin:0 0 4px;letter-spacing:1px">MÉTODO DE PAGO</h2>' +
                '<p style="color:#666;font-size:14px;margin:0 0 24px">Pedido <strong>#' + idPedido + '</strong> &bull; Total: <strong>' + fmtNum(total) + '</strong></p>' +

                '<div style="display:flex;flex-direction:column;gap:10px;margin-bottom:20px">' +
                    '<label style="display:flex;align-items:center;gap:10px;padding:14px 16px;border:2px solid #e0e0e0;border-radius:8px;cursor:pointer;transition:border-color .2s" id="opt-tarjeta">' +
                        '<input type="radio" name="pago-metodo" value="TARJETA_CREDITO" style="accent-color:#1a1a1a;width:18px;height:18px">' +
                        '<span style="font-size:22px">💳</span>' +
                        '<div><p style="margin:0;font-weight:600;font-size:14px">Tarjeta de crédito / débito</p><p style="margin:0;font-size:12px;color:#888">Visa, Mastercard, Amex</p></div>' +
                    '</label>' +
                    '<label style="display:flex;align-items:center;gap:10px;padding:14px 16px;border:2px solid #e0e0e0;border-radius:8px;cursor:pointer;transition:border-color .2s" id="opt-transferencia">' +
                        '<input type="radio" name="pago-metodo" value="TRANSFERENCIA" style="accent-color:#1a1a1a;width:18px;height:18px">' +
                        '<span style="font-size:22px">🏦</span>' +
                        '<div><p style="margin:0;font-weight:600;font-size:14px">Transferencia bancaria</p><p style="margin:0;font-size:12px;color:#888">PSE / Nequi / Daviplata</p></div>' +
                    '</label>' +
                    '<label style="display:flex;align-items:center;gap:10px;padding:14px 16px;border:2px solid #e0e0e0;border-radius:8px;cursor:pointer;transition:border-color .2s" id="opt-efectivo">' +
                        '<input type="radio" name="pago-metodo" value="EFECTIVO" style="accent-color:#1a1a1a;width:18px;height:18px">' +
                        '<span style="font-size:22px">💵</span>' +
                        '<div><p style="margin:0;font-weight:600;font-size:14px">Efectivo / Contraentrega</p><p style="margin:0;font-size:12px;color:#888">Pago al momento de la entrega</p></div>' +
                    '</label>' +
                '</div>' +

                '<div id="pago-ref-wrap" style="display:none;margin-bottom:16px">' +
                    '<label style="font-size:12px;color:#666;font-weight:600">NÚMERO DE REFERENCIA / COMPROBANTE (opcional)</label>' +
                    '<input id="pago-referencia" type="text" placeholder="Ej: 00123456789" style="margin-top:4px;padding:10px 14px;border:1px solid #ddd;border-radius:6px;font-size:14px;width:100%;box-sizing:border-box">' +
                '</div>' +

                '<div id="pago-err" style="display:none;padding:10px 14px;background:#ffebee;border-radius:6px;color:#c62828;font-size:13px;margin-bottom:14px"></div>' +

                '<div style="display:flex;gap:10px;flex-wrap:wrap">' +
                    '<button id="pago-confirmar-btn" onclick="confirmarPago(' + idPedido + ',' + total + ')" ' +
                        'style="flex:1;padding:13px 20px;background:#1a1a1a;color:#fff;border:none;border-radius:6px;font-size:13px;font-weight:700;letter-spacing:1px;cursor:pointer">CONFIRMAR PAGO</button>' +
                    '<button onclick="document.getElementById(\'order-confirm-modal\').remove()" ' +
                        'style="padding:13px 20px;background:#fff;color:#1a1a1a;border:1px solid #ccc;border-radius:6px;font-size:13px;cursor:pointer">Cancelar</button>' +
                '</div>' +
                '<p style="font-size:11px;color:#bbb;text-align:center;margin:14px 0 0">Tu pedido ya fue registrado. El pago confirma el procesamiento.</p>' +
            '</div>';

        document.body.appendChild(modal);

        // Resaltar opción seleccionada
        modal.querySelectorAll('input[name="pago-metodo"]').forEach(function(radio) {
            radio.addEventListener('change', function() {
                modal.querySelectorAll('label[id^="opt-"]').forEach(function(lbl) {
                    lbl.style.borderColor = '#e0e0e0';
                });
                this.closest('label').style.borderColor = '#1a1a1a';
                // Mostrar campo referencia solo en transferencia
                var refWrap = document.getElementById('pago-ref-wrap');
                if (refWrap) refWrap.style.display = (this.value === 'TRANSFERENCIA') ? 'block' : 'none';
            });
        });

        window._pagoCtx = ctx2;
    }

    window.confirmarPago = function(idPedido, total) {
        var metodoEl = document.querySelector('input[name="pago-metodo"]:checked');
        var errEl = document.getElementById('pago-err');
        var btn = document.getElementById('pago-confirmar-btn');
        if (errEl) errEl.style.display = 'none';

        if (!metodoEl) {
            if (errEl) { errEl.textContent = 'Selecciona un método de pago.'; errEl.style.display = 'block'; }
            return;
        }

        var metodo = metodoEl.value;
        var referencia = (document.getElementById('pago-referencia') || {}).value || '';
        var ctx2 = window._pagoCtx || '';
        var fmtNum = function(n) { return parseFloat(n).toLocaleString('es-CO') + ' COP'; };

        if (btn) { btn.disabled = true; btn.textContent = 'Procesando...'; }

        var body = new URLSearchParams({
            idPedido: idPedido,
            metodo: metodo,
            monto: total,
            referencia: referencia
        });

        fetch(ctx2 + '/SvPagos', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body.toString()
        })
        .then(function(r) { return r.json(); })
        .then(function(d) {
            var modal = document.getElementById('order-confirm-modal');
            if (modal) modal.remove();

            if (d.error) {
                // Recrear modal con error
                showOrderConfirmation(idPedido, total);
                var errEl2 = document.getElementById('pago-err');
                if (errEl2) { errEl2.textContent = d.error; errEl2.style.display = 'block'; }
                return;
            }

            // Mostrar confirmación final
            var conf = document.createElement('div');
            conf.id = 'order-confirm-modal';
            conf.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,0.55);z-index:9999;display:flex;align-items:center;justify-content:center;padding:16px';
            var metodosLabel = { 'TARJETA_CREDITO': 'Tarjeta', 'TRANSFERENCIA': 'Transferencia', 'EFECTIVO': 'Efectivo/Contraentrega' };
            conf.innerHTML =
                '<div style="background:#fff;border-radius:12px;padding:40px 32px;max-width:420px;width:100%;text-align:center;box-shadow:0 8px 40px rgba(0,0,0,.2)">' +
                    '<div style="font-size:48px;margin-bottom:12px">✅</div>' +
                    '<h2 style="font-size:20px;font-weight:700;margin-bottom:8px;color:#1a1a1a">¡Pago registrado!</h2>' +
                    '<p style="color:#666;margin-bottom:4px">Pedido <strong>#' + idPedido + '</strong></p>' +
                    '<p style="color:#666;margin-bottom:4px">Total: <strong>' + fmtNum(total) + '</strong></p>' +
                    '<p style="color:#666;margin-bottom:20px">Método: <strong>' + (metodosLabel[metodo] || metodo) + '</strong></p>' +
                    '<p style="font-size:13px;color:#999;margin-bottom:24px">El administrador procesará tu pedido pronto.</p>' +
                    '<div style="display:flex;gap:10px;justify-content:center;flex-wrap:wrap">' +
                        '<a href="' + ctx2 + '/vistas/perfil.jsp" style="background:#1a1a1a;color:#fff;padding:12px 24px;border-radius:6px;font-size:13px;letter-spacing:1px;font-weight:600;text-decoration:none">VER MIS PEDIDOS</a>' +
                        '<button onclick="document.getElementById(\'order-confirm-modal\').remove()" style="background:#fff;color:#1a1a1a;border:1px solid #ccc;padding:12px 24px;border-radius:6px;font-size:13px;cursor:pointer;font-weight:600">SEGUIR COMPRANDO</button>' +
                    '</div>' +
                '</div>';
            document.body.appendChild(conf);
            conf.addEventListener('click', function(e) { if (e.target === conf) conf.remove(); });
        })
        .catch(function() {
            if (btn) { btn.disabled = false; btn.textContent = 'CONFIRMAR PAGO'; }
            if (errEl) { errEl.textContent = 'Error de conexión. Intenta de nuevo.'; errEl.style.display = 'block'; }
        });
    };

    // ─── Init ─────────────────────────────────────────────────────────────────
    function init() {
        if (!document.body.hasAttribute('data-no-cart')) {
            injectCartHTML();
            wrapShoppingIcon();
        }
        updateBadge();
        hookAddToCartButtons();

        // Event listeners
        document.addEventListener('click', e => {
            // Open cart
            if (e.target.closest('#cartToggle')) {
                e.preventDefault();
                openCart();
                return;
            }
            // Close cart
            if (e.target.closest('#cartClose') || e.target.closest('#cartOverlay')) {
                closeCart();
                return;
            }
        });

        document.getElementById('cartBody')?.addEventListener('click', handleCartBodyClick);
        document.getElementById('cartCheckout')?.addEventListener('click', handleCheckout);

        // Keyboard close
        document.addEventListener('keydown', e => {
            if (e.key === 'Escape') closeCart();
        });

        initSearch();
    }

    // ─── Search ───────────────────────────────────────────────────────────────
    var _allProducts = null;

    function getCtx() {
        var p = window.location.pathname.split('/');
        return '/' + p[1];
    }

    function loadAllProducts(cb) {
        if (_allProducts) { cb(_allProducts); return; }
        fetch(getCtx() + '/SvProductos', { credentials: 'same-origin' })
            .then(function(r) { return r.json(); })
            .then(function(data) {
                _allProducts = Array.isArray(data) ? data.filter(function(p) { return p.activo; }) : [];
                cb(_allProducts);
            })
            .catch(function() { cb([]); });
    }

    function initSearch() {
        var input = document.getElementById('search-input');
        var results = document.getElementById('search-results');
        if (!input || !results) return;

        input.addEventListener('input', function() {
            var q = input.value.trim().toLowerCase();
            if (q.length < 2) {
                results.innerHTML = '';
                results.classList.remove('visible');
                return;
            }
            loadAllProducts(function(productos) {
                var found = productos.filter(function(p) {
                    return (p.nombre && p.nombre.toLowerCase().includes(q)) ||
                           (p.marca  && p.marca.toLowerCase().includes(q))  ||
                           (p.descripcion && p.descripcion.toLowerCase().includes(q));
                });

                if (found.length === 0) {
                    results.innerHTML = '<div class="search-no-results">Sin resultados para "' + input.value.trim() + '"</div>';
                    results.classList.add('visible');
                    return;
                }

                var ctx = getCtx();
                var isVistas = window.location.pathname.includes('/vistas/');
                var base = isVistas ? 'detalle.jsp' : 'vistas/detalle.jsp';

                results.innerHTML = found.slice(0, 8).map(function(p) {
                    var precio = parseFloat(p.precio) || 0;
                    var precioStr = precio.toLocaleString('es-CO') + ' COP';
                    var img = (p.imagenUrl && p.imagenUrl.trim() !== '')
                        ? p.imagenUrl
                        : ctx + '/assets/imagenes/Imagen de la losion.webp';
                    var href = base + '?nombre=' + encodeURIComponent(p.nombre);
                    return '<a class="search-result-item" href="' + href + '">' +
                        '<img src="' + img + '" alt="' + p.nombre + '" onerror="this.style.display=\'none\'">' +
                        '<div class="search-result-item__info">' +
                            '<span class="search-result-item__name">' + p.nombre + '</span>' +
                            '<span class="search-result-item__brand">' + (p.marca || '') + '</span>' +
                        '</div>' +
                        '<span class="search-result-item__price">' + precioStr + '</span>' +
                        '</a>';
                }).join('');
                results.classList.add('visible');
            });
        });

        input.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                results.innerHTML = '';
                results.classList.remove('visible');
                input.value = '';
            }
        });
    }

    // Run after DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
