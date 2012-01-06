//------------------------------------------------------------------------------
// TS.getDataIntervalMult - get the data interval multiplier
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:
//
//------------------------------------------------------------------------------
// History:
// 
// 08 Jan 1998	Steven A. Malers,	Split out of SetGet code.
//		Riverside Technology,
//		inc.
//------------------------------------------------------------------------------
// Variables:	I/O	Description		
//
//
//------------------------------------------------------------------------------

#include "resj/TS.h"

int TS :: getDataIntervalMult( void )
{
	return( _data_interval_mult );

/*  ==============  Statements containing RCS keywords:  */
{static char rcs_id1[] = "$Source: /fs/hseb/ob72/rfc/ofs/src/resj_ts/RCS/TS_getDataIntervalMult.cxx,v $";
 static char rcs_id2[] = "$Id: TS_getDataIntervalMult.cxx,v 1.2 2000/05/19 13:35:22 dws Exp $";}
/*  ===================================================  */

}
