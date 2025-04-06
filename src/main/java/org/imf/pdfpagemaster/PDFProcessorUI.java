package org.imf.pdfpagemaster;

import javax.swing.*; // Provides GUI components like windows and buttons.
import java.awt.*; // Provides layout and design tools for the GUI.
import java.awt.datatransfer.DataFlavor; // Helps with drag-and-drop file handling.
import java.awt.dnd.*; // Enables drag-and-drop functionality.
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// This class creates the graphical user interface (GUI) for the app.
public class PDFProcessorUI extends JFrame {
    // Fields for GUI components and the PDF processor instance to handle the logic.
    private JTextField textPrefixField; // Text box for the user to enter a prefix.
    private JComboBox<String> textPositionCombo; // Dropdown for user to select text position.
    private JComboBox<String> pageNumberPositionCombo; // Dropdown for user to select page number position.
    private JCheckBox backgroundCheckBox; // Checkbox to toggle background.
    private DefaultListModel<String> fileListModel; // Model to store the list of PDF file paths.
    private PDFProcessor pdfProcessor; // Instance of the PDFProcessor class to handle PDF logic.

    // Constructor to set up the window when the app starts.
    public PDFProcessorUI() {
        // Set the window title.
        setTitle("PDF PageMaster");
        // Set the window size to 600x400 pixels.
        setSize(600, 400);
        // Close the app when the window is closed.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Center the window on the screen.
        setLocationRelativeTo(null);

        // Create a new PDFProcessor instance to handle PDF operations.
        pdfProcessor = new PDFProcessor();
        // Set up all the GUI components.
        initComponents();
    }

    // This method initializes and arranges all the GUI components.
    private void initComponents() {
        // Create the main panel with a border layout and some padding (10 pixels).
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a configuration panel with a 4x2 grid layout for settings.
        JPanel configPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        // Add a label and text field for the text prefix.
        configPanel.add(new JLabel("Document Number Text Prefix:"));
        textPrefixField = new JTextField("DOCUMENTO"); // Default value is "DOCUMENTO".
        configPanel.add(textPrefixField);

        // Add a label and dropdown for text position.
        configPanel.add(new JLabel("Document Number Text Position:"));
        textPositionCombo = new JComboBox<>(new String[]{"Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right"});
        configPanel.add(textPositionCombo);

        // Add a label and dropdown for page number position.
        configPanel.add(new JLabel("Page Number Position:"));
        pageNumberPositionCombo = new JComboBox<>(new String[]{"Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right"});
        configPanel.add(pageNumberPositionCombo);

        // Add a label and checkbox for the background option.
        configPanel.add(new JLabel("Add Background:"));
        backgroundCheckBox = new JCheckBox(); // Unchecked by default.
        configPanel.add(backgroundCheckBox);

        // Create a list model and list to display dropped (user input) PDF files.
        fileListModel = new DefaultListModel<>();
        JList<String> fileList = new JList<>(fileListModel);

        // Add a scroll pane so the list can scroll if there are many files.
        JScrollPane scrollPane = new JScrollPane(fileList);

        // Set up drag-and-drop functionality for the file list.
        new DropTarget(fileList, new DropTargetListener() {
            // This method runs when files are dropped onto the list.
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    // Accept the drop action as a copy operation.
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    // Get the dropped files as a list.
                    List<?> droppedFiles = (List<?>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    // Loop through each dropped file.
                    for (Object file : droppedFiles) {
                        String path = file.toString();
                        // Only add files that end with ".pdf" (case-insensitive).
                        if (path.toLowerCase().endsWith(".pdf")) {
                            fileListModel.addElement(path);
                        }
                    }
                } catch (Exception ex) {
                    // Print any errors to the console (for debugging).
                    ex.printStackTrace();
                }
            }
            // These empty methods are required by the DropTargetListener interface but not used here.
            @Override public void dragEnter(DropTargetDragEvent dtde) {}
            @Override public void dragOver(DropTargetDragEvent dtde) {}
            @Override public void dropActionChanged(DropTargetDragEvent dtde) {}
            @Override public void dragExit(DropTargetEvent dte) {}
        });

        // Create a button to start processing the PDFs.
        JButton processButton = new JButton("Process PDFs");
        // Add an action listener to call the processPDFs method when clicked.
        processButton.addActionListener(e -> processPDFs());

        // Add all components to the main panel in their respective positions.
        mainPanel.add(configPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(processButton, BorderLayout.SOUTH);

        // Add the main panel to the window.
        add(mainPanel);
    }

    // This method is called when the "Process PDFs" button is clicked.
    private void processPDFs() {
        // Create a list to store the paths of all PDFs in the file list.
        List<String> filePaths = new ArrayList<>();
        for (int i = 0; i < fileListModel.size(); i++) {
            filePaths.add(fileListModel.get(i));
        }

        try {
            // Call the PDFProcessor’s processPDFs method with the user’s settings.
            pdfProcessor.processPDFs(
                    filePaths, // List of PDF file paths.
                    textPrefixField.getText(), // Text from the prefix field.
                    textPositionCombo.getSelectedIndex(), // Index of selected text position (0-3).
                    pageNumberPositionCombo.getSelectedIndex(), // Index of selected page number position (0-3).
                    backgroundCheckBox.isSelected(), // Whether the background checkbox is checked.
                    new File("processed_documents.pdf") // Save the result as "processed_documents.pdf" in the current directory.
            );
            // Show a success message to the user.
            JOptionPane.showMessageDialog(this, "PDFs processed successfully!");
        } catch (Exception ex) {
            // If an error occurs, show an error message with details.
            JOptionPane.showMessageDialog(this, "Error processing PDFs: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}