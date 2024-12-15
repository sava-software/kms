package software.sava.kms.core.signing;

import software.sava.rpc.json.PrivateKeyEncoding;
import systems.comodal.jsoniter.JsonIterator;

import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

public final class MemorySignerFactory implements SigningServiceFactory {

  @Override
  public SigningService createService(final ExecutorService executorService,
                                      final JsonIterator ji,
                                      final Predicate<Throwable> errorTracker) {
    final var signer = PrivateKeyEncoding.fromJsonPrivateKey(ji);
    return new MemorySigner(signer);
  }
}
