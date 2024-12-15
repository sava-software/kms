module kms.kms.core.main {
  requires software.sava.core;
  requires software.sava.rpc;
  requires software.sava.core_services;
  requires systems.comodal.json_iterator;

  exports software.sava.kms.core.signing;

  provides software.sava.kms.core.signing.SigningServiceFactory with
      software.sava.kms.core.signing.MemorySignerFactory,
      software.sava.kms.core.signing.MemorySignerFromFilePointerFactory;
}