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

La primera vez crea las tablas del informe (NEGOCIO, USUARIO, SOCIO, PRODUCTO, COMBO_ITEM, VENTA, CUOTA, PAGO, BITACORA), sus claves
foráneas y restricciones CHECK, y carga los datos iniciales (Fuerza Norte Gym, los productos de la Tabla 4, 3 combos y los juegos de prueba).

> **Si ya tenías la base `fiadito` de la versión anterior, bórrala una vez** (`DROP DATABASE fiadito;` en MySQL Workbench) y vuelve
> a ejecutar el backend: las tasas pasaron del SOCIO a la VENTA y al PAGO, y `ddl-auto=update` no quita las columnas viejas.

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

## Modelo financiero (con las correcciones del profesor)

- **Tasa efectiva por venta** (no por socio): TEA, TES, TET, TEM o TEQ, cuya **TEA equivalente debe estar entre 15 % y 27 %**.
  Se puede cambiar después con `PATCH /ventas/{id}/tasa`: las cuotas pagadas no cambian y las pendientes se recalculan desde su saldo.
- **Plan con interés (método francés, cuota fija)**: `i = TEQ = (1 + TEP)^(15/N) − 1`; `C = P · i / (1 − (1 + i)^−n)`;
  `I_k = SI_k · i`; `A_k = C − I_k`. De 1 a 8 cuotas quincenales; la última cuota absorbe el redondeo. Un pago único lleva una quincena de interés.
- Cortes: días 10 y 25 hasta las 23:59. Pagos: días 15 y 30 (o el último día del mes).
- **Tasa moratoria al pagar, solo si está vencido**: se coloca en Registrar pago (TEA 60 % por defecto; TEA equivalente mínima 40 %).
  `TED = (1 + TEP)^(1/N) − 1`; `Imor = D × ((1 + TED)^dm − 1)`. El pago debe ser igual al total; se imputa mora → interés → capital.
- **Tasas con 7 decimales en porcentaje** (0.1306418 %), es decir 9 decimales como fracción; montos a 2 decimales.
- Límite de crédito y monto de compra entre S/ 400.00 y S/ 1,000.00 (capital). **Combos** de S/ 100, 200 o 400 para llegar al mínimo
  sin vender cientos de bebidas sueltas (un combo de S/ 100 se compra × 4).
- Seguridad de datos: un pago o una venta simultáneos sobre lo mismo no pasan dos veces (bloqueo de la fila); no se aceptan pagos con
  fecha futura; el límite no puede quedar por debajo de la deuda; los errores 404/405/500 ya no responden 401.

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
| PATCH | /ventas/{id}/tasa | NEGOCIO |
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

- `FinancialModelTests`: conversión de tasas (TEA 60 % → TED 0.1306418 %), rangos por TEA equivalente, juego 1 con TEA 20 %
  (cuota S/ 64.66, interés total S/ 17.32), mora de 5 días solo si está vencida (S/ 0.42, total S/ 65.08), juego 3 y pago único.
  Los valores esperados se calcularon aparte con Python (`decimal`).
- `ApiFlowTests`: CP01–CP12, cambio de tasa con cuotas pagadas, combos, pago con fecha futura y errores que no cierran la sesión
  (con base H2 en memoria y fecha del sistema 04/11/2026).
