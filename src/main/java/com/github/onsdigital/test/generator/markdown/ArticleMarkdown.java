package com.github.onsdigital.test.generator.markdown;

import com.github.onsdigital.content.page.statistics.document.article.Article;
import com.github.onsdigital.content.page.statistics.document.article.ArticleDescription;
import com.github.onsdigital.content.page.taxonomy.ProductPage;
import com.github.onsdigital.content.partial.Contact;
import com.github.onsdigital.content.util.ContentUtil;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.test.api.Content;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ArticleMarkdown {
	public static List<String> incidentLog = new ArrayList<>();

	public static Article readArticle(Path file) throws IOException {

		// Read the file
		System.out.println("Processing article from: " + file);
		Markdown markdown = new Markdown(file);

		// Set up the article
		Article article = new Article();
		ArticleDescription description = new ArticleDescription();

		description.setTitle(markdown.title);
		setDescription(description, markdown);
		article.setSections(markdown.sections);
		article.getSections().addAll(markdown.accordion);

		article.setDescription(description);

		// Set the URI if necessary:
		if (article.getUri() == null) {
			article.setUri(toURI(article));
		}

		return article;
	}

	static URI toURI(Article article) {
		URI result = null;

		if (article != null) {
			if (article.getUri() == null) {
				// Get the basic uri root
				String baseUri = getProductPageURI(article) + "/articles";

				// New bulletin URIs include date
				SimpleDateFormat format =
						new SimpleDateFormat("yyyy-MM-dd");
				String dateText = format.format(new Date());
				if (article.getDescription().getReleaseDate() != null) {
					dateText = format.format(article.getDescription().getReleaseDate());
				}

				String uriText = baseUri + "/" + StringUtils.trim(toFilename(article)) + "/" + dateText;
				// Lastly the file name
				article.setUri(URI.create(uriText.toLowerCase()));
			}
			result = article.getUri();
		}

		return result;
	}

	static String getProductPageURI(Article article) {
		String ppURI = "/" + lcaseAlphanumeric(article.getDescription().theme);
		ppURI += "/" + lcaseAlphanumeric(article.getDescription().level2);
		if (article.getDescription().level3 != null && article.getDescription().level3.length() > 0) {
			ppURI += "/" + lcaseAlphanumeric(article.getDescription().level3);
		}
		return ppURI;
	}

	static boolean hasProductPage(Article article, CollectionDescription collection, Http http) {
		if ((article.getDescription().theme == null) || (article.getDescription().level2 == null)) { return false; }

		String productPageURI = getProductPageURI(article);
			try {
				Path path = Content.get(collection.id, productPageURI + "/data.json", http).body;
				ContentUtil.deserialise(Files.newInputStream(path), ProductPage.class);
				return true;
			} catch (Exception e) {
				return false;
			}
	}

	public static List<String> checkMarkdown(Path path, CollectionDescription collection, Http http) throws IOException {
		List <String> errors = new ArrayList<>();
		errors.add("Errors in " + path);

		if (path.toFile().exists() == false) {
			errors.add("CRITICAL: Could not find file");
			return errors;
		}
		Markdown markdown = new Markdown(path);

		// Set up the bulletin
		Article article = new Article();
		ArticleDescription description = new ArticleDescription();

		// Set title specifically otherwise it gets extracted from content
		description.setTitle(markdown.title);
		List<String> descriptionErrors = setDescription(description, markdown);

		// Set the description section
		article.setSections(markdown.sections);
		article.getSections().addAll(markdown.accordion);

		article.setDescription(description);

		if (hasProductPage(article, collection, http) == false) {
			errors.add("CRITICAL Not a product page: " + article.getDescription().theme + " - " + article.getDescription().level2 + " - " + article.getDescription().level3);
		}

		for (String incident: descriptionErrors) {
			errors.add("WARNING: " + incident);
		}
		for (String warning: markdown.errors) {
			errors.add("WARNING: " + warning);
		}

		if (errors.size() > 1) {
			return errors;
		} else {
			return null;
		}
	}

	private static List<String> setDescription(ArticleDescription articleDescription, Markdown markdown) {

		Map<String, String> properties = markdown.properties;
		List<String> errors = new ArrayList<>();

		// Location
		articleDescription.theme = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("theme"), null);
		if (articleDescription.theme == null) { articleDescription.theme = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("level 1"), null); }


		articleDescription.level2 = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("level 2"), null);
		articleDescription.level3 = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("level 3"), null);

		// Additional details
		articleDescription.setSummary(org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("summary"), null));

		// Release and title (babbage details)
		String title = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("name"), null);
		if (title == null) { title = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("title"), null); }
		if (title == null) { errors.add("CRITICAL Error - Article title not given"); }
		articleDescription.setTitle(title);

		String edition = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("edition"), null);
		if (edition == null) { edition = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("release"), null); }
		articleDescription.setEdition(edition);

		String keywords = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("keywords"), null);
		List <String> keywordArray = new ArrayList<>();
		if (keywords != null) {
			keywordArray = Arrays.asList(keywords.split(","));
			for(String keyword: keywordArray) {
				keyword = keywords.trim();
			}
		}
		articleDescription.set_abstract(org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("abstract"), null));


		articleDescription.setKeywords(keywordArray);



		//Contact info
		Contact contact = new Contact();
		contact.setName(org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("contact title"), null));
		if (contact.getName() == null) { contact.setName(org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("contact name"), null));}

		contact.setEmail(org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("contact email"), null));
		contact.setTelephone(org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("phone"), null));

		articleDescription.setContact(contact);

		//TODO: Where is next release?
		articleDescription.setNextRelease(org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("next release"), null));

		Date releaseDate = toDate(properties.remove("release date"),errors);
		if (releaseDate == null) { releaseDate = toDate(properties.remove("released date"), errors); }
		articleDescription.setReleaseDate(releaseDate == null ? null : releaseDate);


		// Additional fields for migration:
		String natStat = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("national statistics"), null);
		if (natStat == null) { natStat = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("national statistic"), "yes"); }
		articleDescription.setNationalStatistic(BooleanUtils.toBoolean(natStat));


		articleDescription.setLanguage(org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("language"), null));
		// Split keywords by commas:
