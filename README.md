# Sistema de Gestión de Taller Automotriz

**Proyecto integrador — Ingeniería de Sistemas, quinto semestre**  
Universidad Santo Tomás, Seccional Tunja — 2026-II  
**Integrantes:** David Santiago Rojas, Sara Juliana Tarazona y Mayra Rodríguez Ríos.

> **Estado de esta documentación (9 de noviembre de 2026):** actualizado a partir de la copia de `Integrador_5Semestre.zip` recibida en esta revisión. La existencia de clases y rutas demuestra implementación en el código, **no** que todas las funciones hayan superado pruebas. Ver [`docs/MATRIZ_REQUISITOS.md`](docs/MATRIZ_REQUISITOS.md) y [`docs/DIAGNOSTICO_PENDIENTES.md`](docs/DIAGNOSTICO_PENDIENTES.md).

## 1. Descripción y alcance

Aplicación web para administrar usuarios, modelos de vehículos, vehículos, repuestos, inventario y reparaciones de un taller automotriz. Las reparaciones registran fecha, vehículo, mecánico, estado, costo de mano de obra, **varios repuestos con sus cantidades y precios unitarios históricos**, y **un lugar donde se realizó la reparación** (nombre del taller, latitud y longitud). Un vehículo puede tener varias reparaciones y, por consiguiente, varias ubicaciones en su historial; cada reparación solo tiene un punto guardado.

El proyecto sigue la arquitectura de capas y MVC practicada en Bibliotech: vista Thymeleaf → Controller → Service → Repository → H2. Se trabaja con **seis tablas**, según la autorización actual del docente. Los distintos formatos de placa del proyecto se conservan.

## 2. Tecnologías

| Componente | Implementación encontrada |
|---|---|
| Lenguaje | Java 21 |
| Aplicación web | Spring Boot 3.2.5, Spring MVC y Tomcat embebido |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | H2 **en archivo**, `./data/tallerdb` (persistente entre reinicios) |
| Vistas | Thymeleaf y water.css |
| Interacción | JavaScript separado en `static/js`; existe también JavaScript inline heredado en algunas plantillas |
| Mapa | Leaflet 1.9.4 + OpenStreetMap |
| Seguridad | Spring Security (sesiones y formulario de login), BCrypt, roles ADMIN y MECHANIC; **no usa JWT** |
| Recuperación de contraseña | Token temporal y Spring Mail/SMTP; entrega real condicionada a configuración SMTP |
| Construcción | Gradle Kotlin DSL (`build.gradle.kts`) |

## 3. Requisitos previos y ejecución local

- JDK 21 e IntelliJ IDEA (o terminal).
- Internet para descargar dependencias y cargar los mosaicos de OpenStreetMap/Leaflet.
- Desde la carpeta raíz ejecutar **Windows:** `gradlew.bat bootRun` o **macOS/Linux:** `./gradlew bootRun`. También se puede ejecutar `Application.java` desde IntelliJ.
- Abrir `http://localhost:8080/login`.
- Antes de actualizar clases JPA o iniciar pruebas destructivas, detener el servidor y **respaldar `data/`**, pues H2 almacena datos persistentes.

### Usuarios de demostración

`DataInitializer.java` crea tres cuentas **solo si no existen** y si `app.seed-test-users=true`:

| Cuenta de desarrollo | Rol |
|---|---|
| `admin@taller.test` | ADMIN |
| `mecanico1@taller.test` | MECHANIC |
| `mecanico2@taller.test` | MECHANIC |

Las contraseñas de prueba están definidas en `DataInitializer.java`; **no deben mantenerse para un despliegue público**. Como la base es persistente, una cuenta creada o modificada previamente no se reinicializa en cada ejecución.

### Consola H2

