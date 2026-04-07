# FLUJO DE ARQUITECTURA — Cómo se conectan Controladores, Servlets y JSP

Este archivo explica paso a paso cómo fluye una petición desde que el usuario
hace clic en el navegador hasta que los datos se guardan en MySQL y regresan
a la pantalla. Se usan ejemplos reales del código del proyecto.

---

## 1. VISIÓN GENERAL — Las 4 capas del proyecto

```
NAVEGADOR (Chrome/Firefox)
      │  El usuario hace clic o envía un formulario
      ▼
JSP (web/vistas/)
      │  Genera el HTML que ve el usuario.
      │  Hace peticiones al servidor con fetch() de JavaScript.
      ▼
SERVLET (src/java/servlets/)
      │  Recibe la petición HTTP.
      │  Contiene la lógica de negocio.
      │  Usa los controladores para hablar con la BD.
      ▼
CONTROLADOR JPA (src/java/persistencias/)
      │  Ejecuta el SQL nativo contra MySQL.
      │  Usa el EntityManager para gestionar la conexión.
      ▼
BASE DE DATOS MySQL (Perfumeria_andreylpz)
      │  Guarda o retorna los datos.
      └─ Tablas: marca, producto, usuario, ...
```

---

## 2. CÓMO SE CONECTA CADA CAPA

### 2.1 JSP → Servlet (petición HTTP)

El JSP no llama al controlador directamente. El JSP contiene código JavaScript
que usa `fetch()` para hacer peticiones HTTP al servlet. El servlet está
registrado en `web/WEB-INF/web.xml` con una URL.

**Ejemplo real — admin.js llama a SvMarcas:**

```javascript
// admin.js (línea 21)
function apiUrl(servlet) { return BASE + '/' + servlet; }
// BASE = "/Proyecto" (el nombre de la app en Tomcat)
// apiUrl("SvMarcas") → "/Proyecto/SvMarcas"

// Para LEER marcas: hace GET a /Proyecto/SvMarcas
function get(servlet) {
    return fetch(apiUrl(servlet), { credentials: 'same-origin' })
        .then(r => r.json());   // parsea la respuesta JSON del servlet
}

// Para CREAR/EDITAR/ELIMINAR: hace POST a /Proyecto/SvMarcas
function post(servlet, params) {
    const body = new URLSearchParams(params);  // convierte {accion:"crear", nombre:"Versace"} a texto
    return fetch(apiUrl(servlet), {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString()   // envía: "accion=crear&nombre=Versace&genero=HOMBRE"
    }).then(r => r.text())
      .then(txt => JSON.parse(txt));  // el servlet responde JSON → JS lo parsea
}
```

**Cómo web.xml conecta la URL con el Servlet:**

```xml
<!-- web/WEB-INF/web.xml -->
<servlet>
    <servlet-name>SvMarcas</servlet-name>
    <servlet-class>servlets.SvMarcas</servlet-class>   <!-- clase Java a ejecutar -->
</servlet>
<servlet-mapping>
    <servlet-name>SvMarcas</servlet-name>
    <url-pattern>/SvMarcas</url-pattern>   <!-- cuando llegue una petición a /SvMarcas... -->
</servlet-mapping>                          <!-- ...Tomcat ejecuta la clase SvMarcas -->
```

Cuando `fetch("/Proyecto/SvMarcas")` sale del navegador:
1. Tomcat recibe la petición
2. Busca en `web.xml` qué clase corresponde a `/SvMarcas`
3. Ejecuta `servlets.SvMarcas.doGet()` o `doPost()` según el método HTTP

---

### 2.2 Servlet → Controlador (llamada a método Java)

Dentro del servlet, se crea una instancia del controlador y se llama al método:

```java
// SvMarcas.java — doGet() (leer todas las marcas)
List<Marca> lista = new MarcaJpaController().findMarcaEntities();
//                  ↑ crea el controlador     ↑ llama al método que ejecuta:
//                                              SELECT * FROM marca ORDER BY id_marca ASC
```

