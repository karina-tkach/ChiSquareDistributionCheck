package uni;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ChiSquareTestSwing {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChiSquareTestSwing::new);
    }

    public ChiSquareTestSwing() {
        runProgram();
    }

    private void runProgram() {
        List<Integer> numbers = inputNumbers();
        if (numbers == null) return;

        Double alpha = inputAlpha();
        if (alpha == null) return;

        // Обчислення емпіричних χ²
        double chiEmpUniform = chiSquareUniform(numbers);
        double chiEmpNormal = chiSquareNormal(numbers);

        int n = 21;
        int kUniform = n - 1;
        int kNormal = n - 3;
        double chiCritUniform = chiSquareCriticalGoldstein(kUniform, alpha);
        double chiCritNormal = chiSquareCriticalGoldstein(kNormal, alpha);

        // Формування результатів
        StringBuilder sb = new StringBuilder();
        sb.append("Вибірка: ").append(numbers).append("\n\n");
        sb.append(String.format("χ²(емп) для рівномірного: %.4f%n", chiEmpUniform));
        sb.append(String.format("χ²(емп) для нормального: %.4f%n", chiEmpNormal));
        sb.append(String.format("χ²(кр. для рівн., α=%.2f) ≈ %.4f%n", alpha, chiCritUniform));
        sb.append(String.format("χ²(кр. для норм., α=%.2f) ≈ %.4f%n%n", alpha, chiCritNormal));

        sb.append("Перевірка гіпотез:\n");
        sb.append("Рівномірний розподіл: ")
                .append(chiEmpUniform < chiCritUniform ? "НЕ відхиляється H₀" : "Відхиляється H₀").append("\n");
        sb.append("Нормальний розподіл: ")
                .append(chiEmpNormal < chiCritNormal ? "НЕ відхиляється H₀" : "Відхиляється H₀").append("\n");

        // Частоти спостережень
        int[] freq = new int[21];
        for (int x : numbers) freq[x]++;

        // Обчислення середнього та σ
        double mean = numbers.stream().mapToDouble(x -> x).average().orElse(0);
        double variance = numbers.stream().mapToDouble(x -> Math.pow(x - mean, 2)).sum() / numbers.size();
        double std = Math.sqrt(variance);

        // Очікувані частоти
        double[] expectedUniform = new double[21];
        double[] expectedNormal = new double[21];
        for (int i = 0; i < 21; i++) {
            expectedUniform[i] = (double) numbers.size() / 21;
            expectedNormal[i] = numbers.size() * normalProb(i, mean, std);
        }

        showResults(sb.toString(), freq, expectedUniform, expectedNormal);
    }

    // Ввід даних

    private List<Integer> inputNumbers() {
        while (true) {
            try {
                String input = JOptionPane.showInputDialog(
                        null,
                        "Введіть цілі числа від 0 до 20 через пробіл:",
                        "Ввід даних",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (input == null) return null;

                List<Integer> numbers = new ArrayList<>();
                for (String s : input.trim().split("\\s+")) {
                    int val = Integer.parseInt(s);
                    if (val < 0 || val > 20)
                        throw new IllegalArgumentException("Число " + val + " не в діапазоні [0;20]");
                    numbers.add(val);
                }

                if(numbers.size() < 50) {
                    throw new IllegalArgumentException("Вибірка повинна містити більше 50 елементів");
                }
                return numbers;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Помилка: " + e.getMessage() + "\nСпробуйте ще раз.",
                        "Помилка вводу", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Double inputAlpha() {
        while (true) {
            try {
                String alphaStr = JOptionPane.showInputDialog(
                        null,
                        "Введіть рівень значущості α (наприклад, 0.05):",
                        "Рівень α",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (alphaStr == null) return null;

                double alpha = Double.parseDouble(alphaStr);
                if (alpha <= 0 || alpha >= 1)
                    throw new IllegalArgumentException("α повинно бути між 0 та 1");
                return alpha;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Помилка: " + e.getMessage() + "\nСпробуйте ще раз.",
                        "Помилка вводу", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Відображення результатів

    private void showResults(String text, int[] observed, double[] uniform, double[] normal) {
        JFrame frame = new JFrame("Перевірка χ² розподілів");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 600);

        JButton repeatButton = new JButton("Повторити");
        repeatButton.addActionListener(e -> {
            frame.dispose();
            runProgram();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(repeatButton);


        JTextArea area = new JTextArea(text);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(420, 550));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel chartPanel = new HistogramPanel(observed, uniform, normal);

        frame.setLayout(new BorderLayout());
        frame.add(scroll, BorderLayout.WEST);
        frame.add(chartPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // χ² для рівномірного розподілу

    private double chiSquareUniform(List<Integer> data) {
        int n = data.size();
        double expected = (double) n / 21;
        int[] freq = new int[21];
        for (int x : data) freq[x]++;
        double chi2 = 0;
        for (int i = 0; i < 21; i++) {
            chi2 += Math.pow(freq[i] - expected, 2) / expected;
        }
        return chi2;
    }

    // χ² для нормального розподілу
    private double chiSquareNormal(List<Integer> data) {
        int n = data.size();
        double mean = data.stream().mapToDouble(x -> x).average().orElse(0);
        double variance = data.stream().mapToDouble(x -> Math.pow(x, 2)).sum() / n - Math.pow(mean, 2);
        double std = Math.sqrt(variance);

        int[] freq = new int[21];
        for (int x : data) freq[x]++;

        double chi2 = 0;
        for (int i = 0; i < 21; i++) {
            double p = normalProb(i, mean, std);
            double expected = n * p;
            if (expected > 0)
                chi2 += Math.pow(freq[i] - expected, 2) / expected;
        }
        return chi2;
    }

    // Ймовірність для нормального розподілу у дискретній точці

    private double normalProb(int x, double mean, double std) {
        double left = (x - 0.5 - mean) / std;
        double right = (x + 0.5 - mean) / std;
        return 0.5 * (erf(right / Math.sqrt(2)) - erf(left / Math.sqrt(2)));
    }

    private double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));
        double ans = 1 - t * Math.exp(-z * z - 1.26551223 +
                t * (1.00002368 +
                        t * (0.37409196 +
                                t * (0.09678418 +
                                        t * (-0.18628806 +
                                                t * (0.27886807 +
                                                        t * (-1.13520398 +
                                                                t * (1.48851587 +
                                                                        t * (-0.82215223 +
                                                                                t * 0.17087277)))))))));
        return z >= 0 ? ans : -ans;
    }

    // Критичне χ² за апроксимацією Голдштейна
    private double chiSquareCriticalGoldstein(int n, double alpha) {
        alpha = 1 - alpha;
        double[][] coeffs = {
                {1.0000886, -0.2237368, -0.01513904},
                {0.4713941, 0.02607083, -0.008986007},
                {0.0001348028, 0.01128186, 0.02277679},
                {-0.008553069, -0.01153761, -0.01323293},
                {0.00312558, 0.005169654, -0.006950356},
                {-0.0008426812, 0.00253001, 0.001060438},
                {0.00009780499, -0.001450117, 0.001565326}
        };

        // Обчислення d
        double d;
        if (alpha >= 0.5 && alpha <= 0.999) {
            d = 2.0637 * Math.pow(Math.log(1 / (1 - alpha)) - 0.16, 0.4274) - 1.5774;
        } else if (alpha >= 0.001 && alpha < 0.5) {
            d = -2.0637 * Math.pow(Math.log(1 / alpha) - 0.16, 0.4274) + 1.5774;
        } else {
            throw new IllegalArgumentException("α має бути в межах [0.001, 0.999]");
        }

        // Обчислення суми Σ
        double sum = 0.0;
        for (int i = 0; i <= 6; i++) {
            double a = coeffs[i][0];
            double b = coeffs[i][1];
            double c = coeffs[i][2];
            double term = Math.pow(n, -i / 2.0) * Math.pow(d, i) * (a + b / n + c / (n * n));
            sum += term;
        }

        return n * Math.pow(sum, 3);
    }


    // Панель гістограми

    static class HistogramPanel extends JPanel {
        final int[] observed;
        final double[] expectedUniform;
        final double[] expectedNormal;

        public HistogramPanel(int[] observed, double[] expectedUniform, double[] expectedNormal) {
            this.observed = observed;
            this.expectedUniform = expectedUniform;
            this.expectedNormal = expectedNormal;
            setPreferredSize(new Dimension(500, 550));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth() - 80;
            int height = getHeight() - 80;
            int nBins = observed.length;
            int x0 = 50, y0 = height + 30;

            int maxVal = Arrays.stream(observed).max().orElse(1);
            maxVal = (int) Math.max(maxVal, Arrays.stream(expectedNormal).max().orElse(1));

            // Осі
            g.drawLine(x0, y0, x0 + width, y0);
            g.drawLine(x0, y0, x0, 30);

            int barWidth = width / nBins;

            // Спостереження (блакитний)
            g.setColor(new Color(100, 150, 255));
            for (int i = 0; i < nBins; i++) {
                int barHeight = (int) ((double) observed[i] / maxVal * (height - 20));
                g.fillRect(x0 + i * barWidth, y0 - barHeight, barWidth - 2, barHeight);
            }

            // Очікуваний рівномірний (помаранчевий)
            g.setColor(new Color(255, 150, 100, 180));
            for (int i = 0; i < nBins; i++) {
                int barHeight = (int) (expectedUniform[i] / maxVal * (height - 20));
                g.fillRect(x0 + i * barWidth + 2, y0 - barHeight, barWidth - 4, 3);
            }

            // Очікуваний нормальний (зелений)
            g.setColor(new Color(100, 200, 100, 180));
            for (int i = 0; i < nBins; i++) {
                int barHeight = (int) (expectedNormal[i] / maxVal * (height - 20));
                g.fillRect(x0 + i * barWidth + 2, y0 - barHeight, barWidth - 4, 3);
            }

            // Підписи
            g.setColor(Color.BLACK);
            for (int i = 0; i < nBins; i++) {
                g.drawString(String.valueOf(i), x0 + i * barWidth + barWidth / 3, y0 + 15);
            }

            g.drawString("Набір (блакитний) | Рівномірний (помаранчевий) | Нормальний (зелений)", 60, 20);
        }
    }
}
