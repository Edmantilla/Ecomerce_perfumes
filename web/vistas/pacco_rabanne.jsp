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

        <section class="cards-lociones">
            <!-- Card 1 -->
            <article class="card">
                <a href="Hombres/Pacco_rabanne/Invictus_Victory_Absolu.jsp">
                    <img class="card__img" src="../assets/imagenes/PACCO RABANNE INVICTUS.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">INVICTUS VICTORY ABSOLU</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia masculina intensa y vibrante que combina notas especiadas
                        y dulces con una base poderosa y amaderada
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">500.000 COP</p>
                </div>
            </article>

            <!-- Card 2 -->
            <article class="card">
                <a href="Hombres/Pacco_rabanne/Phantom_Elixir.jsp">
                    <img class="card__img" src="../assets/imagenes/PHAMTON ELIXIR.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">PHANTOM ELIXIR</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia masculina intensa y adictiva que mezcla notas aromáticas
                        frescas con matices dulces y amaderados para un aroma moderno, sofisticado y memorable.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">620.000 COP</p>
                </div>
            </article>

            <!-- Card 3 -->
            <article class="card">
                <a href="Hombres/Pacco_rabanne/Million_Gold.jsp">
                    <img class="card__img" src="../assets/imagenes/MILLION GOLD.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">MILLION GOLD</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia masculina vibrante y lujosa con notas cítricas
                        energéticas, corazón especiado y una base cálida y amaderada, ofreciendo un aroma elegante y
                        lleno de carácter.
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">510.000 COP</p>
                </div>
            </article>

            <!-- Card 4 -->
            <article class="card">
                <a href="Hombres/Pacco_rabanne/One_Million_Lucky.jsp">
                    <img class="card__img" src="../assets/imagenes/ONE MILLON LUCKY.avif" alt="Richwood Perfume">
                </a>

                <div class="card__content">
                    <h2 class="card__title">ONE Million Lucky</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia masculina dulce y amaderada que combina notas de avellana
                        y ciruela con un corazón de jazmín y pachulí, creando un aroma moderno, seductor y lleno de
                        energía.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">830.000 COP</p>
                </div>
            </article>

            <!-- Card 5 -->
            <article class="card">
                <a href="Hombres/Pacco_rabanne/Phantom_Intense.jsp">
                    <img class="card__img" src="../assets/imagenes/PHAMTON INTENSE.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">PHANTOM INTENSE</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">Phantom Intense es una fragancia masculina audaz y moderna que
                        intensifica el perfil aromático con notas dulces, amaderadas y especiadas para un aroma más
                        profundo, sensual y duradero.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">600.000 COP</p>
                </div>
            </article>

            <!-- Card 6 -->
            <article class="card">
                <a href="Hombres/Pacco_rabanne/Invictus_Parfum.jsp">
                    <img class="card__img" src="../assets/imagenes/INVICTUS PARFUM.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">INVICTUS PARFUM</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia masculina potente y sofisticada que combina notas frescas
                        y cítricas con un corazón especiado y una base cálida de maderas, creando un aroma elegante,
                        enérgico y duradero.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">635.000 COP</p>
                </div>
            </article>

            <article class="card">
                <a href="Hombres/Pacco_rabanne/One_Million.jsp">
                    <img class="card__img" src="../assets/imagenes/ONE MILLON.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">ONE Million</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia masculina atrevida y distintiva que mezcla notas cítricas
                        y especiadas con un corazón floral y una base cálida de cuero y maderas, ofreciendo un aroma
                        lujoso, seductor y moderno.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">500.000 COP</p>
                </div>
            </article>

            <article class="card">
                <a href="Hombres/Pacco_rabanne/One_Million_Royal.jsp">
                    <img class="card__img" src="../assets/imagenes/ONE MILLON ROYALE.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">ONE MILLION ROYAL</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia masculina intensa y elegante que combina notas especiadas
                        y amaderadas con un corazón dulce y oriental, creando un aroma sofisticado, lujoso y seductor.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">720.000 COP</p>
                </div>
            </article>

            <article class="card">
                <a href="Hombres/Pacco_rabanne/One_Million_Elixir.jsp">
                    <img class="card__img" src="../assets/imagenes/ONE MILLON ELIXIR.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">ONE Million Elixir</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia masculina rica y poderosa que mezcla notas dulces de
                        frutos rojos y canela con un corazón especiado y una base profunda de cuero y maderas,
                        ofreciendo un aroma seductor, elegante y duradero.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">620.000 COP</p>
                </div>
            </article>

            <article class="card">
                <a href="Hombres/Pacco_rabanne/One_Million_Parfum.jsp">
                    <img class="card__img" src="../assets/imagenes/ONE MILLON PARFUM.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">ONE Million Parfum</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia masculina elegante y moderna que combina notas cítricas y
                        especiadas con un corazón cálido de cuero y maderas, creando un aroma sofisticado, audaz y
                        duradero.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">620.000 COP</p>
                </div>
            </article>

            <article class="card">
                <a href="Hombres/Pacco_rabanne/Invictus.jsp">
                    <img class="card__img" src="../assets/imagenes/INVICTUS.avif" alt="Richwood Perfume">
                </a>
                <div class="card__content">
                    <h2 class="card__title">INVICTUS</h2>
                    <h3 class="card__subtitle">Perfume</h3>
                    <p class="card__description">una fragancia masculina fresca y enérgica que mezcla notas cítricas y
                        acuáticas con un corazón aromático y una base amaderada, ofreciendo un aroma vigoroso, moderno y
                        seductor.
                    </p>
                    <p class="card__quantity">50 ml - 100 ml</p>
                    <p class="card__price">860.000 COP</p>
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









