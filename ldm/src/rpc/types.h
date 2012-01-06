/* @(#)types.h	2.3 88/08/15 4.0 RPCSRC */
/*
 * Sun RPC is a product of Sun Microsystems, Inc. and is provided for
 * unrestricted use provided that this legend is included on all tape
 * media and as a part of the software program in whole or part.  Users
 * may copy or modify Sun RPC without charge, but are not authorized
 * to license or distribute it to anyone else except as part of a product or
 * program developed by the user.
 * 
 * SUN RPC IS PROVIDED AS IS WITH NO WARRANTIES OF ANY KIND INCLUDING THE
 * WARRANTIES OF DESIGN, MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE, OR ARISING FROM A COURSE OF DEALING, USAGE OR TRADE PRACTICE.
 * 
 * Sun RPC is provided with no support and without any obligation on the
 * part of Sun Microsystems, Inc. to assist in its use, correction,
 * modification or enhancement.
 * 
 * SUN MICROSYSTEMS, INC. SHALL HAVE NO LIABILITY WITH RESPECT TO THE
 * INFRINGEMENT OF COPYRIGHTS, TRADE SECRETS OR ANY PATENTS BY SUN RPC
 * OR ANY PART THEREOF.
 * 
 * In no event will Sun Microsystems, Inc. be liable for any lost revenue
 * or profits or other special, indirect and consequential damages, even if
 * Sun has been advised of the possibility of such damages.
 * 
 * Sun Microsystems, Inc.
 * 2550 Garcia Avenue
 * Mountain View, California  94043
 */
/*      @(#)types.h 1.18 87/07/24 SMI      */

/*
 * Rpc additions to <sys/types.h>
 */
#ifndef __TYPES_RPC_HEADER__
#define __TYPES_RPC_HEADER__

#include <stdlib.h>

#ifndef makedev /* ie, we haven't already included it */
#include <sys/types.h>
#endif
#if defined(_BSD_TYPES)
#  include <time.h>	/* needed by <sys/time.h> on IRIX64 */
#endif
#include <sys/time.h>

/*
 * Needed by rpcgen(1)-generated code on FreeBSD.
 */
#include <inttypes.h>
typedef uint32_t	rpcprog_t;
typedef uint32_t	rpcvers_t;
typedef int32_t		rpc_inline_t;

#define	bool_t	int
#define	enum_t	int
#ifndef FALSE
#  define	FALSE	(0)
#endif
#ifndef TRUE
#  define	TRUE	(1)
#endif
#define __dontcare__	-1
#ifndef NULL
#	define NULL 0
#endif

#define mem_alloc(bsize)	malloc(bsize)
#define mem_free(ptr, bsize)	free(ptr)

/*
 * Attempt to obtain a definition for INADDR_LOOPBACK
 */
#include <netinet/in.h>

#ifndef INADDR_LOOPBACK
#define       INADDR_LOOPBACK         (unsigned long)0x7F000001
#endif

#endif /* ndef __TYPES_RPC_HEADER__ */
