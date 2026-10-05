# LABORATORIO SOLID

**Autores:**

- Deivid Farid Ardila Herrera
- Ángel David Beltrán García

**Docente:** Sergio Enrique Vargas Pedraza  
**Universidad Nacional de Colombia**  
**Facultad de Ingeniería**  
**Departamento de Ingeniería de Sistemas e Industrial**  
**Ingeniería de Software II**  
**Bogotá D.C., 2026-2**

---

## Bloque 0 - Arranque

Preparar el proyecto y entender qué hace el sistema antes de juzgarlo.

1. Creen el proyecto en el lenguaje elegido y copien (o traduzcan) el código base.
2. Ejecuten el programa principal y guarden su salida en un archivo `salida_original.txt`. La usarán en el bloque 2 para comprobar que no cambiaron el comportamiento.
3. Lean el código completo una vez, sin tomar notas, solo para entender el flujo de una transferencia.

**Commit:** `bloque-0-codigo-base`

---



## Bloque 1 - Diagnóstico

Encontrar los problemas de diseño y medir cómo está el sistema antes de tocarlo.

### 1.1 Tabla de hallazgos

En el código hay al menos un problema por cada letra de SOLID, y algunas clases tienen más de uno. Encuéntrenlos y regístrenlos en una tabla como esta en su README:


| Clase / método            | Letra | Evidencia en el código                                                                                                                                                                                                                                                 | Consecuencia para el banco o el cliente                                                                                                                                                                              |
| ------------------------- | ----- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `CobroCuotaManejo.java`   | **S** | `java public void cobrarMensual (List cuentas) { for (Cuenta cuenta : cuentas) { cuenta.retirar (CUOTA); System.out.println("Cuota de manejo cobrada a " + cuenta.getNumero()); } }`                                                                                   | Si el banco incorporara un servicio de notificación por mail, se tendría que cambiar el código del método, por lo que hay dos responsabilidades: la lógica de negocio y la impresión de mensajes                     |
| `TransaccionService.java` | **S** | `java public void transferir (Cuenta origen, Cuenta destino, double monto, String tipo) { // 1. Validación (...) // 2. Cálculo de la comisión (...) // 3. Movimiento del dinero (...) // 4. Persistencia (...) // 5. Comprobante (...) }`                              | Si el banco agregara un nuevo tipo de dato al comprobante o agregar una notificación al mail debe cambiar la misma clase.                                                                                            |
| `TransaccionService.java` | **O** | `java switch (tipo) { case "MISMO_BANCO" -> comision = 0; case "OTRO_BANCO" -> comision = 7_500; case "INTERNACIONAL" -> comision = monto * 0.03 + 25_000; default -> throw new IllegalArgumentException("Tipo de transferencia desconocido"); }`                      | Si el banco en el futuro crea nuevos tipos de transferencia, le tocaría volver a tocar la clase directamente para agregar estos nuevos tipos.                                                                        |
| `CobroCuotaManejo.java`   | **L** | `java public void cobrarMensual (List cuentas) { for (Cuenta cuenta : cuentas) { cuenta.retirar (CUOTA); System.out.println("Cuota de manejo cobrada a " + cuenta.getNumero()); } }`                                                                                   | Si el banco cobra a todas las cuentas existentes, también cobrará a los CDTs, y al retirar en una fecha anterior al vencimiento del CDT, generará error.                                                             |
| `CDT.java`                | **L** | `java @Override public void retirar (double monto) { if (LocalDate.now().isBefore(vencimiento)) { throw new UnsupportedOperationException("Un CDT no permite retiros antes del vencimiento"); } super.retirar (monto); }`                                              | Si el Banco intenta realizar un proceso automático como cobrar la CuotaDeManejo, al no haberse cumplido la fecha de retiro, generará error. Esto complica el cobro del resto de clientes.                            |
| `Cuenta.java`             | **I** | `java public void depositar (double monto) { if (monto <= 0) throw new IllegalArgumentException("Monto inválido"); saldo += monto; } public void retirar (double monto) { if (monto > saldo) throw new IllegalStateException("Saldo insuficiente"); saldo -= monto; }` | Si el banco creara un nuevo tipo de cuenta donde no se pueda retirar (como Pibank, donde solo se puede transferir), se estaría forzando a las subclases a heredar estos métodos y cambiar el comportamiento inicial. |
| `TarjetaCredito.java`     | **I** | `java public void depositar (double monto) { } // no aplica`                                                                                                                                                                                                           | Al cliente le afecta porque si invoca los métodos en cuestión, no podrá saber el estado del depósito o del retiro porque no fue implementado nunca.                                                                  |
| `CreditoVivienda.java`    | **I** | `java public void depositar (double monto) {} // no aplica public void retirar (double monto) { } // no aplica`                                                                                                                                                        | Al cliente le afecta porque si invoca los métodos en cuestión, no podrá saber el estado del depósito o del retiro porque no fue implementado nunca.                                                                  |
| `TransaccionService.java` | **D** | `java private final OracleRepositorio repositorio = new OracleRepositorio(); private final SmsGateway sms = new SmsGateway();`                                                                                                                                         | Si el banco cambia de proveedores, tendría que tocar todas la clases involucradas. Con una abstracción, solo debe modificar las clases de los proveedores.                                                           |


