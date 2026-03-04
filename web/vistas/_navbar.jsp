<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.List, java.util.ArrayList, logica.Marca, persistencias.MarcaJpaController" %>
<%
    List<Marca> todasMarcas = new ArrayList<>();
    try {
        todasMarcas = new MarcaJpaController().findMarcaEntities();
    } catch (Exception ex) { /* sin marcas si falla */ }

    List<Marca> marcasHombre = new ArrayList<>();
    List<Marca> marcasMujer  = new ArrayList<>();
    for (Marca __m : todasMarcas) {
        if (!__m.isActivo()) continue;
        if ("MUJER".equals(__m.getGenero())) marcasMujer.add(__m);
        else marcasHombre.add(__m);
    }

    // Determinar prefijo de rutas según si estamos en /vistas/ o en la raíz
    String __uri = request.getRequestURI();
    boolean __enVistas = __uri.contains("/vistas/");
    String __prefix = __enVistas ? "" : "vistas/";
    String __assets  = __enVistas ? "../assets/" : "assets/";
    String __home    = __enVistas ? "../index.jsp" : "index.jsp";
    String __perfil  = __enVistas ? "perfil.jsp"   : "vistas/perfil.jsp";
%>
<div class="discount">
    <h2 class="discount__title">RECIBA UN KIT DE MUESTRA LITRO POR LA COMPRA DE UNA LOSION</h2>
</div>
<header>
    <div class="navbar">
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
                        <img src="<%= __assets %>imagenes/yves saint laurent.webp" alt="">
                        <div class="navbar__list-megamenu__links">
                            <div class="navbar__list-megamenu__links--title">
                                <h2>PERFUMES</h2>
                            </div>
                            <% for (Marca __m : marcasHombre) {
                                String __url = (__m.getPaginaUrl() != null && !__m.getPaginaUrl().isEmpty())
                                    ? __m.getPaginaUrl() : __m.getNombreMarca().toLowerCase().replace(" ","_") + ".jsp";
                            %>
                                <a href="<%= __prefix + __url %>"><%= __m.getNombreMarca() %></a>
                            <% } %>
                        </div>
                    </div>
                </div>
            </li>
            <li class="navbar__item">
                <a class="navbar__link" href="#">MUJERES</a>
                <div class="navbar__list-megamenu">
                    <div class="navbar__list-megamenu__list">
                        <img src="<%= __assets %>imagenes/1759572947949-bottombanner-fr-mobile_3200x3000.jpg" alt="">
                        <div class="navbar__list-megamenu__links">
                            <div class="navbar__list-megamenu__links--title">
                                <h2>PERFUMES</h2>
                            </div>
                            <% for (Marca __m : marcasMujer) {
                                String __url = (__m.getPaginaUrl() != null && !__m.getPaginaUrl().isEmpty())
                                    ? __m.getPaginaUrl() : __m.getNombreMarca().toLowerCase().replace(" ","_") + ".jsp";
                            %>
                                <a href="<%= __prefix + __url %>"><%= __m.getNombreMarca() %></a>
                            <% } %>
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
                                <a href="<%= __prefix %>Colombia.jsp">Colombia</a>
                                <a href="<%= __prefix %>Venezuela.jsp">Venezuela</a>
                            </div>
                            <img src="<%= __assets %>imagenes/boutique.jpg" alt="">
                            <img src="<%= __assets %>imagenes/boutique 2 .jpg" alt="">
                        </div>
                    </div>
                </div>
            </li>
        </ul>
        <div class="navbar__brand">
            <a class="navbar__logo" href="<%= __home %>">ANDREYLPZ</a>
        </div>
        <ul class="navbar__list">
            <li><a class="navbar__link" href="#">BLOG</a></li>
            <li class="navbar__item">
                <a class="navbar__link" href="#">NOSOTROS</a>
                <div class="navbar__list-megamenu">
                    <div class="navbar__list-megamenu__list">
                        <img src="<%= __assets %>imagenes/yves saint laurent.webp" alt="">
                        <div class="navbar__list-megamenu__list">
                            <div class="navbar__list-megamenu__links--perfiles2">
                                <h2>NOSOTROS</h2>
                                <a href="<%= __prefix %>historia.jsp">Historia</a>
                                <a href="<%= __prefix %>filosofia.jsp">Filosofia</a>
                                <a href="<%= __prefix %>quienes_somos.jsp">Quienes Somos</a>
                            </div>
                        </div>
                    </div>
                </div>
            </li>
        </ul>
        <div class="navbar__actions">
            <nav>
                <ul class="navbar__actions-list">
                    <li><a href="<%= __perfil %>"><img src="<%= __assets %>iconos/user.png" alt=""></a></li>
                    <li><a href="#compras"><img src="<%= __assets %>iconos/shopping.png" alt=""></a></li>
                    <li class="navbar__item">
                        <a href="#buscar"><img src="<%= __assets %>iconos/search.png" alt=""></a>
                        <div class="navbar__list-megamenu">
                            <div class="navbar__list-megamenu__list">
                                <div class="navbar__list-megamenu__links--buscador">
                                    <h2>BUSCAR</h2>
                                    <input type="text" id="search-input" placeholder="Buscar perfumes..." autocomplete="off">
                                    <div id="search-results" class="search-results"></div>
                                </div>
                            </div>
                        </div>
                    </li>
                </ul>
            </nav>
        </div>
    </div>
</header>
