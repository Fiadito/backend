# Fiadito – Backend (Fiadito Platform)

API REST de **Fiadito** (SI642 Finanzas e Ingeniería Económica – UPC) con **Spring Boot 3.5, Java 21 y MySQL 8**.
Autenticación con **JWT** (Spring Security + BCrypt), documentación con **Swagger** y arquitectura **DDD** por bounded contexts,
igual que el frontend.

## Cómo ejecutarlo (IntelliJ IDEA)

1. Ten MySQL 8 corriendo en `localhost:3306`. La base `fiadito` se crea sola.
2. Copia `.env.example` como `.env` en la raíz del proyecto y pon tu clave de MySQL (`DB_PASSWORD`). El `.env` no se sube a Git.
3. Abre el proyecto en IntelliJ (Open → carpeta con el `pom.xml`), espera a que Maven descargue las dependencias y ejecuta
   `FiaditoPlatformApplication`. También por terminal: `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
4. API: `http://localhost:8080/api/v1` · Swagger: `http://localhost:8080/swagger-ui.html`

La primera vez crea las 8 tablas del informe (NEGOCIO, USUARIO, SOCIO, PRODUCTO, VENTA, CUOTA, PAGO, BITACORA), sus claves
foráneas y restricciones CHECK, y carga los datos iniciales (Fuerza Norte Gym, los productos de la Tabla 4 y los juegos de prueba).

| Rol | Correo | Contraseña |
|---|---|---|
| NEGOCIO | josue.flores@fuerzanorte.pe | Fiadito2026! |
| SOCIO | carla.mendoza@correo.pe (y los demás socios con correo) | Socio2026! |

En Swagger: ejecuta `POST /api/v1/auth/login`, copia el `token` y pégalo en **Authorize**.

## Variables (.env)

| Variable | Por defecto | Uso |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | localhost, 3306, fiadito | Conexión a MySQL |
| `DB_USERNAME`, `DB_PASSWORD` | root, (vacía) | Usuario de MySQL |
| `JWT_SECRET` | clave de desarrollo | Firma de los JWT (mínimo 32 caracteres) |
| `SYSTEM_DATE` | (vacía) | Fecha simulada `YYYY-MM-DD` para ensayar casos de prueba |
| `SEED_ENABLED` | true | Carga los datos iniciales si la base está vacía |
| `CORS_ALLOWED_ORIGINS` | http://localhost:5173 | Origen permitido del frontend |

## Modelo financiero (informe aceptado)

- Sin interés compensatorio: cuota `C = P / n`, de 1 a 8 cuotas quincenales; la última cuota absorbe el redondeo.
- Cortes: días 10 y 25 hasta las 23:59. Pagos: días 15 y 30 (o el último día del mes).
- Tasa efectiva moratoria (TEA, TES, TET, TEM o TEQ) de mínimo 40 %: `TED = (1 + TEP)^(1/N) − 1` (7 decimales).
- Mora: `Imor = D × ((1 + TED)^dm − 1)`; el pago debe ser igual al total y se imputa mora → capital.
- Límite de crédito y monto de compra entre S/ 400.00 y S/ 1,000.00. Montos a 2 decimales.

## Estructura

```
src/main/java/pe/edu/upc/fiadito/platform/
  shared/      AuditableAbstractAggregateRoot, EffectiveRate, Rounding, fecha del sistema, errores, Swagger, restricciones de BD
  iam/         Negocio, Usuario, registro del negocio, login JWT, Spring Security (roles NEGOCIO / SOCIO)
  membership/  Socio y sus condiciones de crédito
  catalog/     Producto y modalidad de venta
  credit/      Venta, Cuota, calendario de cortes y pagos, plan de pagos (C = P / n)
  billing/     Pago, interés moratorio, obligaciones, panel, reporte de corte y estado de cuenta
  audit/       Bitácora (cada operación publica un OperationPerformedEvent)
  bootstrap/   Datos iniciales
```

Cada contexto sigue `domain` (agregados, comandos, value objects, servicios), `application` (implementaciones de comandos y
consultas), `infrastructure` (repositorios JPA) e `interfaces` (controladores REST, recursos y fachadas ACL entre contextos).

## Servicios (Tabla 7 del informe)

| Método | Ruta (/api/v1) | Rol |
|---|---|---|
| POST | /auth/registro-negocio, /auth/login | Público |
| POST | /auth/logout | Autenticado |
| GET | /panel | NEGOCIO |
| GET / POST / PUT / PATCH | /socios, /socios/{id}, /socios/{id}/estado | NEGOCIO |
| GET / POST / PUT / PATCH | /productos, /productos/{id}, /productos/{id}/estado | NEGOCIO |
| POST | /ventas/simulacion, /ventas | NEGOCIO |
| GET | /ventas, /ventas/{id}, /cuotas | NEGOCIO |
| GET | /ventas/{id}/cronograma | NEGOCIO / SOCIO |
| GET | /reportes/cortes, /reportes/corte?fecha= | NEGOCIO |
| GET | /obligaciones?estado=PENDIENTE | NEGOCIO |
| POST | /pagos/cotizacion, /pagos | NEGOCIO |
| GET | /pagos, /bitacora, /socios/{id}/estado-cuenta | NEGOCIO |
| GET | /socios/me/estado-cuenta | SOCIO |

Sin token → **401** (CP03). Rol no autorizado → **403** (CP04). Errores de negocio → **400** con `{"message": "..."}`.

## Pruebas

```bash
./mvnw test
```

- `FinancialModelTests`: valores de la Tabla 11 (cuota S/ 62.50, fechas, TED 0.0013064, mora S/ 0.41, total S/ 62.91, juego 3).
- `ApiFlowTests`: CP01–CP12 contra la API (con base H2 en memoria y fecha del sistema 17/10/2026).
