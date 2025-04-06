package org.imf.pdfpagemaster;

// These imports bring in tools from the PDFBox library to work with PDF files.
import org.apache.pdfbox.pdmodel.PDDocument; // Represents a PDF document in memory.
import org.apache.pdfbox.pdmodel.PDPage; // Represents a single page in a PDF.
import org.apache.pdfbox.pdmodel.PDPageContentStream; // Allows drawing text or shapes on a page.
import org.apache.pdfbox.pdmodel.font.PDType1Font; // Defines font styles for text in the PDF.
import org.apache.pdfbox.pdmodel.font.Standard14Fonts; // Provides standard fonts to use.
import org.apache.pdfbox.multipdf.PDFMergerUtility; // Combines multiple PDFs into one.
import org.apache.pdfbox.multipdf.Overlay; // Adds the text overlay (like DOC 1) over an existing PDF.

import java.io.File;
import java.io.IOException;
import java.util.*;

// This class contains the core logic for processing PDFs (merging, adding text, etc.).
public class PDFProcessor {

    // This method processes a list of PDFs by merging them, adding text overlay, and saving the result.
    public void processPDFs(
            List<String> filePaths,   // filePaths: List of file paths to the PDFs the user inputs.
            String textPrefix,        // textPrefix: Text to add to each PDF (e.g., "Document 1").
            int textPosition,         // textPosition: Where to place the prefix text (0-3 for top-left, top-right, etc.).
            int pageNumberPosition,   // pageNumberPosition: Where to place page numbers (same 0-3 options).
            boolean addBackground,    // addBackground: If true, adds a gray box behind the text for readability.
            File outputFile)          // outputFile: Where to save the final processed PDF.

