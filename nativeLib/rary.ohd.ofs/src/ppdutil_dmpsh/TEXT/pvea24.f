C MEMBER PVEA24
C  (from old member PDBDMPSH)
C-----------------------------------------------------------------------
C
C                             LAST UPDATE: 05/19/95.10:00:36 BY $WC20SV
C
C @PROCESS LVL(77)
C
C  PGM: PVEA24(UNT,STA,IDTYP,JULDB,JULDE,IUNFLG,IMF,ICOND,
C  PGM:        DATA,RDATA,LDATA) .. SHEF-OUT EA24
C
C
C   IN: UNT .......... UNIT NUMBER FOR OUTPUT IN SHEF FORMAT - INT
C   IN: STA(2) ....... STATION ID AS 8 PACKED CHARS (2A4) - INT
C   IN: IDTYP ........ DATA TYPE AS 4 PACKED CHARS (A4) - INT
C   IN: JULDB ........ BEGINNING ORDINAL DAY NUM (JAN 1 1900 IS 1) - INT
C   IN: JULDE ........ ENDING ORDINAL DAY NUMBER (JAN 1 1900 IS 1) - INT
C   IN: IUNFLG ....... UNITS FLAG, 0 = ENGLISH, 1 = METRIC - INT
C   IN: IMF .......... INCLUDE-MISSING-DATA FLAG, 0=NO, 1=YES - INT
C  I/O: ICOND ........ CONDITION FLAG, IF NOT 0 SKIP RTN, 1 = ERR - INT
C  OTH: DATA(LDATA) .. DUMMY ARRAY - INT*2
C  OTH: RDATA(LDATA) . DUMMY ARRAY - REAL
C   IN: LDATA ........ DIMENSION OF DUMMY ARRAYS - INT
C
C
C  RQD: SUBPROGRAMS:  RPD1S,PVE1S,UFI2AZ,FFF2A,MDYH1
C  RQD: SUBPROGRAMS:  UMEMST,UMOVEX,PVSUBB,PVSUBE
C  RQD: COMMON:       HDFLTS
C
C
C  HIS: WRITTEN BY D. STREET IN APRIL 1988
C  =====================================================================
      SUBROUTINE PVEA24 (UNT,STA,IDTYP,JULDB,JULDE,IUNFLG,IMF,ICOND,
     *                  DATA,RDATA,LDATA)
C
C
C
      COMMON/HDFLTS/ TIME(3),HNAMRF(2),METRIC,CCODE,DDATES(7),TDATES(7),
     *                  LOCAL,NLSTZ,NHOPDB,NHOCAL
      INTEGER TIME,HNAMRF,CCODE,TDATES,DDATES
C
C
      INTEGER   UNT,IDTYP,JULDB,JULDE,IUNFLG,ICOND,ISTAF1,JUL1,JUL2
      INTEGER   NOTYP,IRLEN,LDATA,KEYE,IEA24,IDOTA,LETE,LETZ,LETDH
      INTEGER   LETDU,ISLS,LETS,LETBL,LTADR,LTDDR,LUSDR,LXCDP,LRPDR
      INTEGER   LRIDR,ISS,NUMDTA,IPPMSG,ISTAT,L,LD,IHRINC,IHRSUM,I
      INTEGER   LINC,IDDX,IHOUR,IMO,IDAY,IYR,IH,IDSAV,IZN,ROUTN(2),MOD
      REAL      RD,PPMSNG
C
      REAL      RDATA(LDATA),RMG(3,6),FCT(6),TFC(6)
      INTEGER   IDELT(1),ITYPER(1),NVPDT(1),STA(2)
      INTEGER   LIN1(20),LIN2(20),LMSS6(2),LMSS5(2),LMSS4(2)
      INTEGER*2 DATA(LDATA),MSNG(1)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/ppdutil_dmpsh/RCS/pvea24.f,v $
     . $',                                                             '
     .$Id: pvea24.f,v 1.1 1995/09/17 19:10:11 dws Exp $
     . $' /
C    ===================================================================
C
C
      DATA    RMG   / 4HDEGF, 4HDEGC, 10.0,
     *                4HDEGF, 4HDEGC, 10.0,
     *                4HMI/H, 4HKM/H, 10.0,
     *                4HPCTD, 4HPCTD,  1.0,
     *                4HPCT , 4HPCT ,  1.0,
     *                4HLY  , 4HLY  ,  1.0  /
      DATA    NOTYP,IRLEN,KEYE /  1, 1, 1 /
      DATA    IEA24 / 4HEA24 /
C
      DATA    IDOTA,LETE, LETZ, LETDH / 4H.A  ,4HE   ,4HZ   ,4HDH   /
      DATA    LETDU,ISLS, LETS, LETBL / 4HDU  ,4H/   ,4HS   ,4H     /
      DATA    LTADR,LTDDR,LUSDR,LXCDP / 4HTADR,4HTDDR,4HUSDR,4HXCDP /
      DATA    LRPDR,LRIDR             / 4HRPDR,4HRIDR               /
      DATA    LMSS6,LMSS5             / 4H    ,4H M  ,4H    ,4HM    /
      DATA    LMSS4,ROUTN             / 4H   M,4H    ,4HPVEA,4H24   /
