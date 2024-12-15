module kms.kms.core.main {
  requires systems.comodal.json_iterator;
  requires software.sava.core;
  requires software.sava.rpc;

  exports software.sava.kms.core.signing;

  provides software.sava.kms.core.signing.SigningServiceFactory with
      software.sava.kms.core.signing.MemorySignerFactory,
      software.sava.kms.core.signing.MemorySignerFromFilePointerFactory;
}