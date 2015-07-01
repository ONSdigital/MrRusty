package com.github.onsdigital.test.generator;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.content.page.statistics.document.article.Article;
import com.github.onsdigital.content.util.ContentUtil;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.test.SetupBeforeTesting;
import com.github.onsdigital.test.api.*;
import com.github.onsdigital.test.generator.markdown.ArticleMarkdown;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.json.Credentials;
import com.github.onsdigital.zebedee.json.PermissionDefinition;
import com.github.onsdigital.zebedee.json.User;
import com.github.onsdigital.zebedee.json.serialiser.IsoDateSerializer;
import com.sun.jna.platform.FileUtils;
import org.apache.poi.util.IOUtils;
import org.bouncycastle.util.Strings;
import org.eclipse.jetty.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomasridd on 23/06/15.
 *
 * The content generator
 *
 */

public class CollectionGenerator {
    static String contentFilePath;
    static Path root;

    static String collectionName = "generator";

    public static User systemUser = SetupBeforeTesting.systemUser;
    public static Credentials systemCredentials = SetupBeforeTesting.systemCredentials;

    public static User adminUser = user("Content admin", "admin@contentgenerator.co.uk");
    public static Credentials adminCredentials = credentials(adminUser.email, Random.password(8));

    public static User publisherUser = user("Content Generator", "generator@contentgenerator.co.uk");
    public static Credentials publisherCredentials = credentials(publisherUser.email, Random.password(8));

    public static User secondSetOfEyesUser = user("Content Second Eyes", "secondeyes@contentgenerator.co.uk");
    public static Credentials secondSetOfEyesCredentials = credentials(secondSetOfEyesUser.email, Random.password(8));

    static Http systemSession = Sessions.get("system");

    static Http httpPublisher;
    static Http httpSecondSetOfEyes;
    static Http httpAdministrator;

    private static String tokenPublisher;
    private static String tokenSecondSetOfEyes;
    private static String tokenAdministrator;

    public static void setupUsers() throws IOException {
        // Set ISO date formatting in Gson to match Javascript Date.toISODate()
        Serialiser.getBuilder().registerTypeAdapter(Date.class, new IsoDateSerializer());

        Response<String> response = systemSession.post(ZebedeeHost.login, systemCredentials, String.class);
        systemSession.addHeader("x-florence-token", response.body);

        systemSession.post(ZebedeeHost.users, adminUser, User.class);
        systemSession.post(ZebedeeHost.password, adminCredentials, String.class);

        systemSession.post(ZebedeeHost.users, publisherUser, User.class);
        systemSession.post(ZebedeeHost.users, secondSetOfEyesUser, User.class);

        systemSession.post(ZebedeeHost.password, publisherCredentials, String.class);
        systemSession.post(ZebedeeHost.password, secondSetOfEyesCredentials, String.class);

        PermissionDefinition adminPermissionDefinition = permission(adminUser, true, false);
        systemSession.post(ZebedeeHost.permission, adminPermissionDefinition, String.class);

        PermissionDefinition publisherPermissionDefinition = permission(publisherUser, false, true);
        systemSession.post(ZebedeeHost.permission, publisherPermissionDefinition, String.class);

        publisherPermissionDefinition = permission(secondSetOfEyesUser, false, true);
        systemSession.post(ZebedeeHost.permission, publisherPermissionDefinition, String.class);

        httpPublisher = Sessions.get("publisher");
        httpSecondSetOfEyes = Sessions.get("secondsetofeyes");
        httpAdministrator = Sessions.get("administrator");

        tokenAdministrator = systemSession.post(ZebedeeHost.login, adminCredentials, String.class).body;
        httpAdministrator.addHeader("x-florence-token", tokenAdministrator);

        tokenPublisher = httpAdministrator.post(ZebedeeHost.login, publisherCredentials, String.class).body;
        tokenSecondSetOfEyes = httpAdministrator.post(ZebedeeHost.login, secondSetOfEyesCredentials, String.class).body;
        httpPublisher.addHeader("x-florence-token", tokenPublisher);
        httpSecondSetOfEyes.addHeader("x-florence-token", tokenSecondSetOfEyes);
    }

    public static CollectionDescription generatorCollection() throws IOException, InterruptedException {
        CollectionDescription collection = new CollectionDescription();
        collection.name = collectionName + Random.id().substring(0,6);
        collection.publishDate = null;

        //deleteWholeCollection(collection); // Tear down any old collection

        Response<CollectionDescription> post = Collection.post(collection, httpPublisher);// Build back an old one

        return post.body;
    }
    public static void killUploadedFiles() {
        // TODO: Code to get rid of the contentGenerator top level collection when finished with
    }

