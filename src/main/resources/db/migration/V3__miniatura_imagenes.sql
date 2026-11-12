-- Version chica (JPEG ~640px) de cada foto, para los listados (catalogo, home, novedades).
-- Se completa al subir cada imagen; las existentes las rellena MiniaturaBackfill al arrancar.
ALTER TABLE mueble_imagenes ADD COLUMN miniatura_imagenes BYTEA;