```java
// SvMarcas.java — doPost() (crear una marca)
MarcaJpaController ctrl = new MarcaJpaController();
Marca marca = new Marca();
marca.setNombreMarca(nombre);       // datos que vinieron del formulario JSP
marca.setDescripcion(descripcion);
marca.setGenero(genero);
marca.setPaginaUrl(nombreJsp);
marca.setActivo(true);
ctrl.create(marca);
// ↑ llama al controlador que ejecuta:
//   INSERT INTO marca (nombre_marca, descripcion, genero, pagina_url, activo)
//   VALUES ("Versace", "Lujo italiano", "HOMBRE", "versace.jsp", 1)
```

```java
// SvMarcas.java — editar
Marca marca = ctrl.findMarca(Integer.parseInt(idStr)); // SELECT * FROM marca WHERE id_marca = ?
marca.setNombreMarca(nombre);   // cambia el valor en el objeto Java
marca.setDescripcion(descripcion);
ctrl.edit(marca);               // UPDATE marca SET nombre_marca=?, descripcion=? WHERE id_marca=?
```

```java
// SvMarcas.java — eliminar
ctrl.destroy(id);
// ↑ DELETE FROM marca WHERE id_marca = ?
```

---

### 2.3 Controlador → EntityManager → MySQL

Dentro del controlador, el `EntityManager` es el objeto que habla directamente
con MySQL. Así se forma la cadena completa:

```
JpaProvider (Singleton)
    └── EntityManagerFactory (una sola instancia en toda la app)
            └── EntityManager (una por operación, se cierra al terminar)
                    └── createNativeQuery("SQL") → MySQL
```

**Código del controlador paso a paso:**

```java
// MarcaJpaController.java — método create()

public void create(Marca marca) throws Exception {

    // 1. Define el SQL que se va a ejecutar en MySQL
    String sql = "INSERT INTO marca (nombre_marca, descripcion, genero, pagina_url, activo) "
               + "VALUES (?1, ?2, ?3, ?4, ?5)";

    EntityManager em = null;       // 2. Declara la variable de sesión en null

    try {
        em = getEntityManager();   // 3. getEntityManager() llama a emf.createEntityManager()
                                   //    Esto abre una conexión desde el pool de EclipseLink a MySQL

        em.getTransaction().begin(); // 4. Inicia la transacción
                                     //    Sin esto, MySQL InnoDB no guardará nada (modo transaccional)

        em.createNativeQuery(sql)    // 5. Prepara el SQL en MySQL (MySQL lo analiza y compila)
          .setParameter(1, marca.getNombreMarca()) // 6. Reemplaza ?1 con "Versace" de forma segura
          .setParameter(2, marca.getDescripcion()) //    Reemplaza ?2 con "Lujo italiano"
          .setParameter(3, marca.getGenero())      //    Reemplaza ?3 con "HOMBRE"
          .setParameter(4, marca.getPaginaUrl())   //    Reemplaza ?4 con "versace.jsp"
          .setParameter(5, marca.isActivo())       //    Reemplaza ?5 con true (→ 1 en MySQL)
          .executeUpdate();          // 7. Envía el INSERT a MySQL. MySQL lo ejecuta y retorna 1 (fila insertada)

        em.getTransaction().commit(); // 8. Confirma la transacción.
                                      //    Los datos quedan guardados permanentemente en disco
    } finally {
        if (em != null) { em.close(); } // 9. SIEMPRE cierra la sesión
                                         //    Devuelve la conexión al pool para que otros la usen
    }
}
```

**Por qué se usa `setParameter()` en lugar de concatenar el string:**

```java
// MAL (vulnerable a SQL Injection):
String sql = "INSERT INTO marca (nombre_marca) VALUES ('" + nombre + "')";
// Si nombre = "'; DROP TABLE marca; --"  → destruye la tabla

// BIEN (setParameter protege):
String sql = "INSERT INTO marca (nombre_marca) VALUES (?1)";
em.createNativeQuery(sql).setParameter(1, nombre).executeUpdate();
// EclipseLink escapa el valor antes de enviarlo a MySQL → seguro
```

---

### 2.4 Servlet → JSP (respuesta JSON)

El servlet nunca le envía HTML al navegador. Le envía **JSON**, y el JavaScript
del JSP usa ese JSON para actualizar la pantalla sin recargar la página.

