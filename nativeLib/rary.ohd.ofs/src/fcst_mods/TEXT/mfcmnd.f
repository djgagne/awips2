C MODULE MFCMND
C-----------------------------------------------------------------------
C
      SUBROUTINE MFCMND (NCARDS,MODCRD,ICMND,IDATE,LDATE,KDATE)
C
C  THIS ROUTINE FINDS A MOD CARD IMAGE AND DECODES ANY DATES ON THE CARD
C
      CHARACTER*8 CMDNAM
      DIMENSION IFIELD(4)
C
      INCLUDE 'udatas'
      INCLUDE 'common/ionum'
      INCLUDE 'common/fdbug'
      INCLUDE 'common/fpwarn'
      INCLUDE 'common/modctl'
      INCLUDE 'ufreex'
      COMMON /MDFLTD/ MHDFLT
      COMMON /MFG/ ISFG,NDTS
C
      DIMENSION MODCRD(20,NCARDS)
      DIMENSION OLDOPN(2)
      character*10  mychar(1)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcst_mods/RCS/mfcmnd.f,v $
     . $',                                                             '
     .$Id: mfcmnd.f,v 1.6 2004/08/05 18:06:29 wkwock Exp $
     . $' /
C    ===================================================================
C
C
C
      IBUG=IFBUG('MODS')+IFBUG('MNMD')
C      
      IF (IBUG.GT.0) WRITE (IODBUG,*) 'ENTER MFCMND'
C
      IOPNUM=0
      CALL FSTWHR ('MFCMND  ',IOPNUM,OLDOPN,IOLDOP)     
C
      LFIELD=4
C
      IDATE=0
      LDATE=0
      KDATE=0  

C
C  SET OPTION TO NOT CHECK FOR VALID DATE

      ICKDAT=0

C
C  GET FIRST FIELD FROM CARD
10    NFLD=0
      ISTRT=-3
      CALL UFIEL2 (NCARDS,MODCRD,NFLD,ISTRT,LEN,ITYPE,NREP,INTGER,REAL,
     1  LFIELD,IFIELD,LLPAR,LRPAR,LASK,LATSGN,LAMPS,LEQUAL,ISTAT)

cav....
      CALL MCMDNA (ICMND,CMDNAM)   
   
C
C  SET OPTION TO CHECK FOR VALID DATE
      ICKDAT=1
C
C  VALID STATUS?
      IF (ISTAT.EQ.0) GO TO 20
C
C  PROBLEMS - CANNOT BE COMMAND CARD - AT END OF INPUT?
      IF (ISTAT.NE.3) GO TO 10
C
C  AT END OF INPUT - SET ICMND TO 0
      ICMND=0
      GO TO 120
C
20    ICMND=MIFCMD(IFIELD,LEN,NFLD)
C

      IF (ICMND.EQ.0) GO TO 10
C
C  VALID COMMAND - NOW DECODE DATES ON THE COMMAND CARD
C  IF NEEDED - IF JUST CHECKING FOR A VALID COMMAND - RETURN
C
      IF (NDTS.EQ.0) GO TO 120
C
C  HAVE DATES TO DECODE
      ISTRT=-3
      CALL UFIEL2 (NCARDS,MODCRD,NFLD,ISTRT,LEN,ITYPE,NREP,INTGER,REAL,
     1  LFIELD,IFIELD,LLPAR,LRPAR,LASK,LATSGN,LAMPS,LEQUAL,ISTAT)
C
      NUMBER=0
C
C  VALID DATE FIELD?
      IF (ISTAT.NE.5) GO TO 70
C
C  DOES FIELD CONTAIN AN ASTERISK?
      IF (LASK.GT.0) GO TO 70
C
C  DATE IS VALID - STORE IN VARIABLE IDATE
      IPACK=1
      NHRADD=0
      NHSWCH=1
      IPRINT=0
      CALL HDATEA (LEN,ICDBUF(ISTRT:ISTRT),IPACK,NHRADD,NHSWCH,IPRINT,
     *   JDA,IHR,IDATE,ISTAT)
      IF (MHDFLT.EQ.0) IDATE=-IDATE
C
C  IS A SECOND DATE EXPECTED?
      IF (NDTS.EQ.1) GO TO 120
