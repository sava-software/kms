# KMS

Key Management Service clients for the purpose of signing Solana transactions.

## Load via Service Provider

```java
final var capacityConfig = CapacityConfig.createSimpleConfig(
    Duration.ofSeconds(8),
    300,
    Duration.ofSeconds(6)
);
final var capacityMonitor = capacityConfig.createMonitor("Google KMS", GoogleKMSErrorTrackerFactory.INSTANCE);

final var jsonConfig = """
    {
      "encoding": "base64KeyPair",
      "secret": "ASDF=="
    }""";

final var factoryClass = MemorySignerFactory.class;
final var serviceFactory = ServiceLoader.load(SigningServiceFactory.class).stream()
    .filter(service -> service.type().equals(factoryClass))
    .findFirst().orElseThrow().get();

final var signingService = serviceFactory.createService(executor, JsonIterator.parse(json), capacityMonitor.errorTracker());

final var base58PublicKey = signingService.publicKey().join();

final byte[] sig = signingService.sign("Hello World".getBytes(StandardCharsets.UTF_8)).join();
```

## Google KMS

### Factory

[GoogleKMSClientFactory](https://github.com/sava-software/kms/blob/main/google_kms/src/main/java/software/sava/kms/google/GoogleKMSClientFactory.java#L16)

#### JSON Config

```json
{
  "project": "google-project-name",
  "location": "global",
  "keyRing": "dev-keyring",
  "cryptoKey": "dev_key",
  "cryptoKeyVersion": "1"
}
```

## Local Disk to In Memory

### Secret Factory

[MemorySignerFactory](https://github.com/sava-software/kms/blob/main/kms_core/src/main/java/software/sava/kms/core/signing/MemorySignerFactory.java)

#### JSON Config

```json
{
  "encoding": "base64KeyPair",
  "secret": "ASDF=="
}
```

### File Pointer Factory

Should point to a file with the secret contents as shown above.

[MemorySignerFromFilePointerFactory](https://github.com/sava-software/kms/blob/main/kms_core/src/main/java/software/sava/kms/core/signing/MemorySignerFromFilePointerFactory.java)

#### JSON Config

```json
{
  "filePath": "/path/to/secret.json"
}
```

