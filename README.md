# LABORATORIO: FAST PROTOTYPING

**Autores:**
- Deivid Farid Ardila Herrera
- Ángel David Beltrán García

**Grupo:** 4  
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

<table>
  <thead>
    <tr>
      <th align="left">Clase / método</th>
      <th align="center">Letra</th>
      <th align="left">Evidencia en el código</th>
      <th align="left">Consecuencia para el banco o el cliente</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>CobroCuotaManejo.java</code></td>
      <td align="center"><strong>S</strong></td>
      <td>
<pre><code class="language-java">public void cobrarMensual (List&lt;Cuenta&gt; cuentas) {
    for (Cuenta cuenta : cuentas) {
        cuenta.retirar (CUOTA);
        System.out.println("Cuota de manejo cobrada a " + cuenta.getNumero());
    }
}</code></pre>
      </td>
      <td>Si el banco incorporara un servicio de notificación por mail, se tendría que cambiar el código del método, por lo que hay dos responsabilidades: la lógica de negocio y la impresión de mensajes</td>
    </tr>
    <tr>
      <td><code>TransaccionService.java</code></td>
      <td align="center"><strong>S</strong></td>
      <td>
<pre><code class="language-java">public void transferir (Cuenta origen, Cuenta destino, double monto, String tipo) {
    // 1. Validación
    (...)
    // 2. Cálculo de la comisión
    (...)
    // 3. Movimiento del dinero
    (...)
    // 4. Persistencia
    (...)
    // 5. Comprobante
    (...)
}</code></pre>
      </td>
      <td>Si el banco agregara un nuevo tipo de dato al comprobante o agregar una notificación al mail debe cambiar la misma clase.</td>
    </tr>
    <tr>
      <td><code>TransaccionService.java</code></td>
      <td align="center"><strong>O</strong></td>
      <td>
<pre><code class="language-java">switch (tipo) {
    case "MISMO_BANCO" -&gt; comision = 0;
    case "OTRO_BANCO" -&gt; comision = 7_500;
    case "INTERNACIONAL" -&gt; comision = monto * 0.03 + 25_000;
    default -&gt; throw new IllegalArgumentException("Tipo de transferencia desconocido");
}</code></pre>
      </td>
      <td>Si el banco en el futuro crea nuevos tipos de transferencia, le tocaría volver a tocar la clase directamente para agregar estos nuevos tipos.</td>
    </tr>
    <tr>
      <td><code>CobroCuotaManejo.java</code></td>
      <td align="center"><strong>L</strong></td>
      <td>
<pre><code class="language-java">public void cobrarMensual (List&lt;Cuenta&gt; cuentas) {
    for (Cuenta cuenta : cuentas) {
        cuenta.retirar (CUOTA);
        System.out.println("Cuota de manejo cobrada a " + cuenta.getNumero());
    }
}</code></pre>
      </td>
      <td>Si el banco cobra a todas las cuentas existentes, también cobrará a los CDTs, y al retirar en una fecha anterior al vencimiento del CDT, generará error.</td>
    </tr>
    <tr>
      <td><code>CDT.java</code></td>
      <td align="center"><strong>L</strong></td>
      <td>
<pre><code class="language-java">@Override
public void retirar (double monto) {
    if (LocalDate.now().isBefore(vencimiento)) {
        throw new UnsupportedOperationException("Un CDT no permite retiros antes del vencimiento");
    }
    super.retirar (monto);
}</code></pre>
      </td>
      <td>Si el Banco intenta realizar un proceso automático como cobrar la CuotaDeManejo, al no haberse cumplido la fecha de retiro, generará error. Esto complica el cobro del resto de clientes.</td>
    </tr>
    <tr>
      <td><code>Cuenta.java</code></td>
      <td align="center"><strong>I</strong></td>
      <td>
<pre><code class="language-java">public void depositar (double monto) {
    if (monto &lt;= 0) throw new IllegalArgumentException("Monto inválido");
    saldo += monto;
}
public void retirar (double monto) {
    if (monto &gt; saldo) throw new IllegalStateException("Saldo insuficiente");
    saldo -= monto;
}</code></pre>
      </td>
      <td>Si el banco creara un nuevo tipo de cuenta donde no se pueda retirar (como Pibank, donde solo se puede transferir), se estaría forzando a las subclases a heredar estos métodos y cambiar el comportamiento inicial.</td>
    </tr>
    <tr>
      <td><code>TarjetaCredito.java</code></td>
      <td align="center"><strong>I</strong></td>
      <td>