C
C
C
C                  CALL TRACE ROUTINE FOR ENTRY INTO THIS ROUTINE
C
      CALL PVSUBB (ROUTN,ICOND)
C
C                  SKIP THIS RTN IF ICOND = 1, OR IF TYPE IS NOT CORRECT
C
      IF ( ICOND.NE.    0 ) GO TO 100
      IF ( IDTYP.EQ.IEA24 ) GO TO 10
          GO TO 100
10    CONTINUE
C
C                  SET FLAG FOR STA TO BE NAME, NOT NUMBER, SET DATES
C
      ISTAF1 = 0
      JUL1 = JULDB
      JUL2 = JULDE
C
C                  GET DATA FROM PPDB DATA BASE FOR GIVEN STA AND TYPE
C                  OUTPUT ERRORS IF ANY, EXIT RTN IF ERROR OR NO DATA
C
      CALL RPD1S(STA,ISTAF1,NOTYP,IDTYP,JUL1,JUL2,IRLEN,ITYPER,
     *           MTYPES,LDATA,DATA,LDFILL,IDELT,NVPDT,MSNG,ISTAT)
        IF ( ISTAT .NE.0 ) CALL PVE1S(KEYE,STA,IDTYP,ISTAT)
        IF ( ISTAT .NE.0 ) GO TO 100
        IF ( LDFILL.LT.1 ) GO TO 100
C
C                  GET TOTAL NUMBER OF DATA ITEMS, SET MISSG DATA VALUES
C
      NUMDTA = NVPDT(1)*24/IDELT(1) * (JUL2-JUL1+1)
      IPPMSG = MSNG(1)
      PPMSNG = IPPMSG
C
C                  CHECK UNITS, MAYBE GET CONVERSION FACTORS
C
      IF ( IUNFLG.EQ.0 ) GO TO 30
        DO 20 ISS=1,6
20      CALL UDUCNV(RMG(1,ISS),RMG(2,ISS),2,1,FCT(ISS),TFC(ISS),ISTAT)
30    CONTINUE
C
C                  CONVERT DATA TO REALS
C
      ISS = 0
      DO 40 L=1,NUMDTA
        IF ( ISS.EQ.6 ) ISS = 0
        ISS = ISS+1
        LD = DATA(L)
        RD = LD
        IF ( LD.EQ.IPPMSG ) GO TO 40
          RD = RD/RMG(3,ISS)
          IF ( IUNFLG.NE.0 ) THEN
             IF (RD.GT.PPMSNG) RD=(RD*FCT(ISS))+TFC(ISS)
             ENDIF
40      RDATA(L) = RD
C
C                  SET UP SHEF FORMAT LINE (EXCEPT FOR DATA VALUE)
C
        CALL UMEMST (LETBL,LIN1,20)
        CALL UMOVEX (IDOTA,1,LIN1,1,2)
        CALL UMOVEX (STA,1,LIN1,5,8)
        CALL UMOVEX (LETZ,1,LIN1,21,1)
        CALL UMOVEX (LETDH,1,LIN1,23,2)
        CALL UMOVEX (ISLS,1,LIN1,27,1)
        CALL UMOVEX (LETDU,1,LIN1,28,2)
        IF ( IUNFLG.EQ.0 ) CALL UMOVEX (LETE,1,LIN1,30,1)
        IF ( IUNFLG.NE.0 ) CALL UMOVEX (LETS,1,LIN1,30,1)
        CALL UMOVEX (ISLS,1,LIN1,31,1)
        CALL UMOVEX (LTADR,1,LIN1,32,4)
        CALL UMOVEX (ISLS,1,LIN1,44,1)
        CALL UMOVEX (LTDDR,1,LIN1,45,4)
        CALL UMOVEX (ISLS,1,LIN1,57,1)
        CALL UMOVEX (LUSDR,1,LIN1,58,4)
C
        CALL UMEMST (LETBL,LIN2,20)
        CALL UMOVEX (IDOTA,1,LIN2,1,2)
        CALL UMOVEX (STA,1,LIN2,5,8)
        CALL UMOVEX (LETZ,1,LIN2,21,1)
        CALL UMOVEX (LETDH,1,LIN2,23,2)
        CALL UMOVEX (ISLS,1,LIN2,27,1)
        CALL UMOVEX (LXCDP,1,LIN2,28,4)
        CALL UMOVEX (ISLS,1,LIN2,38,1)
        CALL UMOVEX (LRPDR,1,LIN2,39,4)
        CALL UMOVEX (ISLS,1,LIN2,50,1)
        CALL UMOVEX (LRIDR,1,LIN2,51,4)
C
C                  INITIALIZE STARTING HOUR AND HOUR INCREMENT
C
      IHRINC = IDELT(1)
      IHRSUM = NHOPDB-IHRINC
