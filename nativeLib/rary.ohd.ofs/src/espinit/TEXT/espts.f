C MODULE ESPTS
C-----------------------------------------------------------------------
C
      SUBROUTINE ESPTS (TS,MTS,TTS,MTTS,TSESP,MTSESP,IER)
C
C   THIS ROUTINE SETS DEFAULTS FOR ALL TIME SERIES IN ARRAY TS THAT
C   HAVE NOT BEEN DEFINED IN ARRAY TTS AND WRITES THE COMBINED ARRAYS TS
C   AND TTS TO ARRAY TSESP.
C
C   THIS ROUTINE WAS WRITTEN BY GERALD N DAY.
C
      CHARACTER*8 OLDOPN,TSTYPE
C
      DIMENSION TS(MTS),TTS(MTTS),TSESP(MTSESP)
      DIMENSION TSID(2),TTSID(2)
C
      INCLUDE 'common/ionum'
      INCLUDE 'common/fdbug'
      INCLUDE 'common/espseg'
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/espinit/RCS/espts.f,v $
     . $',                                                             '
     .$Id: espts.f,v 1.3 2002/02/11 19:54:38 dws Exp $
     . $' /
C    ===================================================================
C
      DATA RMSNG/4hMSNG/
      DATA BLANK/4h    /
C
C
      IOPNUM=0
      CALL FSTWHR ('ESPTS   ',IOPNUM,OLDOPN,IOLDOP)
C
      IF (ITRACE.GE.1) WRITE(IODBUG,*) 'ENTER ESPTS'
C
      IBUG=IFBUG('EARY')
C
      IER=0
      LTS=0
      LTSESP=0
C
C  GET VALUES FROM TS ARRAY
10    ITSTYP=TS(LTS+1)
      IF (ITSTYP.EQ.0) GO TO 430
      TSID(1)=TS(LTS+3)
      TSID(2)=TS(LTS+4)
      DTYPE=TS(LTS+5)
      IDT=TS(LTS+6)
      IF (IBUG.EQ.1) THEN
         WRITE (IODBUG,20) ITSTYP,TSID,DTYPE,IDT
20    FORMAT (' ITSTYP=',I2,' TSID=',2A4,' DTYPE=',A4,' IDT=',I2)
         ENDIF
C
      LTTS=0
      LTTS1=0
C
C  SEARCH TTS ARRAY FOR A MATCH
30    IF (TSID(1).NE.TTS(LTTS+3).OR.TSID(2).NE.TTS(LTTS+4)) GO TO 220
      IF (DTYPE.NE.TTS(LTTS+5)) GO TO 220
      IDTT=TTS(LTTS+6)
      IF (IDT.NE.IDTT) GO TO 220
      ITSTYPT=TTS(LTTS+1)
      IF (ITSTYP.EQ.ITSTYPT) GO TO 70
      GO TO (50,40,70,70),ITSTYP
C
C  TS IS UPDATE IN FC - MUST BE UPDATE OR INPUT IN ESP
40    IF (ITSTYPT.EQ.1) GO TO 70
50    WRITE(IPR,60) TSID,DTYPE,IDT
60    FORMAT ('0**ERROR** NEW TS TYPE IS INVALID FOR TS ',2A4,2X,A4,I5)
      CALL ERROR
      IER=1
      GO TO 460
C
C  CALCULATE LOCATION (NUMBER OF ADD. INFO) VALUE IN TS ARRAY.
70    IF (ITSTYP.NE.4) GO TO 80
C
C  INTERNAL TS IN THE FC PROGRAM
      LNADD=LTS+10
      GO TO 90
C
C  TS IS OTHER THAN INTERNAL IN FC
80    NEXTS=TS(LTS+12)
      LNADD=LTS+13+NEXTS
C
90    NADD=TS(LNADD)
C
C  CALCULATE SPACE NEEDED IN THE TSESP ARRAY
      IF (ITSTYPT.NE.4) GO TO 100
