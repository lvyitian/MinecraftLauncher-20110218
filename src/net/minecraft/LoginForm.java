/*     */ package net.minecraft;
/*     */
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Component;
/*     */ import java.awt.Cursor;
/*     */ import java.awt.Desktop;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.MouseAdapter;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.net.URL;
/*     */ import java.util.Random;
/*     */ import javax.crypto.Cipher;
/*     */ import javax.crypto.CipherInputStream;
/*     */ import javax.crypto.CipherOutputStream;
/*     */ import javax.crypto.SecretKey;
/*     */ import javax.crypto.SecretKeyFactory;
/*     */ import javax.crypto.spec.PBEKeySpec;
/*     */ import javax.crypto.spec.PBEParameterSpec;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JPasswordField;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextField;
/*     */ import javax.swing.JTextPane;
/*     */ import javax.swing.border.Border;
/*     */ import javax.swing.border.MatteBorder;
/*     */ import javax.swing.event.HyperlinkEvent;
/*     */ import javax.swing.event.HyperlinkListener;
/*     */
/*     */ public class LoginForm
/*     */   extends TransparentPanel {
/*     */   private static final int PANEL_SIZE = 100;
/*     */   private static final long serialVersionUID = 1L;
/*  48 */   private static final Color LINK_COLOR = new Color(8421631);
/*     */
/*  50 */   private JTextField userName = new JTextField(20);
/*  51 */   private JPasswordField password = new JPasswordField(20);
/*  52 */   private TransparentCheckbox rememberBox = new TransparentCheckbox("Remember password");
/*  53 */   private TransparentButton launchButton = new TransparentButton("Login");
/*  54 */   private TransparentButton optionsButton = new TransparentButton("Options");
/*  55 */   private TransparentButton retryButton = new TransparentButton("Try again");
/*  56 */   private TransparentButton offlineButton = new TransparentButton("Play offline"); private LauncherFrame launcherFrame; private boolean outdated = false;
/*  57 */   private TransparentLabel errorLabel = new TransparentLabel("", 0);
/*     */
/*     */   private JScrollPane scrollPane;
/*     */
/*     */   public LoginForm(final LauncherFrame launcherFrame) {
/*  62 */     this.launcherFrame = launcherFrame;
/*     */
/*  64 */     this.userName.setBackground(Color.BLACK);
/*  65 */     this.password.setBackground(Color.BLACK);
/*  66 */     this.userName.setForeground(Color.WHITE);
/*  67 */     this.password.setForeground(Color.WHITE);
/*  68 */     this.userName.setCaretColor(Color.WHITE);
/*  69 */     this.password.setCaretColor(Color.WHITE);
/*     */
/*     */
/*     */
/*  73 */     BorderLayout gbl = new BorderLayout();
/*  74 */     setLayout(gbl);
/*     */
/*  76 */     add(buildMainLoginPanel(), "Center");
/*     */
/*  78 */     readUsername();
/*     */
/*  80 */     this.retryButton.addActionListener(new ActionListener() {
/*     */           public void actionPerformed(ActionEvent ae) {
/*  82 */             LoginForm.this.errorLabel.setText("");
/*  83 */             LoginForm.this.removeAll();
/*  84 */             LoginForm.this.add(LoginForm.this.buildMainLoginPanel(), "Center");
/*  85 */             LoginForm.this.validate();
/*     */           }
/*     */         });
/*     */
/*  89 */     this.offlineButton.addActionListener(new ActionListener() {
/*     */           public void actionPerformed(ActionEvent ae) {
/*  91 */             launcherFrame.playCached(LoginForm.this.userName.getText());
/*     */           }
/*     */         });
/*     */
/*  95 */     this.launchButton.addActionListener(new ActionListener() {
/*     */           public void actionPerformed(ActionEvent ae) {
/*  97 */             LoginForm.this.setLoggingIn();
/*  98 */             (new Thread() {
/*     */                 public void run() {
/*     */                   try {
/* 101 */                     launcherFrame.login((LoginForm.this).userName.getText(), new String((LoginForm.this).password.getPassword()));
/* 102 */                   } catch (Exception e) {
/* 103 */                     LoginForm.this.setError(e.toString());
/*     */                   }
/*     */                 }
/* 106 */               }).start();
/*     */           }
/*     */         });
/*     */
/* 110 */     this.optionsButton.addActionListener(new ActionListener() {
/*     */           public void actionPerformed(ActionEvent ae) {
/* 112 */             (new OptionsPanel(launcherFrame)).setVisible(true);
/*     */           }
/*     */         });
/*     */   }
/*     */   private void readUsername() {
/*     */     try {
/*     */       DataInputStream dis;
/* 119 */       File lastLogin = new File(Util.getWorkingDirectory(), "lastlogin");
/*     */
/* 121 */       Cipher cipher = getCipher(2, "passwordfile");
/* 122 */       if (cipher != null) {
/* 123 */         dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
/*     */       } else {
/* 125 */         dis = new DataInputStream(new FileInputStream(lastLogin));
/*     */       }
/* 127 */       this.userName.setText(dis.readUTF());
/* 128 */       this.password.setText(dis.readUTF());
/* 129 */       this.rememberBox.setSelected(((this.password.getPassword()).length > 0));
/* 130 */       dis.close();
/* 131 */     } catch (Exception e) {
/* 132 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */   private void writeUsername() {
/*     */     try {
/*     */       DataOutputStream dos;
/* 138 */       File lastLogin = new File(Util.getWorkingDirectory(), "lastlogin");
/*     */
/* 140 */       Cipher cipher = getCipher(1, "passwordfile");
/* 141 */       if (cipher != null) {
/* 142 */         dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
/*     */       } else {
/* 144 */         dos = new DataOutputStream(new FileOutputStream(lastLogin));
/*     */       }
/* 146 */       dos.writeUTF(this.userName.getText());
/* 147 */       dos.writeUTF(this.rememberBox.isSelected() ? new String(this.password.getPassword()) : "");
/* 148 */       dos.close();
/* 149 */     } catch (Exception e) {
/* 150 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */
/*     */   private Cipher getCipher(int mode, String password) throws Exception {
/* 155 */     Random random = new Random(43287234L);
/* 156 */     byte[] salt = new byte[8];
/* 157 */     random.nextBytes(salt);
/* 158 */     PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);
/*     */
/* 160 */     SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
/* 161 */     Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
/* 162 */     cipher.init(mode, pbeKey, pbeParamSpec);
/* 163 */     return cipher;
/*     */   }
/*     */
/*     */
/*     */
/*     */   private JScrollPane getUpdateNews() {
/* 169 */     if (this.scrollPane != null) return this.scrollPane;
/*     */
/*     */     try {
/* 172 */       final JTextPane editorPane = new JTextPane() {
/*     */           private static final long serialVersionUID = 1L;
/*     */         };
/* 175 */       editorPane.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center>Loading update news..</center></font></body></html>");
/* 176 */       editorPane.addHyperlinkListener(new HyperlinkListener() {
/*     */             public void hyperlinkUpdate(HyperlinkEvent he) {
/* 178 */               if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
/*     */                 try {
/* 180 */                   Desktop.getDesktop().browse(he.getURL().toURI());
/* 181 */                 } catch (Exception e) {
/* 182 */                   e.printStackTrace();
/*     */                 }
/*     */               }
/*     */             }
/*     */           });
/* 187 */       (new Thread() {
/*     */           public void run() {
/*     */             try {
/* 190 */               editorPane.setPage(new URL("https://mcupdate.tumblr.com/"));
/* 191 */             } catch (Exception e) {
/* 192 */               e.printStackTrace();
/* 193 */               editorPane.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center>Failed to update news<br>" + e.toString() + "</center></font></body></html>");
/*     */             }
/*     */           }
/* 196 */         }).start();
/* 197 */       editorPane.setBackground(Color.BLACK);
/* 198 */       editorPane.setEditable(false);
/* 199 */       this.scrollPane = new JScrollPane(editorPane);
/* 200 */       this.scrollPane.setBorder((Border)null);
/* 201 */       editorPane.setMargin((Insets)null);
/*     */
/* 203 */       this.scrollPane.setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));
/* 204 */     } catch (Exception e2) {
/* 205 */       e2.printStackTrace();
/*     */     }
/*     */
/* 208 */     return this.scrollPane;
/*     */   }
/*     */
/*     */   private JPanel buildMainLoginPanel() {
/* 212 */     JPanel p = new TransparentPanel(new BorderLayout());
/* 213 */     p.add(getUpdateNews(), "Center");
/*     */
/* 215 */     JPanel southPanel = new TexturedPanel();
/* 216 */     southPanel.setLayout(new BorderLayout());
/* 217 */     southPanel.add(new LogoPanel(), "West");
/* 218 */     southPanel.add(new TransparentPanel(), "Center");
/* 219 */     southPanel.add(center(buildLoginPanel()), "East");
/* 220 */     southPanel.setPreferredSize(new Dimension(100, 100));
/*     */
/* 222 */     p.add(southPanel, "South");
/* 223 */     return p;
/*     */   }
/*     */
/*     */   private JPanel buildLoginPanel() {
/* 227 */     TransparentPanel panel = new TransparentPanel();
/* 228 */     panel.setInsets(4, 0, 4, 0);
/*     */
/* 230 */     BorderLayout layout = new BorderLayout();
/* 231 */     layout.setHgap(0);
/* 232 */     layout.setVgap(8);
/* 233 */     panel.setLayout(layout);
/*     */
/*     */
/* 236 */     GridLayout gl1 = new GridLayout(0, 1);
/* 237 */     gl1.setVgap(2);
/* 238 */     GridLayout gl2 = new GridLayout(0, 1);
/* 239 */     gl2.setVgap(2);
/* 240 */     GridLayout gl3 = new GridLayout(0, 1);
/* 241 */     gl3.setVgap(2);
/*     */
/* 243 */     TransparentPanel titles = new TransparentPanel(gl1);
/* 244 */     TransparentPanel values = new TransparentPanel(gl2);
/*     */
/* 246 */     titles.add(new TransparentLabel("Username:", 4));
/* 247 */     titles.add(new TransparentLabel("Password:", 4));
/* 248 */     titles.add(new TransparentLabel("", 4));
/*     */
/*     */
/* 251 */     values.add(this.userName);
/* 252 */     values.add(this.password);
/* 253 */     values.add(this.rememberBox);
/*     */
/* 255 */     panel.add(titles, "West");
/* 256 */     panel.add(values, "Center");
/*     */
/* 258 */     TransparentPanel loginPanel = new TransparentPanel(new BorderLayout());
/*     */
/* 260 */     TransparentPanel third = new TransparentPanel(gl3);
/* 261 */     titles.setInsets(0, 0, 0, 4);
/* 262 */     third.setInsets(0, 10, 0, 10);
/*     */
/*     */
/*     */     try {
/* 266 */       if (this.outdated) {
/* 267 */         TransparentLabel accountLink = getUpdateLink();
/* 268 */         third.add(accountLink);
/*     */       }
/*     */       else {
/*     */
/* 272 */         TransparentLabel accountLink = new TransparentLabel("Need account?") {
/*     */             private static final long serialVersionUID = 0L;
/*     */
/*     */             public void paint(Graphics g) {
/* 276 */               super.paint(g);
/*     */
/* 278 */               int x = 0;
/* 279 */               int y = 0;
/*     */
/*     */
/*     */
/* 283 */               FontMetrics fm = g.getFontMetrics();
/* 284 */               int width = fm.stringWidth(getText());
/* 285 */               int height = fm.getHeight();
/*     */
/* 287 */               if (getAlignmentX() == 2.0F) { x = 0; }
/* 288 */               else if (getAlignmentX() == 0.0F) { x = (getBounds()).width / 2 - width / 2; }
/* 289 */               else if (getAlignmentX() == 4.0F) { x = (getBounds()).width - width; }
/* 290 */                y = (getBounds()).height / 2 + height / 2 - 1;
/*     */
/* 292 */               g.drawLine(x + 2, y, x + width - 2, y);
/*     */             }
/*     */
/*     */             public void update(Graphics g) {
/* 296 */               paint(g);
/*     */             }
/*     */           };
/*     */
/* 300 */         accountLink.setCursor(Cursor.getPredefinedCursor(12));
/* 301 */         accountLink.addMouseListener(new MouseAdapter() {
/*     */               public void mousePressed(MouseEvent arg0) {
/*     */                 try {
/* 304 */                   Desktop.getDesktop().browse((new URL("http://www.minecraft.net/register.jsp")).toURI());
/* 305 */                 } catch (Exception e) {
/* 306 */                   e.printStackTrace();
/*     */                 }
/*     */               }
/*     */             });
/* 310 */         accountLink.setForeground(LINK_COLOR);
/* 311 */         third.add(accountLink);
/*     */       }
/*     */
/*     */     }
/* 315 */     catch (Error error) {}
/*     */
/*     */
/* 318 */     third.add(this.launchButton);
/* 319 */     third.add(this.optionsButton);
/*     */
/* 321 */     loginPanel.add(third, "Center");
/* 322 */     panel.add(loginPanel, "East");
/*     */
/* 324 */     this.errorLabel.setFont(new Font(null, 2, 16));
/* 325 */     this.errorLabel.setForeground(new Color(16728128));
/* 326 */     this.errorLabel.setText("");
/* 327 */     panel.add(this.errorLabel, "North");
/*     */
/* 329 */     return panel;
/*     */   }
/*     */
/*     */   private TransparentLabel getUpdateLink() {
/* 333 */     TransparentLabel accountLink = new TransparentLabel("You need to update the launcher!") {
/*     */         private static final long serialVersionUID = 0L;
/*     */
/*     */         public void paint(Graphics g) {
/* 337 */           super.paint(g);
/*     */
/* 339 */           int x = 0;
/* 340 */           int y = 0;
/*     */
/*     */
/*     */
/* 344 */           FontMetrics fm = g.getFontMetrics();
/* 345 */           int width = fm.stringWidth(getText());
/* 346 */           int height = fm.getHeight();
/*     */
/* 348 */           if (getAlignmentX() == 2.0F) { x = 0; }
/* 349 */           else if (getAlignmentX() == 0.0F) { x = (getBounds()).width / 2 - width / 2; }
/* 350 */           else if (getAlignmentX() == 4.0F) { x = (getBounds()).width - width; }
/* 351 */            y = (getBounds()).height / 2 + height / 2 - 1;
/*     */
/* 353 */           g.drawLine(x + 2, y, x + width - 2, y);
/*     */         }
/*     */
/*     */         public void update(Graphics g) {
/* 357 */           paint(g);
/*     */         }
/*     */       };
/*     */
/* 361 */     accountLink.setCursor(Cursor.getPredefinedCursor(12));
/* 362 */     accountLink.addMouseListener(new MouseAdapter() {
/*     */           public void mousePressed(MouseEvent arg0) {
/*     */             try {
/* 365 */               Desktop.getDesktop().browse((new URL("http://www.minecraft.net/download.jsp")).toURI());
/* 366 */             } catch (Exception e) {
/* 367 */               e.printStackTrace();
/*     */             }
/*     */           }
/*     */         });
/* 371 */     accountLink.setForeground(LINK_COLOR);
/* 372 */     return accountLink;
/*     */   }
/*     */
/*     */   private JPanel buildMainOfflinePanel() {
/* 376 */     JPanel p = new TransparentPanel(new BorderLayout());
/* 377 */     p.add(getUpdateNews(), "Center");
/*     */
/* 379 */     JPanel southPanel = new TexturedPanel();
/* 380 */     southPanel.setLayout(new BorderLayout());
/* 381 */     southPanel.add(new LogoPanel(), "West");
/* 382 */     southPanel.add(new TransparentPanel(), "Center");
/* 383 */     southPanel.add(center(buildOfflinePanel()), "East");
/* 384 */     southPanel.setPreferredSize(new Dimension(100, 100));
/*     */
/* 386 */     p.add(southPanel, "South");
/* 387 */     return p;
/*     */   }
/*     */
/*     */   private Component center(Component c) {
/* 391 */     TransparentPanel tp = new TransparentPanel(new GridBagLayout());
/* 392 */     tp.add(c);
/* 393 */     return tp;
/*     */   }
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */   private TransparentPanel buildOfflinePanel() {
/* 402 */     TransparentPanel panel = new TransparentPanel();
/* 403 */     panel.setInsets(0, 0, 0, 20);
/*     */
/*     */
/* 406 */     BorderLayout layout = new BorderLayout();
/* 407 */     panel.setLayout(layout);
/*     */
/* 409 */     TransparentPanel loginPanel = new TransparentPanel(new BorderLayout());
/*     */
/* 411 */     GridLayout gl = new GridLayout(0, 1);
/* 412 */     gl.setVgap(2);
/* 413 */     TransparentPanel pp = new TransparentPanel(gl);
/* 414 */     pp.setInsets(0, 8, 0, 0);
/*     */
/* 416 */     pp.add(this.retryButton);
/* 417 */     pp.add(this.offlineButton);
/*     */
/* 419 */     loginPanel.add(pp, "East");
/*     */
/* 421 */     boolean canPlayOffline = this.launcherFrame.canPlayOffline(this.userName.getText());
/* 422 */     this.offlineButton.setEnabled(canPlayOffline);
/* 423 */     if (!canPlayOffline) {
/* 424 */       loginPanel.add(new TransparentLabel("(Not downloaded)", 4), "South");
/*     */     }
/* 426 */     panel.add(loginPanel, "Center");
/*     */
/* 428 */     TransparentPanel p2 = new TransparentPanel(new GridLayout(0, 1));
/* 429 */     this.errorLabel.setFont(new Font(null, 2, 16));
/* 430 */     this.errorLabel.setForeground(new Color(16728128));
/* 431 */     p2.add(this.errorLabel);
/* 432 */     if (this.outdated) {
/* 433 */       TransparentLabel accountLink = getUpdateLink();
/* 434 */       p2.add(accountLink);
/*     */     }
/*     */
/* 437 */     loginPanel.add(p2, "Center");
/*     */
/*     */
/* 440 */     return panel;
/*     */   }
/*     */
/*     */   public void setError(String errorMessage) {
/* 444 */     removeAll();
/* 445 */     add(buildMainLoginPanel(), "Center");
/* 446 */     this.errorLabel.setText(errorMessage);
/* 447 */     validate();
/*     */   }
/*     */
/*     */   public void loginOk() {
/* 451 */     writeUsername();
/*     */   }
/*     */
/*     */   public void setLoggingIn() {
/* 455 */     removeAll();
/* 456 */     JPanel panel = new JPanel(new BorderLayout());
/* 457 */     panel.add(getUpdateNews(), "Center");
/*     */
/*     */
/* 460 */     JPanel southPanel = new TexturedPanel();
/* 461 */     southPanel.setLayout(new BorderLayout());
/* 462 */     southPanel.add(new LogoPanel(), "West");
/* 463 */     southPanel.add(new TransparentPanel(), "Center");
/* 464 */     JLabel label = new TransparentLabel("Logging in...                      ", 0);
/* 465 */     label.setFont(new Font(null, 1, 16));
/* 466 */     southPanel.add(center(label), "East");
/* 467 */     southPanel.setPreferredSize(new Dimension(100, 100));
/*     */
/* 469 */     panel.add(southPanel, "South");
/*     */
/* 471 */     add(panel, "Center");
/* 472 */     validate();
/*     */   }
/*     */
/*     */   public void setNoNetwork() {
/* 476 */     removeAll();
/* 477 */     add(buildMainOfflinePanel(), "Center");
/* 478 */     validate();
/*     */   }
/*     */
/*     */   public void checkAutologin() {
/* 482 */     if ((this.password.getPassword()).length > 0) {
/* 483 */       this.launcherFrame.login(this.userName.getText(), new String(this.password.getPassword()));
/*     */     }
/*     */   }
/*     */
/*     */   public void setOutdated() {
/* 488 */     this.outdated = true;
/*     */   }
/*     */ }


/* Location:              C:\Users\Harley\Desktop\Javadecompiler\MinecraftLauncher.jar!\net\minecraft\LoginForm.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       1.1.3
 */