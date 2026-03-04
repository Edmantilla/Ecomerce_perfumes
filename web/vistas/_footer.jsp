<%
    String __uri2 = request.getRequestURI();
    boolean __enVistas2 = __uri2.contains("/vistas/");
    String __fp = __enVistas2 ? "" : "vistas/";
%>
<footer>
    <div class="footer__section--newsletter">
        <h2 class="footer__title">Reciba un 10% de descuento en su pr&oacute;ximo pedido superior a 300 cop al
            suscribirse al bolet&iacute;n informativo de andreylpz.</h2>
        <div class="footer__form-wrapper">
            <form action="">
                <input type="text" name="correo_electronico" id="" placeholder="DIRECCION DE CORREO ELECTRONICO">
                <button>INSCRIBIRSE</button>
            </form>
        </div>
    </div>

    <div class="footer__section">
        <h2 class="footer__title">LEGAL</h2>
        <ul class="footer__list">
            <li class="footer__item"><a class="footer__link" href="#">Politica de cookies</a></li>
        </ul>
    </div>
    <div class="footer__section">
        <h2 class="footer__title">ELECCION DE PAIS</h2>
        <ul class="footer__list">
            <li class="footer__item"><a class="footer__link" href="<%= __fp %>Colombia.jsp">Colombia</a></li>
            <li class="footer__item"><a class="footer__link" href="<%= __fp %>Venezuela.jsp">Venezuela</a></li>
        </ul>
    </div>
</footer>
