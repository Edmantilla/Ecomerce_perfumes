<%-- ==========================================================================
     detalle.jsp — Página de detalle de un producto individual.

     Funcionalidad:
     1. Recibe el parámetro ?nombre=... en la URL para identificar el producto.
     2. Hace fetch paralelo a SvProductos (lista de productos activos) y
        SvMarcas (para obtener la URL de la página de cada marca).
     3. Busca el producto por nombre (case-insensitive) y renderiza:
        - Imagen (con fallback a imagen por defecto si falla).
        - Nombre, marca (con enlace a su página), precio, descripción,
          categoría, stock.
     4. Botón "AGREGAR AL CARRITO" que:
        - Guarda en localStorage (clave 'andreylpz_cart').
        - Actualiza badge del carrito y abre el panel lateral.
        - Re-renderiza el contenido del panel con los items actualizados.
     5. Si no hay parámetro o el producto no existe, muestra error.

     Incluye: _navbar.jsp, _footer.jsp, cart.js.
     ========================================================================== --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../assets/estilos/style.css">
    <link rel="stylesheet" href="../assets/estilos/cart.css">
    <title id="page-title">Detalle de Producto - ANDREYLPZ</title>
</head>
<body>

    <%-- Navbar compartido --%>
    <%@ include file="_navbar.jsp" %>

    <main class="main-losion">
        <%-- Sección principal del detalle: loading, error, o contenido --%>
        <section class="section-losion-perfumes" id="detalle-section">
            <%-- Estado de carga: visible mientras se obtienen datos --%>
            <div id="detalle-loading" style="text-align:center;padding:60px;color:#888;font-size:1.1rem;">
                Cargando producto...
            </div>
            <%-- Estado de error: visible si el producto no existe --%>
            <div id="detalle-error" style="display:none;text-align:center;padding:60px;color:#c62828;font-size:1.1rem;">
                Producto no encontrado.
            </div>
            <%-- Contenido del producto: se muestra tras carga exitosa --%>
            <div id="detalle-contenido" style="display:none;">
                <div class="section-losion__contenedor">
                    <div class="section-losion__contenedor2">
                        <h2><a class="section-losion__titulo2" id="detalle-marca-link" href="#">–</a></h2>
                    </div>
                    <h2 class="section-losion__title" id="detalle-nombre">–</h2>
                </div>
                <div class="section-losion__divicion">
                    <div class="section-losion__divicion__img">
                        <img id="detalle-img" src="" alt="" style="max-width:100%;object-fit:contain">
                    </div>
                    <div class="section-losion__divicion__description">
                        <div class="section-losion__divicion__description__img"></div>
                        <div class="section-losion__divicion__description__precios">
                            <h2 class="section-losion__divicion__description__precios__mililitro">50 ML - 100 ML</h2>
                            <h2 class="section-losion__divicion__description__precios__precios" id="detalle-precio">–</h2>
                        </div>
                        <button class="section-losion__divicion__description__button" id="detalle-btn-carrito" data-cart-managed="true">AGREGAR AL CARRITO</button>
                        <p id="detalle-descripcion"></p>
                        <div class="section-losion__divicion__description__type">
                            <h2>TIPO</h2><p id="detalle-categoria">–</p>
                        </div>
                        <div class="section-losion__divicion__description__type">
                            <h2>MARCA</h2><p id="detalle-marca">–</p>
                        </div>
                        <div class="section-losion__divicion__description__type">
                            <h2>STOCK</h2><p id="detalle-stock">–</p>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </main>

    <%-- Footer compartido --%>
    <%@ include file="_footer.jsp" %>

    <%-- cart.js para el carrito y búsqueda en navbar --%>
    <script src="../assets/scripts/cart.js"></script>

    <%-- Script de carga dinámica del producto y lógica del botón "Agregar al carrito" --%>
    <script>
    (function () {
        // Obtener context path dinámicamente (ej: /Proyecto)
        var ctx = (function () {
            var p = window.location.pathname.split('/');
            return '/' + p[1];
        })();

        /** Extrae un parámetro de la query string */
        function getParam(name) {
            return new URLSearchParams(window.location.search).get(name);
        }

        // Leer el nombre del producto desde ?nombre=...
        var nombreParam = getParam('nombre');

        if (!nombreParam) {
            document.getElementById('detalle-loading').style.display = 'none';
            document.getElementById('detalle-error').style.display = 'block';
            return;
        }

        function parsePrice(str) {
            return parseInt(String(str).replace(/\./g, '').replace(/[^0-9]/g, ''), 10) || 0;
        }

        // Fetch paralelo: productos activos y marcas (para obtener URLs de páginas de marca)
        Promise.all([
            fetch(ctx + '/SvProductos', { credentials: 'same-origin' }).then(function(r) { return r.json(); }),
            fetch(ctx + '/SvMarcas',    { credentials: 'same-origin' }).then(function(r) { return r.json(); })
        ]).then(function (results) {
                var productos = results[0];
                var marcas    = results[1];

                // Mapa nombre_marca → pagina_url para enlazar la marca del producto
                var marcaUrls = {};
                if (Array.isArray(marcas)) {
                    marcas.forEach(function(m) {
                        if (m.nombre && m.pagina) marcaUrls[m.nombre.toLowerCase()] = m.pagina;
                    });
                }

                var prod = null;
                if (Array.isArray(productos)) {
                    prod = productos.find(function (p) {
                        return p.nombre && p.nombre.toLowerCase() === nombreParam.toLowerCase() && p.activo;
                    });
                }

                document.getElementById('detalle-loading').style.display = 'none';

                if (!prod) {
                    document.getElementById('detalle-error').style.display = 'block';
                    return;
                }

                document.title = prod.nombre + ' - ANDREYLPZ';

                document.getElementById('detalle-nombre').textContent = prod.nombre;

                var marcaUrl = (prod.marca && marcaUrls[prod.marca.toLowerCase()]) ? marcaUrls[prod.marca.toLowerCase()] : '#';
                var marcaLink = document.getElementById('detalle-marca-link');
                marcaLink.textContent = (prod.marca || '').toUpperCase();
                marcaLink.href = marcaUrl;

                var imgDefault = ctx + '/assets/imagenes/Imagen de la losion.webp';
                var imgSrc = (prod.imagenUrl && prod.imagenUrl.trim() !== '') ? prod.imagenUrl : imgDefault;
                var imgEl = document.getElementById('detalle-img');
                imgEl.src = imgSrc;
                imgEl.alt = prod.nombre;
                imgEl.onerror = function () { this.src = imgDefault; };

                var precio = parseFloat(prod.precio) || 0;
                document.getElementById('detalle-precio').textContent = precio.toLocaleString('es-CO') + ' COP';

                document.getElementById('detalle-descripcion').textContent = prod.descripcion || '';
                document.getElementById('detalle-categoria').textContent = prod.categoria || '–';
                document.getElementById('detalle-marca').textContent = prod.marca || '–';
                document.getElementById('detalle-stock').textContent = prod.stock > 0 ? prod.stock + ' unidades disponibles' : 'Sin stock';

                document.getElementById('detalle-contenido').style.display = 'block';

                var btn = document.getElementById('detalle-btn-carrito');
                if (prod.stock <= 0) {
                    btn.disabled = true;
                    btn.textContent = 'SIN STOCK';
                    btn.style.opacity = '0.5';
                    btn.style.cursor = 'not-allowed';
                    return;
                }

                // === Conectar botón "AGREGAR AL CARRITO" con localStorage ===
                var name  = prod.nombre;
                var brand = prod.marca || '';
                var price = precio;
                var image = imgSrc;
                var id    = (brand + '_' + name).toLowerCase().replace(/[^a-z0-9]/g, '_');

                btn.addEventListener('click', function () {
                    var STORAGE_KEY = 'andreylpz_cart'; // Clave de localStorage compartida con cart.js
                    var cart = [];
                    try { cart = JSON.parse(localStorage.getItem(STORAGE_KEY)) || []; } catch(e) {}
                    var existing = cart.find(function(item) { return item.id === id; });
                    if (existing) {
                        existing.qty += 1;
                    } else {
                        cart.push({ id: id, name: name, brand: brand, price: price, image: image, qty: 1 });
                    }
                    localStorage.setItem(STORAGE_KEY, JSON.stringify(cart));

                    // Actualizar badge del carrito en el navbar
                    document.querySelectorAll('.cart-badge__count').forEach(function(el) {
                        var total = cart.reduce(function(s, i) { return s + i.qty; }, 0);
                        el.textContent = total;
                        el.classList.toggle('visible', total > 0);
                    });

                    btn.classList.add('added');
                    btn.textContent = '\u2713 A\u00d1ADIDO';
                    setTimeout(function () {
                        btn.textContent = 'AGREGAR AL CARRITO';
                        btn.classList.remove('added');
                    }, 2000);

                    // Abrir panel lateral del carrito (elementos creados por cart.js)
                    var panel = document.getElementById('cartPanel');
                    var overlay = document.getElementById('cartOverlay');
                    if (panel) panel.classList.add('open');
                    if (overlay) overlay.classList.add('open');
                    document.body.style.overflow = 'hidden';

                    // Re-renderizar items dentro del panel lateral del carrito
                    var cartBody = document.getElementById('cartBody');
                    var subtotalEl = document.getElementById('cartSubtotal');
                    if (cartBody) {
                        var subtotal = 0;
                        cartBody.innerHTML = cart.map(function(item, index) {
                            var lineTotal = item.price * item.qty;
                            subtotal += lineTotal;
                            return '<div class="cart-item" data-index="' + index + '">' +
                                '<img class="cart-item__img" src="' + item.image + '" alt="' + item.name + '" onerror="this.style.display=\'none\'">' +
                                '<div class="cart-item__info">' +
                                '<p class="cart-item__brand">' + item.brand + '</p>' +
                                '<p class="cart-item__name">' + item.name + '</p>' +
                                '<p class="cart-item__price">' + lineTotal.toLocaleString('es-CO') + ' COP</p>' +
                                '<div class="cart-item__qty-controls">' +
                                '<button class="cart-item__qty-btn" data-action="decrease" data-index="' + index + '">\u2212</button>' +
                                '<input class="cart-item__qty-input" type="number" min="1" value="' + item.qty + '" data-action="setqty" data-index="' + index + '" style="width:46px;text-align:center;border:1px solid #ddd;border-radius:4px;padding:2px 4px;font-size:14px;font-weight:600;-moz-appearance:textfield">' +
                                '<button class="cart-item__qty-btn" data-action="increase" data-index="' + index + '">+</button>' +
                                '</div></div>' +
                                '<button class="cart-item__remove" data-action="remove" data-index="' + index + '">\u2715</button>' +
                                '</div>';
                        }).join('');
                        if (subtotalEl) subtotalEl.textContent = subtotal.toLocaleString('es-CO') + ' COP';
                    }
                });
            })
            .catch(function (e) {
                document.getElementById('detalle-loading').style.display = 'none';
                document.getElementById('detalle-error').style.display = 'block';
                console.error('Error cargando producto:', e);
            });
    })();
    </script>
</body>
</html>
