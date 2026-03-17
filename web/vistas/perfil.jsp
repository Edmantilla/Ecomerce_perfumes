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
    <%@ include file="_navbar.jsp" %>

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
          <div style="display:flex;justify-content:space-between;align-items:center;padding:12px 16px;background:#f8f8f8;border-radius:6px">
            <span style="color:#666;font-size:14px">Dirección</span>
            <div style="display:flex;align-items:center;gap:10px">
              <span id="dir-texto" style="font-weight:500;font-size:14px"><%= (u.getCliente() != null && u.getCliente().getDireccion() != null && !"Sin especificar".equals(u.getCliente().getDireccion())) ? u.getCliente().getDireccion() : "Sin especificar" %></span>
              <button onclick="abrirEditarDir()" style="background:none;border:1px solid #ccc;border-radius:4px;padding:3px 10px;font-size:11px;cursor:pointer;color:#1a1a1a;letter-spacing:.5px;font-weight:600">EDITAR</button>
            </div>
          </div>
          <div id="dir-form" style="display:none;padding:12px 16px;background:#f0f0f0;border-radius:6px">
            <label style="font-size:12px;color:#666;font-weight:600;display:block;margin-bottom:6px">Nueva dirección</label>
            <div style="display:flex;gap:8px;flex-wrap:wrap">
              <input id="dir-input" type="text" placeholder="Calle, carrera, barrio, ciudad" style="flex:1;min-width:180px;padding:9px 12px;border:1px solid #ddd;border-radius:6px;font-size:14px">
              <button onclick="guardarDireccion()" style="padding:9px 18px;background:#1a1a1a;color:#fff;border:none;border-radius:6px;font-size:13px;font-weight:600;cursor:pointer">GUARDAR</button>
              <button onclick="cerrarEditarDir()" style="padding:9px 14px;background:#fff;color:#666;border:1px solid #ccc;border-radius:6px;font-size:13px;cursor:pointer">CANCELAR</button>
            </div>
            <div id="dir-msg" style="font-size:13px;margin-top:6px;min-height:16px"></div>
          </div>
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

    <!-- ── Cambiar Contraseña ── -->
    <section style="max-width:480px;width:100%;margin:0 auto 40px">
      <h2 style="font-size:18px;font-weight:700;letter-spacing:2px;color:#1a1a1a;margin-bottom:16px;padding-bottom:10px;border-bottom:2px solid #1a1a1a">CAMBIAR CONTRASE&Ntilde;A</h2>
      <div style="display:flex;flex-direction:column;gap:14px">
        <div style="display:flex;flex-direction:column;gap:4px">
          <label style="font-size:12px;color:#666;font-weight:600">Contrase&ntilde;a actual</label>
          <input id="cp-actual" type="password" placeholder="Tu contrase&ntilde;a actual" maxlength="20"
            style="padding:10px 12px;border:1px solid #ddd;border-radius:6px;font-size:14px">
          <span id="cp-err-actual" style="display:none;color:#c62828;font-size:12px;margin-top:2px"></span>
        </div>
        <div style="display:flex;flex-direction:column;gap:4px">
          <label style="font-size:12px;color:#666;font-weight:600">Nueva contrase&ntilde;a</label>
          <input id="cp-nueva" type="password" placeholder="Entre 8 y 20 caracteres" maxlength="20"
            style="padding:10px 12px;border:1px solid #ddd;border-radius:6px;font-size:14px">
          <div id="cp-strength-bar" style="display:flex;gap:4px;height:4px;margin-top:5px">
            <span style="flex:1;border-radius:2px;background:#e0e0e0;transition:background .3s"></span>
            <span style="flex:1;border-radius:2px;background:#e0e0e0;transition:background .3s"></span>
            <span style="flex:1;border-radius:2px;background:#e0e0e0;transition:background .3s"></span>
            <span style="flex:1;border-radius:2px;background:#e0e0e0;transition:background .3s"></span>
          </div>
          <div id="cp-strength-label" style="font-size:11px;color:#888;margin-top:2px"></div>
          <span id="cp-err-nueva" style="display:none;color:#c62828;font-size:12px;margin-top:2px"></span>
        </div>
        <div style="display:flex;flex-direction:column;gap:4px">
          <label style="font-size:12px;color:#666;font-weight:600">Confirmar nueva contrase&ntilde;a</label>
          <input id="cp-confirmar" type="password" placeholder="Repite la nueva contrase&ntilde;a" maxlength="20"
            style="padding:10px 12px;border:1px solid #ddd;border-radius:6px;font-size:14px">
          <span id="cp-err-confirmar" style="display:none;color:#c62828;font-size:12px;margin-top:2px"></span>
        </div>
        <div id="cp-msg" style="font-size:13px;min-height:18px"></div>
        <button onclick="guardarContrasena()" style="padding:11px;background:#1a1a1a;color:#fff;border:none;border-radius:6px;font-size:13px;font-weight:700;cursor:pointer;letter-spacing:1px">CAMBIAR CONTRASE&Ntilde;A</button>
      </div>
    </section>

    <!-- ── Mis Teléfonos (RF02) ── -->
    <section style="max-width:720px;width:100%;margin:0 auto 40px">
      <h2 style="font-size:18px;font-weight:700;letter-spacing:2px;color:#1a1a1a;margin-bottom:16px;padding-bottom:10px;border-bottom:2px solid #1a1a1a">MIS TEL&Eacute;FONOS</h2>
      <div id="tel-lista" style="margin-bottom:16px"></div>
      <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:flex-end">
        <div style="display:flex;flex-direction:column;gap:4px;flex:1;min-width:140px">
          <label style="font-size:12px;color:#666;font-weight:600">N&uacute;mero</label>
          <input id="tel-nuevo-numero" type="tel" placeholder="Ej: 3001234567" style="padding:9px 12px;border:1px solid #ddd;border-radius:6px;font-size:14px">
        </div>
        <div style="display:flex;flex-direction:column;gap:4px">
          <label style="font-size:12px;color:#666;font-weight:600">Tipo</label>
          <select id="tel-nuevo-tipo" style="padding:9px 12px;border:1px solid #ddd;border-radius:6px;font-size:14px">
            <option value="CELULAR">Celular</option>
            <option value="FIJO">Fijo</option>
            <option value="TRABAJO">Trabajo</option>
          </select>
        </div>
        <button onclick="agregarTelefono()" style="padding:9px 18px;background:#1a1a1a;color:#fff;border:none;border-radius:6px;font-size:13px;font-weight:600;cursor:pointer;letter-spacing:1px">+ AGREGAR</button>
      </div>
      <div id="tel-msg" style="font-size:13px;margin-top:8px;min-height:18px"></div>
    </section>

    <!-- ── Mis Correos Adicionales (RF03) ── -->
    <section style="max-width:720px;width:100%;margin:0 auto 60px">
      <h2 style="font-size:18px;font-weight:700;letter-spacing:2px;color:#1a1a1a;margin-bottom:16px;padding-bottom:10px;border-bottom:2px solid #1a1a1a">CORREOS ADICIONALES</h2>
      <div id="correo-lista" style="margin-bottom:16px"></div>
      <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:flex-end">
        <div style="display:flex;flex-direction:column;gap:4px;flex:1;min-width:200px">
          <label style="font-size:12px;color:#666;font-weight:600">Nuevo correo</label>
          <input id="correo-nuevo" type="email" placeholder="Ej: otro@correo.com" style="padding:9px 12px;border:1px solid #ddd;border-radius:6px;font-size:14px">
        </div>
        <button onclick="agregarCorreo()" style="padding:9px 18px;background:#1a1a1a;color:#fff;border:none;border-radius:6px;font-size:13px;font-weight:600;cursor:pointer;letter-spacing:1px">+ AGREGAR</button>
      </div>
      <div id="correo-msg" style="font-size:13px;margin-top:8px;min-height:18px"></div>
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

      var ESTADO_ENVIO_LABEL = {
        'PREPARANDO':  { label: 'Preparando',   color: '#f57c00', bg: '#fff3e0' },
        'EN_TRANSITO': { label: 'En tránsito',  color: '#1565c0', bg: '#e3f2fd' },
        'ENTREGADO':   { label: 'Entregado',     color: '#2e7d32', bg: '#e8f5e9' },
        'DEVUELTO':    { label: 'Devuelto',      color: '#c62828', bg: '#ffebee' }
      };

      function envioEstadoBadge(estado) {
        var e = ESTADO_ENVIO_LABEL[estado] || { label: estado, color: '#555', bg: '#f0f0f0' };
        return '<span style="display:inline-block;padding:2px 8px;border-radius:20px;font-size:11px;font-weight:600;color:' + e.color + ';background:' + e.bg + '">' + e.label + '</span>';
      }

      function renderEnvio(envio) {
        if (!envio) return '';
        var html = '<div style="margin-top:14px;padding:12px 14px;background:#f8f8f8;border-radius:6px;border-left:3px solid #1a1a1a">';
        html += '<div style="display:flex;align-items:center;gap:8px;margin-bottom:10px">';
        html += '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#1a1a1a" stroke-width="2"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>';
        html += '<span style="font-size:13px;font-weight:700;letter-spacing:.5px;color:#1a1a1a">INFORMACIÓN DE ENVÍO</span>';
        if (envio.estado) html += ' ' + envioEstadoBadge(envio.estado);
        html += '</div>';
        html += '<div style="display:grid;grid-template-columns:1fr 1fr;gap:6px 20px;font-size:13px">';
        if (envio.transportadora) {
          html += '<div><span style="color:#888;font-size:12px">Transportadora</span><br><span style="font-weight:600">' + envio.transportadora + '</span></div>';
        }
        if (envio.guia) {
          html += '<div><span style="color:#888;font-size:12px">N° Guía</span><br><span style="font-weight:600">' + envio.guia + '</span></div>';
        }
        if (envio.fechaEnvio) {
          html += '<div><span style="color:#888;font-size:12px">Fecha de envío</span><br><span style="font-weight:600">' + envio.fechaEnvio + '</span></div>';
        }
        html += '<div><span style="color:#888;font-size:12px">Entrega estimada</span><br>';
        if (envio.fechaEstimada) {
          html += '<span style="font-weight:700;color:#1565c0;font-size:14px">' + envio.fechaEstimada + '</span>';
        } else {
          html += '<span style="color:#aaa">No definida</span>';
        }
        html += '</div>';
        html += '</div></div>';
        return html;
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
            +   renderEnvio(p.envio)
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

      // ── Teléfonos y Correos (RF02/RF03) ─────────────────────────────────
      var TIPO_LABEL = { CELULAR: 'Celular', FIJO: 'Fijo', TRABAJO: 'Trabajo' };

      function renderTelefonos(lista) {
        var el = document.getElementById('tel-lista');
        if (!el) return;
        if (!lista || lista.length === 0) {
          el.innerHTML = '<p style="color:#999;font-size:13px">Sin teléfonos adicionales registrados.</p>';
          return;
        }
        el.innerHTML = lista.map(function(t) {
          return '<div style="display:flex;justify-content:space-between;align-items:center;padding:10px 14px;background:#f8f8f8;border-radius:6px;margin-bottom:6px;font-size:14px">' +
            '<span>' + t.numero + ' <span style="color:#999;font-size:12px">(' + (TIPO_LABEL[t.tipo] || t.tipo) + ')</span></span>' +
            '<button onclick="eliminarTelefono(' + t.id + ')" style="background:none;border:none;color:#c62828;cursor:pointer;font-size:18px;line-height:1;padding:2px 6px" title="Eliminar">&#10005;</button>' +
            '</div>';
        }).join('');
      }

      function renderCorreos(lista) {
        var el = document.getElementById('correo-lista');
        if (!el) return;
        if (!lista || lista.length === 0) {
          el.innerHTML = '<p style="color:#999;font-size:13px">Sin correos adicionales registrados.</p>';
          return;
        }
        el.innerHTML = lista.map(function(c) {
          return '<div style="display:flex;justify-content:space-between;align-items:center;padding:10px 14px;background:#f8f8f8;border-radius:6px;margin-bottom:6px;font-size:14px">' +
            '<span>' + c.correo + (c.principal ? ' <span style="color:#2e7d32;font-size:11px;font-weight:600">(PRINCIPAL)</span>' : '') + '</span>' +
            (!c.principal ? '<button onclick="eliminarCorreo(' + c.id + ')" style="background:none;border:none;color:#c62828;cursor:pointer;font-size:18px;line-height:1;padding:2px 6px" title="Eliminar">&#10005;</button>' : '') +
            '</div>';
        }).join('');
      }

      function cargarContactos() {
        fetch(ctx + '/SvContactoCliente', { credentials: 'same-origin' })
          .then(function(r) { return r.json(); })
          .then(function(d) {
            if (d.error) {
              var telEl = document.getElementById('tel-lista');
              var corEl = document.getElementById('correo-lista');
              if (telEl) telEl.innerHTML = '<p style="color:#c62828;font-size:13px">No se pudo cargar la información de contacto.</p>';
              if (corEl) corEl.innerHTML = '';
              return;
            }
            renderTelefonos(d.telefonos);
            renderCorreos(d.correos);
          }).catch(function() {
            var telEl = document.getElementById('tel-lista');
            if (telEl) telEl.innerHTML = '<p style="color:#999;font-size:13px">Sin conexión con el servidor.</p>';
          });
      }

      function showToast(msg, ok) {
        var t = document.createElement('div');
        var color = ok ? '#2e7d32' : '#c62828';
        var bg    = ok ? '#e8f5e9' : '#ffebee';
        t.style.cssText = 'position:fixed;bottom:24px;right:24px;z-index:9999;padding:14px 22px;border-radius:8px;border-left:4px solid ' + color + ';background:' + bg + ';color:' + color + ';font-size:14px;font-weight:600;box-shadow:0 4px 16px rgba(0,0,0,.12);animation:slideIn .3s ease;max-width:320px';
        t.textContent = msg;
        document.body.appendChild(t);
        setTimeout(function() { t.style.opacity='0'; t.style.transition='opacity .4s'; setTimeout(function(){ t.remove(); }, 400); }, 3500);
      }

      window.abrirEditarDir = function() {
        var f = document.getElementById('dir-form');
        var input = document.getElementById('dir-input');
        input.value = document.getElementById('dir-texto').textContent.trim();
        f.style.display = 'block';
        input.focus();
      };
      window.cerrarEditarDir = function() {
        document.getElementById('dir-form').style.display = 'none';
        document.getElementById('dir-msg').textContent = '';
      };
      window.guardarDireccion = function() {
        var val = (document.getElementById('dir-input').value || '').trim();
        var msg = document.getElementById('dir-msg');
        if (!val) { msg.textContent = 'La dirección no puede estar vacía.'; msg.style.color = '#c62828'; return; }
        var body = new URLSearchParams({ tipo: 'direccion', direccion: val });
        fetch(ctx + '/SvContactoCliente', { method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: body.toString() })
          .then(function(r) { return r.json(); })
          .then(function(d) {
            if (d.error) { msg.textContent = d.error; msg.style.color = '#c62828'; }
            else {
              document.getElementById('dir-texto').textContent = val;
              cerrarEditarDir();
              showToast('✓ Dirección actualizada correctamente', true);
            }
          }).catch(function() { msg.textContent = 'Error al guardar.'; msg.style.color = '#c62828'; });
      };

      window.agregarTelefono = function() {
        var num  = (document.getElementById('tel-nuevo-numero').value || '').trim();
        var tipo = document.getElementById('tel-nuevo-tipo').value;
        var msg  = document.getElementById('tel-msg');
        if (!num) { msg.textContent = 'Ingresa un número.'; msg.style.color = '#c62828'; return; }
        msg.textContent = 'Guardando...'; msg.style.color = '#888';
        var body = new URLSearchParams({ tipo: 'telefono', numero: num, tipoTel: tipo });
        fetch(ctx + '/SvContactoCliente', { method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: body.toString() })
          .then(function(r) { return r.json(); })
          .then(function(d) {
            if (d.error) { msg.textContent = d.error; msg.style.color = '#c62828'; }
            else {
              msg.textContent = '';
              document.getElementById('tel-nuevo-numero').value = '';
              cargarContactos();
              showToast('✓ Teléfono guardado exitosamente', true);
            }
          }).catch(function(e) { msg.textContent = 'Error al guardar.'; msg.style.color = '#c62828'; });
      }

      function showConfirmPerfil(mensaje, onConfirm) {
        var existing = document.getElementById('perfil-confirm-modal');
        if (existing) existing.remove();
        var modal = document.createElement('div');
        modal.id = 'perfil-confirm-modal';
        modal.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,0.5);z-index:9999;display:flex;align-items:center;justify-content:center;padding:16px';
        modal.innerHTML =
          '<div style="background:#fff;border-radius:10px;padding:28px 24px;max-width:340px;width:100%;text-align:center;box-shadow:0 8px 40px rgba(0,0,0,.2)">' +
            '<div style="font-size:34px;margin-bottom:10px">🗑️</div>' +
            '<p style="color:#1a1a1a;font-size:14px;font-weight:600;margin:0 0 6px">' + mensaje + '</p>' +
            '<p style="color:#888;font-size:13px;margin:0 0 20px">Esta acción no se puede deshacer.</p>' +
            '<div style="display:flex;gap:10px;justify-content:center">' +
              '<button id="perfil-confirm-ok" style="background:#c62828;color:#fff;border:none;padding:9px 22px;border-radius:6px;font-size:13px;font-weight:700;cursor:pointer">ELIMINAR</button>' +
              '<button id="perfil-confirm-cancel" style="background:#fff;color:#333;border:1px solid #ccc;padding:9px 18px;border-radius:6px;font-size:13px;cursor:pointer">Cancelar</button>' +
            '</div>' +
          '</div>';
        document.body.appendChild(modal);
        modal.querySelector('#perfil-confirm-ok').addEventListener('click', function() { modal.remove(); onConfirm(); });
        modal.querySelector('#perfil-confirm-cancel').addEventListener('click', function() { modal.remove(); });
        modal.addEventListener('click', function(e) { if (e.target === modal) modal.remove(); });
      }

      window.eliminarTelefono = function(id) {
        showConfirmPerfil('¿Eliminar este teléfono?', function() {
          var body = new URLSearchParams({ tipo: 'telefono', accion: 'eliminar', id: id });
          fetch(ctx + '/SvContactoCliente', { method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: body.toString() })
            .then(function(r) { return r.json(); })
            .then(function(d) { if (!d.error) { cargarContactos(); showToast('Teléfono eliminado', true); } else showToast(d.error, false); })
            .catch(function() {});
        });
      }

      window.agregarCorreo = function() {
        var correo = (document.getElementById('correo-nuevo').value || '').trim();
        var msg    = document.getElementById('correo-msg');
        if (!correo) { msg.textContent = 'Ingresa un correo.'; msg.style.color = '#c62828'; return; }
        msg.textContent = 'Guardando...'; msg.style.color = '#888';
        var body = new URLSearchParams({ tipo: 'correo', correo: correo });
        fetch(ctx + '/SvContactoCliente', { method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: body.toString() })
          .then(function(r) { return r.json(); })
          .then(function(d) {
            if (d.error) { msg.textContent = d.error; msg.style.color = '#c62828'; }
            else {
              msg.textContent = '';
              document.getElementById('correo-nuevo').value = '';
              cargarContactos();
              showToast('✓ Correo guardado exitosamente', true);
            }
          }).catch(function(e) { msg.textContent = 'Error al guardar.'; msg.style.color = '#c62828'; });
      }

      window.eliminarCorreo = function(id) {
        showConfirmPerfil('¿Eliminar este correo adicional?', function() {
          var body = new URLSearchParams({ tipo: 'correo', accion: 'eliminar', id: id });
          fetch(ctx + '/SvContactoCliente', { method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: body.toString() })
            .then(function(r) { return r.json(); })
            .then(function(d) { if (!d.error) { cargarContactos(); showToast('Correo eliminado', true); } else showToast(d.error, false); })
            .catch(function() {});
        });
      }

      cargarContactos();

      // ── Cambiar Contraseña ─────────────────────────────────────────────────
      (function() {
        var STRENGTH_COLORS = ['', '#c62828', '#f57c00', '#f9a825', '#2e7d32'];
        var STRENGTH_LABELS = ['', 'Muy d\u00e9bil', 'D\u00e9bil', 'Aceptable', 'Fuerte'];

        function cpStrength(p) {
          var s = 0;
          if (p.length >= 8)  s++;
          if (p.length >= 12) s++;
          if (/[A-Z]/.test(p) && /[a-z]/.test(p)) s++;
          if (/[0-9]/.test(p)) s++;
          if (/[^a-zA-Z0-9]/.test(p)) s++;
          return Math.min(4, Math.max(1, s <= 1 ? 1 : s === 2 ? 2 : s === 3 ? 3 : 4));
        }

        function cpUpdateBar(p) {
          var spans = document.getElementById('cp-strength-bar').querySelectorAll('span');
          var lbl   = document.getElementById('cp-strength-label');
          if (!p) { spans.forEach(function(s){ s.style.background='#e0e0e0'; }); lbl.textContent=''; return; }
          var lvl = cpStrength(p);
          spans.forEach(function(s, i){ s.style.background = i < lvl ? STRENGTH_COLORS[lvl] : '#e0e0e0'; });
          lbl.textContent = 'Fortaleza: ' + STRENGTH_LABELS[lvl];
        }

        function cpSetErr(elId, msg) {
          var el = document.getElementById(elId);
          if (msg) { el.textContent = msg; el.style.display = 'block'; return false; }
          else { el.textContent = ''; el.style.display = 'none'; return true; }
        }

        function cpValidateNueva() {
          var v = document.getElementById('cp-nueva').value;
          cpUpdateBar(v);
          if (!v) return cpSetErr('cp-err-nueva', 'Ingresa la nueva contrase\u00f1a.');
          if (v.length < 8) return cpSetErr('cp-err-nueva', 'M\u00ednimo 8 caracteres.');
          if (v.length > 20) return cpSetErr('cp-err-nueva', 'M\u00e1ximo 20 caracteres.');
          if (!/[a-zA-Z]/.test(v) || !/[0-9]/.test(v)) return cpSetErr('cp-err-nueva', 'Debe contener al menos una letra y un n\u00famero.');
          return cpSetErr('cp-err-nueva', '');
        }

        function cpValidateConfirmar() {
          var p1 = document.getElementById('cp-nueva').value;
          var p2 = document.getElementById('cp-confirmar').value;
          if (!p2) return cpSetErr('cp-err-confirmar', 'Confirma la nueva contrase\u00f1a.');
          if (p1 !== p2) return cpSetErr('cp-err-confirmar', 'Las contrase\u00f1as no coinciden.');
          return cpSetErr('cp-err-confirmar', '');
        }

        document.getElementById('cp-nueva').addEventListener('input', function() { cpValidateNueva(); cpValidateConfirmar(); });
        document.getElementById('cp-confirmar').addEventListener('input', cpValidateConfirmar);
        document.getElementById('cp-actual').addEventListener('input', function() { cpSetErr('cp-err-actual', ''); });

        window.guardarContrasena = function() {
          var actual    = document.getElementById('cp-actual').value;
          var msg       = document.getElementById('cp-msg');
          msg.textContent = '';

          var ok = true;
          if (!actual) { cpSetErr('cp-err-actual', 'Ingresa tu contrase\u00f1a actual.'); ok = false; } else cpSetErr('cp-err-actual', '');
          if (!cpValidateNueva())    ok = false;
          if (!cpValidateConfirmar()) ok = false;
          if (!ok) return;

          var nueva = document.getElementById('cp-nueva').value;
          var conf  = document.getElementById('cp-confirmar').value;
          msg.textContent = 'Guardando...'; msg.style.color = '#888';

          var body = new URLSearchParams({ tipo: 'cambiarContrasena', actual: actual, nueva: nueva, confirmar: conf });
          fetch(ctx + '/SvContactoCliente', { method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: body.toString() })
            .then(function(r) { return r.json(); })
            .then(function(d) {
              if (d.error) { msg.textContent = d.error; msg.style.color = '#c62828'; }
              else {
                document.getElementById('cp-actual').value = '';
                document.getElementById('cp-nueva').value = '';
                document.getElementById('cp-confirmar').value = '';
                cpUpdateBar('');
                showToast('\u2713 Contrase\u00f1a cambiada exitosamente', true);
                msg.textContent = '';
              }
            }).catch(function() { msg.textContent = 'Error de conexi\u00f3n. Intenta de nuevo.'; msg.style.color = '#c62828'; });
        };
      })();

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

