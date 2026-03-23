-- ============================================================
-- BASE DE DATOS: Perfumeria_andreylpz
-- Proyecto: ANDREYLPZ E-Commerce de Perfumes
-- Motor: MySQL con InnoDB (soporte transaccional y claves foráneas)
-- Codificación: utf8mb4 para soportar emojis y caracteres especiales
-- ============================================================

-- Elimina la BD si ya existe para poder recrearla desde cero (útil en desarrollo)
DROP DATABASE IF EXISTS Perfumeria_andreylpz;
-- Crea la base de datos con codificación UTF-8 completa (utf8mb4)
-- utf8mb4_unicode_ci = comparaciones insensibles a mayúsculas/acentos
CREATE DATABASE Perfumeria_andreylpz
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Selecciona la BD recién creada como activa para las siguientes sentencias
USE Perfumeria_andreylpz;

-- Desactiva temporalmente la verificación de claves foráneas
-- Esto permite crear las tablas en cualquier orden sin que MySQL
-- rechace las FK que apuntan a tablas aún no creadas
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- TABLAS INDEPENDIENTES (sin FK)
-- Estas tablas no dependen de ninguna otra tabla,
-- por eso se crean primero.
-- ============================================================

-- Tabla: categoria
-- Almacena las categorías de los perfumes (Eau de Parfum, Eau de Toilette, etc.)
-- Cada producto pertenece a una categoría
CREATE TABLE categoria (
    id_categoria     INT          NOT NULL AUTO_INCREMENT, -- PK autoincremental
    nombre_categoria VARCHAR(100) NOT NULL,                -- nombre de la categoría (ej: "Eau de Parfum")
    descripcion      VARCHAR(255)     NULL,                -- descripción opcional de la categoría
    activo           TINYINT(1)   NOT NULL DEFAULT 1,      -- 1=activa, 0=desactivada (eliminación lógica)
    PRIMARY KEY (id_categoria)                             -- clave primaria
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: marca
-- Almacena las marcas de perfumes (Xerjoff, Chanel, Dior, etc.)
-- Cada producto pertenece a una marca
CREATE TABLE marca (
    id_marca     INT          NOT NULL AUTO_INCREMENT, -- PK autoincremental
    nombre_marca VARCHAR(100) NOT NULL,                -- nombre de la marca (ej: "Chanel")
    descripcion  VARCHAR(255)     NULL,                -- descripción opcional
    genero       VARCHAR(6)   NOT NULL DEFAULT 'HOMBRE' COMMENT 'HOMBRE, MUJER', -- género al que se dirige la marca (usado para separar en el navbar)
    pagina_url   VARCHAR(150)     NULL,                -- nombre del archivo JSP de la marca (ej: "Chanel.jsp")
    activo       TINYINT(1)   NOT NULL DEFAULT 1,      -- 1=activa, 0=desactivada
    PRIMARY KEY (id_marca)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: rol
-- Define los roles del sistema (ADMIN, CLIENTE, VENDEDOR, etc.)
-- Cada usuario tiene un rol que determina sus permisos
CREATE TABLE rol (
    id_rol      INT          NOT NULL AUTO_INCREMENT, -- PK autoincremental
    nombre_rol  VARCHAR(100) NOT NULL,                -- nombre del rol (ej: "ADMIN", "CLIENTE")
    descripcion VARCHAR(255)     NULL,                -- descripción del rol
    activo      TINYINT(1)   NOT NULL DEFAULT 1,      -- 1=activo, 0=desactivado
    PRIMARY KEY (id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: permiso
-- Define los permisos individuales del sistema (VER_DASHBOARD, VER_PRODUCTOS, etc.)
-- Los permisos se asignan a roles a través de la tabla intermedia rol_permiso
CREATE TABLE permiso (
    id_permiso     INT          NOT NULL AUTO_INCREMENT, -- PK autoincremental
    nombre_permiso VARCHAR(100) NOT NULL,                -- nombre clave del permiso (ej: "VER_DASHBOARD")
    descripcion    VARCHAR(255)     NULL,                -- descripción legible del permiso
    modulo         VARCHAR(100)     NULL,                -- módulo al que pertenece (ej: "PRODUCTOS", "PEDIDOS")
    activo         TINYINT(1)   NOT NULL DEFAULT 1,      -- 1=activo, 0=desactivado
    PRIMARY KEY (id_permiso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: cliente
-- Almacena los datos personales de cada cliente registrado
-- Se relaciona con usuario (1:1) y con pedido, teléfonos, correos
CREATE TABLE cliente (
    id_cliente      INT          NOT NULL AUTO_INCREMENT, -- PK autoincremental
    nombre_completo VARCHAR(150) NOT NULL,                -- nombre completo del cliente
    direccion       TEXT         NOT NULL,                -- dirección de envío registrada
    activo          TINYINT(1)   NOT NULL DEFAULT 1,      -- 1=activo, 0=desactivado
    created_at      DATETIME         NULL,                -- fecha de creación del registro
    updated_at      DATETIME         NULL,                -- última actualización del registro
    PRIMARY KEY (id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLAS CON FK DE PRIMER NIVEL
-- Estas tablas dependen de las tablas independientes anteriores.
-- Sus claves foráneas (FK) apuntan a las tablas base.
-- ============================================================

-- Tabla: producto
-- Almacena el catálogo de perfumes de la tienda
-- Cada producto pertenece a una categoría y a una marca
-- El campo precio es el precio ACTUAL del producto en el catálogo
CREATE TABLE producto (
    id_producto     INT           NOT NULL AUTO_INCREMENT, -- PK autoincremental
    id_categoria    INT           NOT NULL,                -- FK → categoria: clasifica el tipo de perfume
    id_marca        INT           NOT NULL,                -- FK → marca: casa de perfumería que lo fabrica
    nombre_producto VARCHAR(200)  NOT NULL,                -- nombre del perfume (ej: "Sauvage EDP")
    descripcion     VARCHAR(255)      NULL,                -- descripción corta del perfume
    precio          DECIMAL(10,2) NOT NULL,                -- precio actual en COP (puede cambiar con el tiempo)
    stock           INT           NOT NULL DEFAULT 0,      -- unidades disponibles en inventario
    imagen_url      VARCHAR(500)      NULL,                -- URL de la imagen del producto
    activo          TINYINT(1)    NOT NULL DEFAULT 1,      -- 1=visible en tienda, 0=oculto
    created_at      DATETIME          NULL,                -- fecha de creación
    updated_at      DATETIME          NULL,                -- última actualización
    PRIMARY KEY (id_producto),
    -- FK: cada producto DEBE pertenecer a una categoría existente
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria),
    -- FK: cada producto DEBE pertenecer a una marca existente
    CONSTRAINT fk_producto_marca     FOREIGN KEY (id_marca)     REFERENCES marca     (id_marca)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: usuario
-- Almacena las credenciales de acceso al sistema
-- Si id_cliente es NULL → es un admin puro (sin datos de cliente)
-- Si id_cliente tiene valor → es un cliente registrado
CREATE TABLE usuario (
    id_usuario     INT          NOT NULL AUTO_INCREMENT, -- PK autoincremental
    id_cliente     INT              NULL,                -- FK → cliente: NULL = admin puro, NOT NULL = cliente
    id_rol         INT          NOT NULL,                -- FK → rol: determina los permisos del usuario
    correo_usuario VARCHAR(200) NOT NULL UNIQUE,         -- correo electrónico (login), debe ser único
    contrasena     VARCHAR(255) NOT NULL,                -- contraseña en texto plano (sin hash)
    activo         TINYINT(1)   NOT NULL DEFAULT 1,      -- 1=puede iniciar sesión, 0=bloqueado
    ultimo_acceso  DATETIME         NULL,                -- fecha/hora del último login exitoso
    created_at     DATETIME         NULL,                -- fecha de creación de la cuenta
    updated_at     DATETIME         NULL,                -- última actualización
    PRIMARY KEY (id_usuario),
    -- FK: vincula el usuario con sus datos personales de cliente (opcional)
    CONSTRAINT fk_usuario_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
    -- FK: cada usuario DEBE tener un rol asignado
    CONSTRAINT fk_usuario_rol     FOREIGN KEY (id_rol)     REFERENCES rol     (id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: rol_permiso (tabla intermedia / pivote)
-- Implementa la relación MUCHOS-A-MUCHOS entre rol y permiso
-- Cada fila asigna un permiso específico a un rol
CREATE TABLE rol_permiso (
    id_rol_permiso INT      NOT NULL AUTO_INCREMENT, -- PK autoincremental (sustituye clave compuesta)
    id_rol         INT      NOT NULL,                -- FK → rol: el rol que recibe el permiso
    id_permiso     INT      NOT NULL,                -- FK → permiso: el permiso que se asigna
    created_at     DATETIME     NULL,                -- cuándo se hizo la asignación
    PRIMARY KEY (id_rol_permiso),
    -- FK: el rol debe existir
    CONSTRAINT fk_rolpermiso_rol     FOREIGN KEY (id_rol)     REFERENCES rol     (id_rol),
    -- FK: el permiso debe existir
    CONSTRAINT fk_rolpermiso_permiso FOREIGN KEY (id_permiso) REFERENCES permiso (id_permiso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: telefono_cliente
-- Un cliente puede tener múltiples teléfonos (celular, fijo, trabajo)
-- Relación: cliente 1 → N teléfonos
CREATE TABLE telefono_cliente (
    id_telefono   INT         NOT NULL AUTO_INCREMENT, -- PK autoincremental
    id_cliente    INT         NOT NULL,                -- FK → cliente: dueño del teléfono
    telefono      VARCHAR(20) NOT NULL,                -- número de teléfono
    tipo_telefono VARCHAR(20) NOT NULL DEFAULT 'CELULAR' -- tipo: CELULAR, FIJO o TRABAJO
                  COMMENT 'CELULAR, FIJO, TRABAJO',
    activo        TINYINT(1)  NOT NULL DEFAULT 1,      -- 1=activo, 0=eliminado lógicamente
    PRIMARY KEY (id_telefono),
    CONSTRAINT fk_telefono_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: correo_cliente
-- Un cliente puede tener múltiples correos electrónicos
-- El campo 'principal' marca cuál es el correo preferido
CREATE TABLE correo_cliente (
    id_correo  INT          NOT NULL AUTO_INCREMENT, -- PK autoincremental
    id_cliente INT          NOT NULL,                -- FK → cliente: dueño del correo
    correo     VARCHAR(200) NOT NULL,                -- dirección de correo electrónico
    principal  TINYINT(1)   NOT NULL DEFAULT 0,      -- 1=correo principal, 0=correo secundario
    activo     TINYINT(1)   NOT NULL DEFAULT 1,      -- 1=activo, 0=eliminado lógicamente
    PRIMARY KEY (id_correo),
    CONSTRAINT fk_correo_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: pedido
-- Registra cada compra realizada por un cliente
-- El ciclo de vida del estado es: PENDIENTE → PROCESANDO → PAGO → ENVIADO → ENTREGADO
-- También puede pasar a CANCELADO en cualquier momento
CREATE TABLE pedido (
    id_pedido    INT           NOT NULL AUTO_INCREMENT, -- PK autoincremental
    id_cliente   INT           NOT NULL,                -- FK → cliente: quién hizo la compra
    fecha_pedido DATETIME          NULL,                -- cuándo se realizó el pedido
    estado       VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE' -- estado actual del pedido
                 COMMENT 'PENDIENTE, PROCESANDO, PAGO, ENVIADO, ENTREGADO, CANCELADO',
    total        DECIMAL(10,2)     NULL,                -- monto total del pedido en COP
    activo       TINYINT(1)    NOT NULL DEFAULT 1,      -- 1=activo, 0=eliminado lógicamente
    created_at   DATETIME          NULL,                -- fecha de creación
    updated_at   DATETIME          NULL,                -- última actualización
    PRIMARY KEY (id_pedido),
    -- FK: cada pedido DEBE pertenecer a un cliente registrado
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLAS CON FK DE SEGUNDO NIVEL
-- Estas tablas dependen de tablas que a su vez dependen de otras.
-- Son las tablas más "hijas" del esquema relacional.
-- ============================================================

-- Tabla: detalle_pedido
-- Cada fila representa un producto comprado dentro de un pedido
-- Un pedido puede tener múltiples detalles (uno por cada producto)
-- IMPORTANTE: precio_unitario captura el precio al momento de la compra
-- (distinto del precio actual del producto, que puede cambiar después)
CREATE TABLE detalle_pedido (
    id_detalle      INT           NOT NULL AUTO_INCREMENT, -- PK autoincremental
    id_pedido       INT           NOT NULL,                -- FK → pedido: a qué pedido pertenece este detalle
    id_producto     INT           NOT NULL,                -- FK → producto: qué producto se compró
    cantidad        INT           NOT NULL,                -- cuántas unidades se compraron
    precio_unitario DECIMAL(10,2) NOT NULL,                -- precio del producto AL MOMENTO de la compra (snapshot)
    activo          TINYINT(1)    NOT NULL DEFAULT 1,      -- 1=activo, 0=eliminado lógicamente
    PRIMARY KEY (id_detalle),
    -- FK: el detalle pertenece a un pedido
    CONSTRAINT fk_detalle_pedido   FOREIGN KEY (id_pedido)   REFERENCES pedido   (id_pedido),
    -- FK: el detalle referencia un producto (impide eliminar productos con ventas)
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: pago
-- Registra el pago asociado a un pedido
-- Relación 1:1 con pedido (UNIQUE en id_pedido)
-- El ciclo de vida del estado es: PENDIENTE → APROBADO (o RECHAZADO → REEMBOLSADO)
CREATE TABLE pago (
    id_pago                INT           NOT NULL AUTO_INCREMENT, -- PK autoincremental
    id_pedido              INT           NOT NULL UNIQUE,         -- FK → pedido: UN solo pago por pedido (UNIQUE)
    fecha_pago             DATETIME          NULL,                -- cuándo se registró el pago
    estado_pago            VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE' -- estado del pago
                           COMMENT 'PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO',
    metodo_pago            VARCHAR(50)       NULL,                -- método usado (EFECTIVO, TARJETA, NEQUI, etc.)
    monto_pagado           DECIMAL(10,2)     NULL,                -- monto pagado en COP
    referencia_transaccion VARCHAR(100)      NULL,                -- referencia bancaria o de transacción
    activo                 TINYINT(1)    NOT NULL DEFAULT 1,      -- 1=activo, 0=eliminado lógicamente
    PRIMARY KEY (id_pago),
    -- FK: cada pago corresponde a un pedido
    CONSTRAINT fk_pago_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: envio
-- Registra la información de envío de un pedido
-- Relación 1:1 con pedido (UNIQUE en id_pedido)
-- El ciclo de vida del estado es: PREPARANDO → EN_TRANSITO → ENTREGADO (o DEVUELTO)
CREATE TABLE envio (
    id_envio               INT          NOT NULL AUTO_INCREMENT, -- PK autoincremental
    id_pedido              INT          NOT NULL UNIQUE,         -- FK → pedido: UN solo envío por pedido (UNIQUE)
    fecha_envio            DATETIME         NULL,                -- cuándo se despachó el paquete
    fecha_estimada_entrega DATETIME         NULL,                -- fecha estimada de entrega al cliente
    estado_entrega         VARCHAR(20)  NOT NULL DEFAULT 'PREPARANDO' -- estado actual del envío
                           COMMENT 'PREPARANDO, EN_TRANSITO, ENTREGADO, DEVUELTO',
    direccion_envio        VARCHAR(255)     NULL,                -- dirección física de entrega
    transportadora         VARCHAR(100)     NULL,                -- empresa de transporte (ej: "Servientrega")
    numero_guia            VARCHAR(100)     NULL,                -- número de guía de la transportadora (único, 10-22 chars)
    activo                 TINYINT(1)   NOT NULL DEFAULT 1,      -- 1=activo, 0=eliminado lógicamente
    created_at             DATETIME         NULL,                -- fecha de creación del registro
    updated_at             DATETIME         NULL,                -- última actualización
    PRIMARY KEY (id_envio),
    -- FK: cada envío corresponde a un pedido
    CONSTRAINT fk_envio_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Reactiva la verificación de claves foráneas ahora que todas las tablas existen
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- DATOS SEMILLA (datos iniciales que la aplicación necesita para funcionar)
-- Estos INSERT se ejecutan al crear la BD para tener un estado mínimo usable
-- ============================================================

-- Roles base del sistema:
-- ADMIN (id=1): acceso total al panel de administración
-- CLIENTE (id=2): rol por defecto para usuarios que se registran en la tienda
INSERT INTO rol (nombre_rol, descripcion, activo) VALUES
('ADMIN',   'Administrador con acceso total al sistema', 1),
('CLIENTE', 'Cliente registrado de la tienda',           1);

-- Permisos del sistema: cada uno habilita una funcionalidad específica
-- Se agrupan por módulo para organización lógica
-- Estos permisos se asignan a roles a través de la tabla rol_permiso
INSERT INTO permiso (nombre_permiso, descripcion, modulo, activo) VALUES
('VER_DASHBOARD',        'Acceder al panel de administración',    'DASHBOARD', 1), -- acceso al panel admin
('VER_PRODUCTOS',        'Ver catálogo de productos',             'PRODUCTOS', 1), -- ver lista de productos en admin
('EDITAR_PRODUCTOS',     'Crear y editar productos',              'PRODUCTOS', 1), -- crear/editar productos
('ELIMINAR_PRODUCTOS',   'Eliminar/desactivar productos',         'PRODUCTOS', 1), -- eliminar productos físicamente
('VER_PEDIDOS',          'Ver todos los pedidos',                 'PEDIDOS',   1), -- ver lista de pedidos
('GESTIONAR_PEDIDOS',    'Cambiar estado de pedidos',             'PEDIDOS',   1), -- cambiar estado de pedidos
('VER_USUARIOS',         'Ver y gestionar usuarios',              'USUARIOS',  1), -- ver lista de usuarios
('EDITAR_USUARIOS',      'Activar y desactivar usuarios',         'USUARIOS',  1), -- activar/desactivar usuarios
('GESTIONAR_PAGOS',      'Registrar y ver pagos',                 'PAGOS',     1), -- ver pagos de pedidos
('GESTIONAR_ENVIOS',     'Registrar y actualizar envíos',         'ENVIOS',    1), -- crear/actualizar envíos
('GESTIONAR_CATEGORIAS', 'CRUD completo de categorías',           'CATALOGOS', 1), -- CRUD de categorías
('GESTIONAR_MARCAS',     'CRUD completo de marcas',               'CATALOGOS', 1), -- CRUD de marcas
('GESTIONAR_ROLES',      'Administrar roles y sus permisos',      'SEGURIDAD', 1), -- gestionar roles
('GESTIONAR_PERMISOS',   'Crear y editar permisos del sistema',   'SEGURIDAD', 1); -- gestionar permisos

-- Asignar TODOS los permisos activos al rol ADMIN automáticamente
-- Usa un subquery para obtener el id del rol ADMIN y lo cruza con todos los permisos
-- Resultado: el admin tiene acceso total a todas las funcionalidades
INSERT INTO rol_permiso (id_rol, id_permiso, created_at)
SELECT
    (SELECT id_rol FROM rol WHERE nombre_rol = 'ADMIN'), -- id del rol ADMIN
    id_permiso,                                          -- cada permiso activo
    NOW()                                                -- fecha actual como fecha de asignación
FROM permiso WHERE activo = 1;

-- Categorías de perfumes según su concentración de esencia
-- Cada producto se clasifica en una de estas categorías
INSERT INTO categoria (nombre_categoria, descripcion, activo) VALUES
('Eau de Parfum',   'Alta concentración, larga duración',   1), -- 15-20% concentración
('Eau de Toilette', 'Concentración media, uso diario',      1), -- 5-15% concentración
('Perfume',         'Máxima concentración de esencia',      1), -- 20-30% concentración
('Colonia',         'Concentración ligera y fresca',        1); -- 2-4% concentración

-- Marcas de perfumes con su género objetivo y página JSP asociada
-- genero: determina en qué sección del navbar aparece (HOMBRE o MUJER)
-- pagina_url: nombre del JSP que muestra los productos de esta marca
INSERT INTO marca (nombre_marca, descripcion, genero, pagina_url, activo) VALUES
('Xerjoff',            'Perfumería de lujo italiana',           'HOMBRE', 'cartas.jsp',          1), -- id_marca=1
('Paco Rabanne',       'Marca francesa de moda y perfumería',   'HOMBRE', 'pacco_rabanne.jsp',   1), -- id_marca=2
('Chanel',             'Icónica casa de moda y perfumería',     'MUJER',  'Chanel.jsp',          1), -- id_marca=3
('Dior',               'Alta costura y perfumería francesa',    'MUJER',  'Cristian_dior.jsp',   1), -- id_marca=4
('Yves Saint Laurent', 'Perfumería moderna y sofisticada',      'HOMBRE', 'ejemplo.jsp',         1); -- id_marca=5

-- Productos Xerjoff (id_marca=1, 8 productos)
-- Todos de categoría Eau de Parfum (id_categoria=1)
-- Precios en COP (pesos colombianos)
INSERT INTO producto (id_categoria, id_marca, nombre_producto, descripcion, precio, stock, imagen_url, activo, created_at, updated_at) VALUES
(1, 1, 'Richwood',      'Fragancia exquisita con notas amaderadas y un toque de elegancia.',                                     1000000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Naxos',         'Fragancia unisex cítrica gourmand que celebra la riqueza del Mediterráneo.',                            1120000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Erba Pura',     'Fragancia unisex vibrante y frutal con apertura cítrica de naranja, limón y bergamota.',                1500000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Alexandria II', 'Fragancia amaderada y ámbar oriental con notas de palisandro, lavanda, canela y manzana.',              1100000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Torino XXI',    'Fragancia fresca y verde aromática inspirada en la energía vibrante de Turín.',                         1300000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Erba Gold',     'Fragancia fresca y luminosa con cítricos vibrantes y frutas dulces sobre base cálida.',                 1600000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Lira',          'Fragancia amaderada y afrutada que combina cítricos brillantes con notas florales suaves.',              1000000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Homme',         'Fragancia masculina aromática y fresca con notas cítricas y especiadas sobre maderas y flores suaves.', 1720000.00, 10, NULL, 1, NOW(), NOW());

-- Productos Paco Rabanne (id_marca=2, 8 productos)
-- Mezcla de categorías: mayoría Eau de Parfum (1), uno Eau de Toilette (2)
INSERT INTO producto (id_categoria, id_marca, nombre_producto, descripcion, precio, stock, imagen_url, activo, created_at, updated_at) VALUES
(1, 2, '1 Million',            'Fragancia masculina con notas de canela, menta, pomelo y cuero dorado.',                         420000.00, 15, NULL, 1, NOW(), NOW()),
(1, 2, '1 Million Lucky',      'Fragancia fresca y amaderada con notas de avellana, ron y pachulí.',                             390000.00, 15, NULL, 1, NOW(), NOW()),
(1, 2, 'Invictus',             'Fragancia masculina fresca marina con notas de pomelo, laurel y madera de guayaco.',             400000.00, 15, NULL, 1, NOW(), NOW()),
(1, 2, 'Invictus Aqua',        'Versión más fresca de Invictus con notas acuáticas, menta y madera.',                           410000.00, 15, NULL, 1, NOW(), NOW()),
(1, 2, 'Invictus Victory',     'Fragancia intensa y especiada con notas de cardamomo, laurel y vetiver ahumado.',               450000.00, 12, NULL, 1, NOW(), NOW()),
(1, 2, 'Olympéa',              'Fragancia femenina floral acuática con notas de agua salada, flor de jengibre y vainilla.',     390000.00, 15, NULL, 1, NOW(), NOW()),
(1, 2, 'Lady Million',         'Fragancia femenina floral oriental con notas de frambuesa, flor de naranjo y pachulí.',         420000.00, 15, NULL, 1, NOW(), NOW()),
(2, 2, 'Black XS',             'Fragancia masculina amaderada con notas de albahaca, ciruela negra y cuero.',                   350000.00, 12, NULL, 1, NOW(), NOW());

-- Productos Chanel (id_marca=3, 8 productos)
-- Mezcla de categorías: mayoría Eau de Parfum (1), uno Eau de Toilette (2)
INSERT INTO producto (id_categoria, id_marca, nombre_producto, descripcion, precio, stock, imagen_url, activo, created_at, updated_at) VALUES
(1, 3, 'Chanel N°5',           'El perfume más icónico del mundo, floral aldehídico con rosa, jazmín y sándalo.',               650000.00, 12, NULL, 1, NOW(), NOW()),
(1, 3, 'Chanel N°5 L''Eau',    'Versión fresca y ligera del clásico N°5 con toques cítricos y florales.',                       580000.00, 12, NULL, 1, NOW(), NOW()),
(1, 3, 'Bleu de Chanel',       'Fragancia masculina amaderada aromática con notas de cítricos, jazmín y cedro.',                620000.00, 10, NULL, 1, NOW(), NOW()),
(1, 3, 'Coco Mademoiselle',    'Fragancia femenina oriental floral con notas de naranja, rosa, pachulí y vetiver.',              640000.00, 10, NULL, 1, NOW(), NOW()),
(1, 3, 'Chance',               'Fragancia femenina floral fresca con notas de pomelo, jacinto, iris y vetiver.',                 600000.00, 10, NULL, 1, NOW(), NOW()),
(1, 3, 'Chance Eau Tendre',    'Versión suave y romántica de Chance con notas de pomelo, jazmín y musgo blanco.',               590000.00, 10, NULL, 1, NOW(), NOW()),
(1, 3, 'Allure Homme Sport',   'Fragancia masculina fresca con notas de mandarina, cedro, pimienta blanca y almizcle.',         570000.00, 10, NULL, 1, NOW(), NOW()),
(2, 3, 'Gabrielle Chanel',     'Fragancia femenina floral con notas de naranja, cassis, jazmín, tuberosa y sándalo.',           560000.00, 10, NULL, 1, NOW(), NOW());

-- Productos Christian Dior (id_marca=4, 8 productos)
-- Mezcla de categorías: mayoría Eau de Parfum (1), uno Eau de Toilette (2)
INSERT INTO producto (id_categoria, id_marca, nombre_producto, descripcion, precio, stock, imagen_url, activo, created_at, updated_at) VALUES
(1, 4, 'Sauvage EDP',          'Fragancia masculina amaderada fresca con notas de bergamota, pimienta y ambroxan.',             490000.00, 20, NULL, 1, NOW(), NOW()),
(1, 4, 'Sauvage EDT',          'Versión fresca de Sauvage con notas de bergamota, lavanda y cedro.',                           460000.00, 20, NULL, 1, NOW(), NOW()),
(1, 4, 'Sauvage Parfum',       'La versión más intensa de Sauvage con sándalo, pimienta y ámbar gris.',                        540000.00, 15, NULL, 1, NOW(), NOW()),
(1, 4, 'J''adore',             'Fragancia femenina floral luminosa con notas de pera, magnolia, rosa y almizcle.',              610000.00, 12, NULL, 1, NOW(), NOW()),
(1, 4, 'J''adore L''Or',       'Versión intensa de J''adore con flores blancas, jazmín sambac y sándalo cálido.',              650000.00, 10, NULL, 1, NOW(), NOW()),
(1, 4, 'Miss Dior',            'Fragancia femenina floral fresca con rosa de Grasse, peonía, bergamota y almizcle blanco.',    580000.00, 12, NULL, 1, NOW(), NOW()),
(1, 4, 'Miss Dior Blooming Bouquet', 'Fragancia femenina floral ligera con notas de peonía, frambuesa y almizcle blanco.',     520000.00, 12, NULL, 1, NOW(), NOW()),
(2, 4, 'Dior Homme',           'Fragancia masculina elegante con notas de lirio de los valles, iris y cuero suave.',           510000.00, 10, NULL, 1, NOW(), NOW());

-- Usuario administrador (admin puro, sin datos de cliente)
-- id_cliente = NULL indica que es un admin puro (no tiene datos de cliente en la tabla cliente)
-- id_rol = 1 (ADMIN) le otorga todos los permisos del sistema
-- Credenciales: admin@andreylpz.com / admin123 (texto plano)
INSERT INTO usuario (id_cliente, id_rol, correo_usuario, contrasena, activo, created_at, updated_at) VALUES
(NULL, 1, 'admin@andreylpz.com', 'admin123', 1, NOW(), NOW());

-- ============================================================
-- VERIFICACIÓN
-- Consulta de control que muestra cuántos registros se insertaron
-- en cada tabla. Útil para verificar que el script se ejecutó correctamente.
-- Resultado esperado: Roles=2, Permisos=14, Rol-Permisos=14,
--   Categorías=4, Marcas=5, Productos=32, Usuarios=1
-- ============================================================
SELECT 'Roles'        AS tabla, COUNT(*) AS total FROM rol
UNION ALL SELECT 'Permisos',     COUNT(*) FROM permiso
UNION ALL SELECT 'Rol-Permisos', COUNT(*) FROM rol_permiso
UNION ALL SELECT 'Categorías',   COUNT(*) FROM categoria
UNION ALL SELECT 'Marcas',       COUNT(*) FROM marca
UNION ALL SELECT 'Productos',    COUNT(*) FROM producto
UNION ALL SELECT 'Usuarios',     COUNT(*) FROM usuario;
