SUBROUTINE N3SET( NSEGS,IZONE,NOLAYS,SV,TT,IQF,ISEG,ISD )
      USE CCC,  ONLY: KEYA1
      USE CDGE, ONLY: VOLI, ZTI, ZBI
      USE CDML, ONLY: IMLK, IML
      USE CDNW, ONLY: IILO, IIRO
      USE CDPR, ONLY: TFHT, TL
      USE CDQ,  ONLY: QI
      USE CDSS, ONLY: NSSITE
      USE CDTF, ONLY: XQM, AV, ROF, IKS
      USE CGCO, ONLY: LSGIMP, YNAME
      USE CHCD, ONLY: NKHCO, S0H
      USE CHCP, ONLY: IHV, LAYAL1, LHLENG, LHCU, NRODS, IAHO, IOUT,POWERN
      USE CHCO, ONLY: L7IHV, L2RODS, LIHROD, HC
      USE CHRD, ONLY: LFLUID
      USE CKBO, ONLY: LBORON, CBOR, XBOR
      USE CNI,  ONLY: NLAYSK, INLAYS, ISDK
USE grs_AthletImplementation
DIMENSION NSEGS(NRODS),IZONE(IHV),NOLAYS(L7IHV),IQF(L2RODS),ISEG(LIHROD)
real(8),dimension(LAYAL1)::SV
real(8),dimension(LAYAL1)::TT
call athlet_instance%log%info("grs.Athlet", "n3set() - entry")

      call athlet_instance%n3set%n3set_transferCCC(KEYA1)
      call athlet_instance%n3set%n3set_transferCDGE(VOLI,size(VOLI),ZTI,size(ZTI),ZBI,size(ZBI))
      call athlet_instance%n3set%n3set_transferCDML(IMLK,size(IMLK),IML,size(IML))
      !call n3set_client%n3set_transferCDNW(IILO,size(IILO),IIRO,size(IIRO))
      call athlet_instance%n3set%n3set_transferCDPR(TFHT,size(TFHT),TL,size(TL))
      call athlet_instance%n3set%n3set_transferCDQ(QI,size(QI))
      !call n3set_client%n3set_transferCDSS(NSSITE)
      call athlet_instance%n3set%n3set_transferCDTF(XQM,size(XQM),AV,size(AV),ROF,size(ROF), IKS)
      !call n3set_client%n3set_transferCGCO(LSGIMP,YNAME,size(YNAME))
      !call n3set_client%n3set_transferCHCD(NKHCO,size(NKHCO))
      !away IHV,LAYAL1,LHLENG,LHCU,NRODS,IAHO,size(IAHO),IOUT
      call athlet_instance%n3set%n3set_transferCHCP(POWERN,size(POWERN))
      !L7IHV,L2RODS,LOHROD - away
      call athlet_instance%n3set%n3set_transferCHCO(HC,size(HC))
      !call n3set_client%n3set_transferCHRD(LFLUID,size(LFLUID))
      !LBORON - away
      !call athlet_instance%n3set%n3set_transferCKBO(LBORON,CBOR,size(CBOR),XBOR,size(XBOR))
      call athlet_instance%n3set%n3set_transferCNI(NLAYSK,size(NLAYSK),INLAYS,ISDK)
      call athlet_instance%n3set%n3set_invoke(NSEGS,size(NSEGS),IZONE,size(IZONE),NOLAYS,size(NOLAYS),SV,size(SV),TT,size(TT),IQF,size(IQF),ISEG,size(ISEG),ISD)
      call athlet_instance%n3set%n3set_transferResults(XBOR,size(XBOR),NLAYSK,size(NLAYSK),INLAYS,ISDK)
call athlet_instance%log%info("grs.Athlet", "n3set() - exit")
END
