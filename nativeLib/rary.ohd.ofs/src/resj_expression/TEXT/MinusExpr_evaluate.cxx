//------------------------------------------------------------------------------
// MinusExpr :: evaluate - evaluates the equality of two expressions.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:
//
//------------------------------------------------------------------------------
// History:
// 
// 15 Jan 1998	Matthew J. Rutherford, Riverside Technology, inc
//					Created initial version.
//------------------------------------------------------------------------------
// Variables:	I/O	Description		
//
//
//------------------------------------------------------------------------------
#include "MinusExpr.h"

double MinusExpr :: evaluate()
{
	return( (double)(_left_op->evaluate() - _right_op->evaluate() ) );

/*  ==============  Statements containing RCS keywords:  */
/*  ===================================================  */


/*  ==============  Statements containing RCS keywords:  */
{static char rcs_id1[] = "$Source: /fs/hseb/ob81/ohd/ofs/src/resj_expression/RCS/MinusExpr_evaluate.cxx,v $";
 static char rcs_id2[] = "$Id: MinusExpr_evaluate.cxx,v 1.2 2006/10/26 15:27:37 hsu Exp $";}
/*  ===================================================  */

}