C
C  INTERNAL TS IN ESP PROGRAM
      NUSE=10+NADD
      GO TO 110
C
C  TS IS OTHER THAN INTERNAL IN ESP
100   NUMEXT=TTS(LTTS+12)
      NUMESP=TTS(LTTS+13+NUMEXT)
      NUSE=14+NUMEXT+NADD+NUMESP
C
110   LAST=LTSESP+NUSE
      IF (LAST.LE.MTSESP) GO TO 130
         WRITE(IPR,120) MTSESP
120   FORMAT ('0**ERROR** NOT ENOUGH SPACE IN THE TSESP ARRAY ',
     1 'MTSESP=',I5)
         IER=1
         CALL ERROR
         GO TO 460
C
C  GET VALUES FROM TTS ARRAY
130   IF (ITSTYPT.NE.4) GO TO 160
C
C  INTERNAL TS IN ESP
      DO 140 I=1,9
         TSESP(LTSESP+I)=TTS(LTTS+I)
140      CONTINUE
C
C  GET LOCATION OF TS IN D ARRAY
      TSESP(LTSESP+8)=TS(LTS+8)
      TSESP(LTSESP+10)=NADD+.01
      IF (NADD.EQ.0) GO TO 210
      DO 150 I=1,NADD
         TSESP(LTSESP+10+I)=TS(LNADD+I)
150      CONTINUE
      GO TO 210
C
C  TS IN ESP IS OTHER THAN INTERNAL
160   N=12+NUMEXT
      DO 170 I=1,N
         TSESP(LTSESP+I)=TTS(LTTS+I)
170      CONTINUE
C
C  GET LOCATION OF TS IN D ARRAY
      TSESP(LTSESP+8)=TS(LTS+8)
      TSESP(LTSESP+N+1)=NADD+.01
      IF (NADD.EQ.0) GO TO 190
      DO 180 I=1,NADD
         TSESP(LTSESP+N+1+I)=TS(LNADD+I)
180      CONTINUE
C
C  GET ESP INFO
190   N=N+1+NADD
      TSESP(LTSESP+N+1)=NUMESP+.01
      IF (NUMESP.EQ.0) GO TO 210
      DO 200 I=1,NUMESP
         TSESP(LTSESP+N+1+I)=TTS(LTTS+13+NUMEXT+I)
200      CONTINUE
C
C  UPDATE LOCATION OF NEXT TIME SERIES IN TSESP
210   TSESP(LTSESP+2)=LTSESP+NUSE+1.01
C
C  IDENTIFY A TTS ARRAY MATCH BY SETTING ID EQUAL TO BLANK
      TTS(LTTS+3)=BLANK
C
C  UPDATE LOCATIONS IN ARRAYS
      GO TO 400
C
C  SET UP NEXT TIME SERIES IN THE TTS ARRAY
220   LTTS=TTS(LTTS+2)-1
      NCTTS=TTS(LTTS+1)
      IF (IBUG.EQ.1) WRITE (IPR,*) 'IN ESPTS - LTTS=',LTTS,
     *   ' MTTS=',MTTS,' NCTTS=',NCTTS
      IF (LTTS.GE.MTTS.OR.NCTTS.EQ.0) GO TO 230
      IF (LTTS.GT.LTTS1) THEN
         LTTS1=LTTS
         ELSE
            WRITE (IPR,223) LTTS,LTTS1
223   FORMAT ('0**ERROR** VALUE OF NEXT POSITION IN TTS ARRAY (',I4,
     *   ') IS LESS THAN PREVIOUS POSITION (',I4,').')
            CALL ERROR
            IER=1
            GO TO 460
         ENDIF
      IF (NCTTS.GT.4) THEN
         WRITE (IPR,225) NCTTS
225   FORMAT ('0**ERROR** INVALID VALUE OF NCTTS (',I10,
     *   ') ENCOUNTERED.')
         CALL ERROR
         IER=1
         GO TO 460
         ENDIF
      GO TO 30
