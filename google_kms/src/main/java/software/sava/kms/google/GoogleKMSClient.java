package software.sava.kms.google;

import com.google.cloud.kms.v1.AsymmetricSignRequest;
import com.google.cloud.kms.v1.CryptoKeyVersionName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.protobuf.ByteString;
import software.sava.core.accounts.PublicKey;
import software.sava.kms.core.signing.SigningService;
import software.sava.services.core.remote.call.Backoff;
import software.sava.services.core.remote.call.Call;
import software.sava.services.core.request_capacity.CapacityMonitor;
import software.sava.services.core.request_capacity.CapacityState;
import software.sava.services.core.request_capacity.ErrorTrackedCapacityMonitor;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

final class GoogleKMSClient implements SigningService {

  private final ExecutorService executorService;
  private final Backoff backoff;
  private final KeyManagementServiceClient kmsClient;
  private final CryptoKeyVersionName keyVersionName;
  private final ErrorTrackedCapacityMonitor<Throwable> capacityMonitor;
  private final CapacityState capacityState;
  private final Predicate<Throwable> errorTracker;

  GoogleKMSClient(final ExecutorService executorService,
                  final Backoff backoff,
                  final KeyManagementServiceClient kmsClient,
                  final CryptoKeyVersionName keyVersionName,
                  final ErrorTrackedCapacityMonitor<Throwable> capacityMonitor,
                  final Predicate<Throwable> errorTracker) {
    this.executorService = executorService;
    this.backoff = backoff;
    this.kmsClient = kmsClient;
    this.keyVersionName = keyVersionName;
    this.capacityMonitor = capacityMonitor;
    this.capacityState = capacityMonitor == null ? null : capacityMonitor.capacityState();
    this.errorTracker = errorTracker;
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
      try {
        final var pemPublicKey = kmsClient.getPublicKey(keyVersionName);
        return parsePublicKeyFromPem(pemPublicKey.getPem());
      } catch (final RuntimeException ex) {
        if (errorTracker != null) {
          errorTracker.test(ex);
        }
        throw ex;
      }
    }, executorService);
  }

  @Override
  public CompletableFuture<PublicKey> publicKeyWithRetries() {
    if (capacityState == null) {
      return Call.createComposedCall(
          this::publicKey,
          backoff,
          "GoogleKMSClient::publicKey"
      ).async(executorService);
    } else {
      return Call.createCourteousCall(
          this::publicKey,
          capacityState,
          backoff,
          "GoogleKMSClient::publicKey"
      ).async(executorService);
    }
  }

  private CompletableFuture<byte[]> sign(final ByteString msg) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        final var signRequest = AsymmetricSignRequest.newBuilder()
            .setName(keyVersionName.toString())
            .setData(msg)
            .build();
        if (capacityState != null) {
          capacityState.claimRequest();
        }
        final var result = kmsClient.asymmetricSign(signRequest);
        return result.getSignature().toByteArray();
      } catch (final RuntimeException ex) {
        if (errorTracker != null) {
          errorTracker.test(ex);
        }
        throw ex;
      }
    }, executorService);
  }

  @Override
  public CompletableFuture<byte[]> sign(final byte[] msg) {
    return sign(ByteString.copyFrom(msg));
  }

  @Override
  public CapacityMonitor capacityMonitor() {
    return capacityMonitor;
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
