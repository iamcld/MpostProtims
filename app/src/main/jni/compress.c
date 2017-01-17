#include "platform.h"
#include "compress.h"

TLS unsigned char DataBuf[4200] = {0};
TLS unsigned long CharSym[320]  = {0};
TLS unsigned long SymChar[320]  = {0};
TLS unsigned long SymFreq[320]  = {0};
TLS unsigned long SymCum[320]   = {0};
TLS unsigned long PosCum[4100]  = {0};

static unsigned long GetSym(unsigned long x);
static void UpdateNode(unsigned long sym);
static unsigned long GetPos(unsigned long x);

int Decompress(unsigned char *srcbuf, unsigned char *dstbuf, unsigned long srclen, unsigned long *dstlen)
{
	unsigned char ch   = 0, mask = 0;
	unsigned char *srcend = NULL;
	unsigned char *dstend = NULL;
	unsigned char *sp     = NULL;
	unsigned char *dp	  = NULL;
	unsigned long i = 0, r = 0, j = 0, k = 0, c = 0;
	unsigned long low   = 0, high  = 0, sym   = 0, value = 0, range = 0;
	
	ch    = 0;
	mask  = 0;
	low   = 0;
	value = 0;
	high  = 0x20000;
	sp    = srcbuf;
	dp    = dstbuf;

	i = *sp++;
	i <<= 8;
	i += *sp++;
	i <<= 8;
	i += *sp++;
	i <<= 8;
	i += *sp++;
	*dstlen=i;

	srcend = srcbuf + srclen;
	dstend = dstbuf + i;

	for (i = 0; i < 15 + 2; i++)
	{
		value *= 2;

		if ((mask >>= 1) == 0)
		{
			ch = (sp >= srcend) ? 0 : *(sp++);
			mask = 128;
		}

		value += ((ch & mask) != 0);
	}

	SymCum[314] = 0;

	for (k = 314; k >= 1; k--)
	{
		j = k - 1;
		CharSym[j] = k;
		SymChar[k] = j;
		SymFreq[k] = 1;
		SymCum[k - 1] = SymCum[k] + SymFreq[k];
	}

	SymFreq[0]   = 0;
	PosCum[4096] = 0;

	for (i = 4096; i >= 1; i--)
		PosCum[i - 1] = PosCum[i] + 10000 / (i + 200);

	for (i = 0; i < 4096 - 60; i++)
		DataBuf[i] = ' ';

	r = 4096 - 60;

	while (dp < dstend)
	{
		range = high - low;
		sym = GetSym((unsigned long)(((value - low + 1) * SymCum[0] - 1) / range));
		high = low + (range * SymCum[sym - 1]) / SymCum[0];
		low += (range * SymCum[sym]) / SymCum[0];
		while(1)
		{
			if (low >= 0x10000)
			{
				value -= 0x10000;
				low -= 0x10000;
				high -= 0x10000;
			}
			else if (low >= 0x8000 && high <= 0x18000)
			{
				value -= 0x8000;
				low -= 0x8000;
				high -= 0x8000;
			}
			else if (high > 0x10000)
				break;
			low += low;
			high += high;
			value *= 2;
			if ((mask >>= 1) == 0)
			{
				ch = (sp >= srcend) ? 0 : *(sp++);
				mask = 128;
			}
			value += ((ch & mask) != 0);
		}
		c = SymChar[sym];
		UpdateNode(sym);
		if (c < 256)
		{
			if (dp >= dstend)
				return -1;
			*(dp++) = (unsigned char)c;
			DataBuf[r++] = (unsigned char)c;
			r &= 4095;
		}
		else
		{
			j = c - 255 + 2;
			range = high - low;
			i = GetPos((unsigned long)
				(((value - low + 1) * PosCum[0] - 1) / range));
			high = low + (range * PosCum[i    ]) / PosCum[0];
			low += (range * PosCum[i + 1]) / PosCum[0];
			while(1)
			{
				if (low >= 0x10000)
				{
					value -= 0x10000;
					low -= 0x10000;
					high -= 0x10000;
				}
				else if (low >= 0x8000 && high <= 0x18000)
				{
					value -= 0x8000;
					low -= 0x8000;
					high -= 0x8000;
				}
				else if (high > 0x10000)
					break;
				low += low;
				high += high;
				value *= 2;
				if ((mask >>= 1) == 0)
				{
					ch = (sp >= srcend) ? 0 : *(sp++);
					mask = 128;
				}
				value += ((ch & mask) != 0);
			}
			i = (r - i - 1) & 4095;
			for (k = 0; k < j; k++)
			{
				c = DataBuf[(i + k) & 4095];
				if (dp >= dstend)
					return -1;
				*(dp++) = (unsigned char)c;
				DataBuf[r++] = (unsigned char)c;
				r &= 4095;
			}
		}
	}

	return 0;
}

unsigned long GetSym(unsigned long x)
{
	unsigned long i = 0;
	unsigned long j = 0;
	unsigned long k = 0;
	
	i = 1;
	j = 314;
	while(i < j)
	{
		k = (i + j) / 2;
		if(SymCum[k] > x)
		{
			i = k + 1;
		}
		else
		{
			j = k;
		}
	}

	return i;
}

void UpdateNode(unsigned long sym)
{
	unsigned long i = 0;
	unsigned long j = 0;
	unsigned long k = 0;
	unsigned long ch = 0;
	
	if(SymCum[0] >= 0x7FFF)
	{
		j = 0;
		for(i = 314; i > 0; i--)
		{
			SymCum[i] = j;
			j += (SymFreq[i] = (SymFreq[i] + 1) >> 1);
		}
		SymCum[0] = j;
	}

	for(i = sym; SymFreq[i] == SymFreq[i - 1]; i--) ;
	if(i < sym)
	{
		k  = SymChar[i];
		ch = SymChar[sym];
		SymChar[i]   = ch;
		SymChar[sym] = k;
		CharSym[k]  = sym;
		CharSym[ch] = i;
	}
	SymFreq[i]++;
	while(--i > 0)
	{
		SymCum[i]++;
	}

	SymCum[0]++;
	return;
}

unsigned long GetPos(unsigned long x)
{
	unsigned long i = 0;
	unsigned long j = 0;
	unsigned long k = 0;
	
	i = 1;
	j = 4096;
	while (i < j)
	{
		k = (i + j) / 2;
		if (PosCum[k] > x)
			i = k + 1;
		else
			j = k;
	}
	return (i - 1);
}