C
C  TIME SERIES NOT FOUND IN THE TTS ARRAY - SET UP DEFAULTS
230   IF (ITSTYP.EQ.4) GO TO 370
      WRITE (IPR,240) TSID,DTYPE,IDT
240   FORMAT ('0**WARNING** TIME SERIES WITH THE ID, DATA TYPE AND ',
     *     'TIME INTERVAL OF ',2A4,' ',A4,' ',I2,
     *     ' WAS NOT DEFINED FOR ESP AND' /
     * 13X,'WILL BE ASSIGNED DEFAULT VALUES.')
      CALL WARN
      IF (ITSTYP.NE.1) GO TO 320
C
C  INPUT TIME SERIES
250   CALL FDCODE(DTYPE,UNITS,DIM,MSG,NPDT,TSCALE,NADD,IERR)
      IF (MSG.EQ.1) GO TO 270
         WRITE (IPR,260) DTYPE,TSID
260   FORMAT('0**ERROR** MISSING DATA NOT ALLOWED FOR DATA TYPE ',A4,
     * ' FOR IDENTIFIER ',2A4,'.')
         IER=1
         CALL ERROR
         GO TO 460
C
C  WRITE INFO TO TSESP ARRAY
270   NUMEXT=TS(LTS+12)
      NADD=TS(LTS+13+NUMEXT)
      NUSE=14+NADD
      LAST=LTSESP+NUSE
      IF (LAST.LE.MTSESP) GO TO 280
         WRITE(IPR,120) MTSESP
         IER=1
         CALL ERROR
         GO TO 460
280   TSESP(LTSESP+1)=1.01
      TSESP(LTSESP+2)=LTSESP+NUSE+1.01
      DO 290 I=3,9
         TSESP(LTSESP+I)=TS(LTS+I)
290      CONTINUE
      TSESP(LTSESP+10)=RMSNG
      TSESP(LTSESP+11)=0.01
      TSESP(LTSESP+12)=0.01
      TSESP(LTSESP+13)=NADD+.01
      IF (NADD.EQ.0) GO TO 310
         DO 300 I=1,NADD
            TSESP(LTSESP+13+I)=TS(LTS+13+NUMEXT+I)
300         CONTINUE
310   TSESP(LTSESP+14+NADD)=0.01
C
C  UPDATE LOCATIONS IN ARRAYS
      GO TO 400
C
320   IF (ITSTYP.NE.2) GO TO 330
C
C  UPDATE TIME SERIES - TREAT LIKE INPUT TIME SERIES
      GO TO 250
C
C  OUTPUT TIME SERIES - MAKE IT INTERNAL
C
C  WRITE INFO TO TSESP ARRAY
330   NUMEXT=TS(LTS+12)
      NADD=TS(LTS+13+NUMEXT)
      NUSE=10+NADD
      LAST=LTSESP+NUSE
      IF (LAST.LE.MTSESP) GO TO 340
         WRITE(IPR,120) MTSESP
         IER=1
         CALL ERROR
         GO TO 460
340   TSESP(LTSESP+1)=4.01
      TSESP(LTSESP+2)=LTSESP+NUSE+1.01
      DO 350 I=3,9
         TSESP(LTSESP+I)=TS(LTS+I)
350      CONTINUE
      TSESP(LTSESP+10)=NADD+.01
      IF (NADD.EQ.0) GO TO 400
      DO 360 I=1,NADD
         TSESP(LTSESP+10+I)=TS(LTS+13+NUMEXT+I)
360      CONTINUE
C
C  UPDATE LOCATIONS IN ARRAYS
      GO TO 400
C
C  INTERNAL TIME SERIES
370   NADD=TS(LTS+10)
      NUSE=10+NADD
      LAST=LTSESP+NUSE
      IF (LAST.LE.MTSESP) GO TO 380
         WRITE(IPR,120) MTSESP
         IER=1
         CALL ERROR
         GO TO 460
