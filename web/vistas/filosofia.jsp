<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../assets/estilos/style.css">
    <link rel="stylesheet" href="../assets/estilos/cart.css">
    <link rel="stylesheet" href="../assets/estilos/about.css">
    <title>Filosofía - ANDREYLPZ</title>
</head>

<body class="about-main">

    <%@ include file="_navbar.jsp" %>

    <main>
        <!-- Hero Section -->
        <section class="about-hero"
            style="background-image: url('../assets/imagenes/1759572947949-bottombanner-fr-mobile_3200x3000.jpg');">
            <div class="about-hero__content">
                <h1 class="about-hero__title">Filosofía</h1>
                <p class="about-hero__subtitle">VALORES, VISIÓN Y ESENCIA</p>
            </div>
        </section>

        <!-- Content -->
        <div class="about-content">

            <section class="about-section">
                <h2 class="about-section__title">Nuestra Visión</h2>
                <p class="about-section__text">
                    Creemos que un perfume es mucho más que un aroma; es una extensión de la personalidad, una
                    declaración silenciosa pero poderosa. Nuestra visión es empoderar a cada individuo para que
                    encuentre su firma olfativa única, aquella que resuena con su esencia más profunda.
                </p>
                <p class="about-section__text">
                    No seguimos tendencias efímeras. Buscamos la atemporalidad, creando fragancias que trascienden las
                    modas y se convierten en clásicos instantáneos.
                </p>
            </section>

            <section class="about-section">
                <h2 class="about-section__title">Principios Fundamentales</h2>

                <div class="principles-grid">
                    <div class="principle-card">
                        <div class="principle-icon">âœ¦</div>
                        <h3>Excelencia</h3>
                        <p>Seleccionamos solo los ingredientes más raros y preciosos.</p>
                    </div>
                    <div class="principle-card">
                        <div class="principle-icon">âˆž</div>
                        <h3>Sostenibilidad</h3>
                        <p>Respetamos la naturaleza que nos regala sus esencias.</p>
                    </div>
                    <div class="principle-card">
                        <div class="principle-icon">â™¥</div>
                        <h3>Pasión</h3>
                        <p>Cada botella es llenada con dedicación y amor por el arte.</p>
                    </div>
                </div>
            </section>

            <div class="about-section__highlight">
                "El lujo verdadero reside en los detalles y en la autenticidad."
            </div>

            <section class="about-section">
                <h2 class="about-section__title">Enfoque al Cliente</h2>
                <p class="about-section__text">
                    Usted no es solo un cliente, es el protagonista de nuestra historia. Cada visita a nuestras
                    boutiques o nuestra web está diseñada para ser una experiencia sensorial inmersiva, donde su
                    satisfacción y su viaje olfativo son nuestra única prioridad.
                </p>
            </section>

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



