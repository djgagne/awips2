c$pragma c (chkdollarsign)
C MODULE MROMUL
C-----------------------------------------------------------------------
C
C  THIS ROUTINE PERFORMS THE ROMULT MOD
C
      SUBROUTINE MROMUL(MP,P,MTS,TS,MD,D,NCARDS,MODCRD,IIDATE,LLDATE,
     1  KKDATE,NXTOPN,NXTNAM,IHZERO)
C
      LOGICAL FIRST
      CHARACTER*8 TSID,NXTNAM,OPID,BLANK8,MODNAM
C
      EQUIVALENCE (TSID,TSID4(1))
C
      INCLUDE 'ufreex'
      INCLUDE 'common/ionum'
      INCLUDE 'common/fctime'
      INCLUDE 'common/fctim2'
      INCLUDE 'common/fdbug'
      INCLUDE 'common/fpwarn'
      INCLUDE 'common/fmodft'
      COMMON /IROMUL/IPRWRN
C
      DIMENSION P(MP),TS(MTS),D(MD),VALUES(1),TSID4(2)
      DIMENSION OLDOPN(2),MODCRD(20,NCARDS),Modstmp(20)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcst_mods/RCS/mromul.f,v $
     . $',                                                             '
     .$Id: mromul.f,v 1.4 2004/09/08 16:13:38 aivo Exp $
     . $' /
C    ===================================================================
C
C
      DATA ISLASH/4H/   /,BLANK8/'        '/
      DATA MODNAM/'ROMULT  '/
C
      CALL FSTWHR(8HMROMUL  ,0,OLDOPN,IOLDOP)
C
C     SET DEBUG FLAG
C
      IBUG=IFBUG(4HMODS)+IFBUG(4HROMU)
C
      IF(IBUG.GT.0)WRITE(IODBUG,10)IIDATE,LLDATE,KKDATE,NXTOPN,NXTNAM
10    FORMAT(1H0,10X,'*** SUBROUTINE MROMUL ENTERED ***  IIDATE=',I11,
     1 ', LLDATE=',I11,' KKDATE=',I11,', NXTOPN=',I3,', NXTNAM= ',A8)
C
      IF(NXTOPN.NE.2)GO TO 420
C
      IDATE=IABS(IIDATE)
      LDATE=IABS(LLDATE)
      KDATE=IABS(KKDATE)
C
C     NOW SEE IF LSTCMPDY VALUE EQUAL LASTCMPDATE VALUE
C
      ICOMP=((LDACPD-1)*24)+LHRCPD
C
      IF ((KDATE.EQ.0).AND.(IDATE.LE.ICOMP)) GOTO 60
      IF ((IDATE.LE.KDATE).OR.(IDATE.LE.ICOMP)) GOTO 20
      IF ((IDATE.GT.KDATE).AND.(KDATE.EQ.ICOMP)) GOTO 60
      GOTO 50
C
20    IF (KDATE.EQ.ICOMP) GOTO 60
      IF (LDATE.LE.KDATE) GOTO 60
C
      IF (MODWRN.EQ.0) GOTO 40
C
      WRITE(IPR,30)(MODCRD(I,NRDCRD),I=1,20)
30    FORMAT('0**WARNING** IN ROMULT MOD - THIS MOD IS ',
     1  'ONLY ENABLED BETWEEN THE START DATE AND THE EFFECTIVE DATE ',
     2  'ON THE MOD CARD.' /
     3 13X,'THE CURRENT MOD CARD IMAGE IS: ',20A4)
       CALL WARN
C
40       LDATE=KDATE
         LLDATE=KKDATE
         GOTO 60
C
50       IF (MODWRN.EQ.0) GOTO 420
         ITYPE=4
         CALL MODS1(NCARDS,ICMND,MODNAM,MODCRD,ITYPE)
         GOTO 420
C
C     CHECK TIME PERIOD AGAINST RUN PERIOD - PRINT WARNING ONLY IF
C     ENTIRE PERIOD BEING CHANGED IS OUTSIDE OF RUN PERIOD
C
60    ISTHR=(IDA-1)*24+IHZERO
      IENHR=(LDA-1)*24+LHR
C
C     NOW SEE IF ANY OF PERIOD ENTERED IS WITHIN THE CURRENT RUN PERIOD
C
      IF(IDATE.GT.IENHR.OR.LDATE.LT.ISTHR)GO TO 70
C
C     SOME OF PERIOD BEING CHANGED IS WITHIN THE RUN PERIOD
C
      GO TO 90
