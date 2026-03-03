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
            <h2 class="section-losion__title">CHANEL</h2>
            <img class="section-losion__img" src="../assets/imagenes/CHANEL.jpg" alt="Imagen de la locion">
            <div class="section-losion__description">
                <h2 class="section-losion__description__title">Desde la elegancia atemporal hasta la revolución
                    silenciosa de la moda femenina, Chanel representa el equilibrio perfecto entre sofisticación,
                    libertad y poder</h2>
                <p class="section-losion__description__paragraph">Chanel es una casa de moda francesa fundada en 1910
                    por Gabrielle “Coco” Chanel, reconocida mundialmente por transformar la manera en que las mujeres
                    vestían en el siglo XX. La marca se caracteriza por su estilo elegante, minimalista y refinado,
                    destacando el uso del color negro, el tweed, las perlas y su icónico logotipo de doble C
                    entrelazada.</p>
            </div>
        </section>

        <section class="cards-lociones" id="marca-cards">
        </section>
    </main>

    <!-- Pie de Página -->
    <footer>
        <div class="footer__section--newsletter">
            <h2 class="footer__title">Reciba un 10% de descuento en su próximo pedido superior a 300 cop al
                suscribirse al boletín informativo de andreylpz.</h2>
            <div class="footer__form-wrapper">
                <form action="">
                    <input type="text" name="correo_electronico" id="" placeholder="DIRECCION DE CORREO ELECTRONICO">
                    <button>INSCRIBIRSE</button>
                </form>
            </div>
        </div>
        <div class="footer__section">
            <h2 class="footer__title">SERVICIO AL CLIENTE</h2>
            <ul class="footer__list">
                <li class="footer__item"><a class="footer__link" href="#">Contactanos</a></li>
                <li class="footer__item"><a class="footer__link" href="#">Preguntas Frecuentes</a></li>
            </ul>
        </div>
        <div class="footer__section">
            <h2 class="footer__title">LEGAL</h2>
            <ul class="footer__list">
                <li class="footer__item"><a class="footer__link" href="#">Contactanos</a></li>
                <li class="footer__item"><a class="footer__link" href="#">Politica de cookies</a></li>
            </ul>
        </div>
        <div class="footer__section">
            <h2 class="footer__title">ELECCION DE PAIS</h2>
            <ul class="footer__list">
                <li class="footer__item"><a class="footer__link" href="Colombia.jsp">Colombia</a></li>
                <li class="footer__item"><a class="footer__link" href="#">Estados unidos</a></li>
                <li class="footer__item"><a class="footer__link" href="#">Ecuador</a></li>
            </ul>
        </div>
    </footer>

    <script src="../assets/scripts/cart.js"></script>
    <script>
    (function() {
        var MARCA = 'Chanel';
        var IMG_DEFAULT = '../assets/imagenes/CHANEL.jpg';
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
            .catch(function(e) { console.error('Error cargando productos Chanel:', e); });
    })();
    </script>
</body>

</html>









