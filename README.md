# LABORATORIO: FAST PROTOTYPING

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

![Diagrama de clases UML - Código Original](UML%20SOLID.png)

---

## Bloque 2 - Refactorización SOLID

### Punto de control S (Single Responsibility Principle)

**Separación de responsabilidades identificadas en el sistema (Principio S):**

1. **En `TransaccionService.java`:**
   Se extrajeron las siguientes clases para encapsular cada una de las responsabilidades individuales que estaban mezcladas en el método `transferir`:
   - `ValidadorTransaccion.java`: Valida los límites y restricciones del monto de la transferencia.
   - `CalculadoraComision.java`: Calcula el valor de la comisión según el tipo de transferencia.
   - `ComprobanteService.java`: Genera e imprime el comprobante de la transacción.
   - `NotificacionService.java`: Encapsula la lógica de notificación al cliente a través del canal correspondiente (`SmsGateway`).
   - `AuditoriaService.java`: Registra el log y evento de auditoría con la marca de tiempo.
   - `TransaccionService.java`: Actúa exclusivamente como orquestador del flujo de la transferencia.

2. **En `CobroCuotaManejo.java`:**
   - Previamente mezclaba la lógica de negocio (cobro de la cuota debitando de la cuenta) con la notificación/impresión del mensaje a consola.
   - Se delegó la emisión de la notificación a `NotificacionService.notificarCobroCuota`, dejando a `CobroCuotaManejo` con la única responsabilidad de procesar el débito mensual de las cuentas. Si el banco incorpora un servicio de notificación por correo electrónico o cambia el formato del mensaje, `CobroCuotaManejo` no requiere modificación.

**Pregunta de control:**
- **¿Qué hace `TransaccionService` en una sola frase?**  
  *TransaccionService coordina el flujo de ejecución de una transferencia bancaria entre cuentas.*
- **¿Aparece la palabra "y"?**  
  No. Su única responsabilidad es la coordinación del flujo; no asume validaciones por sí misma, no calcula tarifas, no imprime comprobantes, no formatea mensajes ni audita eventos.
- **Si el área legal pide cambiar el formato del comprobante, ¿qué archivo tocan?**  
  Se modifica únicamente `ComprobanteService.java`. Ningún otro archivo del sistema se ve alterado.
