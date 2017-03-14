C MODULE SCPCPN
C-----------------------------------------------------------------------
C
C  ROUTINE TO PUNCH STATION PCPN PARAMETERS.
C
      SUBROUTINE SCPCPN (IPNCH,UNITS,IVPCPN,STAID,IPROC,IPTIME,
     *   MDRBOX,PCPNCF,IPTWGT,IPSWGT,IPCHAR,STASID,STASWT,ISTAT)
C
      CHARACTER*4 UNITS,PMDR,PCODE
      CHARACTER*8 CHAR
      CHARACTER*80 CARD/' '/
C
      INCLUDE 'scommon/dimsta'     
      INCLUDE 'scommon/dimpcpn'      
C
      INCLUDE 'uio'
      INCLUDE 'scommon/sudbgx'
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/ppinit_punch/RCS/scpcpn.f,v $
     . $',                                                             '
     .$Id: scpcpn.f,v 1.2 1998/04/07 14:57:58 page Exp $
     . $' /
C    ===================================================================
C
C
C
      IF (ISTRCE.GT.0) THEN
         WRITE (IOSDBG,160)
         CALL SULINE (IOSDBG,1)
         ENDIF
C
C  SET DEBUG LEVEL
      LDEBUG=ISBUG('PCPN')
C
      ISTAT=0
C      
      MCHAR=LEN(CHAR)
C
C  PRINT PARAMETER ARRAY VERSION NUMBER
      IF (LDEBUG.GT.0) THEN
         WRITE (IOSDBG,180) IVPCPN
         CALL SULINE (IOSDBG,2)
         ENDIF
C
C  PUNCH PCPN STATION IDENTIFIER
      IF (IPNCH.GT.0) THEN
         NPOS=1
         CALL UTOCRD (ICDPUN,NPOS,'STAN',4,1,CARD,0,0,LNUM,IERR)
         IF (IERR.GT.0) GO TO 140
         CALL UTOCRD (ICDPUN,NPOS,STAID,8,1,CARD,3,0,
     *      LNUM,IERR)
         IF (IERR.GT.0) GO TO 140
         CALL UPNCRD (ICDPUN,CARD)
         ENDIF