La aplicación configura la ruta `/h2-console`, restringida a ADMIN en `SecurityConfig.java`. JDBC: `jdbc:h2:file:./data/tallerdb;AUTO_SERVER=TRUE`, usuario `sa` y contraseña indicada en la configuración local (actualmente vacía en el archivo distribuido). La ruta de la base es relativa al directorio desde el que se inicia la aplicación.

## 4. Configuración relevante

Archivo: `src/main/resources/application.properties`. Variables y propiedades principales:

| Propiedad | Uso |
|---|---|
| `server.port=8080` | Puerto local |
| `spring.datasource.url=jdbc:h2:file:./data/tallerdb;AUTO_SERVER=TRUE` | Persistencia H2 |
| `spring.jpa.hibernate.ddl-auto=update` | Actualización automática del esquema para desarrollo; revisar antes de producción |
| `spring.h2.console.enabled=true` | Consola de H2, protegida para ADMIN |
| `spring.mvc.hiddenmethod.filter.enabled=true` | Formularios HTTP PUT/DELETE usando `_method` |
| `app.upload-dir=uploads/parts` | Almacenamiento local de imágenes |
| `spring.servlet.multipart.max-file-size=10MB` | Límite del servidor; `PartService` aplica la validación de **2 MB** |
| `app.base-url=http://localhost:8080` | Enlace de recuperación; debe cambiarse antes de desplegar públicamente |
| `app.recovery-token-minutes=30` | Duración configurada del enlace |
| `app.seed-test-users=true` | Usuarios de demostración; cambiar a `false` en producción |
| `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | Variables de entorno para SMTP |

**Correo:** `EmailService` intenta enviar por SMTP. Si no se configura o falla, escribe el enlace de recuperación en los registros del servidor para desarrollo: **no equivale a haber entregado un correo**. Evitar exponer tokens en logs públicos.

**Git:** `.gitignore` excluye `data/`, `uploads/`, `build/`, `.gradle/`, `.idea/` y `application-local.properties`. No publicar credenciales de SMTP ni respaldos H2.

## 5. Estructura de carpetas

```text
src/main/java/dev/tarazonarodriguezrojas/taller1/
├── Application.java
├── config/           DataInitializer, SecurityConfig, WebConfig, PasswordChangeInterceptor
├── controller/       Auth, User, VehicleModel, Vehicle, Part, Repair
├── service/          Auth, User, VehicleModel, Vehicle, Part, Repair,
│                     Email, FileStorage, PhoneValidation y auxiliares de seguridad
├── repository/       User, VehicleModel, Vehicle, Part, Repair, RepairPart
└── model/
    ├── constant/     Role, RepairStatus, VehicleType, ServiceType, PlateColor, PlateRules
    ├── dto/          UserDTO, VehicleModelDTO, VehicleDTO, PartDTO,
    │                 RepairDTO, RepairItemDTO, MechanicDTO, PlateModelDTO, LocationDTO
    └── entity/       User, VehicleModel, Vehicle, Part, Repair, RepairPart, RepairPartId
src/main/resources/
├── application.properties
├── templates/
│   ├── auth/         login, recuperación, cambio de contraseña, acceso denegado
│   ├── fragments/    nav.html, ui.html
│   ├── models/       list.html, form.html, detail.html
│   ├── vehicles/     list.html, form.html, detail.html, model-by-plate.html
│   ├── parts/        list.html, form.html, detail.html, compatible.html
│   ├── repairs/      list.html, form.html, detail.html, location.html, map.html
│   ├── users/        list.html, form.html, detail.html
│   └── index.html
└── static/
    ├── css/ui.css
    └── js/           ui.js, password-strength.js, phone-validation.js,
                     vehicles.js, repairs.js, repair-location.js,
                     repair-detail-map.js, repair-history-map.js
