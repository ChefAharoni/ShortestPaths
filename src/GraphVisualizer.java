/**
 * Written by ChatGPT 5
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class GraphVisualizer {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Weighted Graph Visualizer");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GraphPanel graphPanel = new GraphPanel();
            f.setLayout(new BorderLayout());
            f.add(makeToolbar(graphPanel), BorderLayout.NORTH);
            f.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

            f.setSize(1000, 800);
            f.setLocationRelativeTo(null);
            f.setVisible(true);

            if (args.length == 1) {
                try { graphPanel.loadFromFile(Paths.get(args[0])); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(f, "Failed to load file:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private static JComponent makeToolbar(GraphPanel g) {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        JButton open = new JButton("Open…");
        open.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(bar) == JFileChooser.APPROVE_OPTION) {
                try { g.loadFromFile(chooser.getSelectedFile().toPath()); }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(bar, "Failed to load file:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JCheckBox arrows = new JCheckBox("Directed (arrows)", true);
        arrows.addActionListener(e -> g.setShowArrows(arrows.isSelected()));

        JCheckBox curveOpp = new JCheckBox("Curve antiparallel", true);
        curveOpp.addActionListener(e -> g.setCurveOppositeEdges(curveOpp.isSelected()));

        JCheckBox verbose = new JCheckBox("Verbose labels", true);
        verbose.addActionListener(e -> g.setVerboseLabels(verbose.isSelected()));

        bar.add(open);
        bar.addSeparator();
        bar.add(arrows);
        bar.add(curveOpp);
        bar.add(verbose);
        bar.addSeparator();
        bar.add(new JLabel("  Format: first line N, then lines \"FROM TO WEIGHT\""));
        return bar;
    }
}

class GraphPanel extends JPanel {

    private static final int NODE_RADIUS = 28;
    private static final int MARGIN = 90;

    private final java.util.List<Character> vertices = new ArrayList<>();
    private final java.util.List<Edge> edges = new ArrayList<>();
    private final Map<Character, Point2D.Double> pos = new HashMap<>();

    private boolean showArrows = true;
    private boolean curveOppositeEdges = true;
    private boolean verboseLabels = true;

    GraphPanel() {
        setBackground(Color.WHITE);
        setOpaque(true);
        setPreferredSize(new Dimension(1100, 850));
    }

    void setShowArrows(boolean v) { showArrows = v; repaint(); }
    void setCurveOppositeEdges(boolean v) { curveOppositeEdges = v; repaint(); }
    void setVerboseLabels(boolean v) { verboseLabels = v; repaint(); }

    void loadFromFile(Path path) throws IOException {
        vertices.clear(); edges.clear(); pos.clear();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String first = nextNonEmpty(br);
            if (first == null) throw new IOException("Empty file.");
            int n = Integer.parseInt(first.trim());
            if (n <= 0) throw new IOException("Number of vertices must be positive.");
            if (n > 52) throw new IOException("Supports up to 52 vertices (A..Z, a..z).");

            for (int i = 0; i < n; i++) {
                char label = (i < 26) ? (char)('A' + i) : (char)('a' + (i - 26));
                vertices.add(label);
            }

            String line; int lineNo = 1;
            while ((line = nextNonEmpty(br)) != null) {
                lineNo++;
                String[] p = line.trim().split("\\s+");
                if (p.length != 3) throw new IOException("Line " + lineNo + ": expected 3 tokens.");
                char u = parseVertex(p[0], lineNo), v = parseVertex(p[1], lineNo);
                if (!vertices.contains(u) || !vertices.contains(v))
                    throw new IOException("Line " + lineNo + ": vertex out of range.");
                int w;
                try { w = Integer.parseInt(p[2]); }
                catch (NumberFormatException nfe) { throw new IOException("Line " + lineNo + ": weight must be int."); }
                edges.add(new Edge(u, v, w));
            }
        }
        revalidate(); repaint();
    }

    private static String nextNonEmpty(BufferedReader br) throws IOException {
        String s; while ((s = br.readLine()) != null) if (!s.trim().isEmpty()) return s; return null;
    }
    private static char parseVertex(String token, int lineNo) throws IOException {
        if (token.length() != 1 || !Character.isLetter(token.charAt(0)))
            throw new IOException("Line " + lineNo + ": vertex must be a single letter.");
        return token.charAt(0);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (vertices.isEmpty()) { drawHint((Graphics2D) g); return; }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        layoutOnCircle();

        // Edge pass
        for (Edge e : edges) drawEdge(g2, e);

        // Node pass
        for (char v : vertices) drawVertex(g2, v);

        g2.dispose();
    }

    private void drawHint(Graphics2D g2) {
        g2.setColor(new Color(0,0,0,160));
        g2.setFont(getFont().deriveFont(Font.PLAIN, 16f));
        String msg = "Open a file (first line: N, then lines like \"A C 2\")";
        int x = (getWidth() - g2.getFontMetrics().stringWidth(msg)) / 2;
        int y = getHeight() / 2;
        g2.drawString(msg, x, y);
    }

    private void layoutOnCircle() {
        int w = Math.max(1, getWidth()), h = Math.max(1, getHeight());
        double cx = w / 2.0, cy = h / 2.0, r = Math.max(0, Math.min(w, h) / 2.0 - MARGIN);
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            double theta = (2 * Math.PI * i) / n - Math.PI / 2;
            pos.put(vertices.get(i), new Point2D.Double(cx + r * Math.cos(theta), cy + r * Math.sin(theta)));
        }
    }

    private void drawVertex(Graphics2D g2, char v) {
        Point2D p = pos.get(v);
        int x = (int)Math.round(p.getX()) - NODE_RADIUS;
        int y = (int)Math.round(p.getY()) - NODE_RADIUS;

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(245,247,252));
        g2.fillOval(x, y, NODE_RADIUS*2, NODE_RADIUS*2);
        g2.setColor(new Color(60,60,80));
        g2.drawOval(x, y, NODE_RADIUS*2, NODE_RADIUS*2);

        g2.setFont(getFont().deriveFont(Font.BOLD, 16f));
        String s = String.valueOf(v);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, (int)(p.getX() - fm.stringWidth(s)/2.0), (int)(p.getY() + fm.getAscent()/2.5));
    }

    private void drawEdge(Graphics2D g2, Edge e) {
        Point2D p0 = pos.get(e.from), p2 = pos.get(e.to);
        if (p0 == null || p2 == null) return;

        // Trim to node borders
        double dx = p2.getX() - p0.getX(), dy = p2.getY() - p0.getY();
        double angle = Math.atan2(dy, dx);
        double cos = Math.cos(angle), sin = Math.sin(angle);
        double sx = p0.getX() + cos * NODE_RADIUS, sy = p0.getY() + sin * NODE_RADIUS;
        double ex = p2.getX() - cos * NODE_RADIUS, ey = p2.getY() - sin * NODE_RADIUS;

        // Decide if this pair has the opposite edge
        boolean hasOpp = hasReverse(e);
        Shape shape;
        Point2D ctrl;

        if (curveOppositeEdges && hasOpp && e.from != e.to) {
            // Quadratic curve; bend direction is deterministic so pairs don't overlap
            double nx = -sin, ny = cos; // unit normal to the straight line
            double bend = 42;           // curvature magnitude (px)
            double sign = (e.from < e.to) ? +1.0 : -1.0;
            double mx = (sx + ex)/2.0, my = (sy + ey)/2.0;
            double cx = mx + sign * bend * nx;
            double cy = my + sign * bend * ny;

            QuadCurve2D q = new QuadCurve2D.Double(sx, sy, cx, cy, ex, ey);
            shape = q;
            ctrl = new Point2D.Double(cx, cy);

            // Edge stroke
            g2.setColor(new Color(120,130,155,210));
            g2.setStroke(new BasicStroke(1.9f));
            g2.draw(shape);

            // Arrowhead tangent to curve at end; derivative at t=1 is 2*(P2 - C)
            double tx = ex - cx, ty = ey - cy; // (P2 - C)
            double endAngle = Math.atan2(ty, tx);
            if (showArrows) drawArrowHead(g2, ex, ey, endAngle);

            // Label near arrowhead along the curve (t ~ 0.82)
            placeLabelOnQuad(g2, sx, sy, cx, cy, ex, ey, 0.82, e);
        } else {
            // Straight line
            Line2D line = new Line2D.Double(sx, sy, ex, ey);
            shape = line;
            g2.setColor(new Color(120,130,155,210));
            g2.setStroke(new BasicStroke(1.9f));
            g2.draw(shape);

            if (showArrows) drawArrowHead(g2, ex, ey, angle);

            // Label near arrowhead (slightly before end)
            placeLabelOnLine(g2, sx, sy, ex, ey, e);
        }
    }

    private boolean hasReverse(Edge e) {
        for (Edge other : edges) if (other.from == e.to && other.to == e.from) return true;
        return false;
    }

    private void placeLabelOnLine(Graphics2D g2, double sx, double sy, double ex, double ey, Edge e) {
        double t = 0.78; // along the line toward the arrowhead
        double mx = sx + t*(ex - sx), my = sy + t*(ey - sy);
        double nx = -(ey - sy), ny = (ex - sx); // a normal (not unit)
        double nlen = Math.hypot(nx, ny); if (nlen == 0) nlen = 1;
        nx /= nlen; ny /= nlen;

        drawEdgeLabel(g2, mx + nx*14, my + ny*14, e);
    }

    private void placeLabelOnQuad(Graphics2D g2,
                                  double x0, double y0, double cx, double cy, double x2, double y2,
                                  double t, Edge e) {
        // Quadratic Bézier point
        double omt = 1 - t;
        double bx = omt*omt*x0 + 2*omt*t*cx + t*t*x2;
        double by = omt*omt*y0 + 2*omt*t*cy + t*t*y2;

        // Tangent for a normal
        double tx = 2*omt*(cx - x0) + 2*t*(x2 - cx);
        double ty = 2*omt*(cy - y0) + 2*t*(y2 - cy);

        double nlen = Math.hypot(tx, ty); if (nlen == 0) nlen = 1;
        double nx = -ty / nlen, ny = tx / nlen;

        drawEdgeLabel(g2, bx + nx*14, by + ny*14, e);
    }

    private void drawEdgeLabel(Graphics2D g2, double x, double y, Edge e) {
        String txt = verboseLabels ? (e.from + "→" + e.to + ": " + e.weight) : String.valueOf(e.weight);
        Font f = getFont().deriveFont(Font.PLAIN, 14f);
        g2.setFont(f);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(txt), th = fm.getHeight();
        int tx = (int)Math.round(x - tw/2.0);
        int ty = (int)Math.round(y + fm.getAscent()/2.5);
        int pad = 3;

        g2.setColor(new Color(255,255,210,230));            // soft yellow badge
        g2.fillRoundRect(tx - pad, ty - fm.getAscent() - pad, tw + 2*pad, th + 2*pad, 10, 10);
        g2.setColor(new Color(50,50,50));
        g2.drawString(txt, tx, ty);
    }

    private void drawArrowHead(Graphics2D g2, double x, double y, double angle) {
        double len = 12, phi = Math.toRadians(22);
        double x1 = x - len * Math.cos(angle - phi), y1 = y - len * Math.sin(angle - phi);
        double x2 = x - len * Math.cos(angle + phi), y2 = y - len * Math.sin(angle + phi);
        Stroke old = g2.getStroke();
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new Line2D.Double(x, y, x1, y1));
        g2.draw(new Line2D.Double(x, y, x2, y2));
        g2.setStroke(old);
    }

    private static class Edge {
        final char from, to; final int weight;
        Edge(char f, char t, int w) { from = f; to = t; weight = w; }
    }
}