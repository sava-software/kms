package software.sava.kms.core.signing;

import software.sava.services.core.request_capacity.trackers.ErrorTrackerFactory;
import systems.comodal.jsoniter.JsonIterator;

import java.util.concurrent.ExecutorService;

public interface SigningServiceFactory {

  SigningService createService(final ExecutorService executorService,
                               final JsonIterator ji,
                               final ErrorTrackerFactory<Throwable> errorTrackerFactory);

  SigningService createService(final ExecutorService executorService,
                               final JsonIterator ji);

  default SigningService createService(final JsonIterator ji) {
    return createService(null, ji, null);
  }
}
