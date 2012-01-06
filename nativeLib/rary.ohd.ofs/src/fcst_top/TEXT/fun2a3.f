C MODULE FUN2A3
C-----------------------------------------------------------------------
C
C  MAIN ROUTINE FOR FUNCTIONS SAVEDATE AND FREEDATE.
C
      SUBROUTINE FUN2A3 (IFUNCT)
C
      INCLUDE 'common/fccgd'
      INCLUDE 'common/fccgd1'
      INCLUDE 'common/fcunit'
      INCLUDE 'common/ionum'
      INCLUDE 'common/fctime'
      INCLUDE 'common/where'
      COMMON/HDFLTS/XHCL(2),LINPTZ,YHCL(18),LCLHCL,LTZHCL,NPDHCL,NCAHCL
C
      CHARACTER*8 FUNNAM(2)/'SAVEDATE','FREEDATE'/
C
      DIMENSION CGNME(2),ICGARG(4),IDTARG(7)
      DIMENSION SEGOLD(2),OPNOLD(2)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcst_top/RCS/fun2a3.f,v $
     . $',                                                             '
     .$Id: fun2a3.f,v 1.3 2002/02/11 20:43:30 dws Exp $
     . $' /
C    ===================================================================
C
      DATA BLANK/4H    /
C
C
      IOPOLD=IOPNUM
      IOPNUM=0
      CALL UMEMOV ('FUN2A3  ',OPNAME,2)
C
      IOUNT=0
      IF (IOUNT.EQ.1) THEN
      
C  SET VALUE FOR IPR IN COMMON IONUM FROM TECHNIQUE PRINT
      CALL HPAST (8HPRINT   ,IPR,ISTAT)
      IF (ISTAT.GT.0) CALL FPHPWN (ISTAT,8HPRINT   )
      CALL FTEKCK (IPR,8HPRINT   ,6,IPR,1,9)
C
      ENDIF
C
      IF (IFUNCT.EQ.2) GO TO 10
C
C  CHECK THAT TECHNIQUES SAVECGRP AND SAVEDATE ARE ON (=1)
C  IF NOT, CANNOT PERFORM SAVEDATE FUNCTION
C
C  GET TECHNIQUE SAVECGRP (ISCGRP)
C  STORE CARRYOVER GROUP NAME ARRAY IN ICGARG (COMPRESSED)
      CALL HPASTA (8HSAVECGRP,4,ISCGRP,NWORDS,ICGARG,ISTAT)
      IF (ISTAT.GT.0) CALL FPHPWN (ISTAT,8HSAVECGRP)
      CALL FTEKCK (ISCGRP,8HSAVECGRP,0,ISCGRP,0,1)
C
C  GET TECHNIQUE SAVEDATE (ISDATE)
C  STORE DATE IN ARRAY IDTARG (1ST WORD IS JULIAN HOUR)
      CALL HPASTA (8HSAVEDATE,7,ISDATE,NWORDS,IDTARG,ISTAT)
      IF (ISTAT.GT.0) CALL FPHPWN (ISTAT,8HSAVEDATE)
      CALL FTEKCK (ISDATE,8HSAVEDATE,0,ISDATE,0,1)
C
      IF (ISCGRP.EQ.1.AND.ISDATE.EQ.1) GO TO 20
C
      WRITE (IPR,130) FUNNAM(IFUNCT)
      CALL WARN
      GO TO 120
C
C  CHECK THAT TECHNIQUES SAVECGRP AND SAVEDATE ARE ON (=1)
C  IF NOT, CANNOT PERFORM FREEDATE FUNCTION
C
C  GET TECHNIQUE FREECGRP (IFCGRP)
C  STORE CARRYOVER GROUP NAME IN ARRAY ICGARG (COMPRESSED)
10    CALL HPASTA (8HFREECGRP,4,IFCGRP,NWORDS,ICGARG,ISTAT)
      IF (ISTAT.GT.0) CALL FPHPWN (ISTAT,8HFREECGRP)
      CALL FTEKCK (IFCGRP,8HFREECGRP,0,IFCGRP,0,1)
C
C  GET TECHNIQUE FREEDATE (IFDATE)
C  STORE DATE IN ARRAY IDTARG (1ST WORD IS JULIAN HOUR)
      CALL HPASTA (8HFREEDATE,7,IFDATE,NWORDS,IDTARG,ISTAT)
      IF (ISTAT.GT.0) CALL FPHPWN (ISTAT,8HFREEDATE)
      CALL FTEKCK (IFDATE,8HFREEDATE,0,IFDATE,0,1)
C
      IF (IFCGRP.EQ.1.AND.IFDATE.EQ.1) GO TO 20
C
      WRITE (IPR,140) FUNNAM(IFUNCT)
      CALL WARN
      GO TO 120
C
C  GET CARRYOVER GROUP NAME FROM ARRAY ICGARG (CGNAME)
20    CALL HGTSTR (2,ICGARG,CGNME,IFL,ISTAT)
      IF (ISTAT.NE.0) THEN
         WRITE (IPR,150) FUNNAM(IFUNCT),ISTAT
         CALL WARN
         DO 30 I=1,2
           CGNME(I)=BLANK
30         CONTINUE
         ENDIF
C
C  COMPUTE DATE FROM JULIAN HOUR IN IDTARG (JULDA AND JHR)
      JULDA=IDTARG(1)/24+1
      JHR=IDTARG(1)-JULDA*24+24
      IF (JHR.NE.0) GO TO 40
         JULDA=JULDA-1
         JHR=24