380   DO 390 I=1,NUSE
         TSESP(LTSESP+I)=TS(LTS+I)
390      CONTINUE
      TSESP(LTSESP+2)=LTSESP+NUSE+1.01
C
400   IF (IBUG.EQ.1) THEN
         I1=LTSESP+1
         I2=LTSESP+NUSE
         WRITE (IODBUG,410)
410   FORMAT (' TSESP ARRAY:')
         WRITE (IODBUG,420) (TSESP(I),I=I1,I2)
420   FORMAT (' ',10F10.0)
         ENDIF
C
      LTS=TS(LTS+2)-1
      IF (LTS.GE.MTS) GO TO 430
      LTSESP=LTSESP+NUSE
      GO TO 10
C
C  CHECK FOR TIME SERIES IN ARRAY TTS BUT NOT IN ARRAY TS
430   LTTS=0
440   TTSID(1)=TTS(LTTS+3)
      IF (IBUG.EQ.1) WRITE (IODBUG,445) LTTS,TTSID(1)
445   FORMAT (' LTTS=',I4,' TTSID(1)=',A4)
      IF (TTSID(1).NE.BLANK) THEN
         ITSTYP=TTS(LTTS+1)
         TSTYPE='?'
         IF (ITSTYP.EQ.1) TSTYPE='INPUT'
         IF (ITSTYP.EQ.2) TSTYPE='UPDATE'
         IF (ITSTYP.EQ.3) TSTYPE='OUTPUT'
         IF (ITSTYP.EQ.4) TSTYPE='INTERNAL'
         TTSID(2)=TTS(LTTS+4)
         DTYPE=TTS(LTTS+5)
         IDT=TTS(LTTS+6)
         WRITE (IPR,450) TSTYPE(1:LENSTR(TSTYPE)),TTSID,DTYPE,IDT
450   FORMAT ('0**ERROR** ',A,' TIME SERIES WITH THE ID, DATA TYPE ',
     *     'AND TIME INTERVAL OF ',2A4,' ',A4,' ',I2,
     *     ' WAS NOT FOUND IN THE ' /
     * 11X,'FORECAST COMPONENT DEFINITION AND WILL NOT BE ADDED TO ',
     *     'THE ESP DEFINITION.')
         IER=1
         CALL ERROR
         ENDIF
      LTTS=TTS(LTTS+2)-1
      NCTTS=TTS(LTTS+1)
      IF (IBUG.EQ.1) WRITE (IODBUG,*) ' LTTS=',LTTS,' MTTS=',MTTS,
     * ' NCTTS=',NCTTS
      IF (LTTS.GE.MTTS.OR.NCTTS.EQ.0) GO TO 460
      GO TO 440
C
C  COMPLETED THE TSESP ARRAY
460   NEXT=LTSESP+1
      IF (NEXT.GT.MTSESP) GO TO 470
         TSESP(NEXT)=0.01
         LTSESP=NEXT
470   IF (IER.EQ.1) LTSESP=0
C
      IF (IBUG.EQ.1) THEN
         WRITE (IODBUG,*) 'LTSESP=',LTSESP
         WRITE (IODBUG,480)
480   FORMAT (' TS ARRAY:')
         WRITE (IODBUG,490) (TS(I),I=1,MTS)
490   FORMAT (1X,10F10.0)
         WRITE (IODBUG,500)
500   FORMAT (' TTS ARRAY:')
         WRITE (IODBUG,490) (TTS(I),I=1,MTTS)
         WRITE (IODBUG,510)
510   FORMAT (' TSESP ARRAY:')
         WRITE (IODBUG,490) (TSESP(I),I=1,MTSESP)
         ENDIF
C
520   CALL FSTWHR (OLDOPN,IOLDOP,OLDOPN,IOLDOP)
C
      RETURN
C
      END
