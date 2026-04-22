package com.liquidify.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ESPWindow extends JFrame {
    public List<EntityData> entities = new ArrayList<>();
    public CameraData camera = new CameraData();
    public boolean showNames = true;
    private final String UNIQUE_TITLE;

    public ESPWindow() {
        this.UNIQUE_TITLE = "LiquidifyEXTERNAL" + System.currentTimeMillis();
        setTitle(UNIQUE_TITLE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        setType(Type.UTILITY);
        setFocusable(false);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setComposite(AlphaComposite.SrcOver);

                if (entities.isEmpty()) {
                    g2.dispose();
                    return;
                }

                float aspect = (float) getWidth() / getHeight();
                Matrix4 proj = Matrix4.perspective((float) Math.toRadians(camera.fov), aspect, 0.05f, 1000f);
                Matrix4 view = Matrix4.fromQuaternion(camera.qx, camera.qy, camera.qz, camera.qw).invert();

                for (EntityData e : entities) {
                    float[][] corners = {
                            { (float) (e.minX - camera.x), (float) (e.minY - camera.y), (float) (e.minZ - camera.z) },
                            { (float) (e.maxX - camera.x), (float) (e.minY - camera.y), (float) (e.minZ - camera.z) },
                            { (float) (e.minX - camera.x), (float) (e.maxY - camera.y), (float) (e.minZ - camera.z) },
                            { (float) (e.maxX - camera.x), (float) (e.maxY - camera.y), (float) (e.minZ - camera.z) },
                            { (float) (e.minX - camera.x), (float) (e.minY - camera.y), (float) (e.maxZ - camera.z) },
                            { (float) (e.maxX - camera.x), (float) (e.minY - camera.y), (float) (e.maxZ - camera.z) },
                            { (float) (e.minX - camera.x), (float) (e.maxY - camera.y), (float) (e.maxZ - camera.z) },
                            { (float) (e.maxX - camera.x), (float) (e.maxY - camera.y), (float) (e.maxZ - camera.z) }
                    };

                    float minSlightX = Float.MAX_VALUE, minSlightY = Float.MAX_VALUE;
                    float maxSlightX = -Float.MAX_VALUE, maxSlightY = -Float.MAX_VALUE;
                    boolean anyVisible = false;

                    for (float[] corner : corners) {
                        float[] screen = project(corner[0], corner[1], corner[2], view, proj);
                        if (screen != null) {
                            anyVisible = true;
                            if (screen[0] < minSlightX)
                                minSlightX = screen[0];
                            if (screen[0] > maxSlightX)
                                maxSlightX = screen[0];
                            if (screen[1] < minSlightY)
                                minSlightY = screen[1];
                            if (screen[1] > maxSlightY)
                                maxSlightY = screen[1];
                        }
                    }

                    if (anyVisible) {
                        g2.setColor(Color.RED);
                        int x = (int) minSlightX;
                        int y = (int) minSlightY;
                        int w = (int) (maxSlightX - minSlightX);
                        int h = (int) (maxSlightY - minSlightY);
                        g2.drawRect(x, y, w, h);

                        if (showNames) {
                            g2.setFont(new Font("Tahoma", Font.BOLD, 10));
                            String name = e.type;
                            int tw = g2.getFontMetrics().stringWidth(name);
                            g2.setColor(new Color(0, 0, 0, 120));
                            g2.fillRect(x + w / 2 - tw / 2 - 2, y - 14, tw + 4, 12);
                            g2.setColor(Color.WHITE);
                            g2.drawString(name, x + w / 2 - tw / 2, y - 4);
                        }
                    }
                }
                g2.dispose();
            }
        };
        content.setOpaque(false);
        add(content);
        setVisible(true);

        // Apply Stream-Proofing via PowerShell (Cross-Java Version Hack)
        new Thread(() -> {
            try {
                Thread.sleep(100); // Wait for window to be fully registered
                String ps = "$c='[DllImport(\"user32.dll\")]public static extern IntPtr FindWindow(string lC,string lW);[DllImport(\"user32.dll\")]public static extern bool SetWindowDisplayAffinity(IntPtr h,uint a);';$t=Add-Type -MemberDefinition $c -Name 'W' -PassThru;$h=$t::FindWindow($null,'"
                        + UNIQUE_TITLE + "');if($h -ne 0){$t::SetWindowDisplayAffinity($h,0x11)}";
                new ProcessBuilder("powershell", "-Command", ps).start();
                System.out.println("[Liquidify] Stream-Proofing requested for: " + UNIQUE_TITLE);
            } catch (Exception e) {
            }
        }).start();
    }

    private float[] project(float x, float y, float z, Matrix4 view, Matrix4 proj) {
        float[] v = { x, y, z, 1.0f };
        float[] vView = view.multiply(v);
        float[] vProj = proj.multiply(vView);
        if (vProj[3] <= 0.05f)
            return null;
        float sx = (vProj[0] / vProj[3] * 0.5f + 0.5f) * getWidth();
        float sy = (1.0f - (vProj[1] / vProj[3] * 0.5f + 0.5f)) * getHeight();
        return new float[] { sx, sy };
    }

    public static class EntityData {
        public int id;
        public double minX, minY, minZ, maxX, maxY, maxZ;
        public String type;
    }

    public static class CameraData {
        public double x, y, z;
        public float qx, qy, qz, qw;
        public int fov;
    }

    private static class Matrix4 {
        float[] m = new float[16];

        static Matrix4 perspective(float fov, float aspect, float near, float far) {
            Matrix4 res = new Matrix4();
            float t = (float) Math.tan(fov / 2.0);
            res.m[0] = 1.0f / (aspect * t);
            res.m[5] = 1.0f / t;
            res.m[10] = -(far + near) / (far - near);
            res.m[11] = -(2.0f * far * near) / (far - near);
            res.m[14] = -1.0f;
            res.m[15] = 0.0f;
            return res;
        }

        static Matrix4 fromQuaternion(float x, float y, float z, float w) {
            Matrix4 res = new Matrix4();
            res.m[0] = 1 - 2 * y * y - 2 * z * z;
            res.m[1] = 2 * x * y - 2 * z * w;
            res.m[2] = 2 * x * z + 2 * y * w;
            res.m[4] = 2 * x * y + 2 * z * w;
            res.m[5] = 1 - 2 * x * x - 2 * z * z;
            res.m[6] = 2 * y * z - 2 * x * w;
            res.m[8] = 2 * x * z - 2 * y * w;
            res.m[9] = 2 * y * z + 2 * x * w;
            res.m[10] = 1 - 2 * x * x - 2 * y * y;
            res.m[15] = 1.0f;
            return res;
        }

        Matrix4 invert() {
            Matrix4 res = new Matrix4();
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    res.m[i * 4 + j] = m[j * 4 + i];
            res.m[15] = 1.0f;
            return res;
        }

        float[] multiply(float[] v) {
            float[] res = new float[4];
            for (int i = 0; i < 4; i++)
                res[i] = m[i * 4] * v[0] + m[i * 4 + 1] * v[1] + m[i * 4 + 2] * v[2] + m[i * 4 + 3] * v[3];
            return res;
        }
    }
}
