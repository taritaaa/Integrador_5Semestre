# Sistema de Gestión de Taller Automotriz

Proyecto Integrador - Desarrollo Empresarial
Ingeniería de Sistemas, Universidad Santo Tomás, Seccional Tunja (2026 - II)

Autores: David Santiago Rojas, Sara Juliana Tarazona, Mayra Rodríguez Ríos

## Descripción

Aplicación web que centraliza la operación diaria de un taller de reparación automotriz en Colombia: modelos de vehículos, vehículos identificados por placa, repuestos con su stock e historial de reparaciones. Los usuarios son el **administrador** (acceso total) y el **mecánico** (acceso operativo limitado).

## Tecnologías

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.2.5 (Web, Data JPA) |
| Vistas | Thymeleaf + water.css |
| Base de datos | H2 en memoria |
| Build | Gradle (Kotlin DSL) |

## Cómo ejecutar

1. Ejecutar la tarea `bootRun` de Gradle (o `./gradlew bootRun`).
2. Abrir el navegador en **http://localhost:8080/**.
3. Consola de la base de datos: **http://localhost:8080/h2-console**
   - JDBC URL: `jdbc:h2:mem:tallerdb`
   - Usuario: `sa`
   - Contraseña: vacía

La base de datos está en memoria: los datos se borran cada vez que se reinicia la aplicación.

## Estructura del proyecto

Paquete base: `dev.tarazonarodriguezrojas.taller1`

```
src/main/java/dev/tarazonarodriguezrojas/taller1
├── Application.java
├── controller
│   ├── IndexController.java
│   ├── VehicleModelController.java
│   └── VehicleController.java
├── service
│   ├── VehicleModelService.java
│   └── VehicleService.java
├── repository
│   ├── VehicleModelRepository.java
│   └── VehicleRepository.java
└── model
    ├── constant
    │   ├── PlateColor.java
    │   ├── RepairStatus.java
    │   └── Role.java
    ├── dto
    │   ├── VehicleModelDTO.java
    │   ├── VehicleDTO.java
    │   └── PlateModelDTO.java
    └── entity
        ├── VehicleModel.java
        ├── Vehicle.java
        ├── Part.java
        ├── User.java
        ├── Repair.java
        ├── RepairPart.java
        └── RepairPartId.java

src/main/resources
├── application.properties
└── templates
    ├── index.html
    ├── models   (list, form, detail)
    └── vehicles (list, form, detail, model-by-plate)
```

Cada módulo sigue el mismo patrón, tomado del proyecto de referencia `bibliotech`: repository (consultas), DTO como `record`, service (reglas de negocio) y controller MVC que devuelve plantillas Thymeleaf.

## Modelo de datos

Seis tablas (ver el diagrama de entidades del proyecto):

| Entidad | Tabla | Relaciones |
|---|---|---|
| `VehicleModel` | `model` | 1:N con `Vehicle` y con `Part` |
| `Vehicle` | `vehicle` | N:1 con `VehicleModel`; 1:N con `Repair` |
| `Part` | `part` | N:1 con `VehicleModel`; 1:N con `RepairPart` |
| `User` | `users` | 1:N con `Repair` (como mecánico) |
| `Repair` | `repair` | N:1 con `Vehicle` y con `User`; 1:N con `RepairPart` |
| `RepairPart` | `repair_part` | Llave compuesta (`repair_id`, `part_id`) más `quantity` |

Enums: `PlateColor` (YELLOW, WHITE, RED, BLUE, GREEN), `Role` (ADMIN, MECHANIC) y `RepairStatus` (IN_PROGRESS, COMPLETED, CANCELLED).

## Avance por requisito funcional

### Hecho