    //----------------------------------------------------------------------------------------------------------------------------
    // CONTENT GENERATOR

    /**
     * Searches every subfolder in root and finds any where data.json and a potential csdb file exist
     *
     * @return
     */
    public static List<HashMap<String, Path>> pairsOfDatasetsToGenerate(Path folder) throws IOException {
        List<HashMap<String, Path>> pairs = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path: stream) {
                if (Files.isDirectory(path)) {
                    HashMap<String, Path> pair = asCSDBPair(path);
                    if (pair != null) { pairs.add(pair); }
                }
            }
        }
        return pairs;
    }
    static boolean couldBeCSDBFile(Path path) {
        if (Files.isDirectory(path)) {
            return false;
        } else if (path.toString().endsWith(".csdb")) {
            return true;
        } else if (path.toString().contains(".") == false) {
            return true;
        } else {
            return false;
        }
    }


    static HashMap<String, Path> asCSDBPair(Path path) throws IOException {
        HashMap<String, Path> csdbPair = new HashMap<>();
        Path dataPath = path.resolve("data.json");
        if (dataPath.toFile().exists()) {
            csdbPair.put("json", dataPath);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path file: stream) {
                    if (couldBeCSDBFile(file)) {
                        csdbPair.put("file", file);
                        return csdbPair;
                    }
                }
            }
            return null;
        } else {
            return null;
        }
    }

    static void addFolderOfCSDBFilesToCollectionAndApprove(CollectionDescription collection, Path folder) throws IOException {

        String baseUri = "contentgenerator/";
        int filePairs = 1;

        List<HashMap<String, Path>> pairs = pairsOfDatasetsToGenerate(folder);
        for (HashMap<String, Path> pair: pairs) {
            System.out.println("Generating Content for files");
            System.out.println("json: " + pair.get("json").toString());
            System.out.println("file: " + pair.get("file").toString());

            String pairUri = String.format("%s%03d", baseUri, filePairs++);
            Response<String> json1 = Content.upload(collection.id, Strings.toLowerCase(pairUri + "/data.json"), pair.get("json").toFile(), httpPublisher);
            Response<String> file = Content.upload(collection.id, Strings.toLowerCase(pairUri + "/" + pair.get("file").getFileName()), pair.get("file").toFile(), httpPublisher);
            Complete.complete(collection.id, pairUri + "/data.json", httpPublisher);
            Review.review(collection.id, pairUri + "/data.json", httpSecondSetOfEyes);

        }

        //Approve.approve(collection.id, httpPublisher);
    }

    //----------------------------------------------------------------------------------------------------------------------------


    //----------------------------------------------------------------------------------------------------------------------------
    // BULLETINS
    static void addFolderOfBulletinsToCollectionAndApprove(CollectionDescription collection, Path folder) throws IOException {
        String uri = "";
        String content = "";
        List<Path> markdownFiles = new ArrayList<>();

        // Get a list of bulletins
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path: stream) {
                if (Files.isRegularFile(path) && path.toString().length() > 3 && path.toString().endsWith(".md")) {
                    markdownFiles.add(path);
                }
            }
        }

        // Generate each
        for (Path markdownFile: markdownFiles) {
            Content.create(collection.id, content, Strings.toLowerCase(uri + "/data.json"), httpPublisher);
        }
    }
    //----------------------------------------------------------------------------------------------------------------------------


    //----------------------------------------------------------------------------------------------------------------------------
    // ARTICLES
    static void addFolderOfArticlesToCollectionAndApprove(CollectionDescription collection, Path folder) throws IOException {
        String baseUri = "collectiongenerator/";
        String uri = "";
        String content = "";
        List<Path> markdownFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path: stream) {
                if (Files.isRegularFile(path) && path.toString().length() > 3 && path.toString().endsWith(".md")) {
                    markdownFiles.add(path);
                }
            }
        }

        // Generate each
        for (Path markdownFile: markdownFiles) {
            Path processedFile = preprocessBetaContent(markdownFile);

            List<String> errors = ArticleMarkdown.checkMarkdown(processedFile, collection, httpPublisher);

            if (errors != null) {
                for (String error: errors) {
                    System.out.println(error);
                }
            } else {

                Article article = ArticleMarkdown.readArticle(processedFile);


                // Make a temp file
                content = Serialiser.serialise(article);
                File tmp = File.createTempFile(Random.id(), "json");
                Files.write(tmp.toPath(), content.getBytes());

                // Upload
                Response<String> response = Content.upload(collection.id, article.getUri().toString() + "/data.json", tmp, httpPublisher);
                if (response.statusLine.getStatusCode() == HttpStatus.OK_200) {
                    System.out.println("Success");
                } else {
                    System.out.println("Upload failed with error code: " + response.statusLine.getReasonPhrase());
                }

                Complete.complete(collection.id, article.getUri().toString() + "/data.json", httpPublisher);
                Review.review(collection.id, article.getUri().toString() + "/data.json", httpSecondSetOfEyes);
            }
        }
    }
    //----------------------------------------------------------------------------------------------------------------------------


    public static void main(String[] args) throws IOException, InterruptedException {
        // Given
        // a collection

        contentFilePath = System.getenv("CONTENT_GENERATOR_PATH");
        if (contentFilePath == null) {
            System.out.println("ERROR: Please set an environment variable for CONTENT_GENERATOR_PATH");
        } else {
            root = Paths.get(contentFilePath);
        }
        setupUsers();

        CollectionDescription collection = generatorCollection();


        addFolderOfCSDBFilesToCollectionAndApprove(collection, root.resolve("timeseries"));

        addFolderOfBulletinsToCollectionAndApprove(collection, root.resolve("bulletins"));

        addFolderOfArticlesToCollectionAndApprove(collection, root.resolve("articles"));

        // If we
        // we approve it using publish credentials
        //Response<String> response = publish(collection.id, httpPublisher);
    }



    private static Response<String> publish(String collectionID, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.publish.addPathSegment(collectionID);
        return http.post(endpoint, null, String.class);
    }

    /**
     * Convenience method for generating user objects.
     *
     * @param name  The user's name
     * @param email The user's email
     * @return A {@link User} containing the given details.
     */
    private static User user(String name, String email) {
        User user = new User();
        user.name = name;
        user.email = email;
        return user;
    }
    /**
     * Convenience method for generating login credentials.
     *
     * @param email    The email address
     * @param password The password
     * @return A {@link Credentials} instance containing the given details.
     */
    private static Credentials credentials(String email, String password) {
        Credentials credentials = new Credentials();
        credentials.email = email;
        credentials.password = password;
        return credentials;
    }

    private static PermissionDefinition permission(User user, boolean admin, boolean editor) {

        PermissionDefinition permissionDefinition = new PermissionDefinition();
        permissionDefinition.email = user.email;
        permissionDefinition.admin = admin;
        permissionDefinition.editor = editor;
        return permissionDefinition;
    }

    static Path preprocessBetaContent(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);

        List<String> lines2 = new ArrayList<>();
        List<String> result = new ArrayList<>();

        // Preprocess 1 - Cut down and trim
        boolean singleFound = false;
        for (String line : lines) {
            // Cut down ### or more by one header size
            if (line.trim().length() >= 3 && line.trim().startsWith("###")) {
                // If a section break occurs before a title add in a dummy line
                if (singleFound == false) {
                    lines2.add("# Bulletin");
                    singleFound = true;
                }
                lines2.add(lineMinusOneHash(ltrim(line)));

            } else if (line.trim().length() > 1 && line.trim().startsWith("#") && line.trim().charAt(1) != '#') {
                lines2.add(ltrim(line));
                singleFound = true;
            } else if (line.trim().length() > 0 && line.trim().startsWith("#")) {
                lines2.add(ltrim(line));
            } else {
                lines2.add(ltrim(line));
            }
        }

        // Preprocess 2 - Finds the length of the hashes at the beginning