C
C     NONE OF THE PERIOD BEING CHANGED IS WITHIN THE RUN PERIOD
C
70    IF(MODWRN.EQ.0)GO TO 420
      CALL MDYH2(IDA,IHZERO,IM1,ID1,IY1,IH1,DUM1,DUM2,MODTZC)
      CALL MDYH2(LDA,LHR,IM2,ID2,IY2,IH2,DUM1,DUM2,MODTZC)
      IXDA=IDATE/24+1
      IXHR=IDATE-(IXDA-1)*24
      IF(IXHR.EQ.0)IXDA=IXDA-1
      IF(IXHR.EQ.0)IXHR=24
      CALL MDYH2(IXDA,IXHR,IM3,ID3,IY3,IH3,DUM1,DUM2,MODTZC)
      LXDA=LDATE/24+1
      LXHR=LDATE-(LXDA-1)*24
      IF(LXHR.EQ.0)LXDA=LXDA-1
      IF(LXHR.EQ.0)LXHR=24
      CALL MDYH2(LXDA,LXHR,IM4,ID4,IY4,IH4,DUM1,DUM2,MODTZC)
C
      WRITE(IPR,80)IM3,ID3,IY3,IH3,MODTZC,IM4,ID4,IY4,IH4,MODTZC,
     1 IM1,ID1,IY1,IH1,MODTZC,IM2,ID2,IY2,IH2,MODTZC
80    FORMAT('0**WARNING** THE DATES FOR CHANGES IN THE ',
     1  'ROMULT MOD (',I2.2,'/',I2.2,'/',I4,'-',I2,A4,
     1  ' TO ',I2.2,'/',I2.2,'/',I4,'-',I2,A4,')' /
     2 13X,'ARE NOT WITHIN THE CURRENT RUN PERIOD (',
     3  I2.2,'/',I2.2,'/',I4,'-',I2,A4,
     4  ' TO ',I2.2,'/',I2.2,'/',I4,'-',I2,A4,')'/
     4 13X,'THESE CHANGES WILL BE IGNORED.')
      CALL WARN
      GO TO 420
C
C     CALL SUBROUTINE TO READ VALUES
C
90    MXVALS=1
C
C     READ CARD - IF COMMAND LEAVE - IF COMMAND AND 1ST CARD ERROR
C
      FIRST=.TRUE.
      IF(NRDCRD.EQ.NCARDS)GO TO 110
C
100   IF(NRDCRD.EQ.NCARDS)GO TO 420
C
      OPID=BLANK8
C
      IF(MISCMD(NCARDS,MODCRD).EQ.0)GO TO 130
C
      IF(.NOT.FIRST)GO TO 420
C
C     HAVE FOUND COMMAND AS FIRST SUBSEQUENT CARD - ERROR
C
110   IF(MODWRN.EQ.1)WRITE(IPR,120)
120   FORMAT('0**WARNING** NO SUBSEQUENT CARDS FOUND FOR THE ',
     1 'ROMULT MOD.  PROCESSING CONTINUES.')
      GO TO 410
C
130   FIRST=.FALSE.
C
C     CALL SUBROUTINE TO READ VALUES
C
      NFLD=1
      NRDCRD=NRDCRD+1
C
      do j = 1, 20          
          Modstmp(j) = modcrd(j,nrdcrd)  
      end do
      call chkdollarsign(iret,Modstmp)
      if (iret .eq. 1) goto 100
C
      CALL MRDVAL(NCARDS,MODCRD,NFLD,MXVALS,NVALS,VALUES,ISTAT)
C
      IF(IBUG.GT.0)WRITE(IODBUG,140)ISTAT,FIRST,NVALS
140   FORMAT(' IN MROMUL -AFTER CALL TO MRDVAL - ISTAT=',I3,
     1 ', FIRST=',L4,', NVALS=',I3)
      IF(IBUG.GT.0.AND.NVALS.GT.0.AND.ISTAT.NE.-1)WRITE(IODBUG,150)
     1 (VALUES(I),I=1,NVALS)
150   FORMAT(1X,'VALUES='/(1X,10G10.2))
C
C     ISTAT RETURNED FROM MRDVAL MEANS
C       =0, VALUES READ OK, NO ADDITIONAL FIELDS ON CARD
C       =2, VALUES READ OK, ADDITIONAL FIELDS ON CARD
C       =-1, NO VALUES ENTERED
C       ELSE, TOO MANY VALUES ENTERED
C
      IF(ISTAT.EQ.0)GO TO 250
      IF(ISTAT.EQ.2)GO TO 190
      IF(ISTAT.EQ.-1)GO TO 170
C
      IF(MODWRN.EQ.0)GO TO 250
      WRITE(IPR,160)(MODCRD(I,NRDCRD),I=1,20)
160   FORMAT('0**WARNING** IN ROMULT MOD - MORE THAN ',
     1     'ONE VALUE ENTERED. THE FIRST VALUE ENTERED WILL BE USED.' /
     3  13X,'THE CURRENT CARD IMAGE IS:' /
     4  13X,20A4)
      CALL WARN
      GO TO 250
