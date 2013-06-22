      SUBROUTINE N3REST(IDIR, IFORM, IUN)
      USE CCA,   ONLY: L3DNK
      USE CNK,   ONLY: INITIA, QD
      USE CNR,   ONLY: QNKI0I
      USE CNI,   ONLY: NKSGMX
      USE grs_AthletImplementation
      call athlet_instance%log%info("grs.Athlet", "n3rest() - entry")
      !call n3rest_client%n3rest_transferCCA(L3DNK)
      !QD
      call athlet_instance%n3rest%n3rest_transferCNK(INITIA,QD,size(QD))
      call athlet_instance%n3rest%n3rest_transferCNR(QNKI0I,size(QNKI0I))
      !call n3rest_client%n3rest_transferCNI(NKSGMX)
      call athlet_instance%n3rest%n3rest_invoke(IDIR,IFORM,IUN)
      call athlet_instance%n3rest%n3rest_transferResults(QNKI0I,size(QNKI0I))
      call athlet_instance%log%info("grs.Athlet", "n3rest() - exit")
      END
