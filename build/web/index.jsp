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
  <!-- Barra de descuento superior -->
  <div class="discount">
    <h2 class="discount__title">RECIBA UN KIT DE MUESTRA LITRO POR LA COMPRA DE UNA LOSION</h2>
  </div>

  <!-- Encabezado y Barra de Navegación -->
  <header>
    <div class="navbar">
      <!-- Checkbox para el menú hamburguesa (Versión Móvil) -->
      <input type="checkbox" id="navbar-toggle" class="navbar__toggle">
      <label for="navbar-toggle" class="navbar__hamburger">
        <span class="hamburger-line"></span>
        <span class="hamburger-line"></span>
        <span class="hamburger-line"></span>
      </label>
      <ul class="navbar__list">
        <li class="navbar__item">
          <a class="navbar__link" href="#">HOMBRES</a>
          <div class="navbar__list-megamenu">

            <div class="navbar__list-megamenu__list">
              <img src="assets/imagenes/yves saint laurent.webp" alt="">
              <div class="navbar__list-megamenu__links">
                <div class="navbar__list-megamenu__links--title">
                  <h2>PERFUMES</h2>
                </div>
                  <a href="vistas/cartas.jsp">Xerjoff</a>
                  <a href="vistas/pacco_rabanne.jsp">Paco Rabanne</a>
              </div>

              <div class="navbar__list-megamenu__list">
                <div class="navbar__list-megamenu__links--perfiles">
                  <h2>PERFILES DE FRAGANCIA</h2>
                  <a href="#">Oriental</a>
                  <a href="#">Floral</a>
                  <a href="#">Fresco</a>
                  <a href="#">Amaderado</a>
                  <a href="#">Gourmet</a>
                </div>
              </div>
            </div>
          </div>
        </li>
        <li class="navbar__item">
          <a class="navbar__link" href="#">MUJERES</a>
          <div class="navbar__list-megamenu">
            <div class="navbar__list-megamenu__list">
              <img src="assets/imagenes/1759572947949-bottombanner-fr-mobile_3200x3000.jpg" alt="">
              <div class="navbar__list-megamenu__links">
                <div class="navbar__list-megamenu__links--title">
                  <h2>PERFUMES</h2>
                </div>
                <a href="vistas/Chanel.jsp">Chanel</a>
                <a href="vistas/Cristian_dior.jsp">Cristian Dior</a>
              </div>

              <div class="navbar__list-megamenu__list">
                <div class="navbar__list-megamenu__links--perfiles">
                  <h2>PERFILES DE FRAGANCIA</h2>
                  <a href="#">Oriental</a>
                  <a href="#">Floral</a>
                  <a href="#">Fresco</a>
                  <a href="#">Amaderado</a>
                  <a href="#">Gourmet</a>
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
                  <a href="vistas/Colombia.jsp">Colombia</a>
                  
                  <a href="vistas/Venezuela.jsp">Venezuela</a>
                  
                  
                  
                </div>
                <img src="assets/imagenes/boutique.jpg" alt="">
                <img src="assets/imagenes/boutique 2 .jpg" alt="">
              </div>
            </div>
          </div>

        </li>
      </ul>
      <div class="navbar__brand">
        <a class="navbar__logo" href="index.jsp">ANDREYLPZ</a>
      </div>
      <ul class="navbar__list">
        <li><a class="navbar__link" href="#">BLOG</a></li>
        <li class="navbar__item">
          <a class="navbar__link" href="#">NOSOTROS</a>
          <div class="navbar__list-megamenu">
            <div class="navbar__list-megamenu__list">
              <img src="assets/imagenes/yves saint laurent.webp" alt="">
              <div class="navbar__list-megamenu__list">
                <div class="navbar__list-megamenu__links--perfiles2">
                  <h2>PERFILES DE FRAGANCIA</h2>
                  <a href="vistas/historia.jsp">Historia</a>
                  <a href="vistas/filosofia.jsp">Filosofia</a>
                  <a href="vistas/quienes_somos.jsp">Quienes Somos</a>
                </div>
              </div>
            </div>
          </div>
      </ul>

      <div class="navbar__actions">
        <nav>
          <ul class="navbar__actions-list">
            <li><a href="vistas/perfil.jsp"><img src="assets/iconos/user.png" alt=""></a></li>
            <li><a href="#compras"><img src="assets/iconos/shopping.png" alt=""></a></li>
            <li class="navbar__item">
              <a href="#buscar"><img src="assets/iconos/search.png" alt=""></a>
              <div class="navbar__list-megamenu">
                <div class="navbar__list-megamenu__list">
                  <div class="navbar__list-megamenu__links--buscador">
                    <h2>BUSCAR</h2>
                    <input type="text">
                  </div>
                </div>
              </div>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </header>

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








