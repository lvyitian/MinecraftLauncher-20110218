/*     */ package net.minecraft;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Cursor;
/*     */ import java.awt.Desktop;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Window;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.MouseAdapter;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.net.URL;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.border.EmptyBorder;
/*     */ 
/*     */ public class OptionsPanel
/*     */   extends JDialog {
/*     */   private static final long serialVersionUID = 1L;
/*     */   
/*     */   public OptionsPanel(Window parent) {
/*  28 */     super(parent);
/*     */     
/*  30 */     setModal(true);
/*     */     
/*  32 */     JPanel panel = new JPanel(new BorderLayout());
/*  33 */     JLabel label = new JLabel("Launcher options", 0);
/*  34 */     label.setBorder(new EmptyBorder(0, 0, 16, 0));
/*  35 */     label.setFont(new Font("Default", 1, 16));
/*  36 */     panel.add(label, "North");
/*     */     
/*  38 */     JPanel optionsPanel = new JPanel(new BorderLayout());
/*  39 */     JPanel labelPanel = new JPanel(new GridLayout(0, 1));
/*  40 */     JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
/*  41 */     optionsPanel.add(labelPanel, "West");
/*  42 */     optionsPanel.add(fieldPanel, "Center");
/*     */     
/*  44 */     final JButton forceButton = new JButton("Force update!");
/*  45 */     forceButton.addActionListener(new ActionListener() {
/*     */           public void actionPerformed(ActionEvent ae) {
/*  47 */             GameUpdater.forceUpdate = true;
/*  48 */             forceButton.setText("Will force!");
/*  49 */             forceButton.setEnabled(false);
/*     */           }
/*     */         });
/*     */     
/*  53 */     labelPanel.add(new JLabel("Force game update: ", 4));
/*  54 */     fieldPanel.add(forceButton);
/*     */     
/*  56 */     labelPanel.add(new JLabel("Game location on disk: ", 4));
/*  57 */     TransparentLabel dirLink = new TransparentLabel(Util.getWorkingDirectory().toString()) {
/*     */         private static final long serialVersionUID = 0L;
/*     */         
/*     */         public void paint(Graphics g) {
/*  61 */           super.paint(g);
/*     */           
/*  63 */           int x = 0;
/*  64 */           int y = 0;
/*     */ 
/*     */ 
/*     */           
/*  68 */           FontMetrics fm = g.getFontMetrics();
/*  69 */           int width = fm.stringWidth(getText());
/*  70 */           int height = fm.getHeight();
/*     */           
/*  72 */           if (getAlignmentX() == 2.0F) { x = 0; }
/*  73 */           else if (getAlignmentX() == 0.0F) { x = (getBounds()).width / 2 - width / 2; }
/*  74 */           else if (getAlignmentX() == 4.0F) { x = (getBounds()).width - width; }
/*  75 */            y = (getBounds()).height / 2 + height / 2 - 1;
/*     */           
/*  77 */           g.drawLine(x + 2, y, x + width - 2, y);
/*     */         }
/*     */         
/*     */         public void update(Graphics g) {
/*  81 */           paint(g);
/*     */         }
/*     */       };
/*  84 */     dirLink.setCursor(Cursor.getPredefinedCursor(12));
/*  85 */     dirLink.addMouseListener(new MouseAdapter() {
/*     */           public void mousePressed(MouseEvent arg0) {
/*     */             try {
/*  88 */               Desktop.getDesktop().browse((new URL("file://" + Util.getWorkingDirectory().getAbsolutePath())).toURI());
/*  89 */             } catch (Exception e) {
/*  90 */               e.printStackTrace();
/*     */             } 
/*     */           }
/*     */         });
/*  94 */     dirLink.setForeground(new Color(2105599));
/*     */ 
/*     */     
/*  97 */     fieldPanel.add(dirLink);
/*     */     
/*  99 */     panel.add(optionsPanel, "Center");
/*     */     
/* 101 */     JPanel buttonsPanel = new JPanel(new BorderLayout());
/* 102 */     buttonsPanel.add(new JPanel(), "Center");
/* 103 */     JButton doneButton = new JButton("Done");
/* 104 */     doneButton.addActionListener(new ActionListener() {
/*     */           public void actionPerformed(ActionEvent ae) {
/* 106 */             OptionsPanel.this.setVisible(false);
/*     */           }
/*     */         });
/* 109 */     buttonsPanel.add(doneButton, "East");
/* 110 */     buttonsPanel.setBorder(new EmptyBorder(16, 0, 0, 0));
/*     */     
/* 112 */     panel.add(buttonsPanel, "South");
/*     */     
/* 114 */     add(panel);
/* 115 */     panel.setBorder(new EmptyBorder(16, 24, 24, 24));
/* 116 */     pack();
/* 117 */     setLocationRelativeTo(parent);
/*     */   }
/*     */ }


/* Location:              C:\Users\Harley\Desktop\Javadecompiler\MinecraftLauncher.jar!\net\minecraft\OptionsPanel.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       1.1.3
 */