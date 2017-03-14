C MODULE HPASMD
C-----------------------------------------------------------------------
C
      SUBROUTINE HPASMD (MAXMOD,MODCRD,NXI,SEGID,FGID)
C
C  THIS ROUTINE PUTS MOD CARDS IN MODS ARRAY IF AN ID MATCH IS FOUND.
C
      CHARACTER*4 STRNG
      CHARACTER*8 RTNNAM,RTNOLD
      CHARACTER*8 SEGID,FGID,TFGID
      CHARACTER*8 ID,IDX
      CHARACTER*8 OTHRMD(3)/'RRSMSNG ','FMAP24  ','FMAP6   '/
      CHARACTER*80 MODCRD(MAXMOD),TMPMOD
C
      INCLUDE 'uiox'
      INCLUDE 'udebug'
      INCLUDE 'ufreei'
      INCLUDE 'udatas'
      INCLUDE 'common/fcrunc'
      INCLUDE 'common/fcsegn'
      INCLUDE 'common/fcsgnn'
      INCLUDE 'common/fcunit'
      INCLUDE 'hclcommon/hseg1'
      INCLUDE 'hclcommon/hunits'
      COMMON /MFG/ ISFG,NDTS
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/db_hclrw/RCS/hpasmd.f,v $
     . $',                                                             '
     .$Id: hpasmd.f,v 1.4 2004/12/06 21:08:42 dsa Exp $
     . $' /
C    ===================================================================
C
C   
      RTNNAM='HPASMD'
C 
      IF (IHCLTR.GT.0) WRITE (LP,*) 'ENTER ',RTNNAM
C
      IOPNUM=0
      CALL FSTWHR (RTNNAM,IOPNUM,RTNOLD,IOLDOP)
C
      IF (IHCLDB.GT.0) WRITE (IOGDB,10) ITYPRN,RUNID,IFGID,ICGID
10    FORMAT (' ITYPRN=',I2,' RUNID=',2A4,' IFGID=',2A4,' ICGID=',2A4)
C
      IVALCMD=0
      NXISAV=0
      NXISVC=NXI
      NXIIN=NXI
      NENTER=0
      IRANGE=0
      ICMND=0
      IFGIND=0
      ICONT=0
      NUMCDS=0
C
C  SET OPTION TO NOT CHECK IF SEGMENT ON THE MOD CARD IS IN 
C  ONE CGROUP OR FGROUP BUT THE RUN IS FOR ANOTHER
      ICKSEG=0
C
C  INCREMENT POINTER TO THE NEXT OPTION RECORD
      CALL HINCNX
      IF (ISTAT.NE.0) GO TO 460
      CALL HINCNX
      IF (ISTAT.NE.0) GO TO 460
      NUMCDS=IOPTRC(NXOPT)
C
      IF (IHCLDB.GT.0) WRITE (IOGDB,20) NXIIN,NUMCDS,MAXMOD,SEGID,FGID
20    FORMAT (' IN HPASMD - NXIIN=',I3,' NUMCDS=',I3,
     * ' MAXMOD=',I4,' SEGID=',A,' FGID=',A)
C
      IF (NUMCDS.EQ.0) GO TO 460
      NCDS=0
C
C  PROCESS EACH CARD
30    IF (NCDS.EQ.NUMCDS) GO TO 410
C
C  GET MOD CARD
      CALL HUPMOD (TMPMOD,NXI,NCDS,IRANGE,IRECSV,IOPSAV)
      IF (ISTAT.NE.0) GO TO 440
C
C  CHECK IF A COMMAND
      CALL UMEMOV (IBUF(IFSTRT(1)),STRNG,1)
      IF (STRNG.NE.'.') GO TO 150
C
C  CHECK IF VALID MOD COMMAND - THE FIELD STARTS WITH A DOT
40    LENCMD=IFSTOP(1)-IFSTRT(1)+1
      IF (MIFCMD(TMPMOD,LENCMD,1).GT.0) GO TO 90
