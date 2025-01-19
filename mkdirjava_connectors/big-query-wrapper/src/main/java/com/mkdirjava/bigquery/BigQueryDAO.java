package com.mkdirjava.bigquery;

import java.util.List;
import java.util.Map;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableResult;

public class BigQueryDAO {

    private BigQuery bigQuery;

    public BigQueryDAO(String projectId, String endpoint) {
        bigQuery = BigQueryOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(UserCredentials.create(AccessToken.newBuilder().build()))
                .setHost(endpoint)
                .build().getService();
    }

    public void insertData(String dataset, String table, String offset, Map<String, Object> data)
            throws JobException, InterruptedException {
        var row = InsertAllRequest.RowToInsert.of(offset, data);
        bigQuery.insertAll(InsertAllRequest.newBuilder(TableId.of(dataset, table)).addRow(row).build());
    }

    public TableResult getNewData(String dataset, String table) throws JobException, InterruptedException {
        String query = String.format("SELECT * FROM %s.%s WHERE consumed = false", dataset, table);
        var results = bigQuery.query(QueryJobConfiguration.of(query));
        return results;
    }

    public void setConsumed(String dataset, String table, List<String> ids) throws JobException, InterruptedException {
        if (!ids.isEmpty()) {
            String query = String.format("UPDATE %s.%s set consumed = true WHERE id in (%s)", dataset, table,
                    String.join(",", ids));
            bigQuery.query(QueryJobConfiguration.of(query));
        }

    }
}