//		String searchKeywordsString = org.apache.commons.lang.StringUtils.defaultIfBlank(properties.remove("search keywords"), "");
//		String[] keywords = org.apache.commons.lang.StringUtils.split(searchKeywordsString, ',');
//		List<String> searchKeywords = new ArrayList<String>();
//		if (keywords != null) {
//			for (int i = 0; i < keywords.length; i++) {
//				searchKeywords.add(org.apache.commons.lang.StringUtils.trim(keywords[i]));
//			}
//		}
//		articleDescription.setKeywords(searchKeywords);

		// Note any unexpected information
		for (String property : properties.keySet()) {
			String msg = "Article key not recognised: '" + property + "' (length " + property.length() + " for value '" + properties.get(property) + "')";
			errors.add(msg);
		}

		return errors;
	}

	static Date toDate(String date, List<String> errors)  {
		if (org.apache.commons.lang.StringUtils.isBlank(date)) {
			return null;
		}

		try {
			return new SimpleDateFormat("dd MMMM yyyy").parse(date);
		} catch (ParseException e) {
			errors.add("Date formatting failed, date:" + date);
			return null;
		}

	}

	/**
	 * Sanitises an article title to <code>[a-zA-Z0-9]</code>.
	 *
	 * @return A sanitised string.
	 */
	public static String toFilename(Article article) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < article.getDescription().getTitle().length(); i++) {
			String character = article.getDescription().getTitle().substring(i, i + 1);
			if (character.matches("[a-zA-Z0-9]")) {
				result.append(character);
			}
		}
		return result.toString().toLowerCase();
	}
	public static String lcaseAlphanumeric(String string) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			String character = string.substring(i, i + 1);
			if (character.matches("[a-zA-Z0-9]")) {
				result.append(character);
			}
		}
		return result.toString().toLowerCase();
	}
}