C MEMBER UPDT19
C  (from old member FCPACK19)
C
      SUBROUTINE UPDT19(OWE,OSC,TWE,COVER,IUPWE,IUPSC,
     1  WETOL,SCTOL,SI,ADC,IVER)
C.......................................
C     THIS SUBROUTINE UPDATES WATER-EQUIVALENT AND SNOW COVER
C       BASED ON OBSERVATIONS.  AFFECTED CARRYOVER VALUES ARE
C       ADJUSTED.
C.......................................
C     SUBROUTINE WRITTEN BY....
C           ERIC ANDERSON - HRL - DECEMBER 1982
C
CVK        MODIFIED 4/00 BY V. KOREN: TWO NEW STATES ADDED
C
CEA        MODIFIED 11/05 BY E. ANDERSON TO ADD TAPREV TO SNCO19 COMMON
CEA          NO OTHER CHANGES NEEDED.
C
CEA        MODIFIED 1/06 BY E. ANDERSON TO NOT ALLOW VALUES OF OBSERVED
CEA          AREAL COVER TO GET RID OF A SNOW COVER
C.......................................
      REAL NEGHS,LIQW
      DIMENSION ADC(11)
C
C     COMMON BLOCKS
      COMMON/IONUM/IN,IPR,IPU
CVK   SNDPT & SNTMP STATES ADDED
      COMMON/SNCO19/WE,NEGHS,LIQW,TINDEX,ACCMAX,SB,SBAESC,SBWS,
CVK     1 STORGE,AEADJ,NEXLAG,EXLAG(7)
     1 STORGE,AEADJ,NEXLAG,EXLAG(7),SNDPT,SNTMP,TAPREV
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcst_snow/RCS/updt19.f,v $
     . $',                                                             '
     .$Id: updt19.f,v 1.3 2006/10/03 19:38:58 hsu Exp $
     . $' /
C    ===================================================================
C
C.......................................
C
C     UPDATE WATER-EQUIVALENT FIRST IF OBSERVED WE AVAILABLE.
      IF(OWE.LT.0.0) GO TO 250
      IF(IUPWE.EQ.0) GO TO 250
      IF(OWE.LT.1.0) OWE=0.0
C
C     UPDATE WE IF DIFFERENCE EXCEEDS TOLERANCE.
      IF(ABS(OWE-TWE).LE.(WETOL*TWE)) GO TO 250
      TWE=OWE
C
C     UPDATE CARRYOVER VALUES TO BE CONSISTENT WITH THIS CHANGE.
      IF(TWE.GT.0.0) GO TO 235
C
C     NO SNOW REMAINS AFTER UPDATE - CANNOT UPDATE AREAL SNOW
C        COVER EVEN IF OBSERVED VALUE EXISTS.
      CALL ZERO19
      COVER=0.0
      GO TO 290
C
C     ADJUST CARRYOVER VALUES BASED ON NEW WATER-EQUIVALENT.
  235 CALL ADJC19(TWE,SI,ADC,IVER)
C
C     COMPUTE AREAL EXTENT OF SNOW BASED ON NEW WE.
      CALL AESC19(WE,LIQW,ACCMAX,SB,SBAESC,SBWS,SI,ADC,AEADJ,COVER)
C
C     UPDATE AREAL EXTENT OF SNOW-COVER IF OBSERVED AESC AVAILABLE
C         AND A SNOW COVER EXISTS.
  250 IF(OSC.LT.0.0) GO TO 290
      IF(IUPSC.EQ.0) GO TO 290
      IF (TWE.LT.0.1) GO TO 290
C
C     UPDATE AESC IF DIFFERENCE EXCEEDS TOLERANCE - ALSO CHANGE AEADJ.
      IF(ABS(OSC-COVER).LE.SCTOL) GO TO 290
      IF(OSC.GT.0.05) GO TO 260
      IF(OSC.EQ.0.0) GO TO 255
      WRITE(IPR,907)
  907 FORMAT(1H0,112H**WARNING** AREAL EXTENT OF SNOW IS NOT UPDATED FOR
     1 OBSERVED AREAL EXTENT VALUES IN THE RANGE GT 0.0 TO LE 0.05.)
      CALL WARN
      GO TO 290
C
C     CAN ONLY GET RID OF SNOW BY SETTING WATER EQUIVALENT TO ZERO -
C       NOT BY SETTING AESC TO ZERO
  255 WRITE(IPR,908)
  908 FORMAT(1H0,96H**WARNING** SNOW CANNOT BE REMOVED BY SETTING COVER 
     1TO ZERO - MUST SET WATER EQUIVALENT TO ZERO.)
      CALL WARN
CEA     NO SNOW REMAINS
CEA  255 CALL ZERO19
CEA      COVER=0.0
CEA      TWE=0.0
      GO TO 290
C
C     ADJUST CARRYOVER FOR OBS. AREAL EXTENT.
  260 IF(OSC.GT.1.0) OSC=1.0
      COVER=OSC
      CALL AECO19 (COVER,TWE,SI,ADC)
C     ALL UPDATING COMPLETED.
C.......................................
 290  RETURN
      END