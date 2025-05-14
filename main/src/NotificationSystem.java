import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

// Observer Interface
interface Observer {
    void update(String message);
}

// Concrete Observer (Admin)
class Admin implements Observer {
    private String name;
    private boolean isOnline;
    private AdminPanel panel;
    private List<String> messageQueue = new ArrayList<>();

    public Admin(String name, boolean isOnline) {
        this.name = name;
        this.isOnline = isOnline;
        this.panel = new AdminPanel(this);
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean status) {
        this.isOnline = status;
        panel.updateStatusLabel();
        if (status) {
            flushStoredMessages();
        }
    }

    public String getName() {
        return name;
    }

    public AdminPanel getPanel() {
        return panel;
    }

    @Override
    public void update(String message) {
        if (isOnline) {
            panel.showNotification("Real-time: " + message);
        } else {
            messageQueue.add(message);
            // Optional: log or notify that it's stored
            System.out.println("Notification stored for offline admin: " + name);
        }
    }

    private void flushStoredMessages() {
        for (String msg : messageQueue) {
            panel.showNotification("Delivered from storage: " + msg);
        }
        messageQueue.clear();
    }
}


// Subject Interface
interface Subject {
    void addObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers(String message);
}

// Concrete Subject (NotificationManager)
class NotificationManager implements Subject {
    private List<Observer> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer o : observers) {
            try {
                o.update(message);
            } catch (Exception e) {
                System.out.println("Notification failed. Error logged.");
            }
        }
    }

    public void newEventDetected(String eventDescription) {
        String message = "New event: " + eventDescription;
        notifyObservers(message);
    }
}

// GUI for Admin
class AdminPanel extends JFrame {
    private Admin admin;
    private JTextArea textArea;
    private JLabel statusLabel;

    public AdminPanel(Admin admin) {
        this.admin = admin;
        setTitle("Admin: " + admin.getName());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel = new JLabel();
        updateStatusLabel();

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton toggleButton = new JButton("Toggle Online/Offline");
        toggleButton.addActionListener(e -> {
            admin.setOnline(!admin.isOnline());
        });

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(toggleButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    public void updateStatusLabel() {
        statusLabel.setText("Status: " + (admin.isOnline() ? "Online" : "Offline"));
    }

    public void showNotification(String message) {
        textArea.append(message + "\n");
    }
}

// Main Application with GUI Controls
public class NotificationSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NotificationManager manager = new NotificationManager();

            Admin admin1 = new Admin("Hamid", true);
            Admin admin2 = new Admin("Mudassir", false);

            manager.addObserver(admin1);
            manager.addObserver(admin2);

            // Control panel to simulate events
            JFrame controlFrame = new JFrame("Control Panel");
            controlFrame.setSize(300, 200);
            controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            controlFrame.setLayout(new GridLayout(3, 1));

            JButton feedbackBtn = new JButton("Simulate Feedback Submitted");
            JButton reportBtn = new JButton("Simulate Report Ready");

            feedbackBtn.addActionListener(e -> manager.newEventDetected("Feedback submitted by User"));
            reportBtn.addActionListener(e -> manager.newEventDetected("Monthly report is ready"));

            controlFrame.add(new JLabel("Trigger Events Below:", SwingConstants.CENTER));
            controlFrame.add(feedbackBtn);
            controlFrame.add(reportBtn);

            controlFrame.setVisible(true);
        });
    }
}
