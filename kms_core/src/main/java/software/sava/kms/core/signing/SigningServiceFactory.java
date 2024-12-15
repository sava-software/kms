package software.sava.kms.core.signing;

import systems.comodal.jsoniter.JsonIterator;

import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

public interface SigningServiceFactory {

  SigningService createService(final ExecutorService executorService,
                               final JsonIterator ji,
                               final Predicate<Throwable> errorTracker);
}