---



### 1.2 Dos experimentos

1. **El CDT. Modifiquen temporalmente el programa principal para que el cobro de la cuota de manejo incluya el CDT de Ana. ¿Qué pasa?**
  ```text
   [ORACLE] Conectando a jdbc:oracle:thin:@prod-db:1521/BANCO ...
   [ORACLE] INSERT INTO transacciones VALUES ('001-1', '001-2', 150000.0, 7500.0)
   ===== BANCO ANDINO COMPROBANTE =====
   Origen: 001-1
   Destino: 001-2
   Monto: $150000.0
   Comisión: $7500.0
   ====================================
   [SMS] Conectando al proveedor de mensajeria...
   [SMS] Para Ana: Transferiste $150000.0 a la cuenta 001-2
   [AUDITORIA] 2026-10-03T15:32:52.269732900 OTRO_BANCO 001-1 -> 001-2 $150000.0
   Cuota de manejo cobrada a 001-1
   Cuota de manejo cobrada a 001-2
   Exception in thread "main" java.lang.UnsupportedOperationException: Un CDT no permite retiros antes del vencimiento
       at CDT.retirar(CDT.java:12)
       at CobroCuotaManejo.cobrarMensual(CobroCuotaManejo.java:8)
       at Main.main(Main.java:12)
  ```
   Se genera el error `java.lang.UnsupportedOperationException: Un CDT no permite retiros antes del vencimiento`.
   **¿Qué pasaría en producción si el proceso de cobro corre de noche para un millón de cuentas y la cuenta número 500000 es un CDT?**  
   Generaría la misma excepción que en el caso anterior, por lo que no se cobraría el medio millón de cuentas faltantes.
2. **La prueba imposible. Intenten escribir una prueba unitaria que verifique que una transferencia a otro banco cobra $7.500 de comisión, con una condición: la prueba no puede conectarse a Oracle ni enviar un SMS. ¿Lo lograron? ¿Qué les impide hacerlo?**
  Cuando creamos un test, normalmente los creamos tipo caja negra, es decir, definimos lo qué ingresa y el resultado esperado. Estos datos son determinados a través de los parámetros del método, la conexión a Oracle o SMS no depende de los parámetros de ingreso al método transferir. Lo anterior, lleva a que no podamos evitar la conexión a los servicios sin tocar directamente el código del método.

---



### 1.3 Medición "antes"


| Métrica                                                                   | Antes                                                                                                                                         |
| ------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| Líneas del método transferir                                              | 36                                                                                                                                            |
| Número de razones distintas por las que TransaccionService podría cambiar | 7                                                                                                                                             |
| Clases concretas que TransaccionService crea con new                      | 2 clases: 1. OracleRepositorio 2. SmsGateway                                                                                                  |
| Métodos vacíos o que lanzan excepción por "no aplica"                     | 4 métodos: 1. CDT.retirar(double) 2. TarjetaCredito.depositar(double) 3. CreditoVivienda.depositar(double) 4. CreditoVivienda.retirar(double) |
| ¿Se puede probar transferir sin Oracle ni SMS? (Sí/No)                    | No                                                                                                                                            |


---



### 1.4 Diagrama de clases del código original

Dibujen el diagrama de clases UML del código base: clases, interfaces, herencia, implementación y dependencias (new). Puede ser a mano (foto) o con cualquier herramienta (draw.io, PlantUML, Mermaid, etc.). Marquen en rojo las dependencias o herencias que consideren problemáticas.