C
C  CHECK IF THIS IS AN RRS OR FMAP MOD
      LENCMD=LENCMD-1
      IF (LENCMD.LT.1.OR.LENCMD.GT.8) GO TO 60
      ID=' '
      IST=IFSTRT(1)+1
      CALL UPACK1 (IBUF(IST),ID,LENCMD)
      DO 50 I=1,3
         IF (ID.EQ.OTHRMD(I)) THEN
            IVALCMD=1
            GO TO 80
            ENDIF
50       CONTINUE
C
C  CHECK IF FIRST SEGMENT BEING RUN
60    IF (ISEGEX.EQ.1) THEN
         WRITE (LP,70) TMPMOD
70    FORMAT ('0**WARNING** AN INVALID MOD COMMAND HAS BEEN ',
     *    'FOUND ON THE FOLLOWING CARD:' /
     * 13X,A /
     * 13X,'THIS COMMAND CARD AND ANY MOD CARDS UP TO THE NEXT MOD ',
     *     'COMMAND WILL BE IGNORED.')
         CALL WARN
         ENDIF
C
80    NXI=NXI-1
      IF (NCDS.EQ.NUMCDS) GO TO 410
      CALL HUPMOD (TMPMOD,NXI,NCDS,IRANGE,IRECSV,IOPSAV)
      IF (ISTAT.NE.0) GO TO 440
      CALL UMEMOV (IBUF(IFSTRT(1)),STRNG,1)
      IF (STRNG.NE.'.') GO TO 80
      GO TO 40
C
C  CHECK IF ANY ENTERED OR IS THIS FIRST TIME
C  IF COMMAND CARD - MUST BE VALID MOD COMMAND TO GET HERE
C
90    IVALCMD=1
      IF (NENTER.GT.0) GO TO 100
      IF (NXISAV.EQ.0) GO TO 100
C
C  NO CARDS - RESET TO ELIMINATE COMMAND
      NXI=NXISVC
C
C  IT IS A COMMAND - CHECK FOR FGROUP
100   NXISVC=NXI
      NENTER=0
      IFGIND=0
      ICMND=1
      IF (NFIELD.LT.2) GO TO 220
      DO 110 IFLD=2,NFIELD
         N=IFSTOP(IFLD)-IFSTRT(IFLD)+1
         IF (N.GT.LEN(ID)) N=LEN(ID)
         ID=' '
         CALL UPACK1 (IBUF(IFSTRT(IFLD)),ID,N)
         IF (ID.EQ.'FG'.OR.ID.EQ.'FGROUP') GO TO 120
110      CONTINUE
      GO TO 220
C
C  CHECK IF FGROUP LEGAL FOR THIS COMMAND
120   IF (ISFG.EQ.1) GO TO 140
C  FGROUP ENTERED BUT NOT LEGAL FOR THIS COMMAND
         IF (ISEGEX.GT.1) GO TO 80
         WRITE (LP,130) TMPMOD
130   FORMAT ('0**WARNING** THE FIELD FG OR FGROUP HAS BEEN ',
     *     'DECODED ON THE FOLLOWING CARD:' /
     * 13X,A /
     * 13X,'FORECAST GROUP IDS ARE NOT ALLOWED FOR THIS MOD COMMAND.' /
     * 13X,'THIS COMMAND CARD AND ANY MOD CARDS UP TO THE NEXT MOD ',
     *     'COMMAND WILL BE IGNORED.')
         CALL WARN
         GO TO 80
C
140   IFGIND=1
      JFLD=IFLD+1
      GO TO 230
C
C  NOT A COMMAND
150   IF (IVALCMD.EQ.1) GO TO 170
C
C  HAVE FOUND A SUBSEQUENT CARD BEFORE ANY VALID COMMAND CARDS
      IF (ISEGEX.GT.1) GO TO 80
         WRITE (LP,160) TMPMOD
160   FORMAT ('0**WARNING** THE FOLLOWING MOD CARD WAS ',
     *     'ENCOUNTERED BEFORE ANY VALID MOD COMMAND CARDS:' /
     * 13X,A /
     * 13X,'THIS CARD AND ANY MOD CARDS UP TO THE NEXT MOD ',
     *     'COMMAND WILL BE IGNORED.')
         CALL WARN
         GO TO 80