```java
// SvMarcas.java — doGet() construye la respuesta JSON manualmente
response.setContentType("application/json;charset=UTF-8"); // le dice al navegador: esto es JSON
PrintWriter out = response.getWriter();                     // obtiene el escritor de la respuesta

List<Marca> lista = new MarcaJpaController().findMarcaEntities(); // SELECT * FROM marca

StringBuilder sb = new StringBuilder("[");   // empieza a construir el JSON
for (Marca m : lista) {
    sb.append("{");
    sb.append("\"id\":").append(m.getIdMarca()).append(",");
    sb.append("\"nombre\":\"").append(m.getNombreMarca()).append("\",");
    sb.append("\"activo\":").append(m.isActivo());
    sb.append("}");
}
sb.append("]");
out.print(sb.toString()); // envía al navegador: [{"id":1,"nombre":"Chanel","activo":true}, ...]
```

```javascript
// admin.js — recibe el JSON y lo muestra en la tabla HTML
get('SvMarcas').then(function(marcas) {
    // marcas = [{id:1, nombre:"Chanel", activo:true}, {id:2, nombre:"Dior", activo:false}]
    marcas.forEach(function(m) {
        // crea una fila en la tabla HTML con los datos de cada marca
        tabla.innerHTML += `<tr><td>${m.nombre}</td><td>${m.activo}</td></tr>`;
    });
});
```

---

## 3. FLUJO COMPLETO — Ejemplo: CREAR una marca desde el panel admin

```
1. El admin abre admin.jsp en el navegador
   └── admin.jsp verifica la sesión con código Java embebido (scriptlet):
       Object usuarioSess = session.getAttribute("usuario");
       if (usuarioSess == null) response.sendRedirect("perfil.jsp");

2. El admin llena el formulario de "Nueva Marca" y hace clic en Guardar
   └── admin.js captura el evento del botón y llama a:
       post('SvMarcas', { nombre: "Versace", genero: "HOMBRE", descripcion: "..." })
       └── Esto hace un POST HTTP a /Proyecto/SvMarcas con esos parámetros

3. Tomcat recibe el POST y ejecuta SvMarcas.doPost()
   └── El servlet lee los parámetros:
       String nombre = request.getParameter("nombre");      // "Versace"
       String genero = request.getParameter("genero");      // "HOMBRE"
   └── Verifica el permiso:
       AuthHelper.tienePermiso(request, "GESTIONAR_MARCAS") // lee de la sesión
   └── Crea el objeto Marca:
       Marca marca = new Marca();
       marca.setNombreMarca("Versace");
       marca.setGenero("HOMBRE");
       marca.setActivo(true);
   └── Llama al controlador:
       new MarcaJpaController().create(marca);

4. MarcaJpaController.create(marca) ejecuta el SQL
   └── em = emf.createEntityManager()     → abre sesión con MySQL
   └── em.getTransaction().begin()        → inicia transacción
   └── createNativeQuery(                 → prepara el SQL
         "INSERT INTO marca (nombre_marca, genero, activo)
          VALUES (?1, ?2, ?3)")
     .setParameter(1, "Versace")          → reemplaza ?1
     .setParameter(2, "HOMBRE")           → reemplaza ?2
     .setParameter(3, true)               → reemplaza ?3 → 1
     .executeUpdate()                     → MySQL ejecuta el INSERT
   └── em.getTransaction().commit()       → MySQL confirma y guarda
   └── em.close()                         → cierra la sesión

5. SvMarcas responde al navegador
   └── out.print("{\"ok\":true}")         → envía JSON de éxito

6. admin.js recibe la respuesta
   └── Si ok: cierra el modal y recarga la tabla de marcas
       llamando de nuevo a get('SvMarcas') para ver la nueva fila
```

---

## 4. FLUJO COMPLETO — Ejemplo: LEER productos en una página JSP de marca

