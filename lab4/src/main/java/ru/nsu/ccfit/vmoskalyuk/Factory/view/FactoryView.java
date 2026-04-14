// ru/nsu/ccfit/vmoskalyuk/Factory/view/FactoryView.java
package ru.nsu.ccfit.vmoskalyuk.Factory.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.function.Consumer;

public class FactoryView extends JFrame {

    private final JLabel bodyCountLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel motorCountLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel accessoryCountLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel carCountLabel = new JLabel("0", SwingConstants.CENTER);

    // Метки для общего количества произведённых деталей и машин
    private final JLabel totalBodiesLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel totalMotorsLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel totalAccessoriesLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel totalCarsLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel pendingTasksLabel = new JLabel("0", SwingConstants.CENTER);

    // Ползунки
    private JSlider bodySlider;
    private JSlider motorSlider;
    private JSlider accessorySlider;
    private JSlider dealerSlider;

    // Слушатель закрытия окна
    private Runnable closeListener;

    public FactoryView() {
        setTitle("Эмулятор работы фабрики");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 15, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createStoragePanel());
        mainPanel.add(createProductionPanel());
        mainPanel.add(createControlPanel());

        add(mainPanel, BorderLayout.CENTER);

        // Обработка закрытия окна
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (closeListener != null) {
                    closeListener.run();
                }
            }
        });
    }

    private JPanel createStoragePanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(new TitledBorder("Состояние складов"));

        panel.add(new JLabel("Кузова:"));
        panel.add(bodyCountLabel);
        panel.add(new JLabel("Двигатели:"));
        panel.add(motorCountLabel);
        panel.add(new JLabel("Аксессуары:"));
        panel.add(accessoryCountLabel);
        panel.add(new JLabel("Готовые машины:"));
        panel.add(carCountLabel);

        return panel;
    }

    private JPanel createProductionPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.setBorder(new TitledBorder("Произведено всего"));

        panel.add(new JLabel("Кузовов всего:"));
        panel.add(totalBodiesLabel);
        panel.add(new JLabel("Двигателей всего:"));
        panel.add(totalMotorsLabel);
        panel.add(new JLabel("Аксессуаров всего:"));
        panel.add(totalAccessoriesLabel);
        panel.add(new JLabel("Машин всего:"));
        panel.add(totalCarsLabel);
        panel.add(new JLabel("Задач в очереди:"));
        panel.add(pendingTasksLabel);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 12));
        panel.setBorder(new TitledBorder("Скорость (мс)"));

        bodySlider = createSlider(100, 3000, 1000);
        motorSlider = createSlider(100, 3000, 1000);
        accessorySlider = createSlider(100, 3000, 1000);
        dealerSlider = createSlider(100, 3000, 1000);

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

    private JSlider createSlider(int min, int max, int initial) {
        JSlider slider = new JSlider(min, max, initial);
        slider.setMajorTickSpacing(500);
        slider.setMinorTickSpacing(100);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        return slider;
    }

    /**
     * Обновление статистики из контроллера
     * Теперь передаём также общее количество произведённых деталей
     */
    public void updateStats(int bodyCount, int motorCount, int accessoryCount,
                            int carCount,
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

    // ====================== Слушатели ползунков ======================

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

    public void setCloseListener(Runnable listener) {
        this.closeListener = listener;
    }
}