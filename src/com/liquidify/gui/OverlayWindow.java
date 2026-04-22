package com.liquidify.gui;

import com.liquidify.modules.Module;
import com.liquidify.modules.ModuleManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class OverlayWindow extends JFrame {
    private JPanel contentPane;

    public OverlayWindow() {
        setTitle("Liquidify Panel");
        setSize(250, 400);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBackground(new Color(30, 30, 30));

        contentPane = new JPanel();
        contentPane.setBackground(new Color(25, 25, 25));
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JLabel header = new JLabel("Liquidify's external client");
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPane.add(Box.createVerticalStrut(10));
        contentPane.add(header);
        contentPane.add(Box.createVerticalStrut(10));

        for (Module m : ModuleManager.getModules()) {
            JPanel modPanel = new JPanel(new BorderLayout());
            modPanel.setBackground(new Color(35, 35, 35));
            modPanel.setMaximumSize(new Dimension(230, 40));
            modPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            JLabel name = new JLabel(m.getName() + " (" + (char) m.getKeybind() + ")");
            name.setForeground(Color.LIGHT_GRAY);

            JButton toggleBtn = new JButton(m.isEnabled() ? "ON" : "OFF");
            toggleBtn.setFocusPainted(false);
            toggleBtn.setBackground(m.isEnabled() ? new Color(0, 150, 50) : new Color(150, 50, 50));
            toggleBtn.setForeground(Color.WHITE);

            toggleBtn.addActionListener(e -> {
                m.toggle();
                toggleBtn.setText(m.isEnabled() ? "ON" : "OFF");
                toggleBtn.setBackground(m.isEnabled() ? new Color(0, 150, 50) : new Color(150, 50, 50));
            });

            modPanel.add(name, BorderLayout.WEST);
            modPanel.add(toggleBtn, BorderLayout.EAST);

            contentPane.add(modPanel);
            contentPane.add(Box.createVerticalStrut(5));
        }

        JScrollPane scroll = new JScrollPane(contentPane);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll);

        // Timer to refresh button states if toggled by keys
        new Timer(100, e -> {
            refreshButtons();
        }).start();

        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
        requestFocus();
        System.out.println("[OverlayWindow] Window set visible at center.");
    }

    private void refreshButtons() {
        Component[] components = contentPane.getComponents();
        int modIdx = 0;
        for (Component c : components) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                for (Component sub : p.getComponents()) {
                    if (sub instanceof JButton) {
                        JButton btn = (JButton) sub;
                        Module m = ModuleManager.getModules().get(modIdx);
                        btn.setText(m.isEnabled() ? "ON" : "OFF");
                        btn.setBackground(m.isEnabled() ? new Color(0, 150, 50) : new Color(150, 50, 50));
                    }
                }
                modIdx++;
            }
        }
    }

    public void updateOverlay() {
        // Not needed for separate window
    }
}