![Diagrama de clases UML - Código Original](img/UML%20SOLID.png)

---



## Bloque 2 - Refactorización SOLID



### Punto de control S (Single Responsibility Principle)

Se separaron las múltiples responsabilidades que concentraba `TransaccionService.transferir` y `CobroCuotaManejo` extrayendo clases dedicadas para cada tarea: `ValidadorTransaccion` (validación de reglas y topes), `ComprobanteService` (generación e impresión del comprobante), `NotificacionService` (envío de notificaciones) y `AuditoriaService` (registro de auditoría). De este modo, cada clase posee una única razón para cambiar y `TransaccionService` actúa exclusivamente como coordinador del flujo transaccional.

**Preguntas de control:**

- **¿Qué hace** `TransaccionService` **en una sola frase?:** *TransaccionService coordina el flujo de ejecución de una transferencia bancaria entre cuentas.*
- **¿Aparece la palabra "y"?:** No. Su única responsabilidad es la coordinación del flujo; no asume validaciones por sí misma, no calcula tarifas, no imprime comprobantes, no formatea mensajes ni audita eventos.
- **Si el área legal pide cambiar el formato del comprobante, ¿qué archivo tocan?:** Se modifica únicamente `ComprobanteService.java`. Ningún otro archivo del sistema se ve alterado.

---



### Punto de control O (Open/Closed Principle)

Se sustituyó el condicional `switch` de comisiones por polimorfismo mediante el patrón Strategy con la interfaz `TipoTransferencia` y sus implementaciones concretas (`TransferenciaMismoBanco`, `TransferenciaOtroBanco` y `TransferenciaInternacional`). De esta forma, `TransaccionService` queda cerrado a la modificación y abierto a la extensión, permitiendo añadir nuevos tipos de transferencias sin alterar el código existente.

**Pregunta de control:**

> *Si mañana llega un tipo de transferencia nuevo, ¿qué archivos existentes tendrían que modificar? Enumérenlos. Lo ideal es que solo aparezca el punto donde se arma el sistema (el programa principal).*

1. `**Main.java` (únicamente):** El único archivo existente que se modifica es el punto donde se arma el sistema (el programa principal) para instanciar y enviar el nuevo tipo de transferencia (ej. `new TransferenciaPSE()`).
2. **Ningún otro archivo existente se modifica:** El nuevo tipo de transferencia se incorpora creando código nuevo (por ejemplo, `TransferenciaPSE.java`) que implemente la interfaz `TipoTransferencia`. Clases como `TransaccionService.java`, `TipoTransferencia.java`, `ValidadorTransaccion.java`, `ComprobanteService.java`, etc., quedan completamente cerradas a la modificación y abiertas a la extensión.

---



### Punto de control L (Liskov Substitution Principle)

Se rediseñó la jerarquía eliminando el método `retirar` de la clase base `Cuenta` e introduciendo la subclase abstracta `CuentaOperativa` para aquellas cuentas que admiten retiros bajo demanda (`CuentaAhorros`). Al heredar `CDT` directamente de `Cuenta` y tipar las operaciones de retiro en transferencias y cobros con `CuentaOperativa`, se garantiza en tiempo de compilación que los subtipos sean sustituibles sin romper contratos ni arrojar `UnsupportedOperationException`.

**Preguntas de control:**

- **¿Su solución detecta el error al compilar o al ejecutar?:**  
Nuestra solución detecta el error **al compilar** (en tiempo de compilación mediante el sistema de tipos estático de Java). Si un desarrollador intenta incluir un CDT en la lista de cobro (`List.of(ana, luis, cdtAna)`), el compilador emite un error de tipos incompatibles y detiene la construcción del proyecto antes de que llegue a ejecución.
- **¿Por qué es mejor al compilar?:**  
Porque permite una prevención temprana de fallos (*Fail-fast*), evitando que inconsistencias de diseño lleguen a producción o interrumpan procesos masivos críticos (como el cobro nocturno de miles de cuentas que abortaría por un CDT). Además, documenta la regla de negocio de forma explícita y contractual en la signatura del método sin necesidad de comprobaciones defensivas en tiempo de ejecución.
- **¿Por qué "envolver el retiro en un try/catch e ignorar los CDT" no resuelve el problema de diseño?:**  
Porque solo oculta el síntoma sin corregir la violación de LSP; `CDT` seguiría simulando ser un subtipo sustituible de una clase que promete retiros. Además, utiliza indebidamente las excepciones como mecanismo de control del flujo normal del negocio y acopla a `CobroCuotaManejo` a una jerarquía engañosa, con el riesgo añadido de silenciar fallos reales en cuentas que sí debían ser cobradas.

