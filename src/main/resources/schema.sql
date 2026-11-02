-- ==========================================================
--  Tablas de seguridad (iguales al diagrama del ejercicio)
-- ==========================================================

CREATE TABLE IF NOT EXISTS users (
    user_id  INT(11)     NOT NULL AUTO_INCREMENT,
    username VARCHAR(45) NOT NULL,
    password VARCHAR(64) NOT NULL,   -- BCrypt genera hashes de 60 caracteres
    enabled  TINYINT(4)  NOT NULL DEFAULT 1,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_username (username)
);

CREATE TABLE IF NOT EXISTS roles (
    role_id INT(11)     NOT NULL AUTO_INCREMENT,
    name    VARCHAR(45) NOT NULL,
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_roles_name (name)
);

-- Tabla intermedia de la relacion muchos a muchos:
-- un usuario puede tener varios roles y un rol varios usuarios.
CREATE TABLE IF NOT EXISTS users_roles (
    user_id INT(11) NOT NULL,
    role_id INT(11) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles (role_id)
);

-- ==========================================================
--  Tabla de la aplicacion (lo que se protege con los roles)
-- ==========================================================

CREATE TABLE IF NOT EXISTS productos (
    id          INT(11)       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100)  NOT NULL,
    marca       VARCHAR(60)   NOT NULL,
    pais_origen VARCHAR(60)   NOT NULL,
    precio      DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id)
);
