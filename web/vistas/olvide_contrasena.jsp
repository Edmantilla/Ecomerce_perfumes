<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../assets/estilos/style.css">
    <title>Recuperar Contraseña - ANDREYLPZ</title>
    <style>
        .rc-error   { color: #c0392b; font-size: 13px; margin-top: 8px; text-align: center; }
        .rc-success { color: #27ae60; font-size: 13px; margin-top: 8px; text-align: center; }
        .rc-nombre  { font-weight: 700; color: #1a1a1a; }
        .formulario-icono-ok { font-size: 48px; color: #27ae60; text-align: center; margin-bottom: 12px; }
        .rc-spinner { display: inline-block; width: 16px; height: 16px; border: 2px solid #fff;
                      border-top-color: transparent; border-radius: 50%;
                      animation: rc-spin .6s linear infinite; vertical-align: middle; margin-right: 6px; }
        @keyframes rc-spin { to { transform: rotate(360deg); } }
        .formulario-button:disabled { opacity: .7; cursor: not-allowed; }
    </style>
</head>
<body data-no-cart>
    <%@ include file="_navbar.jsp" %>

    <main class="main-perfil">
        <section class="formulario">
            <h2 class="formulario-title">RECUPERAR CONTRASEÑA</h2>

            <!-- PASO 1: Verificar correo -->
            <div id="paso-correo">
                <p class="formulario-descripcion">
                    Ingresa tu correo electrónico para cambiar tu contraseña.
                </p>
                <form id="form-correo" onsubmit="verificarCorreo(event)">
                    <div class="formulario-username">
                        <label for="correo-input">Correo Electrónico</label>
                        <input type="email" id="correo-input" name="correo"
                               placeholder="ejemplo@correo.com" required autocomplete="email">
                    </div>
                    <div id="error-correo" class="rc-error" style="display:none"></div>
                    <button class="formulario-button" type="submit" id="btn-verificar">CONTINUAR</button>
                    <div class="formulario-registrarse">
                        <a class="formulario-registrarse" href="perfil.jsp">Volver a Iniciar Sesión</a>
                    </div>
                </form>
            </div>

            <!-- PASO 2: Nueva contraseña -->
            <div id="paso-nueva" style="display:none">
                <p class="formulario-descripcion">
                    Hola, <span id="nombre-usuario" class="rc-nombre"></span>. Ingresa tu nueva contraseña.
                </p>
                <form id="form-cambiar" onsubmit="cambiarContrasena(event)" novalidate>
                    <div class="formulario-username">
                        <label for="nueva-input">Nueva Contraseña</label>
                        <input type="password" id="nueva-input" name="nueva"
                               placeholder="Entre 8 y 20 caracteres" minlength="8" maxlength="20">
                        <div id="rc-strength-bar" style="display:flex;gap:4px;height:4px;margin-top:6px">
                            <span style="flex:1;border-radius:2px;background:#e0e0e0;transition:background .3s"></span>
                            <span style="flex:1;border-radius:2px;background:#e0e0e0;transition:background .3s"></span>
                            <span style="flex:1;border-radius:2px;background:#e0e0e0;transition:background .3s"></span>
                            <span style="flex:1;border-radius:2px;background:#e0e0e0;transition:background .3s"></span>
                        </div>
                        <div id="rc-strength-label" style="font-size:11px;color:#888;margin-top:3px"></div>
                        <span id="rc-err-nueva" class="rc-error" style="display:none;text-align:left"></span>
                    </div>
                    <div class="formulario-username" style="margin-top:12px">
                        <label for="confirmar-input">Confirmar Contraseña</label>
                        <input type="password" id="confirmar-input" name="confirmar"
                               placeholder="Repite la contraseña" minlength="8" maxlength="20">
                        <span id="rc-err-confirmar" class="rc-error" style="display:none;text-align:left"></span>
                    </div>
                    <div id="error-cambiar" class="rc-error" style="display:none"></div>
                    <button class="formulario-button" type="submit" id="btn-cambiar">CAMBIAR CONTRASEÑA</button>
                    <div class="formulario-registrarse">
                        <a class="formulario-registrarse" href="#" onclick="volverPaso1(event)">Usar otro correo</a>
                    </div>
                </form>
            </div>

            <!-- PASO 3: Éxito -->
            <div id="paso-exito" style="display:none; text-align:center">
                <div class="formulario-icono-ok">✓</div>
                <p class="formulario-descripcion">
                    ¡Tu contraseña fue cambiada exitosamente!<br>
                    Ya puedes iniciar sesión con tu nueva contraseña.
                </p>
                <a class="formulario-button" href="perfil.jsp"
                   style="display:inline-block;margin-top:12px;text-decoration:none">IR AL LOGIN</a>
            </div>

        </section>
    </main>

    <%@ include file="_footer.jsp" %>

    <script>
    (function () {
        var BASE = (function () {
            var p = window.location.pathname.split('/');
            return '/' + p[1];
        })();
        var correoVerificado = '';

        function setLoading(btn, loading) {
            if (loading) {
                btn.disabled = true;
                btn.innerHTML = '<span class="rc-spinner"></span>Verificando...';
            } else {
                btn.disabled = false;
                btn.innerHTML = btn.dataset.label || btn.innerHTML;
            }
        }

        window.verificarCorreo = function (e) {
            e.preventDefault();
            var correo = document.getElementById('correo-input').value.trim();
            var errEl  = document.getElementById('error-correo');
            var btn    = document.getElementById('btn-verificar');
            errEl.style.display = 'none';
            btn.dataset.label = 'CONTINUAR';
            setLoading(btn, true);

            var body = new URLSearchParams({ accion: 'verificar', correo: correo });
            fetch(BASE + '/SvRecuperarContrasena', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body.toString()
            })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                setLoading(btn, false);
                btn.textContent = 'CONTINUAR';
                if (data.error) {
                    errEl.textContent = data.error;
                    errEl.style.display = 'block';
                    return;
                }
                correoVerificado = correo;
                document.getElementById('nombre-usuario').textContent = data.nombre;
                document.getElementById('paso-correo').style.display = 'none';
                document.getElementById('paso-nueva').style.display  = 'block';
            })
            .catch(function () {
                setLoading(btn, false);
                btn.textContent = 'CONTINUAR';
                errEl.textContent = 'Error de conexión. Intenta de nuevo.';
                errEl.style.display = 'block';
            });
        };

        // ── Fortaleza y validación cliente ──
        var RC_STRENGTH_COLORS = ['', '#c62828', '#f57c00', '#f9a825', '#2e7d32'];
        var RC_STRENGTH_LABELS = ['', 'Muy débil', 'Débil', 'Aceptable', 'Fuerte'];

        function rcStrength(p) {
            var s = 0;
            if (p.length >= 8)  s++;
            if (p.length >= 12) s++;
            if (/[A-Z]/.test(p) && /[a-z]/.test(p)) s++;
            if (/[0-9]/.test(p)) s++;
            if (/[^a-zA-Z0-9]/.test(p)) s++;
            return Math.min(4, Math.max(1, s <= 1 ? 1 : s === 2 ? 2 : s === 3 ? 3 : 4));
        }

        function rcUpdateBar(p) {
            var spans = document.getElementById('rc-strength-bar').querySelectorAll('span');
            var lbl   = document.getElementById('rc-strength-label');
            if (!p) { spans.forEach(function(s){ s.style.background='#e0e0e0'; }); lbl.textContent=''; return; }
            var lvl = rcStrength(p);
            spans.forEach(function(s, i){ s.style.background = i < lvl ? RC_STRENGTH_COLORS[lvl] : '#e0e0e0'; });
            lbl.textContent = 'Fortaleza: ' + RC_STRENGTH_LABELS[lvl];
        }

        function rcSetErr(id, msg) {
            var el = document.getElementById(id);
            if (msg) { el.textContent = msg; el.style.display = 'block'; return false; }
            else { el.textContent = ''; el.style.display = 'none'; return true; }
        }

        function rcValidateNueva() {
            var v = document.getElementById('nueva-input').value;
            rcUpdateBar(v);
            if (!v) return rcSetErr('rc-err-nueva', 'Ingresa la nueva contraseña.');
            if (v.length < 8) return rcSetErr('rc-err-nueva', 'Mínimo 8 caracteres.');
            if (v.length > 20) return rcSetErr('rc-err-nueva', 'Máximo 20 caracteres.');
            if (!/[a-zA-Z]/.test(v) || !/[0-9]/.test(v)) return rcSetErr('rc-err-nueva', 'Debe contener al menos una letra y un número.');
            return rcSetErr('rc-err-nueva', '');
        }

        function rcValidateConfirmar() {
            var p2 = document.getElementById('confirmar-input').value;
            var p1 = document.getElementById('nueva-input').value;
            if (!p2) return rcSetErr('rc-err-confirmar', 'Confirma la contraseña.');
            if (p1 !== p2) return rcSetErr('rc-err-confirmar', 'Las contraseñas no coinciden.');
            return rcSetErr('rc-err-confirmar', '');
        }

        document.getElementById('nueva-input').addEventListener('input', function() { rcValidateNueva(); rcValidateConfirmar(); });
        document.getElementById('confirmar-input').addEventListener('input', rcValidateConfirmar);

        window.cambiarContrasena = function (e) {
            e.preventDefault();
            var nueva     = document.getElementById('nueva-input').value;
            var confirmar = document.getElementById('confirmar-input').value;
            var errEl     = document.getElementById('error-cambiar');
            var btn       = document.getElementById('btn-cambiar');
            errEl.style.display = 'none';

            var ok = true;
            if (!rcValidateNueva())    ok = false;
            if (!rcValidateConfirmar()) ok = false;
            if (!ok) return;
            btn.dataset.label = 'CAMBIAR CONTRASEÑA';
            btn.disabled = true;
            btn.innerHTML = '<span class="rc-spinner"></span>Guardando...';

            var body = new URLSearchParams({
                accion: 'cambiar',
                correo: correoVerificado,
                nueva: nueva,
                confirmar: confirmar
            });
            fetch(BASE + '/SvRecuperarContrasena', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body.toString()
            })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                btn.disabled = false;
                btn.textContent = 'CAMBIAR CONTRASEÑA';
                if (data.error) {
                    errEl.textContent = data.error;
                    errEl.style.display = 'block';
                    return;
                }
                document.getElementById('paso-nueva').style.display = 'none';
                document.getElementById('paso-exito').style.display = 'block';
            })
            .catch(function () {
                btn.disabled = false;
                btn.textContent = 'CAMBIAR CONTRASEÑA';
                errEl.textContent = 'Error de conexión. Intenta de nuevo.';
                errEl.style.display = 'block';
            });
        };

        window.volverPaso1 = function (e) {
            e.preventDefault();
            correoVerificado = '';
            document.getElementById('correo-input').value = '';
            document.getElementById('nueva-input').value = '';
            document.getElementById('confirmar-input').value = '';
            document.getElementById('error-correo').style.display  = 'none';
            document.getElementById('error-cambiar').style.display = 'none';
            document.getElementById('paso-nueva').style.display  = 'none';
            document.getElementById('paso-correo').style.display = 'block';
        };
    })();
    </script>

    <script src="../assets/scripts/cart.js"></script>
</body>
</html>









