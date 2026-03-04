<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../assets/estilos/style.css">
    <link rel="stylesheet" href="../assets/estilos/cart.css">
    <title>Ejemplo</title>
</head>
<body>
    <%@ include file="_navbar.jsp" %>
    <main class="main-losion">
        <section class="section-losion">
            <h2 class="section-losion__title">EJEMPLO</h2>
            <img class="section-losion__img" src="../assets/imagenes/Imagen de la losion.webp" alt="Ejemplo">
            <div class="section-losion__description">
                <h2 class="section-losion__description__title">Ejemplo</h2>
            </div>
        </section>
        <section class="cards-lociones" id="marca-cards">
        </section>
    </main>
    <script>
    (function() {
        var MARCA_NOMBRE = 'ejemplo';
        var ctx = (function() { var p = window.location.pathname.split('/'); return '/' + p[1]; })();
        function fmt(n) { return parseFloat(n).toLocaleString('es-CO') + ' COP'; }
        fetch(ctx + '/SvProductos', { credentials: 'same-origin' })
            .then(function(r) { return r.json(); })
            .then(function(productos) {
                if (!Array.isArray(productos)) return;
                var filtrados = productos.filter(function(p) {
                    return p.activo && p.marca && p.marca.toUpperCase() === MARCA_NOMBRE.toUpperCase();
                });
                if (filtrados.length === 0) return;
                var section = document.getElementById('marca-cards');
                filtrados.forEach(function(p) {
                    var imgSrc = p.imagenUrl && p.imagenUrl.trim() !== ''
                        ? p.imagenUrl : ctx + '/assets/imagenes/Imagen de la losion.webp';
                    var art = document.createElement('article');
                    art.className = 'card';
                    art.innerHTML =
                        '<a href="detalle.jsp?nombre=' + encodeURIComponent(p.nombre) + '">' +
                            '<img class="card__img" src="' + imgSrc + '" alt="' + p.nombre + '">' +
                        '</a>' +
                        '<div class="card__content">' +
                            '<h2 class="card__title">' + p.nombre.toUpperCase() + '</h2>' +
                            '<h3 class="card__subtitle">Perfume</h3>' +
                            '<p class="card__description">' + (p.descripcion || '') + '</p>' +
                            '<p class="card__price">' + fmt(p.precio) + '</p>' +
                        '</div>';
                    section.appendChild(art);
                });
            })
            .catch(function() {});
    })();
    </script>
    <%@ include file="_footer.jsp" %>
    <script src="../assets/scripts/cart.js"></script>
</body>
</html>
