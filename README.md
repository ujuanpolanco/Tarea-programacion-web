# Ejercicio: Acceso basado en roles con Spring Security

**Curso:** Programación Web — Universidad Surcolombiana
**Tecnologías:** Java 21, Spring Boot 3.5.3, Spring Security 6, Spring Data JPA, Thymeleaf, MySQL, Maven

## 1. Qué pedía el ejercicio

1. **Acceso basado en roles o perfiles.** Cada usuario tiene uno o varios roles (USER, CREATOR, EDITOR, ADMIN) y según eso puede o no entrar a ciertas URLs.
2. **Lectura desde DB.** Los usuarios, contraseñas y roles no van quemados en el código: se leen de MySQL con las tablas `users`, `roles` y `users_roles` del diagrama.
3. **Páginas personalizadas.** Login propio y página propia de "acceso denegado" (403), no las que trae Spring por defecto.

Para tener algo que proteger hice un pequeño **gestor de productos** (listar, crear, editar y eliminar), que son justo las URLs de la diapositiva: `/`, `/new`, `/edit/**` y `/delete/**`.

## 2. Matriz de permisos

| URL | USER | CREATOR | EDITOR | ADMIN |
|---|:-:|:-:|:-:|:-:|
| `/` (ver productos) | ✔ | ✔ | ✔ | ✔ |
| `/new` (crear) | ✘ | ✔ | ✘ | ✔ |
| `/edit/{id}` (editar) | ✘ | ✘ | ✔ | ✔ |
| `/delete/{id}` (eliminar) | ✘ | ✘ | ✘ | ✔ |

Si un usuario entra a una URL sin el rol necesario, ve la página personalizada **403**.

## 3. Usuarios de prueba

Todos tienen la contraseña **`123456`** (en la base solo está guardado el hash BCrypt).

| Usuario | Roles | Para qué sirve |
|---|---|---|
| `usuario` | USER | Solo puede ver la lista |
| `creador` | CREATOR | Ver y crear |
| `editor` | EDITOR | Ver y editar |
| `admin` | ADMIN | Todo |
| `juan` | CREATOR + EDITOR | Muestra la relación muchos a muchos: suma los permisos de los dos roles |
| `inactivo` | ADMIN | Tiene `enabled = 0`, no puede iniciar sesión aunque la contraseña sea correcta |

## 4. Estructura del proyecto

```
SeguridadRoles
├── pom.xml
└── src
    ├── main
    │   ├── java/co/edu/usco/seguridadroles
    │   │   ├── SeguridadRolesApplication.java   -> clase main
    │   │   ├── config/WebSecurityConfig.java     -> reglas de seguridad (lo de la diapositiva)
    │   │   ├── security/UserDetailsServiceImpl.java -> lee el usuario y sus roles de MySQL
    │   │   ├── model/       User, Role, Producto (entidades JPA)
    │   │   ├── repository/  UserRepository, ProductoRepository
    │   │   └── controller/  LoginController (login y 403), ProductoController (CRUD)
    │   └── resources
    │       ├── application.properties  -> conexión a MySQL
    │       ├── schema.sql              -> crea las tablas del diagrama
    │       ├── data.sql                -> roles, usuarios y productos de prueba
    │       ├── templates/              -> login.html, 403.html, index.html, producto_form.html
    │       └── static/css/estilos.css
    └── test
        └── java/.../SeguridadRolesApplicationTests.java -> pruebas de la matriz de permisos
```

## 5. Cómo funciona (explicado paso a paso)

### 5.1 La base de datos
`schema.sql` crea las tres tablas tal como están en el diagrama:

- `users (user_id, username, password, enabled)`
- `roles (role_id, name)`
- `users_roles (user_id, role_id)`: es la tabla intermedia, porque la relación es **muchos a muchos** (un usuario puede tener varios roles y un rol lo tienen varios usuarios).

La columna `password` es `VARCHAR(64)` porque BCrypt genera hashes de 60 caracteres, así que cabe.

En Java esto se ve en `User.java` con `@ManyToMany` + `@JoinTable(name = "users_roles")`.