C
C  PUNCH 'PCPN' STARTING IN COLUMN 1
      NPOS=1
      CALL UTOCRD (ICDPUN,NPOS,'PCPN',4,1,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
C
C  PUNCH DATA TIME INTERVAL
      CALL UINTCH (IPTIME,MCHAR,CHAR,NFILL,IERR)
      CALL UTOCRD (ICDPUN,NPOS,CHAR,MCHAR,1,CARD,1,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
C
C  PUNCH PROCESSING CODE
      PCODE='????'
      IF (IPROC.EQ.0) PCODE='NORM'
      IF (IPROC.EQ.1) PCODE='ZERO'
      IF (IPROC.EQ.2) PCODE='SYN'
      CALL UTOCRD (ICDPUN,NPOS,PCODE,4,1,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
C
C  PUNCH MDR USAGE OPTION
      PMDR='MDR'
      IF (MDRBOX.EQ.0) PMDR='NMDR'
      CALL UTOCRD (ICDPUN,NPOS,PMDR,4,1,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
C
      IF (IPTWGT.GT.0) GO TO 30
C
C  PUNCH 'D2'
      CALL UTOCRD (ICDPUN,NPOS,'D2',2,1,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
      GO TO 50
C
C  PUNCH IDENTIFIERS AND WEIGHTS OF STATIONS WITH SIGNIFICANCE WEIGHTS
30    CALL UTOCRD (ICDPUN,NPOS,'SIG(',4,0,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
      NSPACE=1
      DO 40 I=1,IPTWGT
         NCHAR=LEN(STASID(I))
         CALL UTOCRD (ICDPUN,NPOS,STASID(I),NCHAR,0,CARD,3,0,LNUM,IERR)
         IF (IERR.GT.0) GO TO 140
         CALL UTOCRD (ICDPUN,NPOS,',',1,0,CARD,0,0,LNUM,IERR)
         IF (IERR.GT.0) GO TO 140
         IF (I.EQ.IPTWGT) NSPACE=0
         NUMDEC=2
         CALL URELCH (STASWT(I),MCHAR,CHAR,NUMDEC,NFILL,IERR)
         CALL UTOCRD (ICDPUN,NPOS,CHAR,MCHAR,NSPACE,CARD,0,0,
     *      LNUM,IERR)
         IF (IERR.GT.0) GO TO 140
40       CONTINUE
      CALL UTOCRD (ICDPUN,NPOS,')',1,1,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
C
50    IF (PCPNCF(1).EQ.1.) GO TO 70
C
C  PUNCH CORRECTION FACTORS
      IF (PCPNCF(2).EQ.-999.) GO TO 60
C
C  TWO CORRECTION FACTORS
      CALL UTOCRD (ICDPUN,NPOS,'CF(',3,0,CARD,0,14,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
      NUMDEC=2
      CALL URELCH (PCPNCF(1),MCHAR,CHAR,NUMDEC,NFILL,IERR)
      CALL UTOCRD (ICDPUN,NPOS,CHAR,MCHAR,0,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
      CALL UTOCRD (ICDPUN,NPOS,',',1,0,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
      CALL URELCH (PCPNCF(2),MCHAR,CHAR,NUMDEC,NFILL,IERR)
      CALL UTOCRD (ICDPUN,NPOS,CHAR,MCHAR,0,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
      CALL UTOCRD (ICDPUN,NPOS,')',1,1,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
      GO TO 70
C
C  ONE CORRECTION FACTOR
60    CALL UTOCRD (ICDPUN,NPOS,'CF(',3,0,CARD,0,9,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
      NUMDEC=2
      CALL URELCH (PCPNCF(1),MCHAR,CHAR,NUMDEC,NFILL,IERR)
      CALL UTOCRD (ICDPUN,NPOS,CHAR,MCHAR,0,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
      CALL UTOCRD (ICDPUN,NPOS,')',1,1,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
C
C  START NEW CARD
      CALL UPNCRD (ICDPUN,CARD)
      NPOS=6
C
70    IF (IPCHAR.EQ.0) GO TO 120
C
C  START NEW CARD
      CALL UPNCRD (ICDPUN,CARD)
      NPOS=6
C
C  PUNCH PRECIPITATION CHARACTERISTICS
      CALL RPP1CH (IPCHAR,PXCHR,IERR)
      IR=IERR+1
      GO TO (80,100,110),IR
         WRITE (LP,190) IERR
         CALL SUERRS (LP,2,-1)
         GO TO 120
80    CALL UTOCRD (ICDPUN,NPOS,'CHAR(',5,0,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
      NSPACE=1
      DO 90 I=1,12
         IF (I.EQ.12) NSPACE=0
         VALUE=PXCHR(I)
         IF (UNITS.EQ.'METR') THEN
            CALL UDUCNV ('IN  ','MM  ',1,1,PXCHR(I),VALUE,IERR)
            ENDIF
         NUMDEC=2
         IF (VALUE.LT.0.1) NUMDEC=3
         CALL URELCH (VALUE,MCHAR,CHAR,NUMDEC,NFILL,IERR)
         CALL UTOCRD (ICDPUN,NPOS,CHAR,MCHAR,NSPACE,CARD,0,0,
     *      LNUM,IERR)
         IF (IERR.GT.0) GO TO 140
90       CONTINUE
      CALL UTOCRD (ICDPUN,NPOS,')',1,1,CARD,0,0,LNUM,IERR)
      IF (IERR.GT.0) GO TO 140
      GO TO 120
100   WRITE (LP,200)
      CALL SUERRS (LP,2,-1)
      ISTAT=1
      GO TO 120
110   WRITE (LP,210) IPCHAR
      CALL SUERRS (LP,2,-1)
      ISTAT=1
C
120   IF (IPSWGT.EQ.1) GO TO 130
C
C  PUNCH INDICATOR THAT STATION NOT TO BE USED FOR WEIGHTING
      CALL UTOCRD (ICDPUN,NPOS,'NWGT',4,1,CARD,0,0,LNUM,IERR)
C
130   CALL UPNCRD (ICDPUN,CARD)
      GO TO 150
C
140   ISTAT=1
C
150   IF (ISTRCE.GT.0) THEN
         WRITE (IOSDBG,220)
         CALL SULINE (IOSDBG,1)
         ENDIF
C
      RETURN
C
C- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
C
160   FORMAT (' *** ENTER SCPCPN')
180   FORMAT ('0PARAMETER ARRAY VERSION NUMBER = ',I2)
190   FORMAT ('0*** ERROR - IN SCPCPN - STATUS CODE FROM RPP1CH NOT ',
     *   'RECOGNIZED : ',I3)
200   FORMAT ('0*** ERROR - IN SCPCPN - SYSTEM ERROR ACCESSING ',
     *   'PRECIPITATION CHARACTERISTICS FILE.')
210   FORMAT ('0*** ERROR - IN SCPCPN - INVALID VALUE OF POINTER TO ',
     *   'LOCATION OF CHARACTERISTICS. IPCHAR = ',I5)
220   FORMAT(' *** EXIT SCPCPN')
C
      END
