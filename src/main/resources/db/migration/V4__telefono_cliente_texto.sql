-- El teléfono pasa a ser texto: 10 dígitos no entran en INT y así conserva "+54" y ceros
ALTER TABLE cliente MODIFY telefono_cliente VARCHAR(30) NULL;
UPDATE cliente SET telefono_cliente = NULL WHERE telefono_cliente = '0';