C
C  YES - DECODE AND STORE IN VARIABLE LDATE
      ISTRT=-3
      CALL UFIEL2 (NCARDS,MODCRD,NFLD,ISTRT,LEN,ITYPE,NREP,INTGER,REAL,
     1  LFIELD,IFIELD,LLPAR,LRPAR,LASK,LATSGN,LAMPS,LEQUAL,ISTAT)
C
C  CHECK FOR A NULL FIELD
      IF (ISTRT.NE.-2) GO TO 40
      
C  GET MOD NAME      
      CALL MCMDNA (ICMND,CMDNAM)

C
C  CHECK IF OPTIONAL FIELD
      IF (CMDNAM.EQ.'BASEF') GO TO 120
      IF (CMDNAM.EQ.'ROCHNG') GO TO 120
C
40    NUMBER=1
C
C  VALID DATE FIELD?
      IF (ISTAT.NE.5) GO TO 70
C
C  DOES FIELD CONTAIN AN ASTERISK?
      IF (LASK.GT.0) GO TO 70
C
C  DATE IS VALID - STORE IN VARIABLE LDATE
      CALL HDATEA (LEN,ICDBUF(ISTRT:ISTRT),IPACK,NHRADD,NHSWCH,IPRINT,
     *   JDA,IHR,LDATE,ISTAT)
      IF (MHDFLT.EQ.0) LDATE=-LDATE
C
C  IS A THIRD DATE EXPECTED?
      IF (NDTS.EQ.2) GO TO 120
C
C  YES - DECODE AND STORE IN VARIABLE KDATE
      ISTRT=-3
      CALL UFIEL2 (NCARDS,MODCRD,NFLD,ISTRT,LEN,ITYPE,NREP,INTGER,REAL,
     1  LFIELD,IFIELD,LLPAR,LRPAR,LASK,LATSGN,LAMPS,LEQUAL,ISTAT)
C
C  GET MOD NAME      
      CALL MCMDNA (ICMND,CMDNAM)
C
C  CHECK IF NO MORE FIELDS FOUND
      IF (ISTRT.NE.-2) GO TO 60
C
C  CHECK IF DATE FIELD IS OPTIONAL
      IF (CMDNAM.EQ.'ROMULT') GO TO 120
C
60    NUMBER=2
C
C  VALID DATE FIELD?
      IF (ISTAT.NE.5) THEN
         IF (CMDNAM.EQ.'ROMULT') GO TO 120
         GO TO 70
         ENDIF
C
C  DOES FIELD CONTAIN AN ASTERISK?
      IF (LASK.GT.0) GO TO 70
C
C  DATE IS VALID - STORE IN VARIABLE KDATE
      CALL HDATEA (LEN,ICDBUF(ISTRT:ISTRT),IPACK,NHRADD,NHSWCH,IPRINT,
     *   JDA,IHR,KDATE,ISTAT)
      IF (MHDFLT.EQ.0) KDATE=-KDATE
      GO TO 120
C
C  ERROR ON A DATE FIELD FOR A VALID COMMAND
70    IF (IWHERE.GT.1.OR.MODWRN.EQ.0) GO TO 110
      CALL MCMDNA (ICMND,CMDNAM)
      IF (ISTRT.EQ.-2) THEN
         WRITE (IPR,90) NDTS,CMDNAM,NUMBER
90    FORMAT ('0**WARNING** ',I2,' DATES ARE EXPECTED FOR THE MOD ',A8,
     1 ' BUT ',I2,' DATES WERE ENTERED. ',
     2 'THIS MOD WILL BE SKIPPED.')
         ELSE
            WRITE (IPR,80) CMDNAM
80    FORMAT ('0**WARNING** ERROR IN A DATE FIELD FOR MOD ',A8,
     1  '. THIS MOD WILL BE SKIPPED.')
         ENDIF
      WRITE (IPR,100) (MODCRD(I,NRDCRD),I=1,20)
100   FORMAT (13X,'THE MOD CARD IMAGE IS: ',20A4)
      CALL WARN
C
110   ICMND=-ICMND

C
120   CALL FSTWHR (OLDOPN,IOLDOP,OLDOPN,IOLDOP)
C 
  
      IF (IBUG.EQ.1) THEN
         WRITE (IODBUG,*)
     *      ' IDATE=',IDATE,
     *      ' LDATE=',LDATE,
     *      ' KDATE=',KDATE,
     *      ' '
         WRITE (IODBUG,*) 'EXIT MFCMND'
         ENDIF
C
      RETURN
C
      END
