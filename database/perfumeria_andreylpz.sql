-- ============================================================
-- BASE DE DATOS: Perfumeria_andreylpz
-- Proyecto: ANDREYLPZ E-Commerce de Perfumes
-- ============================================================

DROP DATABASE IF EXISTS Perfumeria_andreylpz;
CREATE DATABASE Perfumeria_andreylpz
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE Perfumeria_andreylpz;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- TABLAS INDEPENDIENTES (sin FK)
-- ============================================================

CREATE TABLE categoria (
    id_categoria     INT          NOT NULL AUTO_INCREMENT,
    nombre_categoria VARCHAR(100) NOT NULL,
    descripcion      VARCHAR(255)     NULL,
    activo           TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE marca (
    id_marca     INT          NOT NULL AUTO_INCREMENT,
    nombre_marca VARCHAR(100) NOT NULL,
    descripcion  VARCHAR(255)     NULL,
    genero       VARCHAR(6)   NOT NULL DEFAULT 'HOMBRE' COMMENT 'HOMBRE, MUJER',
    pagina_url   VARCHAR(150)     NULL,
    activo       TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_marca)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rol (
    id_rol      INT          NOT NULL AUTO_INCREMENT,
    nombre_rol  VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)     NULL,
    activo      TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE permiso (
    id_permiso     INT          NOT NULL AUTO_INCREMENT,
    nombre_permiso VARCHAR(100) NOT NULL,
    descripcion    VARCHAR(255)     NULL,
    modulo         VARCHAR(100)     NULL,
    activo         TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_permiso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cliente (
    id_cliente      INT          NOT NULL AUTO_INCREMENT,
    nombre_completo VARCHAR(150) NOT NULL,
    direccion       TEXT         NOT NULL,
    activo          TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME         NULL,
    updated_at      DATETIME         NULL,
    PRIMARY KEY (id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLAS CON FK DE PRIMER NIVEL
-- ============================================================

CREATE TABLE producto (
    id_producto     INT           NOT NULL AUTO_INCREMENT,
    id_categoria    INT           NOT NULL,
    id_marca        INT           NOT NULL,
    nombre_producto VARCHAR(200)  NOT NULL,
    descripcion     VARCHAR(255)      NULL,
    precio          DECIMAL(10,2) NOT NULL,
    stock           INT           NOT NULL DEFAULT 0,
    imagen_url      VARCHAR(500)      NULL,
    activo          TINYINT(1)    NOT NULL DEFAULT 1,
    created_at      DATETIME          NULL,
    updated_at      DATETIME          NULL,
    PRIMARY KEY (id_producto),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria),
    CONSTRAINT fk_producto_marca     FOREIGN KEY (id_marca)     REFERENCES marca     (id_marca)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuario (
    id_usuario     INT          NOT NULL AUTO_INCREMENT,
    id_cliente     INT              NULL,
    id_rol         INT          NOT NULL,
    correo_usuario VARCHAR(200) NOT NULL UNIQUE,
    contrasena     VARCHAR(255) NOT NULL,
    activo         TINYINT(1)   NOT NULL DEFAULT 1,
    ultimo_acceso  DATETIME         NULL,
    created_at     DATETIME         NULL,
    updated_at     DATETIME         NULL,
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_usuario_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
    CONSTRAINT fk_usuario_rol     FOREIGN KEY (id_rol)     REFERENCES rol     (id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rol_permiso (
    id_rol_permiso INT      NOT NULL AUTO_INCREMENT,
    id_rol         INT      NOT NULL,
    id_permiso     INT      NOT NULL,
    created_at     DATETIME     NULL,
    PRIMARY KEY (id_rol_permiso),
    CONSTRAINT fk_rolpermiso_rol     FOREIGN KEY (id_rol)     REFERENCES rol     (id_rol),
    CONSTRAINT fk_rolpermiso_permiso FOREIGN KEY (id_permiso) REFERENCES permiso (id_permiso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE telefono_cliente (
    id_telefono   INT         NOT NULL AUTO_INCREMENT,
    id_cliente    INT         NOT NULL,
    telefono      VARCHAR(20) NOT NULL,
    tipo_telefono VARCHAR(20) NOT NULL DEFAULT 'CELULAR'
                  COMMENT 'CELULAR, FIJO, TRABAJO',
    activo        TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id_telefono),
    CONSTRAINT fk_telefono_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE correo_cliente (
    id_correo  INT          NOT NULL AUTO_INCREMENT,
    id_cliente INT          NOT NULL,
    correo     VARCHAR(200) NOT NULL,
    principal  TINYINT(1)   NOT NULL DEFAULT 0,
    activo     TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_correo),
    CONSTRAINT fk_correo_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pedido (
    id_pedido    INT           NOT NULL AUTO_INCREMENT,
    id_cliente   INT           NOT NULL,
    fecha_pedido DATETIME          NULL,
    estado       VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE'
                 COMMENT 'PENDIENTE, PROCESANDO, PAGO, ENVIADO, ENTREGADO, CANCELADO',
    total        DECIMAL(10,2)     NULL,
    activo       TINYINT(1)    NOT NULL DEFAULT 1,
    created_at   DATETIME          NULL,
    updated_at   DATETIME          NULL,
    PRIMARY KEY (id_pedido),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLAS CON FK DE SEGUNDO NIVEL
-- ============================================================

CREATE TABLE detalle_pedido (
    id_detalle      INT           NOT NULL AUTO_INCREMENT,
    id_pedido       INT           NOT NULL,
    id_producto     INT           NOT NULL,
    cantidad        INT           NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    activo          TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id_detalle),
    CONSTRAINT fk_detalle_pedido   FOREIGN KEY (id_pedido)   REFERENCES pedido   (id_pedido),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pago (
    id_pago                INT           NOT NULL AUTO_INCREMENT,
    id_pedido              INT           NOT NULL UNIQUE,
    fecha_pago             DATETIME          NULL,
    estado_pago            VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE'
                           COMMENT 'PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO',
    metodo_pago            VARCHAR(50)       NULL,
    monto_pagado           DECIMAL(10,2)     NULL,
    referencia_transaccion VARCHAR(100)      NULL,
    activo                 TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id_pago),
    CONSTRAINT fk_pago_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE envio (
    id_envio               INT          NOT NULL AUTO_INCREMENT,
    id_pedido              INT          NOT NULL UNIQUE,
    fecha_envio            DATETIME         NULL,
    fecha_estimada_entrega DATETIME         NULL,
    estado_entrega         VARCHAR(20)  NOT NULL DEFAULT 'PREPARANDO'
                           COMMENT 'PREPARANDO, EN_TRANSITO, ENTREGADO, DEVUELTO',
    direccion_envio        VARCHAR(255)     NULL,
    transportadora         VARCHAR(100)     NULL,
    numero_guia            VARCHAR(100)     NULL,
    activo                 TINYINT(1)   NOT NULL DEFAULT 1,
    created_at             DATETIME         NULL,
    updated_at             DATETIME         NULL,
    PRIMARY KEY (id_envio),
    CONSTRAINT fk_envio_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- DATOS SEMILLA
-- ============================================================

-- Roles
INSERT INTO rol (nombre_rol, descripcion, activo) VALUES
('ADMIN',   'Administrador con acceso total al sistema', 1),
('CLIENTE', 'Cliente registrado de la tienda',           1);

-- Permisos
INSERT INTO permiso (nombre_permiso, descripcion, modulo, activo) VALUES
('VER_DASHBOARD',        'Acceder al panel de administración',    'DASHBOARD', 1),
('VER_PRODUCTOS',        'Ver catálogo de productos',             'PRODUCTOS', 1),
('EDITAR_PRODUCTOS',     'Crear y editar productos',              'PRODUCTOS', 1),
('ELIMINAR_PRODUCTOS',   'Eliminar/desactivar productos',         'PRODUCTOS', 1),
('VER_PEDIDOS',          'Ver todos los pedidos',                 'PEDIDOS',   1),
('GESTIONAR_PEDIDOS',    'Cambiar estado de pedidos',             'PEDIDOS',   1),
('VER_USUARIOS',         'Ver y gestionar usuarios',              'USUARIOS',  1),
('EDITAR_USUARIOS',      'Activar y desactivar usuarios',         'USUARIOS',  1),
('GESTIONAR_PAGOS',      'Registrar y ver pagos',                 'PAGOS',     1),
('GESTIONAR_ENVIOS',     'Registrar y actualizar envíos',         'ENVIOS',    1),
('GESTIONAR_CATEGORIAS', 'CRUD completo de categorías',           'CATALOGOS', 1),
('GESTIONAR_MARCAS',     'CRUD completo de marcas',               'CATALOGOS', 1),
('GESTIONAR_ROLES',      'Administrar roles y sus permisos',      'SEGURIDAD', 1),
('GESTIONAR_PERMISOS',   'Crear y editar permisos del sistema',   'SEGURIDAD', 1);

-- Asignar todos los permisos al rol ADMIN
INSERT INTO rol_permiso (id_rol, id_permiso, created_at)
SELECT
    (SELECT id_rol FROM rol WHERE nombre_rol = 'ADMIN'),
    id_permiso,
    NOW()
FROM permiso WHERE activo = 1;

-- Categorías
INSERT INTO categoria (nombre_categoria, descripcion, activo) VALUES
('Eau de Parfum',   'Alta concentración, larga duración',   1),
('Eau de Toilette', 'Concentración media, uso diario',      1),
('Perfume',         'Máxima concentración de esencia',      1),
('Colonia',         'Concentración ligera y fresca',        1);

-- Marcas
INSERT INTO marca (nombre_marca, descripcion, genero, pagina_url, activo) VALUES
('Xerjoff',            'Perfumería de lujo italiana',           'HOMBRE', 'cartas.jsp',          1),
('Paco Rabanne',       'Marca francesa de moda y perfumería',   'HOMBRE', 'pacco_rabanne.jsp',   1),
('Chanel',             'Icónica casa de moda y perfumería',     'MUJER',  'Chanel.jsp',          1),
('Dior',               'Alta costura y perfumería francesa',    'MUJER',  'Cristian_dior.jsp',   1),
('Yves Saint Laurent', 'Perfumería moderna y sofisticada',      'HOMBRE', 'ejemplo.jsp',         1);

-- Productos Xerjoff (8 productos)
INSERT INTO producto (id_categoria, id_marca, nombre_producto, descripcion, precio, stock, imagen_url, activo, created_at, updated_at) VALUES
(1, 1, 'Richwood',      'Fragancia exquisita con notas amaderadas y un toque de elegancia.',                                     1000000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Naxos',         'Fragancia unisex cítrica gourmand que celebra la riqueza del Mediterráneo.',                            1120000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Erba Pura',     'Fragancia unisex vibrante y frutal con apertura cítrica de naranja, limón y bergamota.',                1500000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Alexandria II', 'Fragancia amaderada y ámbar oriental con notas de palisandro, lavanda, canela y manzana.',              1100000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Torino XXI',    'Fragancia fresca y verde aromática inspirada en la energía vibrante de Turín.',                         1300000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Erba Gold',     'Fragancia fresca y luminosa con cítricos vibrantes y frutas dulces sobre base cálida.',                 1600000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Lira',          'Fragancia amaderada y afrutada que combina cítricos brillantes con notas florales suaves.',              1000000.00, 10, NULL, 1, NOW(), NOW()),
(1, 1, 'Homme',         'Fragancia masculina aromática y fresca con notas cítricas y especiadas sobre maderas y flores suaves.', 1720000.00, 10, NULL, 1, NOW(), NOW());

-- Productos Paco Rabanne (id_marca=2)
INSERT INTO producto (id_categoria, id_marca, nombre_producto, descripcion, precio, stock, imagen_url, activo, created_at, updated_at) VALUES
(1, 2, '1 Million',            'Fragancia masculina con notas de canela, menta, pomelo y cuero dorado.',                         420000.00, 15, NULL, 1, NOW(), NOW()),
(1, 2, '1 Million Lucky',      'Fragancia fresca y amaderada con notas de avellana, ron y pachulí.',                             390000.00, 15, NULL, 1, NOW(), NOW()),
(1, 2, 'Invictus',             'Fragancia masculina fresca marina con notas de pomelo, laurel y madera de guayaco.',             400000.00, 15, NULL, 1, NOW(), NOW()),
(1, 2, 'Invictus Aqua',        'Versión más fresca de Invictus con notas acuáticas, menta y madera.',                           410000.00, 15, NULL, 1, NOW(), NOW()),
(1, 2, 'Invictus Victory',     'Fragancia intensa y especiada con notas de cardamomo, laurel y vetiver ahumado.',               450000.00, 12, NULL, 1, NOW(), NOW()),
(1, 2, 'Olympéa',              'Fragancia femenina floral acuática con notas de agua salada, flor de jengibre y vainilla.',     390000.00, 15, NULL, 1, NOW(), NOW()),
(1, 2, 'Lady Million',         'Fragancia femenina floral oriental con notas de frambuesa, flor de naranjo y pachulí.',         420000.00, 15, NULL, 1, NOW(), NOW()),
(2, 2, 'Black XS',             'Fragancia masculina amaderada con notas de albahaca, ciruela negra y cuero.',                   350000.00, 12, NULL, 1, NOW(), NOW());

-- Productos Chanel (id_marca=3)
INSERT INTO producto (id_categoria, id_marca, nombre_producto, descripcion, precio, stock, imagen_url, activo, created_at, updated_at) VALUES
(1, 3, 'Chanel N°5',           'El perfume más icónico del mundo, floral aldehídico con rosa, jazmín y sándalo.',               650000.00, 12, NULL, 1, NOW(), NOW()),
(1, 3, 'Chanel N°5 L''Eau',    'Versión fresca y ligera del clásico N°5 con toques cítricos y florales.',                       580000.00, 12, NULL, 1, NOW(), NOW()),
(1, 3, 'Bleu de Chanel',       'Fragancia masculina amaderada aromática con notas de cítricos, jazmín y cedro.',                620000.00, 10, NULL, 1, NOW(), NOW()),
(1, 3, 'Coco Mademoiselle',    'Fragancia femenina oriental floral con notas de naranja, rosa, pachulí y vetiver.',              640000.00, 10, NULL, 1, NOW(), NOW()),
(1, 3, 'Chance',               'Fragancia femenina floral fresca con notas de pomelo, jacinto, iris y vetiver.',                 600000.00, 10, NULL, 1, NOW(), NOW()),
(1, 3, 'Chance Eau Tendre',    'Versión suave y romántica de Chance con notas de pomelo, jazmín y musgo blanco.',               590000.00, 10, NULL, 1, NOW(), NOW()),
(1, 3, 'Allure Homme Sport',   'Fragancia masculina fresca con notas de mandarina, cedro, pimienta blanca y almizcle.',         570000.00, 10, NULL, 1, NOW(), NOW()),
(2, 3, 'Gabrielle Chanel',     'Fragancia femenina floral con notas de naranja, cassis, jazmín, tuberosa y sándalo.',           560000.00, 10, NULL, 1, NOW(), NOW());

-- Productos Christian Dior (id_marca=4)
INSERT INTO producto (id_categoria, id_marca, nombre_producto, descripcion, precio, stock, imagen_url, activo, created_at, updated_at) VALUES
(1, 4, 'Sauvage EDP',          'Fragancia masculina amaderada fresca con notas de bergamota, pimienta y ambroxan.',             490000.00, 20, NULL, 1, NOW(), NOW()),
(1, 4, 'Sauvage EDT',          'Versión fresca de Sauvage con notas de bergamota, lavanda y cedro.',                           460000.00, 20, NULL, 1, NOW(), NOW()),
(1, 4, 'Sauvage Parfum',       'La versión más intensa de Sauvage con sándalo, pimienta y ámbar gris.',                        540000.00, 15, NULL, 1, NOW(), NOW()),
(1, 4, 'J''adore',             'Fragancia femenina floral luminosa con notas de pera, magnolia, rosa y almizcle.',              610000.00, 12, NULL, 1, NOW(), NOW()),
(1, 4, 'J''adore L''Or',       'Versión intensa de J''adore con flores blancas, jazmín sambac y sándalo cálido.',              650000.00, 10, NULL, 1, NOW(), NOW()),
(1, 4, 'Miss Dior',            'Fragancia femenina floral fresca con rosa de Grasse, peonía, bergamota y almizcle blanco.',    580000.00, 12, NULL, 1, NOW(), NOW()),
(1, 4, 'Miss Dior Blooming Bouquet', 'Fragancia femenina floral ligera con notas de peonía, frambuesa y almizcle blanco.',     520000.00, 12, NULL, 1, NOW(), NOW()),
(2, 4, 'Dior Homme',           'Fragancia masculina elegante con notas de lirio de los valles, iris y cuero suave.',           510000.00, 10, NULL, 1, NOW(), NOW());

-- Usuario administrador
-- correo: admin@andreylpz.com  |  contraseña: admin123
INSERT INTO usuario (id_cliente, id_rol, correo_usuario, contrasena, activo, created_at, updated_at) VALUES
(NULL, 1, 'admin@andreylpz.com', 'admin123', 1, NOW(), NOW());

-- ============================================================
-- VERIFICACIÓN
-- ============================================================
SELECT 'Roles'        AS tabla, COUNT(*) AS total FROM rol
UNION ALL SELECT 'Permisos',     COUNT(*) FROM permiso
UNION ALL SELECT 'Rol-Permisos', COUNT(*) FROM rol_permiso
UNION ALL SELECT 'Categorías',   COUNT(*) FROM categoria
UNION ALL SELECT 'Marcas',       COUNT(*) FROM marca
UNION ALL SELECT 'Productos',    COUNT(*) FROM producto
UNION ALL SELECT 'Usuarios',     COUNT(*) FROM usuario;
