<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../assets/estilos/style.css">
    <link rel="stylesheet" href="../assets/estilos/cart.css">
    <link rel="stylesheet" href="../assets/estilos/about.css">
    <title>Quiénes Somos - ANDREYLPZ</title>
</head>

<body class="about-main">

    <div class="discount">
        <h2 class="discount__title">RECIBA UN KIT DE MUESTRA LITRO POR LA COMPRA DE UNA LOSION</h2>
    </div>

    <!-- Header / Navbar (Reused) -->
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
                                    <a href="historia.jsp">Historia</a><a
                                        href="filosofia.jsp">Filosofia</a><a
                                        href="quienes_somos.jsp">Quienes Somos</a>
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
        <!-- Hero Section -->
        <section class="about-hero" style="background-image: url('../assets/imagenes/boutique.jpg');">
            <div class="about-hero__content">
                <h1 class="about-hero__title">Quiénes Somos</h1>
                <p class="about-hero__subtitle">DETRÁS DE LA ESENCIA</p>
            </div>
        </section>

        <!-- Content -->
        <div class="about-content">

            <section class="about-section">
                <h2 class="about-section__title">Nuestra Identidad</h2>
                <p class="about-section__text">
                    En ANDREYLPZ, somos artesanos del olfato, curadores de experiencias y amantes de la belleza eterna.
                    Fundada por Andrey Lopez, nuestra casa de perfumes nació con un propósito claro: redefinir el lujo a
                    través de la autenticidad y la conexión humana.
                </p>
                <img src="../assets/imagenes/boutique.jpg" alt="Nuestro Equipo" class="about-image">
            </section>

            <section class="about-section">
                <h2 class="about-section__title">Lo Que Nos Diferencia</h2>
                <p class="about-section__text">
                    La diferencia radica en nuestro enfoque humano. No vendemos productos; construimos relaciones. Cada
                    fragancia que sale de nuestro taller lleva consigo una parte de nuestra alma y dedicación.
                </p>
                <p class="about-section__text">
                    Nos alejamos de la producción masiva para centrarnos en lo artesanal, lo cercano y lo real. Queremos
                    que al usar un perfume ANDREYLPZ, sientas la mano del creador y la pasión de todo un equipo que
                    trabaja para ti.
                </p>
            </section>

            <div class="about-section__highlight">
                "Más que una marca, somos una familia unida por la pasión por lo extraordinario."
            </div>

        </div>

        <!-- CTA -->
        <div class="about-cta">
            <p class="about-cta__text">¿Listo para encontrar tu esencia?</p>
            <a href="perfil.jsp" class="about-cta__button">CONTÁCTANOS</a>
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
                <li class="footer__item"><a class="footer__link" href="Colombia.jsp">Colombia</a></li>
                <li class="footer__item"><a class="footer__link" href="Venezuela.jsp">Venezuela</a></li>
            </ul>
        </div>
    </footer>

    <script src="../assets/scripts/cart.js"></script>
</body>

</html>



