
      subroutine athlet_setup
CH+
CN    ATHLET
CA    GRE
CM    DEI 24.11.2011
C*
CV    2.2C
C*
CP    ATHLET - MAIN PROGRAM
C*
C*
CR    ADIM       AGETPA     AIN0       AIN1       APRESET    ASTART
CR    ATRANS     ECORE      EPRESET    ERRSET     FPRESET    MTIME
CR    SERROR     STIMES
C*
CH-
C
#ifdef _OMP
      USE OMP_LIB
#endif
      USE CACFX, ONLY: LCFX, KCFX, IRCFX
      USE CCS, ONLY: ICPUTM
      USE CCA, ONLY: T, IWBER, IPUNC, VERSN, CPUT0
      USE CCAC, ONLY: T0TRNS
      USE CCC, ONLY: KEYA1, IOIN, IOPRI, IOPLO, IOPL2, IOSIG, PID, RID,
     &               RRID, TARDIR, RESDIR, INFILE, NOQUIT, EXECFIL
      USE EMODATHL, ONLY: IHEAT
      USE CAOHTM, ONLY: IHTMFL
C
      IMPLICIT DOUBLE PRECISION (A-H,O-Z)
C
      CHARACTER DATE55*10, TIME55*10
      INTEGER TIME56,H55,M55
      REAL(4) TA(2)
      CHARACTER*10 NTHRDS, SCHED
C
#ifdef _WIN32
      CALL CPU_TIME(CPUT0)
#else
#ifdef _INTEL
      CALL CPU_TIME(CPUT0)
#else
      CPUT0 = ETIME(TA)
#endif
#endif
C
      IF(LCFX) THEN
         GOTO (1,9,3,9,9,9,7,9,9,9) KCFX/100
      ENDIF
C
 1    VERSN(1) ='ATHLET M'
      VERSN(2) ='od3.0 Cy'
      VERSN(3) ='cle A   '
      VERSN(4) ='26.11.12'
C
      CALL APRESET
      CALL EPRESET
      CALL FPRESET
C
C --- GET PARAMETERS OF ATHLGO AND OPEN FILES
      CALL AGETPA
C
C --- PRINT CODE VERSION
      WRITE(IOPRI,9020) VERSN
C
C --- PRINT NUMBER OF THREADS
#ifdef _OMP
      WRITE(IOPRI,9021)
#else
      WRITE(IOPRI,9023)
#endif
C
C --- PRINT TIME AND DATE OF PROCESSING
      CALL DATE_AND_TIME(DATE55,TIME55)
      DATE55 = DATE55(7:8)//'.'//DATE55(5:6)//'.'//DATE55(1:4)
      TIME55 = TIME55(1:2)//':'//TIME55(3:4)//':'//TIME55(5:6)
C
C --- PRINT TIME AND DATE OF PROCESSING (RISC SYSTEM/6000   AIX)
      WRITE(IOPRI,9030) DATE55, TIME55
      WRITE(IOPRI,9040)
C
C
      CALL ERRSET( 208, 0, 20,2,0,208)
      CALL ERRSET( 202,10,255,2,0,202)
      CALL ERRSET( 141,10,255,2,0,141)
      CALL ERRSET( 162, 0, 20,2,0,162)
      CALL ERRSET( 163, 0, 20,2,0,163)
      CALL ERRSET( 187,10,255,2,0,187)
      CALL ERRSET( 209,10,255,2,0,209)
C
C --- GENERAL INPUT AND DIMENSIONING
C
      KEYA1  = 100
      CALL AIN0
C
C --- OPEN RESTART OUTPUT DATA
      IF (IPUNC .LT. 0) THEN
         IF (LEN_TRIM(PID) > 0) THEN
            OPEN (7,FILE=TARDIR(1:LEN_TRIM(TARDIR)) // '/' //
     &         PID(1:LEN_TRIM(PID)) // '.' // RID(1:LEN_TRIM(RID)) //
     &         '.re',STATUS='UNKNOWN',FORM='FORMATTED')
         ELSE
            OPEN(7,STATUS='UNKNOWN',FORM='FORMATTED')
         ENDIF
      ENDIF
      IF (IPUNC .GT. 0) THEN
      IF (LEN_TRIM(PID) > 0) THEN
            OPEN (7,FILE=TARDIR(1:LEN_TRIM(TARDIR)) // '/' //
     &         PID(1:LEN_TRIM(PID)) // '.' // RID(1:LEN_TRIM(RID)) //
     &         '.re',STATUS='UNKNOWN',FORM='UNFORMATTED')
         ELSE
            OPEN(7,STATUS='UNKNOWN',FORM='UNFORMATTED')
         ENDIF
      ENDIF
C
C --- OPEN RESTART INPUT DATA
      IF (IWBER < 0) THEN
         IF  (LEN_TRIM(PID) > 0) THEN
            OPEN (8,FILE=RESDIR(1:LEN_TRIM(RESDIR)) // '/' //
     &         PID(1:LEN_TRIM(PID)) // '.' // RRID(1:LEN_TRIM(RRID)) //
     &         '.re',STATUS='UNKNOWN',FORM='FORMATTED')
         ELSE
            OPEN (8,STATUS='UNKNOWN',FORM='FORMATTED')
         ENDIF
      ELSEIF (IWBER > 0) THEN
         IF (LEN_TRIM(PID) > 0) THEN
            OPEN (8,FILE=RESDIR(1:LEN_TRIM(RESDIR)) // '/' //
     &         PID(1:LEN_TRIM(PID)) // '.' // RRID(1:LEN_TRIM(RRID)) //
     &         '.re',STATUS='UNKNOWN',FORM='UNFORMATTED')
         ELSE
            OPEN(8,STATUS='UNKNOWN',FORM='UNFORMATTED')
         ENDIF
      ENDIF