<pre><code class="language-java">public void depositar (double monto) { } // no aplica</code></pre>
      </td>
      <td>Al cliente le afecta porque si invoca los métodos en cuestión, no podrá saber el estado del depósito o del retiro porque no fue implementado nunca.</td>
    </tr>
    <tr>
      <td><code>CreditoVivienda.java</code></td>
      <td align="center"><strong>I</strong></td>
      <td>
<pre><code class="language-java">public void depositar (double monto) {} // no aplica
public void retirar (double monto) { } // no aplica</code></pre>
      </td>
      <td>Al cliente le afecta porque si invoca los métodos en cuestión, no podrá saber el estado del depósito o del retiro porque no fue implementado nunca.</td>
    </tr>
    <tr>
      <td><code>TransaccionService.java</code></td>
      <td align="center"><strong>D</strong></td>
      <td>
<pre><code class="language-java">private final OracleRepositorio repositorio = new OracleRepositorio();
private final SmsGateway sms = new SmsGateway();</code></pre>
      </td>
      <td>Si el banco cambia de proveedores, tendría que tocar todas la clases involucradas. Con una abstracción, solo debe modificar las clases de los proveedores.</td>
    </tr>
  </tbody>
</table>

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

| Métrica | Antes |
| :--- | :--- |
| Líneas del método transferir | 36 |
| Número de razones distintas por las que TransaccionService podría cambiar | 7 |
| Clases concretas que TransaccionService crea con new | 2 clases:<br>1. OracleRepositorio<br>2. SmsGateway |
| Métodos vacíos o que lanzan excepción por "no aplica" | 4 métodos:<br>1. CDT.retirar(double)<br>2. TarjetaCredito.depositar(double)<br>3. CreditoVivienda.depositar(double)<br>4. CreditoVivienda.retirar(double) |
| ¿Se puede probar transferir sin Oracle ni SMS? (Sí/No) | No |

---

### 1.4 Diagrama de clases del código original

Dibujen el diagrama de clases UML del código base: clases, interfaces, herencia, implementación y dependencias (new). Puede ser a mano (foto) o con cualquier herramienta (draw.io, PlantUML, Mermaid, etc.). Marquen en rojo las dependencias o herencias que consideren problemáticas.

![Diagrama de clases UML - Código Original](img/UML%20SOLID.png)

---

## Bloque 2 - Refactorización SOLID

### Punto de control S (Single Responsibility Principle)

Se separaron las múltiples responsabilidades que concentraba `TransaccionService.transferir` y `CobroCuotaManejo` extrayendo clases dedicadas para cada tarea: `ValidadorTransaccion` (validación de reglas y topes), `ComprobanteService` (generación e impresión del comprobante), `NotificacionService` (envío de notificaciones) y `AuditoriaService` (registro de auditoría). De este modo, cada clase posee una única razón para cambiar y `TransaccionService` actúa exclusivamente como coordinador del flujo transaccional.

**Pregunta de control:**
> *Después del cambio, describan en una frase qué hace TransaccionService. ¿Aparece la palabra "y"? Si el área legal pide cambiar el formato del comprobante, ¿qué archivo tocan?*

**Respuesta:**
- **¿Qué hace `TransaccionService` en una sola frase?:** *TransaccionService coordina el flujo de ejecución de una transferencia bancaria entre cuentas.*
- **¿Aparece la palabra "y"?:** No. Su única responsabilidad es la coordinación del flujo; no asume validaciones por sí misma, no calcula tarifas, no imprime comprobantes, no formatea mensajes ni audita eventos.
- **Si el área legal pide cambiar el formato del comprobante, ¿qué archivo tocan?:** Se modifica únicamente `ComprobanteService.java`. Ningún otro archivo del sistema se ve alterado.

---

### Punto de control O (Open/Closed Principle)

Se sustituyó el condicional `switch` de comisiones por polimorfismo mediante el patrón Strategy con la interfaz `TipoTransferencia` y sus implementaciones concretas (`TransferenciaMismoBanco`, `TransferenciaOtroBanco` y `TransferenciaInternacional`). De esta forma, `TransaccionService` queda cerrado a la modificación y abierto a la extensión, permitiendo añadir nuevos tipos de transferencias sin alterar el código existente.

**Pregunta de control:**
> *Si mañana llega un tipo de transferencia nuevo, ¿qué archivos existentes tendrían que modificar? Enumérenlos. Lo ideal es que solo aparezca el punto donde se arma el sistema (el programa principal).*

