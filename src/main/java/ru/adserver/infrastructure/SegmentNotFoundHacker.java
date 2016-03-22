package ru.adserver.infrastructure;

import org.apache.lucene.index.IndexWriter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * SegmentNotFoundHacker
 *
 * @author Kontsur Alex (bona)
 * @since 14.11.2015
 */
@Service
public class SegmentNotFoundHacker {

    @Inject
    private IndexWriter indexWriter;

    @PostConstruct
    public void init() throws Exception {
        indexWriter.commit();
    }

}
