<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="logica.Usuario" %>
<!-- Página de registro de nuevo usuario -->
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/estilos/style.css">
    <title>Crear Cuenta - ANDREYLPZ</title>
</head>

<body data-no-cart>
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
                            <img src="<%= request.getContextPath() %>/assets/imagenes/yves saint laurent.webp" alt="">
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
                            <img src="<%= request.getContextPath() %>/assets/imagenes/1759572947949-bottombanner-fr-mobile_3200x3000.jpg" alt="">
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
                                    <a href="Colombia.jsp">Colombia</a>
                                    
                                    <a href="Venezuela.jsp">Venezuela</a>
                                    
                                    
                                    
                                </div>
                                <img src="<%= request.getContextPath() %>/assets/imagenes/boutique.jpg" alt="">
                                <img src="<%= request.getContextPath() %>/assets/imagenes/boutique 2 .jpg" alt="">
                            </div>
                        </div>
                    </div>
                </li>
            </ul>
            <div class="navbar__brand">
                <a class="navbar__logo" href="../index.jsp">ANDREYLPZ</a>
            </div>
            <ul class="navbar__list">
                <li><a class="navbar__link" href="#">BLOG</a></li>
                <li class="navbar__item">
                    <a class="navbar__link" href="#">NOSOTROS</a>
                    <div class="navbar__list-megamenu">
                        <div class="navbar__list-megamenu__list">
                            <img src="<%= request.getContextPath() %>/assets/imagenes/yves saint laurent.webp" alt="">
                            <div class="navbar__list-megamenu__list">
                                <div class="navbar__list-megamenu__links--perfiles2">
                                    <h2>PERFILES DE FRAGANCIA</h2>
                                    <a href="historia.jsp">Historia</a>
                                    <a href="filosofia.jsp">Filosofia</a>
                                    <a href="quienes_somos.jsp">Quienes Somos</a>
                                </div>
                            </div>
                        </div>
                    </div>
            </ul>

            <div class="navbar__actions">
                <nav>
                    <ul class="navbar__actions-list">
                        <li><a href="perfil.jsp"><img src="<%= request.getContextPath() %>/assets/iconos/user.png" alt=""></a></li>
                        <li><a href="#compras"><img src="<%= request.getContextPath() %>/assets/iconos/shopping.png" alt=""></a></li>
                        <li class="navbar__item">
                            <a href="#buscar"><img src="<%= request.getContextPath() %>/assets/iconos/search.png" alt=""></a>
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

    <!-- Contenido Principal: Formulario de Registro -->
    <main class="main-perfil">
        <% 
          Usuario uSesion = (Usuario) session.getAttribute("usuario");
          if (uSesion != null) {
              response.sendRedirect(request.getContextPath() + "/index.jsp");
              return;
          }
        %>
        <section class="formulario">
            <h2 class="formulario-title">CREAR CUENTA</h2>
            <% String errorReg = (String) request.getAttribute("error"); %>
            <% if (errorReg != null) { %>
              <p style="color:red; text-align:center; margin-bottom:12px;"><%= errorReg %></p>
            <% } %>
            <form method="post" action="<%= request.getContextPath() %>/SvRegistro">

                <div class="formulario-username">
                    <label for="nombre">Nombre</label>
                    <input type="text" id="nombre" name="nombre" placeholder="Tu nombre" required>
                </div>

                <div class="formulario-username">
                    <label for="apellido">Apellido</label>
                    <input type="text" id="apellido" name="apellido" placeholder="Tu apellido" required>
                </div>

                <div class="formulario-username">
                    <label for="correo">Correo Electrónico</label>
                    <input type="email" id="correo" name="correo_electronico" placeholder="ejemplo@correo.com" required>
                </div>

                <div class="formulario-contrasena">
                    <label for="contrasena">Contraseña</label>
                    <input type="password" id="contrasena" name="contrasena" placeholder="Mínimo 8 caracteres" required
                        minlength="8">
                </div>

                <div class="formulario-contrasena">
                    <label for="confirmar_contrasena">Confirmar Contraseña</label>
                    <input type="password" id="confirmar_contrasena" name="confirmar_contrasena"
                        placeholder="Repite tu contraseña" required minlength="8">
                </div>

                <div class="formulario-username">
                    <label for="fecha_nacimiento">Fecha de Nacimiento</label>
                    <input type="date" id="fecha_nacimiento" name="fecha_nacimiento" required max="2008-02-18"
                        min="1900-01-01">
                </div>

                <div class="formulario-username">
                    <label for="direccion">Dirección</label>
                    <input type="text" id="direccion" name="direccion" placeholder="Calle, carrera, barrio, ciudad" required>
                </div>

                <div class="formulario-checkbox">
                    <input type="checkbox" id="terminos" name="terminos" required>
                    <label for="terminos">He leído y acepto los
                        <a href="#" class="formulario-link">Términos y Condiciones</a>
                        de ANDREYLPZ.</label>
                </div>

                <div class="formulario-checkbox">
                    <input type="checkbox" id="tratamiento_datos" name="tratamiento_datos" required>
                    <label for="tratamiento_datos">Doy mi consentimiento para el
                        <a href="#" class="formulario-link">Tratamiento de mis Datos Personales</a>
                        conforme a la política de privacidad de ANDREYLPZ.</label>
                </div>

                <button class="formulario-button" type="submit">CREAR CUENTA</button>

                <div class="formulario-registrarse">
                    <a class="formulario-registrarse" href="perfil.jsp">¿Ya tienes una cuenta? Inicia
                        sesión</a>
                </div>

            </form>
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
                <li class="footer__item"><a class="footer__link" href="Colombia.jsp">Colombia</a></li>
                <li class="footer__item"><a class="footer__link" href="#">Estados unidos</a></li>
                <li class="footer__item"><a class="footer__link" href="#">Ecuador</a></li>
            </ul>
        </div>
    </footer>


    <script src="<%= request.getContextPath() %>/assets/scripts/cart.js"></script>
</body>

</html>









