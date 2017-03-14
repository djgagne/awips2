C MEMBER PRPB51
C***********************************************************************
C
C@PROCESS LVL(77)
C
      SUBROUTINE PRPB51(LPO,LTS,PO)
C
C     ROUTINE PRINTS THE INFORMATION STORED IN THE P ARRAY FOR THE
C     SSARRESV RESERVOIR OPERATION (UPERBKWR AND LWERBKWR).
C
C***********************************************************************
C     PROGRAMMED BY KUANG HSU  OCTOBER 1994
C***********************************************************************
      DIMENSION PO(*),X(365),Y(365),Z(365),BWTYP(2)
C
      INCLUDE 'common/ionum'
      INCLUDE 'common/fdbug'
      INCLUDE 'common/fengmt'
      INCLUDE 'common/unit51'
      REAL*8 GTSKW(12)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcinit_ssarresv/RCS/prpb51.f,v $
     . $',                                                             '
     .$Id: prpb51.f,v 1.3 1997/12/31 18:07:52 page Exp $
     . $' /
C    ===================================================================
C
C
      DATA BLANK/4H    /
C
      DATA GTSKW/8HINSTQI1 ,8HINSTQI2 ,8HINSTQO1 ,8HINSTQO2 ,8HMEANQOUT,
     1           8HPOOL    ,8HSTORAGE ,8HOBSQO   ,8HOBSQOM  ,8HOBSH    ,
     1           8HTRIBQL1 ,8HTRIBQL2 /
      DATA BWTYP/4HFLOW,4HELEV/
C
C
      ISTYP=PO(LPO-1)
      NVAL=PO(LPO)
      IX=LPO
      IY=IX+NVAL
      IZ=IY+NVAL
      DO 105 I=1,NVAL
 105  X(I)=PO(IX+I )
      DO 107 I=1,NVAL
 107  Y(I)=PO(IY+I)
      DO 109 I=1,NVAL
 109  Z(I)=PO(IZ+I)
      LPO=LPO+3*NVAL+1
C
      WRITE(IPR,1720)
      IB=1
      IE=NVAL
      IF(IE .GT. 8)IE=8
 110  WRITE(IPR,1725) UNITL,(X(I),I=IB,IE)
      WRITE(IPR,1726) UNITST,(Y(I),I=IB,IE)
C      WRITE(IPR,1727) UNITQ,(Z(I),I=IB,IE)
      IF(IE .GE. NVAL)GO TO 120
      IB=IE+1
      IE=IE+8
      IF(IE .GT. NVAL)IE=NVAL
      GO TO 110
C
1720  FORMAT(/10X,'STORAGE VS. ELEVATION CURVE:')
1725  FORMAT(/20X,' ELEV(',A2,1H),2X,8F11.2)
1726  FORMAT(20X,' STOR(',A4,1H),8F11.0)
1727  FORMAT(20X,'DISCH(',A3,1H),8F11.1)
C
120   CONTINUE
      NVAL=PO(LPO)
      LBWTYP=LPO+3*NVAL+1
      IBWTYP=PO(LBWTYP)
      DO 135 I=1,NVAL
      IX=LPO+(I-1)*3+1
      X(I)=PO(IX)
      IY=IX+1
      Y(I)=PO(IY)
      IZ=IY+1
      Z(I)=PO(IZ)
 135  CONTINUE
C
      WRITE(IPR,'(/10X,''BACKWATER TABLE: '')')
      WRITE(IPR,1820)
 1820 FORMAT(/20X,'   RES RELS   RES ELEV D/S CONTRL',
     & '   RES RELS   RES ELEV  DS CONTRL',
     & '   RES RELS   RES ELEV  DS CONTRL')
      IF(IBWTYP.EQ.1) WRITE(IPR,1821) UNITQ,UNITL,UNITQ,
     & UNITQ,UNITL,UNITQ,UNITQ,UNITL,UNITQ
      IF(IBWTYP.EQ.2) WRITE(IPR,1821) UNITQ,UNITL,UNITL,
     & UNITQ,UNITL,UNITL,UNITQ,UNITL,UNITL
      IB=1
      IE=NVAL
      IF(IE .GT. 3) IE=3
 140  IF(IBWTYP.EQ.1) WRITE(IPR,1824) (X(I),Y(I),Z(I),I=IB,IE)
      IF(IBWTYP.EQ.2) WRITE(IPR,1825) (X(I),Y(I),Z(I),I=IB,IE)
      IF(IE .GE. NVAL)GO TO 150
      IB=IE+1
      IE=IE+3
      IF(IE .GT. NVAL) IE=NVAL
      GO TO 140
C
1821  FORMAT(28X,A4,7X,A4,7X,A4,
     & 7X,A4,7X,A4,7X,A4,7X,A4,7X,A4,7X,A4)
1824  FORMAT(20X,3(F11.0,F11.2,F11.0))
1825  FORMAT(20X,3(F11.0,2F11.2))
C
 150  CONTINUE
      LPO=LPO+3*NVAL+1
C
C  PRINT BACKWATER CONTROL TYPE
c      IBWTYP=PO(LPO)
      IF(ISTYP.EQ.1) WRITE(IPR,1714) BWTYP(IBWTYP)
1714  FORMAT(/10X,'BACKWATR CONTROL TYPE:',2X,A4,
     & '  --CONTROLLED BY DOWNSTREAM RESERVOIR.')
      IF(ISTYP.EQ.2) WRITE(IPR,1715) BWTYP(IBWTYP)
1715  FORMAT(/10X,'BACKWATR CONTROL TYPE:',2X,A4,
     & '  --CONTROLLED BY TRIBUTARY.')
      LPO=LPO+1
C
C  PRINT MAXEL
      ELMAX=PO(LPO)
      WRITE(IPR,1730)UNITL,ELMAX
1730  FORMAT(/10X,'MAXIMUM ELEVATION (',A2,2H)=,F15.2)
      LPO=LPO+1
C
C  PRINT MINEL
      ELMIN=PO(LPO)
      WRITE(IPR,1731)UNITL,ELMIN
1731  FORMAT(/10X,'MINIMUM ELEVATION ALLOWED (',A2,2H)=,F15.2)
      LPO=LPO+1
C
C  PRINT MINQREL
      QRELMN=PO(LPO)
      WRITE(IPR,1732)UNITQ,QRELMN
 1732 FORMAT(/10X,'MINIMUM RESERVOIR RELEASE (',A3,2H)=,F15.0)
      LPO=LPO+1
C
C  PRINT SHUTRESV
      QSHUT=PO(LPO)
      WRITE(IPR,1733) UNITQ,QSHUT
 1733 FORMAT(/10X,'MAXIMUM TRIBUTARY FLOW TO SHUT OFF RESERVOIR (',
     & A3,2H)=,F15.0)
      LPO=LPO+1
C
C***********************************************************************
C     TIME SERIES INFORMATION
      WRITE(IPR,1745)
1745  FORMAT(/10X,20H       TIME SERIES:,13X,2HID,10X,4HTYPE,
     1 10X,8HTIME(HR)/)
      DO 155 I=1,10
      II=I+2
      IF(PO(LTS).EQ.BLANK)GO TO 152
      IT=PO(LTS+3)
      WRITE(IPR,1750) GTSKW(II),PO(LTS),PO(LTS+1),PO(LTS+2),IT
1750  FORMAT(21X,A8,10X,2A4,8X,A4,12X,I2)
152   LTS=LTS+5
155   CONTINUE
      RETURN
      END
