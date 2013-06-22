   SUBROUTINE N3INP
   USE CAI,    ONLY: AIKEY,AICONT
   USE CADIMN, ONLY: INM03
   USE CANW,   ONLY: NOBJ, ANAMO
   USE CCC,    ONLY: CARD, IOIN, IOPRI
   USE CDGE,   ONLY: ZBI
   USE CDNW,   ONLY: IILO, IIRO
   USE CHCD,   ONLY: NHOBJ, ACOMP0, ANAMH, AOLH, AORH, FPROD
   USE CHCP,   ONLY: NRODS, IHVMAX
   USE CRCI,   ONLY: IQF10, AROD
   USE grs_AthletImplementation
   print *, "invoke n3inp"
   call athlet_instance%log%info("grs.Athlet", "n3inp() - entry")
   call athlet_instance%n3inp%n3inp_transferCAI(AIKEY,size(AIKEY),AICONT,size(AICONT))
   call athlet_instance%n3inp%n3inp_transferCADIMN(INM03)
   call athlet_instance%n3inp%n3inp_transferCANW(NOBJ,ANAMO,size(ANAMO))
   call athlet_instance%n3inp%n3inp_transferCCC(CARD,IOIN,IOPRI)
   call athlet_instance%n3inp%n3inp_transferCDGE(ZBI,size(ZBI))
   call athlet_instance%n3inp%n3inp_transferCDNW(IILO,size(IILO),IIRO,size(IILO))
   call athlet_instance%n3inp%n3inp_transferCHCD(NHOBJ,ACOMP0,size(ACOMP0),ANAMH,size(ANAMH),&
        AOLH,size(AOLH),AORH,size(AORH),FPROD,size(FPROD))
   call athlet_instance%n3inp%n3inp_transferCHCP(NRODS,IHVMAX)
   call athlet_instance%n3inp%n3inp_transferCRCI(IQF10,size(IQF10),AROD,size(AROD))
   call athlet_instance%n3inp%n3inp_invoke()
   call athlet_instance%log%info("grs.Athlet", "n3inp() - exit")
   END
