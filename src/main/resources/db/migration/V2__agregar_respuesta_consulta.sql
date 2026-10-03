-- Agrega la respuesta del administrador a una consulta.
-- Igual que V1: Hibernate (ddl-auto=update) crearía esta columna solo al arrancar,
-- pero se deja el script explícito para poder aplicarlo de forma independiente.
--
-- Cómo ejecutarlo:
--   mysql -h localhost -P 3306 -u root -proot mdzmueblesbd < src/main/resources/db/migration/V2__agregar_respuesta_consulta.sql

ALTER TABLE consulta
    ADD COLUMN IF NOT EXISTS respuesta TEXT NULL;
