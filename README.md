# KMS

Key Management Service clients for signing Solana transactions.

## Load via Service Provider

```java
final var jsonConfig = """
    {
      "encoding": "base64KeyPair",
      "secret": "ASDF=="
    }""";

final var factoryClass = MemorySignerFactory.class;
final var serviceFactory = ServiceLoader.load(SigningServiceFactory.class).stream()
    .filter(service -> service.type().equals(factoryClass))
    .findFirst().orElseThrow().get();
final var signingService = serviceFactory.createService(executor, JsonIterator.parse(json));

final var base58PublicKey = signingService.publicKey().join();
final byte[] sig = signingService.sign("Hello World".getBytes(StandardCharsets.UTF_8)).join();
```

## Google KMS

### Factory

[GoogleKMSClientFactory]()

#### Create directly

```json
GoogleKMSClientFactory.createService(
    Executors.newVirtualThreadPerTaskExecutor(),
    KeyManagementServiceClient.create(),
    CryptoKeyVersionName.of(
        "google-project-name",
        "global",
        "dev-keyring",
        "dev_key",
        "1"
    )
);
```

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

