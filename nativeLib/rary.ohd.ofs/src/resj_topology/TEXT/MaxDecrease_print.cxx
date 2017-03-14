//------------------------------------------------------------------------------
// MaxDecrease :: print - printd instance data members.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:
//
//------------------------------------------------------------------------------
// History:
// 
// 04 Mar 1998	Matthew J. Rutherford, RTi
//					Created initial version.
//------------------------------------------------------------------------------
// Variables:	I/O	Description		
//
//
//------------------------------------------------------------------------------
#include "MaxDecrease.h"
#include "TSDateIterator.h"

int MaxDecrease :: print( FILE* fp )
{
	char routine[] = "MaxDecrease::print";

	if( fp == NULL ) {
		PrintWarning( 1, routine, "Cannot print MaxDecrease info -"
		" null FILE*.");
		return( STATUS_FAILURE );
	}

	// Print self id fnd owner first
	fprintf( fp, "MaxDecrease method \"%s\" owned by Component \"%s\".\n", 
		_id, _owner->getID() );

	return( STATUS_SUCCESS );

/*  ==============  Statements containing RCS keywords:  */
/*  ===================================================  */


/*  ==============  Statements containing RCS keywords:  */
{static char rcs_id1[] = "$Source: /fs/hseb/ob81/ohd/ofs/src/resj_topology/RCS/MaxDecrease_print.cxx,v $";
 static char rcs_id2[] = "$Id: MaxDecrease_print.cxx,v 1.2 2006/10/26 15:26:08 hsu Exp $";}
/*  ===================================================  */

}
