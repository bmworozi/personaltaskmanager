import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TaskManagerApp extends JFrame {
    private List<Task> tasks = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable taskTable;
    private JTextField nameTextField = new JTextField(20);
    private JTextArea descriptionTextArea = new JTextArea(5, 20);
    private JTextField dueDateTextField = new JTextField(10);
    private JPanel inputPanel = new JPanel(new GridBagLayout());

    public TaskManagerApp() {
        setTitle("Task Manager");
        setSize(800, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Task Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(nameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(new JScrollPane(descriptionTextArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(dueDateTextField, gbc);

        JButton createButton = new JButton("Create Task");
        createButton.addActionListener(e -> createTask());
        gbc.gridx = 1;
        gbc.gridy = 3;
        inputPanel.add(createButton, gbc);

        JButton editButton = new JButton("Edit Task");
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editTask());
        gbc.gridx = 2;
        inputPanel.add(editButton, gbc);

        JButton deleteButton = new JButton("Delete Task");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteTask());
        gbc.gridx = 3;
        inputPanel.add(deleteButton, gbc);

        tableModel = new DefaultTableModel(new Object[]{"Name", "Description", "Due Date", "Status"}, 0);
        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.getSelectionModel().addListSelectionListener(e -> {
            boolean isSelected = taskTable.getSelectedRow() >= 0;
            editButton.setEnabled(isSelected);
            deleteButton.setEnabled(isSelected);
        });

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                TaskStatus status = (TaskStatus) value;
                if (status == TaskStatus.COMPLETED) {
                    component.setBackground(Color.GREEN);
                } else if (status == TaskStatus.PENDING) {
                    component.setBackground(Color.YELLOW);
                } else if (status == TaskStatus.RUNNING) {
                    component.setBackground(Color.GRAY);
                }
                return component;
            }
        };

        taskTable.getColumnModel().getColumn(3).setCellRenderer(statusRenderer);

        mainPanel.add(new JScrollPane(taskTable), BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        loadTasks();

        getContentPane().add(mainPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createTask() {
        String name = nameTextField.getText();
        String description = descriptionTextArea.getText();
        String dueDate = dueDateTextField.getText();

        if (name.isEmpty() || description.isEmpty() || dueDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all task details.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = new Task(name, description, dueDate);
        tasks.add(task);

        Object[] rowData = {task.getName(), task.getDescription(), task.getDueDate(), task.getStatus()};
        tableModel.addRow(rowData);

        nameTextField.setText("");
        descriptionTextArea.setText("");
        dueDateTextField.setText("");

        saveTasks();
    }

    private void editTask() {
        int selectedIndex = taskTable.getSelectedRow();
        if (selectedIndex >= 0) {
            Task selectedTask = tasks.get(selectedIndex);

            nameTextField.setText(selectedTask.getName());
            descriptionTextArea.setText(selectedTask.getDescription());
            dueDateTextField.setText(selectedTask.getDueDate());

            JComboBox<TaskStatus> statusComboBox = new JComboBox<>(TaskStatus.values());
            statusComboBox.setSelectedItem(selectedTask.getStatus());

            JPanel editPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 10, 5, 10);

            gbc.gridx = 0;
            gbc.gridy = 0;
            editPanel.add(new JLabel("Task Name:"), gbc);
            gbc.gridx = 1;
            editPanel.add(nameTextField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            editPanel.add(new JLabel("Description:"), gbc);
            gbc.gridx = 1;
            editPanel.add(new JScrollPane(descriptionTextArea), gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            editPanel.add(new JLabel("Due Date:"), gbc);
            gbc.gridx = 1;
            editPanel.add(dueDateTextField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            editPanel.add(new JLabel("Status:"), gbc);
            gbc.gridx = 1;
            editPanel.add(statusComboBox, gbc);

            int result = JOptionPane.showConfirmDialog(this, editPanel,
                    "Edit Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                selectedTask.setName(nameTextField.getText());
                selectedTask.setDescription(descriptionTextArea.getText());
                selectedTask.setDueDate(dueDateTextField.getText());
                selectedTask.setStatus((TaskStatus) statusComboBox.getSelectedItem());

                tableModel.setValueAt(selectedTask.getName(), selectedIndex, 0);
                tableModel.setValueAt(selectedTask.getDescription(), selectedIndex, 1);
                tableModel.setValueAt(selectedTask.getDueDate(), selectedIndex, 2);
                tableModel.setValueAt(selectedTask.getStatus(), selectedIndex, 3);

                saveTasks();
            }

            nameTextField.setText("");
            descriptionTextArea.setText("");
            dueDateTextField.setText("");
        }
    }

    private void deleteTask() {
        int selectedIndex = taskTable.getSelectedRow();
        if (selectedIndex >= 0) {
            int confirmDelete = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this task?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirmDelete == JOptionPane.YES_OPTION) {
                tasks.remove(selectedIndex);
                tableModel.removeRow(selectedIndex);
                saveTasks();
            }
        }
    }

    private void loadTasks() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("tasks.dat"))) {
            tasks = (List<Task>) inputStream.readObject();
            for (Task task : tasks) {
                Object[] rowData = {task.getName(), task.getDescription(), task.getDueDate(), task.getStatus()};
                tableModel.addRow(rowData);
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void saveTasks() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("tasks.dat"))) {
            outputStream.writeObject(tasks);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TaskManagerApp::new);
    }
}
