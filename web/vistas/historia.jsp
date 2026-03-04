<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../assets/estilos/style.css">
    <link rel="stylesheet" href="../assets/estilos/cart.css">
    <link rel="stylesheet" href="../assets/estilos/about.css">
    <title>Nuestra Historia - ANDREYLPZ</title>
</head>

<body class="about-main">

    <%@ include file="_navbar.jsp" %>

    <main>
        <!-- Hero Section -->
        <section class="about-hero" style="background-image: url('../assets/imagenes/yves\ saint\ laurent.webp');">
            <div class="about-hero__content">
                <h1 class="about-hero__title">Nuestra Historia</h1>
                <p class="about-hero__subtitle">EL ARTE DE LA PERFUMERÍA A TRAVÉS DEL TIEMPO</p>
            </div>
        </section>

        <!-- Content -->
        <div class="about-content">

            <section class="about-section">
                <h2 class="about-section__title">El Origen</h2>
                <p class="about-section__text">
                    Nacida de la pasión por lo exquisito, ANDREYLPZ comenzó no como una marca, sino como una búsqueda
                    incansable de la perfección olfativa. Fundada en los valles donde florecen las esencias más puras,
                    nuestra historia se teje con hilos de tradición y vanguardia.
                </p>
                <p class="about-section__text">
                    La inspiración inicial provino de los antiguos alquimistas, aquellos que buscaban capturar el alma
                    de la naturaleza en una botella. Queríamos revivir ese arte perdido, trayendo al mundo contemporáneo
                    fragancias que no solo se huelen, sino que se sienten.
                </p>
                <img src="../assets/imagenes/boutique 2 .jpg" alt="Laboratorio de Perfumes Antiguo" class="about-image">
            </section>

            <div class="about-section__highlight">
                "No creamos perfumes, destilamos memorias."
            </div>

            <section class="about-section">
                <h2 class="about-section__title">Evolución y Momentos Clave</h2>

                <div class="timeline-item">
                    <span class="timeline-year">2015</span>
                    <p class="about-section__text">Apertura de nuestra primera boutique, un espacio íntimo dedicado a la
                        personalización de aromas.</p>
                </div>

                <div class="timeline-item">
                    <span class="timeline-year">2018</span>
                    <p class="about-section__text">Lanzamiento de la colección "Ecos de la Tierra", galardonada
                        internacionalmente por su uso de ingredientes sostenibles.</p>
                </div>

                <div class="timeline-item">
                    <span class="timeline-year">2023</span>
                    <p class="about-section__text">Expansión a Latinoamérica con nuestras boutiques insignia en Bogotá y
                        Caracas, llevando el lujo a nuevos horizontes.</p>
                </div>
            </section>

        </div>

        <!-- CTA -->
        <div class="about-cta">
            <p class="about-cta__text">Forma parte de nuestra historia.</p>
            <a href="cartas.jsp" class="about-cta__button">DESCUBRE NUESTRAS COLECCIONES</a>
        </div>

    </main>

    <%@ include file="_footer.jsp" %>

    <script src="../assets/scripts/cart.js"></script>
</body>

</html>



