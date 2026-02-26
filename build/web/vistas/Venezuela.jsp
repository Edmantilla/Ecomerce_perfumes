<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../assets/estilos/style.css">
    <link rel="stylesheet" href="../assets/estilos/cart.css">
    <link rel="stylesheet" href="../assets/estilos/boutique.css">
    <title>Boutique Venezuela - ANDREYLPZ</title>
</head>

<body class="boutique-main">

    <div class="discount">
        <h2 class="discount__title">RECIBA UN KIT DE MUESTRA LITRO POR LA COMPRA DE UNA LOSION</h2>
    </div>

    <!-- Header / Navbar (Reused from existing template) -->
    <header>
        <div class="navbar">
            <input type="checkbox" id="navbar-toggle" class="navbar__toggle">
            <label for="navbar-toggle" class="navbar__hamburger"><span class="hamburger-line"></span><span
                    class="hamburger-line"></span><span class="hamburger-line"></span></label>
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
                                    <a href="#">Oriental</a><a href="#">Floral</a><a href="#">Fresco</a><a
                                        href="#">Amaderado</a><a href="#">Gourmet</a>
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
                                    <a href="#">Oriental</a><a href="#">Floral</a><a href="#">Fresco</a><a
                                        href="#">Amaderado</a><a href="#">Gourmet</a>
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
                                    <a href="Colombia.jsp">Colombia</a><a
                                        href="Venezuela.jsp">Venezuela</a>
                                </div>
                                <img src="../assets/imagenes/boutique.jpg" alt=""><img
                                    src="../assets/imagenes/boutique 2 .jpg" alt="">
                            </div>
                        </div>
                    </div>
                </li>
            </ul>
            <div class="navbar__brand"><a class="navbar__logo" href="../index.jsp">ANDREYLPZ</a></div>
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
                                    <a href="historia.jsp">Historia</a><a href="filosofia.jsp">Filosofia</a><a href="quienes_somos.jsp">Quienes Somos</a>
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
                                        <h2>BUSCAR</h2><input type="text">
                                    </div>
                                </div>
                            </div>
                        </li>
                    </ul>
                </nav>
            </div>
        </div>
    </header>

    <main>
        <!-- 1 & 2: Hero Section -->
        <section class="boutique-hero" style="background-image: url('../assets/imagenes/boutique 2 .jpg');">
            <div class="boutique-hero__content">
                <h1 class="boutique-hero__title">BOUTIQUE VENEZUELA</h1>
                <p class="boutique-hero__subtitle">CARACAS • MARACAIBO • VALENCIA</p>
            </div>
        </section>

        <!-- 3: Info Block -->
        <section class="boutique-info">
            <div class="boutique-info__grid">
                <div class="boutique-info__column">
                    <h3 class="boutique-info__heading">Ubicación</h3>
                    <p class="boutique-info__text">Av. Principal de Las Mercedes</p>
                    <p class="boutique-info__text">Centro San Ignacio, Caracas</p>
                    <p class="boutique-info__text">Venezuela</p>
                </div>
                <div class="boutique-info__column">
                    <h3 class="boutique-info__heading">Horarios</h3>
                    <p class="boutique-info__text">Lunes - Viernes: 10:00 AM - 7:00 PM</p>
                    <p class="boutique-info__text">Sábados: 11:00 AM - 8:00 PM</p>
                </div>
                <div class="boutique-info__column">
                    <h3 class="boutique-info__heading">Contacto</h3>
                    <p class="boutique-info__text">+58 212 999 9999</p>
                    <p class="boutique-info__text">contacto.ve@andreylpz.com</p>
                </div>
                <div class="boutique-info__column">
                    <h3 class="boutique-info__heading">Servicios Exclusivos</h3>
                    <ul class="boutique-info__list">
                        <li>Cata de Perfumes</li>
                        <li>Atelier de Regalos</li>
                        <li>Entregas VIP</li>
                    </ul>
                </div>
            </div>
        </section>

        <!-- 4: Gallery -->
        <section class="boutique-gallery">
            <div class="boutique-gallery__grid">
                <div class="boutique-gallery__item">
                    <img src="../assets/imagenes/boutique.jpg" alt="Interior Boutique" class="boutique-gallery__img">
                </div>
                <div class="boutique-gallery__item">
                    <img src="../assets/imagenes/boutique 2 .jpg" alt="Detalle Productos" class="boutique-gallery__img">
                </div>
                <div class="boutique-gallery__item">
                    <img src="../assets/imagenes/boutique.jpg" alt="Experiencia Cliente" class="boutique-gallery__img">
                </div>
            </div>
        </section>

        <!-- 5: CTA Button -->
        <div class="boutique-cta">
            <a href="#" class="boutique-cta__button">Reservar Cita</a>
        </div>
    </main>

    <!-- Footer (Reused) -->
    <footer>
        <div class="footer__section--newsletter">
            <h2 class="footer__title">Reciba un 10% de descuento en su próximo pedido.</h2>
            <div class="footer__form-wrapper">
                <form action=""><input type="text" placeholder="CORREO ELECTRONICO"><button>INSCRIBIRSE</button></form>
            </div>
        </div>
        <div class="footer__section">
            <h2 class="footer__title">SERVICIO AL CLIENTE</h2>
            <ul class="footer__list">
                <li class="footer__item"><a class="footer__link" href="#">Contactanos</a></li>
            </ul>
        </div>
        <div class="footer__section">
            <h2 class="footer__title">ELECCION DE PAIS</h2>
            <ul class="footer__list">
                <li class="footer__item"><a class="footer__link" href="Venezuela.jsp">Venezuela</a></li>
            </ul>
        </div>
    </footer>

    <script src="../assets/scripts/cart.js"></script>
</body>

</html>




