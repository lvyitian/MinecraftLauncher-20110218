/*     */ package LZMA;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ class CRangeDecoder
/*     */ {
/*     */   static final int kNumTopBits = 24;
/*     */   static final int kTopValue = 16777216;
/*     */   static final int kTopValueMask = -16777216;
/*     */   static final int kNumBitModelTotalBits = 11;
/*     */   static final int kBitModelTotal = 2048;
/*     */   static final int kNumMoveBits = 5;
/*     */   InputStream inStream;
/*     */   int Range;
/*     */   int Code;
/*     */   byte[] buffer;
/*     */   int buffer_size;
/*     */   int buffer_ind;
/*     */   static final int kNumPosBitsMax = 4;
/*     */   
/*     */   CRangeDecoder(InputStream paramInputStream) throws IOException {
/*  26 */     this.buffer = new byte[16384];
/*  27 */     this.inStream = paramInputStream;
/*  28 */     this.Code = 0;
/*  29 */     this.Range = -1;
/*  30 */     for (byte b = 0; b < 5; b++)
/*  31 */       this.Code = this.Code << 8 | Readbyte(); 
/*     */   }
/*     */   static final int kNumPosStatesMax = 16; static final int kLenNumLowBits = 3; static final int kLenNumLowSymbols = 8; static final int kLenNumMidBits = 3; static final int kLenNumMidSymbols = 8; static final int kLenNumHighBits = 8; static final int kLenNumHighSymbols = 256; static final int LenChoice = 0; static final int LenChoice2 = 1; static final int LenLow = 2; static final int LenMid = 130; static final int LenHigh = 258; static final int kNumLenProbs = 514;
/*     */   int Readbyte() throws IOException {
/*  35 */     if (this.buffer_size == this.buffer_ind) {
/*  36 */       this.buffer_size = this.inStream.read(this.buffer);
/*  37 */       this.buffer_ind = 0;
/*     */       
/*  39 */       if (this.buffer_size < 1)
/*  40 */         throw new LzmaException("LZMA : Data Error"); 
/*     */     } 
/*  42 */     return this.buffer[this.buffer_ind++] & 0xFF;
/*     */   }
/*     */   
/*     */   int DecodeDirectBits(int paramInt) throws IOException {
/*  46 */     int i = 0;
/*  47 */     for (int j = paramInt; j > 0; j--) {
/*  48 */       this.Range >>>= 1;
/*  49 */       int k = this.Code - this.Range >>> 31;
/*  50 */       this.Code -= this.Range & k - 1;
/*  51 */       i = i << 1 | 1 - k;
/*     */       
/*  53 */       if (this.Range < 16777216) {
/*     */         
/*  55 */         this.Code = this.Code << 8 | Readbyte();
/*  56 */         this.Range <<= 8;
/*     */       } 
/*     */     } 
/*  59 */     return i;
/*     */   }
/*     */   
/*     */   int BitDecode(int[] paramArrayOfint, int paramInt) throws IOException {
/*  63 */     int i = (this.Range >>> 11) * paramArrayOfint[paramInt];
/*  64 */     if ((this.Code & 0xFFFFFFFFL) < (i & 0xFFFFFFFFL)) {
/*     */       
/*  66 */       this.Range = i;
/*  67 */       paramArrayOfint[paramInt] = paramArrayOfint[paramInt] + (2048 - paramArrayOfint[paramInt] >>> 5);
/*     */       
/*  69 */       if ((this.Range & 0xFF000000) == 0) {
/*  70 */         this.Code = this.Code << 8 | Readbyte();
/*  71 */         this.Range <<= 8;
/*     */       } 
/*  73 */       return 0;
/*     */     } 
/*  75 */     this.Range -= i;
/*  76 */     this.Code -= i;
/*  77 */     paramArrayOfint[paramInt] = paramArrayOfint[paramInt] - (paramArrayOfint[paramInt] >>> 5);
/*     */     
/*  79 */     if ((this.Range & 0xFF000000) == 0) {
/*  80 */       this.Code = this.Code << 8 | Readbyte();
/*  81 */       this.Range <<= 8;
/*     */     } 
/*  83 */     return 1;
/*     */   }
/*     */ 
/*     */   
/*     */   int BitTreeDecode(int[] paramArrayOfint, int paramInt1, int paramInt2) throws IOException {
/*  88 */     int i = 1;
/*  89 */     for (int j = paramInt2; j > 0; j--) {
/*  90 */       i = i + i + BitDecode(paramArrayOfint, paramInt1 + i);
/*     */     }
/*  92 */     return i - (1 << paramInt2);
/*     */   }
/*     */   
/*     */   int ReverseBitTreeDecode(int[] paramArrayOfint, int paramInt1, int paramInt2) throws IOException {
/*  96 */     int i = 1;
/*  97 */     int j = 0;
/*     */     
/*  99 */     for (byte b = 0; b < paramInt2; b++) {
/* 100 */       int k = BitDecode(paramArrayOfint, paramInt1 + i);
/* 101 */       i = i + i + k;
/* 102 */       j |= k << b;
/*     */     } 
/* 104 */     return j;
/*     */   }
/*     */   
/*     */   byte LzmaLiteralDecode(int[] paramArrayOfint, int paramInt) throws IOException {
/* 108 */     int i = 1;
/*     */     do {
/* 110 */       i = i + i | BitDecode(paramArrayOfint, paramInt + i);
/* 111 */     } while (i < 256);
/*     */     
/* 113 */     return (byte)i;
/*     */   }
/*     */   
/*     */   byte LzmaLiteralDecodeMatch(int[] paramArrayOfint, int paramInt, byte paramByte) throws IOException {
/* 117 */     int i = 1;
/*     */     do {
/* 119 */       int j = paramByte >> 7 & 0x1;
/* 120 */       paramByte = (byte)(paramByte << 1);
/* 121 */       int k = BitDecode(paramArrayOfint, paramInt + (1 + j << 8) + i);
/* 122 */       i = i << 1 | k;
/*     */       
/* 124 */       if (j != k) {
/* 125 */         while (i < 256) {
/* 126 */           i = i + i | BitDecode(paramArrayOfint, paramInt + i);
/*     */         }
/*     */         break;
/*     */       } 
/* 130 */     } while (i < 256);
/*     */     
/* 132 */     return (byte)i;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   int LzmaLenDecode(int[] paramArrayOfint, int paramInt1, int paramInt2) throws IOException {
/* 153 */     if (BitDecode(paramArrayOfint, paramInt1 + 0) == 0) {
/* 154 */       return BitTreeDecode(paramArrayOfint, paramInt1 + 2 + (paramInt2 << 3), 3);
/*     */     }
/*     */     
/* 157 */     if (BitDecode(paramArrayOfint, paramInt1 + 1) == 0) {
/* 158 */       return 8 + BitTreeDecode(paramArrayOfint, paramInt1 + 130 + (paramInt2 << 3), 3);
/*     */     }
/*     */     
/* 161 */     return 16 + BitTreeDecode(paramArrayOfint, paramInt1 + 258, 8);
/*     */   }
/*     */ }


/* Location:              C:\Users\Harley\Desktop\MinecraftLauncher.jar!\LZMA\CRangeDecoder.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       1.1.3
 */