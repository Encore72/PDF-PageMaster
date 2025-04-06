package org.imf.pdfpagemaster;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // SwingUtilities.invokeLater ensures the graphical user interface (GUI) is created safely (safely = EDT = Event Dispatch Thread which is a special thread in Java for handling GUI events).
        SwingUtilities.invokeLater(() -> {
            // Create a new window using the PDFProcessorUI class, which handles the UI.
            PDFProcessorUI frame = new PDFProcessorUI();
            frame.setVisible(true);
        });
    }
}