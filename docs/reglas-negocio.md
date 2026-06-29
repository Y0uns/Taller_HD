# Reglas de Negocio

SansaWeigh implementa reglas de negocio estrictas solicitadas para el manejo de los paquetes.

## 1. Categorías de Peso (Sansas)

- **LIVIANO:** $\le 10$ Sansas
- **MEDIANO:** $> 10$ y $\le 50$ Sansas
- **PESADO:** $> 50$ Sansas

*(Conversión oficial: 1 Sansa = 1.337 Kg)*

## 2. Restricción Horaria (Turno Nocturno)

Ningún paquete categorizado como **PESADO** puede ingresar al sistema entre las **20:00 y las 06:00**. Si se intenta, el sistema arrojará un error de conflicto (`HTTP 409`).

## 3. Regla de Balanza "Prima"

Si el ID de la balanza es un **número primo** (ej. 2, 3, 5, 7) y el día actual del mes es **impar**, la balanza NO podrá procesar paquetes **PESADOS**.

## 4. Máquina de Estados de un Pesaje

Un pesaje solo puede avanzar a través de estados predefinidos. Transiciones inválidas causan error `HTTP 400`.

- `INGRESADO` $\rightarrow$ `PESADO`
- `PESADO` $\rightarrow$ `APROBADO` o `RECHAZADO`
- `APROBADO` $\rightarrow$ `DESPACHADO`
- `RECHAZADO` $\rightarrow$ `DESPACHADO`
- `DESPACHADO` (Estado final, sin transición posible)
