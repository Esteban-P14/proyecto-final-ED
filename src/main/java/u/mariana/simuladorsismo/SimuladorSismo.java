package u.mariana.simuladorsismo;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class SimuladorSismo extends JFrame {

    private static final int SENOIDAL = 0;
    private static final int PULSO = 1;
    private static final int ESCALON = 2;
    private static final String[] TIPOS = { "Senoidal", "Pulso (semiperíodo)", "Escalón" };

    private double m = 50000.0;
    private double c = 50000.0;
    private double k = 5000000.0;
    private double Y0 = 1.0;
    private double omega = 10.0;
    private double h = 0.01;
    private double tMax = 15.0;
    private double x0 = 0.0;
    private double v0 = 0.0;
    private int tipo = SENOIDAL;

    private boolean restableciendo = false;

    private XYSeries seriesRespuesta;
    private XYSeries seriesAnalitica;
    private XYSeries seriesSismo;
    private XYSeries seriesCurva;
    private XYSeries seriesPunto;

    private JSlider sliderM, sliderC, sliderK, sliderY0, sliderOmega, sliderH, sliderTmax, sliderX0, sliderV0;
    private JLabel labelM, labelC, labelK, labelY0, labelOmega, labelH, labelTmax, labelX0, labelV0;
    private JComboBox<String> comboTipo;
    private JLabel labelResultados;

    public SimuladorSismo() {
        super("Simulador: Edificio de un piso ante un sismo (Ecuaciones Diferenciales)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(EstilosUI.COLOR_BG);

        // --- SERIES Y GRÁFICAS ---
        seriesRespuesta = new XYSeries("x(t) numérico (RK4)");
        seriesAnalitica = new XYSeries("x(t) analítico (senoidal)");
        seriesSismo = new XYSeries("Aceleración y''(t)");
        seriesCurva = new XYSeries("X(ω) en estado estacionario");
        seriesPunto = new XYSeries("ω actual");

        XYSeriesCollection dsRespuesta = new XYSeriesCollection();
        dsRespuesta.addSeries(seriesRespuesta);
        dsRespuesta.addSeries(seriesAnalitica);

        XYSeriesCollection dsSismo = new XYSeriesCollection(seriesSismo);

        XYSeriesCollection dsCurva = new XYSeriesCollection();
        dsCurva.addSeries(seriesCurva);
        dsCurva.addSeries(seriesPunto);

        JFreeChart chartRespuesta = ChartFactory.createXYLineChart(
                "Respuesta Estructural x(t)", "Tiempo (s)", "Desplazamiento (m)",
                dsRespuesta, PlotOrientation.VERTICAL, true, true, false);

        JFreeChart chartSismo = ChartFactory.createXYLineChart(
                "Excitación Sísmica y''(t)", "Tiempo (s)", "Aceleración (m/s²)",
                dsSismo, PlotOrientation.VERTICAL, false, true, false);

        JFreeChart chartCurva = ChartFactory.createXYLineChart(
                "Curva de Resonancia X(ω)", "Frecuencia del suelo ω (rad/s)", "Amplitud X (m)",
                dsCurva, PlotOrientation.VERTICAL, true, true, false);

        EstilosUI.estilizarGrafica(chartRespuesta, EstilosUI.COLOR_PRIMARIO);
        EstilosUI.configurarSerie(chartRespuesta, 1, EstilosUI.COLOR_ANALITICA, 2.0f, true);

        EstilosUI.estilizarGrafica(chartSismo, EstilosUI.COLOR_SECUNDARIO);

        EstilosUI.estilizarGrafica(chartCurva, EstilosUI.COLOR_CURVA);
        EstilosUI.configurarMarcador(chartCurva, 1, EstilosUI.COLOR_SECUNDARIO);
        chartCurva.getXYPlot().getDomainAxis().setRange(1.0, 40.0);

        JPanel panelTiempo = new JPanel(new GridLayout(2, 1, 0, 10));
        panelTiempo.setOpaque(false);
        panelTiempo.add(new ChartPanel(chartRespuesta));
        panelTiempo.add(new ChartPanel(chartSismo));

        JPanel panelGraficas = new JPanel(new GridLayout(1, 2, 10, 0));
        panelGraficas.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panelGraficas.setBackground(EstilosUI.COLOR_BG);
        panelGraficas.add(panelTiempo);
        panelGraficas.add(new ChartPanel(chartCurva));
        add(panelGraficas, BorderLayout.CENTER);

        // --- PANEL SUPERIOR DE RESULTADOS ---
        labelResultados = new JLabel(" ");
        labelResultados.setFont(EstilosUI.fuente(Font.BOLD, 13));
        labelResultados.setForeground(EstilosUI.COLOR_TEXTO);

        JButton btnReset = new JButton("Restablecer Valores");
        EstilosUI.estilizarBoton(btnReset);
        btnReset.addActionListener(e -> restablecerValores());

        JPanel panelResultados = new JPanel(new BorderLayout(15, 0));
        panelResultados.setBackground(EstilosUI.COLOR_PANEL);
        panelResultados.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 8)));
        panelResultados.add(labelResultados, BorderLayout.CENTER);
        panelResultados.add(btnReset, BorderLayout.EAST);

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(EstilosUI.COLOR_BG);
        panelNorte.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panelNorte.add(panelResultados, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // --- CONTROLES (TRES TARJETAS) ---
        JPanel panelControles = new JPanel(new GridLayout(1, 3, 10, 0));
        panelControles.setBackground(EstilosUI.COLOR_BG);
        panelControles.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // 1. Propiedades físicas
        JPanel panelFisica = new JPanel(new GridBagLayout());
        panelFisica.setBackground(EstilosUI.COLOR_PANEL);
        panelFisica.setBorder(EstilosUI.crearBordePanel(" Propiedades Físicas del Edificio "));

        labelM = EstilosUI.crearLabel();
        sliderM = crearSliderConfig(10000, 100000, (int) m, labelM, "Masa (kg): ", "%,d", 1.0);
        labelC = EstilosUI.crearLabel();
        sliderC = crearSliderConfig(1000, 500000, (int) c, labelC, "Amort. (N·s/m): ", "%,d", 1.0);
        labelK = EstilosUI.crearLabel();
        sliderK = crearSliderConfig(1000000, 10000000, (int) k, labelK, "Rigidez (N/m): ", "%,d", 1.0);

        agregarFila(panelFisica, labelM, sliderM, 0);
        agregarFila(panelFisica, labelC, sliderC, 1);
        agregarFila(panelFisica, labelK, sliderK, 2);
        cerrarTarjeta(panelFisica, 3);

        // 2. Excitación sísmica
        JPanel panelExcitacion = new JPanel(new GridBagLayout());
        panelExcitacion.setBackground(EstilosUI.COLOR_PANEL);
        panelExcitacion.setBorder(EstilosUI.crearBordePanel(" Excitación Sísmica "));

        JLabel labelTipo = EstilosUI.crearLabel();
        labelTipo.setText("Tipo de excitación:");
        comboTipo = new JComboBox<>(TIPOS);
        EstilosUI.estilizarCombo(comboTipo);
        comboTipo.addActionListener(e -> {
            if (!restableciendo) {
                actualizarSimulacion();
            }
        });

        labelY0 = EstilosUI.crearLabel();
        sliderY0 = crearSliderConfig(10, 500, (int) (Y0 * 100), labelY0, "Amp. Sismo (m/s²): ", "%.2f", 100.0);
        labelOmega = EstilosUI.crearLabel();
        sliderOmega = crearSliderConfig(10, 400, (int) (omega * 10), labelOmega, "Freq. Sismo (rad/s): ", "%.1f", 10.0);

        agregarFila(panelExcitacion, labelTipo, comboTipo, 0);
        agregarFila(panelExcitacion, labelY0, sliderY0, 1);
        agregarFila(panelExcitacion, labelOmega, sliderOmega, 2);
        cerrarTarjeta(panelExcitacion, 3);

        // 3. Condiciones iniciales y método numérico
        JPanel panelNumerico = new JPanel(new GridBagLayout());
        panelNumerico.setBackground(EstilosUI.COLOR_PANEL);
        panelNumerico.setBorder(EstilosUI.crearBordePanel(" Condiciones Iniciales y Método Numérico "));

        labelX0 = EstilosUI.crearLabel();
        sliderX0 = crearSliderConfig(-100, 100, (int) (x0 * 1000), labelX0, "Desp. inicial x₀ (m): ", "%.3f", 1000.0);
        labelV0 = EstilosUI.crearLabel();
        sliderV0 = crearSliderConfig(-100, 100, (int) (v0 * 100), labelV0, "Vel. inicial v₀ (m/s): ", "%.2f", 100.0);
        labelH = EstilosUI.crearLabel();
        sliderH = crearSliderConfig(1, 50, (int) (h * 1000), labelH, "Paso h (s): ", "%.4f", 1000.0);
        labelTmax = EstilosUI.crearLabel();
        sliderTmax = crearSliderConfig(5, 40, (int) tMax, labelTmax, "Tiempo Máx (s): ", "%.0f", 1.0);

        agregarFila(panelNumerico, labelX0, sliderX0, 0);
        agregarFila(panelNumerico, labelV0, sliderV0, 1);
        agregarFila(panelNumerico, labelH, sliderH, 2);
        agregarFila(panelNumerico, labelTmax, sliderTmax, 3);
        cerrarTarjeta(panelNumerico, 4);

        panelControles.add(panelFisica);
        panelControles.add(panelExcitacion);
        panelControles.add(panelNumerico);
        add(panelControles, BorderLayout.SOUTH);

        actualizarSimulacion();
    }

    // ------------------------------------------------------------------
    // Utilidades de interfaz
    // ------------------------------------------------------------------

    private void agregarFila(JPanel panel, JComponent izquierda, JComponent derecha, int fila) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = fila;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new Insets(6, 0, 6, 8);
        panel.add(izquierda, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        panel.add(derecha, gbc);
    }

    private void cerrarTarjeta(JPanel panel, int fila) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = fila;
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);
    }

    private JSlider crearSliderConfig(int min, int max, int val, JLabel label, String prefijo, String formato,
            double escala) {
        JSlider slider = new JSlider(min, max, val);
        EstilosUI.estilizarSlider(slider);
        actualizarTextoLabel(label, prefijo, val / escala, formato);

        slider.addChangeListener(e -> {
            double valorReal = slider.getValue() / escala;
            actualizarTextoLabel(label, prefijo, valorReal, formato);
            if (!restableciendo) {
                actualizarSimulacion();
            }
        });
        return slider;
    }

    private void actualizarTextoLabel(JLabel label, String prefijo, double valor, String formato) {
        if (formato.equals("%,d")) {
            label.setText(prefijo + String.format("%,d", (int) valor));
        } else {
            label.setText(prefijo + String.format(formato, valor));
        }
    }

    // ------------------------------------------------------------------
    // Modelo físico y método numérico
    // ------------------------------------------------------------------

    private static double aceleracionSismo(double t, double Y0, double omega, int tipo) {
        switch (tipo) {
            case PULSO:
                return (t <= Math.PI / omega) ? Y0 : 0.0;
            case ESCALON:
                return Y0;
            default:
                return Y0 * Math.sin(omega * t);
        }
    }

    private static double[] f_v(double x, double v, double t, double m, double c, double k,
            double Y0, double omega, int tipo) {
        double dxdt = v;
        double dvdt = -(c / m) * v - (k / m) * x - aceleracionSismo(t, Y0, omega, tipo);
        return new double[] { dxdt, dvdt };
    }

    public static double[][] resolverRK4(double m, double c, double k, double Y0, double omega, int tipo,
            double x0, double v0, double tMax, double h) {
        int n = (int) Math.round(tMax / h) + 1;
        double[] t = new double[n];
        double[] x = new double[n];
        double[] v = new double[n];

        t[0] = 0.0;
        x[0] = x0;
        v[0] = v0;

        for (int i = 0; i < n - 1; i++) {
            double ti = t[i];
            double xi = x[i];
            double vi = v[i];

            double[] k1 = f_v(xi, vi, ti, m, c, k, Y0, omega, tipo);
            double[] k2 = f_v(xi + 0.5 * h * k1[0], vi + 0.5 * h * k1[1], ti + 0.5 * h, m, c, k, Y0, omega, tipo);
            double[] k3 = f_v(xi + 0.5 * h * k2[0], vi + 0.5 * h * k2[1], ti + 0.5 * h, m, c, k, Y0, omega, tipo);
            double[] k4 = f_v(xi + h * k3[0], vi + h * k3[1], ti + h, m, c, k, Y0, omega, tipo);

            x[i + 1] = xi + (h / 6.0) * (k1[0] + 2 * k2[0] + 2 * k3[0] + k4[0]);
            v[i + 1] = vi + (h / 6.0) * (k1[1] + 2 * k2[1] + 2 * k3[1] + k4[1]);
            t[i + 1] = ti + h;
        }
        return new double[][] { t, x, v };
    }

    /**
     * Coeficientes {A, B, C1, C2, wd} de la solución analítica (excitación
     * senoidal, ζ < 1).
     */
    private static double[] coeficientesAnaliticos(double wn, double zeta, double Y0, double omega,
            double x0, double v0) {
        double wd = wn * Math.sqrt(1.0 - zeta * zeta);
        double D = wn * wn - omega * omega;
        double E = 2.0 * zeta * wn * omega;
        double den = D * D + E * E;
        double A = -Y0 * D / den;
        double B = Y0 * E / den;
        double C1 = x0 - B;
        double C2 = (v0 - omega * A + zeta * wn * C1) / wd;
        return new double[] { A, B, C1, C2, wd };
    }

    private static double amplitudEstacionaria(double w, double wn, double zeta, double Y0) {
        double D = wn * wn - w * w;
        double E = 2.0 * zeta * wn * w;
        return Y0 / Math.sqrt(D * D + E * E);
    }

    // ------------------------------------------------------------------
    // Actualización de la simulación
    // ------------------------------------------------------------------

    private void actualizarSimulacion() {
        m = sliderM.getValue();
        c = sliderC.getValue();
        k = sliderK.getValue();
        Y0 = sliderY0.getValue() / 100.0;
        omega = sliderOmega.getValue() / 10.0;
        h = sliderH.getValue() / 1000.0;
        tMax = sliderTmax.getValue();
        x0 = sliderX0.getValue() / 1000.0;
        v0 = sliderV0.getValue() / 100.0;
        tipo = comboTipo.getSelectedIndex();

        double[][] res = resolverRK4(m, c, k, Y0, omega, tipo, x0, v0, tMax, h);
        double[] t = res[0];
        double[] x = res[1];

        double wn = Math.sqrt(k / m);
        double zeta = c / (2.0 * Math.sqrt(k * m));
        boolean hayAnalitica = (tipo == SENOIDAL) && zeta < 1.0;
        double[] coef = hayAnalitica ? coeficientesAnaliticos(wn, zeta, Y0, omega, x0, v0) : null;

        XYSeries[] todas = { seriesRespuesta, seriesAnalitica, seriesSismo, seriesCurva, seriesPunto };
        for (XYSeries s : todas) {
            s.setNotify(false);
            s.clear();
        }

        for (int i = 0; i < t.length; i++) {
            seriesRespuesta.add(t[i], x[i]);
            seriesSismo.add(t[i], aceleracionSismo(t[i], Y0, omega, tipo));
            if (hayAnalitica) {
                double xa = coef[0] * Math.sin(omega * t[i]) + coef[1] * Math.cos(omega * t[i])
                        + Math.exp(-zeta * wn * t[i])
                                * (coef[2] * Math.cos(coef[4] * t[i]) + coef[3] * Math.sin(coef[4] * t[i]));
                seriesAnalitica.add(t[i], xa);
            }
        }

        for (double w = 1.0; w <= 40.0 + 1e-9; w += 0.1) {
            seriesCurva.add(w, amplitudEstacionaria(w, wn, zeta, Y0));
        }
        seriesPunto.add(omega, amplitudEstacionaria(omega, wn, zeta, Y0));

        // Al reactivar la notificación, cada serie avisa a su gráfica una sola vez
        for (XYSeries s : todas) {
            s.setNotify(true);
        }

        actualizarResultados(x, wn, zeta);
    }

    private void actualizarResultados(double[] x, double wn, double zeta) {
        double xMax = Arrays.stream(x).map(Math::abs).max().orElse(0.0);

        String regimen;
        if (zeta < 0.999) {
            regimen = "subamortiguado";
        } else if (zeta > 1.001) {
            regimen = "sobreamortiguado";
        } else {
            regimen = "crítico";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ωn = %.2f rad/s   |   ζ = %.3f (%s)   |   |x|máx = %.4f m",
                wn, zeta, regimen, xMax));

        boolean resonancia = false;
        if (tipo == SENOIDAL) {
            double ratio = omega / wn;
            double xEst = amplitudEstacionaria(omega, wn, zeta, Y0);
            sb.append(String.format("   |   ω/ωn = %.2f   |   X estacionario = %.4f m", ratio, xEst));
            resonancia = ratio >= 0.9 && ratio <= 1.1;
        }
        if (resonancia) {
            sb.append("   ⚠ RESONANCIA");
        }

        labelResultados.setText(sb.toString());
        labelResultados.setForeground(resonancia ? EstilosUI.COLOR_SECUNDARIO : EstilosUI.COLOR_TEXTO);
    }

    private void restablecerValores() {
        restableciendo = true;
        sliderM.setValue(50000);
        sliderC.setValue(50000);
        sliderK.setValue(5000000);
        sliderY0.setValue(100);
        sliderOmega.setValue(100);
        sliderH.setValue(10);
        sliderTmax.setValue(15);
        sliderX0.setValue(0);
        sliderV0.setValue(0);
        comboTipo.setSelectedIndex(SENOIDAL);
        restableciendo = false;
        actualizarSimulacion();
    }

    // ------------------------------------------------------------------
    // Validación del método numérico (se ejecuta una sola vez, en consola)
    // ------------------------------------------------------------------

    private static void ejecutarValidacion() {
        System.out.println("=== REPORTE DE VALIDACIÓN DEL MÉTODO NUMÉRICO (RK4) ===");
        double mVal = 50000, cVal = 50000, kVal = 5000000, Y0Val = 1.0, omegaVal = 10.0, tMaxVal = 5.0;
        double[] pasos = { 0.1, 0.05, 0.025, 0.0125 };
        double[] errores = new double[pasos.length];

        double[][] ref = resolverRK4(mVal, cVal, kVal, Y0Val, omegaVal, SENOIDAL, 0.0, 0.0, tMaxVal, 0.001);
        double[] xRef = ref[1];

        System.out.printf("%-10s | %-20s | %-15s%n", "Paso (h)", "Error Global e(h)", "Razón de Error");
        System.out.println("---------------------------------------------------");

        for (int idx = 0; idx < pasos.length; idx++) {
            double hStep = pasos[idx];
            double[][] sol = resolverRK4(mVal, cVal, kVal, Y0Val, omegaVal, SENOIDAL, 0.0, 0.0, tMaxVal, hStep);
            double[] tNum = sol[0];
            double[] xNum = sol[1];

            double errorMax = 0.0;
            for (int i = 0; i < tNum.length; i++) {
                int refIdx = (int) Math.round(tNum[i] / 0.001);
                if (refIdx < xRef.length) {
                    double diff = Math.abs(xNum[i] - xRef[refIdx]);
                    if (diff > errorMax) {
                        errorMax = diff;
                    }
                }
            }
            errores[idx] = errorMax;
            String razon = (idx == 0) ? "-" : String.format("%.2f", errores[idx - 1] / errorMax);
            System.out.printf("%-10.4f | %-20.6e | %-15s%n", hStep, errorMax, razon);
        }
        System.out.println("Nota: Para RK4, al reducir h a la mitad, el error disminuye un factor aproximado de 16.\n");
    }

    public static void main(String[] args) {
        // Opcional: si agregas FlatLaf al proyecto, la interfaz se ve moderna; si no,
        // usa el estilo por defecto
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception ex) {
            // se mantiene el Look and Feel por defecto
        }
        ejecutarValidacion();
        SwingUtilities.invokeLater(() -> new SimuladorSismo().setVisible(true));
    }
}