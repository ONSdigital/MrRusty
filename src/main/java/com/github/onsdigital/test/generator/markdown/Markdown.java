package com.github.onsdigital.test.generator.markdown;

import com.github.onsdigital.content.partial.markdown.MarkdownSection;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

class Markdown {

	static Pattern sane = Pattern.compile("[A-Za-z0-9 ]");
	private String filename;
	String title;
	Map<String, String> properties = new HashMap<>();
	List<MarkdownSection> sections = new ArrayList<>();
	List<MarkdownSection> accordion = new ArrayList<>();
	List<String> errors = new ArrayList<>();

	public Markdown(Path file) throws IOException {

		filename = file.getFileName().toString();

		// Read the markdown - it may be in UTF8 or in Windows encoding (cp1252)
		// because these files will be edited on Linux, Mac and Windows
		// machines.


		try (Reader reader = Files.newBufferedReader(file, Charset.forName("UTF8"))) {
			try (Scanner scanner = new Scanner(reader)) {
				readHeader(scanner);
				// System.out.println("UTF8: " + properties);
				readContent(scanner);
			}
		}

		// It looks like if UTF8 fails we don't get any content, so retry with
		// Windows encoding:
		if (properties.size() == 0) {

			try (Reader reader = Files.newBufferedReader(file, Charset.forName("cp1252"))) {
				try (Scanner scanner = new Scanner(reader)) {
					readHeader(scanner);
					// System.out.println("cp1252: " + properties);
					readContent(scanner);
				}
			}
		}

	}


	/**
	 * Reads the "header" information about the article. Information is expected
	 * in the form "key : value" and the header block should be terminated with
	 * an empty line. The recognised keys are as follows.
	 * <ul>
	 * <li>Next release</li>
	 * <li>Contact title</li>
	 * <li>Contact email</li>
	 * <li>Lede</li>
	 * <li>More</li>
	 * <li>Headline 1</li>
	 * <li>Headline 2</li>
	 * <li>Headline 3</li>
	 * </ul>
	 *
	 * @param scanner
	 *            The {@link Scanner} to read lines from.
	 */
	private void readHeader(Scanner scanner) {
		errors = new ArrayList<>();

		String line;
		while (scanner.hasNextLine() && StringUtils.isNotBlank(line = scanner.nextLine())) {
			// Extract property values:
			String[] property = readProperty(line);
			String key = StringUtils.lowerCase(StringUtils.trim(property[0]));
			if (key == null) {
				errors.add("Line '" + line + "' in properties could not be parsed");
			} else {
				properties.put(sanitise(key), property[1]);
			}
		}
	}

	/**
	 * This method is a bit of a hardcore string sanitiser.
	 * <p>
	 * This became necessary due to some dodgy encoding issues that created a
	 * non-printing character at the start of the string which wasn't removed by
	 * trimming.
	 *
	 * @param name
	 * @return
	 */
	private static String sanitise(String name) {
		StringBuilder result = new StringBuilder();

		for (char c : name.toCharArray()) {
			if (sane.matcher(String.valueOf(c)).matches()) {
				// System.out.println("'" + c + "'");
				result.append(c);
			}
		}
		// System.out.println();
		return result.toString();
	}

	/**
	 * Parses the markdown content of the article into title and sections;
	 *
	 * @param scanner
	 *            The {@link Scanner} to read lines from.
	 */
	private void readContent(Scanner scanner) {

		MarkdownSection currentSection = null;

		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();

			// Title
			Boolean matched = false;
			if (StringUtils.isBlank(title)) {
				String value = matchTitle(line);
				if (StringUtils.isNotBlank(value)) {
					this.title = value;
					matched = true;
				}
			}

			// Section heading
			if (!matched) {
				MarkdownSection newSection = matchHeading(line);
				if (newSection != null) {
					if (newSection.getTitle().matches("\\[accordion\\].*")) {
						// Remove the marker, case insensitively with "(?i)"
						// and add the section to the accordion list:
						newSection.setTitle(newSection.getTitle().replaceFirst("(?i)\\[accordion\\]\\s*", ""));
						accordion.add(newSection);
					} else {
						sections.add(newSection);
					}
					currentSection = newSection;
					matched = true;
				}
			}

			// Section content
			if (!matched && currentSection != null) {
				String markdown = currentSection.getMarkdown();
				if (StringUtils.isNotBlank(markdown)) {
					markdown += "\n";
				}
				markdown += line;
				currentSection.setMarkdown(ltrim(markdown));
			}

		}

		// Set a default title if none was found in the markdown:
		title = StringUtils.defaultIfBlank(title, FilenameUtils.getBaseName(filename));
	}

	public static String ltrim(String s) {
		int i = 0;
		while (i < s.length() && (s.charAt(i) == ' ')) {
			i++;
		}
		return s.substring(i);
	}

	/**
	 * Sanitises an article title to <code>[a-zA-Z0-9]</code>.
	 *
	 * @return A sanitised string.
	 */
	String toFilename() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < title.length(); i++) {
			String character = title.substring(i, i + 1);
			if (character.matches("[a-zA-Z0-9]")) {
				result.append(character);
			}
		}
		return result.toString().toLowerCase();
	}

	/**
	 * Extracts a property key and value from the given line.
	 *
	 * @param line
	 *            The String to be parsed.
	 * @return A two-element String array. If the line can't be parsed the
	 *         elements of the array will be null.
	 */
	static String[] readProperty(String line) {
		String[] result = new String[2];

		int separatorIndex = line.indexOf(':');
		if (separatorIndex > 0) {
			result[0] = StringUtils.trim(line.substring(0, separatorIndex));
			if (line.length() > separatorIndex + 1) {
				result[1] = line.substring(separatorIndex + 1);
			}
		}

		result[0] = StringUtils.trim(result[0]);
		result[1] = StringUtils.trim(result[1]);

		return result;
	}

	/**
	 * If the given line matches markdown H1 syntax (atx only, not Setext), sets
	 * the article title to the title text, unless the title has already been
	 * set.
	 *
	 * @param line
	 *            The line to be matched.
	 * @return
	 * @see <a
	 *      href="http://daringfireball.net/projects/markdown/syntax">http://daringfireball.net/projects/markdown/syntax</a>
	 */
	static String matchTitle(String line) {
		String result = null;

		// Set the title
		String h1Regex = "#\\s+";
		if (line.matches(h1Regex + ".*")) {
			result = line.replaceFirst(h1Regex, "");
		}

		return result;
	}

	/**
	 * If the given line matches markdown H1 syntax (atx only, not Setext), sets
	 * the article title to the title text, unless the title has already been
	 * set.
	 *
	 * @param line
	 *            The line to be matched.
	 * @see <a
	 *      href="http://daringfireball.net/projects/markdown/syntax">http://daringfireball.net/projects/markdown/syntax</a>
	 */
	static MarkdownSection matchHeading(String line) {
		MarkdownSection result = null;

		// Set the section title
		String h2Regex = "##\\s+";
		if (line.matches(h2Regex + ".*")) {
			result = new MarkdownSection();
			result.setTitle(line.replaceFirst(h2Regex, "").trim());
			result.setMarkdown(StringUtils.EMPTY);
		}

		return result;
	}

}