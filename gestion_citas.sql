-- =============================================================================
-- Universidad Americana (UAM) - Ingenieria de Sistemas
-- Asignatura: Programacion de Aplicaciones de Escritorio
-- Caso de Estudio: TecnoAsistencia Nicaragua S.A.
-- Evaluacion Practica: JavaFX y JDBC
-- Base de Datos: gestion_citas
-- =============================================================================

-- 1. Creacion de la base de datos (Ejecutar solo si se trabaja en servidor local PostgreSQL)
-- Nota: En NeonDB la base de datos ya viene creada por defecto (generalmente llamada 'neondb').
-- CREATE DATABASE gestion_citas;

-- 2. Limpieza de tablas previas (en orden inverso de dependencia)
DROP TABLE IF EXISTS cita_tecnica CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;

-- 3. Tabla: cliente
-- Reglas:
-- - id: Identificador unico autoincremental (Clave Primaria)
-- - nombres: Maximo 80 caracteres, obligatorio
-- - apellidos: Maximo 80 caracteres, obligatorio
-- - telefono: Maximo 20 caracteres, obligatorio
-- - correo: Maximo 120 caracteres, unico y obligatorio
-- - activo: Booleano obligatorio (por defecto true)
CREATE TABLE cliente (
    id SERIAL PRIMARY KEY,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(120) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 4. Tabla: cita_tecnica
-- Reglas:
-- - id: Identificador unico autoincremental (Clave Primaria)
-- - cliente_id: Clave foranea hacia cliente(id). 
--   ON DELETE RESTRICT prohibe expresamente la eliminacion de un cliente que tenga citas asociadas.
-- - fecha: Fecha obligatoria (DATE)
-- - hora: Hora obligatoria (TIME)
-- - tipo_servicio: Tipo de atencion o mantenimiento, obligatorio
-- - descripcion: Detalle del equipo o problema reportado, maximo 255 caracteres
-- - estado: Solo permite 'Pendiente', 'En proceso', 'Finalizada' o 'Cancelada'
CREATE TABLE cita_tecnica (
    id SERIAL PRIMARY KEY,
    cliente_id INT NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    tipo_servicio VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT fk_cita_cliente FOREIGN KEY (cliente_id) 
        REFERENCES cliente (id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    CONSTRAINT chk_estado_cita CHECK (estado IN ('Pendiente', 'En proceso', 'Finalizada', 'Cancelada'))
);

-- 5. Indices recomendados para optimizar busquedas y relaciones
CREATE INDEX idx_cliente_correo ON cliente(correo);
CREATE INDEX idx_cliente_nombres_apellidos ON cliente(nombres, apellidos);
CREATE INDEX idx_cita_cliente_id ON cita_tecnica(cliente_id);
CREATE INDEX idx_cita_estado ON cita_tecnica(estado);
CREATE INDEX idx_cita_fecha ON cita_tecnica(fecha);

-- 6. Datos de prueba iniciales (Opcionales para validar la conexion y las tablas)
INSERT INTO cliente (nombres, apellidos, telefono, correo, activo) VALUES
('Carlos Alberto', 'Mendoza Lopez', '8888-1111', 'carlos.mendoza@email.com', true),
('Maria Elena', 'Robleto Ruiz', '8777-2222', 'maria.robleto@email.com', true),
('Jose Daniel', 'Torres Silva', '8666-3333', 'jose.torres@email.com', true);

INSERT INTO cita_tecnica (cliente_id, fecha, hora, tipo_servicio, descripcion, estado) VALUES
(1, CURRENT_DATE, '09:00:00', 'Mantenimiento Preventivo', 'Limpieza profunda de componentes y cambio de pasta termica', 'Pendiente'),
(2, CURRENT_DATE, '11:30:00', 'Reparacion de Hardware', 'Diagnostico de pantalla azul y cambio de disco a SSD NVMe', 'En proceso'),
(3, CURRENT_DATE + INTERVAL '1 day', '14:00:00', 'Instalacion de Software', 'Instalacion de sistema operativo y software de oficina', 'Pendiente');
