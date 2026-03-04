<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Página de catálogo de productos (Cartas/Tarjetas) -->
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

    <%@ include file="_navbar.jsp" %>

    <!-- Contenido Principal: Catálogo de Lociones -->
    <main class="main-losion">

        <section class="section-losion">
            <h2 class="section-losion__title">XERJOFF</h2>
            <img class="section-losion__img" src="../assets/imagenes/Imagen de la losion.webp" alt="Imagen de la locion">
            <div class="section-losion__description">
                <h2 class="section-losion__description__title">NATURE PROVIDES THE MATERIALS, THE HUMAN HAND TRANSFORMS
                    THEM INTO SOMETHING NEW.</h2>
                <p class="section-losion__description__paragraph">The 17/17 Stone Label collection is a union of natural
                    beauty and human artistry. Each perfume is an expression of creativity, housed in a flacon adorned
                    with a hand-cut, semi-precious stone. A work of art within a work of art. Inspired by the display at
                    London’s Natural History Museum, each stone holds its own symbolic value, representing mankind’s
                    innate ability to find meaning and inspiration in nature.</p>
            </div>
        </section>

        <section class="cards-lociones" id="marca-cards">
        </section>
    </main>

    <script>
    (function() {
        var MARCA = 'Xerjoff';
        var IMG_DEFAULT = '../assets/imagenes/Imagen de la losion.webp';
        var ctx = (function() { var p = window.location.pathname.split('/'); return '/' + p[1]; })();
        fetch(ctx + '/SvProductos', { credentials: 'same-origin' })
            .then(function(r) { return r.json(); })
            .then(function(productos) {
                if (!Array.isArray(productos)) return;
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
            .catch(function(e) { console.error('Error cargando productos Xerjoff:', e); });
    })();
    </script>

    <%@ include file="_footer.jsp" %>

    <script src="../assets/scripts/cart.js"></script>
</body>

</html>









