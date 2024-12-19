package software.sava.kms.core.signing;

import software.sava.services.core.remote.call.Backoff;
import software.sava.services.core.remote.call.BackoffConfig;
import systems.comodal.jsoniter.FieldBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;

import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static systems.comodal.jsoniter.JsonIterator.fieldEquals;

public record SigningServiceConfig(Backoff backoff, SigningService signingService) {

  public static SigningServiceConfig parseConfig(final ExecutorService executorService, final JsonIterator ji) {
    final var parser = new Parser(executorService);
    ji.testObject(parser);
    return parser.createConfig(ji);
  }

  public static SigningServiceConfig parseConfig(final JsonIterator ji) {
    return parseConfig(Executors.newVirtualThreadPerTaskExecutor(), ji);
  }

  private static final class Parser implements FieldBufferPredicate {

    private final ExecutorService executorService;

    private Backoff backoff;
    private String factoryClass;
    private SigningService signingService;
    private int configMark = -1;

    private Parser(final ExecutorService executorService) {
      this.executorService = executorService;
    }

    SigningServiceConfig createConfig(final JsonIterator ji) {
      if (backoff == null) {
        backoff = Backoff.exponential(1, 32);
      }
      if (signingService == null) {
        if (configMark < 0) {
          throw new IllegalStateException("Must configure a signing service");
        } else if (factoryClass == null) {
          throw new IllegalStateException("Must configure a signing service factory class");
        }
        final int mark = ji.mark();
        createService(executorService, ji);
        ji.reset(mark);
      }

      return new SigningServiceConfig(backoff, signingService);
    }

    private void createService(final ExecutorService executorService, final JsonIterator ji) {
      final var serviceFactory = ServiceLoader.load(SigningServiceFactory.class).stream()
          .filter(service -> service.type().getName().equals(factoryClass))
          .findFirst().orElseThrow().get();
      signingService = serviceFactory.createService(executorService, ji);
    }

    @Override
    public boolean test(final char[] buf, final int offset, final int len, final JsonIterator ji) {
      if (fieldEquals("backoff", buf, offset, len)) {
        backoff = BackoffConfig.parseConfig(ji).createHandler();
      } else if (fieldEquals("factoryClass", buf, offset, len)) {
        factoryClass = ji.readString();
      } else if (fieldEquals("config", buf, offset, len)) {
        if (factoryClass == null) {
          configMark = ji.mark();
        } else {
          createService(executorService, ji);
        }
      } else {
        ji.skip();
      }
      return true;
    }
  }
}
