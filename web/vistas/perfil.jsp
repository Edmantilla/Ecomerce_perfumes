<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="logica.Usuario" %>
<!-- Página de perfil de usuario e inicio de sesión -->
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="../assets/estilos/style.css">
  <link rel="stylesheet" href="../assets/estilos/cart.css">
  <title>perfil</title>
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
              <img src="../assets/imagenes/yves saint laurent.webp" alt="">
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
              <img src="../assets/imagenes/1759572947949-bottombanner-fr-mobile_3200x3000.jpg" alt="">
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
                <img src="../assets/imagenes/boutique.jpg" alt="">
                <img src="../assets/imagenes/boutique 2 .jpg" alt="">
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
              <img src="../assets/imagenes/yves saint laurent.webp" alt="">
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
            <li><a href="perfil.jsp"><img src="../assets/iconos/user.png" alt=""></a></li>
            <li><a href="#compras"><img src="../assets/iconos/shopping.png" alt=""></a></li>
            <li class="navbar__item">
              <a href="#buscar"><img src="../assets/iconos/search.png" alt=""></a>
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
  <main class="main-perfil" style="display:flex;flex-direction:column;align-items:center;padding:40px 20px;gap:0;min-height:60vh">
    <%
      Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
      if (usuarioSesion != null && Boolean.TRUE.equals(session.getAttribute("esAdmin"))) {
          response.sendRedirect(request.getContextPath() + "/vistas/admin.jsp");
          return;
      }
    %>

    <% if (session.getAttribute("usuario") != null) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        String nombreMostrar = (u.getCliente() != null) ? u.getCliente().getNombreCompleto() : "Usuario";
    %>
    <!-- Vista de perfil cuando el usuario está logueado -->
    <section class="formulario" style="max-width:480px">
      <h2 class="formulario-title">MI PERFIL</h2>
      <div style="display:flex;flex-direction:column;gap:18px;padding:8px 0 24px">
        <div style="display:flex;align-items:center;gap:20px;padding:16px;background:#f8f8f8;border-radius:8px">
          <div style="width:64px;height:64px;border-radius:50%;background:#1a1a1a;display:flex;align-items:center;justify-content:center;color:#fff;font-size:26px;font-weight:700;flex-shrink:0">
            <%= nombreMostrar.charAt(0) %>
          </div>
          <div>
            <div style="font-size:18px;font-weight:600;color:#1a1a1a"><%= nombreMostrar %></div>
            <div style="font-size:13px;color:#666;margin-top:4px"><%= u.getCorreoUsuario() %></div>
            <div style="font-size:12px;color:#999;margin-top:2px"><%= u.getRol() != null ? u.getRol().getNombreRol() : "" %></div>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:12px">
          <div style="display:flex;justify-content:space-between;padding:12px 16px;background:#f8f8f8;border-radius:6px">
            <span style="color:#666;font-size:14px">Correo</span>
            <span style="font-weight:500;font-size:14px"><%= u.getCorreoUsuario() %></span>
          </div>
          <% if (u.getCliente() != null && u.getCliente().getDireccion() != null && !"Sin especificar".equals(u.getCliente().getDireccion())) { %>
          <div style="display:flex;justify-content:space-between;padding:12px 16px;background:#f8f8f8;border-radius:6px">
            <span style="color:#666;font-size:14px">Dirección</span>
            <span style="font-weight:500;font-size:14px"><%= u.getCliente().getDireccion() %></span>
          </div>
          <% } %>
          <% String colorEstado = u.isActivo() ? "#2e7d32" : "#c62828"; %>
          <div style="display:flex;justify-content:space-between;padding:12px 16px;background:#f8f8f8;border-radius:6px">
            <span style="color:#666;font-size:14px">Estado</span>
            <span style="font-weight:500;font-size:14px;color:<%= colorEstado %>">
              <%= u.isActivo() ? "Activo" : "Inactivo" %>
            </span>
          </div>
        </div>

        <a href="<%= request.getContextPath() %>/SvLogout"
           style="display:block;text-align:center;padding:12px;background:#1a1a1a;color:#fff;border-radius:6px;text-decoration:none;font-size:14px;letter-spacing:1px;font-weight:600;margin-top:8px">
          CERRAR SESIÓN
        </a>
        <a href="../index.jsp"
           style="display:block;text-align:center;padding:10px;border:1px solid #ccc;color:#1a1a1a;border-radius:6px;text-decoration:none;font-size:13px;letter-spacing:1px">
          ← VOLVER A LA TIENDA
        </a>
      </div>
    </section>

    <!-- ── Mis Pedidos ── -->
    <section style="max-width:720px;width:100%;margin:0 auto 60px">
      <h2 style="font-size:18px;font-weight:700;letter-spacing:2px;color:#1a1a1a;margin-bottom:20px;padding-bottom:10px;border-bottom:2px solid #1a1a1a">MIS PEDIDOS</h2>
      <div id="mis-pedidos-container">
        <p style="color:#999;font-size:14px;text-align:center;padding:24px 0">Cargando pedidos...</p>
      </div>
    </section>

    <script>
    (function() {
      var ctx = (function() {
        var p = window.location.pathname.split('/');
        return '/' + p[1];
      })();

      var ESTADO_LABEL = {
        'PENDIENTE':   { label: 'Pendiente',   color: '#e65100', bg: '#fff3e0' },
        'PROCESANDO':  { label: 'Procesando',  color: '#1565c0', bg: '#e3f2fd' },
        'ENVIADO':     { label: 'Enviado',      color: '#6a1b9a', bg: '#f3e5f5' },
        'ENTREGADO':   { label: 'Entregado',    color: '#2e7d32', bg: '#e8f5e9' },
        'CANCELADO':   { label: 'Cancelado',    color: '#c62828', bg: '#ffebee' }
      };

      function fmt(n) {
        return parseFloat(n).toLocaleString('es-CO') + ' COP';
      }

      function estadoBadge(estado) {
        var e = ESTADO_LABEL[estado] || { label: estado, color: '#555', bg: '#f0f0f0' };
        return '<span style="display:inline-block;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:600;color:' + e.color + ';background:' + e.bg + '">' + e.label + '</span>';
      }

      function renderDetalles(detalles) {
        if (!detalles || detalles.length === 0) return '<p style="color:#999;font-size:13px;padding:8px 0">Sin detalles disponibles.</p>';
        var html = '<table style="width:100%;border-collapse:collapse;font-size:13px;margin-top:8px">'
          + '<thead><tr style="border-bottom:1px solid #ddd">'
          + '<th style="text-align:left;padding:6px 8px;color:#666;font-weight:600">Producto</th>'
          + '<th style="text-align:center;padding:6px 8px;color:#666;font-weight:600">Cant.</th>'
          + '<th style="text-align:right;padding:6px 8px;color:#666;font-weight:600">Precio unit.</th>'
          + '<th style="text-align:right;padding:6px 8px;color:#666;font-weight:600">Subtotal</th>'
          + '</tr></thead><tbody>';
        detalles.forEach(function(d) {
          html += '<tr style="border-bottom:1px solid #f0f0f0">'
            + '<td style="padding:7px 8px">' + d.producto + '</td>'
            + '<td style="padding:7px 8px;text-align:center">' + d.cantidad + '</td>'
            + '<td style="padding:7px 8px;text-align:right">' + fmt(d.precioUnitario) + '</td>'
            + '<td style="padding:7px 8px;text-align:right;font-weight:600">' + fmt(d.subtotal) + '</td>'
            + '</tr>';
        });
        html += '</tbody></table>';
        return html;
      }

      function renderPedidos(pedidos) {
        var container = document.getElementById('mis-pedidos-container');
        if (!pedidos || pedidos.length === 0) {
          container.innerHTML = '<div style="text-align:center;padding:40px 0;color:#999;border:1px dashed #ddd;border-radius:8px">'
            + '<div style="font-size:36px;margin-bottom:12px">🛍️</div>'
            + '<p style="font-size:15px">Aún no tienes pedidos.</p>'
            + '<a href="../index.jsp" style="color:#1a1a1a;font-size:13px;text-decoration:underline">Ir a la tienda →</a>'
            + '</div>';
          return;
        }

        var html = '';
        pedidos.forEach(function(p, idx) {
          var detId = 'det-' + p.id;
          html += '<div style="border:1px solid #e0e0e0;border-radius:8px;margin-bottom:14px;overflow:hidden">'
            + '<div style="display:flex;align-items:center;justify-content:space-between;padding:14px 18px;background:#fafafa;cursor:pointer;gap:12px" onclick="toggleDet(\'' + detId + '\', this)">'
            +   '<div style="display:flex;align-items:center;gap:14px;flex-wrap:wrap">'
            +     '<span style="font-weight:700;font-size:14px;color:#1a1a1a">#' + p.id + '</span>'
            +     estadoBadge(p.estado)
            +     '<span style="font-size:13px;color:#888">' + p.fecha + '</span>'
            +   '</div>'
            +   '<div style="display:flex;align-items:center;gap:16px">'
            +     '<span style="font-weight:700;font-size:14px;color:#1a1a1a">' + fmt(p.total) + '</span>'
            +     '<span style="font-size:18px;color:#999;transition:transform .2s" id="arrow-' + detId + '">▼</span>'
            +   '</div>'
            + '</div>'
            + '<div id="' + detId + '" style="display:none;padding:14px 18px;border-top:1px solid #e0e0e0;background:#fff">'
            +   renderDetalles(p.detalles)
            + '</div>'
            + '</div>';
        });
        container.innerHTML = html;
      }

      window.toggleDet = function(id, row) {
        var el = document.getElementById(id);
        var arrow = document.getElementById('arrow-' + id);
        var open = el.style.display !== 'none';
        el.style.display = open ? 'none' : 'block';
        if (arrow) arrow.style.transform = open ? '' : 'rotate(180deg)';
      };

      var _cachedPedidos = [];

      function cargarPedidos(silencioso) {
        fetch(ctx + '/SvMisPedidos', { credentials: 'same-origin' })
          .then(function(r) { return r.json(); })
          .then(function(data) {
            if (data.error) {
              if (!silencioso) {
                document.getElementById('mis-pedidos-container').innerHTML =
                  '<p style="color:#c62828;font-size:14px;text-align:center">' + data.error + '</p>';
              }
              return;
            }
            // Detectar cambios de estado para mostrar notificación
            if (silencioso && _cachedPedidos.length > 0) {
              data.forEach(function(p) {
                var prev = _cachedPedidos.find(function(x) { return x.id === p.id; });
                if (prev && prev.estado !== p.estado) {
                  mostrarNotifEstado(p.id, p.estado);
                }
              });
            }
            _cachedPedidos = data;
            renderPedidos(data);
          })
          .catch(function() {
            if (!silencioso) {
              document.getElementById('mis-pedidos-container').innerHTML =
                '<p style="color:#999;font-size:14px;text-align:center">No se pudieron cargar los pedidos.</p>';
            }
          });
      }

      function mostrarNotifEstado(idPedido, estado) {
        var ESTADO_LABEL = {
          'PENDIENTE': 'Pendiente', 'PROCESANDO': 'Procesando',
          'ENVIADO': 'Enviado', 'ENTREGADO': 'Entregado', 'CANCELADO': 'Cancelado'
        };
        var colors = {
          'PENDIENTE': '#e65100', 'PROCESANDO': '#1565c0',
          'ENVIADO': '#6a1b9a', 'ENTREGADO': '#2e7d32', 'CANCELADO': '#c62828'
        };
        var label = ESTADO_LABEL[estado] || estado;
        var color = colors[estado] || '#1a1a1a';

        var notif = document.createElement('div');
        notif.style.cssText = 'position:fixed;top:20px;right:20px;z-index:9999;background:#fff;border-left:4px solid ' + color + ';padding:14px 20px;border-radius:6px;box-shadow:0 4px 20px rgba(0,0,0,.15);max-width:300px;font-size:14px;animation:slideIn .3s ease';
        notif.innerHTML = '<strong style="display:block;margin-bottom:4px">Estado actualizado</strong>'
          + 'Tu pedido <strong>#' + idPedido + '</strong> ahora está: '
          + '<span style="color:' + color + ';font-weight:700">' + label + '</span>';
        document.body.appendChild(notif);
        setTimeout(function() {
          notif.style.opacity = '0';
          notif.style.transition = 'opacity .4s';
          setTimeout(function() { notif.remove(); }, 400);
        }, 6000);
      }

      // Añadir keyframes si no existen
      if (!document.getElementById('perfil-anim-style')) {
        var st = document.createElement('style');
        st.id = 'perfil-anim-style';
        st.textContent = '@keyframes slideIn{from{opacity:0;transform:translateX(30px)}to{opacity:1;transform:translateX(0)}}';
        document.head.appendChild(st);
      }

      cargarPedidos(false);
      setInterval(function() { cargarPedidos(true); }, 15000);
    })();
    </script>

    <% } else { %>
    <!-- Formulario de Login -->
    <section class="formulario">
      <h2 class="formulario-title">LOGIN</h2>
      <%
        String mensajeExito = (String) session.getAttribute("registroExitoso");
        if (mensajeExito != null) {
            session.removeAttribute("registroExitoso");
      %>
        <p style="color:#2e7d32;text-align:center;margin-bottom:12px;padding:10px;background:#e8f5e9;border-radius:6px;font-size:14px"><%= mensajeExito %></p>
      <% } %>
      <%
        String errorLogin = request.getParameter("error");
        if (errorLogin == null) errorLogin = (String) request.getAttribute("error");
      %>
      <% if (errorLogin != null && !errorLogin.isBlank()) { %>
        <p style="color:#c62828;text-align:center;margin-bottom:12px;padding:10px;background:#ffebee;border-radius:6px;font-size:14px;font-weight:500"><%= errorLogin %></p>
      <% } %>
      <form method="post" action="<%= request.getContextPath() %>/SvLogin">
        <div class="formulario-username">
          <label>Correo Electronico</label>
          <input type="email" name="correo_electronico" placeholder="ejemplo@correo.com" required>
        </div>
        <div class="formulario-contrasena">
          <label>Contraseña</label>
          <input type="password" name="contrasena" placeholder="Tu contraseña" required>
        </div>
        <div class="formulario-recordar">
          <a class="formulario-recordar" href="olvide_contrasena.jsp">¿Olvidaste tu contraseña?</a>
        </div>
        <button class="formulario-button" type="submit">INICIAR SESION</button>
        <div class="formulario-registrarse">
          <a class="formulario-registrarse" href="registro.jsp">Crear una cuenta</a>
        </div>
      </form>
    </section>
    <% } %>

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

  <script src="../assets/scripts/cart.js"></script>
</body>

</html>