            throws Exception {

        // Step 1: Merge all input PDFs into one temporary file to work with.
        File tempMergedFile = File.createTempFile("merged", ".pdf"); // Creates a temporary file with "merged" in its name.
        PDFMergerUtility mergerUtility = new PDFMergerUtility(); // method to merge PDFs.
        mergerUtility.setDestinationFileName(tempMergedFile.getAbsolutePath()); // Set the destination for the merged PDF.

        // Loop through each PDF file path and add it to the merger.
        for (String filePath : filePaths) {
            mergerUtility.addSource(new File(filePath)); // Add each PDF to the list to be merged.
        }
        mergerUtility.mergeDocuments(null); // Merge all PDFs into the temporary file.

        // Step 2: Open the merged PDF to process its pages.
        try (PDDocument document = org.apache.pdfbox.Loader.loadPDF(tempMergedFile)) {
            // The "try" block ensures the PDF document is closed properly when finished.
            PDDocument overlayDoc = new PDDocument(); // Create a new document for the overlay text that will be added.

            int pageCounter = 1; // Tracks the page number to display (starts at 1).
            int currentFileIndex = 0; // Tracks which original PDF file we're working on.
            int pagesInCurrentFile = 0; // Number of pages in the current original PDF.
            int totalProcessedPages = 0; // Pages processed from the current original PDF.

            // Create a list to store the number of pages in each original PDF (original PDF = user input PDF).
            List<Integer> pagesPerFile = new ArrayList<>();
            for (String filePath : filePaths) {
                try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(new File(filePath))) {
                    pagesPerFile.add(doc.getNumberOfPages()); // Add the page count for this PDF.
                }
            }

            // Loop through each page of the merged PDF to add text.
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                // Check if we've finished processing pages from the current file.
                if (totalProcessedPages >= pagesInCurrentFile && currentFileIndex < pagesPerFile.size()) {
                    pagesInCurrentFile = pagesPerFile.get(currentFileIndex); // Update page count for the new file.
                    totalProcessedPages = 0; // Resets the processed pages for the new file.
                    currentFileIndex++; // Move to the next PDF in the list
                }

                // Create a new page for the overlay, matching the size of the original page.
                PDPage overlayPage = new PDPage(document.getPage(i).getMediaBox());
                overlayDoc.addPage(overlayPage); // Add this page to the overlay document.

                // Draw text on the overlay page.
                try (PDPageContentStream contentStream = new PDPageContentStream(
                        overlayDoc, overlayPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    // contentStream lets us draw on the overlay page.
                    // "APPEND" means we add to the page, "true" keeps existing content, "true" compresses the result.

                    // Add the prefix text (e.g., "Document 1") only on the first page of each original PDF.
                    if (totalProcessedPages == 0) {
                        addText(contentStream, overlayPage,
                                textPrefix + " " + currentFileIndex, // Text like "Document 1".
                                textPosition, true, addBackground); // true = it's a title.
                    }

                    // Add the page number to every page.
                    addText(contentStream, overlayPage,
                            String.valueOf(pageCounter), // Convert number to string (e.g., "1").
                            pageNumberPosition, false, addBackground); // false = not a title.
                }

                pageCounter++; // Increment the page number for the next page.
                totalProcessedPages++; // Increment the processed pages for this file.
            }

            // Save the overlay document to a temporary file.
            File overlayFile = File.createTempFile("overlay", ".pdf");
            overlayDoc.save(overlayFile);
            overlayDoc.close(); // Close the overlay document to free up resources.

            // Apply the overlay to the merged PDF.
            Overlay overlay = new Overlay();
            overlay.setInputFile(tempMergedFile.getAbsolutePath()); // The merged PDF to overlay.
            overlay.setAllPagesOverlayFile(overlayFile.getAbsolutePath()); // The overlay to apply.
            overlay.setOverlayPosition(Overlay.Position.FOREGROUND); // Overlay goes on top.

            // Combine the overlay with the merged PDF and save the result.
            try (PDDocument resultDoc = overlay.overlay(new HashMap<Integer, String>())) {
                resultDoc.save(outputFile); // Save the final PDF to the specified output file.
            }

            // Clean up by deleting temporary files.
            overlayFile.delete();
            tempMergedFile.delete();
        }
    }

    // This method adds text to a page at a specific position.
    private void addText(PDPageContentStream contentStream, PDPage page, String text,
                         int position, boolean isTitle, boolean addBackground) throws IOException {
        // contentStream: Where we draw the text.
        // page: The page we're drawing on.
        // text: The text to write (e.g., "Document 1" or "1").
        // position: Where to place it (0-3 for corners).
        // isTitle: True if it's a prefix, false if it's a page number.
        // addBackground: True to add a gray box behind the text.

        float width = page.getMediaBox().getWidth(); // Get the page width in points.
        float height = page.getMediaBox().getHeight(); // Get the page height in points.
        float margin = 50; // A 50-point margin from the edges.
        float fontSize = isTitle ? 12 : 10; // Titles are size 12, page numbers are size 10.

        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD); // Use bold Helvetica font.
        float textWidth = font.getStringWidth(text) / 1000 * fontSize; // Calculate text width in points.
        float textHeight = fontSize; // Text height will be the same as font size.

        float x, y; // Coordinates for where the text will go.
        switch (position) {
            case 0: // Top-left corner.
                x = margin;
                y = height - margin;
                break;
            case 1: // Top-right corner.
                x = width - margin - textWidth;
                y = height - margin;
                break;
            case 2: // Bottom-left corner.
                x = margin;
                y = margin;
                break;
            case 3: // Bottom-right corner.
                x = width - margin - textWidth;
                y = margin;
                break;
            default: // Default to top-left if position is ever (redundant for now).
                x = margin;
                y = height - margin;
        }

        // Add a gray background behind the text if requested (for now gray color is the only option).
        if (addBackground) {
            float r = 200f / 255f; // Red value for light gray (0-1 scale because it's the scale used by PDFBOX).
            float g = 200f / 255f; // Green value for light gray.
            float b = 200f / 255f; // Blue value for light gray.

            contentStream.setNonStrokingColor(r, g, b); // We set the background color to the R G B values defined before.
            contentStream.addRect(x - 5, y - 5, textWidth + 10, textHeight + 5); // Draw a rectangle behind the text.
            contentStream.fill(); // Fill the rectangle with the color.
            contentStream.setNonStrokingColor(0, 0, 0); // Reset to black for the text.
        }

        // Draw the text on the page.
        contentStream.beginText(); // Start writing text.
        contentStream.setFont(font, fontSize); // Set the font and size.
        contentStream.newLineAtOffset(x, y); // Move to the text position.
        contentStream.showText(text); // Write the text.
        contentStream.endText(); // Finish writing text.
    }
}