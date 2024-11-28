/**
 * Commands List:
  .paragraph: starts a new paragraph
	.fill: sets indentation to fill for paragraphs, where the last character of a line must
	end at the end of the margin (except for the last line of a paragraph)
	.nofill: the default, sets the formatter to regular formatting
	.regular: resets the font to the normal font
	.italic: sets the font to italic
	.bold: sets the font to bold
	.indent <number>: indents the specified amount (each unit is probably about the length of the string “WWWW”, but other values would work)
	.large: increase the font size
	.normal: set the font size back to normal

	* @author asadhumayun
 */

import java.io.File;
import java.util.Scanner;

import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

public class App {
		public static void main(String[] args) throws Exception {
				// ASSUMPTION(S) - input and output path(s)
				final String FILE_PATH = "input.txt";
				final String OUTPUT_PATH = "out.pdf";

				try {
					// Read data from input file
					ArrayList<String> lines = App.readInputFile(FILE_PATH);

					// Define and output PDF file
					createPdf(lines, OUTPUT_PATH);

					System.out.println("Pdf successfully compiled at " + OUTPUT_PATH);
				} catch (Exception e) {
					System.out.println("[App.main]: " + e);
				}
		}

		/**
		 * Reads input file and returns its contents, line-by-line as an ArrayList<String>.
		 * @param path Path to input file, containing PDF text and formatting commands (refer to top-of-file for list)
		 * @return File content, line-by-line
		 */
		public static ArrayList<String> readInputFile(String path) {
			// Dynamic ArrayList to store file lines
			ArrayList<String> rawInput = new ArrayList<>();

			// Attempt to open input file
			try {
				File file = new File(path);
				Scanner data = new Scanner(file);

				// iterate through file, add next line to ArrayList
				while (data.hasNextLine()) {
					String next = data.nextLine();
					rawInput.add(next); // ArrayList seems to function more like a Set/Collection
				}

				data.close(); // close file to prevent resource leaks
			} catch (FileNotFoundException e) {
				System.out.println("[App.readInputFile]: FileNotFoundException with file input (param: path) -> \"" + path + "\"");
			}

			return rawInput;
		}

		/**
		 * Attempts to create the PDF document using iText API
		 * @param input Array of lines from the input file 
		 * @param dest Output path for PDF file
		 * @throws IOException
		 */
		public static void createPdf(ArrayList<String> input, String dest) throws IOException {

			// Declare here to keep in function-block scope instead of isolating to try/catch block
			PdfDocument pdf = null;
			Document document = null;

			try {
				// Open output file as PdfDocument using PdfWriter class
				pdf = new PdfDocument(new PdfWriter(dest));
				// Document class necessary to add paragraphs to the PdfDocument
				document = new Document(pdf);
			} catch (FileNotFoundException e) {
				System.out.println("[App.createPdf]: FileNoutFoundException where dest=" + dest);
			}

			// default font size
			Integer fontSize = 12;

			// Used to keep track of current "state"
			Boolean bold = false;
			Boolean italics = false;
			Boolean justify = false;
			Boolean large = false;

			// Unit-value is 10x larger than this (for .indent)
			Integer indentations = 0;

			// ASSUMPTION - The default font stated is not available builtin and likely has copyrights against it
			PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
			PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
			PdfFont italicsFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

			Paragraph current = new Paragraph()
														.setFont(regularFont)
														.setFontSize(fontSize);

			// iterate through input file, line-by-line
			for (String line : input) {
				if (!line.startsWith(".")) {
					// Line is not a command, so is therefore subject to its preceding command

					// line content (now established that it is text)
					Text text = new Text(line);

					if (bold) text.setFont(boldFont);
					if (italics) text.setFont(italicsFont);
					if (large) text.setFontSize(22);
					if (justify) current.setTextAlignment(TextAlignment.JUSTIFIED);

					// Add text to paragraph
					current.add(text);

					// Reset state
					bold = false;
					italics = false;
					large = false;
					justify = false;
				}

				// Split line into command and args, used primarily for .indent command where an argument is accessed
				String[] lineArgs = line.split(" "); // ".indent 4" -> [".indent", "4"]
				String command = lineArgs[0];

				// switch (command), NOT line - doing this results in ".indent" case not being triggered
				switch (command) {
					case ".paragraph":
					 // Finish current paragraph, then create a new one ready for next pass
						document.add(current);
						current = new Paragraph()
						.setFont(regularFont)
						.setFontSize(fontSize);

						break;

					case ".bold":
						bold = true;
						break;
					
					case ".italics":
						italics = true;
						break;

					case ".large":
						large = true;
						break;

					case ".fill":
						document.add(current);
						// create new paragraph with appropriate padding from left side
						current = new Paragraph()
						.setFont(regularFont)
						.setPaddingLeft(100)
						.setFontSize(fontSize);
						// This gets the paragraph "justified" on next pass - see above
						justify = true;
						break;

				 case ".nofill":
						document.add(current);
						current = new Paragraph()
							.setFont(regularFont)
							.setFontSize(fontSize);
						justify = false;
						break;

				 case ".regular":
					// Reset all states
					bold = false;
					italics = false;
					large = false;
					justify = false;
					break;

				 case ".indent":
					// Extract indentation # and cast to Int
					indentations = Integer.parseInt(lineArgs[1]);

					// Finish adding current paragraph then create new one with variable-based padding
					document.add(current);

					current = new Paragraph()
					.setFont(regularFont)
					.setPaddingLeft(10 * indentations)
					.setFontSize(fontSize);

					break;
				}
			}

			// add to ensure that content of paragraph is still added to the document
			// otherwise data may be lost
			document.add(current);

			// Close document
			document.close();
		}
}