C
170   IF(MODWRN.EQ.0)GO TO 100
      WRITE(IPR,180)(MODCRD(I,NRDCRD),I=1,20)
180   FORMAT('0**WARNING** ',
     1     'NO VALUES ENTERED ON A SUBSEQUENT CARD FOR ROMULT MOD.' /
     2  13X,'THE CURRENT MOD CARD IMAGE IS: ',20A4)
      GO TO 410
C
C     HAVE ADDITIONAL FIELDS - LOOK FOR A SLASH (/) AND
C     AN OPERATION NAME - REPROCESS CURRENT FIELD TO SEE IF A SLASH
C
190   ISTRT=-1
      NCHAR=1
      ICKDAT=0
C
      CALL UFIEL2(NCARDS,MODCRD,NFLD,ISTRT,LEN,ITYPE,NREP,INTGER,REAL,
     1  NCHAR,IFIELD,LLPAR,LRPAR,LASK,LATSGN,LAMPS,LEQUAL,ISTAT)
C
      IF(IFIELD.EQ.ISLASH)GO TO 210
C
      IF(MODWRN.EQ.1)
     .  WRITE(IPR,200)(MODCRD(I,NRDCRD),I=1,20)
200   FORMAT('0**WARNING** IN ROMULT MOD - A SLASH ',
     1    'WAS NOT FOUND WHERE EXPECTED ON THE FOLLOWING CARD:' /
     2 13X,20A4 /
     3 13X,'NO CHANGES ENTERED ON THIS CARD WILL BE MADE.')
      GO TO 410
C
C     NOW READ OPERATION NAME
C
210   ISTRT=-3
      NCHAR=2
      ICKDAT=0
C
      CALL UFIEL2(NCARDS,MODCRD,NFLD,ISTRT,LEN,ITYPE,NREP,INTGER,REAL,
     1  NCHAR,OPID,LLPAR,LRPAR,LASK,LATSGN,LAMPS,LEQUAL,ISTAT)
C
      IF(ISTRT.NE.-2)GO TO 230
      IF(MODWRN.EQ.0)GO TO 100
      WRITE(IODBUG,220)(MODCRD(I,NRDCRD),I=1,20)
220   FORMAT('0**WARNING** NO OPERATION NAME ENTERED ',
     1    'AFTER A SLASH ON THE FOLLOWING MOD CARD:' /
     2 13X,20A4 /
     2 13X,'THIS CARD IS IGNORED.')
      GO TO 410
230   CONTINUE
C
C     CHECK THAT OPERATION NAME ENTERED MATCHES NXTNAM -
C     IF NOT, READ NEXT CARD
C
      IF(IBUG.GT.0)WRITE(IODBUG,240)OPID,NXTNAM
240   FORMAT(1X,'COMPARING THE OPERATION NAME ENTERED - ',A8,
     1 ' WITH THE NEXT OPERATION NAME - ',A8)
      IF(OPID.EQ.NXTNAM)GO TO 250
      GO TO 100
C
C     FIND START OF SECOND PORTION OF P ARRAY FOR THE OPERATION
C
250   LOCOPN=0
      CALL FSERCH(2,NXTNAM,LOCOPN,P,MP)
C
      IF(LOCOPN.GT.0)GO TO 270
C
C     SHOULD NEVER GET HERE - MEANS PROBLEM IN P ARRAY
C
      IF(MODWRN.EQ.1)
     .WRITE(IPR,260)NXTNAM
260   FORMAT('0**WARNING** IN ROMULT MOD - CANNOT FIND ',
     1 'UNIT-HG OPERATION ',A8,' IN THE P ARRAY.')
      GO TO 410
C
C     CHECK FOR NEGATIVE MULTIPLIER
C
270   IF(VALUES(1).GE.0.0)GO TO 290
      IF(MODWRN.EQ.1)WRITE(IPR,280)(MODCRD(J,NRDCRD),J=1,20)
280   FORMAT('0**WARNING** IN ROMULT MOD - ',
     1    'A NEGATIVE MULTIPLIER WAS ENTERED. ',
     2    'NO CHANGES ON THIS CARD WILL BE MADE. ' /
     3  13X,'THE CURRENT MOD CARD IMAGE IS: ',20A4)
      GO TO 410
C
290   CONTINUE
C
C     TIME SERIES ID IS IN POSITIONS 13-14 OF P ARRAY
C     DATA TYPE IS IN POSITION 15, TIME INTERVAL IS IN POSITION 16
C
      LOCP=LOCOPN-1
      TSID4(1)=P(LOCP+13)
      TSID4(2)=P(LOCP+14)
      DTYPE=P(LOCP+15)
      IDT=P(LOCP+16)