---



### Punto de control I (Interface Segregation Principle)

Se segregó la interfaz monolítica `ProductoBancario` separándola en interfaces de rol específicas: `Extractable` (enfocada únicamente en `generarExtracto`) y `ProductoCredito` (para `calcularIntereses` y `pagarCuota`). Así, `TarjetaCredito` y `CreditoVivienda` ya no están obligadas a implementar métodos ficticios o vacíos (`depositar` y `retirar`), y cualquier producto financiero (incluyendo `Cuenta`) puede emitir su extracto mediante `Extractable`.

**Preguntas de control:**

- **¿Pudieron lograr que un mismo generador de extractos funcione para cuentas, tarjetas y créditos a la vez?:**  
Sí. Tanto las cuentas (`Cuenta`), las tarjetas (`TarjetaCredito`) como los créditos (`CreditoVivienda`) implementan la interfaz común `Extractable`. Gracias al polimorfismo, una misma rutina o servicio (`GeneradorExtractos`) puede recibir una colección heterogénea conteniendo cuentas, tarjetas y créditos hipotecarios, e imprimir el extracto de cada uno de manera uniforme y transparente.
- **¿Qué interfaz necesitó para eso?:**  
Necesitó la interfaz segregada `**Extractable**` (que declara únicamente el método `String generarExtracto()`).
- **¿Por qué no necesitó conocer los demás métodos de cada producto?:**  
Porque para generar un extracto, el generador únicamente requiere consultar la representación del estado o resumen financiero del producto. Conocer si un producto permite `depositar`, `retirar`, `pagarCuota` o `calcularIntereses` viola el Principio de Segregación de Interfaces (ISP), ya que son responsabilidades operativas ajenas a la generación de reportes. Al depender exclusivamente de `Extractable`, el generador está 100% desacoplado de las operaciones transaccionales y de amortización de cada producto, garantizando que cambios en las reglas de retiro, pago o intereses no afecten ni rompan el generador de extractos.

---



### Punto de control D (Dependency Inversion Principle)

Se eliminó la creación de dependencias con `new` dentro de `TransaccionService`, haciendo que dependa exclusivamente de abstracciones (`TransaccionRepositorio`, `NotificacionService`, `ComprobanteService`, `AuditoriaService` y `ValidadorTransaccion`) recibidas mediante inyección por constructor. Con ello, todo el armado del sistema se centraliza en el programa principal (`Main.java`), permitiendo desacoplar la lógica de negocio de la infraestructura tecnológica (Oracle, SMS).

**Preguntas de control:**

- **¿Cuántas clases concretas conoce ahora** `TransaccionService`**?:**  
 No contiene ninguna llamada a `new` ni tiene referencias a clases concretas. Todas sus dependencias (`ValidadorTransaccion`, `TransaccionRepositorio`, `ComprobanteService`, `NotificacionService`, `AuditoriaService`) son interfaces. Sus parámetros en el método `transferir` son clases abstractas (`CuentaOperativa`, `Cuenta`) e interfaces (`TipoTransferencia`).
- **¿Quién decide si se usa Oracle o si se notifica por SMS?:**  
El programa principal (`Main.java`), que actúa como el *Composition Root*. `TransaccionService` es totalmente agnóstico a la infraestructura; es `Main` quien decide instanciar `OracleRepositorio` y `SmsNotificacionService` y pasárselos al servicio. Si mañana el banco decide migrar a PostgreSQL o notificar por correo o WhatsApp, solo se cambia la instanciación en `Main`, sin tocar una sola línea de `TransaccionService`.
- **Vuelvan al experimento 2 del bloque 1: ¿ya es posible esa prueba?:**  
Sí, ya es completamente posible. En el experimento 2 del bloque 1 era imposible verificar de forma unitaria la comisión de 7.500 sin conectarse a la base de datos Oracle y disparar un SMS porque las clases estaban acopladas fijamente con `new`. Ahora, gracias a la inyección de dependencias mediante interfaces, un test unitario puede suministrar dobles de prueba (*mocks*, *fakes* o *stubs* en memoria) para el repositorio y el notificador (por ejemplo, `(origen, destino, monto, comision) -> {}`). Esto permite que la prueba unitaria verifique el débito, el crédito y la comisión en milisegundos, de forma aislada, determinista y sin depender de servicios externos.

