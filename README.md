# 🔧 Sistema de Gestión de Taller Automotriz

**Proyecto Integrador · Desarrollo Empresarial**
Ingeniería de Sistemas · Universidad Santo Tomás, Seccional Tunja · 2026 - II

**Autores:** David Santiago Rojas · Sara Juliana Tarazona · Mayra Rodríguez Ríos

---

## 📑 Tabla de contenido

1. [Descripción](#-descripción)
2. [Problema que resuelve](#-problema-que-resuelve)
3. [Roles de usuario](#-roles-de-usuario)
4. [Tecnologías](#-tecnologías)
5. [Cómo ejecutar el proyecto](#-cómo-ejecutar-el-proyecto)
6. [Configuración](#-configuración)
7. [Estructura del proyecto](#-estructura-del-proyecto)
8. [Modelo de datos](#-modelo-de-datos)
9. [Avance por requisito funcional](#-avance-por-requisito-funcional)
10. [Rutas de la aplicación](#-rutas-de-la-aplicación)
11. [Reglas de negocio implementadas](#-reglas-de-negocio-implementadas)
12. [Cómo probar lo implementado](#-cómo-probar-lo-implementado)
13. [Decisiones de diseño](#-decisiones-de-diseño)
14. [Problemas frecuentes](#-problemas-frecuentes)
15. [Notas de seguridad](#-notas-de-seguridad)
16. [Siguientes pasos](#-siguientes-pasos)

---

## 📖 Descripción

Aplicación web que centraliza la operación diaria de un taller de reparación automotriz en Colombia. Permite registrar **modelos de vehículos**, **vehículos** (identificados por su placa colombiana), **repuestos** con su stock e imagen de referencia, y el **historial de reparaciones** con su mecánico, estado, costo total y la ubicación en un mapa de la tienda donde se compró el repuesto.

Restricciones del contexto: desarrollo de aproximadamente un mes por un equipo de estudiantes.

## 🎯 Problema que resuelve

Los talleres gestionan su operación de forma dispersa: cuadernos u hojas de cálculo, inventario de repuestos llevado a mano y sin una forma rápida de saber qué repuestos sirven para cada modelo. Esto genera errores de digitación, pérdida de trazabilidad, inventario desactualizado y tiempos de espera largos.

Con este sistema el taller puede:

- Reducir errores de registro.
- Consultar la compatibilidad de repuestos en segundos.
- Mantener el stock actualizado automáticamente al registrar, cancelar o eliminar reparaciones.
- Contar con un historial completo por vehículo, con su costo total.

## 👥 Roles de usuario

| Rol | Descripción | Puede hacer |
|---|---|---|
| **Administrador** | Responsable del taller, acceso total | Gestionar usuarios, CRUD completo de modelos, vehículos, repuestos y reparaciones, subir imágenes de repuestos y **es el único que elimina registros** |
| **Mecánico** | Técnico que atiende los vehículos, acceso operativo | Consultar modelos y repuestos, registrar/consultar/actualizar vehículos, registrar y consultar reparaciones (solo actualiza las que tiene asignadas) y usar las búsquedas. No elimina ni gestiona usuarios |

> ⚠️ La restricción por rol (RF-05) y el inicio de sesión (RF-01) **aún no están implementados**. Hoy cualquiera que abra la aplicación puede ejecutar todas las acciones, incluidas las de administrador.

## 🛠 Tecnologías

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.2.5 (Web, Data JPA) |
| Vistas | Thymeleaf + [water.css](https://watercss.kognise.dev/) |
| Mapas | [Leaflet](https://leafletjs.com/) + OpenStreetMap (sin clave de API) |
| Base de datos | H2 en memoria |
| Build | Gradle (Kotlin DSL) |
| Servidor | Tomcat embebido (puerto 8080) |

Dependencias en `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
```

## ▶ Cómo ejecutar el proyecto

**Requisitos:** JDK 21 instalado y conexión a internet (para las librerías de Gradle y para los mapas).

1. Clonar el repositorio y abrirlo en IntelliJ IDEA.
2. Esperar a que Gradle descargue las dependencias.
3. Ejecutar la tarea `bootRun` (o desde la terminal: `./gradlew bootRun`, en Windows `gradlew.bat bootRun`).
4. Abrir el navegador en **http://localhost:8080/**.

### Consola de la base de datos (H2)

Abrir **http://localhost:8080/h2-console** e ingresar:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:tallerdb` |
| User Name | `sa` |
| Password | *(vacío)* |

Tablas esperadas: `MODEL`, `VEHICLE`, `PART`, `USERS`, `REPAIR` y `REPAIR_PART`.

> La base de datos está **en memoria**: los datos se borran cada vez que se reinicia la aplicación. Al arrancar solo quedan los 3 usuarios de prueba de `data.sql`.

### Datos de prueba

Mientras no exista el módulo de usuarios, el archivo `src/main/resources/data.sql` crea al iniciar:

| Nombre | Rol |
|---|---|
| Administrador de prueba | `ADMIN` |
| Mecánico de prueba 1 | `MECHANIC` |
| Mecánico de prueba 2 | `MECHANIC` |

Los mecánicos aparecen en la lista desplegable al registrar una reparación.

## ⚙ Configuración

Archivo `src/main/resources/application.properties`:

```properties
spring.application.name=taller
server.port=8080

# Base de datos H2 en memoria
spring.datasource.url=jdbc:h2:mem:tallerdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Consola web de H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Crea las tablas a partir de las entidades y muestra el SQL en consola
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Permite que data.sql (usuarios de prueba) se ejecute después de crear las tablas
spring.jpa.defer-datasource-initialization=true

# Permite que los formularios envíen PUT y DELETE (campo oculto _method)
spring.mvc.hiddenmethod.filter.enabled=true

# Los formularios (input type="date") envían fechas en formato ISO yyyy-MM-dd
spring.mvc.format.date=iso

# Carpeta donde se guardan las imágenes de referencia de los repuestos (RF-20)
app.upload-dir=uploads/parts

# Tamaño máximo que acepta el servidor al subir archivos.
# El límite real de 2 MB lo valida PartService para mostrar un mensaje claro.
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

Agregar a `.gitignore` para no subir las imágenes al repositorio:

```
uploads/
```

## 🗂 Estructura del proyecto

Paquete base: `dev.tarazonarodriguezrojas.taller1`

```
src/main/java/dev/tarazonarodriguezrojas/taller1
├── Application.java
├── config
│   └── WebConfig.java
├── controller
│   ├── IndexController.java
│   ├── VehicleModelController.java
│   ├── VehicleController.java
│   ├── PartController.java
│   └── RepairController.java
├── service
│   ├── VehicleModelService.java
│   ├── VehicleService.java
│   ├── PartService.java
│   ├── RepairService.java
│   └── FileStorageService.java
├── repository
│   ├── VehicleModelRepository.java
│   ├── VehicleRepository.java
│   ├── PartRepository.java
│   ├── RepairRepository.java
│   ├── RepairPartRepository.java
│   └── UserRepository.java
└── model
    ├── constant
    │   ├── PlateColor.java
    │   ├── RepairStatus.java
    │   └── Role.java
    ├── dto
    │   ├── VehicleModelDTO.java
    │   ├── VehicleDTO.java
    │   ├── PlateModelDTO.java
    │   ├── PartDTO.java
    │   ├── RepairDTO.java
    │   ├── MechanicDTO.java
    │   └── LocationDTO.java
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
├── data.sql
└── templates
    ├── index.html
    ├── models
    │   ├── list.html
    │   ├── form.html
    │   └── detail.html
    ├── vehicles
    │   ├── list.html
    │   ├── form.html
    │   ├── detail.html
    │   └── model-by-plate.html
    ├── parts
    │   ├── list.html
    │   ├── form.html
    │   ├── detail.html
    │   └── compatible.html
    └── repairs
        ├── list.html
        ├── form.html
        ├── detail.html
        ├── location.html
        └── map.html

uploads/parts/    (se crea sola; guarda las imágenes de los repuestos; no se sube a Git)
```

### Patrón de cada módulo

Cada módulo sigue la estructura del proyecto de referencia `bibliotech`:

| Capa | Responsabilidad |
|---|---|
| **Repository** | Consultas a la base de datos (Spring Data JPA) |
| **DTO** (`record`) | Datos que viajan entre formulario, controller y service |
| **Service** | Reglas de negocio y validaciones; lanza `EntityNotFoundException`, `IllegalArgumentException` o `IllegalStateException` |
| **Controller** (MVC) | Recibe la petición, llama al service y devuelve la plantilla; los errores se muestran como mensajes |
| **Plantillas** | Vistas Thymeleaf de listado, formulario y detalle |

Clases de apoyo: `FileStorageService` guarda y borra las imágenes en disco, y `WebConfig` publica la carpeta de imágenes en la ruta `/part-images/`.

## 🗃 Modelo de datos

Seis tablas:

| Entidad | Tabla | Relaciones |
|---|---|---|
| `VehicleModel` | `model` | 1:N con `Vehicle` y con `Part` |
| `Vehicle` | `vehicle` | N:1 con `VehicleModel`; 1:N con `Repair` |
| `Part` | `part` | N:1 con `VehicleModel`; 1:N con `RepairPart` |
| `User` | `users` | 1:N con `Repair` (como mecánico) |
| `Repair` | `repair` | N:1 con `Vehicle` y con `User`; 1:N con `RepairPart` |
| `RepairPart` | `repair_part` | Llave compuesta (`repair_id`, `part_id`) más `quantity` y `unit_price` |

**Enums:**

| Enum | Valores |
|---|---|
| `PlateColor` | `YELLOW`, `WHITE`, `RED`, `BLUE`, `GREEN` |
| `Role` | `ADMIN`, `MECHANIC` |
| `RepairStatus` | `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |

**Cálculo del costo total de una reparación:**

```
costo total = mano de obra + (precio unitario del repuesto × cantidad)
```

El precio unitario se guarda en `repair_part.unit_price` al momento de usar el repuesto, por lo que cambiar el precio del repuesto después **no altera** el costo de reparaciones ya registradas.

## ✅ Avance por requisito funcional

### Hecho

| Módulo | RF | Descripción | Ruta |
|---|---|---|---|
| Inicio | — | Página de inicio con enlaces a los módulos y tabla de avance | `GET /` |
| Modelos | RF-08 | Registrar modelo (nombre y marca); rechaza duplicados | `GET /models/new`, `POST /models` |
| Modelos | RF-09 | Listado y detalle de modelos | `GET /models`, `GET /models/{id}` |
| Modelos | RF-10 | Actualizar modelo | `GET /models/{id}/edit`, `PUT /models/{id}` |
| Modelos | RF-11 | Eliminar modelo; informa si tiene vehículos o repuestos asociados | `DELETE /models/{id}` |
| Vehículos | RF-12 | Registrar vehículo con placa, color de placa, propietario y modelo | `GET /vehicles/new`, `POST /vehicles` |
| Vehículos | RF-13 | Listado con filtro por placa o propietario, y detalle | `GET /vehicles`, `GET /vehicles/{id}` |
| Vehículos | RF-14 | Actualizar vehículo (la placa no se modifica) | `GET /vehicles/{id}/edit`, `PUT /vehicles/{id}` |
| Vehículos | RF-15 | Eliminar vehículo; informa si tiene reparaciones asociadas | `DELETE /vehicles/{id}` |
| Repuestos | RF-16 | Registrar repuesto (nombre, descripción, precio, EAN, stock, imagen opcional) | `GET /parts/new`, `POST /parts` |
| Repuestos | RF-17 | Listado y detalle de repuestos; marca los agotados | `GET /parts`, `GET /parts/{id}` |
| Repuestos | RF-18 | Actualizar repuesto, incluido precio y stock | `GET /parts/{id}/edit`, `PUT /parts/{id}` |
| Repuestos | RF-19 | Eliminar repuesto; informa si está asociado a reparaciones | `DELETE /parts/{id}` |
| Repuestos | RF-20 | Adjuntar o reemplazar la imagen de referencia y verla en el detalle | `POST /parts/{id}/image` |
| Reparaciones | RF-21 | Registrar reparación con repuesto opcional y costo total calculado | `GET /repairs/new`, `POST /repairs` |
| Reparaciones | RF-22 | Historial con filtro por placa o estado, y costo total | `GET /repairs`, `GET /repairs/{id}` |
| Reparaciones | RF-23 | Actualizar tipo, estado y mano de obra | `GET /repairs/{id}/edit`, `PUT /repairs/{id}` |
| Reparaciones | RF-24 | Eliminar reparación del historial | `DELETE /repairs/{id}` |
| Reparaciones | RF-25 | Descontar stock, validar compatibilidad y stock suficiente, devolver al cancelar | Dentro de `RepairService` |
| Repuestos | RF-26 | Buscar repuestos compatibles con un modelo, con precio y stock | `GET /parts/compatible?modelId=...` |
| Vehículos | RF-27 | Buscar modelo y marca por placa, indicando el tipo de placa | `GET /vehicles/model-by-plate?plate=...` |
| Reparaciones | RF-28 | Registrar y ver en un mapa la ubicación de la tienda del repuesto | `GET/PUT /repairs/{id}/location`, `GET /repairs/map` |

### Pendiente

| Módulo | RF | Descripción |
|---|---|---|
| Usuarios | RF-06, RF-07 | Registrar, consultar, actualizar y desactivar cuentas |
| Acceso | RF-01, RF-02 | Inicio y cierre de sesión |
| Acceso | RF-03, RF-04 | Recuperación y restablecimiento de contraseña con enlace temporal |
| Acceso | RF-05 | Control de acceso por rol |
| Interfaz | RF-29 | Notificaciones tipo Toast (hoy los mensajes son texto simple) |
| Interfaz | RF-30 | Modo claro y oscuro |

**Progreso:** 21 de 30 requisitos funcionales completados (RF-08 a RF-28).

## 🌐 Rutas de la aplicación

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | Inicio |
| GET | `/models` | Listado de modelos |
| GET | `/models/new` | Formulario para registrar modelo |
| GET | `/models/{id}` | Detalle de un modelo |
| GET | `/models/{id}/edit` | Formulario para editar modelo |
| POST | `/models` | Registrar modelo |
| PUT | `/models/{id}` | Actualizar modelo |
| DELETE | `/models/{id}` | Eliminar modelo |
| GET | `/vehicles` | Listado de vehículos (filtros `?plate=` y `?owner=`) |
| GET | `/vehicles/new` | Formulario para registrar vehículo |
| GET | `/vehicles/{id}` | Detalle de un vehículo |
| GET | `/vehicles/{id}/edit` | Formulario para editar vehículo |
| GET | `/vehicles/model-by-plate` | Buscar modelo y marca por placa (`?plate=`) |
| POST | `/vehicles` | Registrar vehículo |
| PUT | `/vehicles/{id}` | Actualizar vehículo |
| DELETE | `/vehicles/{id}` | Eliminar vehículo |
| GET | `/parts` | Listado de repuestos |
| GET | `/parts/new` | Formulario para registrar repuesto |
| GET | `/parts/compatible` | Repuestos compatibles con un modelo (`?modelId=`) |
| GET | `/parts/{id}` | Detalle de un repuesto, con imagen |
| GET | `/parts/{id}/edit` | Formulario para editar repuesto |
| POST | `/parts` | Registrar repuesto (con imagen opcional) |
| PUT | `/parts/{id}` | Actualizar repuesto |
| DELETE | `/parts/{id}` | Eliminar repuesto |
| POST | `/parts/{id}/image` | Adjuntar o reemplazar la imagen de referencia |
| GET | `/part-images/{archivo}` | Muestra una imagen guardada |
| GET | `/repairs` | Historial de reparaciones (filtros `?plate=` y `?status=`) |
| GET | `/repairs/new` | Formulario para registrar reparación |
| GET | `/repairs/map` | Mapa con las tiendas marcadas |
| GET | `/repairs/{id}` | Detalle de una reparación |
| GET | `/repairs/{id}/edit` | Formulario para editar reparación |
| GET | `/repairs/{id}/location` | Pantalla para marcar la tienda en el mapa |
| POST | `/repairs` | Registrar reparación |
| PUT | `/repairs/{id}` | Actualizar reparación |
| PUT | `/repairs/{id}/location` | Guardar la ubicación de la tienda |
| DELETE | `/repairs/{id}` | Eliminar reparación |

> Los navegadores solo envían GET y POST. Los formularios simulan PUT y DELETE con un campo oculto `<input type="hidden" name="_method" value="put|delete">`.

## 📏 Reglas de negocio implementadas

### Modelos

- Nombre y marca obligatorios, máximo 100 caracteres.
- No puede haber dos modelos con el mismo nombre y marca (sin distinguir mayúsculas).
- No se puede eliminar un modelo que tenga vehículos o repuestos asociados.

### Vehículos

- Color de placa, nombre, documento y teléfono del propietario son obligatorios.
- La placa es única y se guarda en mayúsculas y con guion.
- El modelo seleccionado debe existir.
- La placa no se puede modificar después de registrar el vehículo.
- No se puede eliminar un vehículo que tenga reparaciones asociadas.

### Placas colombianas

Se pueden escribir con o sin guion: `abc123` se guarda como `ABC-123`.

| Tipo | Formato | Ejemplo |
|---|---|---|
| Carro, camión o tractocamión (tractomula) | `AAA-123` | `ABC-123` |
| Moto | `AAA-12` o `AAA-12A` | `ABC-12D` |
| Remolque | `R-12345` | `R-12345` |
| Semirremolque (tráiler de tractomula) | `S-12345` | `S-12345` |
| Diplomático | `CC-1234` o `CD-1234` | `CD-1234` |

> El tractocamión (la cabeza de la tractomula) usa el mismo formato que un carro o camión, por eso no se puede distinguir por la placa. El semirremolque sí tiene formato propio.

El filtro de placa de los listados de vehículos y reparaciones compara **sin guiones y por coincidencia parcial**: `12A` encuentra `ABC-12A` y `R-1` encuentra `R-12345`.

### Repuestos

- Nombre obligatorio (máximo 100 caracteres); descripción opcional (máximo 255).
- Precio obligatorio, no negativo y hasta 99.999.999,99.
- Stock obligatorio y no negativo. Los repuestos con stock 0 se muestran como "Agotado".
- El código de barras (EAN) debe tener exactamente 13 dígitos y no puede repetirse. No se modifica después de registrar.
- Cada repuesto es compatible con un único modelo, que debe existir.
- Imagen de referencia: solo JPG, PNG o WEBP, máximo 2 MB. Al reemplazarla se borra la anterior; al eliminar el repuesto también se borra su imagen.
- No se puede eliminar un repuesto asociado a reparaciones.

### Reparaciones

- Fecha, tipo, estado, vehículo, mecánico y costo de mano de obra son obligatorios al registrar.
- El mecánico debe estar activo y tener rol `MECHANIC`.
- El repuesto es opcional; si se usa, la cantidad debe ser al menos 1.
- **Stock automático (RF-25):**
  - El repuesto debe ser compatible con el modelo del vehículo.
  - Debe haber stock suficiente; si no, no se registra nada.
  - Al registrar se descuenta la cantidad usada.
  - Al pasar a `CANCELLED` se devuelve el stock; si luego sale de `CANCELLED`, se vuelve a descontar.
  - Al eliminar una reparación no cancelada se devuelve el stock.
- Al editar solo se modifican tipo, estado y mano de obra; fecha, vehículo, mecánico y repuesto no cambian.
- Ubicación de la tienda: nombre obligatorio y un punto marcado en el mapa (latitud entre -90 y 90, longitud entre -180 y 180).

## 🧪 Cómo probar lo implementado

1. **Verificar usuarios de prueba:** en `/h2-console`, ejecutar `SELECT * FROM USERS;` (deben estar los 3).
2. **Registrar un modelo:** `/models/new`, por ejemplo "Spark GT" de "Chevrolet". Probar el duplicado.
3. **Registrar vehículos de cada tipo** en `/vehicles/new`: `ABC-123`, `abc12d`, `R12345`, `S12345`, `CD1234` (algunos sin guion).
4. **Buscar por placa:** en `/vehicles/model-by-plate`, probar `ABC-123` y `abc123`; deben dar el mismo resultado con el tipo de placa. Probar `AB-1234` (inválida).
5. **Registrar un repuesto** en `/parts/new` del mismo modelo: precio `25000.50`, stock `10`, EAN `7701234567890`. Probar EAN repetido, EAN de 12 dígitos y precio negativo.
6. **Imagen:** en el detalle del repuesto, subir una imagen y luego reemplazarla. Probar un `.pdf` y un archivo de más de 2 MB (deben dar error).
7. **Compatibles:** en `/parts/compatible`, elegir el modelo y verificar precio y stock.
8. **Registrar una reparación** en `/repairs/new` con ese vehículo, un mecánico, mano de obra `50000`, el repuesto y cantidad `3`. El costo total debe ser `50000 + 3 × precio` y el stock debe bajar de 10 a 7.
9. **Errores de stock:** cantidad `100` (stock insuficiente) y un repuesto de otro modelo (no compatible). No debe cambiar el stock.
10. **Cancelar:** editar la reparación a `CANCELLED` (el stock vuelve a 10) y luego a `IN_PROGRESS` (baja a 7).
11. **Precio histórico:** cambiar el precio del repuesto; el costo total de la reparación ya registrada no debe cambiar.
12. **Mapa:** en el detalle de la reparación, "Registrar ubicación en el mapa", marcar un punto y guardar. Revisar `/repairs/map`.
13. **Reglas de eliminación:** intentar eliminar el vehículo o el repuesto de una reparación existente (debe informar el motivo); eliminar el modelo que tiene vehículos; eliminar la reparación (el stock se devuelve).

## 🧭 Decisiones de diseño

- **Seis tablas.** El PDF del proyecto menciona un máximo de cinco. Se mantuvo `repair_part` para guardar la cantidad y el precio unitario del repuesto de cada reparación. Pendiente de confirmar con el docente.
- **Un repuesto por reparación.** El RF-21 pide un repuesto opcional con su cantidad. La tabla `repair_part` permitiría varios, pero hoy la interfaz y el servicio manejan uno.
- **Precio unitario guardado en `repair_part`:** el costo histórico de una reparación no cambia si el precio del repuesto se modifica después.
- **`VehicleModel` en lugar de `Model`:** evita confundirse con `org.springframework.ui.Model` en los controllers (la tabla se sigue llamando `model`).
- **Tabla `users`:** `USER` es palabra reservada en H2.
- **Relaciones `LAZY` y sin `cascade`:** el sistema debe informar al administrador cuando algo no se puede eliminar, no borrar en cascada.
- **Controllers MVC con Thymeleaf**, igual que `bibliotech`, en lugar de REST con JSON.
- **Mensajes de resultado** como `successMessage` y `errorMessage`: para pasar a notificaciones Toast (RF-29) solo hay que cambiar cómo se muestran en las plantillas.
- **Datos que no se editan:** la placa del vehículo, el EAN del repuesto y la fecha, vehículo, mecánico y repuesto de una reparación no cambian después de registrarse.
- **`image_url` guarda solo el nombre del archivo**, no una ruta completa, para poder cambiar la carpeta de imágenes en `application.properties` sin tocar la base de datos.
- **EAN sin dígito verificador:** se valida que sean 13 dígitos, pero no el dígito de control, para facilitar las pruebas.
- **Eliminar una reparación devuelve el stock:** el PDF no lo indica, pero evita dejar el inventario descontado por un registro erróneo.
- **Las columnas `workshop_*` de `repair`** guardan la tienda donde se compró el repuesto (nombre y punto en el mapa), según el RF-28.

## 🩺 Problemas frecuentes

| Problema | Causa y solución |
|---|---|
| Error 404 en `/` | Falta `IndexController` o la plantilla `index.html` |
| Error al abrir una página: plantilla no encontrada | Falta la dependencia `spring-boot-starter-thymeleaf` en `build.gradle.kts` |
| Los botones Editar y Eliminar no funcionan | Falta `spring.mvc.hiddenmethod.filter.enabled=true` en `application.properties` |
| No conecta a la consola H2 | La JDBC URL debe ser exactamente `jdbc:h2:mem:tallerdb`, con usuario `sa` y contraseña vacía |
| Desaparecieron los datos | Es normal: la base de datos es en memoria y se borra al reiniciar |
| Error `jakarta.servlet.http.Part cannot be converted to ...entity.Part` | IntelliJ importó la clase `Part` equivocada. Usar siempre `dev.tarazonarodriguezrojas.taller1.model.entity.Part` y borrar `import jakarta.servlet.http.Part;` |
| No aparecen mecánicos al registrar una reparación | Falta `data.sql` o la línea `spring.jpa.defer-datasource-initialization=true` |
| La fecha de la reparación da error | Falta `spring.mvc.format.date=iso` en `application.properties` |
| La imagen del repuesto no se ve | Verificar que existe `WebConfig.java` y que `app.upload-dir` apunta a una carpeta con permisos de escritura |
| El mapa aparece en blanco | Leaflet y OpenStreetMap necesitan conexión a internet |
| El paquete no compila | Los nombres de paquetes no pueden tener un segmento que empiece por número (por ejemplo, `taller1.0`) |

## 🔒 Notas de seguridad

- Aún **no hay autenticación ni control por rol**: cualquier persona que abra la aplicación puede registrar, editar y eliminar datos.
- Los usuarios de `data.sql` son **solo de prueba**: su contraseña `pendiente` no es una contraseña real y se reemplazará cuando se implemente el inicio de sesión (RF-01).
- La carpeta `uploads/` está en `.gitignore`; no subir imágenes al repositorio.
- No subir credenciales reales en `application.properties` si el repositorio es público.

## 🚀 Siguientes pasos

1. **Usuarios** (RF-06 y RF-07): registrar, consultar, actualizar y desactivar cuentas.
2. **Sesión y contraseña** (RF-01 a RF-04), con Spring Security y contraseñas cifradas. Reemplaza los usuarios de prueba de `data.sql`.
3. **Roles y permisos** (RF-05): ocultar y bloquear las acciones de administrador (registrar modelos y repuestos, eliminar) para el mecánico, y que el mecánico solo actualice las reparaciones que tiene asignadas (RF-23).
4. **Interfaz**: notificaciones Toast y modo claro/oscuro (RF-29 y RF-30).

---

*Proyecto académico · Universidad Santo Tomás, Seccional Tunja · 2026 - II*
