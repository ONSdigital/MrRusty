package com.github.onsdigital;

import com.github.onsdigital.test.api.Collection;
import com.github.onsdigital.test.api.Complete;
import com.github.onsdigital.test.api.Review;
import com.github.onsdigital.test.api.oneliners.OneShot;
import com.github.onsdigital.test.json.CollectionDescription;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by thomasridd on 11/09/15.
 */
public class Scripts {

    public static CollectionDescription buildCustomCollection(int bulletins, int datasets, int csdbFiles) throws Exception {
        OneShot.setup();


        List<String> uris = new ArrayList<>();
        final Path bulletinSource = Paths.get("src/main/resources/script_resources/complex_bulletin");
        final Path datasetSource = Paths.get("src/main/resources/script_resources/big_dataset");
        final Path csdbSource = Paths.get("src/main/resources/script_resources/csdb_dataset");

        SimpleDateFormat format = new SimpleDateFormat("EEEE_hh-mm-ss");
        String collectionTime = format.format(new Date());

        final CollectionDescription collection = OneShot.publishedCollection();

        // upload bulletins by walking the filetree a bunch of times
        for (int i = 1; i <= bulletins; i++) {
            final String uriRoot = "/archive/" + collectionTime + "/bulletins/bulletin_" + i;
            Files.walkFileTree(bulletinSource, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs)
                        throws IOException {
                    String uri = uriRoot + "/" + bulletinSource.relativize(filePath);
                    Path uploadPath = filePath;

                    // Replace uris if necessary
                    if (filePath.toString().endsWith(".json")) { uploadPath = tempFileWithStringReplacement(filePath, "<URI_ROOT>", uriRoot); }

                    // Do the upload
                    OneShot.upload(collection.id, uri, uploadPath.toFile(), OneShot.httpPublisher);

                    return FileVisitResult.CONTINUE;
                }
            });
        }

        // upload big datasetss
        for (int i = 1; i <= datasets; i++) {
            final String uriRoot = "/archive/" + collectionTime + "/datasets/dataset_" + i + "/";
            Files.walkFileTree(datasetSource, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs)
                        throws IOException {
                    String uri = uriRoot + datasetSource.relativize(filePath);
                    Path uploadPath = filePath;

                    // Replace uris if necessary
                    if (filePath.toString().endsWith(".json")) { uploadPath = tempFileWithStringReplacement(filePath, "<URI_ROOT>", uriRoot); }

                    // Do the upload
                    OneShot.upload(collection.id, uri, uploadPath.toFile(), OneShot.httpPublisher);

                    return FileVisitResult.CONTINUE;
                }
            });
        }

        // upload csdb files
        for (int i = 1; i <= csdbFiles; i++) {
            final String uriRoot = "/archive/" + collectionTime + "/datasets/csdb_" + i + "/";
            Files.walkFileTree(csdbSource, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs)
                        throws IOException {
                    String uri = uriRoot + csdbSource.relativize(filePath);
                    Path uploadPath = filePath;

                    // Replace uris if necessary
                    if (filePath.toString().endsWith(".json")) { uploadPath = tempFileWithStringReplacement(filePath, "<URI_ROOT>", uriRoot); }

                    // Do the upload
                    OneShot.upload(collection.id, uri, uploadPath.toFile(), OneShot.httpPublisher);

                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return Collection.get(collection.id, OneShot.httpPublisher).body;
    }

    public static CollectionDescription buildReviewedCustomCollection(int bulletins, int datasets, int csdbFiles) throws Exception {
        CollectionDescription collection = Scripts.buildCustomCollection(bulletins, datasets, csdbFiles);

        // Complete everything
        collection = Collection.get(collection.id, OneShot.httpPublisher).body;
        for (String uri : collection.inProgressUris) {
            if (uri.endsWith("data.json")) { Complete.complete(collection.id, uri, OneShot.httpPublisher);}
        }

        // Review everything
        collection = Collection.get(collection.id, OneShot.httpPublisher).body;
        for (String uri : collection.completeUris) {
            if (uri.endsWith("data.json")) { Review.review(collection.id, uri, OneShot.httpSecondSetOfEyes);}
        }

        return Collection.get(collection.id, OneShot.httpPublisher).body;
    }

    private static Path tempFileWithStringReplacement(Path fromPath,String replaceText, String withText) throws IOException {
        Path tmp = Files.createTempFile("temp", ".json");
        List<String> lines = Files.readAllLines(fromPath, Charset.forName("UTF8"));

            try(PrintStream ps = new PrintStream(tmp.toFile())) {
                for(String line: lines) {
                    String outString = line.replaceAll(replaceText, withText);
                    ps.print(outString);
                }
            }
        return tmp;
    }

    public static void main(String[] args) throws Exception {
        buildReviewedCustomCollection(1, 1, 1);
    }
}
