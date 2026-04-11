/* ===========================
   CART SYSTEM - ANDREYLPZ
   Sistema completo de carrito de compras del lado del cliente.
   Maneja: persistencia en localStorage, badge contador en el ícono,
   panel lateral deslizable, controles de cantidad (+/−/input),
   eliminación de ítems, checkout vía SvCompra, flujo de pago vía SvPagos,
   modales de confirmación/error/login requerido, y búsqueda en tiempo real
   de productos desde el navbar vía SvProductos.
   Se ejecuta como IIFE para no contaminar el scope global.
   =========================== */

(function () {
    'use strict';

    // ─── State ───────────────────────────────────────────────────────────────
    // Clave de localStorage donde se persiste el carrito como JSON array
    const STORAGE_KEY = 'andreylpz_cart';

    /** Lee el carrito desde localStorage. Retorna [] si está vacío o corrupto. */
    function getCart() {
        try {
            return JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];
        } catch {
            return [];
        }
    }

    /** Guarda el array del carrito en localStorage como JSON string. */
    function saveCart(cart) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(cart));
    }

    // ─── Cart HTML Injection ──────────────────────────────────────────────────
    /**
     * Inyecta el HTML del panel lateral del carrito y su overlay oscuro al final del <body>.
     * Se ejecuta una sola vez en init(). Crea: overlay (#cartOverlay),
     * panel aside (#cartPanel), header con título y botón cerrar,
     * body donde se renderizan los ítems, y footer con subtotal + botón checkout.
     */
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
    /**
     * Actualiza el número que aparece sobre el ícono del carrito (badge).
     * Suma las cantidades de todos los ítems y muestra/oculta el badge
     * según si hay productos o no.
     */
    function updateBadge() {
        const cart = getCart();
        const total = cart.reduce((sum, item) => sum + item.qty, 0);

        // Busca todos los badges (puede haber varios por distintos navbars en distintas páginas)
        document.querySelectorAll('.cart-badge__count').forEach(el => {
            el.textContent = total;
            el.classList.toggle('visible', total > 0);
        });
    }

    // ─── Wrap the shopping icon with the badge wrapper ────────────────────────
    /**
     * Transforma el enlace del ícono de carrito (<a href="#compras">) en un
     * botón que abre el panel lateral. Le agrega la clase cart-badge,
     * cambia el href a "#" para prevenir navegación, y añade un <span>
     * para el contador numérico (badge).
     */
    function wrapShoppingIcon() {
        // Busca todos los enlaces al ícono del carrito en el navbar
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
    /** Formatea un número como precio colombiano: "450.000 COP" */
    function formatPrice(value) {
        return value.toLocaleString('es-CO') + ' COP';
    }

    /** Convierte un string de precio ("450.000 COP") a entero (450000) */
    function parsePrice(str) {
        return parseInt(str.replace(/\./g, '').replace(/[^0-9]/g, ''), 10) || 0;
    }

    /**
     * Renderiza los ítems del carrito dentro del panel lateral (#cartBody).
     * Si el carrito está vacío muestra un mensaje centrado.
     * Para cada ítem genera un card con: imagen, marca, nombre, precio de línea,
     * controles de cantidad (−/input/+) y botón eliminar (✕).
     * Calcula y muestra el subtotal acumulado.
     */
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
                        <input class="cart-item__qty-input" type="number" min="1" value="${item.qty}" data-action="setqty" data-index="${index}" style="width:46px;text-align:center;border:1px solid #ddd;border-radius:4px;padding:2px 4px;font-size:14px;font-weight:600;-moz-appearance:textfield">
                        <button class="cart-item__qty-btn" data-action="increase" data-index="${index}">+</button>
                    </div>
                </div>
                <button class="cart-item__remove" data-action="remove" data-index="${index}" aria-label="Eliminar ${item.name}">✕</button>
            </div>`;
        }).join('');

        if (subtotalEl) subtotalEl.textContent = formatPrice(subtotal);
    }

    // ─── Panel Open/Close ─────────────────────────────────────────────────────
    /** Abre el panel lateral del carrito y el overlay. Bloquea el scroll del body. */
    function openCart() {
        document.getElementById('cartPanel')?.classList.add('open');
        document.getElementById('cartOverlay')?.classList.add('open');
        document.body.style.overflow = 'hidden';
        renderCart();
    }

    /** Cierra el panel lateral y el overlay. Restaura el scroll del body. */
    function closeCart() {
        document.getElementById('cartPanel')?.classList.remove('open');
        document.getElementById('cartOverlay')?.classList.remove('open');
        document.body.style.overflow = '';
    }

    // ─── Add Product ──────────────────────────────────────────────────────────
    /**
     * Agrega un producto al carrito. Si ya existe (mismo id), incrementa qty.
     * Si es nuevo, lo agrega con qty=1. Guarda, actualiza badge, renderiza
     * y abre el panel lateral automáticamente.
     * @param {Object} product - {id, name, brand, price, image}
     */
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
    /**
     * Busca el botón "AGREGAR AL CARRITO" en detalle.jsp y le añade
     * un listener de click que extrae los datos del producto de la página
     * (nombre, precio, imagen, marca) y llama a addProduct().
     * Muestra feedback visual ("✓ AÑADIDO") durante 2 segundos.
     */
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
    /**
     * Delegación de eventos para los botones dentro del panel del carrito.
     * Detecta data-action en el elemento clickeado: remove, increase, decrease.
     * Actualiza el array del carrito en localStorage y re-renderiza.
     */
    function handleCartBodyClick(e) {
        const btn = e.target.closest('[data-action]');
        if (!btn || btn.tagName === 'INPUT') return;

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

    /**
     * Maneja el cambio manual del input numérico de cantidad.
     * Si el valor es ≤ 0 o NaN, elimina el ítem del carrito.
     */
    function handleCartBodyChange(e) {
        const input = e.target.closest('input[data-action="setqty"]');
        if (!input) return;
        const index = parseInt(input.dataset.index, 10);
        const val = parseInt(input.value, 10);
        const cart = getCart();
        if (isNaN(val) || val <= 0) {
            cart.splice(index, 1);
        } else {
            cart[index].qty = val;
        }
        saveCart(cart);
        updateBadge();
        renderCart();
    }

    // ─── Checkout Button ──────────────────────────────────────────────────────
    /**
     * handleCheckout — Se ejecuta cuando el usuario hace clic en "Finalizar Compra".
     * Flujo completo:
     *   1. Valida que el carrito no esté vacío.
     *   2. Deshabilita el botón para evitar doble envío.
     *   3. Detecta el contexto de la aplicación (ruta base de Tomcat).
     *   4. Serializa cada ítem del carrito como parámetros de formulario.
     *   5. Envía POST a SvCompra con los datos del carrito.
     *   6. Maneja la respuesta: sesión expirada, error de negocio o compra exitosa.
     */
    function handleCheckout() {

        // Lee el array del carrito desde localStorage.
        // getCart() retorna [] si está vacío o si el JSON está corrupto.
        const cart = getCart();

        // Si el carrito no tiene ningún ítem, muestra un modal de error y detiene la ejecución.
        // cart.length === 0 significa que el array está vacío (no hay productos agregados).
        if (cart.length === 0) {
            showCartError('Tu carrito está vacío. Agrega productos antes de continuar.');
            return; // sale de la función aquí; no envía nada al servidor
        }

        // Busca el botón "Finalizar Compra" en el DOM por su id.
        const btn = document.getElementById('cartCheckout');

        // Si el botón existe, lo deshabilita y cambia su texto a "Procesando...".
        // Esto evita que el usuario haga clic varias veces mientras espera la respuesta del servidor,
        // lo que crearía pedidos duplicados en la base de datos.
        if (btn) { btn.disabled = true; btn.textContent = 'Procesando...'; }

        // IIFE (función que se define y se ejecuta inmediatamente) para calcular el contexto de la app.
        // window.location.pathname en Tomcat es algo como "/Proyecto/vistas/Chanel.jsp".
        // .split('/') lo convierte en ["", "Proyecto", "vistas", "Chanel.jsp"].
        // p[1] toma el segundo elemento: "Proyecto" (el nombre del WAR desplegado).
        // El resultado es "/Proyecto", que es la ruta base para construir las URLs de los servlets.
        // Esto evita hardcodear el nombre del proyecto en las URLs.
        const ctx = (function () {
            const p = window.location.pathname.split('/'); // divide la ruta por "/"
            return '/' + p[1];                             // retorna "/Proyecto" (o el nombre del WAR)
        })();

        // URLSearchParams construye la cadena de parámetros tipo formulario (application/x-www-form-urlencoded).
        // El servidor (SvCompra.doPost) los lee con request.getParameter("nombre").
        var params = new URLSearchParams();

        // Agrega el número total de ítems del carrito.
        // SvCompra lo usa para saber cuántos item_name_N / item_price_N / item_qty_N debe leer.
        // Ejemplo: si hay 2 productos → itemCount=2
        params.append('itemCount', cart.length);

        // Recorre cada ítem del carrito con su índice i (0, 1, 2, ...).
        // Para cada ítem agrega 4 parámetros numerados con el sufijo _i.
        cart.forEach(function(item, i) {

            // Nombre del producto. El || '' garantiza que si item.name es undefined, se envía cadena vacía.
            // Ejemplo: item_name_0=Chanel+N5
            params.append('item_name_' + i,  item.name  || '');

            // Marca del producto. SvCompra la usa para buscar o crear la marca en BD.
            // Ejemplo: item_brand_0=Chanel
            params.append('item_brand_' + i, item.brand || '');

            // Precio unitario del producto al momento de agregarlo al carrito.
            // Se guarda el precio del momento para que no cambie si el admin lo edita después.
            // Ejemplo: item_price_0=250000
            params.append('item_price_' + i, item.price || 0);

            // Cantidad de unidades de este producto.
            // El || 1 garantiza mínimo 1 unidad si qty fuera undefined o 0.
            // Ejemplo: item_qty_0=2
            params.append('item_qty_' + i,   item.qty   || 1);
        });
        // Resultado final de params para un carrito de 2 ítems:
        // "itemCount=2&item_name_0=Chanel+N5&item_brand_0=Chanel&item_price_0=250000&item_qty_0=2
        //  &item_name_1=Dior+Sauvage&item_brand_1=Dior&item_price_1=180000&item_qty_1=1"

        // Envía la petición HTTP POST al servlet SvCompra.
        fetch(ctx + '/SvCompra', {          // URL completa: "/Proyecto/SvCompra"
            method: 'POST',                 // método HTTP POST (los datos van en el body, no en la URL)
            credentials: 'same-origin',     // incluye automáticamente la cookie JSESSIONID para que el servidor
                                            // pueda identificar al usuario a través de request.getSession()
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, // indica al servidor que el body
                                            // es un formulario (no JSON), para que request.getParameter() funcione
            body: params.toString()         // convierte URLSearchParams a string: "itemCount=2&item_name_0=..."
        })

        // PRIMER .then(): convierte la respuesta HTTP a texto y luego intenta parsearla como JSON.
        // Se usa r.text() en lugar de r.json() directamente para poder manejar respuestas
        // que no sean JSON válido (ej. errores HTML de Tomcat o páginas de error del servidor).
        .then(function(r) {
            return r.text().then(function(text) { // lee el cuerpo de la respuesta como texto plano
                try {
                    // Intenta convertir el texto a objeto JavaScript.
                    // Si SvCompra respondió correctamente, text = '{"ok":true,"idPedido":42,"total":680000}'
                    // Empaqueta el resultado con el código HTTP (status) para usarlo en el siguiente .then()
                    return { status: r.status, data: JSON.parse(text) };
                } catch(e) {
                    // Si el texto no es JSON válido (ej. Tomcat devolvió una página HTML de error 500),
                    // JSON.parse lanza SyntaxError y se captura aquí.
                    // Se devuelve un objeto de error genérico para que el siguiente .then() lo maneje.
                    return { status: r.status, data: { error: 'Ocurrió un problema al procesar tu compra. Intenta de nuevo.' } };
                }
            });
        })

        // SEGUNDO .then(): evalúa el resultado del servidor y actúa según el caso.
        // res = { status: 200|401|500, data: { ok:true, idPedido:42 } | { error:"..." } }
        .then(function(res) {
            var data = res.data; // el objeto JSON parseado de la respuesta del servidor

            // CASO 1: el usuario no tiene sesión activa (HTTP 401) o el mensaje menciona "sesión".
            // Ocurre cuando la sesión expiró mientras el usuario tenía el carrito abierto,
            // o cuando accede sin haber iniciado sesión.
            if (res.status === 401 || (data.error && data.error.includes('sesión'))) {
                if (btn) { btn.disabled = false; btn.textContent = 'Finalizar Compra'; } // reactiva el botón
                closeCart();                    // cierra el panel lateral del carrito
                showLoginRequiredModal(ctx);    // muestra modal con botones "Iniciar Sesión" / "Crear Cuenta"
                return;                         // sale; no procesa más
            }

            // CASO 2: el servidor devolvió un error de negocio (stock insuficiente, carrito vacío, etc.).
            // data.error contiene el mensaje que SvCompra escribió en el JSON de respuesta.
            // Ejemplo: { "error": "Stock insuficiente para Chanel N5" }
            if (data.error) {
                showCartError(data.error);                                           // muestra el error en modal
                if (btn) { btn.disabled = false; btn.textContent = 'Finalizar Compra'; } // reactiva el botón
                return;                         // sale; el carrito queda intacto para que el usuario lo corrija
            }

            // CASO 3: compra exitosa. data = { ok:true, idPedido:42, total:680000 }

            saveCart([]);       // vacía el carrito en localStorage (guarda array vacío)
            updateBadge();      // actualiza el contador del ícono del carrito a 0 en el navbar
            closeCart();        // cierra el panel lateral del carrito

            // Muestra el modal de confirmación de pedido con el ID y total del pedido recién creado.
            // Desde este modal el usuario puede ver su número de pedido y proceder al pago.
            showOrderConfirmation(data.idPedido, data.total);
            // data.idPedido = 42 (el id_pedido generado por MySQL en la tabla pedido)
            // data.total    = 680000 (la suma calculada por SvCompra)
        })

        // .catch(): captura errores de red (sin internet, servidor caído, timeout).
        // No captura errores HTTP (400, 401, 500); esos los maneja el .then() de arriba.
        // Solo se ejecuta si fetch() falló completamente antes de recibir respuesta.
        .catch(function() {
            showCartError('No se pudo procesar tu compra. Verifica tu conexión e intenta de nuevo.');
            if (btn) { btn.disabled = false; btn.textContent = 'Finalizar Compra'; } // reactiva el botón
        });
    }

    /**
     * Modal que se muestra cuando el usuario intenta comprar sin sesión.
     * Ofrece botones para "INICIAR SESIÓN" (perfil.jsp) o "CREAR CUENTA" (registro.jsp).
     */
    function showLoginRequiredModal(ctx) {
        var existing = document.getElementById('login-required-modal');
        if (existing) existing.remove();
        var modal = document.createElement('div');
        modal.id = 'login-required-modal';
        modal.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,0.55);z-index:9999;display:flex;align-items:center;justify-content:center;padding:16px';
        modal.innerHTML =
            '<div style="background:#fff;border-radius:12px;padding:40px 32px;max-width:400px;width:100%;text-align:center;box-shadow:0 8px 40px rgba(0,0,0,.25)">' +
                '<div style="font-size:48px;margin-bottom:16px">🔒</div>' +
                '<h2 style="font-size:18px;font-weight:700;color:#1a1a1a;margin:0 0 10px;letter-spacing:0.5px">Inicia sesión para comprar</h2>' +
                '<p style="color:#666;font-size:14px;margin:0 0 24px;line-height:1.5">Necesitas tener una cuenta activa para realizar tu pedido. Es rápido y gratuito.</p>' +
                '<div style="display:flex;gap:10px;justify-content:center;flex-wrap:wrap">' +
                    '<a href="' + ctx + '/vistas/perfil.jsp" style="background:#1a1a1a;color:#fff;padding:12px 24px;border-radius:6px;font-size:13px;font-weight:700;letter-spacing:1px;text-decoration:none">INICIAR SESIÓN</a>' +
                    '<a href="' + ctx + '/vistas/registro.jsp" style="background:#fff;color:#1a1a1a;border:1px solid #ccc;padding:12px 24px;border-radius:6px;font-size:13px;font-weight:600;text-decoration:none">CREAR CUENTA</a>' +
                '</div>' +
                '<button onclick="document.getElementById(\'login-required-modal\').remove()" style="margin-top:16px;background:none;border:none;color:#999;font-size:13px;cursor:pointer;text-decoration:underline">Cancelar</button>' +
            '</div>';
        document.body.appendChild(modal);
        modal.addEventListener('click', function(e) { if (e.target === modal) modal.remove(); });
    }

    /**
     * Modal genérico de error del carrito. Muestra un ícono de advertencia
     * y el mensaje de error recibido del servidor o generado localmente.
     */
    function showCartError(msg) {
        var existing = document.getElementById('cart-error-modal');
        if (existing) existing.remove();
        var modal = document.createElement('div');
        modal.id = 'cart-error-modal';
        modal.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,0.55);z-index:9999;display:flex;align-items:center;justify-content:center;padding:16px';
        modal.innerHTML =
            '<div style="background:#fff;border-radius:12px;padding:36px 32px;max-width:380px;width:100%;text-align:center;box-shadow:0 8px 40px rgba(0,0,0,.25)">' +
                '<div style="font-size:44px;margin-bottom:14px">⚠️</div>' +
                '<h2 style="font-size:17px;font-weight:700;color:#1a1a1a;margin:0 0 10px">Hubo un problema</h2>' +
                '<p style="color:#555;font-size:14px;margin:0 0 24px;line-height:1.5">' + msg + '</p>' +
                '<button onclick="document.getElementById(\'cart-error-modal\').remove()" style="background:#1a1a1a;color:#fff;border:none;padding:12px 28px;border-radius:6px;font-size:13px;font-weight:700;letter-spacing:1px;cursor:pointer">ENTENDIDO</button>' +
            '</div>';
        document.body.appendChild(modal);
        modal.addEventListener('click', function(e) { if (e.target === modal) modal.remove(); });
    }

    /**
     * Modal de selección de método de pago mostrado tras crear el pedido exitosamente.
     * Opciones: Tarjeta, Transferencia, Efectivo/Contraentrega.
     * Si es transferencia, muestra campo de referencia.
     * El botón "CONFIRMAR PAGO" llama a window.confirmarPago().
     */
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

    /**
     * Función global llamada desde el modal de pago.
     * Envía POST a SvPagos con: idPedido, metodo, monto, referencia.
     * Si el pago cubre el total, el servidor cambia el estado del pedido a PAGO.
     * Muestra modal de confirmación final con resumen del pedido.
     */
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
    /**
     * Función principal de inicialización. Ejecutada al cargar el DOM.
     * 1. Inyecta el HTML del panel lateral y wrappea el ícono del carrito.
     * 2. Actualiza el badge y hookea el botón "Agregar al carrito".
     * 3. Registra event listeners globales (abrir/cerrar panel, Escape).
     * 4. Inicializa la búsqueda en tiempo real del navbar.
     */
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
        document.getElementById('cartBody')?.addEventListener('change', handleCartBodyChange);
        document.getElementById('cartCheckout')?.addEventListener('click', handleCheckout);

        // Keyboard close
        document.addEventListener('keydown', e => {
            if (e.key === 'Escape') closeCart();
        });

        initSearch();
    }

    // ─── Search ───────────────────────────────────────────────────────────────
    // Caché local de productos para evitar múltiples requests al servidor
    var _allProducts = null;

    /** Obtiene el context path de la app (ej: "/Proyecto") desde la URL actual. */
    function getCtx() {
        var p = window.location.pathname.split('/');
        return '/' + p[1];
    }

    /**
     * Carga todos los productos activos desde SvProductos (GET) y los cachea
     * en _allProducts. En llamadas posteriores retorna el caché sin fetch.
     * @param {Function} cb - callback que recibe el array de productos
     */
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

    /**
     * Inicializa la búsqueda en tiempo real del navbar.
     * Se activa con el input #search-input y muestra resultados en #search-results.
     * Filtra productos por nombre, marca o descripción (mínimo 2 caracteres).
     * Muestra hasta 8 resultados con enlace a detalle.jsp?nombre=...
     * Escape limpia el input y cierra los resultados.
     */
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

    // Ejecutar init() cuando el DOM esté listo (o inmediatamente si ya lo está)
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