**Respuesta:**
1. **`Main.java` (únicamente):** El único archivo existente que se modifica es el punto donde se arma el sistema (el programa principal) para instanciar y enviar el nuevo tipo de transferencia (ej. `new TransferenciaPSE()`).  
2. **Ningún otro archivo existente se modifica:** El nuevo tipo de transferencia se incorpora creando código nuevo (por ejemplo, `TransferenciaPSE.java`) que implemente la interfaz `TipoTransferencia`. Clases como `TransaccionService.java`, `TipoTransferencia.java`, `ValidadorTransaccion.java`, `ComprobanteService.java`, etc., quedan completamente cerradas a la modificación y abiertas a la extensión.

---

### Punto de control L (Liskov Substitution Principle)

Se rediseñó la jerarquía eliminando el método `retirar` de la clase base `Cuenta` e introduciendo la subclase abstracta `CuentaOperativa` para aquellas cuentas que admiten retiros bajo demanda (`CuentaAhorros`). Al heredar `CDT` directamente de `Cuenta` y tipar las operaciones de retiro en transferencias y cobros con `CuentaOperativa`, se garantiza en tiempo de compilación que los subtipos sean sustituibles sin romper contratos ni arrojar `UnsupportedOperationException`.

**Pregunta de control:**
> *¿Su solución detecta el error al compilar (o con el verificador de tipos de su lenguaje) o al ejecutar? ¿Por qué es mejor lo primero? Si alguien propone "envolver el retiro en un try/catch e ignorar los CDT", ¿por qué eso no resuelve el problema de diseño?*

**Respuesta:**
- **¿Su solución detecta el error al compilar o al ejecutar?:**  
  Nuestra solución detecta el error **al compilar** (en tiempo de compilación mediante el sistema de tipos estático de Java). Si un desarrollador intenta incluir un CDT en la lista de cobro (`List.of(ana, luis, cdtAna)`), el compilador emite un error de tipos incompatibles y detiene la construcción del proyecto antes de que llegue a ejecución.
- **¿Por qué es mejor al compilar?:**  
  1. *Prevención temprana de fallos (Fail-fast):* Detectar el error en compilación evita que errores de diseño o de lógica lleguen a ambientes de prueba o a producción.  
  2. *Seguridad en procesos batch críticos:* Si el proceso de cobro se ejecutara de noche para 1.000.000 de cuentas y la cuenta 500.000 fuera un CDT, un fallo en tiempo de ejecución abortaría el proceso, dejando la mitad de las cuentas sin cobrar e inconsistencias operativas. La verificación en compilación garantiza contractualmente que toda cuenta procesada es apta para cobro.  
  3. *Claridad y contratos expresivos:* La signatura `cobrarMensual(List<? extends CuentaOperativa>)` documenta con precisión la precondición del método sin depender de comentarios ni de validaciones manuales con `instanceof`.
- **¿Por qué "envolver el retiro en un try/catch e ignorar los CDT" no resuelve el problema de diseño?:**  
  1. *Mantiene la violación de LSP:* No resuelve el defecto del modelo; solo oculta el síntoma. `CDT` seguiría pretendiendo ser un tipo sustituible de una clase que promete retiros, rompiendo el contrato en tiempo de ejecución.  
  2. *Acoplamiento indebido:* `CobroCuotaManejo` tendría que asumir que la jerarquía miente y manejar excepciones para decidir qué procesar y qué ignorar.  
  3. *Mal uso de excepciones:* Las excepciones deben utilizarse para circunstancias excepcionales o imprevistas, no para controlar el flujo normal del negocio (saber que los CDT no pagan cuota de manejo).  
  4. *Riesgo de enmascarar errores verdaderos:* Un bloque `try/catch` genérico puede silenciar fallos reales en cuentas que sí debían ser cobradas.

---

### Punto de control I (Interface Segregation Principle)

Se segregó la interfaz monolítica `ProductoBancario` separándola en interfaces de rol específicas: `Extractable` (enfocada únicamente en `generarExtracto`) y `ProductoCredito` (para `calcularIntereses` y `pagarCuota`). Así, `TarjetaCredito` y `CreditoVivienda` ya no están obligadas a implementar métodos ficticios o vacíos (`depositar` y `retirar`), y cualquier producto financiero (incluyendo `Cuenta`) puede emitir su extracto mediante `Extractable`.

