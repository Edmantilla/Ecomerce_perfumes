<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Página principal del sitio web (Home) -->
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="assets/estilos/style.css">
  <link rel="stylesheet" href="assets/estilos/cart.css">
  <title>Home</title>
</head>

<body>
  <%@ include file="vistas/_navbar.jsp" %>

  <!-- Contenido Principal -->
  <main class="main-home">

    <section class="main__section">
      <h2 class="main__section__title">TORINO</h2>
      <h3 class="main__section__subtitle">EL AROMA DE LA CELEBRACION</h3>
      <button class="main__section__button"><a href="">DESCUBRIR</a></button>
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
        <li class="footer__item"><a class="footer__link" href="vistas/Colombia.jsp">Colombia</a></li>
        <li class="footer__item"><a class="footer__link" href="#">Estados unidos</a></li>
        <li class="footer__item"><a class="footer__link" href="#">Ecuador</a></li>
      </ul>
    </div>
  </footer>


    <script src="assets/scripts/cart.js"></script>
</body>

</html>








