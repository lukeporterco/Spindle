# Artifact Trust

Spindle Security-1 adds a lightweight artifact trust model for local `mods/` jars and the existing `spindle.lock.json`.

## Current Inputs

Spindle currently recognizes:

- local jar presence in `mods/`
- `spindle.lock.json` hash identity
- optional detached sidecars named `mods/example.jar.spindle-signature.json`

It does not yet add:

- Modrinth or CurseForge identity integration
- Sigstore
- central registries
- human review
- sandboxing

## Sidecar Format

Spindle uses a loader-native detached sidecar:

```json
{
  "schemaVersion": 1,
  "signatureKind": "spindle-ed25519",
  "algorithm": "Ed25519",
  "signerId": "example.dev",
  "publicKey": "<base64 x509 ed25519 public key>",
  "signature": "<base64 signature>",
  "artifactSha256": "<hex>",
  "signedFields": {
    "modId": "examplemod",
    "version": "1.0.0"
  }
}
```

The verified payload is deterministic:

```text
spindle-artifact-signature-v1
artifactSha256=<sha256>
modId=<modId>
version=<version>
signerId=<signerId>
```

## Trust States

- `local-unsigned`: no sidecar is present yet
- `locked-hash`: the current artifact matches verified `spindle.lock.json` identity, but no publisher signature is present
- `signed-artifact`: a valid Spindle Ed25519 sidecar verified the jar hash and signed fields
- `signature-sidecar-invalid`: a sidecar exists, but it is malformed or unsupported
- `signature-artifact-hash-mismatch`: the sidecar hash does not match the actual jar
- `signature-invalid`: the sidecar is structurally valid, but signature verification failed

## Findings

- `SEC-TRUST-001`: warning for `local-unsigned`
- `SEC-TRUST-002`: fatal for malformed or unsupported sidecars
- `SEC-TRUST-003`: fatal for sidecar hash mismatch
- `SEC-TRUST-004`: fatal for signature verification failure
- `SEC-TRUST-005`: warning for `locked-hash`
- `SEC-TRUST-006`: warning when provenance is not present

Invalid claimed signatures are fatal because they assert publisher trust and fail verification.

## Practical Reading

- local development jar: runs with warnings
- lockfile-managed pack: repeatable hash identity, but still no publisher identity unless signed
- signed release: validated publisher claim and artifact hash
- tampered signed jar: blocked before lifecycle execution
