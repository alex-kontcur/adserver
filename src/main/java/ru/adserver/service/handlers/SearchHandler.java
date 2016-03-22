package ru.adserver.service.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.adserver.service.CaseHandler;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * SearchService
 *
 * @author Kontsur Alex (bona)
 * @since 12.11.2015
 */
@Service
public class SearchHandler implements CaseHandler {

    private static final Logger logger = LoggerFactory.getLogger(SearchHandler.class);

    private static final String URI_PREFIX = "/search/";

    @Inject
    private AdProvider adProvider;

    @Inject
    private QueryParser queryParser;

    @Value("${index.path}")
    private String indexPath;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public Pair<String, String> applyCase(String uri) {
        String phrase = uri.substring(URI_PREFIX.length());
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath).toFile()));
            IndexSearcher indexSearcher = new IndexSearcher(reader);

            List<String> nums = new ArrayList<>();
            TopDocs results = indexSearcher.search(queryParser.parse(phrase), reader.numDocs());
            ScoreDoc[] scoreDocs = results.scoreDocs;
            for (ScoreDoc hit : scoreDocs) {
                int docId = hit.doc;
                Document d = indexSearcher.doc(docId);
                String num = d.get("num");
                nums.add(num);
            }
            return Pair.of(gson.toJson(nums), "application/json");
        } catch (Exception e) {
            logger.error("Error while searching documents ->", e);
        }
        return Pair.of("Not found", "text/html");
    }

    @Override
    public boolean canHandle(String uri) {
        return uri.startsWith(URI_PREFIX);
    }
}
