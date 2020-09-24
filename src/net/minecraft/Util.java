/*     */ package net.minecraft;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.net.URL;
/*     */ import java.security.PublicKey;
/*     */ import java.security.cert.Certificate;
/*     */ import javax.net.ssl.HttpsURLConnection;
/*     */ 
/*     */ public class Util
/*     */ {
/*     */   private enum OS {
/*  17 */     linux, solaris, windows, macos, unknown;
/*     */   }
/*     */   
/*  20 */   private static File workDir = null;
/*     */   
/*     */   public static File getWorkingDirectory() {
/*  23 */     if (workDir == null) workDir = getWorkingDirectory("minecraft"); 
/*  24 */     return workDir;
/*     */   }
/*     */   public static File getWorkingDirectory(String applicationName) {
/*     */     File workingDirectory;
/*  28 */     String applicationData, userHome = System.getProperty("user.home", ".");
/*     */     
/*  30 */     switch (getPlatform()) {
/*     */       case solaris:
/*  33 */         workingDirectory = new File(userHome, String.valueOf('.') + applicationName + '/');
/*     */         break;
/*     */       case windows:
/*  36 */         applicationData = System.getenv("APPDATA");
/*  37 */         if (applicationData != null) { workingDirectory = new File(applicationData, "." + applicationName + '/'); break; }
/*  38 */          workingDirectory = new File(userHome, String.valueOf('.') + applicationName + '/');
/*     */         break;
/*     */       case macos:
/*  41 */         workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
/*     */         break;
/*     */       default:
/*  44 */         workingDirectory = new File(userHome, String.valueOf(applicationName) + '/'); break;
/*     */     } 
/*  46 */     if (!workingDirectory.exists() && !workingDirectory.mkdirs()) throw new RuntimeException("The working directory could not be created: " + workingDirectory); 
/*  47 */     return workingDirectory;
/*     */   }
/*     */   
/*     */   private static OS getPlatform() {
/*  51 */     String osName = System.getProperty("os.name").toLowerCase();
/*  52 */     if (osName.contains("win")) return OS.windows; 
/*  53 */     if (osName.contains("mac")) return OS.macos; 
/*  54 */     if (osName.contains("solaris")) return OS.solaris; 
/*  55 */     if (osName.contains("sunos")) return OS.solaris; 
/*  56 */     if (osName.contains("linux")) return OS.linux; 
/*  57 */     if (osName.contains("unix")) return OS.linux; 
/*  58 */     return OS.unknown;
/*     */   }
/*     */ 
/*     */   
/*     */   public static String excutePost(String targetURL, String urlParameters) {
/*  63 */     HttpsURLConnection connection = null;
/*     */     
/*     */     try {
/*  66 */       URL url = new URL(targetURL);
/*  67 */       connection = (HttpsURLConnection)url.openConnection();
/*  68 */       connection.setRequestMethod("POST");
/*  69 */       connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
/*     */       
/*  71 */       connection.setRequestProperty("Content-Length", Integer.toString((urlParameters.getBytes()).length));
/*  72 */       connection.setRequestProperty("Content-Language", "en-US");
/*     */       
/*  74 */       connection.setUseCaches(false);
/*  75 */       connection.setDoInput(true);
/*  76 */       connection.setDoOutput(true);
/*     */ 
/*     */       
/*  79 */       connection.connect();
/*  80 */       Certificate[] certs = connection.getServerCertificates();
/*     */       
/*  82 */       byte[] bytes = new byte[294];
/*  83 */       DataInputStream dis = new DataInputStream(Util.class.getResourceAsStream("minecraft.key"));
/*  84 */       dis.readFully(bytes);
/*  85 */       dis.close();
/*     */       
/*  87 */       Certificate c = certs[0];
/*  88 */       PublicKey pk = c.getPublicKey();
/*  89 */       byte[] data = pk.getEncoded();
/*     */       
/*  91 */       for (int i = 0; i < data.length; i++) {
/*  92 */         if (data[i] != bytes[i]) throw new RuntimeException("Public key mismatch");
/*     */       
/*     */       } 
/*     */       
/*  96 */       DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
/*  97 */       wr.writeBytes(urlParameters);
/*  98 */       wr.flush();
/*  99 */       wr.close();
/*     */ 
/*     */       
/* 102 */       InputStream is = connection.getInputStream();
/* 103 */       BufferedReader rd = new BufferedReader(new InputStreamReader(is));
/*     */       
/* 105 */       StringBuffer response = new StringBuffer(); String line;
/* 106 */       while ((line = rd.readLine()) != null) {
/* 107 */         response.append(line);
/* 108 */         response.append('\r');
/*     */       } 
/* 110 */       rd.close();
/*     */ 
/*     */ 
/*     */       
/* 114 */       return response.toString();
/*     */     }
/* 116 */     catch (Exception e) {
/*     */       
/* 118 */       e.printStackTrace();
/* 119 */       return null;
/*     */     }
/*     */     finally {
/*     */       
/* 123 */       if (connection != null)
/* 124 */         connection.disconnect(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\Harley\Desktop\Javadecompiler\MinecraftLauncher.jar!\net\minecraft\Util.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       1.1.3
 */