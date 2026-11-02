-- ==========================================================
--  Datos de prueba
--  INSERT IGNORE: si el registro ya existe (misma llave), no hace nada.
--  Asi el script se puede ejecutar en cada arranque sin duplicar.
-- ==========================================================

-- Los nombres de los roles son exactamente las autoridades que usa
-- WebSecurityConfig en hasAnyAuthority(...) / hasAuthority(...).
INSERT IGNORE INTO roles (role_id, name) VALUES
    (1, 'USER'),
    (2, 'CREATOR'),
    (3, 'EDITOR'),
    (4, 'ADMIN');

-- La contrasena de todos los usuarios es: 123456
-- En la base solo se guarda el hash BCrypt, nunca el texto plano.
INSERT IGNORE INTO users (user_id, username, password, enabled) VALUES
    (1, 'usuario', '$2a$10$vNbljdorlPz5MJGQsmHr5uhnc7wHZIqa6kLLp1hXljYsHOTDw8MDK', 1),
    (2, 'creador', '$2a$10$vNbljdorlPz5MJGQsmHr5uhnc7wHZIqa6kLLp1hXljYsHOTDw8MDK', 1),
    (3, 'editor',  '$2a$10$vNbljdorlPz5MJGQsmHr5uhnc7wHZIqa6kLLp1hXljYsHOTDw8MDK', 1),
    (4, 'admin',   '$2a$10$vNbljdorlPz5MJGQsmHr5uhnc7wHZIqa6kLLp1hXljYsHOTDw8MDK', 1),
    -- Usuario con dos roles a la vez, para mostrar la relacion muchos a muchos
    (5, 'juan',    '$2a$10$vNbljdorlPz5MJGQsmHr5uhnc7wHZIqa6kLLp1hXljYsHOTDw8MDK', 1),
    -- Usuario deshabilitado (enabled = 0): no debe poder iniciar sesion
    (6, 'inactivo','$2a$10$vNbljdorlPz5MJGQsmHr5uhnc7wHZIqa6kLLp1hXljYsHOTDw8MDK', 0);

INSERT IGNORE INTO users_roles (user_id, role_id) VALUES
    (1, 1),   -- usuario  -> USER
    (2, 2),   -- creador  -> CREATOR
    (3, 3),   -- editor   -> EDITOR
    (4, 4),   -- admin    -> ADMIN
    (5, 2),   -- juan     -> CREATOR
    (5, 3),   -- juan     -> EDITOR
    (6, 4);   -- inactivo -> ADMIN (pero esta deshabilitado)

INSERT IGNORE INTO productos (id, nombre, marca, pais_origen, precio) VALUES
    (1, 'Portatil IdeaPad 3',  'Lenovo',  'China',          2499000.00),
    (2, 'Mouse inalambrico',   'Logitech','Suiza',            89900.00),
    (3, 'Monitor 24 pulgadas', 'Samsung', 'Corea del Sur',   649000.00),
    (4, 'Cafe molido 500 g',   'Juan Valdez', 'Colombia',     32000.00);
