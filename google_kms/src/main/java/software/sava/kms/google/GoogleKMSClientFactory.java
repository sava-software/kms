package software.sava.kms.google;

import com.google.cloud.kms.v1.CryptoKeyVersionName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import software.sava.signing.SigningService;
import software.sava.signing.SigningServiceFactory;
import systems.comodal.jsoniter.FieldBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutorService;

import static systems.comodal.jsoniter.JsonIterator.fieldEquals;

public final class GoogleKMSClientFactory implements SigningServiceFactory, FieldBufferPredicate {

  private CryptoKeyVersionName.Builder builder;

  public GoogleKMSClientFactory() {
  }

  public static SigningService createService(final ExecutorService executorService,
                                             final KeyManagementServiceClient kmsClient,
                                             final CryptoKeyVersionName keyVersionName) {
    return new GoogleKMSClient(
        executorService,
        kmsClient,
        keyVersionName
    );
  }

  @Override
  public SigningService createService(final ExecutorService executorService, final JsonIterator ji) {
    this.builder = CryptoKeyVersionName.newBuilder();
    ji.testObject(this);
    try {
      return new GoogleKMSClient(
          executorService,
          KeyManagementServiceClient.create(),
          builder.build()
      );
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public boolean test(final char[] buf, final int offset, final int len, final JsonIterator ji) {
    if (fieldEquals("project", buf, offset, len)) {
      builder.setProject(ji.readString());
    } else if (fieldEquals("location", buf, offset, len)) {
      builder.setLocation(ji.readString());
    } else if (fieldEquals("keyRing", buf, offset, len)) {
      builder.setKeyRing(ji.readString());
    } else if (fieldEquals("cryptoKey", buf, offset, len)) {
      builder.setCryptoKey(ji.readString());
    } else if (fieldEquals("cryptoKeyVersion", buf, offset, len)) {
      builder.setCryptoKeyVersion(ji.readNumberOrNumberString());
    } else {
      ji.skip();
    }
    return true;
  }
}
