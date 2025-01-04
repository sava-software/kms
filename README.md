# KMS

Key Management Service clients for the purpose of signing Solana transactions.

## Load via Service Provider

```java
final var jsonConfig = ""; // See examples below.

final var factoryClassName = "software.sava.kms.google.GoogleKMSClientFactory";
final var serviceFactory = ServiceLoader.load(SigningServiceFactory.class).stream()
    .filter(service -> service.type().name().equals(factoryClassName))
    .findFirst().orElseThrow().get();

final var signingService = serviceFactory.createService(executor, JsonIterator.parse(json));

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
  "cryptoKeyVersion": "1",
  "capacity": {
    "minCapacityDuration": "PT8S",
    "maxCapacity": 300,
    "resetDuration": "PT6S"
  }
}
```

## Local Disk to In Memory

[Supported private key encodings.](https://github.com/sava-software/sava/blob/main/rpc/src/main/java/software/sava/rpc/json/PrivateKeyEncoding.java)

### Secret Factory

[MemorySignerFactory](https://github.com/sava-software/kms/blob/main/kms_core/src/main/java/software/sava/kms/core/signing/MemorySignerFactory.java)

#### JSON Config

```json
{
  "pubKey": "<PUB_KEY>",
  "encoding": "base64KeyPair",
  "secret": "<BASE64_ENCODED_PUBLIC_PRIVATE_KEY_PAIR>"
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

## HTTP KMS

### Headers:

* X-ENCODING:
    * base58
    * base64

### Endpoints

#### GET v0/publicKey

* Response Headers:
    * X-ENCODING: Defaults to base58.

### POST v0/sign

* Request Headers:
    * X-ENCODING: base64

### Factory

[HttpKMSClientFactory](https://github.com/sava-software/kms/blob/main/http_kms/src/main/java/software/sava/http/google/HttpKMSClientFactory.java)

#### JSON Config

```json
{
  "endpoint": "https://api.signing.service/",
  "capacity": {
    "minCapacityDuration": "PT8S",
    "maxCapacity": 300,
    "resetDuration": "PT6S"
  }
}
```
