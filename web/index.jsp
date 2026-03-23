<%-- ==========================================================================
     index.jsp — Página principal (Home) del sitio ANDREYLPZ.

     Estructura:
     - Incluye _navbar.jsp (navbar dinámico con marcas desde BD).
     - Sección hero con título "TORINO" y botón DESCUBRIR → cartas.jsp (Xerjoff).
     - Incluye _footer.jsp (footer compartido).
     - Carga cart.js para funcionalidad del carrito y búsqueda en navbar.

     Nota: Esta página está en la RAÍZ del proyecto, no en /vistas/,
     por lo que los includes usan rutas con prefijo "vistas/".
     ========================================================================== --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
  <%-- Navbar dinámico compartido (carga marcas desde BD) --%>
  <%@ include file="vistas/_navbar.jsp" %>

  <%-- Contenido Principal: sección hero con botón de descubrimiento --%>
  <main class="main-home">

    <section class="main__section">
      <h2 class="main__section__title">TORINO</h2>
      <h3 class="main__section__subtitle">EL AROMA DE LA CELEBRACION</h3>
      <%-- Botón DESCUBRIR enlaza a la página de Xerjoff (cartas.jsp) --%>
      <button class="main__section__button"><a href="vistas/cartas.jsp">DESCUBRIR</a></button>
    </section>
  </main>

  <%-- Footer compartido --%>
  <%@ include file="vistas/_footer.jsp" %>

  <%-- Script del carrito: gestiona carrito en localStorage, panel lateral y búsqueda --%>
  <script src="assets/scripts/cart.js"></script>
</body>

</html>