C
C      - - - - - - START LOOP THRU NUMDTA VALUES FOR OUTPUT
C
      LINC = NVPDT(1)
      DO 90 L=1,NUMDTA,LINC
C
C                  INCREMENT HOURS FROM FIRST DATE-TIME
C
        IHRSUM = IHRSUM + IHRINC
C
C                  COMPUTE THE HOUR AND DAY
C
        IHOUR  = 0
        IHOUR  = IHOUR + IHRSUM
        IDDX   = JUL1
50      IF ( IHOUR.LT.24 ) GO TO 60
          IDDX  = IDDX + 1
          IHOUR = IHOUR - 24
            GO TO 50
60      CALL MDYH1(IDDX,1,IMO,IDAY,IYR,IH,100,IDSAV,IZN)
        IYR = MOD(IYR,100)
C
C                  GET DATA VALUE, IF NOT MISSG PUT VALUE IN OUTPT LINE,
C                  IF MISSG OUTPUT IF MISSG-DATA FLAG IS ON (IMF.GT.0)
C
        RD = RDATA(L)
        LD = RD
        XD = RDATA(L+1)
        JD = XD
        ZD = RDATA(L+2)
        KD = ZD
        IF ( LD.EQ.IPPMSG.AND.JD.EQ.IPPMSG.AND.KD.EQ.IPPMSG.AND.
     *                         IMF.EQ.0 ) GO TO 70
        IPRERR=1
        IF ( LD.EQ.IPPMSG ) CALL UMOVEX (LMSS6,1,LIN1,37,6)
        IF ( LD.NE.IPPMSG ) CALL UFF2A (RD,LIN1,37,6,1,IPRERR,LP,IERR)
        IF ( JD.EQ.IPPMSG ) CALL UMOVEX (LMSS6,1,LIN1,50,6)
        IF ( JD.NE.IPPMSG ) CALL UFF2A (XD,LIN1,50,6,1,IPRERR,LP,IERR)
        IF ( KD.EQ.IPPMSG ) CALL UMOVEX (LMSS5,1,LIN1,63,5)
        IF ( KD.NE.IPPMSG ) CALL UFF2A (ZD,LIN1,63,5,1,IPRERR,LP,IERR)
C
C                  CONVERT DATE AND TIME TO CHARACTERS IN OUTPUT LINE
C
        CALL UFI2AZ (LIN1,14,2,1,IYR)
        CALL UFI2AZ (LIN1,16,2,1,IMO)
        CALL UFI2AZ (LIN1,18,2,1,IDAY)
        CALL UFI2AZ (LIN1,25,2,1,IHOUR)
C
C                  OUTPUT SHEF DATA LINE ON UNIT UNT
C
        WRITE (UNT,80) (LIN1(I),I=1,17)
C
C                  GET DATA VALUE, IF NOT MISSG PUT VALUE IN OUTPT LINE,
C                  IF MISSG OUTPUT IF MISSG-DATA FLAG IS ON (IMF.GT.0)
C
70      RD = RDATA(L+3)
        LD = RD
        XD = RDATA(L+4)
        JD = XD
        ZD = RDATA(L+5)
        KD = ZD
        IF ( LD.EQ.IPPMSG.AND.JD.EQ.IPPMSG.AND.KD.EQ.IPPMSG.AND.
     *                         IMF.EQ.0 ) GO TO 90
        IF ( LD.EQ.IPPMSG ) CALL UMOVEX (LMSS4,1,LIN2,33,4)
        IF ( LD.NE.IPPMSG ) CALL UFF2A (RD,LIN2,33,4,1,IPRERR,LP,IERR)
        IF ( JD.EQ.IPPMSG ) CALL UMOVEX (LMSS5,1,LIN2,44,5)
        IF ( JD.NE.IPPMSG ) CALL UFF2A (XD,LIN2,44,5,1,IPRERR,LP,IERR)
        IF ( KD.EQ.IPPMSG ) CALL UMOVEX (LMSS6,1,LIN2,56,6)
        IF ( KD.NE.IPPMSG ) CALL UFF2A (ZD,LIN2,56,6,1,IPRERR,LP,IERR)
C
C                  CONVERT DATE AND TIME TO CHARACTERS IN OUTPUT LINE
C
        CALL UFI2AZ (LIN2,14,2,1,IYR)
        CALL UFI2AZ (LIN2,16,2,1,IMO)
        CALL UFI2AZ (LIN2,18,2,1,IDAY)
        CALL UFI2AZ (LIN2,25,2,1,IHOUR)
C
C                  OUTPUT SHEF DATA LINE ON UNIT UNT
C
        WRITE (UNT,80) (LIN2(I),I=1,16)
80      FORMAT(17A4)
C
90    CONTINUE
C
C      - - - - - - END LOOP THRU DATA
C
C                  CALL TRACE ROUTINE FOR EXIT FROM THIS ROUTINE
C
100   CONTINUE
      CALL PVSUBE (ROUTN,ICOND)
C
C
      RETURN
C
      END
