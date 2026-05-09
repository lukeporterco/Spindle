# Static Risk Signals

Security-2 adds warning-only static risk visibility to `spindle.security-report.json`.

The scanner is intentionally small and deterministic:

- it reads jar entries directly
- it reads `.class` constant-pool UTF-8 strings directly
- it does not classload mod classes
- it does not execute mod code
- it does not claim that a warning proves malicious behavior

## Report Shape

`spindle.security-report.json` now includes:

- `riskSignals.summary.signalCount`
- `riskSignals.summary.modCountWithSignals`
- `riskSignals.signals[*].ruleId`
- `riskSignals.signals[*].severity`
- `riskSignals.signals[*].modId`
- `riskSignals.signals[*].location`
- `riskSignals.signals[*].evidence`
- `riskSignals.signals[*].message`
- `riskSignals.signals[*].fix`

`findings` may also include summarized warning entries for the same rule so the top-level warning count stays useful without repeating every detailed signal.

## Current Rules

- `RISK-PROCESS-001`: `Runtime.exec` or `ProcessBuilder`
- `RISK-NATIVE-001`: `System.load`, `System.loadLibrary`, or bundled native files
- `RISK-NETWORK-001`: `java.net`, `java.net.http`, or `javax.net`
- `RISK-REFLECTION-001`: reflection or selected method-handle APIs
- `RISK-UNSAFE-001`: `Unsafe`, `jdk.internal`, or `java.lang.foreign`
- `RISK-DYNAMIC-CLASSLOAD-001`: `ClassLoader`, `URLClassLoader`, or `defineClass`
- `RISK-SCRIPT-001`: `javax.script` or Nashorn
- `RISK-SERVICE-001`: `META-INF/services/...`
- `RISK-EMBEDDED-JAR-001`: nested `.jar` entries
- `RISK-CLASSFILE-001`: class entry could not be statically scanned

## What To Do With A Warning

Treat a warning as a prompt to review and document intent.

Good responses include:

- remove an unused dependency
- remove a stale embedded payload
- avoid native code when a Java-only path is available
- document why a network, reflection, or process feature is required
- wait for a future Spindle capability when that is a better fit than ad hoc implementation
