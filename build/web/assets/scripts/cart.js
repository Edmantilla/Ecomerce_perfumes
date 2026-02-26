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
        alert('¡Gracias por tu compra! Tu pedido ha sido recibido. 🛍️');
        saveCart([]);
        updateBadge();
        renderCart();
        closeCart();
    }

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
    }

    // Run after DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