| Módulo | RF | Descripción | Ruta |
|---|---|---|---|
| Modelos | RF-08 | Registrar modelo (nombre y marca); rechaza duplicados | `GET /models/new`, `POST /models` |
| Modelos | RF-09 | Listado y detalle de modelos | `GET /models`, `GET /models/{id}` |
| Modelos | RF-10 | Actualizar modelo | `GET /models/{id}/edit`, `PUT /models/{id}` |
| Modelos | RF-11 | Eliminar modelo; informa si tiene vehículos o repuestos asociados | `DELETE /models/{id}` |
| Vehículos | RF-12 | Registrar vehículo con placa, color de placa, propietario y modelo | `GET /vehicles/new`, `POST /vehicles` |
| Vehículos | RF-13 | Listado con filtro por placa o propietario y detalle | `GET /vehicles`, `GET /vehicles/{id}` |
| Vehículos | RF-14 | Actualizar vehículo (la placa no se modifica) | `GET /vehicles/{id}/edit`, `PUT /vehicles/{id}` |
| Vehículos | RF-15 | Eliminar vehículo; informa si tiene reparaciones asociadas | `DELETE /vehicles/{id}` |
| Vehículos | RF-27 | Buscar modelo y marca por placa, indicando el tipo de placa | `GET /vehicles/model-by-plate?plate=...` |

### Pendiente

| Módulo | RF | Descripción |
|---|---|---|
| Repuestos | RF-16 a RF-19 | CRUD de repuestos (precio, EAN, stock) |
| Repuestos | RF-20 | Subir y reemplazar imagen de referencia |
| Repuestos | RF-26 | Buscar repuestos compatibles con un modelo |
| Reparaciones | RF-21 a RF-24 | Registrar, consultar, actualizar y eliminar reparaciones; costo total |
| Reparaciones | RF-25 | Descontar stock al usar un repuesto y devolverlo al cancelar |
| Reparaciones | RF-28 | Ubicación del taller en un mapa |
| Usuarios | RF-06, RF-07 | Registrar, consultar, actualizar y desactivar cuentas |
| Acceso | RF-01 a RF-04 | Inicio y cierre de sesión, recuperación y restablecimiento de contraseña |
| Acceso | RF-05 | Control de acceso por rol (hoy cualquiera puede eliminar) |
| Interfaz | RF-29 | Notificaciones tipo Toast (hoy los mensajes son texto simple) |
| Interfaz | RF-30 | Modo claro y oscuro |

## Reglas de negocio implementadas

**Modelos**
- Nombre y marca obligatorios, máximo 100 caracteres.
- No puede haber dos modelos con el mismo nombre y marca (sin distinguir mayúsculas).
- No se puede eliminar un modelo con vehículos o repuestos asociados.

**Vehículos**
- Color de placa, nombre, documento y teléfono del propietario son obligatorios.
- La placa es única y se guarda en mayúsculas y con guion.
- El modelo debe existir.
- No se puede eliminar un vehículo con reparaciones asociadas.

**Placas** (se pueden escribir con o sin guion; `abc123` se guarda como `ABC-123`)

| Tipo | Formato | Ejemplo |
|---|---|---|
| Carro, camión o tractocamión (tractomula) | `AAA-123` | `ABC-123` |
| Moto | `AAA-12` o `AAA-12A` | `ABC-12D` |
| Remolque | `R-12345` | `R-12345` |
| Semirremolque (tráiler) | `S-12345` | `S-12345` |
| Diplomático | `CC-1234` o `CD-1234` | `CD-1234` |

El filtro de placa del listado compara sin guiones y por coincidencia parcial (`12A` encuentra `ABC-12A`).

## Decisiones de diseño

- **Seis tablas.** El PDF menciona un máximo de cinco. Se mantuvo `repair_part` para permitir varios repuestos por reparación y se le agregó `quantity`. Si el costo histórico importa, conviene agregarle también el precio unitario.
- **`VehicleModel` en lugar de `Model`**, para no confundirse con `org.springframework.ui.Model` en los controllers.
- **Tabla `users`**, porque `USER` es palabra reservada en H2.
- **Formularios con PUT y DELETE** mediante el campo oculto `_method`. Requiere `spring.mvc.hiddenmethod.filter.enabled=true`.
- **Mensajes de resultado** como `successMessage` y `errorMessage`, para cambiar a Toast (RF-29) solo tocando las plantillas.

## Notas de seguridad

- Aún no hay autenticación: cualquier persona que abra la aplicación puede registrar y eliminar datos.
- No subir credenciales reales en `application.properties` si el repositorio es público.

## Siguientes pasos

1. Repuestos (RF-16 a RF-20 y RF-26), incluida la subida de imagen.
2. Reparaciones (RF-21 a RF-25 y RF-28).
3. Usuarios, sesión y roles (RF-01 a RF-07), con Spring Security.
4. Toast y modo oscuro (RF-29 y RF-30).
