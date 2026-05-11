package ru.nsu.ccfit.vmoskalyuk.Serialization.client;

import ru.nsu.ccfit.vmoskalyuk.Serialization.message.*;

import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SwingChatClient extends JFrame {
    private static final Color BARBIE_HOT = new Color(0xFF0099);
    private static final Color BARBIE_LIGHT = new Color(0xFF69B4);
    private static final Color BARBIE_PALE = new Color(0xFFB6C1);
    private static final Color BARBIE_BLUSH = new Color(0xFFF0F5);
    private static final Color BARBIE_WHITE = new Color(0xFFFAFB);
    private static final Color BARBIE_TEXT = new Color(0x8B0057);
    private static final Color BARBIE_GOLD = new Color(0xFFD700);
    private static final Color BARBIE_PURPLE = new Color(0xCC44AA);

    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_BODY = new Font("Arial", Font.PLAIN, 13);
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 12);

    private final JTextField hostField = styledTextField("localhost", 12);
    private final JTextField portField = styledTextField("5555", 5);
    private final JTextField nameField = styledTextField("Barbie", 10);
    private final JButton connectButton = barbieButton("✨ Connect", true);
    private final JButton disconnectButton = barbieButton("💔 Disconnect", false);
    private final JTextArea messagesArea  = new JTextArea();
    private final DefaultListModel<String> usersModel = new DefaultListModel<>();
    private final JList<String> usersList = new JList<>(usersModel);
    private final JTextField messageField = styledTextField("", 0);
    private final JButton sendButton = barbieButton("Send 💌", true);
    private final JButton emojiButton = barbieButton("Emoji", false);

    private ChatClientConnection connection;

    public SwingChatClient() {
        super("💖 Barbie Chat - Serialization 💖");
        applyGlobalLookAndFeel();
        createInterface();
        bindActions();
        setConnected(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingChatClient().setVisible(true));
    }

    private void applyGlobalLookAndFeel() {
        UIManager.put("OptionPane.background", BARBIE_BLUSH);
        UIManager.put("Panel.background", BARBIE_BLUSH);
        UIManager.put("OptionPane.messageForeground", BARBIE_TEXT);
        UIManager.put("Button.background", BARBIE_HOT);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", FONT_LABEL);
        UIManager.put("ScrollBar.thumb", new ColorUIResource(BARBIE_LIGHT));
        UIManager.put("ScrollBar.track", new ColorUIResource(BARBIE_BLUSH));
    }

    private void createInterface() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(820, 560));
        setLocationByPlatform(true);
        getContentPane().setBackground(BARBIE_BLUSH);

        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        connectionPanel.setBackground(BARBIE_HOT);
        connectionPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, BARBIE_GOLD));

        connectionPanel.add(barbieLabel("🏠 Host"));
        connectionPanel.add(hostField);
        connectionPanel.add(barbieLabel("🔌 Port"));
        connectionPanel.add(portField);
        connectionPanel.add(barbieLabel("🎀 Nick"));
        connectionPanel.add(nameField);
        connectionPanel.add(connectButton);
        connectionPanel.add(disconnectButton);

        messagesArea.setEditable(false);
        messagesArea.setLineWrap(true);
        messagesArea.setWrapStyleWord(true);
        messagesArea.setBackground(BARBIE_WHITE);
        messagesArea.setForeground(BARBIE_TEXT);
        messagesArea.setFont(FONT_BODY);
        messagesArea.setCaretColor(BARBIE_HOT);
        messagesArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane messagesScroll = pinkScrollPane(messagesArea);
        messagesScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BARBIE_LIGHT, 2, true),
                " 💬 Messages ", 0, 0, FONT_LABEL, BARBIE_HOT));

        usersList.setBackground(BARBIE_WHITE);
        usersList.setForeground(BARBIE_TEXT);
        usersList.setFont(FONT_BODY);
        usersList.setSelectionBackground(BARBIE_PALE);
        usersList.setSelectionForeground(BARBIE_TEXT);
        usersList.setFixedCellHeight(26);

        JScrollPane usersScroll = pinkScrollPane(usersList);
        usersScroll.setPreferredSize(new Dimension(190, 100));
        usersScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BARBIE_LIGHT, 2, true),
                " 👗 Online ", 0, 0, FONT_LABEL, BARBIE_HOT));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, messagesScroll, usersScroll);
        splitPane.setResizeWeight(1);
        splitPane.setBackground(BARBIE_BLUSH);
        splitPane.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 8));
        splitPane.setDividerSize(6);

        messageField.setFont(FONT_BODY);
        messageField.setBackground(BARBIE_WHITE);
        messageField.setForeground(BARBIE_TEXT);
        messageField.setCaretColor(BARBIE_HOT);
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BARBIE_LIGHT, 2, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 0));
        bottomPanel.setBackground(BARBIE_BLUSH);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, BARBIE_GOLD),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        bottomPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttonsPanel.setBackground(BARBIE_BLUSH);
        buttonsPanel.add(emojiButton);
        buttonsPanel.add(sendButton);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        add(connectionPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        pack();
    }

    private void bindActions() {
        connectButton.addActionListener(e -> connect());
        disconnectButton.addActionListener(e -> disconnect());
        emojiButton.addActionListener(e -> showEmojiPicker(emojiButton));
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
                dispose();
            }
        });
    }

    private void connect() {
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            String name = nameField.getText().trim();
            connection = new ChatClientConnection(host, port, this::processMessage);
            connection.login(name);
            appendSystem("Connected as " + name);
            setConnected(true);
            requestUsers();
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void disconnect() {
        if (connection == null) return;
        try {
            connection.logout();
        } catch (Exception ignored) {}
        connection = null;
        appendSystem("Disconnected");
        setConnected(false);
        usersModel.clear();
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || connection == null) return;
        try {
            connection.sendMessage(text);
            messageField.setText("");
        } catch (IOException exception) {
            showError(exception.getMessage());
        }
    }

    private void requestUsers() {
        if (connection != null) {
            try {
                connection.requestUsers();
            } catch (Exception ignored) {}
        }
    }

    private void processMessage(Object obj) {
        SwingUtilities.invokeLater(() -> {
            if (obj instanceof ErrorResponse error) {
                appendSystem("Error: " + error.getMessage());
                setConnected(false);
                return;
            }

            if (obj instanceof ChatEvent event) {
                switch (event.getEventType()) {
                    case "message" -> appendMessage(
                            event.getUser(),
                            event.getText(),
                            event.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    case "userlogin" -> {
                        appendSystem(event.getUser() + " joined chat");
                        requestUsers();
                    }
                    case "userlogout" -> {
                        appendSystem(event.getUser() + " left chat");
                        requestUsers();
                    }
                }
            } else if (obj instanceof UserListResponse response) {
                showUsers(response.getUsers());
            }
        });
    }

    private void showUsers(List<UserInfo> users) {
        usersModel.clear();
        for (UserInfo user : users) {
            String type = user.getType();
            if (type != null && !type.isBlank()) {
                usersModel.addElement("🌸 " + user.getName() + " [" + type + "]");
            } else {
                usersModel.addElement("🌸 " + user.getName());
            }
        }
    }

    private void appendMessage(String name, String message, String time) {
        String prefix = time.isBlank() ? "" : "[" + time + "] ";
        messagesArea.append("💬 " + prefix + name + ": " + message + System.lineSeparator());
        messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
    }

    private void appendSystem(String message) {
        messagesArea.append("✨ [system] " + message + System.lineSeparator());
        messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
    }

    private void setConnected(boolean connected) {
        hostField.setEnabled(!connected);
        portField.setEnabled(!connected);
        nameField.setEnabled(!connected);
        connectButton.setEnabled(!connected);
        disconnectButton.setEnabled(connected);
        sendButton.setEnabled(connected);
        emojiButton.setEnabled(connected);
        messageField.setEnabled(connected);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "💔 Chat Error", JOptionPane.ERROR_MESSAGE);
    }

    private static JButton barbieButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isEnabled()
                        ? (primary ? BARBIE_HOT : BARBIE_PURPLE)
                        : BARBIE_PALE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BARBIE_GOLD);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_LABEL);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(BARBIE_GOLD); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(Color.WHITE); }
        });
        return btn;
    }

    private static JTextField styledTextField(String text, int cols) {
        JTextField f = cols > 0 ? new JTextField(text, cols) : new JTextField(text);
        f.setFont(FONT_BODY);
        f.setBackground(BARBIE_WHITE);
        f.setForeground(BARBIE_TEXT);
        f.setCaretColor(BARBIE_HOT);
        f.setSelectionColor(BARBIE_PALE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BARBIE_LIGHT, 2, true),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        return f;
    }

    private static JLabel barbieLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private static JScrollPane pinkScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BARBIE_BLUSH);
        sp.getViewport().setBackground(BARBIE_WHITE);
        sp.getVerticalScrollBar().setBackground(BARBIE_BLUSH);
        sp.getHorizontalScrollBar().setBackground(BARBIE_BLUSH);
        return sp;
    }
    private void showEmojiPicker(Component parent) {
        JPopupMenu popup = new JPopupMenu("Emoji");
        popup.setBackground(BARBIE_WHITE);
        popup.setBorder(BorderFactory.createLineBorder(BARBIE_LIGHT, 2));

        String[][] categories = {
                {"😀", "😂", "🥰", "😍", "🤩", "😎", "🤗", "😇", "🤔", "😴", "😢", "😡", "🥶", "🤯", "🥳", "😱", "🤤", "😋", "🤐", "😤"},
                {"👍", "👎", "👏", "🙌", "🤝", "💪", "👋", "✌️", "🤞", "🫶"},
                {"❤️", "💕", "💗", "💖", "💘", "💝", "💔", "🩷", "🩵", "🤍"},
                {"🐶", "🐱", "🦊", "🐼", "🐨", "🐸", "🦄", "🐝", "🦋", "🌸"},
                {"☕", "🍵", "🧁", "🍰", "🍩", "🍓", "🍕", "🍔", "🌮", "🍦"},
                {"💎", "👑", "🎀", "💄", "👗", "👜", "💐", "🎁", "🌟", "💌"},
                {"✨", "💫", "🔥", "💯", "✅", "❌", "💬", "📌", "🎵", "💢"},
                {"⏰", "☀️", "🌙", "🌈", "💧", "❄️", "🍀", "🎄", "🎃", "🕯️"}
        };

        String[] categoryNames = {"Смайлы", "Жесты", "Сердечки", "Животные", "Еда", "Предметы", "Символы", "Погода"};

        for (int i = 0; i < categories.length; i++) {
            JMenuItem categoryLabel = new JMenuItem("── " + categoryNames[i] + " ──");
            categoryLabel.setFont(new Font("Arial", Font.BOLD, 11));
            categoryLabel.setForeground(BARBIE_HOT);
            categoryLabel.setBackground(BARBIE_BLUSH);
            categoryLabel.setEnabled(false);
            categoryLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 1, 8));
            popup.add(categoryLabel);

            JPanel emojiPanel = new JPanel(new GridLayout(0, 10, 2, 2));
            emojiPanel.setBackground(BARBIE_WHITE);
            emojiPanel.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));

            for (String emoji : categories[i]) {
                JButton emojiBtn = new JButton(emoji);
                emojiBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                emojiBtn.setBackground(BARBIE_WHITE);
                emojiBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
                emojiBtn.setFocusPainted(false);
                emojiBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                emojiBtn.setContentAreaFilled(false);
                emojiBtn.setOpaque(true);

                emojiBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        emojiBtn.setBackground(BARBIE_PALE);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        emojiBtn.setBackground(BARBIE_WHITE);
                    }
                });

                emojiBtn.addActionListener(ev -> {
                    String currentText = messageField.getText();
                    int caretPos = messageField.getCaretPosition();
                    // Вставка эмодзи на текущую позицию курсора
                    String before = currentText.substring(0, caretPos);
                    String after = currentText.substring(caretPos);
                    messageField.setText(before + emoji + after);
                    messageField.setCaretPosition(caretPos + emoji.length());
                    messageField.requestFocus();
                    popup.setVisible(false);
                });

                emojiPanel.add(emojiBtn);
            }
            popup.add(emojiPanel);
        }

        popup.show(parent, 0, -popup.getPreferredSize().height);
    }
}
