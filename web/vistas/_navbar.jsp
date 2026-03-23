<%-- ==========================================================================
     _navbar.jsp — Componente compartido (include) del navbar principal.
     Se incluye con <%@ include file=".../_navbar.jsp" %> en todas las páginas.

     Funcionalidad:
     1. Consulta TODAS las marcas activas de la BD vía MarcaJpaController.
     2. Las separa en dos listas: marcasHombre y marcasMujer según el campo género.
     3. Calcula prefijos de ruta dinámicos (__prefix, __assets, __home, __perfil)
        dependiendo de si la página actual está en /vistas/ o en la raíz.
     4. Renderiza el navbar con mega-menús desplegables:
        - HOMBRES: lista dinámica de marcas masculinas con enlace a su JSP.
        - MUJERES: lista dinámica de marcas femeninas con enlace a su JSP.
        - BOUTIQUE: enlaces estáticos a Colombia.jsp y Venezuela.jsp.
        - NOSOTROS: Historia, Filosofía, Quiénes Somos.
     5. Acciones del navbar: icono usuario → perfil, carrito → panel lateral,
        búsqueda → input con resultados en tiempo real (manejado por cart.js).
     ========================================================================== --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.List, java.util.ArrayList, logica.Marca, persistencias.MarcaJpaController" %>
<%
    // --- Carga de marcas desde la BD ---
    // Obtiene todas las marcas registradas para poblar los mega-menús dinámicamente.
    List<Marca> todasMarcas = new ArrayList<>();
    try {
        todasMarcas = new MarcaJpaController().findMarcaEntities();
    } catch (Exception ex) { /* Si falla la conexión, los menús quedan vacíos */ }

    // Separar marcas activas por género para los menús HOMBRES y MUJERES
    List<Marca> marcasHombre = new ArrayList<>();
    List<Marca> marcasMujer  = new ArrayList<>();
    for (Marca __m : todasMarcas) {
        if (!__m.isActivo()) continue; // Solo marcas activas
        if ("MUJER".equals(__m.getGenero())) marcasMujer.add(__m);
        else marcasHombre.add(__m);    // Por defecto HOMBRE
    }

    // --- Prefijos de ruta dinámicos ---
    // Detecta si la página actual está dentro de /vistas/ o en la raíz del proyecto
    // para ajustar las rutas relativas de assets, home, perfil y páginas de marca.
    String __uri = request.getRequestURI();
    boolean __enVistas = __uri.contains("/vistas/");
    String __prefix = __enVistas ? "" : "vistas/";          // Prefijo para páginas en /vistas/
    String __assets  = __enVistas ? "../assets/" : "assets/"; // Ruta a la carpeta de assets
    String __home    = __enVistas ? "../index.jsp" : "index.jsp";   // Enlace al home
    String __perfil  = __enVistas ? "perfil.jsp"   : "vistas/perfil.jsp"; // Enlace al perfil
%>
<%-- Banner promocional fijo en la parte superior --%>
<div class="discount">
    <h2 class="discount__title">RECIBA UN KIT DE MUESTRA LITRO POR LA COMPRA DE UNA LOSION</h2>
</div>

<%-- === HEADER / NAVBAR PRINCIPAL === --%>
<header>
    <div class="navbar">
        <input type="checkbox" id="navbar-toggle" class="navbar__toggle">
        <label for="navbar-toggle" class="navbar__hamburger">
            <span class="hamburger-line"></span>
            <span class="hamburger-line"></span>
            <span class="hamburger-line"></span>
        </label>
        <%-- Menú de navegación izquierdo: HOMBRES, MUJERES, BOUTIQUE --%>
        <ul class="navbar__list">
            <%-- === Mega-menú HOMBRES: marcas masculinas dinámicas desde BD === --%>
            <li class="navbar__item">
                <a class="navbar__link" href="#">HOMBRES</a>
                <div class="navbar__list-megamenu">
                    <div class="navbar__list-megamenu__list">
                        <img src="<%= __assets %>imagenes/yves saint laurent.webp" alt="">
                        <div class="navbar__list-megamenu__links">
                            <div class="navbar__list-megamenu__links--title">
                                <h2>PERFUMES</h2>
                            </div>
                            <%-- Itera marcas de HOMBRE: genera enlace a su JSP --%>
                            <% for (Marca __m : marcasHombre) {
                                // Usa paginaUrl de la BD, o genera nombre_marca.jsp por defecto
                                String __url = (__m.getPaginaUrl() != null && !__m.getPaginaUrl().isEmpty())
                                    ? __m.getPaginaUrl() : __m.getNombreMarca().toLowerCase().replace(" ","_") + ".jsp";
                            %>
                                <a href="<%= __prefix + __url %>"><%= __m.getNombreMarca() %></a>
                            <% } %>
                        </div>
                    </div>
                </div>
            </li>
            <%-- === Mega-menú MUJERES: marcas femeninas dinámicas desde BD === --%>
            <li class="navbar__item">
                <a class="navbar__link" href="#">MUJERES</a>
                <div class="navbar__list-megamenu">
                    <div class="navbar__list-megamenu__list">
                        <img src="<%= __assets %>imagenes/1759572947949-bottombanner-fr-mobile_3200x3000.jpg" alt="">
                        <div class="navbar__list-megamenu__links">
                            <div class="navbar__list-megamenu__links--title">
                                <h2>PERFUMES</h2>
                            </div>
                            <%-- Itera marcas de MUJER: genera enlace a su JSP --%>
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
            <%-- === Mega-menú BOUTIQUE: tiendas físicas por país === --%>
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
        <%-- Menú de navegación derecho: BLOG, NOSOTROS --%>
        <ul class="navbar__list">
            <li><a class="navbar__link" href="#">BLOG</a></li>
            <%-- === Mega-menú NOSOTROS: Historia, Filosofía, Quiénes Somos === --%>
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
        <%-- === Acciones del navbar (iconos derecha) === --%>
        <div class="navbar__actions">
            <nav>
                <ul class="navbar__actions-list">
                    <%-- Icono de usuario → enlace al perfil --%>
                    <li><a href="<%= __perfil %>"><img src="<%= __assets %>iconos/user.png" alt=""></a></li>
                    <%-- Icono de carrito → abre panel lateral (cart.js) --%>
                    <li><a href="#compras"><img src="<%= __assets %>iconos/shopping.png" alt=""></a></li>
                    <%-- Icono de búsqueda → despliega mega-menú con input de búsqueda --%>
                    <li class="navbar__item">
                        <a href="#buscar"><img src="<%= __assets %>iconos/search.png" alt=""></a>
                        <div class="navbar__list-megamenu">
                            <div class="navbar__list-megamenu__list">
                                <div class="navbar__list-megamenu__links--buscador">
                                    <h2>BUSCAR</h2>
                                    <%-- Input de búsqueda: cart.js escucha este input con initSearch() --%>
                                    <input type="text" id="search-input" placeholder="Buscar perfumes..." autocomplete="off">
                                    <%-- Contenedor de resultados en tiempo real (populado por JS) --%>
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