---



## Bloque 3 - Pruebas unitarias

Se implementaron las 5 pruebas unitarias automatizadas utilizando **JUnit 5** y dobles de prueba (*Fakes/Spies* en memoria) para simular el repositorio y la notificación sin tocar infraestructura real.

**Preguntas de control:**

- **¿Cuánto tardan en ejecutarse todas sus pruebas?:**  
Tardan aproximadamente **135 ms** en total (menos de 0.2 segundos) para descubrir y ejecutar la totalidad de la suite de pruebas.
- **¿Cuántas líneas de** `TransaccionService` **tuvieron que cambiar para poder probarla?:**  
**Cero (0) líneas.** Gracias a la aplicación de DIP en el Punto de control D, `TransaccionService` ya dependía de abstracciones inyectadas por constructor, por lo que fue 100% testeable sin modificar su código de producción.
- **¿Qué habría pasado si intentaran estas mismas pruebas en el bloque 1?:**  
Habría sido imposible ejecutarlas de forma aislada y rápida: cada test habría intentado conectarse a la base de datos real de Oracle (arrojando fallos de conexión a JDBC) y enviar mensajes SMS reales, demorando segundos, generando costos e impidiendo probar escenarios de error de forma segura y determinista.

---



## Bloque 4 - "Negocio pidió cambios"

Evaluación de la mantenibilidad y extensibilidad del diseño refactorizado frente a nuevos requerimientos del negocio.

### 1. Estimación previa sobre el código original (`bloque-0-codigo-base`)

- Para el R1 Transferencias por llave tendríamos que modificar la clase TransaccionService.java, ya que aquí se encuentra el método transferir.
- Para el R2 Cuenta infantil no se modifica ninguna de las clases iniciales, sino que se crea una nueva clase.
- Para el R3 Notificaciones push no se modifica ninguna de las clases iniciales, sino que se crea una nueva clase.
- Para el R4 Sistema antifraude tendríamos que modificar la clase TransaccionService.java, ya que aquí se encuentra el método transferir  y aquí es dónde se debería agregar la verificación [ANTIFRAUDE]
- Para el R5 Migración a PostgreSQL tendríamos que modificar la clase TransaccionService.java, ya que esta es la que instancia el repositorio y aparte tendríamos que crear una nueva clase.
- Para el R6 Pago de servicios públicos tendríamos que modificar la clase TransaccionService.java, ya que esta se encarga de las transferencias y el pago de los servicios es una transferencia. Aparte tocaría crear una interfaz y varias clases que extiendan de esta.

---



### R1: Transferencias por llave

Los clientes pueden transferir fondos utilizando una llave (número celular o documento) en lugar del número de cuenta. Son inmediatas y no tienen comisión.

- **Criterio de aceptación:** Una transferencia de tipo `LLAVE` por 50.000 descuenta exactamente 50.000 de la cuenta de origen (comisión 0).



#### Implementación sobre el código refactorizado

- Se creó la clase `TransferenciaLlave.java` implementando la interfaz `TipoTransferencia`:
  ```java
  public class TransferenciaLlave implements TipoTransferencia {
      @Override
      public String getNombre() {
          return "LLAVE";
      }

      @Override
      public double calcularComision(double monto) {
          return 0;
      }
  }
  ```
- Se añadió la prueba unitaria automatizada `testTransferenciaLlave()` en `TransaccionServiceTest.java` validando que el débito en la cuenta de origen sea exactamente de 50.000 y la comisión registrada sea 0.



#### Registro de impacto en código (Métrica R1)

- **Archivos existentes modificados (en** `src/`**):** **0** (ninguna clase existente del dominio o servicios fue tocada, demostrando cumplimiento estricto de OCP).
- **Archivos nuevos creados (en** `src/`**):** **1** (`TransferenciaLlave.java`).

---



### R2: Cuenta infantil

