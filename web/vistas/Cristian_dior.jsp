<%-- ==========================================================================
     Cristian_dior.jsp — Página de catálogo de la marca CHRISTIAN DIOR.

     Estructura idéntica a cartas.jsp (Xerjoff):
     - Hero con imagen y descripción de la marca.
     - Cards dinámicas cargadas desde SvProductos filtrando por marca "Dior".
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
    <title>Chanel</title>
</head>

<body>

    <%-- Navbar compartido --%>
    <%@ include file="_navbar.jsp" %>

    <%-- Contenido Principal: Hero de marca + cards de productos --%>
    <main class="main-losion">

        <section class="section-losion">
            <h2 class="section-losion__title">CRISTIAN DIOR</h2>
            <img class="section-losion__img" src="../assets/imagenes/christian-dior-logo-pattern-kzgcxoozpsciriye.jpg"
                alt="Imagen de la locion">
            <div class="section-losion__description">
                <h2 class="section-losion__description__title">El Universo Sublime de la Perfumería de Christian Dior
                </h2>
                <p class="section-losion__description__paragraph">La perfumería de Christian Dior es una expresión
                    refinada de elegancia, sofisticación y arte olfativo. Desde su fundación, la casa Dior ha
                    transformado cada fragancia en una obra maestra que captura emociones, recuerdos y estilos de vida.
                    Cada perfume está diseñado con una meticulosa selección de ingredientes nobles, combinando tradición
                    artesanal francesa con innovación contemporánea</p>
            </div>
        </section>

        <section class="cards-lociones" id="marca-cards">
        </section>
    </main>

    <%-- Footer compartido --%>
    <%@ include file="_footer.jsp" %>

    <%-- cart.js para carrito y búsqueda en navbar --%>
    <script src="../assets/scripts/cart.js"></script>

    <%-- Script de carga dinámica: productos de Dior desde SvProductos --%>
    <script>
    (function() {
        var MARCA = 'Dior'; // Nombre exacto de la marca en BD
        var p = window.location.pathname.split('/');
        var ctx = '/' + p[1];
        // Fetch productos activos y filtrar por marca
        fetch(ctx + '/SvProductos', { credentials: 'same-origin' })
            .then(function(r) { return r.json(); })
            .then(function(productos) {
                if (!Array.isArray(productos)) return;
                // Filtrar solo productos activos de esta marca
                var filtrados = productos.filter(function(p) {
                    return p.marca && p.marca.toLowerCase() === MARCA.toLowerCase() && p.activo;
                });
                if (filtrados.length === 0) return;
                var section = document.querySelector('.cards-lociones');
                if (!section) return;
                filtrados.forEach(function(p) {
                    var precio = parseFloat(p.precio) || 0;
                    var precioStr = precio.toLocaleString('es-CO') + ' COP';
                    var img = p.imagenUrl ? p.imagenUrl : '../assets/imagenes/christian-dior-logo-pattern-kzgcxoozpsciriye.jpg';
                    var art = document.createElement('article');
                    art.className = 'card';
                    art.innerHTML =
                        '<a href="detalle.jsp?nombre=' + encodeURIComponent(p.nombre) + '">' +
                        '<img class="card__img" src="' + img + '" alt="' + p.nombre + '"></a>' +
                        '<div class="card__content">' +
                        '<h2 class="card__title">' + p.nombre.toUpperCase() + '</h2>' +
                        '<h3 class="card__subtitle">Perfume</h3>' +
                        '<p class="card__description">' + (p.descripcion || '') + '</p>' +
                        '<p class="card__price">' + precioStr + '</p>' +
                        '</div>';
                    section.appendChild(art);
                });
            })
            .catch(function(e) { console.error('Error cargando productos dinámicos:', e); });
    })();
    </script>
</body>

</html>