//        for (String line : lines2) {
//            int hashes = 0;
//            while ((hashes < line.length()) && line.charAt(hashes) == '#') {
//                hashes++;
//            }
//            if (hashes > 0) {
//                result.add(line.substring(0, hashes) + " " + line.substring(hashes).trim());
//            } else {
//                result.add(line);
//            }
//        }

        // Write the file to a processed directory
        Path processedPath = path.getParent().resolve("processed").resolve(path.getFileName());

        if (!Files.exists(processedPath.getParent())) {
            Files.createDirectory(processedPath.getParent());
        }
        if (Files.exists(processedPath)) {
            Files.delete(processedPath);
        }

        Files.write(processedPath, lines2);

        return processedPath;
    }
    static String lineMinusOneHash(String line) {
        String trimmed = line.trim();
        String prefix = line.substring(0, line.length() - trimmed.length());
        String suffix = "";

        int hashes = 0;
        while ((hashes < trimmed.length()) && trimmed.charAt(hashes) == '#') {
            hashes++;
        }
        if (hashes > 1) {
            suffix = trimmed.substring(1, hashes) + " " + trimmed.substring(hashes).trim();
        } else {
            suffix = trimmed;
        }

        return prefix + suffix;
    }

    public static String ltrim(String s) {
        int i = 0;
        while (i < s.length() && (s.charAt(i) == ' ')) {
            i++;
        }
        return s.substring(i);
    }
}
