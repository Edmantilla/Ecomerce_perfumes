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
      <button class="main__section__button"><a href="vistas/cartas.jsp">DESCUBRIR</a></button>
    </section>
  </main>

  <%@ include file="vistas/_footer.jsp" %>


    <script src="assets/scripts/cart.js"></script>
</body>

</html>








