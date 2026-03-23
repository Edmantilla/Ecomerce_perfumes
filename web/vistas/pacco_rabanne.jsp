<%-- ==========================================================================
     pacco_rabanne.jsp — Página de catálogo de la marca PACO RABANNE.

     Estructura idéntica a cartas.jsp (Xerjoff):
     - Hero con imagen y descripción de la marca.
     - Cards dinámicas cargadas desde SvProductos filtrando por marca
       "Paco Rabanne".
     - Cada card enlaza a detalle.jsp?nombre=...
     - Incluye _navbar.jsp, _footer.jsp, cart.js.
     ========================================================================== --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../assets/estilos/style.css">
    <link rel="stylesheet" href="../assets/estilos/cart.css">
    <title>Cartas</title>
</head>

<body>

    <%-- Navbar compartido --%>
    <%@ include file="_navbar.jsp" %>

    <%-- Contenido Principal: Hero de marca + cards de productos --%>
    <main class="main-losion">

        <section class="section-losion">
            <h2 class="section-losion__title">PACCO RABANNE</h2>
            <img class="section-losion__img" src="../assets/imagenes/PACCO RABANNE.jpg" alt="Imagen de la locion">
            <div class="section-losion__description">
                <h2 class="section-losion__description__title">Paco Rabanne es un icono de la moda y perfumería que
                    combina creatividad audaz con estilo vanguardista y sofisticación atemporal.</h2>
                <p class="section-losion__description__paragraph">Paco Rabanne es una casa española reconocida por su
                    enfoque innovador y rompedor en moda y fragancias. Desde sus diseños metálicos y futuristas hasta
                    sus perfumes icónicos como 1 Million y Lady Million, la marca se distingue por fusionar creatividad,
                    modernidad y elegancia, ofreciendo productos que reflejan personalidad, lujo y un espíritu audaz que
                    trasciende generaciones.</p>
            </div>
        </section>

        <section class="cards-lociones" id="marca-cards">
        </section>
    </main>

    <%-- Footer compartido --%>
    <%@ include file="_footer.jsp" %>

    <%-- cart.js para carrito y búsqueda en navbar --%>
    <script src="../assets/scripts/cart.js"></script>

    <%-- Script de carga dinámica: productos de Paco Rabanne desde SvProductos --%>
    <script>
    (function() {
        var MARCA = 'Paco Rabanne'; // Nombre exacto de la marca en BD
        var IMG_DEFAULT = '../assets/imagenes/PACCO RABANNE.jpg';
        var ctx = (function() { var p = window.location.pathname.split('/'); return '/' + p[1]; })();
        // Fetch productos activos y filtrar por marca
        fetch(ctx + '/SvProductos', { credentials: 'same-origin' })
            .then(function(r) { return r.json(); })
            .then(function(productos) {
                if (!Array.isArray(productos)) return;
                // Filtrar solo productos activos de esta marca
                var filtrados = productos.filter(function(p) {
                    return p.activo && p.marca && p.marca.toLowerCase() === MARCA.toLowerCase();
                });
                var section = document.getElementById('marca-cards');
                if (!section) return;
                if (filtrados.length === 0) {
                    section.innerHTML = '<p style="text-align:center;color:#888;padding:40px">No hay productos disponibles.</p>';
                    return;
                }
                filtrados.forEach(function(p) {
                    var precio = parseFloat(p.precio) || 0;
                    var precioStr = precio.toLocaleString('es-CO') + ' COP';
                    var img = (p.imagenUrl && p.imagenUrl.trim() !== '') ? p.imagenUrl : IMG_DEFAULT;
                    var art = document.createElement('article');
                    art.className = 'card';
                    art.innerHTML =
                        '<a href="detalle.jsp?nombre=' + encodeURIComponent(p.nombre) + '">' +
                        '<img class="card__img" src="' + img + '" alt="' + p.nombre + '" onerror="this.src=\''+IMG_DEFAULT+'\'"></a>' +
                        '<div class="card__content">' +
                        '<h2 class="card__title">' + p.nombre.toUpperCase() + '</h2>' +
                        '<h3 class="card__subtitle">Perfume</h3>' +
                        '<p class="card__description">' + (p.descripcion || '') + '</p>' +
                        '<p class="card__price">' + precioStr + '</p>' +
                        '</div>';
                    section.appendChild(art);
                });
            })
            .catch(function(e) { console.error('Error cargando productos Paco Rabanne:', e); });
    })();
    </script>
</body>

</html>









