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

        <section class="cards-lociones">
            <!-- Card 1 -->
            <article class="card">
                <a href="Hombres/Xerjoff/Losion.jsp">
                    <img class="card__img" src="../assets/imagenes/RICHWOOD.webp" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">RICHWOOD</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">Una fragancia exquisita con notas amaderadas y un toque de elegancia.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">1.000.000 COP</p>
                </div>
            </article>

            <!-- Card 2 -->
            <article class="card">
                <a href="Hombres/Xerjoff/Naxos.jsp">
                    <img class="card__img" src="../assets/imagenes/NAXOS.webp" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">NAXOS</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">Una fragancia unisex de la familia cítrica gourmand que celebra la
                        riqueza y la luz del Mediterráneo.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">1.120.000 COP</p>
                </div>
            </article>

            <!-- Card 3 -->
            <article class="card">
                <a href="Hombres/Xerjoff/Erba_Pura.jsp">
                    <img class="card__img" src="../assets/imagenes/erba pura.webp" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">ERBA PURA</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">Una fragancia unisex vibrante y frutal con apertura cítrica de naranja,
                        limón y bergamota.
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">1.500.000 COP</p>
                </div>
            </article>

            <!-- Card 4 -->
            <article class="card">
                <a href="Hombres/Xerjoff/Alexandria_II.jsp">
                    <img class="card__img" src="../assets/imagenes/ALEXANDRIA 2.webp" alt="Richwood Perfume">
                </a>

                <div class="card__content">
                    <h2 class="card__title">ALEXANDRIA II</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia unisex de la familia amaderada y ámbar oriental que
                        combina notas de salida de palisandro, lavanda, canela y manzana.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">1.100.000 COP</p>
                </div>
            </article>

            <!-- Card 5 -->
            <article class="card">
                <a href="Hombres/Xerjoff/Torino_XXI.jsp">
                    <img class="card__img" src="../assets/imagenes/TORINO 21.webp" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">TORINO XXI</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">es una fragancia unisex fresca y verde aromática inspirada en la
                        energía vibrante de Turín y el espíritu deportivo
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">1.300.000 COP</p>
                </div>
            </article>

            <!-- Card 6 -->
            <article class="card">
                <a href="Hombres/Xerjoff/Erba_Gold.jsp">
                    <img class="card__img" src="../assets/imagenes/ERBA GOLD.webp" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">ERBA GOLD</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia unisex fresca y luminosa que mezcla cítricos vibrantes y
                        frutas dulces sobre una base cálida de almizcle y ámbar
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">1.600.000 COP</p>
                </div>
            </article>

            <article class="card">
                <a href="Hombres/Xerjoff/Lira.jsp">
                    <img class="card__img" src="../assets/imagenes/LIRA.webp" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">LIRA</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia unisex amaderada y afrutada que combina cítricos
                        brillantes con notas florales suaves y una base cálida de maderas y almizcle
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">1.000.000 COP</p>
                </div>
            </article>

            <article class="card">
                <a href="Hombres/Xerjoff/Homme.jsp">
                    <img class="card__img" src="../assets/imagenes/HOMME.webp" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">HOMME</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia masculina aromática y fresca con notas cítricas y
                        especiadas combinadas con un corazón elegante de maderas y flores suaves
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">1.720.000 COP</p>
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
</body>

</html>









