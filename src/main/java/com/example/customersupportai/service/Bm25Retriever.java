package com.example.customersupportai.service;

import com.example.customersupportai.model.Department;
import com.example.customersupportai.model.RetrievedSop;
import com.example.customersupportai.model.SopDocument;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class Bm25Retriever {

    private static final Logger log = LoggerFactory.getLogger(Bm25Retriever.class);

    private static final double K1 = 1.5;
    private static final double B = 0.75;

    private final SopRepository sopRepository;
    private final int defaultTopK;
    private final Map<String, Integer> documentFrequencies = new HashMap<>();
    private final Map<String, Map<String, Integer>> termFrequenciesBySopId = new HashMap<>();
    private double averageDocumentLength;

    public Bm25Retriever(
            SopRepository sopRepository,
            @Value("${customer-support.retrieval-top-k}") int defaultTopK
    ) {
        this.sopRepository = sopRepository;
        this.defaultTopK = defaultTopK;
    }

    @PostConstruct
    void buildIndex() {
        List<SopDocument> documents = sopRepository.findAll();
        if (documents.isEmpty()) {
            averageDocumentLength = 0;
            log.warn("No SOP documents found for BM25 index");
            return;
        }

        int totalLength = 0;
        for (SopDocument document : documents) {
            List<String> tokens = SopRepository.tokenize(document.content());
            totalLength += tokens.size();
            Map<String, Integer> termFrequencies = new HashMap<>();
            Set<String> seen = new HashSet<>();

            for (String token : tokens) {
                termFrequencies.merge(token, 1, Integer::sum);
                if (seen.add(token)) {
                    documentFrequencies.merge(token, 1, Integer::sum);
                }
            }

            termFrequenciesBySopId.put(document.id(), termFrequencies);
        }

        averageDocumentLength = (double) totalLength / documents.size();
        log.info("BM25 index built documents={} avgDocumentLength={}", documents.size(), String.format("%.2f", averageDocumentLength));
    }

    public List<RetrievedSop> retrieve(String message, Department department) {
        return retrieve(message, department, defaultTopK);
    }

    List<RetrievedSop> retrieve(String message, Department department, int topK) {
        String query = message + " " + department.name().replace("_", " ");
        List<String> queryTerms = SopRepository.tokenize(query);
        List<SopDocument> documents = sopRepository.findAll();

        List<RetrievedSop> results = documents.stream()
                .map(document -> new ScoredDocument(document, score(document, queryTerms)))
                .sorted(Comparator.comparingDouble(ScoredDocument::score).reversed())
                .limit(Math.max(1, topK))
                .map(scored -> new RetrievedSop(
                        scored.document().id(),
                        scored.document().filename(),
                        scored.score(),
                        excerpt(scored.document().content(), 1500)
                ))
                .toList();
        log.debug("BM25 retrieval queryTerms={} topK={} results={}", queryTerms.size(), topK,
                results.stream().map(RetrievedSop::sopId).toList());
        return results;
    }

    private double score(SopDocument document, List<String> queryTerms) {
        Map<String, Integer> termFrequencies = termFrequenciesBySopId.getOrDefault(document.id(), Map.of());
        int documentLength = Math.max(1, document.wordCount());
        int documentCount = Math.max(1, sopRepository.findAll().size());
        double total = 0;

        for (String term : queryTerms) {
            int termFrequency = termFrequencies.getOrDefault(term, 0);
            if (termFrequency == 0) {
                continue;
            }
            int documentFrequency = documentFrequencies.getOrDefault(term, 0);
            double idf = Math.log(1 + ((documentCount - documentFrequency + 0.5) / (documentFrequency + 0.5)));
            double normalizedLength = 1 - B + B * (documentLength / Math.max(1.0, averageDocumentLength));
            total += idf * ((termFrequency * (K1 + 1)) / (termFrequency + K1 * normalizedLength));
        }

        return total;
    }

    private String excerpt(String content, int maxWords) {
        String[] words = content.split("\\s+");
        int limit = Math.min(words.length, maxWords);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(words[i]);
        }
        return builder.toString().trim();
    }

    private record ScoredDocument(SopDocument document, double score) {
    }
}