Nuevo producto para menores de edad con recepción de depósitos sin límite y control de retiros de máximo 200.000 acumulados en un mismo día. Se utiliza como origen de transferencias y está sujeta al cobro de cuota de manejo como cualquier cuenta operativa.

- **Criterio de aceptación:** Si la cuenta ya retiró 150.000 hoy, un retiro de 60.000 se rechaza con excepción y el saldo no cambia.



#### Implementación sobre el código refactorizado

- Se creó la clase `CuentaInfantil.java` que extiende de `CuentaOperativa`:
  ```java
  public class CuentaInfantil extends CuentaOperativa {
      private static final double TOPE_DIARIO_RETIRO = 200_000;
      private double totalRetiradoHoy = 0;

      public CuentaInfantil(String numero, String titular, double saldoInicial) {
          super(numero, titular, saldoInicial);
      }

      @Override
      public void retirar(double monto) {
          if (totalRetiradoHoy + monto > TOPE_DIARIO_RETIRO) {
              throw new IllegalStateException("Supera el tope diario de retiro de $" + TOPE_DIARIO_RETIRO);
          }
          super.retirar(monto);
          totalRetiradoHoy += monto;
      }

      public double getTotalRetiradoHoy() {
          return totalRetiradoHoy;
      }
  }
  ```
- Se añadieron pruebas unitarias en `TransaccionServiceTest.java`:
  - `testCuentaInfantilTopeRetiro()`: Valida el rechazo de retiros que excedan el tope diario y la inmutabilidad del saldo.
  - `testCuentaInfantilComoOrigenTransferenciaYCuotaManejo()`: Valida que pueda ser sustituida como origen de transferencias y participar en el cobro de cuota de manejo respetando LSP.



#### Registro de impacto en código (Métrica R2)

- **Archivos existentes modificados (en** `src/`**):** **0** (gracias a la jerarquía `CuentaOperativa`, `CobroCuotaManejo` y `TransaccionService` operan sin modificaciones).
- **Archivos nuevos creados (en** `src/`**):** **1** (`CuentaInfantil.java`).

---



### R3: Notificaciones push

Además del SMS actual, el cliente debe recibir una notificación push en la aplicación móvil por cada transferencia realizada.

- **Criterio de aceptación:** Por cada transferencia exitosa aparecen en consola un mensaje `[SMS]` y un mensaje `[PUSH]`.



#### Implementación sobre el código refactorizado

- Se aplicó el patrón de diseño estructural **Composite** implementando `NotificacionCompuestaService.java` que delega la notificación a una lista de servicios que implementan `NotificacionService` (`SmsNotificacionService`, `PushNotificacionService`).
- Se crearon las clases `PushGateway.java` (driver de infraestructura simulado) y `PushNotificacionService.java` (adaptador del contrato de notificación).
  ```java
  public class NotificacionCompuestaService implements NotificacionService {
      private final List<NotificacionService> servicios;

      public NotificacionCompuestaService(NotificacionService... servicios) {
          this.servicios = List.of(servicios);
      }

      @Override
      public void notificarTransferencia(Cuenta origen, Cuenta destino, double monto) {
          for (NotificacionService servicio : servicios) {
              servicio.notificarTransferencia(origen, destino, monto);
          }
      }

      @Override
      public void notificarCobroCuota(String numeroCuenta) {
          for (NotificacionService servicio : servicios) {
              servicio.notificarCobroCuota(numeroCuenta);
          }
      }
  }
  ```
- Se añadió la prueba unitaria automatizada `testNotificacionCompuestaSmsYPush()` en `TransaccionServiceTest.java` verificando que ambos canales reciban el evento de notificación sin modificar `TransaccionService`.



#### Registro de impacto en código (Métrica R3)

- **Archivos existentes modificados (en** `src/`**):** **0** (`TransaccionService` interactúa con la interfaz `NotificacionService` mediante DIP y OCP).
- **Archivos nuevos creados (en** `src/`**):** **3** (`PushGateway.java`, `PushNotificacionService.java`, `NotificacionCompuestaService.java`).

---



### R4: Sistema antifraude

Por requerimiento regulatorio, cada transacción exitosa debe enviarse al sistema antifraude del banco (emitiendo `[ANTIFRAUDE]`), preservando la auditoría regulatoria actual (`[AUDITORIA]`). Las transferencias rechazadas no deben generar ningún registro.

