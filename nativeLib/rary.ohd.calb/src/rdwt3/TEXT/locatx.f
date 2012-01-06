C MEMBER LOCATX
C  (from old member RWLOCATE)
C-----------------------------------------------------------------------
C
C                             LAST UPDATE: 07/01/94.15:28:50 BY $WC20SV
C
      SUBROUTINE LOCATX (IMO,IYR,LMO,LYR,UNITS,FILEN,TYPE,IT,STAID,
     *                   DESCRP,NXREAD,ISEQ)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/calb/src/rdwt3/RCS/locatx.f,v $
     . $',                                                             '
     .$Id: locatx.f,v 1.1 1996/05/24 16:34:24 dws Exp $
     . $' /
C    ===================================================================
C
C
C
C  LOCATX IS FOR WHEN TIME SERIES HEADER CARD ALREADY READ
C
C  ROUTINE  LOCATES  A TIME SERIES IN THE CALIBRATION DATA FILES
C  AND ESTABLISHES AN INTERNAL BOOKKEEPING SYSTEM USED FOR READING
C  THE DATA.
C
C
C  INPUT VARIABLES IN THE ARGUMENT LIST ARE:
C     IMO    - FIRST MONTH TO BE READ
C     IYR    - FIRST YEAR TO BE READ
C     LMO    - LAST MONTH TO BE READ
C     LYR    - LAST YEAR TO BE READ
C     *NOTE* - IF IMO, IYR, LMO, AND LYR ARE ZERO, THESE VARIABLES WILL
C              BE FILLED WITH THE VALUES FOUND IN THE BLOCK HEADER.
C     UNITS  - REQUESTED UNITS. IF 'SAME' ARE PASSED, NO CONVERSION WILL
C              BE PERFORMED
C
C     FILEN  - FILE NAME
C     TYPE   - DATA TYPE CODE
C     IT     - DATA TIME INTERVAL
C     STAID  - 12 CHARACTER IDENTIFICATION CODE
C
C  OUTPUT VARIABLES IN THE ARGUMENT LIST:
C     DESCRP - 20 CHARACTER DESCRIPTION
C     NXREAD - RECORD NUMBER WHERE FIRST MONTH OF DATA BEGINS
C     ISEQ   - LOCATION OF TIME SERIES INFORMATION IN BOOKKEEPING SYSTEM
C
C  FORMAT OF THE TIME SERIES HEADER CARD READ BY SUBROUTINE LOCATE:
C     COLUMN  1-12   FILE NAME
C            13-16   DATA TYPE CODE
C            17-20   DATA TIME INTERVAL
C            21-26   BLOCK HEADER RECORD NUMBER. IF NOT ENTERED OR IS
C                    OR IS INCORRECT, THE FILE WILL BE SEARCHED.
C               27   MINUS SIGN (-)
C            28-39   IDENTIFICATION CODE
C               40   MINUS SIGN (-)
C            41-42   MONTH TIME SERIES WAS CREATED (I2)
C               43   SLASH (/)
C            44-45   DAY TIME SERIES WAS CREATED (I2)
C               46   SLASH (/)
C            47-48   YEAR (2 DIGITS) TIME SERIES WAS CREATED
C               49   MINUS SIGN (-)
C            50-53   HOUR AND MINUTE (MILITARY TIME) TIME SERIES CREATED
C               54   PERIOD (.)
C            55-58   SECOND (TO THE HUNDREDTH OF A SECOND) TIME SERIES
C                    WAS CREATED
C            59-60   BLANK
C            61-80   DESCRIPTIVE INFORMATION
C
C      *NOTE* - IF ANY OF THE CREATION TIME IS MISSING, LOCATE WILL:
C               1. USE THE TIME SERIES IF THE OTHER INFORMATION ON THE
C                  HEADER CARD MATCHES THAT IN THE BLOCK HEADER.
C               2. USE THE FIRST TIME SERIES IN THE FILE THAT MATCHES
C                  THE INFORMATION IN COLUMNS 1-20 AND 28-39 PLUS ANY
C                  PART OF THE CREATION TIME SUPPLIED IF THE BLOCK
C                  HEADER LOCATION IS MISSING OR IN ERROR.
C
cew    change locate to return nonsense for chekcin if on
cew    workstations or not
cew

      imo=-1000

      RETURN
      END
