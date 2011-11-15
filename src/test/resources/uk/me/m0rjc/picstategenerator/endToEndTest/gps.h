/*****************************************************************************
 * This file was auto-generated by PICStateGenerator. Do not edit.
 * Generated: Tue Nov 15 20:56:26 GMT 2011
 *****************************************************************************/
#ifndef _GPS_H_
#define _GPS_H_

/*****************************************************************************
 * Initialisation method. Call before first use.
 *****************************************************************************/
extern void gpsInit(void);

/*****************************************************************************
 * Step method.
 * Call to advance state machine after setting input.
 *****************************************************************************/
extern void gpsStep(void);


/*****************************************************************************
 * Access Variables
 *****************************************************************************/
extern struct
{
unsigned NEW_POSITION :1;
unsigned NORTH :1;
unsigned EAST :1;
unsigned TEST_PLEASE_SEND :1;
unsigned Test4 :1;
unsigned Test5 :1;
unsigned Test6 :1;
unsigned Test7 :1;
unsigned Test8 :1;
unsigned Test9 :1;
} gpsFlags;
extern char gpsInput;


/*****************************************************************************
 * Variables for bank gpsBank1
 *****************************************************************************/
#pragma varlocate "gpsBank1" gpsLatitudeDegMin
extern char gpsLatitudeDegMin[];
#pragma varlocate "gpsBank1" gpsLongitudeHundredths
extern char gpsLongitudeHundredths[];
#pragma varlocate "gpsBank1" gpsTime
extern char gpsTime[];
#pragma varlocate "gpsBank1" testSendVar
extern char testSendVar;
#pragma varlocate "gpsBank1" gpsLongitudeDegMin
extern char gpsLongitudeDegMin[];
#pragma varlocate "gpsBank1" gpsQuality
extern char gpsQuality;
#pragma varlocate "gpsBank1" testSendIndex
extern char testSendIndex;
#pragma varlocate "gpsBank1" gpsLatitudeHundredths
extern char gpsLatitudeHundredths[];

#endif
