<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Página de catálogo de productos (Cartas/Tarjetas) -->
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

    <%@ include file="_navbar.jsp" %>

    <!-- Contenido Principal: Catálogo de Lociones -->
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

    <%@ include file="_footer.jsp" %>

    <script src="../assets/scripts/cart.js"></script>
    <script>
    (function() {
        var MARCA = 'Dior';
        var p = window.location.pathname.split('/');
        var ctx = '/' + p[1];
        fetch(ctx + '/SvProductos', { credentials: 'same-origin' })
            .then(function(r) { return r.json(); })
            .then(function(productos) {
                if (!Array.isArray(productos)) return;
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