```
1. El usuario abre Chanel.jsp en el navegador
   └── Tomcat sirve el archivo JSP estático (HTML + script JS dentro)
   └── El navegador ejecuta el script embebido en el JSP:

       fetch('/Proyecto/SvProductos', { credentials: 'same-origin' })
       └── Hace GET a /Proyecto/SvProductos (sin parámetros)

2. Tomcat ejecuta SvProductos.doGet()
   └── Como NO viene ?admin=true → filtra solo productos activos:
       List<Producto> productos = ctrl.findProductoEntities();
       productos = productos.stream().filter(Producto::isActivo)...

3. ProductoJpaController.findProductoEntities() ejecuta:
   SELECT * FROM producto ORDER BY id_producto ASC
   └── EclipseLink mapea cada fila → objeto Producto usando @Column
   └── Retorna List<Producto>

4. SvProductos construye el JSON y lo envía:
   [
     {"id":1, "nombre":"Chanel N°5", "precio":350000, "marca":"Chanel", "activo":true},
     {"id":2, "nombre":"Coco Mademoiselle", "precio":420000, "marca":"Chanel", "activo":true}
   ]

5. El JavaScript en Chanel.jsp recibe el JSON
   └── Filtra solo los que tengan marca === "Chanel"
   └── Por cada producto crea una tarjeta HTML:
       var art = document.createElement('article');
       art.innerHTML = '<h2>' + p.nombre + '</h2><p>' + p.precio + '</p>';
       section.appendChild(art);
   └── Las tarjetas aparecen en pantalla sin recargar la página
```

---

## 5. CÓMO SE GUARDA LA SESIÓN DEL USUARIO (login → JSP → Servlet)

```
1. El usuario envía el formulario de login
   └── POST a /SvLogin

2. SvLogin busca al usuario en la BD:
   Usuario u = new UsuarioJpaController().findUsuario(id);
   // SELECT * FROM usuario WHERE id_usuario = ?

3. Si las credenciales son correctas, SvLogin guarda en la sesión HTTP:
   session.setAttribute("usuario",        objetoUsuario);
   session.setAttribute("idUsuario",      u.getIdUsuario());
   session.setAttribute("esAdmin",        u.esAdmin());          // true si no tiene cliente
   session.setAttribute("permisosUsuario", listaDeNombres);      // ["VER_PRODUCTOS", "GESTIONAR_MARCAS"]

4. Cada JSP lee la sesión para decidir qué mostrar:
   // admin.jsp — scriptlet Java en la parte superior
   Object usuarioSess = session.getAttribute("usuario");
   if (usuarioSess == null) { response.sendRedirect("perfil.jsp"); return; }
   boolean esAdminPuro = Boolean.TRUE.equals(session.getAttribute("esAdmin"));

5. Cada Servlet lee la sesión para decidir qué permitir:
   // SvMarcas.java
   AuthHelper.tienePermiso(request, "GESTIONAR_MARCAS")
   // AuthHelper lee session.getAttribute("permisosUsuario")
   // Si el permiso no está en la lista → retorna HTTP 403 Forbidden
```

---

## 6. RESUMEN — Quién hace qué

| Capa | Archivo(s) | Responsabilidad |
|---|---|---|
| **Vista** | `web/vistas/*.jsp` | Muestra HTML. Código Java solo para leer la sesión. Todo lo dinámico lo hace JavaScript. |
| **JavaScript** | `web/assets/scripts/admin.js`, `cart.js` | Hace peticiones `fetch()` al servidor, recibe JSON, actualiza el DOM sin recargar la página. |
| **Servlet** | `src/java/servlets/Sv*.java` | Recibe la petición HTTP, verifica permisos, lee parámetros, llama al controlador, construye y envía la respuesta JSON. |
| **Controlador** | `src/java/persistencias/*JpaController.java` | Ejecuta el SQL nativo usando el EntityManager. No sabe nada de HTTP ni de HTML. Solo habla con la BD. |
| **Entidad** | `src/java/logica/*.java` | Objeto Java que representa una fila de la tabla. Tiene los campos y sus validaciones. |
| **JpaProvider** | `src/java/persistencias/JpaProvider.java` | Singleton que tiene la única `EntityManagerFactory` de la app. Todos los controladores la usan para abrir sus sesiones. |
| **persistence.xml** | `src/conf/persistence.xml` | Configura la conexión a MySQL: URL, usuario, contraseña, lista de entidades. |
