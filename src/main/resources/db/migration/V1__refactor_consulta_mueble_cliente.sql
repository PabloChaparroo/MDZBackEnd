-- Migración: reemplaza "solicitar visita" por "consulta" y simplifica Mueble/Cliente.
--
-- El proyecto NO usa Flyway/Liquibase todavía (el esquema se maneja con
-- spring.jpa.hibernate.ddl-auto=update). Ese modo crea tablas/columnas nuevas
-- automáticamente al arrancar, pero NUNCA elimina columnas ni tablas existentes.
-- Por eso este script cubre justamente lo que Hibernate no hace solo:
--   a) eliminar la tabla y columnas que ya no corresponden.
--   b) crear la tabla "consulta" (Hibernate también la crearía sola al arrancar
--      con el nuevo Entity, pero se deja explícita acá para poder aplicar esta
--      migración de forma independiente, o de cara a adoptar Flyway más adelante).
--
-- Cómo ejecutarlo ahora mismo (elegí una opción):
--   mysql -h localhost -P 3306 -u root -proot mdzmueblesbd < src/main/resources/db/migration/V1__refactor_consulta_mueble_cliente.sql
--   o pegando el contenido en un cliente MySQL (Workbench, DBeaver, etc.)
--
-- Ejecutar ANTES de levantar el backend con el código ya refactorizado, así
-- evitás que Hibernate intente convivir un momento con las columnas viejas.

-- 1) Eliminar la tabla vieja de "solicitar visita" (reemplazada por "consulta")
DROP TABLE IF EXISTS solitar_visita;

-- 2) Simplificar "mueble": ya no es un dato fijo del catálogo (muebles a medida)
ALTER TABLE mueble
    DROP COLUMN IF EXISTS dimension,
    DROP COLUMN IF EXISTS tipo_madera,
    DROP COLUMN IF EXISTS precio;

-- 3) Simplificar "cliente": ya no maneja estados
ALTER TABLE cliente
    DROP COLUMN IF EXISTS estado_cliente;

-- 4) Crear la nueva tabla "consulta"
CREATE TABLE IF NOT EXISTS consulta (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    mueble_id BIGINT NOT NULL,
    mensaje_consulta VARCHAR(255),
    fecha_hora_alta_consulta DATETIME(6),
    fecha_hora_modificacion_consulta DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_consulta_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_consulta_mueble FOREIGN KEY (mueble_id) REFERENCES mueble (id)
) ENGINE = InnoDB;
