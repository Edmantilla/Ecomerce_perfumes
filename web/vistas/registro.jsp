<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="logica.Usuario" %>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/estilos/style.css">
    <title>Crear Cuenta - ANDREYLPZ</title>
    <style>
        .field-error {
            display: none;
            color: #c62828;
            font-size: 12px;
            margin-top: 4px;
            margin-bottom: 2px;
        }
        .field-error.visible { display: block; }
        .input-invalid {
            border-color: #c62828 !important;
            box-shadow: 0 0 0 2px rgba(198,40,40,.15) !important;
        }
        .input-valid {
            border-color: #2e7d32 !important;
            box-shadow: 0 0 0 2px rgba(46,125,50,.12) !important;
        }
        .pass-strength {
            display: flex;
            gap: 4px;
            margin-top: 6px;
            height: 4px;
        }
        .pass-strength span {
            flex: 1;
            border-radius: 2px;
            background: #e0e0e0;
            transition: background .3s;
        }
        .pass-strength.s1 span:nth-child(1)                        { background: #c62828; }
        .pass-strength.s2 span:nth-child(-n+2)                     { background: #f57c00; }
        .pass-strength.s3 span:nth-child(-n+3)                     { background: #f9a825; }
        .pass-strength.s4 span                                     { background: #2e7d32; }
        .pass-strength-label {
            font-size: 11px;
            margin-top: 3px;
            color: #888;
        }
        .registro-server-error {
            background: #fdecea;
            border: 1px solid #e57373;
            border-radius: 8px;
            color: #c62828;
            padding: 10px 14px;
            font-size: 14px;
            text-align: center;
            margin-bottom: 16px;
        }
    </style>
</head>

<body data-no-cart>
    <%@ include file="_navbar.jsp" %>

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
              <div class="registro-server-error"><%= errorReg %></div>
            <% } %>

            <form id="form-registro" method="post" action="<%= request.getContextPath() %>/SvRegistro" novalidate>

                <div class="formulario-username">
                    <label for="nombre">Nombre</label>
                    <input type="text" id="nombre" name="nombre" placeholder="Tu nombre" autocomplete="given-name">
                    <span class="field-error" id="err-nombre"></span>
                </div>

                <div class="formulario-username">
                    <label for="apellido">Apellido</label>
                    <input type="text" id="apellido" name="apellido" placeholder="Tu apellido" autocomplete="family-name">
                    <span class="field-error" id="err-apellido"></span>
                </div>

                <div class="formulario-username">
                    <label for="correo">Correo Electrónico</label>
                    <input type="email" id="correo" name="correo_electronico" placeholder="ejemplo@correo.com" autocomplete="email">
                    <span class="field-error" id="err-correo"></span>
                </div>

                <div class="formulario-contrasena">
                    <label for="contrasena">Contraseña</label>
                    <input type="password" id="contrasena" name="contrasena" placeholder="Entre 8 y 20 caracteres" autocomplete="new-password" maxlength="20">
                    <div class="pass-strength" id="pass-strength-bar">
                        <span></span><span></span><span></span><span></span>
                    </div>
                    <div class="pass-strength-label" id="pass-strength-label"></div>
                    <span class="field-error" id="err-contrasena"></span>
                </div>

                <div class="formulario-contrasena">
                    <label for="confirmar_contrasena">Confirmar Contraseña</label>
                    <input type="password" id="confirmar_contrasena" name="confirmar_contrasena" placeholder="Repite tu contraseña" autocomplete="new-password">
                    <span class="field-error" id="err-confirmar"></span>
                </div>

                <div class="formulario-username">
                    <label for="fecha_nacimiento">Fecha de Nacimiento</label>
                    <input type="date" id="fecha_nacimiento" name="fecha_nacimiento" min="1900-01-01">
                    <span class="field-error" id="err-fecha"></span>
                </div>

                <div class="formulario-username">
                    <label for="direccion">Dirección</label>
                    <input type="text" id="direccion" name="direccion" placeholder="Calle, carrera, barrio, ciudad" autocomplete="street-address">
                    <span class="field-error" id="err-direccion"></span>
                </div>

                <div class="formulario-checkbox">
                    <input type="checkbox" id="terminos" name="terminos">
                    <label for="terminos">He leído y acepto los
                        <a href="#" class="formulario-link">Términos y Condiciones</a>
                        de ANDREYLPZ.</label>
                </div>
                <span class="field-error" id="err-terminos"></span>

                <div class="formulario-checkbox">
                    <input type="checkbox" id="tratamiento_datos" name="tratamiento_datos">
                    <label for="tratamiento_datos">Doy mi consentimiento para el
                        <a href="#" class="formulario-link">Tratamiento de mis Datos Personales</a>
                        conforme a la política de privacidad de ANDREYLPZ.</label>
                </div>
                <span class="field-error" id="err-datos"></span>

                <button class="formulario-button" type="submit" id="btn-registro">CREAR CUENTA</button>

                <div class="formulario-registrarse">
                    <a class="formulario-registrarse" href="perfil.jsp">¿Ya tienes una cuenta? Inicia sesión</a>
                </div>

            </form>
        </section>
    </main>

    <%@ include file="_footer.jsp" %>

    <script src="<%= request.getContextPath() %>/assets/scripts/cart.js"></script>
    <script>
    (function() {
        // Fecha máxima: hace exactamente 18 años desde hoy
        var hoy = new Date();
        var maxFecha = new Date(hoy.getFullYear() - 18, hoy.getMonth(), hoy.getDate());
        var yyyy = maxFecha.getFullYear();
        var mm   = String(maxFecha.getMonth() + 1).padStart(2, '0');
        var dd   = String(maxFecha.getDate()).padStart(2, '0');
        document.getElementById('fecha_nacimiento').setAttribute('max', yyyy + '-' + mm + '-' + dd);

        var SOLO_LETRAS = /^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]{2,50}$/;
        var CORREO_RE   = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;

        function setError(inputId, errorId, msg) {
            var inp = document.getElementById(inputId);
            var err = document.getElementById(errorId);
            if (msg) {
                inp && inp.classList.add('input-invalid');
                inp && inp.classList.remove('input-valid');
                err.textContent = msg;
                err.classList.add('visible');
                return false;
            } else {
                inp && inp.classList.remove('input-invalid');
                inp && inp.classList.add('input-valid');
                err.textContent = '';
                err.classList.remove('visible');
                return true;
            }
        }

        function setCheckError(errorId, msg) {
            var err = document.getElementById(errorId);
            if (msg) { err.textContent = msg; err.classList.add('visible'); return false; }
            else { err.textContent = ''; err.classList.remove('visible'); return true; }
        }

        function validateNombre() {
            var v = document.getElementById('nombre').value.trim();
            if (!v) return setError('nombre', 'err-nombre', 'El nombre es obligatorio.');
            if (!SOLO_LETRAS.test(v)) return setError('nombre', 'err-nombre', 'Solo letras, mínimo 2 caracteres.');
            return setError('nombre', 'err-nombre', '');
        }

        function validateApellido() {
            var v = document.getElementById('apellido').value.trim();
            if (!v) return setError('apellido', 'err-apellido', 'El apellido es obligatorio.');
            if (!SOLO_LETRAS.test(v)) return setError('apellido', 'err-apellido', 'Solo letras, mínimo 2 caracteres.');
            return setError('apellido', 'err-apellido', '');
        }

        function validateCorreo() {
            var v = document.getElementById('correo').value.trim();
            if (!v) return setError('correo', 'err-correo', 'El correo es obligatorio.');
            if (!CORREO_RE.test(v)) return setError('correo', 'err-correo', 'Ingresa un correo válido (ej: nombre@dominio.com).');
            return setError('correo', 'err-correo', '');
        }

        function getPasswordStrength(p) {
            var score = 0;
            if (p.length >= 8)  score++;
            if (p.length >= 12) score++;
            if (/[A-Z]/.test(p) && /[a-z]/.test(p)) score++;
            if (/[0-9]/.test(p)) score++;
            if (/[^a-zA-Z0-9]/.test(p)) score++;
            if (score <= 1) return 1;
            if (score === 2) return 2;
            if (score === 3) return 3;
            return 4;
        }

        var LABELS = ['', 'Muy débil', 'Débil', 'Aceptable', 'Fuerte'];
        function updateStrengthBar(p) {
            var bar   = document.getElementById('pass-strength-bar');
            var label = document.getElementById('pass-strength-label');
            if (!p) { bar.className = 'pass-strength'; label.textContent = ''; return; }
            var s = getPasswordStrength(p);
            bar.className = 'pass-strength s' + s;
            label.textContent = 'Fortaleza: ' + LABELS[s];
        }

        function validateContrasena() {
            var v = document.getElementById('contrasena').value;
            updateStrengthBar(v);
            if (!v) return setError('contrasena', 'err-contrasena', 'La contraseña es obligatoria.');
            if (v.length < 8) return setError('contrasena', 'err-contrasena', 'La contraseña debe tener mínimo 8 caracteres.');
            if (v.length > 20) return setError('contrasena', 'err-contrasena', 'La contraseña no puede superar los 20 caracteres.');
            if (!/[a-zA-Z]/.test(v) || !/[0-9]/.test(v)) return setError('contrasena', 'err-contrasena', 'Debe contener al menos una letra y un número.');
            var ok = setError('contrasena', 'err-contrasena', '');
            validateConfirmar();
            return ok;
        }

        function validateConfirmar() {
            var p1 = document.getElementById('contrasena').value;
            var p2 = document.getElementById('confirmar_contrasena').value;
            if (!p2) return setError('confirmar_contrasena', 'err-confirmar', 'Confirma tu contraseña.');
            if (p1 !== p2) return setError('confirmar_contrasena', 'err-confirmar', 'Las contraseñas no coinciden.');
            return setError('confirmar_contrasena', 'err-confirmar', '');
        }

        function validateFecha() {
            var v = document.getElementById('fecha_nacimiento').value;
            if (!v) return setError('fecha_nacimiento', 'err-fecha', 'La fecha de nacimiento es obligatoria.');
            var nac  = new Date(v + 'T00:00:00');
            var hoy2 = new Date();
            if (nac > hoy2) return setError('fecha_nacimiento', 'err-fecha', 'La fecha no puede ser futura.');
            var edad = hoy2.getFullYear() - nac.getFullYear();
            var m = hoy2.getMonth() - nac.getMonth();
            if (m < 0 || (m === 0 && hoy2.getDate() < nac.getDate())) edad--;
            if (edad < 18) return setError('fecha_nacimiento', 'err-fecha', 'Debes tener al menos 18 años.');
            if (edad > 120) return setError('fecha_nacimiento', 'err-fecha', 'La fecha no es válida.');
            return setError('fecha_nacimiento', 'err-fecha', '');
        }

        function validateDireccion() {
            var v = document.getElementById('direccion').value.trim();
            if (!v) return setError('direccion', 'err-direccion', 'La dirección es obligatoria.');
            if (v.length < 10) return setError('direccion', 'err-direccion', 'La dirección debe ser más específica (mín. 10 caracteres).');
            return setError('direccion', 'err-direccion', '');
        }

        // Bind blur/input para feedback inmediato
        document.getElementById('nombre').addEventListener('blur', validateNombre);
        document.getElementById('nombre').addEventListener('input', validateNombre);
        document.getElementById('apellido').addEventListener('blur', validateApellido);
        document.getElementById('apellido').addEventListener('input', validateApellido);
        document.getElementById('correo').addEventListener('blur', validateCorreo);
        document.getElementById('correo').addEventListener('input', validateCorreo);
        document.getElementById('contrasena').addEventListener('input', validateContrasena);
        document.getElementById('contrasena').addEventListener('blur', validateContrasena);
        document.getElementById('confirmar_contrasena').addEventListener('input', validateConfirmar);
        document.getElementById('confirmar_contrasena').addEventListener('blur', validateConfirmar);
        document.getElementById('fecha_nacimiento').addEventListener('change', validateFecha);
        document.getElementById('fecha_nacimiento').addEventListener('blur', validateFecha);
        document.getElementById('direccion').addEventListener('blur', validateDireccion);
        document.getElementById('direccion').addEventListener('input', validateDireccion);

        document.getElementById('form-registro').addEventListener('submit', function(e) {
            var ok = true;
            if (!validateNombre())    ok = false;
            if (!validateApellido())  ok = false;
            if (!validateCorreo())    ok = false;
            if (!validateContrasena()) ok = false;
            if (!validateConfirmar()) ok = false;
            if (!validateFecha())     ok = false;
            if (!validateDireccion()) ok = false;
            if (!document.getElementById('terminos').checked)
                ok = setCheckError('err-terminos', 'Debes aceptar los Términos y Condiciones.') && ok;
            else setCheckError('err-terminos', '');
            if (!document.getElementById('tratamiento_datos').checked)
                ok = setCheckError('err-datos', 'Debes aceptar el tratamiento de datos personales.') && ok;
            else setCheckError('err-datos', '');
            if (!ok) {
                e.preventDefault();
                var firstErr = document.querySelector('.input-invalid');
                if (firstErr) firstErr.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    })();
    </script>
</body>

</html>