C
C  FILL VALUES OF INPTZC, LOCAL AND NLSTZ IN COMMON FCTIME BEFORE
C  CALLING MDYH2
40    INPTZC=LINPTZ
      LOCAL=LCLHCL
      NLSTZ=LTZHCL
      CALL MDYH2 (JULDA,JHR,MONTH,IDAY,IYEAR,IHOUR,NXX,NDXX,INPTZC)
C
C  GET INFORMATION INTO COMMON FCCGD1 FOR CARRYOVER GROUP CGNME
      CALL UREADT (KFCGD,1,NSLOTS,IERR)
      IF (NCG.EQ.0) THEN
         WRITE (IPR,160) FUNNAM(IFUNCT),CGNME
         CALL WARN
         GO TO 120
         ENDIF
C
      DO 50 ICG=1,NCG
         IF (CGIDS(1,ICG).NE.CGNME(1)) GO TO 50
         IF (CGIDS(2,ICG).NE.CGNME(2)) GO TO 50
         GO TO 60
50       CONTINUE
C
      WRITE (IPR,170) FUNNAM(IFUNCT),CGNME
      CALL WARN
      GO TO 120
C
60    CALL UREADT (KFCGD,ICOREC(ICG),CGIDC,IERR)
C
C  HAVE CARRYOVER GROUP REQUESTED IN COMMON BLOCK FCCGD1
C  CHECK TO SEE IF DATE REQUESTED MATCHES DATE FOR THIS CARRYOVER GROUP
      DO 70 ISLOT=1,NSLOTS
         IF (ICODAY(ISLOT).NE.JULDA) GO TO 70
         IF (ICOTIM(ISLOT).NE.JHR) GO TO 70
         GO TO 80
70       CONTINUE
C
      WRITE (IPR,180) FUNNAM(IFUNCT),CGNME,MONTH,IDAY,IYEAR,
     *   IHOUR,INPTZC
      CALL WARN
      GO TO 120
C
80    ICURPC=IPC(ISLOT)
C
C  ICURPC = 0 -- VOLATILE AND INCOMPLETE
C         = 1 -- VOLATILE AND COMPLETE
C         = 2 -- PROTECTED AND INCOMPLETE
C         = 3 -- PROTECTED AND COMPLETE
C
      IF (IFUNCT.EQ.2) GO TO 100
C
C  PROCESS SAVEDATE COMMAND
      IF (ICURPC.LE.1) GO TO 90
         WRITE (IPR,190) CGNME,MONTH,IDAY,IYEAR,IHOUR,INPTZC
         CALL WARN
         GO TO 120
90    IPC(ISLOT)=IPC(ISLOT)+2
      CALL UWRITT (KFCGD,ICOREC(ICG),CGIDC,IERR)
      WRITE (IPR,200) 'PROTECTED',
     *   CGNME,MONTH,IDAY,IYEAR,IHOUR,INPTZC
      GO TO 120
C
C  PROCESS FREEDATE COMMAND
100   IF (ICURPC.GE.2) GO TO 110
         WRITE (IPR,210) CGNME,MONTH,IDAY,IYEAR,IHOUR,INPTZC
         CALL WARN
         GO TO 120
110   IPC(ISLOT)=IPC(ISLOT)-2
      CALL UWRITT (KFCGD,ICOREC(ICG),CGIDC,IERR)
      WRITE (IPR,200) 'FREED',
     *   CGNME,MONTH,IDAY,IYEAR,IHOUR,INPTZC
C
120   IOPNUM=IOPOLD
      CALL UMEMOV (SEGOLD,ISEG,2)
      CALL UMEMOV (OPNOLD,OPNAME,2)
C
      RETURN
C
C- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C
130   FORMAT ('0**WARNING** IN ',A,' - BOTH TECHNIQUES ',
     *   'SAVECGRP AND SAVEDATE MUST BE SET.')
140   FORMAT ('0**WARNING** IN ',A,' - BOTH TECHNIQUES ',
     *   'FREECGRP AND FREEDATE MUST BE SET.')
150   FORMAT ('0**WARNING** IN ',A,' - STATUS OF ',I3,
     *   'RETURNED FROM ROUTINE HGTSTR.')
160   FORMAT ('0**WARNING** IN ',A,' - REQUEST TO RUN CARRYOVER ',
     *   'GROUP ',2A4,' BUT NO CARRYOVER GROUPS ARE DEFINED.')
170   FORMAT ('0**WARNING** IN ',A,' - CARRYOVER GROUP ',2A4,
     *   ' NOT FOUND.')
180   FORMAT ('0**WARNING** IN ',A,' - NO MATCH FOUND IN ',
     *   'CARRYOVER GROUP ',2A4,' FOR ',
     *   'DATE ',I2.2,'/',I2.2,'/',I4,'-',I2.2,A4,'.')
190   FORMAT ('0**WARNING** SAVEDATE SPECIFIED ',
     *   'FOR A DATE ALREADY PROTECTED. ',
     *   'THE CARRYOVER GROUP IS ',2A4,' AND ',
     *   'DATE ',I2.2,'/',I2.2,'/',I4,'-',I2.2,A4,'.')
200   FORMAT ('0**NOTE** CARRYOVER SUCCESSFULLY ',A,
     *   ' FOR CARRYOVER GROUP ',2A4,' AND ',
     *   'DATE ',I2.2,'/',I2.2,'/',I4,'-',I2.2,A4,'.')
210   FORMAT ('0**WARNING** FREEDATE SPECIFIED ',
     *   'FOR A DATE ALREADY FREED. ',
     *   'THE CARRYOVER GROUP IS ',2A4,' AND ',
     *   'DATE ',I2.2,'/',I2.2,'/',I4,'-',I2.2,A4,'.')
C
      END