- **Criterio de aceptación:** Por cada transferencia exitosa aparecen `[AUDITORIA]` y `[ANTIFRAUDE]`. Una transferencia rechazada no genera ninguno.



#### Implementación sobre el código refactorizado

- Se aplicó nuevamente el patrón **Composite** mediante `AuditoriaCompuestaService.java` implementando `AuditoriaService`.
- Se creó `AntifraudeAuditoriaService.java` para procesar la evaluación de riesgo.
  ```java
  public class AuditoriaCompuestaService implements AuditoriaService {
      private final List<AuditoriaService> servicios;

      public AuditoriaCompuestaService(AuditoriaService... servicios) {
          this.servicios = List.of(servicios);
      }

      @Override
      public void registrar(String tipo, Cuenta origen, Cuenta destino, double monto) {
          for (AuditoriaService servicio : servicios) {
              servicio.registrar(tipo, origen, destino, monto);
          }
      }
  }
  ```
- Se añadieron pruebas unitarias en `TransaccionServiceTest.java`:
  - `testSistemaAntifraudeTransaccionExitosa()`: Comprueba que ambos canales se ejecutan tras una transferencia exitosa.
  - `testSistemaAntifraudeTransaccionRechazada()`: Comprueba que ante un rechazo por validación de monto, ninguno de los dos servicios registra eventos.



#### Registro de impacto en código (Métrica R4)

- **Archivos existentes modificados (en** `src/`**):** **0** (DIP y OCP permiten componer la auditoría en el punto de ensamblado sin tocar `TransaccionService`).
- **Archivos nuevos creados (en** `src/`**):** **2** (`AntifraudeAuditoriaService.java`, `AuditoriaCompuestaService.java`).

---



### R5: Migración a PostgreSQL

El banco migra el motor de persistencia a PostgreSQL (emitiendo `[POSTGRES]`). Se conserva la clase `OracleRepositorio` intacta ante cualquier eventualidad de *rollback*.

- **Criterio de aceptación:** El programa guarda en PostgreSQL y las pruebas unitarias no cambian.



#### Implementación sobre el código refactorizado

- Se creó la clase `PostgreSqlRepositorio.java` implementando la interfaz `TransaccionRepositorio`:
  ```java
  public class PostgreSqlRepositorio implements TransaccionRepositorio {
      @Override
      public void guardarTransaccion(String origen, String destino, double monto, double comision) {
          System.out.println("[POSTGRES] Conectando a jdbc:postgresql://prod-db:5432/BANCO ...");
          System.out.println("[POSTGRES] INSERT INTO transacciones VALUES ('" + origen + "', '" + destino + "', " + monto + ", " + comision + ")");
      }
  }
  ```
- Gracias al Principio de Inversión de Dependencias (DIP), cambiar de Oracle a PostgreSQL solo requiere instanciar `new PostgreSqlRepositorio()` en el punto de ensamblado (`Main.java`). `TransaccionService` y la suite de pruebas unitarias permanecen completamente inalteradas.
- Se añadió la prueba unitaria `testPostgreSqlRepositorio()` en `TransaccionServiceTest.java`.



#### Registro de impacto en código (Métrica R5)

- **Archivos existentes modificados (en** `src/`**):** **0** (`TransaccionRepositorio` y `TransaccionService` permanecen cerradas y sin cambios).
- **Archivos nuevos creados (en** `src/`**):** **1** (`PostgreSqlRepositorio.java`).

---



## Bloque 5 - Revisión cruzada

Equipo: 

- Oscar Ivan Ulises Gutierrez Palacios.
- Daniel Alonso Gracia Pinto.

Comprobar si el diseño es fácil de extender para alguien que no lo escribió.

1. Intercambien su repositorio con otra pareja (el docente indica con cuál). No pueden explicarle su código: el código debe explicarse solo.
2. El docente entregará un requerimiento nuevo. Impleméntenlo sobre el código de la otra pareja.
3. Llenen la lista de revisión y entréguensela a la otra pareja.

**Commit (en el repositorio de la otra pareja, en una rama):** `revision-cruzada`

---



### R6: Pago de servicios públicos

Los clientes podrán pagar sus facturas de servicios públicos (agua, luz, gas, internet) desde una cuenta, indicando la referencia de la factura y el valor.