C
170   ICMND=0
C
C  CHECK FOR A DASH INDICATING A RANGE OF IDENTIFIERS
      CALL USRDSH (IBUF,IFSTRT(1),IFSTOP(1),IX)
      IF (IX.GE.IFSTOP(1)) GO TO 180
C
C  RANGE FOUND - CHECK IF STARTING AND ENDING IDS MATCH
      N=IX-IFSTRT(1)
      IF (N.GT.LEN(ID)) N=LEN(ID)
      ID=' '
      CALL UPACK1 (IBUF(IFSTRT(1)),ID,N)
      N=IFSTOP(1)-IX
      IF (N.GT.LEN(IDX)) N=LEN(IDX)
      IDX=' '
      CALL UPACK1 (IBUF(IX+1),IDX,N)
      IF (ID.NE.IDX) GO TO 350
C
C  IDS MATCH - TREAT AS A SINGLE ID AND IGNORE PART AFTER DASH
      IFSTOP(1)=IX-1
C
C  NOT A RANGE - CHECK SINGLE ID
180   N=IFSTOP(1)-IFSTRT(1)+1
      IF (N.GT.LEN(ID)) N=LEN(ID)
      ID=' '
      CALL UPACK1 (IBUF(IFSTRT(1)),ID,N)
      IF (IFGIND.EQ.0) THEN
         IF (ISEGEX.EQ.1) THEN
C        CHECK IF SEGMENT EXISTS
            CALL FLOCSG (ID,ISEGR)
            IF (ISEGR.EQ.0) THEN
               WRITE (LP,190) 'SEGMENT',ID,TMPMOD
190   FORMAT ('0**WARNING** ',A,' ',A,' SPECIFIED ON THE FOLLOWING ',
     *     'MOD CARD DOES NOT EXIST:' /
     * 11X,A)
               CALL WARN
               ELSE
                  IF (ICKSEG.EQ.1) THEN
C                 READ FILE SEGMENT STATUS
                     CALL UREADT (KFSGST,ISEGR,JDSEGN,IERR)
C                 CHECK IF CARRYOVER GROUP RUN
                     IF (ITYPRN.EQ.1) THEN
                        CALL UCMPAR (ICGID,JCGID,2,IMATCH)
                        IF (IMATCH.EQ.1) THEN
                           WRITE (LP,200) ID,'CARRYOVER GROUP',JCGID,
     *                       'CARRYOVER GROUP',ICGID,TMPMOD
200   FORMAT ('0**WARNING** SEGMENT ',A,' SPECIFIED ON THE FOLLOWING ',
     *     'MOD CARD IS IN ',A,' ',2A4,' BUT THE RUN IS FOR ' /
     * 13X,A,' ',2A4,':' /
     * 13X,A)
                           CALL WARN
                           ENDIF
                        ENDIF
C                 CHECK IF FORECAST GROUP RUN
                     IF (ITYPRN.EQ.2) THEN
                        CALL UCMPAR (IFGID,JFGID,2,IMATCH)
                        IF (IMATCH.EQ.1) THEN
                           WRITE (LP,200) ID,'FORECAST GROUP',JFGID,
     *                        'FORECAST GROUP',IFGID, TMPMOD
                           CALL WARN
                           ENDIF
                        ENDIF
                     ENDIF
               ENDIF
            ENDIF
         IF (ID.EQ.SEGID) GO TO 220
         GO TO 320
         ENDIF
      IF (ISEGEX.EQ.1) THEN
C     CHECK IF FORECAST GROUP EXISTS
         CALL FGGET (ID,IFGREC,IFOUND)
         IF (IFOUND.EQ.0) THEN
            WRITE (LP,190) 'FORECAST GROUP',ID,TMPMOD
            CALL WARN
            ELSE
C           CHECK IF FORECAST GROUP RUN
               IF (ITYPRN.EQ.2) THEN
                  CALL UMEMOV (IFGID,TFGID,2)
                  IF (ID.NE.TFGID) THEN
                     WRITE (LP,210) ID,TFGID,TMPMOD
