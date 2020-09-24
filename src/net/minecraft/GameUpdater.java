/*     */ package net.minecraft;
/*     */ 
/*     */ import java.applet.Applet;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.FilePermission;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.io.StringWriter;
/*     */ import java.io.Writer;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.Method;
/*     */ import java.math.BigInteger;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.JarURLConnection;
/*     */ import java.net.SocketPermission;
/*     */ import java.net.URL;
/*     */ import java.net.URLClassLoader;
/*     */ import java.net.URLConnection;
/*     */ import java.security.AccessControlException;
/*     */ import java.security.AccessController;
/*     */ import java.security.CodeSource;
/*     */ import java.security.MessageDigest;
/*     */ import java.security.PermissionCollection;
/*     */ import java.security.PrivilegedExceptionAction;
/*     */ import java.security.SecureClassLoader;
/*     */ import java.security.cert.Certificate;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ import java.util.jar.JarEntry;
/*     */ import java.util.jar.JarFile;
/*     */ import java.util.jar.JarOutputStream;
/*     */ //import java.util.jar.Pack200;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class GameUpdater
/*     */   implements Runnable
/*     */ {
/*     */   public static final int STATE_INIT = 1;
/*     */   public static final int STATE_DETERMINING_PACKAGES = 2;
/*     */   public static final int STATE_CHECKING_CACHE = 3;
/*     */   public static final int STATE_DOWNLOADING = 4;
/*     */   public static final int STATE_EXTRACTING_PACKAGES = 5;
/*     */   public static final int STATE_UPDATING_CLASSPATH = 6;
/*     */   public static final int STATE_SWITCHING_APPLET = 7;
/*     */   public static final int STATE_INITIALIZE_REAL_APPLET = 8;
/*     */   public static final int STATE_START_REAL_APPLET = 9;
/*     */   public static final int STATE_DONE = 10;
/*     */   public int percentage;
/*     */   public int currentSizeDownload;
/*     */   public int totalSizeDownload;
/*     */   public int currentSizeExtract;
/*     */   public int totalSizeExtract;
/*     */   protected URL[] urlList;
/*     */   private static ClassLoader classLoader;
/*     */   protected Thread loaderThread;
/*     */   protected Thread animationThread;
/*     */   public boolean fatalError;
/*     */   public String fatalErrorDescription;
/* 108 */   protected String subtaskMessage = "";
/* 109 */   protected int state = 1;
/*     */   
/*     */   protected boolean lzmaSupported = false;
/*     */   
/*     */   protected boolean pack200Supported = false;
/* 114 */   protected String[] genericErrorMessage = new String[] {
/* 115 */       "An error occured while loading the applet.", "Please contact support to resolve this issue.", "<placeholder for error message>"
/*     */     };
/*     */   
/*     */   protected boolean certificateRefused;
/*     */   
/* 120 */   protected String[] certificateRefusedMessage = new String[] {
/* 121 */       "Permissions for Applet Refused.", "Please accept the permissions dialog to allow", "the applet to continue the loading process."
/*     */     };
/*     */   
/*     */   protected static boolean natives_loaded = false;
/*     */   
/*     */   public static boolean forceUpdate = false;
/*     */   private String latestVersion;
/*     */   private String mainGameUrl;
/*     */   public boolean pauseAskUpdate;
/*     */   public boolean shouldUpdate;
/*     */   
/*     */   public GameUpdater(String latestVersion, String mainGameUrl) {
/* 133 */     this.latestVersion = latestVersion;
/* 134 */     this.mainGameUrl = mainGameUrl;
/*     */   }
/*     */   
/*     */   public void init() {
/* 138 */     this.state = 1;
/*     */     
/*     */     try {
/* 141 */       Class.forName("LZMA.LzmaInputStream");
/* 142 */       this.lzmaSupported = true;
/* 143 */     } catch (Throwable throwable) {}
/*     */ 
/*     */     
/*     *///     try {
/* 147 *///       Pack200.class.getSimpleName();
/* 148 *///       this.pack200Supported = true;
/* 149 *///     } catch (Throwable throwable) {}
/*     */   }
/*     */ 
/*     */   
/*     */   private String generateStacktrace(Exception exception) {
/* 154 */     Writer result = new StringWriter();
/* 155 */     PrintWriter printWriter = new PrintWriter(result);
/* 156 */     exception.printStackTrace(printWriter);
/* 157 */     return result.toString();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected String getDescriptionForState() {
/* 165 */     switch (this.state) {
/*     */       case 1:
/* 167 */         return "Initializing loader";
/*     */       case 2:
/* 169 */         return "Determining packages to load";
/*     */       case 3:
/* 171 */         return "Checking cache for existing files";
/*     */       case 4:
/* 173 */         return "Downloading packages";
/*     */       case 5:
/* 175 */         return "Extracting downloaded packages";
/*     */       case 6:
/* 177 */         return "Updating classpath";
/*     */       case 7:
/* 179 */         return "Switching applet";
/*     */       case 8:
/* 181 */         return "Initializing real applet";
/*     */       case 9:
/* 183 */         return "Starting real applet";
/*     */       case 10:
/* 185 */         return "Done loading";
/*     */     } 
/* 187 */     return "unknown state";
/*     */   }
/*     */ 
/*     */   
/*     */   protected String trimExtensionByCapabilities(String file) {
/* 192 */     if (!this.pack200Supported) {
/* 193 */       file = file.replaceAll(".pack", "");
/*     */     }
/*     */     
/* 196 */     if (!this.lzmaSupported) {
/* 197 */       file = file.replaceAll(".lzma", "");
/*     */     }
/* 199 */     return file;
/*     */   }
/*     */   
/*     */   protected void loadJarURLs() throws Exception {
/* 203 */     this.state = 2;
/* 204 */     String jarList = "lwjgl.jar, jinput.jar, lwjgl_util.jar, " + this.mainGameUrl;
/* 205 */     jarList = trimExtensionByCapabilities(jarList);
/*     */     
/* 207 */     StringTokenizer jar = new StringTokenizer(jarList, ", ");
/* 208 */     int jarCount = jar.countTokens() + 1;
/*     */     
/* 210 */     this.urlList = new URL[jarCount];
/*     */     
/* 212 */     URL path = new URL("http://s3.amazonaws.com/MinecraftDownload/");
/*     */     
/* 214 */     for (int i = 0; i < jarCount - 1; i++) {
/* 215 */       this.urlList[i] = new URL(path, jar.nextToken());
/*     */     }
/*     */     
/* 218 */     String osName = System.getProperty("os.name");
/* 219 */     String nativeJar = null;
/*     */     
/* 221 */     if (osName.startsWith("Win")) {
/* 222 */       nativeJar = "windows_natives.jar.lzma";
/* 223 */     } else if (osName.startsWith("Linux")) {
/* 224 */       nativeJar = "linux_natives.jar.lzma";
/* 225 */     } else if (osName.startsWith("Mac")) {
/* 226 */       nativeJar = "macosx_natives.jar.lzma";
/* 227 */     } else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
/* 228 */       nativeJar = "solaris_natives.jar.lzma";
/*     */     } else {
/* 230 */       fatalErrorOccured("OS (" + osName + ") not supported", null);
/*     */     } 
/*     */     
/* 233 */     if (nativeJar == null) {
/* 234 */       fatalErrorOccured("no lwjgl natives files found", null);
/*     */     } else {
/* 236 */       nativeJar = trimExtensionByCapabilities(nativeJar);
/* 237 */       this.urlList[jarCount - 1] = new URL(path, nativeJar);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/* 243 */     init();
/* 244 */     this.state = 3;
/*     */     
/* 246 */     this.percentage = 5;
/*     */     
/*     */     try {
/* 249 */       loadJarURLs();
/*     */       
/* 251 */       String path = AccessController.<String>doPrivileged(new PrivilegedExceptionAction() {
/*     */             public Object run() throws Exception {
/* 253 */               return Util.getWorkingDirectory() + File.separator + "bin" + File.separator;
/*     */             }
/*     */           });
/*     */       
/* 257 */       File dir = new File(path);
/*     */       
/* 259 */       if (!dir.exists()) {
/* 260 */         dir.mkdirs();
/*     */       }
/*     */       
/* 263 */       if (this.latestVersion != null) {
/* 264 */         File versionFile = new File(dir, "version");
/*     */         
/* 266 */         boolean cacheAvailable = false;
/* 267 */         if (!forceUpdate && versionFile.exists() && (
/* 268 */           this.latestVersion.equals("-1") || this.latestVersion.equals(readVersionFile(versionFile)))) {
/* 269 */           cacheAvailable = true;
/* 270 */           this.percentage = 90;
/*     */         } 
/*     */ 
/*     */         
/* 274 */         if (forceUpdate || !cacheAvailable) {
/* 275 */           this.shouldUpdate = true;
/* 276 */           if (!forceUpdate && versionFile.exists())
/*     */           {
/*     */             
/* 279 */             checkShouldUpdate();
/*     */           }
/* 281 */           if (this.shouldUpdate) {
/*     */ 
/*     */             
/* 284 */             writeVersionFile(versionFile, "");
/*     */             
/* 286 */             downloadJars(path);
/* 287 */             extractJars(path);
/* 288 */             extractNatives(path);
/*     */             
/* 290 */             if (this.latestVersion != null) {
/* 291 */               this.percentage = 90;
/* 292 */               writeVersionFile(versionFile, this.latestVersion);
/*     */             } 
/*     */           } else {
/* 295 */             cacheAvailable = true;
/* 296 */             this.percentage = 90;
/*     */           } 
/*     */         } 
/*     */       } 
/*     */       
/* 301 */       updateClassPath(dir);
/* 302 */       this.state = 10;
/* 303 */     } catch (AccessControlException ace) {
/* 304 */       fatalErrorOccured(ace.getMessage(), ace);
/* 305 */       this.certificateRefused = true;
/* 306 */     } catch (Exception e) {
/* 307 */       fatalErrorOccured(e.getMessage(), e);
/*     */     } finally {
/* 309 */       this.loaderThread = null;
/*     */     } 
/*     */   }
/*     */   
/*     */   private void checkShouldUpdate() {
/* 314 */     this.pauseAskUpdate = true;
/* 315 */     while (this.pauseAskUpdate) {
/*     */       try {
/* 317 */         Thread.sleep(1000L);
/* 318 */       } catch (InterruptedException e) {
/* 319 */         e.printStackTrace();
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   protected String readVersionFile(File file) throws Exception {
/* 325 */     DataInputStream dis = new DataInputStream(new FileInputStream(file));
/* 326 */     String version = dis.readUTF();
/* 327 */     dis.close();
/* 328 */     return version;
/*     */   }
/*     */   
/*     */   protected void writeVersionFile(File file, String version) throws Exception {
/* 332 */     DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
/* 333 */     dos.writeUTF(version);
/* 334 */     dos.close();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected void updateClassPath(File dir) throws Exception {
/* 340 */     this.state = 6;
/*     */     
/* 342 */     this.percentage = 95;
/*     */     
/* 344 */     URL[] urls = new URL[this.urlList.length];
/* 345 */     for (int i = 0; i < this.urlList.length; i++) {
/* 346 */       urls[i] = (new File(dir, getJarName(this.urlList[i]))).toURI().toURL();
/*     */     }
/*     */     
/* 349 */     if (classLoader == null) {
/* 350 */       classLoader = new URLClassLoader(urls) {
/*     */           protected PermissionCollection getPermissions(CodeSource codesource) {
/* 352 */             PermissionCollection perms = null;
/*     */ 
/*     */ 
/*     */             
/*     */             try {
/* 357 */               Method method = SecureClassLoader.class.getDeclaredMethod("getPermissions", new Class[] {
/* 358 */                     CodeSource.class
/*     */                   });
/* 360 */               method.setAccessible(true);
/* 361 */               perms = (PermissionCollection)method.invoke(getClass().getClassLoader(), new Object[] {
/* 362 */                     codesource
/*     */                   });
/*     */               
/* 365 */               String host = "www.minecraft.net";
/*     */               
/* 367 */               if (host != null && host.length() > 0)
/*     */               
/*     */               { 
/* 370 */                 perms.add(new SocketPermission(host, "connect,accept")); }
/* 371 */               else { codesource.getLocation().getProtocol().equals("file"); }
/*     */ 
/*     */               
/* 374 */               perms.add(new FilePermission("<<ALL FILES>>", "read"));
/*     */             }
/* 376 */             catch (Exception e) {
/* 377 */               e.printStackTrace();
/*     */             } 
/*     */             
/* 380 */             return perms;
/*     */           }
/*     */         };
/*     */     }
/*     */     
/* 385 */     String path = dir.getAbsolutePath();
/* 386 */     if (!path.endsWith(File.separator)) path = String.valueOf(path) + File.separator; 
/* 387 */     unloadNatives(path);
/*     */     
/* 389 */     System.setProperty("org.lwjgl.librarypath", String.valueOf(path) + "natives");
/* 390 */     System.setProperty("net.java.games.input.librarypath", String.valueOf(path) + "natives");
/*     */     
/* 392 */     natives_loaded = true;
/*     */   }
/*     */ 
/*     */   
/*     */   private void unloadNatives(String nativePath) {
/* 397 */     if (!natives_loaded) {
/*     */       return;
/*     */     }
/*     */     
/*     */     try {
/* 402 */       Field field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
/* 403 */       field.setAccessible(true);
/* 404 */       Vector<String> libs = (Vector<String>)field.get(getClass().getClassLoader());
/*     */       
/* 406 */       String path = (new File(nativePath)).getCanonicalPath();
/*     */       
/* 408 */       for (int i = 0; i < libs.size(); i++) {
/* 409 */         String s = libs.get(i);
/*     */         
/* 411 */         if (s.startsWith(path)) {
/* 412 */           libs.remove(i);
/* 413 */           i--;
/*     */         } 
/*     */       } 
/* 416 */     } catch (Exception e) {
/* 417 */       e.printStackTrace();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public Applet createApplet() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
/* 423 */     Class<Applet> appletClass = (Class)classLoader.loadClass("net.minecraft.client.MinecraftApplet");
/* 424 */     return appletClass.newInstance();
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
/*     */   protected void downloadJars(String path) throws Exception {
/* 441 */     File versionFile = new File(path, "md5s");
/* 442 */     Properties md5s = new Properties();
/* 443 */     if (versionFile.exists()) {
/*     */       try {
/* 445 */         FileInputStream fis = new FileInputStream(versionFile);
/* 446 */         md5s.load(fis);
/* 447 */         fis.close();
/* 448 */       } catch (Exception e) {
/* 449 */         e.printStackTrace();
/*     */       } 
/*     */     }
/* 452 */     this.state = 4;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 457 */     int[] fileSizes = new int[this.urlList.length];
/* 458 */     boolean[] skip = new boolean[this.urlList.length];
/*     */ 
/*     */     
/* 461 */     for (int i = 0; i < this.urlList.length; i++) {
/* 462 */       URLConnection urlconnection = this.urlList[i].openConnection();
/* 463 */       urlconnection.setDefaultUseCaches(false);
/* 464 */       skip[i] = false;
/* 465 */       if (urlconnection instanceof HttpURLConnection) {
/* 466 */         ((HttpURLConnection)urlconnection).setRequestMethod("HEAD");
/*     */         
/* 468 */         String etagOnDisk = "\"" + md5s.getProperty(getFileName(this.urlList[i])) + "\"";
/*     */         
/* 470 */         if (!forceUpdate && etagOnDisk != null) urlconnection.setRequestProperty("If-None-Match", etagOnDisk);
/*     */         
/* 472 */         int code = ((HttpURLConnection)urlconnection).getResponseCode();
/* 473 */         if (code / 100 == 3) {
/* 474 */           skip[i] = true;
/*     */         }
/*     */       } 
/* 477 */       fileSizes[i] = urlconnection.getContentLength();
/* 478 */       this.totalSizeDownload += fileSizes[i];
/*     */     } 
/*     */ 
/*     */     
/* 482 */     int initialPercentage = this.percentage = 10;
/*     */ 
/*     */     
/* 485 */     byte[] buffer = new byte[65536];
/* 486 */     for (int j = 0; j < this.urlList.length; j++) {
/* 487 */       if (skip[j]) {
/* 488 */         this.percentage = initialPercentage + fileSizes[j] * 45 / this.totalSizeDownload;
/*     */       } else {
/*     */ 
/*     */         
/*     */         try {
/*     */ 
/*     */           
/* 495 */           md5s.remove(getFileName(this.urlList[j]));
/* 496 */           md5s.store(new FileOutputStream(versionFile), "md5 hashes for downloaded files");
/* 497 */         } catch (Exception e) {
/* 498 */           e.printStackTrace();
/*     */         } 
/*     */         
/* 501 */         int unsuccessfulAttempts = 0;
/* 502 */         int maxUnsuccessfulAttempts = 3;
/* 503 */         boolean downloadFile = true;
/*     */ 
/*     */         
/* 506 */         while (downloadFile) {
/* 507 */           downloadFile = false;
/*     */           
/* 509 */           URLConnection urlconnection = this.urlList[j].openConnection();
/*     */           
/* 511 */           String etag = "";
/*     */           
/* 513 */           if (urlconnection instanceof HttpURLConnection) {
/* 514 */             urlconnection.setRequestProperty("Cache-Control", "no-cache");
/*     */             
/* 516 */             urlconnection.connect();
/*     */ 
/*     */             
/* 519 */             etag = urlconnection.getHeaderField("ETag");
/* 520 */             etag = etag.substring(1, etag.length() - 1);
/*     */           } 
/*     */           
/* 523 */           String currentFile = getFileName(this.urlList[j]);
/* 524 */           InputStream inputstream = getJarInputStream(currentFile, urlconnection);
/* 525 */           FileOutputStream fos = new FileOutputStream(String.valueOf(path) + currentFile);
/*     */ 
/*     */ 
/*     */           
/* 529 */           long downloadStartTime = System.currentTimeMillis();
/* 530 */           int downloadedAmount = 0;
/* 531 */           int fileSize = 0;
/* 532 */           String downloadSpeedMessage = "";
/*     */           
/* 534 */           MessageDigest m = MessageDigest.getInstance("MD5"); int bufferSize;
/* 535 */           while ((bufferSize = inputstream.read(buffer, 0, buffer.length)) != -1) {
/* 536 */             fos.write(buffer, 0, bufferSize);
/* 537 */             m.update(buffer, 0, bufferSize);
/* 538 */             this.currentSizeDownload += bufferSize;
/* 539 */             fileSize += bufferSize;
/* 540 */             this.percentage = initialPercentage + this.currentSizeDownload * 45 / this.totalSizeDownload;
/* 541 */             this.subtaskMessage = "Retrieving: " + currentFile + " " + (this.currentSizeDownload * 100 / this.totalSizeDownload) + "%";
/*     */             
/* 543 */             downloadedAmount += bufferSize;
/* 544 */             long timeLapse = System.currentTimeMillis() - downloadStartTime;
/*     */             
/* 546 */             if (timeLapse >= 1000L) {
/* 547 */               float downloadSpeed = downloadedAmount / (float)timeLapse;
/* 548 */               downloadSpeed = (int)(downloadSpeed * 100.0F) / 100.0F;
/* 549 */               downloadSpeedMessage = " @ " + downloadSpeed + " KB/sec";
/* 550 */               downloadedAmount = 0;
/* 551 */               downloadStartTime += 1000L;
/*     */             } 
/*     */             
/* 554 */             this.subtaskMessage = String.valueOf(this.subtaskMessage) + downloadSpeedMessage;
/*     */           } 
/*     */           
/* 557 */           inputstream.close();
/* 558 */           fos.close();
/* 559 */           String md5 = (new BigInteger(1, m.digest())).toString(16);
/* 560 */           while (md5.length() < 32) {
/* 561 */             md5 = "0" + md5;
/*     */           }
/* 563 */           boolean md5Matches = true;
/* 564 */           if (etag != null) {
/* 565 */             md5Matches = md5.equals(etag);
/*     */           }
/*     */           
/* 568 */           if (urlconnection instanceof HttpURLConnection) {
/* 569 */             if (md5Matches && (fileSize == fileSizes[j] || fileSizes[j] <= 0)) {
/*     */               
/*     */               try {
/* 572 */                 md5s.setProperty(getFileName(this.urlList[j]), etag);
/* 573 */                 md5s.store(new FileOutputStream(versionFile), "md5 hashes for downloaded files");
/* 574 */               } catch (Exception e) {
/* 575 */                 e.printStackTrace();
/*     */               }  continue;
/*     */             } 
/* 578 */             unsuccessfulAttempts++;
/* 579 */             if (unsuccessfulAttempts < maxUnsuccessfulAttempts) {
/* 580 */               downloadFile = true;
/* 581 */               this.currentSizeDownload -= fileSize; continue;
/*     */             } 
/* 583 */             throw new Exception("failed to download " + currentFile);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 591 */     this.subtaskMessage = "";
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
/*     */   protected InputStream getJarInputStream(String currentFile, final URLConnection urlconnection) throws Exception {
/* 603 */     final InputStream[] is = new InputStream[1];
/*     */ 
/*     */ 
/*     */     
/* 607 */     for (int j = 0; j < 3 && is[0] == null; j++) {
/* 608 */       Thread t = new Thread() {
/*     */           public void run() {
/*     */             try {
/* 611 */               is[0] = urlconnection.getInputStream();
/* 612 */             } catch (IOException iOException) {}
/*     */           }
/*     */         };
/*     */ 
/*     */       
/* 617 */       t.setName("JarInputStreamThread");
/* 618 */       t.start();
/*     */       
/* 620 */       int iterationCount = 0;
/* 621 */       while (is[0] == null && iterationCount++ < 5) {
/*     */         try {
/* 623 */           t.join(1000L);
/* 624 */         } catch (InterruptedException interruptedException) {}
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 629 */       if (is[0] == null) {
/*     */         try {
/* 631 */           t.interrupt();
/* 632 */           t.join();
/* 633 */         } catch (InterruptedException interruptedException) {}
/*     */       }
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 639 */     if (is[0] == null) {
/* 640 */       if (currentFile.equals("minecraft.jar")) {
/* 641 */         throw new Exception("Unable to download " + currentFile);
/*     */       }
/* 643 */       throw new Exception("Unable to download " + currentFile);
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 648 */     return is[0];
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
/*     */   protected void extractLZMA(String in, String out) throws Exception {
/* 663 */     File f = new File(in);
/* 664 */     if (!f.exists())
/* 665 */       return;  FileInputStream fileInputHandle = new FileInputStream(f);
/*     */ 
/*     */     
/* 668 */     Class<?> clazz = Class.forName("LZMA.LzmaInputStream");
/* 669 */     Constructor<?> constructor = clazz.getDeclaredConstructor(new Class[] {
/* 670 */           InputStream.class
/*     */         });
/* 672 */     InputStream inputHandle = (InputStream)constructor.newInstance(new Object[] {
/* 673 */           fileInputHandle
/*     */         });
/*     */ 
/*     */     
/* 677 */     OutputStream outputHandle = new FileOutputStream(out);
/*     */     
/* 679 */     byte[] buffer = new byte[16384];
/*     */     
/* 681 */     int ret = inputHandle.read(buffer);
/* 682 */     while (ret >= 1) {
/* 683 */       outputHandle.write(buffer, 0, ret);
/* 684 */       ret = inputHandle.read(buffer);
/*     */     } 
/*     */     
/* 687 */     inputHandle.close();
/* 688 */     outputHandle.close();
/*     */     
/* 690 */     outputHandle = null;
/* 691 */     inputHandle = null;
/*     */ 
/*     */     
/* 694 */     f.delete();
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
/*     */   protected void extractPack(String in, String out) throws Exception {
/* 708 */     File f = new File(in);
/* 709 */     if (!f.exists())
/*     */       return; 
/* 711 */     FileOutputStream fostream = new FileOutputStream(out);
/* 712 */     JarOutputStream jostream = new JarOutputStream(fostream);
/*     */     
/* 714 *///     Pack200.Unpacker unpacker = Pack200.newUnpacker();
/* 715 *///     unpacker.unpack(f, jostream);
/* 716 *///     jostream.close();
/*     */ 
/*     */     
/* 719 */     f.delete();
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
/*     */   protected void extractJars(String path) throws Exception {
/* 731 */     this.state = 5;
/*     */     
/* 733 */     float increment = 10.0F / this.urlList.length;
/*     */     
/* 735 */     for (int i = 0; i < this.urlList.length; i++) {
/* 736 */       this.percentage = 55 + (int)(increment * (i + 1));
/* 737 */       String filename = getFileName(this.urlList[i]);
/*     */       
/* 739 */       if (filename.endsWith(".pack.lzma")) {
/* 740 */         this.subtaskMessage = "Extracting: " + filename + " to " + filename.replaceAll(".lzma", "");
/* 741 */         extractLZMA(String.valueOf(path) + filename, String.valueOf(path) + filename.replaceAll(".lzma", ""));
/*     */         
/* 743 */         this.subtaskMessage = "Extracting: " + filename.replaceAll(".lzma", "") + " to " + filename.replaceAll(".pack.lzma", "");
/* 744 */         extractPack(String.valueOf(path) + filename.replaceAll(".lzma", ""), String.valueOf(path) + filename.replaceAll(".pack.lzma", ""));
/* 745 */       } else if (filename.endsWith(".pack")) {
/* 746 */         this.subtaskMessage = "Extracting: " + filename + " to " + filename.replace(".pack", "");
/* 747 */         extractPack(String.valueOf(path) + filename, String.valueOf(path) + filename.replace(".pack", ""));
/* 748 */       } else if (filename.endsWith(".lzma")) {
/* 749 */         this.subtaskMessage = "Extracting: " + filename + " to " + filename.replace(".lzma", "");
/* 750 */         extractLZMA(String.valueOf(path) + filename, String.valueOf(path) + filename.replace(".lzma", ""));
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   protected void extractNatives(String path) throws Exception {
/* 757 */     this.state = 5;
/*     */     
/* 759 */     int initialPercentage = this.percentage;
/*     */     
/* 761 */     String nativeJar = getJarName(this.urlList[this.urlList.length - 1]);
/*     */     
/* 763 */     Certificate[] certificate = Launcher.class.getProtectionDomain().getCodeSource().getCertificates();
/*     */     
/* 765 */     if (certificate == null) {
/* 766 */       URL location = Launcher.class.getProtectionDomain().getCodeSource().getLocation();
/*     */       
/* 768 */       JarURLConnection jurl = (JarURLConnection)(new URL("jar:" + location.toString() + "!/net/minecraft/Launcher.class")).openConnection();
/* 769 */       jurl.setDefaultUseCaches(true);
/*     */       try {
/* 771 */         certificate = jurl.getCertificates();
/* 772 */       } catch (Exception exception) {}
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 777 */     File nativeFolder = new File(String.valueOf(path) + "natives");
/* 778 */     if (!nativeFolder.exists()) {
/* 779 */       nativeFolder.mkdir();
/*     */     }
/*     */     
/* 782 */     File file = new File(String.valueOf(path) + nativeJar);
/* 783 */     if (!file.exists())
/* 784 */       return;  JarFile jarFile = new JarFile(file, true);
/* 785 */     Enumeration<JarEntry> entities = jarFile.entries();
/*     */     
/* 787 */     this.totalSizeExtract = 0;
/*     */ 
/*     */     
/* 790 */     while (entities.hasMoreElements()) {
/* 791 */       JarEntry entry = entities.nextElement();
/*     */ 
/*     */ 
/*     */       
/* 795 */       if (entry.isDirectory() || entry.getName().indexOf('/') != -1) {
/*     */         continue;
/*     */       }
/* 798 */       this.totalSizeExtract = (int)(this.totalSizeExtract + entry.getSize());
/*     */     } 
/*     */     
/* 801 */     this.currentSizeExtract = 0;
/*     */     
/* 803 */     entities = jarFile.entries();
/*     */     
/* 805 */     while (entities.hasMoreElements()) {
/* 806 */       JarEntry entry = entities.nextElement();
/*     */       
/* 808 */       if (entry.isDirectory() || entry.getName().indexOf('/') != -1) {
/*     */         continue;
/*     */       }
/*     */       
/* 812 */       File file1 = new File(String.valueOf(path) + "natives" + File.separator + entry.getName());
/* 813 */       if (file1.exists() && 
/* 814 */         !file1.delete()) {
/*     */         continue;
/*     */       }
/*     */ 
/*     */ 
/*     */       
/* 820 */       InputStream in = jarFile.getInputStream(jarFile.getEntry(entry.getName()));
/* 821 */       OutputStream out = new FileOutputStream(String.valueOf(path) + "natives" + File.separator + entry.getName());
/*     */ 
/*     */       
/* 824 */       byte[] buffer = new byte[65536];
/*     */       int bufferSize;
/* 826 */       while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
/* 827 */         out.write(buffer, 0, bufferSize);
/* 828 */         this.currentSizeExtract += bufferSize;
/*     */         
/* 830 */         this.percentage = initialPercentage + this.currentSizeExtract * 20 / this.totalSizeExtract;
/* 831 */         this.subtaskMessage = "Extracting: " + entry.getName() + " " + (this.currentSizeExtract * 100 / this.totalSizeExtract) + "%";
/*     */       } 
/*     */       
/* 834 */       validateCertificateChain(certificate, entry.getCertificates());
/*     */       
/* 836 */       in.close();
/* 837 */       out.close();
/*     */     } 
/* 839 */     this.subtaskMessage = "";
/*     */     
/* 841 */     jarFile.close();
/*     */     
/* 843 */     File f = new File(String.valueOf(path) + nativeJar);
/* 844 */     f.delete();
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
/*     */   protected static void validateCertificateChain(Certificate[] ownCerts, Certificate[] native_certs) throws Exception {
/* 856 */     if (ownCerts == null)
/* 857 */       return;  if (native_certs == null) throw new Exception("Unable to validate certificate chain. Native entry did not have a certificate chain at all");
/*     */     
/* 859 */     if (ownCerts.length != native_certs.length) throw new Exception("Unable to validate certificate chain. Chain differs in length [" + ownCerts.length + " vs " + native_certs.length + "]");
/*     */     
/* 861 */     for (int i = 0; i < ownCerts.length; i++) {
/* 862 */       if (!ownCerts[i].equals(native_certs[i])) {
/* 863 */         throw new Exception("Certificate mismatch: " + ownCerts[i] + " != " + native_certs[i]);
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   protected String getJarName(URL url) {
/* 869 */     String fileName = url.getFile();
/*     */     
/* 871 */     if (fileName.contains("?")) {
/* 872 */       fileName = fileName.substring(0, fileName.indexOf("?"));
/*     */     }
/* 874 */     if (fileName.endsWith(".pack.lzma")) {
/* 875 */       fileName = fileName.replaceAll(".pack.lzma", "");
/* 876 */     } else if (fileName.endsWith(".pack")) {
/* 877 */       fileName = fileName.replaceAll(".pack", "");
/* 878 */     } else if (fileName.endsWith(".lzma")) {
/* 879 */       fileName = fileName.replaceAll(".lzma", "");
/*     */     } 
/*     */     
/* 882 */     return fileName.substring(fileName.lastIndexOf('/') + 1);
/*     */   }
/*     */   
/*     */   protected String getFileName(URL url) {
/* 886 */     String fileName = url.getFile();
/* 887 */     if (fileName.contains("?")) {
/* 888 */       fileName = fileName.substring(0, fileName.indexOf("?"));
/*     */     }
/* 890 */     return fileName.substring(fileName.lastIndexOf('/') + 1);
/*     */   }
/*     */   
/*     */   protected void fatalErrorOccured(String error, Exception e) {
/* 894 */     e.printStackTrace();
/* 895 */     this.fatalError = true;
/* 896 */     this.fatalErrorDescription = "Fatal error occured (" + this.state + "): " + error;
/* 897 */     System.out.println(this.fatalErrorDescription);
/*     */     
/* 899 */     System.out.println(generateStacktrace(e));
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean canPlayOffline() {
/*     */     try {
/* 906 */       String path = AccessController.<String>doPrivileged(new PrivilegedExceptionAction() {
/*     */             public Object run() throws Exception {
/* 908 */               return Util.getWorkingDirectory() + File.separator + "bin" + File.separator;
/*     */             }
/*     */           });
/*     */       
/* 912 */       File dir = new File(path);
/* 913 */       if (!dir.exists()) return false;
/*     */       
/* 915 */       dir = new File(dir, "version");
/* 916 */       if (!dir.exists()) return false;
/*     */       
/* 918 */       if (dir.exists()) {
/* 919 */         String version = readVersionFile(dir);
/* 920 */         if (version != null && version.length() > 0) {
/* 921 */           return true;
/*     */         }
/*     */       } 
/* 924 */     } catch (Exception e) {
/* 925 */       e.printStackTrace();
/* 926 */       return false;
/*     */     } 
/* 928 */     return false;
/*     */   }
/*     */ }


/* Location:              C:\Users\Harley\Desktop\Javadecompiler\MinecraftLauncher.jar!\net\minecraft\GameUpdater.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       1.1.3
 */