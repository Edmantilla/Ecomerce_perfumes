<%-- ==========================================================================
     Venezuela.jsp — Página de la boutique de ANDREYLPZ en Venezuela.

     Contenido estático con información de la tienda física:
     - Hero con ciudades (Caracas, Maracaibo, Valencia).
     - Info grid: ubicación, horarios, contacto, servicios exclusivos.
     - Galería de imágenes de la boutique.
     - CTA: botón "Reservar Cita".
     Incluye _navbar.jsp, _footer.jsp, cart.js.
     Usa boutique.css para estilos específicos.
     ========================================================================== --%>
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

    <%-- Navbar compartido --%>
    <%@ include file="_navbar.jsp" %>

    <main>
        <%-- Hero Section con imagen de fondo de boutique --%>
        <section class="boutique-hero" style="background-image: url('../assets/imagenes/boutique 2 .jpg');">
            <div class="boutique-hero__content">
                <h1 class="boutique-hero__title">BOUTIQUE VENEZUELA</h1>
                <p class="boutique-hero__subtitle">CARACAS • MARACAIBO • VALENCIA</p>
            </div>
        </section>

        <%-- Bloque de información: ubicación, horarios, contacto, servicios --%>
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

        <%-- Galería de imágenes de la boutique --%>
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

        <%-- CTA: Reservar cita --%>
        <div class="boutique-cta">
            <a href="#" class="boutique-cta__button">Reservar Cita</a>
        </div>
    </main>

    <%-- Footer compartido --%>
    <%@ include file="_footer.jsp" %>

    <%-- cart.js para carrito y búsqueda en navbar --%>
    <script src="../assets/scripts/cart.js"></script>
</body>

</html>




