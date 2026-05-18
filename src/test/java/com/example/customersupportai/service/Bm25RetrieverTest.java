package com.example.customersupportai.service;

import com.example.customersupportai.model.Department;
import com.example.customersupportai.model.RetrievedSop;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Bm25RetrieverTest {

    @Test
    void retrievesBillingSopForDuplicateChargeMessage() throws IOException {
        SopRepository repository = new SopRepository(
                new PathMatchingResourcePatternResolver(),
                "classpath:sops/sop_*.txt"
        );
        repository.loadDocuments();

        Bm25Retriever retriever = new Bm25Retriever(repository, 4);
        retriever.buildIndex();

        List<RetrievedSop> results = retriever.retrieve(
                "I was charged twice for my recent order",
                Department.BILLING,
                4
        );

        assertThat(results).isNotEmpty();
        assertThat(results)
                .extracting(RetrievedSop::sopId)
                .contains("SOP_003_BILLING_DISPUTES");
    }
}
