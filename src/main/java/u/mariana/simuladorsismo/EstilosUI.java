package u.mariana.simuladorsismo;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class EstilosUI {
    public static final Color COLOR_BG = new Color(245, 247, 250);
    public static final Color COLOR_PANEL = Color.WHITE;
    public static final Color COLOR_TEXTO = new Color(40, 44, 52);
    public static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    public static final Color COLOR_PRIMARIO_HOVER = new Color(31, 97, 141);
    public static final Color COLOR_SECUNDARIO = new Color(231, 76, 60);
    public static final Color COLOR_ANALITICA = new Color(230, 126, 34);
    public static final Color COLOR_CURVA = new Color(142, 68, 173);
    public static final Color COLOR_GRID = new Color(220, 224, 230);

    private static final String FAMILIA = elegirFamilia();

    private static String elegirFamilia() {
        String[] preferidas = { "Segoe UI", "SF Pro Text", "Helvetica Neue", "Roboto", "Arial" };
        Set<String> disponibles = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : preferidas) {
            if (disponibles.contains(f)) {
                return f;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Font fuente(int estilo, int tamaño) {
        return new Font(FAMILIA, estilo, tamaño);
    }

    public static JLabel crearLabel() {
        JLabel label = new JLabel();
        label.setFont(fuente(Font.PLAIN, 13));
        label.setForeground(COLOR_TEXTO);
        label.setPreferredSize(new Dimension(200, 24)); // ancho fijo: todo queda alineado
        return label;
    }

    public static void estilizarSlider(JSlider slider) {
        slider.setBackground(COLOR_PANEL);
        slider.setOpaque(false);
        slider.setFocusable(false);
    }

    public static void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(fuente(Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(COLOR_TEXTO);
        combo.setFocusable(false);
    }

    public static void estilizarBoton(JButton boton) {
        boton.setFont(fuente(Font.BOLD, 13));
        boton.setBackground(COLOR_PRIMARIO);
        boton.setForeground(Color.WHITE);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(COLOR_PRIMARIO_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(COLOR_PRIMARIO);
            }
        });
    }

    public static Border crearBordePanel(String titulo) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true),
                                titulo,
                                TitledBorder.LEFT,
                                TitledBorder.TOP,
                                fuente(Font.BOLD, 13),
                                COLOR_TEXTO),
                        BorderFactory.createEmptyBorder(6, 10, 8, 10)));
    }

    public static void estilizarGrafica(JFreeChart chart, Color colorSerie) {
        chart.setBackgroundPaint(COLOR_PANEL);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        chart.getTitle().setFont(fuente(Font.BOLD, 15));
        chart.getTitle().setPaint(COLOR_TEXTO);

        if (chart.getLegend() != null) {
            chart.getLegend().setFrame(BlockBorder.NONE);
            chart.getLegend().setItemFont(fuente(Font.PLAIN, 12));
        }

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinePaint(COLOR_GRID);
        plot.setRangeGridlinePaint(COLOR_GRID);

        for (ValueAxis eje : new ValueAxis[] { plot.getDomainAxis(), plot.getRangeAxis() }) {
            eje.setLabelFont(fuente(Font.PLAIN, 12));
            eje.setTickLabelFont(fuente(Font.PLAIN, 11));
            eje.setLabelPaint(COLOR_TEXTO);
            eje.setTickLabelPaint(COLOR_TEXTO);
        }
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getDomainAxis().setUpperMargin(0.0);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(renderer);
        configurarSerie(chart, 0, colorSerie, 2.2f, false);
    }

    public static void configurarSerie(JFreeChart chart, int idx, Color color, float grosor, boolean punteada) {
        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
        r.setSeriesPaint(idx, color);
        float[] guion = punteada ? new float[] { 8f, 6f } : null;
        int cap = punteada ? BasicStroke.CAP_BUTT : BasicStroke.CAP_ROUND;
        r.setSeriesStroke(idx, new BasicStroke(grosor, cap, BasicStroke.JOIN_ROUND, 10f, guion, 0f));
    }

    public static void configurarMarcador(JFreeChart chart, int idx, Color color) {
        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
        r.setSeriesLinesVisible(idx, false);
        r.setSeriesShapesVisible(idx, true);
        r.setSeriesShape(idx, new Ellipse2D.Double(-6, -6, 12, 12));
        r.setSeriesPaint(idx, color);
    }
}