**Pregunta de control:**
> *¿Pudieron lograr que un mismo generador de extractos funcione para cuentas, tarjetas y créditos a la vez? ¿Qué interfaz necesitó para eso, y por qué no necesitó conocer los demás métodos de cada producto?*

**Respuesta:**
- **¿Pudieron lograr que un mismo generador de extractos funcione para cuentas, tarjetas y créditos a la vez?:**  
  **Sí.** Tanto las cuentas (`Cuenta`), las tarjetas (`TarjetaCredito`) como los créditos (`CreditoVivienda`) implementan la interfaz común `Extractable`. Gracias al polimorfismo, una misma rutina o servicio (`GeneradorExtractos`) puede recibir una colección heterogénea conteniendo cuentas, tarjetas y créditos hipotecarios, e imprimir el extracto de cada uno de manera uniforme y transparente.
- **¿Qué interfaz necesitó para eso?:**  
  Necesitó la interfaz segregada **`Extractable`** (que declara únicamente el método `String generarExtracto()`).
- **¿Por qué no necesitó conocer los demás métodos de cada producto?:**  
  Porque para generar un extracto, el generador únicamente requiere consultar la representación del estado o resumen financiero del producto. Conocer si un producto permite `depositar`, `retirar`, `pagarCuota` o `calcularIntereses` viola el Principio de Segregación de Interfaces (ISP), ya que son responsabilidades operativas ajenas a la generación de reportes. Al depender exclusivamente de `Extractable`, el generador está 100% desacoplado de las operaciones transaccionales y de amortización de cada producto, garantizando que cambios en las reglas de retiro, pago o intereses no afecten ni rompan el generador de extractos.

---

### Punto de control D (Dependency Inversion Principle)

Se eliminó la creación de dependencias con `new` dentro de `TransaccionService`, haciendo que dependa exclusivamente de abstracciones (`TransaccionRepositorio`, `NotificacionService`, `ComprobanteService`, `AuditoriaService` y `ValidadorTransaccion`) recibidas mediante inyección por constructor. Con ello, todo el armado del sistema se centraliza en el programa principal (`Main.java`), permitiendo desacoplar la lógica de negocio de la infraestructura tecnológica (Oracle, SMS).

**Pregunta de control:**
> *¿Cuántas clases concretas conoce ahora TransaccionService? ¿Quién decide si se usa Oracle o si se notifica por SMS? Vuelvan al experimento 2 del bloque 1: ¿ya es posible esa prueba?*

**Respuesta:**
- **¿Cuántas clases concretas conoce ahora `TransaccionService`?:**  
  **Cero (0).** `TransaccionService` no contiene ninguna llamada a `new` ni tiene referencias a clases concretas. Todas sus dependencias (`ValidadorTransaccion`, `TransaccionRepositorio`, `ComprobanteService`, `NotificacionService`, `AuditoriaService`) son interfaces. Sus parámetros en el método `transferir` son clases abstractas (`CuentaOperativa`, `Cuenta`) e interfaces (`TipoTransferencia`). Cumple rigurosamente la regla: *"Los módulos de alto nivel no deben depender de módulos de bajo nivel; ambos deben depender de abstracciones"*.
- **¿Quién decide si se usa Oracle o si se notifica por SMS?:**  
  **El programa principal (`Main.java`)**, que actúa como el *Composition Root* (punto centralizado de armado del sistema). `TransaccionService` es totalmente agnóstico a la infraestructura; es `Main` quien decide instanciar `OracleRepositorio` y `SmsNotificacionService` y pasárselos al servicio. Si mañana el banco decide migrar a PostgreSQL o notificar por correo o WhatsApp, solo se cambia la instanciación en `Main`, sin tocar una sola línea de `TransaccionService`.
- **Vuelvan al experimento 2 del bloque 1: ¿ya es posible esa prueba?:**  
  **Sí, ya es completamente posible.** En el experimento 2 del bloque 1 era imposible verificar de forma unitaria la comisión de \$7.500 sin conectarse a la base de datos Oracle y disparar un SMS porque las clases estaban acopladas fijamente con `new`. Ahora, gracias a la inyección de dependencias mediante interfaces, un test unitario puede suministrar dobles de prueba (*mocks*, *fakes* o *stubs* en memoria) para el repositorio y el notificador (por ejemplo, `(origen, destino, monto, comision) -> {}`). Esto permite que la prueba unitaria verifique el débito, el crédito y la comisión en milisegundos, de forma aislada, determinista y sin depender de servicios externos.