210   FORMAT ('0**WARNING** FORECAST GROUP SPECIFIED ON THE FOLLOWING ',
     *     'MOD CARD (',A,') IS NOT THE SAME AS THE FORECAST GROUP ',
     *     'BEING RUN (',A,'):' /
     * 11X,A)
                     CALL WARN
                     ENDIF
                  ENDIF
            ENDIF
         ENDIF
      IF (ID.EQ.FGID) GO TO 220
      GO TO 320
C
C  MATCH FOUND
220   JFLD=2
C
C  IF MODCRD IS FULL DO NOT STORE THIS MOD CARD IMAGE - RESET NXI
C  AND LEAVE
230   IF (NXI.LE.MAXMOD) GO TO 260
      IF (ICMND.EQ.1) GO TO 280
      WRITE (LP,240) MAXMOD,SEGID,NXISVC
240   FORMAT ('0**WARNING** IN HPASMD - ',
     *     'MORE THAN ',I4,' MOD CARDS ENTERED FOR SEGMENT ',A,'.' /
     * 13X,'THE MOD COMMAND ON MOD CARD ',I4,' FOR THIS SEGMENT AND ',
     *     'THE FOLLOWING MOD CARDS WILL BE IGNORED:')
      CALL WARN
C
C  SKIP REST OF MOD CARDS IF NECESSARY
250   IF (NCDS.EQ.NUMCDS) GO TO 420
      CALL HUPMOD (TMPMOD,NXI,NCDS,IRANGE,IRECSV,IOPSAV)
      GO TO 250
C
260   IF (IHCLDB.GT.0) WRITE (IOGDB,270) TMPMOD,NXI
270   FORMAT (' ABOUT TO MOVE MOD CARD IMAGE (',A,')',
     * ' INTO MODCRD ROW ',I3)
C
C  MOVE INTO MODCRD ARRAY
      MODCRD(NXI)=TMPMOD
      GO TO 300
C
280   IF (IHCLDB.GT.0) WRITE (IOGDB,290) TMPMOD
290   FORMAT (' MODCRD ARRAY IS FULL SO NOT LOADING THE ',
     * 'FOLLOWING MOD COMMAND : ',A)
300   IF (ICMND.EQ.0) NENTER=NENTER+1
C
C  CHECK IF THERE IS CONTINUATION
      CALL HFNDCM (JFLD,ICONT)
      IF (ICONT.EQ.0) GO TO 310
C
C  READ NEXT CARD AND PASS IT BECAUSE OF CONTINUATION
      CALL HUPMOD (TMPMOD,NXI,NCDS,IRANGE,IRETMP,IOPTMP)
      IF (ISTAT.NE.0) GO TO 330
      JFLD=1
      GO TO 230
C
C  RESET SAVE VALUE TO THIS ONE
310   NXISAV=NXI
      GO TO 30
C
C  DO NOT WANT THIS CARD
320   CALL HFNDCM (1,ICONT)
      IF (ICONT.NE.0) THEN
         CALL HUPMOD (TMPMOD,NXI,NCDS,IRANGE,IRETMP,IOPTMP)
         GO TO 320
         ENDIF
C
C  RESET NXI TO BE BEGINNING BECAUSE DIDNT WANT ANY OF THESE CARDS
330   NXI=NXISAV
      IF (IHCLDB.GT.0) WRITE (IOGDB,340) NXI
340   FORMAT (' IN HPASMD - NO MATCH - NXI RESET TO ',I3)
      GO TO 30
C
C  RANGE IS SPECIFIED - CHECK IF RANGE INDICATOR IS SET
350   IF (SEGID.EQ.'?') THEN
         WRITE (IOGDB,*) 'SEGID=',SEGID,' IRANGE=',IRANGE
         WRITE (IOGDB,*) 'TMPMOD=',TMPMOD
         ENDIF
      IF (IRANGE.EQ.1) GO TO 390
      NFLD=1
      N=IX-IFSTRT(NFLD)
      IF (N.GT.LEN(ID)) N=LEN(ID)
      ID=' '
      CALL UPACK1 (IBUF(IFSTRT(NFLD)),ID,N)
      IF (IFGIND.EQ.0) THEN
         IF (ISEGEX.EQ.1) THEN
