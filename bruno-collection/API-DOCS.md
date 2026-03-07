# Food Ordering System - API Documentation

## Environments

| Environment | Descripcion | Base URL |
|-------------|-------------|----------|
| **local** | Servicios corriendo individualmente | Cada servicio en su puerto (8081-8085) |
| **docker** | Todo via API Gateway (docker-compose) | `http://localhost:8080` |

## Flujo Secuencial de Pruebas

El orden de ejecucion de las carpetas sigue el flujo de negocio real:

```
1-users -> 2-catalog -> 3-orders -> 4-payments -> 5-deliveries -> 6-drivers
```

### Variables de Environment

Las variables se propagan automaticamente entre requests usando scripts `post-response`.
Al crear un recurso, su ID se guarda en la variable correspondiente para usarlo en las siguientes requests.

| Variable | Descripcion | Se setea en |
|----------|-------------|-------------|
| `user_id` | ID del usuario creado | 1-users/1-register-user |
| `restaurant_id` | ID del restaurante creado | 2-catalog/1-create-restaurant |
| `product_id` | ID del producto creado | 2-catalog/5-create-product |
| `order_id` | ID de la orden creada | 3-orders/1-create-order |
| `payment_id` | ID del pago procesado | 4-payments/1-process-payment |
| `delivery_id` | ID de la entrega creada | 5-deliveries/1-create-delivery |
| `driver_id` | ID del conductor registrado | 6-drivers/1-register-driver |

---

## 1. Users Service (port 8081)

| # | Metodo | Endpoint | Descripcion |
|---|--------|----------|-------------|
| 1 | POST | `/api/v1/users` | Registrar usuario |
| 2 | GET | `/api/v1/users/{id}` | Obtener usuario por ID |
| 3 | GET | `/api/v1/users/email/{email}` | Obtener usuario por email |
| 4 | PUT | `/api/v1/users/{id}` | Actualizar usuario |

## 2. Catalog Service (port 8083)

### Restaurants

| # | Metodo | Endpoint | Descripcion |
|---|--------|----------|-------------|
| 1 | POST | `/api/v1/restaurants` | Crear restaurante |
| 2 | GET | `/api/v1/restaurants/{id}` | Obtener restaurante por ID |
| 3 | GET | `/api/v1/restaurants?page=0&size=10` | Listar restaurantes (paginado) |
| 4 | GET | `/api/v1/restaurants/search?cuisineType=X` | Buscar por tipo de cocina |

### Products

| # | Metodo | Endpoint | Descripcion |
|---|--------|----------|-------------|
| 5 | POST | `/api/v1/restaurants/{restaurantId}/products` | Crear producto |
| 6 | GET | `/api/v1/products/{id}` | Obtener producto por ID |
| 7 | GET | `/api/v1/restaurants/{restaurantId}/products` | Listar productos de restaurante |
| 8 | PUT | `/api/v1/products/{id}` | Actualizar producto |
| 9 | PATCH | `/api/v1/products/{id}/availability` | Toggle disponibilidad |

## 3. Orders Service (port 8082)

| # | Metodo | Endpoint | Descripcion |
|---|--------|----------|-------------|
| 1 | POST | `/api/v1/orders` | Crear orden |
| 2 | GET | `/api/v1/orders/{id}` | Obtener orden por ID |
| 3 | GET | `/api/v1/orders/user/{userId}` | Ordenes por usuario |
| 4 | PATCH | `/api/v1/orders/{id}/status` | Actualizar estado de orden |

## 4. Payments Service (port 8084)

| # | Metodo | Endpoint | Descripcion |
|---|--------|----------|-------------|
| 1 | POST | `/api/v1/payments` | Procesar pago |
| 2 | GET | `/api/v1/payments/{id}` | Obtener pago por ID |
| 3 | GET | `/api/v1/payments/order/{orderId}` | Obtener pago por orden |
| 4 | POST | `/api/v1/payments/{id}/refund` | Reembolsar pago |

## 5. Deliveries Service (port 8085)

| # | Metodo | Endpoint | Descripcion |
|---|--------|----------|-------------|
| 1 | POST | `/api/v1/deliveries` | Crear entrega |
| 2 | GET | `/api/v1/deliveries/{id}` | Obtener entrega por ID |
| 3 | GET | `/api/v1/deliveries/order/{orderId}` | Obtener entrega por orden |
| 4 | PATCH | `/api/v1/deliveries/{id}/status` | Actualizar estado de entrega |

## 6. Drivers (port 8085)

| # | Metodo | Endpoint | Descripcion |
|---|--------|----------|-------------|
| 1 | POST | `/api/v1/drivers` | Registrar conductor |
| 2 | GET | `/api/v1/drivers/available` | Listar conductores disponibles |
| 3 | PATCH | `/api/v1/drivers/{id}/availability` | Toggle disponibilidad |

---

## API Gateway (port 8080)

Todas las rutas anteriores estan disponibles a traves del gateway en `http://localhost:8080`.
El gateway enruta automaticamente por prefijo de path al servicio correspondiente.

## Swagger UI (OpenAPI 3.0)

Cada servicio expone su documentacion interactiva:

| Servicio | URL directa | Via Gateway |
|----------|-------------|-------------|
| Users | `http://localhost:8081/swagger-ui.html` | `http://localhost:8080/users/swagger-ui.html` |
| Orders | `http://localhost:8082/swagger-ui.html` | `http://localhost:8080/orders/swagger-ui.html` |
| Catalog | `http://localhost:8083/swagger-ui.html` | `http://localhost:8080/catalog/swagger-ui.html` |
| Payments | `http://localhost:8084/swagger-ui.html` | `http://localhost:8080/payments/swagger-ui.html` |
| Deliveries | `http://localhost:8085/swagger-ui.html` | `http://localhost:8080/deliveries/swagger-ui.html` |

## Seguridad (OAuth2 + JWT)

Todos los endpoints (excepto registro de usuario y catalogo GET) requieren un JWT Bearer token.

| Rol | Permisos |
|-----|----------|
| `CUSTOMER` | Crear ordenes, ver sus pedidos, procesar pagos, ver entregas |
| `RESTAURANT_OWNER` | Gestionar restaurantes y productos, ver/actualizar estado de ordenes |
| `DELIVERY_DRIVER` | Actualizar estado de entregas, toggle disponibilidad |
| `ADMIN` | Acceso completo a todos los endpoints |

Header requerido: `Authorization: Bearer <jwt_token>`

## Eventos Kafka

| Topic | Productor | Consumidor | Trigger |
|-------|-----------|------------|---------|
| `order-created` | orders-service | payments-service | Al crear orden |
| `payment-completed` | payments-service | orders-service | Al completar pago |
| `order-status-changed` | orders-service | deliveries-service | Al cambiar estado |
| `delivery-assigned` | deliveries-service | - | Al asignar conductor |
