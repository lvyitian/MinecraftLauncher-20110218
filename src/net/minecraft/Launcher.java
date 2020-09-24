/*     */ package net.minecraft;
/*     */ 
/*     */ import java.applet.Applet;
/*     */ import java.applet.AppletStub;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Image;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.awt.event.MouseListener;
/*     */ import java.awt.image.VolatileImage;
/*     */ import java.io.IOException;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import javax.imageio.ImageIO;
/*     */ 
/*     */ public class Launcher
/*     */   extends Applet implements Runnable, AppletStub, MouseListener {
/*     */   private static final long serialVersionUID = 1L;
/*  24 */   public Map<String, String> customParameters = new HashMap<String, String>();
/*     */   
/*     */   private GameUpdater gameUpdater;
/*     */   private boolean gameUpdaterStarted = false;
/*     */   private Applet applet;
/*     */   private Image bgImage;
/*     */   private boolean active = false;
/*  31 */   private int context = 0;
/*     */   
/*     */   private boolean hasMouseListener = false;
/*     */   
/*     */   private VolatileImage img;
/*     */   
/*     */   public boolean isActive() {
/*  38 */     if (this.context == 0) {
/*  39 */       this.context = -1;
/*     */       try {
/*  41 */         if (getAppletContext() != null) this.context = 1; 
/*  42 */       } catch (Exception exception) {}
/*     */     } 
/*     */     
/*  45 */     if (this.context == -1) return this.active; 
/*  46 */     return super.isActive();
/*     */   }
/*     */ 
/*     */   
/*     */   public void init(String userName, String latestVersion, String downloadTicket, String sessionId) {
/*     */     try {
/*  52 */       this.bgImage = ImageIO.read(LoginForm.class.getResource("dirt.png")).getScaledInstance(32, 32, 16);
/*  53 */     } catch (IOException e) {
/*  54 */       e.printStackTrace();
/*     */     } 
/*     */     
/*  57 */     this.customParameters.put("username", userName);
/*  58 */     this.customParameters.put("sessionid", sessionId);
/*     */     
/*  60 */     this.gameUpdater = new GameUpdater(latestVersion, "minecraft.jar?user=" + userName + "&ticket=" + downloadTicket);
/*     */   }
/*     */   
/*     */   public boolean canPlayOffline() {
/*  64 */     return this.gameUpdater.canPlayOffline();
/*     */   }
/*     */   
/*     */   public void init() {
/*  68 */     if (this.applet != null) {
/*  69 */       this.applet.init();
/*     */       return;
/*     */     } 
/*  72 */     init(getParameter("userName"), getParameter("latestVersion"), getParameter("downloadTicket"), getParameter("sessionId"));
/*     */   }
/*     */   
/*     */   public void start() {
/*  76 */     if (this.applet != null) {
/*  77 */       this.applet.start();
/*     */       return;
/*     */     } 
/*  80 */     if (this.gameUpdaterStarted)
/*     */       return; 
/*  82 */     Thread t = new Thread() {
/*     */         public void run() {
/*  84 */           Launcher.this.gameUpdater.run();
/*     */           try {
/*  86 */             if (!Launcher.this.gameUpdater.fatalError) {
/*  87 */               Launcher.this.replace(Launcher.this.gameUpdater.createApplet());
/*     */             }
/*     */           }
/*  90 */           catch (ClassNotFoundException e) {
/*  91 */             e.printStackTrace();
/*  92 */           } catch (InstantiationException e) {
/*  93 */             e.printStackTrace();
/*  94 */           } catch (IllegalAccessException e) {
/*  95 */             e.printStackTrace();
/*     */           } 
/*     */         }
/*     */       };
/*  99 */     t.setDaemon(true);
/* 100 */     t.start();
/*     */     
/* 102 */     t = new Thread() {
/*     */         public void run() {
/* 104 */           while (Launcher.this.applet == null) {
/* 105 */             Launcher.this.repaint();
/*     */             try {
/* 107 */               Thread.sleep(10L);
/* 108 */             } catch (InterruptedException e) {
/* 109 */               e.printStackTrace();
/*     */             } 
/*     */           } 
/*     */         }
/*     */       };
/* 114 */     t.setDaemon(true);
/* 115 */     t.start();
/*     */     
/* 117 */     this.gameUpdaterStarted = true;
/*     */   }
/*     */   
/*     */   public void stop() {
/* 121 */     if (this.applet != null) {
/* 122 */       this.active = false;
/* 123 */       this.applet.stop();
/*     */       return;
/*     */     } 
/*     */   }
/*     */   
/*     */   public void destroy() {
/* 129 */     if (this.applet != null) {
/* 130 */       this.applet.destroy();
/*     */       return;
/*     */     } 
/*     */   }
/*     */   
/*     */   public void replace(Applet applet) {
/* 136 */     this.applet = applet;
/* 137 */     applet.setStub(this);
/* 138 */     applet.setSize(getWidth(), getHeight());
/*     */     
/* 140 */     setLayout(new BorderLayout());
/* 141 */     add(applet, "Center");
/*     */     
/* 143 */     applet.init();
/* 144 */     this.active = true;
/* 145 */     applet.start();
/* 146 */     validate();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void update(Graphics g) {
/* 153 */     paint(g);
/*     */   }
/*     */   
/*     */   public void paint(Graphics g2) {
/* 157 */     if (this.applet != null)
/*     */       return; 
/* 159 */     int w = getWidth() / 2;
/* 160 */     int h = getHeight() / 2;
/* 161 */     if (this.img == null || this.img.getWidth() != w || this.img.getHeight() != h) {
/* 162 */       this.img = createVolatileImage(w, h);
/*     */     }
/*     */     
/* 165 */     Graphics g = this.img.getGraphics();
/* 166 */     for (int x = 0; x <= w / 32; x++) {
/* 167 */       for (int y = 0; y <= h / 32; y++)
/* 168 */         g.drawImage(this.bgImage, x * 32, y * 32, null); 
/*     */     } 
/* 170 */     if (this.gameUpdater.pauseAskUpdate) {
/* 171 */       if (!this.hasMouseListener) {
/* 172 */         System.out.println("Adding mouse listener yay");
/* 173 */         this.hasMouseListener = true;
/* 174 */         addMouseListener(this);
/*     */       } 
/* 176 */       g.setColor(Color.LIGHT_GRAY);
/* 177 */       String msg = "New update available";
/* 178 */       g.setFont(new Font(null, 1, 20));
/* 179 */       FontMetrics fm = g.getFontMetrics();
/* 180 */       g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - fm.getHeight() * 2);
/*     */       
/* 182 */       g.setFont(new Font(null, 0, 12));
/* 183 */       fm = g.getFontMetrics();
/*     */       
/* 185 */       g.fill3DRect(w / 2 - 56 - 8, h / 2, 56, 20, true);
/* 186 */       g.fill3DRect(w / 2 + 8, h / 2, 56, 20, true);
/*     */       
/* 188 */       msg = "Would you like to update?";
/* 189 */       g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - 8);
/*     */       
/* 191 */       g.setColor(Color.BLACK);
/* 192 */       msg = "Yes";
/* 193 */       g.drawString(msg, w / 2 - 56 - 8 - fm.stringWidth(msg) / 2 + 28, h / 2 + 14);
/* 194 */       msg = "Not now";
/* 195 */       g.drawString(msg, w / 2 + 8 - fm.stringWidth(msg) / 2 + 28, h / 2 + 14);
/*     */     }
/*     */     else {
/*     */       
/* 199 */       g.setColor(Color.LIGHT_GRAY);
/*     */ 
/*     */ 
/*     */       
/* 203 */       String msg = "Updating Minecraft";
/* 204 */       if (this.gameUpdater.fatalError) {
/* 205 */         msg = "Failed to launch";
/*     */       }
/*     */       
/* 208 */       g.setFont(new Font(null, 1, 20));
/* 209 */       FontMetrics fm = g.getFontMetrics();
/* 210 */       g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - fm.getHeight() * 2);
/*     */       
/* 212 */       g.setFont(new Font(null, 0, 12));
/* 213 */       fm = g.getFontMetrics();
/* 214 */       msg = this.gameUpdater.getDescriptionForState();
/* 215 */       if (this.gameUpdater.fatalError) {
/* 216 */         msg = this.gameUpdater.fatalErrorDescription;
/*     */       }
/*     */       
/* 219 */       g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 + fm.getHeight() * 1);
/* 220 */       msg = this.gameUpdater.subtaskMessage;
/* 221 */       g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 + fm.getHeight() * 2);
/*     */       
/* 223 */       if (!this.gameUpdater.fatalError) {
/* 224 */         g.setColor(Color.black);
/* 225 */         g.fillRect(64, h - 64, w - 128 + 1, 5);
/* 226 */         g.setColor(new Color(32768));
/* 227 */         g.fillRect(64, h - 64, this.gameUpdater.percentage * (w - 128) / 100, 4);
/* 228 */         g.setColor(new Color(2138144));
/* 229 */         g.fillRect(65, h - 64 + 1, this.gameUpdater.percentage * (w - 128) / 100 - 2, 1);
/*     */       } 
/*     */     } 
/*     */     
/* 233 */     g.dispose();
/*     */ 
/*     */ 
/*     */     
/* 237 */     g2.drawImage(this.img, 0, 0, w * 2, h * 2, null);
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {}
/*     */   
/*     */   public String getParameter(String name) {
/* 244 */     String custom = this.customParameters.get(name);
/* 245 */     if (custom != null) return custom; 
/*     */     try {
/* 247 */       return super.getParameter(name);
/* 248 */     } catch (Exception e) {
/* 249 */       this.customParameters.put(name, null);
/* 250 */       return null;
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void appletResize(int width, int height) {}
/*     */   
/*     */   public URL getDocumentBase() {
/*     */     try {
/* 259 */       return new URL("http://www.minecraft.net/game/");
/* 260 */     } catch (MalformedURLException e) {
/* 261 */       e.printStackTrace();
/*     */       
/* 263 */       return null;
/*     */     } 
/*     */   }
/*     */   public void mouseClicked(MouseEvent arg0) {
/* 267 */     System.out.println("OMG CLICK");
/*     */   }
/*     */ 
/*     */   
/*     */   public void mouseEntered(MouseEvent arg0) {}
/*     */ 
/*     */   
/*     */   public void mouseExited(MouseEvent arg0) {}
/*     */   
/*     */   public void mousePressed(MouseEvent me) {
/* 277 */     System.out.println("OMG CLICK");
/* 278 */     int x = me.getX() / 2;
/* 279 */     int y = me.getY() / 2;
/* 280 */     int w = getWidth() / 2;
/* 281 */     int h = getHeight() / 2;
/*     */     
/* 283 */     if (contains(x, y, w / 2 - 56 - 8, h / 2, 56, 20)) {
/* 284 */       removeMouseListener(this);
/* 285 */       this.gameUpdater.shouldUpdate = true;
/* 286 */       this.gameUpdater.pauseAskUpdate = false;
/* 287 */       this.hasMouseListener = false;
/*     */     } 
/* 289 */     if (contains(x, y, w / 2 + 8, h / 2, 56, 20)) {
/* 290 */       removeMouseListener(this);
/* 291 */       this.gameUpdater.shouldUpdate = false;
/* 292 */       this.gameUpdater.pauseAskUpdate = false;
/* 293 */       this.hasMouseListener = false;
/*     */     } 
/*     */   }
/*     */   
/*     */   private boolean contains(int x, int y, int xx, int yy, int w, int h) {
/* 298 */     return (x >= xx && y >= yy && x < xx + w && y < yy + h);
/*     */   }
/*     */   
/*     */   public void mouseReleased(MouseEvent arg0) {}
/*     */ }


/* Location:              C:\Users\Harley\Desktop\Javadecompiler\MinecraftLauncher.jar!\net\minecraft\Launcher.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       1.1.3
 */