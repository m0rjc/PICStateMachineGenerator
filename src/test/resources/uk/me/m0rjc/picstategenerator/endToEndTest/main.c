/****************************************************************
 * PIC part of the end to end test. Test harness for the state
 * machine produced by the generator.
 ****************************************************************/

#include <p18f14k50.h>
#include <string.h>
#include <stdlib.h>
#include <usart.h>
#include "gps.h"

#pragma config WDTEN=OFF  	// disable watchdog timer
#pragma config MCLRE = OFF  // MCLEAR Pin on
#pragma config DEBUG = ON	// Enable Debug Mode
#pragma config LVP = OFF	// Low-Voltage programming disabled (necessary for debugging)
#pragma config CPB = OFF	// Code Protect
#pragma config CP0 = OFF	// Code Protect
#pragma config CP1 = OFF	// Code Protect
//	#pragma config FOSC = IRCCLKOUT;Internal oscillator, clock out on pin 3
// #pragma config FOSC = IRC	// Internal oscillator, pin 3 is IO
#pragma config FOSC = HS    // External clock. 10MHz on my board.
#pragma config HFOFST = OFF	// Start on HFINTOSC immediately
#pragma config PLLEN = OFF	// Do not scale the clock up
#pragma config XINST = OFF	// Do not use extended instructions

#pragma code

void sendTestData(void);
void sendValue(char value);

void main(void)
{
    int count = 0;
    char buffer[20];

    OSCCON = 0x70;	

	gpsInit();

	OpenUSART(USART_TX_INT_OFF  &
              USART_RX_INT_OFF  &
              USART_ASYNCH_MODE &
              USART_EIGHT_BIT   &
              USART_CONT_RX     &
              USART_BRGH_HIGH,
              64 );                  // (10,000,000 / 9,600 / 16) - 1

	while(1)
	{
		// Read a byte. Echo it back to prove to the host that the connection works.
		while (!DataRdyUSART());
		gpsInput = ReadUSART();
		WriteUSART(gpsInput);

		// Step the state machine.
		gpsStep();

		// Respond to the host test harness requesting information.
		if(gpsFlags.TEST_PLEASE_SEND)
		{
			sendTestData();
			gpsFlags.TEST_PLEASE_SEND = 0; // Tell the state machine we've understood.
		}
	}
}

void sendTestData(void)
{
	switch(testSendVar)
	{
		case 1:
			sendValue(testSendIndex);
			break;
		case 2:
			switch(testSendIndex)
			{
				case 0:
					sendValue(gpsFlags.NEW_POSITION ? '1' : '0');
					break;
				case 1:
					sendValue(gpsFlags.NORTH ? 'N' : 'S');
					break;
				case 2:
					sendValue(gpsFlags.EAST ? 'E' : 'W');
					break;
				case 3:
					sendValue(gpsFlags.Test9 ? '1' : '0');
					break;
				default:
					putcUSART('E');
					putcUSART('0');
					putcUSART('1');
			}
			break;
		case 3:
			sendValue(gpsLatitudeDegMin[testSendIndex]);
			break;
		case 4:
			sendValue(gpsLatitudeHundredths[testSendIndex]);
			break;
		case 5:
			sendValue(gpsLongitudeDegMin[testSendIndex]);
			break;
		case 6:
			sendValue(gpsLongitudeHundredths[testSendIndex]);
			break;
		case 7:
			sendValue(gpsTime[testSendIndex]);
			break;
		case 8:
			sendValue(gpsQuality);
			break;
		default:
			putcUSART('E');
			putcUSART('0');
			putcUSART('2');
			sendValue(testSendVar); 
	}
}

void sendValue(char value)
{
	putcUSART('>');
	putcUSART('>');
	putcUSART(value);
	putcUSART('<');
}
