C MEMBER CHAN21
C  (from old member FCPIN21)
C
C                             LAST UPDATE: 02/14/94.14:37:49 BY $WC30JL
C
C
C @PROCESS LVL(77)
      SUBROUTINE CHAN21(NB,AS,BS,HS,NCS,ASS,BSS,HSS,NCSS1,NCSSS,NCSS,
     1 JN,JNK,K7,K9)
C
C           THE FUNCTION OF THIS SUBROUTINE IS TO FILL THE ARRAY SPACE
C           FOR THE CROSS SECTIONAL AREA BELOW EACH TOPWIDTH (BOTH
C           ACTIVE AND INACTIVE.
C
C           THIS SUBROUTINE WAS WRITTEN BY:
C           JANICE LEWIS      HRL   NOVEMBER,1982     VERSION NO. 1
C
      COMMON/FDBUG/IODBUG,ITRACE,IDBALL,NDEBUG,IDEBUG(20)
      COMMON/IONUM/IN,IPR,IPU
C
      DIMENSION AS(K7,1),BS(K7,1),HS(K7,1),NCSS1(1),NB(1)
      DIMENSION ASS(K9,1),BSS(K9,1),HSS(K9,1),NCSSS(1),SNAME(2)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcinit_dwoper/RCS/chan21.f,v $
     . $',                                                             '
     .$Id: chan21.f,v 1.2 1996/01/17 19:55:46 page Exp $
     . $' /
C    ===================================================================
C
C
      DATA SNAME/4HCHAN,4H21  /
C
C
      CALL FPRBUG(SNAME,1,21,IBUG)
C
C        COMPUTE THE TOTAL ACTIVE AREA BELOW EACH ACTIVE TOP WIDTH
C
      DO 216 J=1,JN
      N=NB(J)
      LIJ=LCAT21(1,J,NB)-1
      IF(JNK.GE.1.AND.IBUG.EQ.1) WRITE(IODBUG,8030) J
 8030 FORMAT(1H0,30X,45HACTIVE AREA BELOW EACH TOP WIDTH ON RIVER NO.,I4
     1)
      DO 215 I=1,N
      ICNT=0
      DO 214 K=2,NCS
      KL=K-1
      IJ=I+LIJ
      IF(HS(K,IJ).GT.HS(KL,IJ)) GO TO 214
      ICNT=ICNT+1
      IF(ICNT.GT.1) GO TO 20
      WRITE(IPR,10) I,J,KL,HS(KL,IJ)
   10 FORMAT(1H0,10X,78H**WARNING** THE TABLE OF TOPWIDTHS VS. ELEVATION
     1S IS NOT COMPLETE FOR SECTION ,I3,13H ON RIVER NO.,I2,1H./1H ,22X,
     232HTHE LAST GOOD VALUE IS AT LEVEL ,I3,26H WHICH HAS AN ELEVATION
     3OF,F10.2,21H FEET.  AT ELEVATIONS /1H ,22X,79HHIGHER THAN THIS, TH
     4E MODEL WILL LINEARLY EXTRAPOLATE FROM THE LAST TWO POINTS.)
      CALL WARN
      DIF=0.5/(NCS-KL)
   20 Z=(BS(KL,IJ)-BS(KL-1,IJ))/(HS(KL,IJ)-HS(KL-1,IJ))
      HS(K,IJ)=HS(KL,IJ)+DIF
      IF(BS(K,IJ).LT.0.01) BS(K,IJ)=BS(KL,IJ)+2*Z*DIF
  214 AS(K,IJ)=AS(KL,IJ)+0.5*(BS(K,IJ)+BS(KL,IJ))*
     1(HS(K,IJ)-HS(KL,IJ))
      IF(JNK.GE.1.AND.IBUG.EQ.1)
     1 WRITE(IODBUG,7000) (AS(K,IJ),K=1,NCS)
 7000 FORMAT(1H ,10X,10F12.2)
  215 CONTINUE
      IF(JNK.GE.1.AND.IBUG.EQ.1) WRITE(IODBUG,7010)
 7010 FORMAT(/)
  216 CONTINUE
      IF(NCSS) 219,219,217
C
C        COMPUTE THE TOTAL INACTIVE AREA BELOW EACH INACTIVE TOP WIDTH
C
  217 DO 220 J=1,JN
      N=NB(J)
      NCSJ=NCSS1(J)
      IF(NCSJ.EQ.0) GO TO 220
      LIJ=LCAT21(1,J,NCSS1)-1
      IF(JNK.GE.1.AND.IBUG.EQ.1) WRITE(IODBUG,8040) J
 8040 FORMAT(1H0,30X,47HINACTIVE AREA BELOW EACH TOPWIDTH FOR RIVER NO.,
     1I4)
      DO 218 L=1,NCSJ
C
C        FIND THE CROSS SECTION WITH INACTIVE STORAGE
C
      DO 230 I=1,N
      ICNT=0
      IF(I-NCSSS(L+LIJ)) 230,2170,230
  230 CONTINUE
C
      GO TO 218
C        COMPUTE THE INACTIVE AREA
C
 2170 IF(JNK.GE.1.AND.IBUG.EQ.1) WRITE(IODBUG,8060) I
 8060 FORMAT(1H0,10X,11HSECTION NO.,I4)
      DO 2118 K=2,NCSS
      KL=K-1
      LJ=L+LIJ
      IF(HSS(K,LJ).GT.HSS(KL,LJ)) GO TO 250
      ICNT=ICNT+1
      IF(ICNT.GT.1) GO TO 240
      WRITE(IPR,235) I,J,KL,HSS(KL,LJ)
  235 FORMAT(1H0,10X,89H**WARNING** THE TABLE OF TOPWIDTHS VS. ELEVATION
     1S (INACTIVE) IS NOT COMPLETE FOR SECTION ,I3,13H ON RIVER NO.,I2,
     21H./1H ,22X,32HTHE LAST GOOD VALUE IS AT LEVEL ,I3,26H WHICH HAS A
     3N ELEVATION OF,F10.2,6H FEET./1H ,22X,93HAT ELEVATIONS HIGHER THAN
     4THIS, THE MODEL WILL LINEARLY EXTRAPOLATE FROM THE LAST TWO POINTS
     5.  )
      CALL WARN
      DIF=0.5/(NCSS-KL)
  240 Z=(BSS(KL,LJ)-BSS(KL-1,LJ))/(HSS(KL,LJ)-HSS(KL-1,LJ))
      HSS(K,LJ)=HSS(KL,LJ)+DIF
      IF(BSS(K,LJ).LT.0.01) BSS(K,LJ)=BSS(KL,LJ)+2*Z*DIF
      IF(BSS(K,LJ).LT.0.) BSS(K,LJ)=0.
  250 ASS(K,LJ)=ASS(KL,LJ)+0.5*(BSS(K,LJ)+BSS(KL,LJ))*
     1(HSS(K,LJ)-HSS(KL,LJ))
 2118 CONTINUE
      IF(JNK.GE.1.AND.IBUG.EQ.1) WRITE(IODBUG,7000)
     1 (ASS(K,LJ),K=1,NCSS)
  218 CONTINUE
  220 CONTINUE
  219 CONTINUE
C
      IF(ITRACE.EQ.1) WRITE(IODBUG,9000) SNAME
 9000 FORMAT(1H0,2H**,1X,2A4,8H EXITED.)
C
      RETURN
      END
