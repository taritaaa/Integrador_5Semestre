
-->(27/10/2026)se trabajo todo lo de vehicle  como el dto, controller, service , repository etc 
## Mapeo de Requisitos Funcionales (RF)

| RF | Descripción / Implementación |
| :--- | :--- |
| **RF-08** | `GET /models/new` + `POST /models`. El `service` rechaza duplicados de nombre y marca. |
| **RF-09** | `GET /models` y `GET /models/{id}`. |
| **RF-10** | `GET /models/{id}/edit` + `PUT /models/{id}`. |
| **RF-11** | `DELETE /models/{id}`. Informa si el modelo tiene vehículos o repuestos asociados. |

> **Nota sobre notificaciones:** Los mensajes de éxito y error ya se transfieren como `successMessage` y `errorMessage`. Cuando se trabaje en el **RF-29 (Toast)**, solo será necesario ajustar su visualización en las plantillas HTML, sin modificar los controladores.

---

## Pendientes y Observaciones

* **Control de Acceso (Solo Administrador):** Los botones de registrar, editar y eliminar deben ocultarse para el rol de mecánico. Esto se resolverá al implementar el **RF-05 (Gestión de Roles)** con Spring Security.
* **Módulo de Vehículos (`Vehicle`):** Funcionalidad de vehículos trabajada e integrada.
* **Ajustes en Repuestos (`RepairPart`):**
    * Se corrigió y ajustó el método `quantity`.
    * **Costo histórico:** Dado que `RepairPart` solo almacena la cantidad, si el precio de un repuesto cambia en el futuro, el costo total de las reparaciones antiguas se alteraría. Para preservar el historial (como se especificaba en el PDF), se recomienda agregar el campo `precio_unitario_repuesto` a `RepairPart` antes de desarrollar el módulo de reparaciones.


---> para entrar a h2:
JDBC URL: jdbc:h2:mem:testdb
User Name: sa
Password: (dejar en blanco)