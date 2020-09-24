/*    */ package net.minecraft;
/*    */ 
/*    */ import java.awt.Insets;
/*    */ import java.awt.LayoutManager;
/*    */ import javax.swing.JPanel;
/*    */ 
/*    */ public class TransparentPanel
/*    */   extends JPanel
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */   private Insets insets;
/*    */   
/*    */   public TransparentPanel() {}
/*    */   
/*    */   public TransparentPanel(LayoutManager layout) {
/* 16 */     setLayout(layout);
/*    */   }
/*    */   
/*    */   public boolean isOpaque() {
/* 20 */     return false;
/*    */   }
/*    */   
/*    */   public void setInsets(int a, int b, int c, int d) {
/* 24 */     this.insets = new Insets(a, b, c, d);
/*    */   }
/*    */   
/*    */   public Insets getInsets() {
/* 28 */     if (this.insets == null) return super.getInsets(); 
/* 29 */     return this.insets;
/*    */   }
/*    */ }


/* Location:              C:\Users\Harley\Desktop\Javadecompiler\MinecraftLauncher.jar!\net\minecraft\TransparentPanel.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       1.1.3
 */