```

## 6. Modelo de datos: seis tablas

| Tabla | Entidad JPA | Datos y relaciones principales |
|---|---|---|
| `users` | `User` | Datos, correo, teléfono, contraseña cifrada, rol, estado activo, token de recuperación y `must_change_password`; un mecánico tiene muchas reparaciones |
| `model` | `VehicleModel` | Nombre, marca y `vehicle_type`; un modelo tiene vehículos y repuestos |
| `vehicle` | `Vehicle` | Placa, `service_type`, color de placa, propietario, modelo (`model_id`) |
| `part` | `Part` | Nombre, descripción, precio, EAN, stock, `image_url`, modelo compatible (`model_id`) |
| `repair` | `Repair` | Fecha, tipo, estado, costo de mano de obra, vehículo, mecánico, `workshop_name`, `workshop_latitude`, `workshop_longitude` |
| `repair_part` | `RepairPart` | Clave compuesta (`repair_id`, `part_id`), `quantity` y `unit_price` histórico |

**Nota de consistencia con el diagrama académico:** el diagrama proporcionado omite algunos atributos que sí existen en las entidades (por ejemplo `vehicle_type`, `service_type`, `must_change_password`, `quantity` y `unit_price`). No se han creado tablas adicionales. Antes de entregar el modelo definitivo se debe actualizar el diagrama para reflejar el esquema real; consultar `docs/DIAGNOSTICO_PENDIENTES.md`.

## 7. Funciones y permisos

| Actor | Acciones esperadas implementadas en código |
|---|---|
| ADMIN | Administración de usuarios, CRUD de modelos, vehículos, repuestos y reparaciones, imágenes y consultas |
| MECHANIC | Consulta de modelos y repuestos, registro/edición de vehículos, registro de reparaciones y edición de las propias; sin eliminar registros |

Spring Security define reglas de acceso en `SecurityConfig`. `RepairController` comprueba que el mecánico sea responsable antes de editar una reparación o su ubicación. **Estas restricciones deben validarse mediante pruebas de acceso directo**, no solo por botones ocultos.

### Teléfonos y correos

`PhoneValidation.java` comprueba formatos internacionales de **Colombia (+57), México (+52), EE. UU./Canadá (+1), España (+34), Chile (+56) y Perú (+51)**; el teléfono se guarda con el prefijo internacional en la misma columna. Los formularios usan `phone-validation.js`. `UserService` valida la sintaxis del correo y la unicidad. **No se verifica automáticamente si la línea o el buzón existen**.

### Inventario y reparaciones

- Una reparación admite **varios repuestos diferentes**, cada uno con cantidad mayor que cero.
- Se valida la compatibilidad por modelo y el stock disponible en el servidor.
- El stock se descuenta al guardar reparaciones no canceladas; se restituye o ajusta al cancelar, eliminar o editar la reparación.
- `unit_price` se guarda en `repair_part` como precio histórico; el costo total suma mano de obra y subtotales de los repuestos.
- El algoritmo actual usa lecturas y actualizaciones JPA convencionales; **no se ha demostrado protección ante concurrencia** (dos usuarios consumiendo el mismo último stock al mismo tiempo).

### Ubicaciones y mapa

- `repair` guarda una única ubicación (taller, latitud, longitud) por reparación.
- Al crear o editar la reparación se puede ingresar coordenadas, marcar en Leaflet o solicitar la ubicación del dispositivo (con autorización del navegador).
- El mapa `/repairs/map?plate=ABC-123` filtra por placa y muestra **todas las reparaciones con coordenadas** de esa placa; un mismo vehículo puede tener varios puntos históricos.
- Si dos reparaciones tienen las mismas coordenadas, `repair-history-map.js` separa visualmente los marcadores sin cambiar los datos guardados.
- `repairs/location.html` sigue existiendo como una ruta adicional de edición de ubicación, aunque la captura principal se hace ahora en `repairs/form.html`.

## 8. Rutas importantes

| Ruta | Función |
|---|---|
| `/login`, `/logout` | Inicio y cierre de sesión (logout por POST) |
| `/forgot-password`, `/reset-password`, `/change-password` | Recuperación y cambio de contraseña |
| `/users`, `/users/new`, `/users/{id}`, `/users/{id}/edit` | Usuarios (ADMIN) |
| `/models`, `/models/new`, `/models/{id}`, `/models/{id}/edit` | Modelos |
| `/vehicles`, `/vehicles/new`, `/vehicles/{id}`, `/vehicles/model-by-plate` | Vehículos y búsqueda por placa |
| `/parts`, `/parts/new`, `/parts/{id}`, `/parts/compatible` | Repuestos e identificación de compatibles |
| `/repairs`, `/repairs/new`, `/repairs/{id}`, `/repairs/{id}/edit` | Historial y formulario de reparaciones |
| `/repairs/{id}/location` | Edición independiente de la ubicación de una reparación |
| `/repairs/map`, `/repairs/map?plate=ABC-123` | Mapa de ubicaciones y filtro por placa |

Las operaciones de escritura utilizan POST, PUT y DELETE según el controlador y la configuración de `_method`.

## 9. Estado de los 30 requisitos

Se localizaron rutas/clases/vistas relacionadas con los **30 requisitos funcionales**, pero **no se ejecutó una prueba integral** en esta revisión. El detalle por RF, evidencia y pruebas pendientes está en [`docs/MATRIZ_REQUISITOS.md`](docs/MATRIZ_REQUISITOS.md).

**Verificaciones reportadas durante el trabajo:** registro y consulta de repuestos y reparaciones; el usuario informó que estas funciones operaban. Esas verificaciones manuales **no sustituyen** pruebas de regresión sobre este ZIP final ni justifican afirmar cobertura del 100 %.

## 10. Pruebas recomendadas

1. Ejecutar `gradlew.bat test` (Windows) o `./gradlew test`: el ZIP **no incluye clases en `src/test`**, por lo que es necesario crear pruebas automatizadas posteriormente.
2. Probar registro y edición de modelos, vehículos, usuarios y repuestos con datos válidos e inválidos.
3. Probar inicio/cierre de sesión, cuenta inactiva, permisos de mecánico y administrador y acceso por URL directa.
4. Probar reparación sin repuestos, con varios repuestos, cantidades inválidas, incompatibilidad y falta de stock.
5. Probar ediciones de cantidades, cancelaciones y eliminaciones y revisar stock antes y después.
6. Probar búsqueda por placa en mapa con dos reparaciones en lugares distintos y con coordenadas iguales.
7. Probar subida y reemplazo de imágenes permitidas y prohibidas.
8. Probar Toast, cambio de tema, persistencia del tema y recuperación real de contraseña mediante SMTP configurado.

Guía detallada: [`docs/PLAN_PRUEBAS.md`](docs/PLAN_PRUEBAS.md).

## 11. Riesgos y pendientes de revisión

- **Desfase documentación/diagrama:** actualizar el diagrama de seis tablas y los textos de la portada `index.html` que aún hablan de una tienda de repuestos y marcan todo como «Listo».
- **Correo:** confirmar la entrega SMTP real; sin SMTP el enlace se escribe en registros.
- **Seguridad:** eliminar usuarios de demostración en despliegues públicos, desactivar H2-console y dejar de exponer enlaces de recuperación en logs.
- **Inventario:** verificar concurrencia, transacciones, devoluciones y consistencia en cambios de estado.
- **Pruebas:** actualmente no hay clases de test en el ZIP.
- **Validaciones:** revisar EAN (se comprueban 13 dígitos, no dígito de control), existencia real de contactos y casos excepcionales de cada formulario.

Más detalle y prioridades: [`docs/DIAGNOSTICO_PENDIENTES.md`](docs/DIAGNOSTICO_PENDIENTES.md).

---

**Mantenimiento de esta documentación:** actualizar cada estado en la matriz solamente después de ejecutar el caso de prueba y registrar su resultado. **No se ha modificado código de aplicación en esta entrega documental.**
