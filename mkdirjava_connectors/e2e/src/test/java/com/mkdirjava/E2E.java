package com.mkdirjava;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableId;

public class E2E {
    private static BigQuery bigQuery;

    private String dataset = "dataset1";
    private String source_table = "table_b";
    private String sink_table = "table_a";

    record TestDetails(
            Integer id,
            String nameInput,
            String expectedOutput) {
    }
    @BeforeAll
    public static void setup() {
        var bqHost = System.getenv().getOrDefault("BIG_QUERY_PROTOCOL_AND_HOST", "http://0.0.0.0:9050");
        bigQuery=BigQueryOptions.newBuilder()
                .setProjectId("test")
                .setCredentials(UserCredentials.create(AccessToken.newBuilder().build()))
                .setHost(bqHost)
                // "http://bigquery:9050"
                .build().getService();
    }

    @ParameterizedTest
    @MethodSource("provideE2ETestInput")
    public void endToendTest(TestDetails details) throws JobException, InterruptedException {
        insertIntoBigQuery(details);
        verifyResultPresent(details,6);
    }

    private void insertIntoBigQuery(TestDetails details) {
        Map<String, Object> data = Map.of(
            "id", details.id,
            "name", details.nameInput,
            "consumed", false
        );
        var row = InsertAllRequest.RowToInsert.of(Integer.toString(details.id), data);
        bigQuery.insertAll(InsertAllRequest.newBuilder(TableId.of(dataset, source_table)).addRow(row).build());
    }

    private void verifyResultPresent(TestDetails details,int attempts) throws JobException, InterruptedException {
        
        if (attempts == 0) {
            throw new RuntimeException("Data not found in BigQuery");
        }
        String query = String.format("SELECT * FROM %s.%s", dataset, sink_table);
        var results = bigQuery.query(QueryJobConfiguration.of(query));
        var isPresent = results.streamAll().filter(
            row -> row.get("id").getStringValue().equals(details.id.toString()) && row.get("name").getStringValue().equals(details.expectedOutput)).findFirst().isPresent();
        if (!isPresent) {
            Thread.sleep(6000);
            verifyResultPresent(details, attempts - 1);
        }
    }

    private static Stream<TestDetails> provideE2ETestInput() {
        return Stream.of(
                new TestDetails(1,"abcdefgh", "REDACTED, origional ABCDEFGH"),
                new TestDetails(2,"abc", "abc"),
                new TestDetails(3,"abcdefg", "ABCDEFG"));
    }

}