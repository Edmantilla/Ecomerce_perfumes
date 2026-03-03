<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Página para recuperar contraseña -->
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../assets/estilos/style.css">
    <title>Recuperar Contraseña - ANDREYLPZ</title>
</head>

<body data-no-cart>
    <%@ include file="_navbar.jsp" %>

    <!-- Contenido Principal: Recuperar Contraseña -->
    <main class="main-perfil">
        <section class="formulario">
            <h2 class="formulario-title">RECUPERAR CONTRASEÑA</h2>

            <!-- Paso 1: Ingresar correo -->
            <div id="paso-correo">
                <p class="formulario-descripcion">
                    Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.
                </p>
                <form id="form-recuperar" method="post" action="" onsubmit="mostrarPasoConfirmacion(event)">
                    <div class="formulario-username">
                        <label for="correo-recuperar">Correo Electrónico</label>
                        <input type="email" id="correo-recuperar" name="correo_electronico"
                            placeholder="ejemplo@correo.com" required>
                    </div>
                    <button class="formulario-button" type="submit">ENVIAR ENLACE</button>
                    <div class="formulario-registrarse">
                        <a class="formulario-registrarse" href="perfil.jsp">Volver a Iniciar Sesión</a>
                    </div>
                </form>
            </div>

            <!-- Paso 2: Confirmación (se muestra tras enviar) -->
            <div id="paso-confirmacion" style="display: none; text-align: center;">
                <div class="formulario-icono-ok">âœ“</div>
                <p class="formulario-descripcion">
                    Si el correo está registrado, recibirás un enlace en tu bandeja de entrada en los próximos minutos.
                    Revisa también tu carpeta de spam.
                </p>
                <div class="formulario-registrarse">
                    <a class="formulario-registrarse" href="perfil.jsp">Volver a Iniciar Sesión</a>
                </div>
            </div>

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

    <script>
        function mostrarPasoConfirmacion(e) {
            e.preventDefault();
            document.getElementById('paso-correo').style.display = 'none';
            document.getElementById('paso-confirmacion').style.display = 'block';}
    </script>


    <script src="../assets/scripts/cart.js"></script>
</body>

</html>