C
C --- OPEN RETURN FILE (CFX COUPLING)
      IF (LCFX) THEN
         IF (IPUNC >= 0) THEN
            IF  (LEN_TRIM(PID) > 0) THEN
               OPEN (IRCFX,FILE=TARDIR(1:LEN_TRIM(TARDIR)) // '/' //
     &         PID(1:LEN_TRIM(PID)) // '.' // RID(1:LEN_TRIM(RID)) //
     &         '.ret',STATUS='UNKNOWN',FORM='UNFORMATTED')
            ELSE
               OPEN (IRCFX,STATUS='UNKNOWN',FORM='UNFORMATTED')
            ENDIF
         ELSE
            IF  (LEN_TRIM(PID) > 0) THEN
               OPEN (IRCFX,FILE=TARDIR(1:LEN_TRIM(TARDIR)) // '/' //
     &         PID(1:LEN_TRIM(PID)) // '.' // RID(1:LEN_TRIM(RID)) //
     &         '.ret',STATUS='UNKNOWN',FORM='FORMATTED')
            ELSE
               OPEN (IRCFX,STATUS='UNKNOWN',FORM='FORMATTED')
            ENDIF
         ENDIF
      ENDIF
C
      CALL ADIM
C
C --- INPUT OF MODEL DATA AND INDIVIDUAL PRESET
      KEYA1  = 200
#ifdef _WIN32
#ifndef _COCOSYS
      WRITE(6,*) 'ATHLET: Start of processing input data'
#endif
#endif
      CALL AIN1
#ifdef _WIN32
#ifndef _COCOSYS
      WRITE(6,*) 'ATHLET: End   of processing input data'
#endif
#endif
C
C --- CLOSE INPUT UNIT
      CLOSE (UNIT=1)
C
      IF(LCFX) GOTO 10000
C
C --- GLOBAL INITIALIZATION - START CALCULATION (STEADY STATE)
C
 3    IF (IWBER.NE.0) GOTO 7
      KEYA1 = 300
#ifdef _WIN32
#ifndef _COCOSYS
      WRITE(6,*) 'ATHLET: Start of steady state calculation'
#endif
#endif
C      CALL ASTART
#ifdef _WIN32
#ifndef _COCOSYS
      WRITE(6,*) 'ATHLET: End   of steady state calculation'
#endif
#endif
C
      IF(LCFX) GOTO 10000
C
C --- TRANSIENT PART
C
 7    KEYA1 = 700
C     ZERO-TRANSIENT CALCULATION FOR TFD MODELS?
      IF(T.LT.T0TRNS) KEYA1 = 500
C
C      CALL ATRANS
C
      IF(LCFX) THEN
         IF(KCFX /= 709) GOTO 10000
      ENDIF
C
C --- SUMMARY OF ERRORS/WARNINGS DETECTED
      !CALL SERROR('ATHLET  ',20,-1,' PROGRAM RUN ENDED;',*1000)
C
 1000 CONTINUE
C
      IF (ICPUTM.GT.0) CALL STIMES (3,0)
      IF(IHEAT.GT.0) CALL ECORE(5,ID2,ID3,RD1,RD2)
C
#ifdef _WIN32
C      IF (.NOT. NOQUIT) THEN
C         WRITE(6,*) 'Press "Enter" to continue'
C         READ(5,'(a)')
C      ENDIF
#ifndef _COCOSYS
       WRITE(6,*) 'ATHLET finished!'
#endif
#endif
C
C --- CLOSE I/O FILES
      IF (IHTMFL == 1) THEN
         WRITE(6,2302)
         WRITE(99,2102)
         CLOSE (99)
         CLOSE (98)
      END IF
C
      CLOSE (7)
      CLOSE (IOPLO)
      CLOSE (IOPL2)
      CLOSE (55,STATUS='DELETE')
      CLOSE (IOSIG,STATUS='DELETE')
      CLOSE (IOIN,STATUS='DELETE')
      IF(LCFX) CLOSE (IRCFX,STATUS='DELETE')

C
 9    print *,""

C
 9020 FORMAT(/,' ',80('+'),//,' ATHLET PROGRAM VERSION:',
     &/,' ',23('-'),/,' ',3A8,/,' PROGRAM VERSION DATE: ',A8)
 9021 FORMAT(/,' OMP PARALLEL VERSION OF ATHLET:',/,' ',31('-'),/,
     &' OMP SCHEDULE: DYNAMIC',/,
     &' NUMBER OF THREADS AND CHUNK SIZE SPECIFIED BY INPUT.')
 9023 FORMAT(/,' SERIAL VERSION OF ATHLET',/,' ',24('-'))
 9030 FORMAT(/,' RUN START TIME:',/,' ',15('-'),
     &/,' DATE:  ',A,/,
     &' TIME:  ',A)
 9040 FORMAT(/,' ',80('+'),///)
 2102 FORMAT('</body>'/'</html>')
 2302 FORMAT('</pre>'/'</body>'/'</html>')
C     RETURNS IF LCFX=.T. (WITHOUT RETURN STATEMENT)
10000 CONTINUE
C
      END subroutine

      subroutine athlet_start
      end subroutine

