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

    <div class="discount">
        <h2 class="discount__title">RECIBA UN KIT DE MUESTRA LITRO POR LA COMPRA DE UNA LOSION</h2>
    </div>
    <!-- Encabezado y Barra de Navegación -->
    <header>
        <div class="navbar">
            <!-- Checkbox para el menú hamburguesa (Versión Móvil) -->
            <input type="checkbox" id="navbar-toggle" class="navbar__toggle">
            <label for="navbar-toggle" class="navbar__hamburger">
                <span class="hamburger-line"></span>
                <span class="hamburger-line"></span>
                <span class="hamburger-line"></span>
            </label>
            <ul class="navbar__list">
                <li class="navbar__item">
                    <a class="navbar__link" href="#">HOMBRES</a>
                    <div class="navbar__list-megamenu">

                        <div class="navbar__list-megamenu__list">
                            <img src="../assets/imagenes/yves saint laurent.webp" alt="">
                            <div class="navbar__list-megamenu__links">
                                <div class="navbar__list-megamenu__links--title">
                                    <h2>PERFUMES</h2>
                                </div>
                                <a href="cartas.jsp">Xerjoff</a>
                                <a href="pacco_rabanne.jsp">Paco Rabanne</a>
                            </div>

                            <div class="navbar__list-megamenu__list">
                                <div class="navbar__list-megamenu__links--perfiles">
                                    <h2>PERFILES DE FRAGANCIA</h2>
                                    <a href="#">Oriental</a>
                                    <a href="#">Floral</a>
                                    <a href="#">Fresco</a>
                                    <a href="#">Amaderado</a>
                                    <a href="#">Gourmet</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </li>
                <li class="navbar__item">
                    <a class="navbar__link" href="#">MUJERES</a>
                    <div class="navbar__list-megamenu">
                        <div class="navbar__list-megamenu__list">
                            <img src="../assets/imagenes/1759572947949-bottombanner-fr-mobile_3200x3000.jpg" alt="">
                            <div class="navbar__list-megamenu__links">
                                <div class="navbar__list-megamenu__links--title">
                                    <h2>PERFUMES</h2>
                                </div>
                                <a href="Chanel.jsp">Chanel</a>
                                <a href="Cristian_dior.jsp">Cristian Dior</a>
                            </div>

                            <div class="navbar__list-megamenu__list">
                                <div class="navbar__list-megamenu__links--perfiles">
                                    <h2>PERFILES DE FRAGANCIA</h2>
                                    <a href="#">Oriental</a>
                                    <a href="#">Floral</a>
                                    <a href="#">Fresco</a>
                                    <a href="#">Amaderado</a>
                                    <a href="#">Gourmet</a>
                                </div>
                            </div>
                        </div>
                    </div>

                </li>

                <li class="navbar__item">
                    <a class="navbar__link" href="#">BOUTIQUE</a>
                    <div class="navbar__list-megamenu">
                        <div class="navbar__list-megamenu__list">
                            <div class="navbar__list-megamenu__list">
                                <div class="navbar__list-megamenu__links--perfiles2">
                                    <h2>LATINOAMERICA</h2>
                                    <a href="Colombia.jsp">Colombia</a>
                                    
                                    <a href="Venezuela.jsp">Venezuela</a>
                                    
                                    
                                    
                                </div>
                                <img src="../assets/imagenes/boutique.jpg" alt="">
                                <img src="../assets/imagenes/boutique 2 .jpg" alt="">
                            </div>
                        </div>
                    </div>

                </li>
            </ul>
            <div class="navbar__brand">
                <a class="navbar__logo" href="../index.jsp">ANDREYLPZ</a>
            </div>
            <ul class="navbar__list">
                <li><a class="navbar__link" href="#">BLOG</a></li>
                <li class="navbar__item">
                    <a class="navbar__link" href="#">NOSOTROS</a>
                    <div class="navbar__list-megamenu">
                        <div class="navbar__list-megamenu__list">
                            <img src="../assets/imagenes/yves saint laurent.webp" alt="">
                            <div class="navbar__list-megamenu__list">
                                <div class="navbar__list-megamenu__links--perfiles2">
                                    <h2>PERFILES DE FRAGANCIA</h2>
                                    <a href="historia.jsp">Historia</a>
                                    <a href="filosofia.jsp">Filosofia</a>
                                    <a href="quienes_somos.jsp">Quienes Somos</a>
                                </div>
                            </div>
                        </div>
                    </div>
            </ul>

            <div class="navbar__actions">
                <nav>
                    <ul class="navbar__actions-list">
                        <li><a href="perfil.jsp"><img src="../assets/iconos/user.png" alt=""></a></li>
                        <li><a href="#compras"><img src="../assets/iconos/shopping.png" alt=""></a></li>
                        <li class="navbar__item">
                            <a href="#buscar"><img src="../assets/iconos/search.png" alt=""></a>
                            <div class="navbar__list-megamenu">
                                <div class="navbar__list-megamenu__list">
                                    <div class="navbar__list-megamenu__links--buscador">
                                        <h2>BUSCAR</h2>
                                        <input type="text">
                                    </div>
                                </div>
                            </div>
                        </li>

                    </ul>
                </nav>
            </div>
        </div>
    </header>

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

        <section class="cards-lociones">
            <!-- Card 2 -->
            <article class="card">
                <a href="Mujeres/Cristhian_dior/Dior_Addict_Rosy_Glow.jsp">
                    <img class="card__img" src="../assets/imagenes/Dior Addict Rosy Glow.webp" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">Dior Addict Rosy Glow</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia floral-afrutada delicada y luminosa que combina esencia
                        de rosa damascena con toques brillantes de lichi y un acorde cremoso con notas caramelizadas,
                        creando un aroma chispeante, dulce y resplandeciente que evoca alegría y feminidad suave.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">480.000 COP</p>
                </div>
            </article>


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









