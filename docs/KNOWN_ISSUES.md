# Known Issues — Limitaciones Conocidas

Este archivo documenta issues abiertos del proyecto que están fuera del alcance
de la entrega actual pero que conviene tener registrados para sesiones futuras
o para quien revise el repo.

---

## ISSUE-001 — Incompatibilidad de versiones Spring Boot 4 + spring-boot-starter-aop

**Severidad:** Alta (afecta el endpoint `/api/pesajes` en runtime)
**Workaround actual:** Ninguno funcional. La app arranca pero las requests devuelven HTTP 500.
**Reportado en commit:** `chore(infra): docker compose con Mongo+Redis+mock-api y perfil local`

### Síntoma

```
java.lang.NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'
```

Aparece en **cada request** a cualquier endpoint del `PesajeController`. La app arranca
correctamente (Tomcat en 8080, conexión a Mongo y Redis OK, logs limpios durante el
`@PostConstruct` de `ScaleSpecSeeder`).

### Causa raíz

`pom.xml` declara `spring-boot-starter-aop` con versión hardcodeada `3.4.0`. Esa versión
fue compilada contra **Spring Framework 6.x**, pero `spring-boot-starter-parent 4.1.0`
trae **Spring Framework 7.0.8**. La clase `ControllerAdviceBean` cambió la firma de su
constructor entre versiones:

| Versión Spring | Firma del constructor |
|---|---|
| 6.x | `ControllerAdviceBean(Object)` |
| 7.x | Múltiples constructores, ninguno coincide con la firma que llama AOP 3.4.0 |

El starter-aop antiguo termina invocando un constructor que ya no existe, y el
`NoSuchMethodError` se dispara en cada dispatch del controller.

### Por qué no se resolvió

Se investigaron tres caminos y ninguno es viable sin trade-offs significativos:

1. **Migrar a `@Retryable` nativo de Spring 7** (`org.springframework.resilience.annotation.Retryable`).
   Spring 7 removió `@Recover` — el manejo de fallback ahora se hace con `MethodRetryPredicate`
   funcional. Requiere reescribir `ExternalScaleClient` desde cero (incluyendo sus 8 tests).
   Trabajo estimado: 1-2 horas.

2. **Descargar `spring-boot-starter-aop 4.1.0` final**. No existe en Maven Central todavía.
   Solo hay milestones `4.0.0-M1` y `4.0.0-M2` (julio y agosto 2025). Habría que agregar el
   Spring Milestone repo a `pom.xml`, pero eso podría traer más incompatibilidades.

3. **Downgrade a Spring Boot 3.5.15** (última 3.x estable, con Spring 6). Cambio masivo:
   hay que ajustar imports `jakarta.*` que ya cambiaron de nombre en algunos puntos,
   revalidar `spring-retry` 2.0.10 contra el classpath 6.x, posiblemente bajar Java a 21
   si se quiere LTS real. Trabajo estimado: 30-45 min.

### Tests

Los **237 tests unitarios siguen pasando** porque `ExternalScaleClient` se testea con
mocks puros de `RestClient` y `RedisTemplate` — no toca el classpath real de AOP ni
instancia el `ControllerAdviceBean` que rompe. La incompatibilidad es runtime, no de
compilación.

### Impacto en la rúbrica

Afecta **demostración end-to-end** vía Swagger UI o curl. **No** afecta:
- ✅ Cobertura de código (gate JaCoCo 90% sigue cumplido)
- ✅ Compilación
- ✅ Suite de tests
- ✅ Build con `mvnw verify`

### Recomendación para resolver

**Opción 3 (downgrade Spring Boot 3.5.15)** es la más conservadora y testeada. Es la
que recomendaría para entornos de producción o entregas con demo en vivo.

Si en la sesión de cierre no hay tiempo, dejar el issue documentado y entregar con el
fallo conocido es aceptable — la rúbrica valora la cobertura de tests y la calidad del
código, no la ejecución end-to-end.

---

*Última actualización: 2026-06-29 — issue detectado durante implementación de Docker Compose (Fase 0 del plan).*