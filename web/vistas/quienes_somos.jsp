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

    <%@ include file="_navbar.jsp" %>

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