### 5.2 Lectura desde la DB (el login)
Cuando alguien envía el formulario de login, Spring Security llama a `UserDetailsServiceImpl.loadUserByUsername(...)`. Ese método:

1. Busca el usuario en MySQL con `userRepository.findByUsername(username)`.
2. Si no existe, lanza `UsernameNotFoundException` (Spring lo muestra como "usuario o contraseña incorrectos").
3. Si existe, le devuelve a Spring el hash de la contraseña, si está habilitado (`enabled`) y la lista de roles convertida en **autoridades** (`USER`, `ADMIN`, etc.).

La comparación de la contraseña la hace Spring con el `BCryptPasswordEncoder` que definí como `@Bean` en `WebSecurityConfig`.

### 5.3 Las reglas de acceso
Las reglas son las mismas de la diapositiva. **La única diferencia es la sintaxis**, porque la diapositiva está en Spring Security 5 y Spring Boot 3 trae Spring Security 6, donde `antMatchers()`, `authorizeRequests()` y `.and()` ya no existen:

| Diapositiva (Spring Security 5) | Este proyecto (Spring Security 6) |
|---|---|
| `http.authorizeRequests()` | `http.authorizeHttpRequests(auth -> auth ...)` |
| `.antMatchers("/new").hasAnyAuthority(...)` | `.requestMatchers("/new").hasAnyAuthority(...)` |
| `.and().formLogin().permitAll()` | `.formLogin(form -> form.loginPage("/login").permitAll())` |
| `.and().logout().permitAll()` | `.logout(logout -> logout.permitAll())` |
| `.and().exceptionHandling().accessDeniedPage("/403")` | `.exceptionHandling(ex -> ex.accessDeniedPage("/403"))` |

Con lambdas cada parte va en su propio bloque, por eso ya no hace falta `.and()`.

Usé `hasAuthority` / `hasAnyAuthority` (como en la diapositiva) y no `hasRole`, porque `hasRole("ADMIN")` busca la autoridad `ROLE_ADMIN` con prefijo, y en la tabla `roles` el nombre está guardado sin prefijo (`ADMIN`).

**El orden importa:** Spring revisa las reglas de arriba hacia abajo y usa la primera que coincide; por eso `anyRequest().authenticated()` va de última.

### 5.4 Páginas personalizadas
- **`/login`** (`login.html`): formulario propio. Muestra mensajes distintos si la contraseña es incorrecta (`?error`), si la cuenta está deshabilitada (`?deshabilitado`) o si se cerró sesión (`?logout`).
- **`/403`** (`403.html`): dice qué usuario es y qué rol tiene, y que no tiene permiso.
- En `index.html` usé `sec:authorize` (de *thymeleaf-extras-springsecurity6*) para **ocultar los botones** que el usuario no puede usar. Ojo: esto es solo visual; la seguridad real está en `WebSecurityConfig`, por eso si alguien escribe `/delete/1` a mano igual le sale 403.

### 5.5 Detalles que revisé
- **Guardar también está protegido.** Los formularios se envían por POST a la misma URL que los muestra (`/new`, `/edit/{id}`). Así la misma regla protege mostrar el formulario y guardar los datos. Si hubiera hecho un `/save` aparte, un USER podría guardar enviando el POST directo, porque `/save` no está en las reglas.
- **Eliminar es POST, no un enlace.** Un enlace GET que borra datos se puede ejecutar con solo abrir una URL. Con POST además se exige el token CSRF.
- **Error que encontré y corregí:** cuando se negaba un POST (por ejemplo, un CREATOR intentando guardar `/edit/1`), salía un error **405** y no la página 403. Pasaba porque Spring hace un *forward* a `/403` con el **mismo método** de la petición rechazada (POST), y yo tenía `/403` con `@GetMapping`. Lo cambié a `@RequestMapping("/403")`, que acepta cualquier método.
- **CSRF:** está activado (viene así por defecto). Los formularios con `th:action` agregan el token solos. Por eso el *logout* es un formulario POST y no un enlace.

## 6. Cómo ejecutarlo en Eclipse

