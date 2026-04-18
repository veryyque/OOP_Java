package ru.nsu.ccfit.vmoskalyuk.Factory.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.function.Consumer;

public class FactoryView extends JFrame {

    // ==================== Цвета ====================
    private static final Color BURGUNDY_DARK_BG = new Color(74, 18, 32);   // фон окна
    private static final Color BURGUNDY_MAIN = new Color(128, 32, 54);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color TEXT_DARK = new Color(40, 28, 32);
    private static final Color TEXT_LIGHT = new Color(110, 85, 90);

    // Метки
    private final JLabel bodyCountLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel motorCountLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel accessoryCountLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel carCountLabel = new JLabel("0", SwingConstants.CENTER);

    private final JLabel totalBodiesLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel totalMotorsLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel totalAccessoriesLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel totalCarsLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel pendingTasksLabel = new JLabel("0", SwingConstants.CENTER);

    private JSlider bodySlider, motorSlider, accessorySlider, dealerSlider;

    private Runnable closeListener;

    public FactoryView() {
        setTitle("Фабрика по производству машин");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        initComponents();
        pack();
        setMinimumSize(new Dimension(1260, 700));
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        getContentPane().setBackground(BURGUNDY_DARK_BG);

        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 24, 20));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        mainPanel.add(createCardPanel("Состояние складов", createStoragePanel()));
        mainPanel.add(createCardPanel("Произведено всего", createProductionPanel()));
        mainPanel.add(createCardPanel("Управление скоростью (мс)", createControlPanel()));

        add(mainPanel, BorderLayout.CENTER);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (closeListener != null) closeListener.run();
            }
        });
    }

    /** Карточка с большим круглым радиусом */
    private JPanel createCardPanel(String title, JPanel content) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);   // большой радиус
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 22, 22));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(BURGUNDY_MAIN);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 14, 0));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    // ==================== Панель складов (без цветных рамок) ====================
    private JPanel createStoragePanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 16, 18));
        panel.setOpaque(false);

        addStorageRow(panel, "Кузова:", bodyCountLabel);
        addStorageRow(panel, "Двигатели:", motorCountLabel);
        addStorageRow(panel, "Аксессуары:", accessoryCountLabel);
        addStorageRow(panel, "Готовые машины:", carCountLabel);

        return panel;
    }

    private void addStorageRow(JPanel panel, String labelText, JLabel valueLabel) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(TEXT_DARK);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));   // одинаковый большой размер
        valueLabel.setForeground(TEXT_DARK);

        panel.add(label);
        panel.add(valueLabel);
    }

    // ==================== Панель "Произведено всего" ====================
    private JPanel createProductionPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 16, 14));
        panel.setOpaque(false);

        addProductionRow(panel, "Кузовов всего:", totalBodiesLabel);
        addProductionRow(panel, "Двигателей всего:", totalMotorsLabel);
        addProductionRow(panel, "Аксессуаров всего:", totalAccessoriesLabel);
        addProductionRow(panel, "Машин всего:", totalCarsLabel);
        addProductionRow(panel, "Задач в очереди:", pendingTasksLabel);

        return panel;
    }

    private void addProductionRow(JPanel panel, String labelText, JLabel valueLabel) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(TEXT_LIGHT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));   // чуть меньше, чем на складе
        valueLabel.setForeground(TEXT_DARK);

        panel.add(label);
        panel.add(valueLabel);
    }

    // ==================== Панель управления ====================
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 16, 22));
        panel.setOpaque(false);

        bodySlider = createStyledSlider(1200);
        motorSlider = createStyledSlider(1200);
        accessorySlider = createStyledSlider(800);
        dealerSlider = createStyledSlider(600);

        panel.add(new JLabel("Поставка кузовов:"));
        panel.add(bodySlider);
        panel.add(new JLabel("Поставка двигателей:"));
        panel.add(motorSlider);
        panel.add(new JLabel("Поставка аксессуаров:"));
        panel.add(accessorySlider);
        panel.add(new JLabel("Скорость дилеров:"));
        panel.add(dealerSlider);

        return panel;
    }

    private JSlider createStyledSlider(int initialValue) {
        JSlider slider = new JSlider(100, 4000, initialValue);
        slider.setBackground(CARD_BG);
        slider.setMajorTickSpacing(500);
        slider.setMinorTickSpacing(0);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setPreferredSize(new Dimension(500, 70));

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(100, new JLabel("100"));
        labelTable.put(500, new JLabel("500"));
        labelTable.put(1000, new JLabel("1k"));
        labelTable.put(2000, new JLabel("2k"));
        labelTable.put(3000, new JLabel("3k"));
        labelTable.put(4000, new JLabel("4k"));

        labelTable.values().forEach(label -> {
            label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            label.setForeground(new Color(130, 130, 130));
        });

        slider.setLabelTable(labelTable);

        slider.setUI(new javax.swing.plaf.basic.BasicSliderUI(slider) {
            @Override
            protected Dimension getThumbSize() {
                return new Dimension(28, 28);
            }

            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BURGUNDY_MAIN);
                g2.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.8f));
                g2.drawOval(thumbRect.x + 2, thumbRect.y + 2, thumbRect.width - 4, thumbRect.height - 4);
            }
        });

        return slider;
    }

    // ====================== Обновление статистики ======================
    public void updateStats(int bodyCount, int motorCount, int accessoryCount, int carCount,
                            int totalBodies, int totalMotors, int totalAccessories,
                            int totalCars, int pendingTasks) {

        SwingUtilities.invokeLater(() -> {
            bodyCountLabel.setText(String.valueOf(bodyCount));
            motorCountLabel.setText(String.valueOf(motorCount));
            accessoryCountLabel.setText(String.valueOf(accessoryCount));
            carCountLabel.setText(String.valueOf(carCount));

            totalBodiesLabel.setText(String.valueOf(totalBodies));
            totalMotorsLabel.setText(String.valueOf(totalMotors));
            totalAccessoriesLabel.setText(String.valueOf(totalAccessories));
            totalCarsLabel.setText(String.valueOf(totalCars));
            pendingTasksLabel.setText(String.valueOf(pendingTasks));
        });
    }

    public void setBodySpeedListener(Consumer<Integer> listener) {
        bodySlider.addChangeListener(e -> listener.accept(bodySlider.getValue()));
    }

    public void setMotorSpeedListener(Consumer<Integer> listener) {
        motorSlider.addChangeListener(e -> listener.accept(motorSlider.getValue()));
    }

    public void setAccessorySpeedListener(Consumer<Integer> listener) {
        accessorySlider.addChangeListener(e -> listener.accept(accessorySlider.getValue()));
    }

    public void setDealerSpeedListener(Consumer<Integer> listener) {
        dealerSlider.addChangeListener(e -> listener.accept(dealerSlider.getValue()));
    }

    public void setInitialDelays(int bodyDelay, int motorDelay, int accessoryDelay, int dealerDelay) {
        bodySlider.setValue(clampToSliderRange(bodySlider, bodyDelay));
        motorSlider.setValue(clampToSliderRange(motorSlider, motorDelay));
        accessorySlider.setValue(clampToSliderRange(accessorySlider, accessoryDelay));
        dealerSlider.setValue(clampToSliderRange(dealerSlider, dealerDelay));
    }

    private int clampToSliderRange(JSlider slider, int value) {
        return Math.max(slider.getMinimum(), Math.min(slider.getMaximum(), value));
    }

    public void setCloseListener(Runnable listener) {
        this.closeListener = listener;
    }
}