- El pago tiene una comisión fija de 1.500.
- Aplican las mismas validaciones de monto que en las transferencias.
- Como cualquier transacción, el pago se guarda, genera comprobante, notifica al cliente y pasa por auditoría y antifraude.
- Un CDT no puede pagar servicios.
- **Criterio de aceptación:** Un pago de 184.300 descuenta 185.800 de la cuenta, guarda la transacción e imprime el comprobante con la referencia de la factura como destino.
- **Restricción:** No se permite copiar y pegar la lógica de `TransaccionService`. Se deben reutilizar las piezas que la otra pareja ya construyó.



#### Análisis previo

Al leer el código se observó que un pago de servicios es el mismo flujo que una transferencia (validar, cobrar comisión, mover dinero, guardar, comprobante, notificar, auditar). Lo único que cambia es la comisión (fija de 1.500) y el destino.

#### Archivos creados

- `TipoServicioPublico.java` : enumeración con los servicios admitidos (`AGUA`, `LUZ`, `GAS`, `INTERNET`).
- `FacturaServicioPublico.java`: representa la factura como destino del pago. Extiende `Cuenta` para poder ser recibida por `TransaccionService`, `ComprobanteService`, `NotificacionService` y `AuditoriaService` 
- `PagoServicioPublico.java` : `TipoTransferencia` con la comisión fija de 1.500
- `PagoServiciosService.java`: punto de entrada del caso de uso y delega todo el flujo en `TransaccionService` 
- `Main.java` (modificado, solo el punto de armado): se compuso la auditoría para que el pago pase por auditoría **y** antifraude, y se agregó un pago de ejemplo de 184.300.
- `test/PagoServiciosServiceTest.java` : archivo para los críterios de aceptación.

**¿Cuántas de las piezas que necesitan ya existían en el código de la otra pareja? ¿Pudieron usarlas tal como estaban, o tuvieron que modificarlas?**  
`TransaccionService`, `ValidadorMontoTope`, `TransaccionRepositorio`/`OracleRepositorio`, `ComprobanteBancoAndino`, `SmsNotificacionService`, `ConsolaAuditoriaService`, `AntifraudeAuditoriaService`, `AuditoriaCompuestaService`, la interfaz `TipoTransferencia` y la jerarquía `Cuenta`/`CuentaOperativa`. Todas se usaron tal como estaban, sin modificar su código. 

---



### Lista de revisión


| Lista de revisión                                                             | Sí  | No  | Observación                                                                                                                                                                                                                                                                |
| ----------------------------------------------------------------------------- | --- | --- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Entendimos qué hace cada clase leyendo solo su nombre y sus métodos públicos. | X   |     |                                                                                                                                                                                                                                                                            |
| Pudimos reutilizar piezas existentes sin copiar y pegar código.               | X   |     | `ComprobanteService`, `NotificacionService` y `AuditoriaService` reciben `Cuenta destino`. Para que una factura pudiera ser el destino de un pago, tuvimos que modelarla como subclase de `Cuenta`, aunque una factura no es una cuenta, por eso el cliente recibe un SMS. |
| Implementamos el requerimiento sin modificar la lógica de clases existentes.  | X   |     |                                                                                                                                                                                                                                                                            |
| No encontramos métodos vacíos ni que lancen “no aplica”.                      | X   |     |                                                                                                                                                                                                                                                                            |
| No encontramos if/switch por tipo que tuvimos que extender.                   | X   |     |                                                                                                                                                                                                                                                                            |
| Las pruebas existentes siguieron pasando después de nuestro cambio.           | X   |     |                                                                                                                                                                                                                                                                            |
| No encontramos abstracciones innecesarias (interfaces que no aportan).        | X   |     |                                                                                                                                                                                                                                                                            |


**Lo mejor del diseño:**  
La  inyección de dependencias por constructor y el patrón Strategy en `TipoTransferencia`. La jerarquía `CuentaOperativa` ayudo  en la restricción del CDT  y los dobles de prueba quedaron bien aislados, los pudimos reutilizar en nuestras pruebas.

**Lo que nos costó entender o extender:**

- Los requerimientos R3, R4 y R5 no estaban conectados en `Main.java`**.** Las clases `NotificacionCompuestaService`, `AntifraudeAuditoriaService`/`AuditoriaCompuestaService` y `PostgreSqlRepositorio` existen y tienen pruebas, pero el programa principal seguía usando solo SMS,  auditoría y Oracle, asi que para R6 los agregamos.

