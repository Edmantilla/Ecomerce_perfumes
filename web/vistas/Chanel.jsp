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

        <section class="cards-lociones">
            <!-- Card 1 -->
            <article class="card">
                <a href="Mujeres/Chanel/N5.jsp">
                    <img class="card__img" src="../assets/imagenes/CHANEL.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">N°5</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una de las fragancias más icónicas y reconocidas del mundo, lanzada en
                        1921 por la casa de moda francesa Chanel
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">360.000 COP</p>
                </div>
            </article>

            <!-- Card 2 -->
            <article class="card">
                <a href="Mujeres/Chanel/Coco_Mademoiselle.jsp">
                    <img class="card__img" src="../assets/imagenes/COCO CHANEL.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">COCO MADEMOISELLE</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia femenina lanzada por Chanel en 2001 que representa la
                        elegancia moderna, fresca y audaz de la mujer contemporánea
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">410.000 COP</p>
                </div>
            </article>

            <!-- Card 3 -->
            <article class="card">
                <a href="Mujeres/Chanel/Chance_Eau_Splendide.jsp">
                    <img class="card__img" src="../assets/imagenes/CHANEL EAU SPLENDIDE.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">CHANCE EAU SPLENDIDE</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia femenina de la línea Chance de Chanel que evoca frescura,
                        alegría y un espíritu vibrante
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">480.000 COP</p>
                </div>
            </article>

            <!-- Card 4 -->
            <article class="card">
                <a href="Mujeres/Chanel/Chance_Eau_Tendre.jsp">
                    <img class="card__img" src="../assets/imagenes/CHANCE EAU TENDRE.avif" alt="Richwood Perfume">
                </a>

                <div class="card__content">
                    <h2 class="card__title">CHANCE EAU TENDRE</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia femenina de Chanel que destaca por su aroma delicado,
                        fresco y romántico
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">460.000 COP</p>
                </div>
            </article>

            <!-- Card 5 -->
            <article class="card">
                <a href="Mujeres/Chanel/Chance_Eau_Fraiche.jsp">
                    <img class="card__img" src="../assets/imagenes/CHANCE EAU FRAICHE.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">CHANCE EAU FRAÎCHE</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia femenina lanzada en 2007 que se distingue por su carácter
                        vibrante, fresco y energizante
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">490.000 COP</p>
                </div>
            </article>

            <!-- Card 6 -->
            <article class="card">
                <a href="Mujeres/Chanel/Chance.jsp">
                    <img class="card__img" src="../assets/imagenes/CHANCE.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">CHANCE</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia femenina lanzada por Chanel en 2003 que simboliza
                        energía, elegancia y espíritu audaz
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">460.000 COP</p>
                </div>
            </article>

            <!-- Card 7 -->
            <article class="card">
                <a href="Mujeres/Chanel/Gabrielle_Chanel.jsp">
                    <img class="card__img" src="../assets/imagenes/GABRIELLE CHANEL.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">GABRIELLE CHANEL</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia femenina lanzada en 2017 que rinde homenaje a la
                        fundadora de la casa, Gabrielle “Coco” Chanel. Su aroma es luminoso, floral y sofisticado,
                        compuesto por una armoniosa mezcla de jazmín, ylang-ylang, flor de azahar y nardo</p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">510.000 COP</p>
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









