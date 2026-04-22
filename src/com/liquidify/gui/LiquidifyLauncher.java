package com.liquidify.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.liquidify.Injector;

public class LiquidifyLauncher extends JFrame {
    private static final Color BG = new Color(15, 15, 20);
    private static final Color ACCENT = new Color(110, 80, 240);
    
    private float opacity = 0f;
    private float textY = 250f;
    private boolean injecting = false;
    private int progress = 0;
    private String statusText = "Injecting...";

    public LiquidifyLauncher() {
        setSize(800, 600);
        setUndecorated(true);
        setType(Window.Type.UTILITY);
        setBackground(BG);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                g2.setColor(BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Title
                g2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 42));
                g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int)(opacity * 50)));
                g2.drawString("Liquidify's external client", 155, textY + 5);
                
                g2.setColor(new Color(255, 255, 255, (int)(opacity * 255)));
                g2.drawString("Liquidify's external client", 150, textY);

                if (injecting) {
                    g2.setColor(new Color(50, 50, 60));
                    g2.fillRoundRect(200, 400, 400, 6, 3, 3);
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(200, 400, (int)(4.0 * progress), 6, 3, 3);
                    
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    g2.setColor(new Color(150, 150, 160));
                    g2.drawString(statusText, 400 - (g2.getFontMetrics().stringWidth(statusText)/2), 430);
                }

                g2.dispose();
            }
        };
        content.setLayout(null);
        add(content);

        JButton injectBtn = new JButton("INJECT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Bold", Font.PLAIN, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        injectBtn.setBounds(300, 350, 200, 50);
        injectBtn.setBorderPainted(false);
        injectBtn.setContentAreaFilled(false);
        injectBtn.setFocusPainted(false);
        injectBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        injectBtn.addActionListener(e -> {
            if (injecting) return;
            injecting = true;
            injectBtn.setVisible(false);
            
            // Background Injection
            new Thread(() -> {
                boolean success = Injector.performInjection();
                if (!success) {
                    statusText = "Injection Failed! (Check if MC is open)";
                    repaint();
                    try { Thread.sleep(2000); } catch (Exception ex) {}
                    System.exit(0);
                } else {
                    statusText = "Injected! Synchronizing...";
                }
            }).start();

            // Progress Bar Animation
            new Thread(() -> {
                while (progress < 100) {
                    progress += 2;
                    if (progress > 90 && statusText.equals("Injecting...")) {
                        // Wait for injection to actually finish if it's slow
                        progress = 90;
                    }
                    if (statusText.contains("Injected") && progress >= 90) {
                        progress += 5;
                    }
                    repaint();
                    try { Thread.sleep(30); } catch (Exception ex) {}
                }
                try { Thread.sleep(300); } catch (Exception ex) {}
                SwingUtilities.invokeLater(() -> {
                    new LiquidifyPanel();
                    dispose();
                });
            }).start();
        });
        content.add(injectBtn);

        setVisible(true);

        // Entrance animation
        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                opacity = i / 50f;
                textY = 250f - (i * 1.5f);
                repaint();
                try { Thread.sleep(10); } catch (Exception e) {}
            }
        }).start();
    }
}