C
      IF(IBUG.LT.1)GO TO 320
C
      WRITE(IODBUG,300)IDATE,LDATE,TSID,DTYPE,IDT,NVALS,OPID
300   FORMAT(' IN SUBROUTINE MROMUL - ROMULT MOD'/1X,
     1 'IDATE,     LDATE, TSID, DTYPE, IDT, NVALS, OPID ='/5X,
     1 2I11,1X,A8,1X,A4,1X,I3,1X,I3,1X,A8)
      IF(NVALS.GT.0)WRITE(IODBUG,310)(VALUES(I),I=1,NVALS)
310   FORMAT(' VALUES ='/(1X,10G9.2))
C
320   CONTINUE
C
C     GET LOCATION OF TIME SERIES IN SEGMENT
C
      CALL FINDTS(TSID,DTYPE,IDT,LOCD,LOCTS,DIMS)
C
      IF(LOCD.GT.0)GO TO 340
C
      IF(MODWRN.EQ.1)
     .WRITE(IPR,330)TSID,DTYPE,IDT
330   FORMAT('0**WARNING** IN ROMULT MOD - TIME SERIES ',
     1 A8,1X,A4,I3,' NOT IN THIS SEGMENT.')
      GO TO 410
C
340   CONTINUE
C
C     SEE IF THIS DATA TYPE ALLOWS MISSING DATA
C     AND GET VALUE FOR NUMBER OF VALUES PER DELTA T (NVPDT)
C
      CALL FDCODE(DTYPE,UNITS,DIMS,MISALW,NVPDT,TSCALE,NADINF,IERR)
C
C     CHECK TIME PERIOD AGAINST RUN PERIOD - PRINT WARNING ONLY IF
C     ENTIRE PERIOD BEING CHANGED IS OUTSIDE OF RUN PERIOD
C
C     IF START HOUR NOT ENTERED - USE DEFAULT VALUE OF 12Z
C     IF START HOUR ENTERED - SET TO CLOSEST DT OF TIME SERIES
C
C     IF HOUR ENTERED IIDATE LESS THAN ZERO
C
      IF(IIDATE.LT.0)GO TO 350
C
C     HOUR NOT ENTERED - IDATE SHOULD BE DIVISIBLE BY 24
C
      JSTHR=IDATE
      GO TO 360
C
350   CONTINUE
C
C     HOUR ENTERED - SET TO NEAREST DT
C
      JSTHR=MISTDT(IDATE,IDT)
C
360   CONTINUE
C
C     IF END HOUR ENTERED LLDATE LESS THAN ZERO
C
      IF(LLDATE.LT.0)GO TO 370
C
C     HOUR NOT ENTERED
C
      JENHR=LDATE
      GO TO 380
C
C     HOUR ENTERED - SET TO NEAREST DT
C
370   JENHR=MISTDT(LDATE,IDT)
C
380   CONTINUE
C
C     NVALS IS NOW THE NUMBER OF VALUES FOR TOTAL PERIOD REQUESTED
C
      NVALS=((JENHR-JSTHR)/IDT+1)*NVPDT
C
C  GET LOCATIONS TO START CHANGES AND NUMBER OF VALUES TO MOVE
C
      CALL MLOCCH(8HROMULT  ,ISTHR,IENHR,JSTHR,JENHR,
     1 TSID,DTYPE,IDT,NVALS,NVPDT,LOCD,IBUG,
     2 IOFFST,LDSTRT,NMOVE,ISTART,ISTATT)
C
      IF(ISTATT.EQ.1)GO TO 100
C
      IF(IBUG.GT.0)WRITE(IODBUG,390)NMOVE,TSID,DTYPE,IDT,LDSTRT,
     1 VALUES(1),ISTART
390   FORMAT(' ABOUT TO MULTIPLY ',I3,' VALUES IN ',A8,1X,A4,I3,
     1 ' STARTING AT LOCATION ',I5,'.'/1X,'MULTIPLIER = ',F10.3,
     2 ', STARTING HOUR FOR CHANGES = ',I7)
C
      DO 400 I=1,NMOVE
      TEMPD=D(LDSTRT+I)
      IF(IFMSNG(TEMPD).EQ.0)D(LDSTRT+I)=TEMPD*VALUES(1)
400   CONTINUE
      CALL MF27CB(TSID4,DTYPE,IDT,ISTART,NMOVE,IBUG)
      GO TO 100
C
410   CONTINUE
      IF(MODWRN.EQ.1)CALL WARN
      GO TO 100
C
420   CONTINUE
      CALL FSTWHR(OLDOPN,IOLDOP,OLDOPN,IOLDOP)
      IF(IBUG.GT.0)WRITE(IODBUG,430)
430   FORMAT(' *** LEAVING MROMUL ***')
      RETURN
      END
