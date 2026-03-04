<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../assets/estilos/style.css">
    <link rel="stylesheet" href="../assets/estilos/cart.css">
    <link rel="stylesheet" href="../assets/estilos/boutique.css">
    <title>Boutique Colombia - ANDREYLPZ</title>
</head>

<body class="boutique-main">

    <%@ include file="_navbar.jsp" %>

    <main>
        <!-- 1 & 2: Hero Section -->
        <section class="boutique-hero" style="background-image: url('../assets/imagenes/boutique.jpg');">
            <div class="boutique-hero__content">
                <h1 class="boutique-hero__title">BOUTIQUE COLOMBIA</h1>
                <p class="boutique-hero__subtitle">BOGOTÁ • MEDELLÍN • CALI</p>
            </div>
        </section>

        <!-- 3: Info Block -->
        <section class="boutique-info">
            <div class="boutique-info__grid">
                <div class="boutique-info__column">
                    <h3 class="boutique-info__heading">Ubicación</h3>
                    <p class="boutique-info__text">Calle 81 # 11-94</p>
                    <p class="boutique-info__text">Zona T, Bogotá</p>
                    <p class="boutique-info__text">Colombia</p>
                </div>
                <div class="boutique-info__column">
                    <h3 class="boutique-info__heading">Horarios</h3>
                    <p class="boutique-info__text">Lunes - Sábado: 10:00 AM - 8:00 PM</p>
                    <p class="boutique-info__text">Domingos: 11:00 AM - 6:00 PM</p>
                </div>
                <div class="boutique-info__column">
                    <h3 class="boutique-info__heading">Contacto</h3>
                    <p class="boutique-info__text">+57 1 234 5678</p>
                    <p class="boutique-info__text">contacto.co@andreylpz.com</p>
                </div>
                <div class="boutique-info__column">
                    <h3 class="boutique-info__heading">Servicios Exclusivos</h3>
                    <ul class="boutique-info__list">
                        <li>Consultoría de Fragancias</li>
                        <li>Personalización de Frascos</li>
                        <li>Eventos Privados</li>
                    </ul>
                </div>
            </div>
        </section>

        <!-- 4: Gallery -->
        <section class="boutique-gallery">
            <div class="boutique-gallery__grid">
                <div class="boutique-gallery__item">
                    <img src="../assets/imagenes/boutique 2 .jpg" alt="Interior Boutique" class="boutique-gallery__img">
                </div>
                <div class="boutique-gallery__item">
                    <img src="../assets/imagenes/boutique.jpg" alt="Detalle Productos" class="boutique-gallery__img">
                </div>
                <div class="boutique-gallery__item">
                    <img src="../assets/imagenes/boutique 2 .jpg" alt="Experiencia Cliente" class="boutique-gallery__img">
                </div>
            </div>
        </section>

        <!-- 5: CTA Button -->
        <div class="boutique-cta">
            <a href="#" class="boutique-cta__button">Reservar Cita</a>
        </div>
    </main>

    <%@ include file="_footer.jsp" %>

    <script src="../assets/scripts/cart.js"></script>
</body>

</html>




