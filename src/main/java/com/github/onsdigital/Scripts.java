package com.github.onsdigital;

import com.github.onsdigital.test.api.Approve;
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
import java.util.Date;
import java.util.List;

/**
 * Created by thomasridd on 11/09/15.
 */
public class Scripts {

    /**
     * Get a collection with a specified numbers of bulletins, datasets, and csdbFiles
     *
     * @param bulletins number of large bulletins (UK Environmental Accounts - 118 files)
     * @param datasets number of spreadsheet datasets to include (zip file of size 60mb)
     * @param csdbFiles number of csdb datasets to include (Blue Book - 3800 files)
     * @return
     * @throws Exception
     */
    public static CollectionDescription buildCustomCollection(int bulletins, int datasets, int csdbFiles) throws Exception {
        OneShot.setup();

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

    /**
     * Get and bring to review stage a collection with a specified numbers of bulletins, datasets, and csdbFiles
     *
     * @param bulletins number of large bulletins (UK Environmental Accounts - 118 files)
     * @param datasets number of spreadsheet datasets to include (zip file of size 60mb)
     * @param csdbFiles number of csdb datasets to include (Blue Book - 3800 files)
     * @return
     * @throws Exception
     */
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

    /**
     * Copy a resource to a temporary upload
     *
     * If a json file update its internals so that it references appropriate uris (just in case)
     *
     * @param fromPath
     * @param replaceText <URI_ROOT>
     * @param withText new root uri for this collection
     * @return
     * @throws IOException
     */
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
        CollectionDescription collection = buildReviewedCustomCollection(1, 1, 1);
        Approve.approve(collection.id, OneShot.httpPublisher);
    }
}
