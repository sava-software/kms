package software.sava.kms.google;

import com.google.cloud.kms.v1.AsymmetricSignRequest;
import com.google.cloud.kms.v1.CryptoKeyVersionName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.protobuf.ByteString;
import software.sava.core.accounts.PublicKey;
import software.sava.signing.SigningService;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

final class GoogleKMSClient implements SigningService {

  private final ExecutorService executorService;
  private final KeyManagementServiceClient kmsClient;
  private final CryptoKeyVersionName keyVersionName;

  GoogleKMSClient(final ExecutorService executorService,
                  final KeyManagementServiceClient kmsClient,
                  final CryptoKeyVersionName keyVersionName) {
    this.executorService = executorService;
    this.kmsClient = kmsClient;
    this.keyVersionName = keyVersionName;
  }

  private static PublicKey parsePublicKeyFromPem(final String pem) {
    final int from = pem.indexOf('\n') + 1;
    final int to = pem.indexOf('\n', from);
    final var substring = pem.substring(from, to);
    final byte[] pubKeyBytes = Base64.getDecoder().decode(substring);
    return PublicKey.readPubKey(pubKeyBytes, pubKeyBytes.length - PublicKey.PUBLIC_KEY_LENGTH);
  }

  @Override
  public CompletableFuture<PublicKey> publicKey() {
    return CompletableFuture.supplyAsync(() -> {
      final var pemPublicKey = kmsClient.getPublicKey(keyVersionName);
      return parsePublicKeyFromPem(pemPublicKey.getPem());
    }, executorService);
  }

  private CompletableFuture<byte[]> sign(final ByteString msg) {
    return CompletableFuture.supplyAsync(() -> {
      final var signRequest = AsymmetricSignRequest.newBuilder()
          .setName(keyVersionName.toString())
          .setData(msg)
          .build();
      final var result = kmsClient.asymmetricSign(signRequest);
      return result.getSignature().toByteArray();
    }, executorService);
  }

  @Override
  public CompletableFuture<byte[]> sign(final byte[] msg) {
    return sign(ByteString.copyFrom(msg));
  }

  @Override
  public CompletableFuture<byte[]> sign(final byte[] msg, final int offset, final int length) {
    return sign(ByteString.copyFrom(msg, offset, length));
  }

  @Override
  public void close() {
    kmsClient.close();
  }
}