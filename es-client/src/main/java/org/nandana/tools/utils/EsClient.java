package org.nandana.tools.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;


public class EsClient {

    private Client client;

    private String clusterName;

    private String ipAddress;

    private int port;

    private static final String CLUSTER_NAME = "cluster.name";

    private static final String AGGREGATION = "aggregations";

    public static final String COMPLETION = "completion";

    private static final int MAX_RESULTS = 1000;

    public EsClient(String clusterName, String ipAddress, int port) {

        this.clusterName = clusterName;
        this.ipAddress = ipAddress;
        this.port = port;

        Settings settings = ImmutableSettings.settingsBuilder()
                .put(CLUSTER_NAME, clusterName).build();

        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(ipAddress, port));
    }

    public SearchHits searchMatch(String index, Set<String> types, Map<String, String> termMatches) {


        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        for (String key : termMatches.keySet()) {
            String value = termMatches.get(key);

            if (!Strings.isNullOrEmpty(value)) {
                queryBuilder.must(QueryBuilders.termQuery(key, value));
            }

        }

        SearchResponse response = client.prepareSearch(index)
                .setTypes(Joiner.on(",").join(types))
                .setSearchType(SearchType.DEFAULT)
                .setQuery(queryBuilder)
                .setFrom(0).setSize(MAX_RESULTS).setExplain(true)
                .execute()
                .actionGet();

        return response.getHits();

    }

    public SearchHits searchMatch(String index, Set<String> types, String key, String value) {

        SearchResponse response = client.prepareSearch(index)
                .setTypes(Joiner.on(",").join(types))
                .setSearchType(SearchType.DEFAULT)

                .setQuery(QueryBuilders.matchQuery(key, value))
                .setFrom(0).setSize(MAX_RESULTS).setExplain(true)
                .execute()
                .actionGet();

        return response.getHits();

    }


    public long getCount(String index, String type) {

        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .setSearchType(SearchType.COUNT)
                .setFrom(0).setSize(MAX_RESULTS).setExplain(true)
                .execute()
                .actionGet();

        return response.getHits().getTotalHits();

    }

    public SearchHits search(String index, Set<String> types, String field, int size, SortOrder order) {

        SearchResponse response = client.prepareSearch(index)
                .setTypes(Joiner.on(",").join(types))
                .setSearchType(SearchType.DEFAULT)
                .addSort(SortBuilders.fieldSort(field).order(order))
                .setFrom(0).setSize(size).setExplain(true)
                .execute()
                .actionGet();

        return response.getHits();

    }

    public SuggestResponse suggest(String index, String field, String value, int size) {


        CompletionSuggestionBuilder compBuilder = new CompletionSuggestionBuilder(COMPLETION);
        compBuilder.text(value);
        compBuilder.field(field);
        compBuilder.size(size);

        SuggestRequestBuilder suggestRequestBuilder = client.prepareSuggest(index)
                .addSuggestion(compBuilder);

        SuggestResponse suggestResponse = suggestRequestBuilder.execute().actionGet();

        return suggestResponse;

    }

    public List<Terms.Bucket> bucketTermAggregate(String index, Set<String> types, String field) {

        try {
            SearchResponse response = client.prepareSearch(index)
                    .setTypes(Joiner.on(",").join(types))
                    .setQuery(matchAllQuery())
                    .addAggregation(terms(AGGREGATION).field(field).size(MAX_RESULTS))
                    .setSearchType(SearchType.COUNT)
                    .execute()
                    .actionGet();

            Terms terms = response.getAggregations().get(AGGREGATION);
            return terms.getBuckets();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
