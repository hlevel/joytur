package cn.joytur.common.extensions.wechat.weblistener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

class ImageViewer extends JFrame {
    private JLabel label = new JLabel();

    ImageViewer(byte[] data) {
        setTitle("微信登录");
        setSize(400, 420);
        getContentPane().add(label, BorderLayout.CENTER);
        getContentPane().setBackground(Color.WHITE);

        setImage(data);
        //为主面板添加窗口监听器
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                label.setSize(getWidth(), getHeight());
            }
        });
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    void setImage(byte[] data) {
        label.setIcon(new ScaleIcon(new ImageIcon(data)));
    }

    static class ScaleIcon implements Icon {

        private Icon icon = null;

        ScaleIcon(Icon icon) {
            this.icon = icon;
        }

        @Override
        public int getIconHeight() {
            return icon.getIconHeight();
        }

        @Override
        public int getIconWidth() {
            return icon.getIconWidth();
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            float length = Math.min(c.getWidth(), c.getHeight());
            int iconWid = icon.getIconWidth();
            int iconHei = icon.getIconHeight();

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.scale(length / iconWid, length / iconHei);
            icon.paintIcon(c, g2d, 0, 0);
        }

    }
}
