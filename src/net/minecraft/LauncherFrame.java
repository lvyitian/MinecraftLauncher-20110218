/*     */ package net.minecraft;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Component;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Frame;
/*     */ import java.awt.event.WindowAdapter;
/*     */ import java.awt.event.WindowEvent;
/*     */ import java.io.IOException;
/*     */ import java.net.URLEncoder;
/*     */ import javax.imageio.ImageIO;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.UIManager;
/*     */ 
/*     */ public class LauncherFrame extends Frame {
/*     */   public static final int VERSION = 12;
/*     */   private static final long serialVersionUID = 1L;
/*     */   private Launcher launcher;
/*     */   private LoginForm loginForm;
/*     */
/*     */   public LauncherFrame() {
/*  23 */     super("Minecraft Launcher");
/*     */     
/*  25 */     setBackground(Color.BLACK);
/*  26 */     this.loginForm = new LoginForm(this);
/*  27 */     JPanel p = new JPanel();
/*  28 */     p.setLayout(new BorderLayout());
/*  29 */     p.add(this.loginForm, "Center");
/*     */     
/*  31 */     p.setPreferredSize(new Dimension(854, 480));
/*     */     
/*  33 */     setLayout(new BorderLayout());
/*  34 */     add(p, "Center");
/*     */     
/*  36 */     pack();
/*  37 */     setLocationRelativeTo((Component)null);
/*     */     
/*     */     try {
/*  40 */       setIconImage(ImageIO.read(LauncherFrame.class.getResource("favicon.png")));
/*  41 */     } catch (IOException e1) {
/*  42 */       e1.printStackTrace();
/*     */     } 
/*     */     
/*  45 */     addWindowListener(new WindowAdapter() {
/*     */           public void windowClosing(WindowEvent arg0) {
/*  47 */             (new Thread() {
/*     */                 public void run() {
/*     */                   try {
/*  50 */                     Thread.sleep(30000L);
/*  51 */                   } catch (InterruptedException e) {
/*  52 */                     e.printStackTrace();
/*     */                   } 
/*  54 */                   System.out.println("FORCING EXIT!");
/*  55 */                   System.exit(0);
/*     */                 }
/*  58 */               }).start();
/*  59 */             if (LauncherFrame.this.launcher != null) {
/*  60 */               LauncherFrame.this.launcher.stop();
/*  61 */               LauncherFrame.this.launcher.destroy();
/*     */             } 
/*  63 */             System.exit(0);
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   public void playCached(String userName) {
/*     */     try {
/*  70 */       if (userName == null || userName.length() <= 0) {
/*  71 */         userName = "Player";
/*     */       }
/*  73 */       this.launcher = new Launcher();
/*  74 */       this.launcher.customParameters.put("userName", userName);
/*  75 */       this.launcher.init();
/*  76 */       removeAll();
/*  77 */       add(this.launcher, "Center");
/*  78 */       validate();
/*  79 */       this.launcher.start();
/*  80 */       this.loginForm = null;
/*  81 */       setTitle("Minecraft");
/*  82 */     } catch (Exception e) {
/*  83 */       e.printStackTrace();
/*  84 */       showError(e.toString());
/*     */     } 
/*     */   }
/*     */   
/*     */   public void login(String userName, String password) {
/*     */     try {
/*  90 */       String parameters = "user=" + URLEncoder.encode(userName, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8") + "&version=" + '\f';
/*  91 */       String result = Util.excutePost("https://login.minecraft.net/", parameters);
/*  92 */       if (result == null) {
/*  93 */         showError("Can't connect to minecraft.net");
/*  94 */         this.loginForm.setNoNetwork();
/*     */         return;
/*     */       } 
/*  97 */       if (!result.contains(":")) {
/*  98 */         if (result.trim().equals("Bad login")) {
/*  99 */           showError("Login failed");
/* 100 */         } else if (result.trim().equals("Old version")) {
/* 101 */           this.loginForm.setOutdated();
/* 102 */           showError("Outdated launcher");
/*     */         } else {
/* 104 */           showError(result);
/*     */         } 
/* 106 */         this.loginForm.setNoNetwork();
/*     */         return;
/*     */       } 
/* 109 */       String[] values = result.split(":");
/*     */       
/* 111 */       this.launcher = new Launcher();
/* 112 */       this.launcher.customParameters.put("userName", values[2].trim());
/* 113 */       this.launcher.customParameters.put("latestVersion", values[0].trim());
/* 114 */       this.launcher.customParameters.put("downloadTicket", values[1].trim());
/* 115 */       this.launcher.customParameters.put("sessionId", values[3].trim());
/* 116 */       this.launcher.init();
/*     */       
/* 118 */       removeAll();
/* 119 */       add(this.launcher, "Center");
/* 120 */       validate();
/* 121 */       this.launcher.start();
/* 122 */       this.loginForm.loginOk();
/* 123 */       this.loginForm = null;
/* 124 */       setTitle("Minecraft");
/* 125 */     } catch (Exception e) {
/* 126 */       e.printStackTrace();
/* 127 */       showError(e.toString());
/* 128 */       this.loginForm.setNoNetwork();
/*     */     } 
/*     */   }
/*     */   
/*     */   private void showError(String error) {
/* 133 */     removeAll();
/* 134 */     add(this.loginForm);
/* 135 */     this.loginForm.setError(error);
/* 136 */     validate();
/*     */   }
/*     */   
/*     */   public boolean canPlayOffline(String userName) {
/* 140 */     Launcher launcher = new Launcher();
/* 141 */     launcher.init(userName, null, null, null);
/* 142 */     return launcher.canPlayOffline();
/*     */   }
/*     */   
/*     */   public static void main(String[] args) {
/*     */     try {
/* 147 */       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
/* 148 */     } catch (Exception exception) {}
/*     */ 
/*     */     
/* 151 */     LauncherFrame launcherFrame = new LauncherFrame();
/* 152 */     launcherFrame.setVisible(true);
/*     */   }
/*     */ }


/* Location:              C:\Users\Harley\Desktop\Javadecompiler\MinecraftLauncher.jar!\net\minecraft\LauncherFrame.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       1.1.3
 */