C        CHECK IF FIRST SEGMENT EXISTS
            CALL FLOCSG (ID,ISEGR)
            IF (ISEGR.EQ.0) THEN
               WRITE (LP,190) 'SEGMENT',ID,TMPMOD
               CALL WARN
               ELSE
                  IF (ICKSEG.EQ.1) THEN
C                 READ FILE SEGMENT STATUS
                     CALL UREADT (KFSGST,ISEGR,JDSEGN,IERR)
C                 CHECK IF CARRYOVER GROUP RUN
                     IF (ITYPRN.EQ.1) THEN
                        CALL UCMPAR (ICGID,JCGID,2,IMATCH)
                        IF (IMATCH.EQ.1) THEN
                           WRITE (LP,200) ID,'CARRYOVER GROUP',JCGID,
     *                        'CARRYOVER GROUP',ICGID,TMPMOD
                           CALL WARN
                           ENDIF
                        ENDIF
C                 CHECK IF FORECAST GROUP RUN
                     IF (ITYPRN.EQ.2) THEN
                        CALL UCMPAR (IFGID,JFGID,2,IMATCH)
                        IF (IMATCH.EQ.1) THEN
                           WRITE (LP,200) ID,'FORECAST GROUP',JFGID,
     *                        'FORECAST GROUP',IFGID, TMPMOD
                           CALL WARN
                           ENDIF
                        ENDIF
                     ENDIF
               ENDIF
C        CHECK IF SECOND SEGMENT EXISTS
            CALL FLOCSG (IDX,ISEGR)
            IF (ISEGR.EQ.0) THEN
               WRITE (LP,190) 'SEGMENT',IDX,TMPMOD
               CALL WARN
               ELSE
                  IF (ICKSEG.EQ.1) THEN
C                 READ FILE SEGMENT STATUS
                     CALL UREADT (KFSGST,ISEGR,JDSEGN,IERR)
C                 CHECK IF CARRYOVER GROUP RUN
                     IF (ITYPRN.EQ.1) THEN
                        CALL UCMPAR (ICGID,JCGID,2,IMATCH)
                        IF (IMATCH.EQ.1) THEN
                           WRITE (LP,200) IDX,'CARRYOVER GROUP',JCGID,
     *                        'CARRYOVER GROUP',ICGID,TMPMOD
                           CALL WARN
                           ENDIF
                        ENDIF
C                 CHECK IF FORECAST GROUP RUN
                     IF (ITYPRN.EQ.2) THEN
                        CALL UCMPAR (IFGID,JFGID,2,IMATCH)
                        IF (IMATCH.EQ.1) THEN
                           WRITE (LP,200) IDX,'FORECAST GROUP',JFGID,
     *                        'FORECAST GROUP',IFGID, TMPMOD
                           CALL WARN
                           ENDIF
                        ENDIF
                     ENDIF
               ENDIF
            ENDIF
         IF (ID.EQ.SEGID) GO TO 360
         GO TO 320
         ENDIF
      IF (ISEGEX.EQ.1) THEN
C     CHECK IF FORECAST GROUP EXISTS
         CALL FGGET (ID,IFGREC,IFOUND)
         IF (IFOUND.EQ.0) THEN
            WRITE (LP,190) 'FORECAST GROUP',ID,TMPMOD
            CALL WARN
            ELSE
C           CHECK IF FORECAST GROUP RUN
               IF (ITYPRN.EQ.2) THEN
                  CALL UMEMOV (IFGID,TFGID,2)
                  IF (ID.NE.TFGID) THEN
                     WRITE (LP,210) ID,TFGID,TMPMOD
                     CALL WARN
                     ENDIF
                  ENDIF
            ENDIF
         ENDIF
      IF (ID.EQ.FGID) GO TO 360
      GO TO 320
C
C  MATCHED
360   IF (NUMRED.EQ.IRECSV) GO TO 370
C
C  READ RECORD
      CALL UREADT (KHDFLT,IRECSV,IOPTRC,ISTAT)
      IF (ISTAT.NE.0) GO TO 440