**Requisitos:** JDK 21 o superior, MySQL encendido y Eclipse IDE for Enterprise Java (trae Maven integrado).

1. **MySQL (ya está hecho en mi PC):** la aplicación no usa `root`, sino un usuario creado solo para este proyecto, que solo tiene permisos sobre la base `seguridad_roles`. Se creó en Workbench con:
   ```sql
   CREATE USER IF NOT EXISTS 'seguridad_app'@'localhost' IDENTIFIED BY 'SegRoles_2026';
   GRANT ALL PRIVILEGES ON seguridad_roles.* TO 'seguridad_app'@'localhost';
   ```
   Y en `application.properties` quedó:
   ```properties
   spring.datasource.username=${DB_USER:seguridad_app}
   spring.datasource.password=${DB_PASSWORD:SegRoles_2026}
   ```
   Si el proyecto se corre en otro computador (por ejemplo el del profe), hay que ejecutar esas dos líneas SQL allá también, o cambiar el usuario y la contraseña en `application.properties`. **No hay que crear la base de datos a mano:** la URL tiene `createDatabaseIfNotExist=true`, y al arrancar se ejecutan `schema.sql` y `data.sql`.
2. **Importar:** `File > Import... > Maven > Existing Maven Projects`, seleccionar la carpeta `SeguridadRoles` y dar *Finish*. La primera vez Eclipse descarga las dependencias (abajo a la derecha se ve el progreso).
3. **Ejecutar:** clic derecho sobre `SeguridadRolesApplication.java` → `Run As > Java Application`. En la consola debe aparecer `Started SeguridadRolesApplication`.
4. **Probar:** abrir <http://localhost:8080> en el navegador, iniciar sesión con los usuarios de la tabla de arriba y probar las URLs `/new`, `/edit/1` y `/delete/1`.
5. **Pruebas automáticas:** clic derecho sobre `SeguridadRolesApplicationTests.java` → `Run As > JUnit Test`. Las pruebas usan una base H2 en memoria con los mismos scripts, así que no necesitan MySQL.

### Problemas comunes
- **`Access denied for user 'seguridad_app'@'localhost'`**: ese usuario no existe en el MySQL de ese computador; ejecutar las dos líneas SQL del paso 1.
- **`Port 8080 was already in use`**: otra aplicación (por ejemplo el Tomcat de clase) está usando el puerto. Ciérrala o cambia `server.port` en `application.properties`.
- **Eclipse marca errores rojos en todo después de importar**: clic derecho al proyecto → `Maven > Update Project...` → OK.
- **Si quiero reiniciar los datos de prueba**: en Workbench ejecutar `DROP DATABASE seguridad_roles;` y volver a correr la aplicación.

## 7. Pruebas realizadas

`SeguridadRolesApplicationTests` comprueba (10 pruebas, todas pasan):

- Login correcto con un usuario leído de la base, y que queda con la autoridad `ADMIN`.
- Contraseña incorrecta → `/login?error`.
- Usuario deshabilitado → `/login?deshabilitado`.
- Sin sesión → redirige al login.
- USER: ve la lista, pero `/new`, `/edit/1` y `/delete/1` dan 403 (y se muestra la página `/403`).
- CREATOR: puede crear, no editar ni eliminar.
- EDITOR: puede editar, no crear ni eliminar.
- `juan` (CREATOR + EDITOR): puede crear y editar, pero no eliminar.
- ADMIN: entra a todo.

Además lo probé con la aplicación corriendo contra **MySQL real**:

- Al arrancar se crearon las 4 tablas con 6 usuarios, 4 roles, 7 asignaciones en `users_roles` y 4 productos.
- Al reiniciar la aplicación no se duplicó nada (gracias a `CREATE TABLE IF NOT EXISTS` e `INSERT IGNORE`).
- Entré con cada usuario y revisé `/`, `/new`, `/edit/1` y `/delete`; todos dieron el resultado de la matriz de la sección 2.
- Con `admin` creé un producto, verifiqué en MySQL que quedó guardado y luego lo eliminé.
- También revisé qué botones aparecen con cada rol y que el cierre de sesión funcione.
