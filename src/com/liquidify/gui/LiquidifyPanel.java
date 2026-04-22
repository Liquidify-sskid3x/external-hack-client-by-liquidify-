package com.liquidify.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class LiquidifyPanel extends JFrame {

    private static final Color BG_DARK = new Color(20, 20, 25, 240);
    private static final Color BG_CARD = new Color(30, 30, 38, 220);
    private static final Color ACCENT = new Color(110, 80, 240);
    private static final Color GREEN = new Color(40, 190, 90);
    private static final Color RED = new Color(190, 50, 50);
    private static final Color TEXT_WHITE = new Color(240, 240, 245);
    private static final Color TEXT_DIM = new Color(130, 130, 150);

    private final List<ModuleEntry> entries = new ArrayList<>();
    private String bindingModule = null;
    private ESPWindow espWindow;
    private Map<String, List<SettingData>> moduleSettings = new HashMap<>();
    
    private Map<String, CategoryFrame> categories = new HashMap<>();
    private JPanel overlayContent;
    private volatile boolean overlayOpen = false;
    private final List<String> profiles = new ArrayList<>();
    private ProfilesFrame profilesFrame;
    private volatile boolean guiAllowed = false;
    private static final int BASE_LAYOUT_WIDTH = 1280;
    private static final int BASE_LAYOUT_HEIGHT = 720;
    private float currentScale = 1.0f;

    public LiquidifyPanel() {
        setTitle("Liquidify Panel");
        // This is an in-game overlay; closing it should never kill the process.
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setAlwaysOnTop(true);
        setUndecorated(true);
        setType(Window.Type.UTILITY);
        setFocusableWindowState(true);
        setAutoRequestFocus(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparent JFrame
        
        espWindow = new ESPWindow();

        overlayContent = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 120)); // Tinted black background
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlayContent.setOpaque(false);
        setContentPane(overlayContent);

        // Initialize Categories
        categories.put("Combat", new CategoryFrame("Combat", 50, 50));
        categories.put("Render", new CategoryFrame("Render", 250, 50));
        categories.put("Movement", new CategoryFrame("Movement", 450, 50));
        categories.put("Misc", new CategoryFrame("Misc", 650, 50));
        profilesFrame = new ProfilesFrame(850, 50);

        for (CategoryFrame cf : categories.values()) {
            overlayContent.add(cf);
        }
        overlayContent.add(profilesFrame);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                // Allow closing the overlay even when Minecraft isn't focused.
                // Use AWT key events (VK_SHIFT + keyLocation) rather than GLFW codes.
                if (e.getID() == KeyEvent.KEY_PRESSED && overlayOpen) {
                    boolean esc = e.getKeyCode() == KeyEvent.VK_ESCAPE;
                    boolean rshift = e.getKeyCode() == KeyEvent.VK_SHIFT
                            && e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT;
                    if (esc || rshift) {
                        bindingModule = null; // avoid getting stuck in "BINDING..."
                        overlayOpen = false;
                        SwingUtilities.invokeLater(() -> setOverlayVisible(false));
                        return true;
                    }
                }

                if (e.getID() == KeyEvent.KEY_PRESSED && bindingModule != null) {
                    final String mod = bindingModule;
                    int keyCode = e.getKeyCode();
                    final String key = (keyCode == KeyEvent.VK_ESCAPE) ? "NONE" : KeyEvent.getKeyText(keyCode);
                    new Thread(() -> sendCommand("BIND " + mod + " " + key)).start();
                    bindingModule = null;
                    return true;
                }

                // While the overlay is open, consume key presses so they don't affect Minecraft.
                // (The overlay is an external window; we want it to behave like a modal UI.)
                return overlayOpen && e.getID() == KeyEvent.KEY_PRESSED;
            }
        });

        // Start hidden
        setVisible(false);
        overlayOpen = false;

        Thread poller = new Thread(() -> {
            while (true) {
                try {
                    handleSync();
                } catch (Exception e) {}
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        });
        poller.setDaemon(true);
        poller.start();
    }

    private String getCategory(String modName) {
        if (modName.equals("KillAura") || modName.equals("AimAssist")) return "Combat";
        if (modName.equals("ESP")) return "Render";
        if (modName.equals("Fly") || modName.equals("Eagle")) return "Movement";
        return "Misc";
    }

    private class CategoryFrame extends JPanel {
        String category;
        JPanel content;
        int baseX;
        int baseY;
        
        public CategoryFrame(String category, int x, int y) {
            this.category = category;
            this.baseX = x;
            this.baseY = y;
            setLayout(new BorderLayout());
            setBackground(BG_CARD);
            setBounds(x, y, 160, 25);
            
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(BG_DARK);
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT));
            JLabel title = new JLabel(" " + category);
            title.setForeground(Color.WHITE);
            title.setFont(new Font("Segoe UI", Font.BOLD, 12));
            header.add(title, BorderLayout.WEST);
            header.setPreferredSize(new Dimension(160, 25));
            
            final int[] dragX = { 0 }, dragY = { 0 };
            header.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    dragX[0] = e.getX();
                    dragY[0] = e.getY();
                }
            });
            header.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    int newX = getX() + e.getX() - dragX[0];
                    int newY = getY() + e.getY() - dragY[0];
                    setLocation(newX, newY);
                    baseX = Math.round(newX / Math.max(currentScale, 0.0001f));
                    baseY = Math.round(newY / Math.max(currentScale, 0.0001f));
                }
            });
            
            add(header, BorderLayout.NORTH);
            
            content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            add(content, BorderLayout.CENTER);
        }

        public void clearModules() {
            content.removeAll();
            setBounds(getX(), getY(), 160, 25);
        }

        public void addModule(ModuleEntry entry) {
            content.add(entry.panel);
            int newHeight = 25 + content.getComponentCount() * 50;
            setBounds(getX(), getY(), 160, newHeight);
        }

        public int getModuleCount() {
            return content.getComponentCount();
        }

        public int getBaseX() { return baseX; }
        public int getBaseY() { return baseY; }
    }

    private void handleSync() throws Exception {
        try (Socket socket = new Socket("localhost", 55555);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            socket.setSoTimeout(500);
            out.println("STATUS");

            List<String[]> modStatus = new ArrayList<>();
            List<ESPWindow.EntityData> entities = new ArrayList<>();
            List<String> profileStatus = new ArrayList<>();
            boolean espEnabled = false;
            boolean statusGuiAllowed = false;

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("END"))
                    break;
                String[] p = line.split("\\|");
                if (p[0].equals("GUI_ALLOWED")) {
                    statusGuiAllowed = p.length > 1 && Boolean.parseBoolean(p[1]);
                } else if (p[0].equals("GUI_TOGGLE")) {
                    if (p[1].equals("true")) {
                        SwingUtilities.invokeLater(() -> {
                            if (guiAllowed) {
                                overlayOpen = !overlayOpen;
                                setOverlayVisible(overlayOpen);
                            }
                        });
                    }
                } else if (p[0].equals("CAMERA")) {
                    espWindow.camera.x = Double.parseDouble(p[1]);
                    espWindow.camera.y = Double.parseDouble(p[2]);
                    espWindow.camera.z = Double.parseDouble(p[3]);
                    espWindow.camera.qx = Float.parseFloat(p[4]);
                    espWindow.camera.qy = Float.parseFloat(p[5]);
                    espWindow.camera.qz = Float.parseFloat(p[6]);
                    espWindow.camera.qw = Float.parseFloat(p[7]);
                    espWindow.camera.fov = Integer.parseInt(p[8]);
                } else if (p[0].equals("WINDOW")) {
                    int wx = Integer.parseInt(p[1]);
                    int wy = Integer.parseInt(p[2]);
                    int ww = Integer.parseInt(p[3]);
                    int wh = Integer.parseInt(p[4]);
                    SwingUtilities.invokeLater(() -> {
                        espWindow.setBounds(wx, wy, ww, wh);
                        LiquidifyPanel.this.setBounds(wx, wy, ww, wh);
                        applyLayoutScale(ww, wh);
                    });
                } else if (p[0].equals("MOD")) {
                    modStatus.add(new String[] { p[1], p[2], p[3] });
                    if (p[1].equals("ESP") && p[3].equals("true"))
                        espEnabled = true;
                } else if (p[0].equals("SETTING")) {
                    String mod = p[1];
                    SettingData sd = new SettingData(p[2], p[3], p[4], p.length > 5 ? p[5] : "");
                    moduleSettings.computeIfAbsent(mod, k -> new ArrayList<>()).removeIf(s -> s.name.equals(sd.name));
                    moduleSettings.get(mod).add(sd);
                } else if (p[0].equals("ENT")) {
                    ESPWindow.EntityData ed = new ESPWindow.EntityData();
                    ed.id = Integer.parseInt(p[1]);
                    ed.minX = Double.parseDouble(p[2]);
                    ed.minY = Double.parseDouble(p[3]);
                    ed.minZ = Double.parseDouble(p[4]);
                    ed.maxX = Double.parseDouble(p[5]);
                    ed.maxY = Double.parseDouble(p[6]);
                    ed.maxZ = Double.parseDouble(p[7]);
                    ed.type = p[8];
                    entities.add(ed);
                } else if (p[0].equals("PROFILE") && p.length > 1) {
                    profileStatus.add(p[1]);
                }
            }

            final boolean showESP = espEnabled;
            final boolean finalShowNames = moduleSettings.getOrDefault("ESP", new ArrayList<>())
                    .stream().filter(s -> s.name.equals("Names")).map(s -> Boolean.parseBoolean(s.value)).findFirst()
                    .orElse(true);
            final List<ESPWindow.EntityData> finalEnts = entities;
            final List<String[]> finalMods = modStatus;
            final List<String> finalProfiles = profileStatus;
            final boolean finalGuiAllowed = statusGuiAllowed;

            SwingUtilities.invokeLater(() -> {
                guiAllowed = finalGuiAllowed;
                espWindow.setVisible(showESP);
                if (showESP) {
                    espWindow.showNames = finalShowNames;
                    espWindow.entities = finalEnts;
                    espWindow.repaint();
                }
                rebuildModuleList(finalMods);
                rebuildProfiles(finalProfiles);
            });
        }
    }

    private void setOverlayVisible(boolean visible) {
        if (visible == isVisible()) return;
        setVisible(visible);

        if (visible) {
            try {
                setExtendedState(JFrame.NORMAL);
                toFront();
                requestFocus();
                requestFocusInWindow();
            } catch (Exception ignored) {}

            // Prevent in-game camera movement while the Swing overlay is up.
            new Thread(() -> sendCommand("GUI|OPEN")).start();
        } else {
            new Thread(() -> sendCommand("GUI|CLOSE")).start();
        }
    }

    private void applyLayoutScale(int windowWidth, int windowHeight) {
        float sx = windowWidth / (float) BASE_LAYOUT_WIDTH;
        float sy = windowHeight / (float) BASE_LAYOUT_HEIGHT;
        // Scale down for small windows, but do not upscale regular/fullscreen layouts.
        float scale = Math.max(0.60f, Math.min(Math.min(sx, sy), 1.00f));
        currentScale = scale;

        int frameW = scaleInt(160, scale);
        int headerH = scaleInt(25, scale);
        int rowH = scaleInt(50, scale);

        setCategoryScaled("Combat", frameW, headerH, rowH, scale);
        setCategoryScaled("Render", frameW, headerH, rowH, scale);
        setCategoryScaled("Movement", frameW, headerH, rowH, scale);
        setCategoryScaled("Misc", frameW, headerH, rowH, scale);

        if (profilesFrame != null) {
            int px = scaleInt(850, scale);
            int py = scaleInt(50, scale);
            int pw = scaleInt(190, scale);
            int ph = scaleInt(260, scale);
            profilesFrame.setBounds(px, py, pw, ph);
        }
    }

    private void setCategoryScaled(String name, int width, int headerH, int rowH, float scale) {
        CategoryFrame cf = categories.get(name);
        if (cf == null) return;
        int x = scaleInt(cf.getBaseX(), scale);
        int y = scaleInt(cf.getBaseY(), scale);
        int h = headerH + cf.getModuleCount() * rowH;
        cf.setBounds(x, y, width, h);
    }

    private int scaleInt(int value, float scale) {
        return Math.max(1, Math.round(value * scale));
    }

    private void rebuildProfiles(List<String> names) {
        profiles.clear();
        profiles.addAll(names);
        Collections.sort(profiles, String.CASE_INSENSITIVE_ORDER);
        if (profilesFrame != null) profilesFrame.updateProfiles(profiles);
    }

    private void sendCommand(String cmd) {
        try (Socket socket = new Socket("localhost", 55555);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(cmd);
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("END"))
                    break;
            }
        } catch (Exception e) {
        }
    }

    private void rebuildModuleList(List<String[]> status) {
        if (entries.size() != status.size()) {
            for (CategoryFrame cf : categories.values()) {
                cf.clearModules();
            }
            entries.clear();
            
            for (String[] parts : status) {
                ModuleEntry entry = new ModuleEntry(parts[0], parts[1], Boolean.parseBoolean(parts[2]));
                entries.add(entry);
                String cat = getCategory(parts[0]);
                CategoryFrame cf = categories.get(cat);
                if (cf != null) {
                    cf.addModule(entry);
                }
            }
            
            for (CategoryFrame cf : categories.values()) {
                cf.revalidate();
                cf.repaint();
            }
        } else {
            for (int i = 0; i < status.size(); i++)
                entries.get(i).update(status.get(i)[1], Boolean.parseBoolean(status.get(i)[2]));
        }
    }

    private void showSettingsMenu(String moduleName, Component invoker, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(BG_CARD);
        menu.setBorder(BorderFactory.createLineBorder(ACCENT));

        List<SettingData> settings = moduleSettings.get(moduleName);
        if (settings == null || settings.isEmpty()) {
            JMenuItem item = new JMenuItem("No settings");
            item.setEnabled(false);
            menu.add(item);
        } else {
            for (SettingData s : settings) {
                if (s.type.equals("BOOL")) {
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(s.name, Boolean.parseBoolean(s.value));
                    item.addActionListener(
                            e -> sendCommand("SET|" + moduleName + "|" + s.name + "|" + item.isSelected()));
                    menu.add(item);
                } else {
                    JMenuItem item = new JMenuItem(s.name + ": " + s.value);
                    item.addActionListener(e -> {
                        String newVal = JOptionPane.showInputDialog(this, "New value for " + s.name + ":", s.value);
                        if (newVal != null)
                            sendCommand("SET|" + moduleName + "|" + s.name + "|" + newVal);
                    });
                    menu.add(item);
                }
            }
        }
        menu.show(invoker, x, y);
    }

    private class ModuleEntry {
        JPanel panel;
        JButton toggleBtn;
        JLabel keyLabel;
        String name;
        boolean enabled;

        ModuleEntry(String name, String key, boolean enabled) {
            this.name = name;
            this.enabled = enabled;
            panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.setMaximumSize(new Dimension(160, 50));
            panel.setPreferredSize(new Dimension(160, 50));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabel.setForeground(TEXT_WHITE);

            keyLabel = new JLabel("[" + key + "]");
            keyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            keyLabel.setForeground(TEXT_DIM);
            keyLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            keyLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    bindingModule = name;
                    keyLabel.setText("BINDING...");
                    keyLabel.setForeground(ACCENT);
                }
            });

            JPanel left = new JPanel(new GridLayout(2, 1));
            left.setOpaque(false);
            left.add(nameLabel);
            left.add(keyLabel);

            toggleBtn = new JButton(enabled ? "ON" : "OFF") {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ModuleEntry.this.enabled ? GREEN : RED);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                            (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
            };
            toggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
            toggleBtn.setPreferredSize(new Dimension(40, 20));
            toggleBtn.setBorderPainted(false);
            toggleBtn.setContentAreaFilled(false);
            toggleBtn.setFocusPainted(false);
            toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            toggleBtn.addActionListener(e -> new Thread(() -> sendCommand("TOGGLE " + name)).start());

            panel.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        showSettingsMenu(name, panel, e.getX(), e.getY());
                    }
                }
            });

            panel.add(left, BorderLayout.WEST);
            panel.add(toggleBtn, BorderLayout.EAST);
        }

        void update(String newKey, boolean newEnabled) {
            if (bindingModule == null) {
                keyLabel.setText("[" + newKey + "]");
                keyLabel.setForeground(TEXT_DIM);
            }
            if (this.enabled != newEnabled) {
                this.enabled = newEnabled;
                toggleBtn.setText(newEnabled ? "ON" : "OFF");
                toggleBtn.repaint();
            }
        }
    }

    private static class SettingData {
        String name, type, value, extra;

        SettingData(String n, String t, String v, String e) {
            name = n;
            type = t;
            value = v;
            extra = e;
        }
    }

    private class ProfilesFrame extends JPanel {
        private final DefaultListModel<String> profileModel = new DefaultListModel<>();
        private final JList<String> profileList = new JList<>(profileModel);

        ProfilesFrame(int x, int y) {
            setLayout(new BorderLayout());
            setBackground(BG_CARD);
            setBounds(x, y, 190, 260);

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(BG_DARK);
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT));
            JLabel title = new JLabel(" Profiles");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("Segoe UI", Font.BOLD, 12));
            header.add(title, BorderLayout.WEST);
            header.setPreferredSize(new Dimension(190, 25));
            add(header, BorderLayout.NORTH);

            JPanel controls = new JPanel(new GridLayout(1, 2, 6, 0));
            controls.setOpaque(false);
            controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            JButton saveBtn = new JButton("Save");
            JButton loadBtn = new JButton("Load");
            styleProfileButton(saveBtn);
            styleProfileButton(loadBtn);
            controls.add(saveBtn);
            controls.add(loadBtn);

            profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            profileList.setBackground(new Color(25, 25, 30, 220));
            profileList.setForeground(TEXT_WHITE);
            profileList.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            JScrollPane scrollPane = new JScrollPane(profileList);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
            scrollPane.getVerticalScrollBar().setUnitIncrement(14);

            JPanel center = new JPanel(new BorderLayout());
            center.setOpaque(false);
            center.add(controls, BorderLayout.NORTH);
            center.add(scrollPane, BorderLayout.CENTER);
            add(center, BorderLayout.CENTER);

            saveBtn.addActionListener(e -> {
                new Thread(() -> {
                    sendCommand("PROFILE|SAVE|AUTO");
                    requestProfileListRefresh();
                }).start();
            });
            loadBtn.addActionListener(e -> {
                String selected = profileList.getSelectedValue();
                if (selected == null || selected.isEmpty()) return;
                new Thread(() -> sendCommand("PROFILE|LOAD|" + selected)).start();
            });
        }

        void updateProfiles(List<String> names) {
            String selected = profileList.getSelectedValue();
            profileModel.clear();
            for (String n : names) profileModel.addElement(n);
            if (selected != null && profileModel.contains(selected)) {
                profileList.setSelectedValue(selected, true);
            }
        }

        private void styleProfileButton(JButton btn) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setBackground(ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    private void requestProfileListRefresh() {
        try (Socket socket = new Socket("localhost", 55555);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            socket.setSoTimeout(500);
            out.println("PROFILE|LIST");
            List<String> listed = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("END")) break;
                String[] p = line.split("\\|");
                if (p.length > 1 && p[0].equals("PROFILE")) listed.add(p[1]);
            }
            SwingUtilities.invokeLater(() -> rebuildProfiles(listed));
        } catch (Exception ignored) {}
    }
}