C
C  SET RANGE INDICATOR BY SETTING LENGTH AS NEGATIVE
370   IF (IHCLDB.GT.0) THEN
         IF (IOPTRC(IOPSAV).GT.0) WRITE (IOGDB,380) SEGID,'FIRST'
         IF (IOPTRC(IOPSAV).LT.0) WRITE (IOGDB,380) SEGID,'LAST'
380   FORMAT (' SEGMENT ID ',A,' MATCHES ',A,' ID IN RANGE')
         ENDIF
      IOPTRC(IOPSAV)=-IOPTRC(IOPSAV)
      CALL UWRITT (KHDFLT,IRECSV,IOPTRC,ISTAT)
      IF (IHCLDB.GT.0) WRITE (IOGDB,*) 'SEGID=',SEGID,
     *   ' IRECSV=',IRECSV,' IOPSAV=',IOPSAV,
     *   ' IOPTRC(IOPSAV)=',IOPTRC(IOPSAV)
C
      IF (IRECSV.EQ.NUMRED) GO TO 220
C
C  REREAD CURRENT RECORD
      CALL UREADT (KHDFLT,NUMRED,IOPTRC,ISTAT)
      IF (ISTAT.NE.0) GO TO 440
      GO TO 220
C
C  RANGE INDICATOR IS SET - CHECK END OF RANGE
390   IF (SEGID.EQ.'?') THEN
         WRITE (IOGDB,*) 'SEGID=',SEGID
         ENDIF
      NFLD=1
      N=IFSTOP(NFLD)-IX
      IF (N.GT.LEN(ID)) N=LEN(ID)
      ID=' '
      CALL UPACK1 (IBUF(IX+1),ID,N)
      IF (IFGIND.EQ.0) THEN
         IF (ID.EQ.SEGID) GO TO 400
         GO TO 220
         ENDIF
      IF (ISEGEX.EQ.1) THEN
C     CHECK IF FORECAST GROUP EXISTS
         CALL FGGET (ID,IFGREC,IFOUND)
         IF (IFOUND.EQ.0) THEN
            WRITE (LP,190) 'FORECAST GROUP',ID,TMPMOD
            CALL WARN
            ELSE
C           CHECK IF FORECAST GROUP RUN
               IF (ITYPRN.EQ.2) THEN
                  CALL UMEMOV (IFGID,TFGID,2)
                  IF (ID.NE.TFGID) THEN
                     WRITE (LP,210) ID,TFGID,TMPMOD
                     CALL WARN
                     ENDIF
                  ENDIF
            ENDIF
         ENDIF
      IF (ID.EQ.FGID) GO TO 400
      GO TO 220
C
C  MATCHES END OF RANGE
400   IF (SEGID.EQ.'?') THEN
         WRITE (IOGDB,*) 'SEGID=',SEGID
         ENDIF
      GO TO 360
C
C  NO MORE CARDS
410   IF (NENTER.GT.0) GO TO 460
C
C  RESET
420   NXI=NXISVC-1
C
C  CHECK NXI AGAINST NXIIN INSTEAD OF AGAINST ZERO - SOLVES A PROBLEM
C  WHEN MORE THAN ONE SETOF MOD-ENDMOD CARDS ARE ENTERED IN A RUN
      IF (NXI.LT.NXIIN) NXI=NXIIN
      IF (IHCLDB.GT.0) WRITE (IOGDB,430) NXI
430   FORMAT (' IN HPASMD - RESET NXI TO ',I4)
      GO TO 460
C
440   WRITE (LP,450) ISTAT
450   FORMAT ('0**ERROR** IN HPASMD - READING FROM OPTIONS. ISTAT=',I6)
      CALL ERROR
C
460   IF (IHCLDB.GT.0) WRITE (IOGDB,470) NXI,ISTAT
470   FORMAT (' IN HPASMD - NXI=',I4,' ISTAT=',I2)
C
      CALL FSTWHR (RTNOLD,IOLDOP,RTNOLD,IOLDOP)
C
      IF (IHCLTR.GT.1) WRITE (LP,*) 'EXIT ',RTNNAM
C
      RETURN
C
      END
