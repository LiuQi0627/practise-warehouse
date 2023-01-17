package com.messi.system.order.elasticsearch.client;

import com.alibaba.fastjson.JSONObject;
import com.messi.system.order.constants.OrderElasticsearchConstants;
import com.messi.system.order.domain.dto.OrderDetailDTO;
import com.messi.system.order.elasticsearch.annotation.EsDocument;
import com.messi.system.order.elasticsearch.annotation.EsField;
import com.messi.system.order.elasticsearch.constants.EsDataTypeConstants;
import com.messi.system.order.elasticsearch.enums.EsIndexEnums;
import com.messi.system.order.elasticsearch.index.OrderEsIndex;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;

import static com.messi.system.order.constants.OrderElasticsearchConstants.*;

/**
 * es client配置
 */
@Slf4j
@Component
public class ElasticsearchClientService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${elasticsearch.shards-number}")
    private Integer shardsNumber;

    @Value("${elasticsearch.replicas-number}")
    private Integer replicasNumber;

    @Value("${elasticsearch.request-max-return-number}")
    private Integer requestMaxReturnNumber;

    private static final long SEARCH_TOTAL_HITS = 0L;

    /**
     * 创建es的索引
     */
    public void createIndexIfNotExists(String indexName) {
        //  如果存在当前索引
        boolean indexExists = indexExists(indexName);
        if (indexExists) {
            //  流程结束
            log.info("es索引:{}已存在", OrderElasticsearchConstants.ORDER_ES_INDEX);
            return;
        }

        //  创建索引
        try {
            if (createIndex(indexName)) {
                log.info("es索引:{}创建完成", OrderElasticsearchConstants.ORDER_ES_INDEX);
            }
        } catch (IOException e) {
            log.error("es索引:{}创建失败,失败原因:{}", OrderElasticsearchConstants.ORDER_ES_INDEX, e);
        }
    }

    /**
     * 创建es索引
     *
     * @param indexName 索引名称
     */
    private Boolean createIndex(String indexName) throws IOException {
        //  1、设置基本参数
        Settings.Builder settings = Settings.builder()
                //  分片数
                .put(INDEX_NUMBER_OF_SHARDS, shardsNumber)
                //  副本数
                .put(INDEX_NUMBER_OF_REPLICAS, replicasNumber)
                //  查询最大返回数
                .put(INDEX_MAX_RESULT_WINDOW, requestMaxReturnNumber);

        //  2、加入配置
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        createIndexRequest.settings(settings);
        XContentBuilder xContentBuilder = createXContentBuilder(OrderEsIndex.class);
        createIndexRequest.mapping(xContentBuilder);

        //  3、做创建
        CreateIndexResponse indexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        //  被确认返回响应
        boolean acknowledged = indexResponse.isAcknowledged();
        //  已启动分片
        boolean shardsAcknowledged = indexResponse.isShardsAcknowledged();
        return acknowledged || shardsAcknowledged;
    }

    /**
     * 创建es文档内容构造器
     */
    private XContentBuilder createXContentBuilder(Class<?> clazz) throws IOException {
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();
        xContentBuilder.startObject();
        xContentBuilder.startObject(INDEX_MAPPING_PROPERTIES);
        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            //  只处理使用@EsFeild注解标注过的字段
            if (declaredField.isAnnotationPresent(EsField.class)) {
                //  获取自定义注解
                EsField esField = declaredField.getDeclaredAnnotation(EsField.class);
                if (esField.type().equals(EsDataTypeConstants.OBJECT)) {
                    //  处理es复杂的object数据类型
                    xContentBuilder.startObject(declaredField.getName());
                    xContentBuilder.startObject(INDEX_MAPPING_PROPERTIES);

                    workWithObjectTypes(xContentBuilder, declaredField);

                    xContentBuilder.endObject();
                    xContentBuilder.endObject();
                } else {
                    //  基本es数据类型
                    xContentBuilder.startObject(declaredField.getName());

                    workWithBaseTypes(xContentBuilder, esField);

                    xContentBuilder.endObject();
                }
            }
        }
        xContentBuilder.endObject();
        xContentBuilder.endObject();
        return xContentBuilder;
    }

    private void workWithObjectTypes(XContentBuilder xContentBuilder, Field declaredField) throws IOException {
        for (Field field : declaredField.getType().getDeclaredFields()) {
            EsField declaredAnnotation = field.getDeclaredAnnotation(EsField.class);
            xContentBuilder.startObject(field.getName());

            xContentBuilder.field(INDEX_MAPPING_TYPE, declaredAnnotation.type());
            if (declaredAnnotation.type().equals(EsDataTypeConstants.DATE)) {
                xContentBuilder.field(INDEX_MAPPING_FORMAT, DATE_FORMAT);
            }

            xContentBuilder.endObject();
        }
    }

    private void workWithBaseTypes(XContentBuilder xContentBuilder, EsField esField) throws IOException {
        xContentBuilder.field(INDEX_MAPPING_TYPE, esField.type());
        if (esField.type().equals(EsDataTypeConstants.DATE)) {
            xContentBuilder.field(INDEX_MAPPING_FORMAT, DATE_FORMAT);
        }
    }

    /**
     * es索引已存在
     *
     * @param indexName es索引名称
     * @return 是否存在索引
     */
    private boolean indexExists(String indexName) {
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
            getIndexRequest.humanReadable(true);
            return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 执行搜索
     *
     * @param indexName           索引名称
     * @param searchSourceBuilder 搜索构造器
     * @return 搜索结果
     */
    @SneakyThrows
    public OrderDetailDTO search(EsIndexEnums indexName, SearchSourceBuilder searchSourceBuilder) {
        SearchHits searchHits = searchHits(indexName, searchSourceBuilder);
        if (searchHits.getTotalHits().value == SEARCH_TOTAL_HITS) {
            return null;
        }
        SearchHit hit = searchHits.getHits()[0];
        String sourceAsString = hit.getSourceAsString();
        return JSONObject.parseObject(sourceAsString, OrderDetailDTO.class);
    }

    private SearchHits searchHits(EsIndexEnums indexName, SearchSourceBuilder searchSourceBuilder) throws IOException {
        //  构造分页查询参数,查询单独数据时可以不用
        SearchRequest searchRequest = new SearchRequest(indexName.getIndexName());
        searchRequest.source(searchSourceBuilder);
        if (searchSourceBuilder.from() == -1) {
            searchSourceBuilder.size(requestMaxReturnNumber);
        }

        //  查询es
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //  返回查询命中hits
        return searchResponse.getHits();
    }

    /**
     * 推送订单数据到es
     */
    public void sendOrderToEs(Class<?> clazz, OrderDetailDTO orderDetailDTO) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        EsDocument esDocument = clazz.getDeclaredAnnotation(EsDocument.class);
        //  获取标注的索引名称
        String indexName = esDocument.index().getIndexName();

        //  数据转换成JSON
        IndexRequest indexRequest = new IndexRequest(indexName);
        String jsonString = JSONObject.toJSONString(orderDetailDTO);
        indexRequest.source(jsonString, XContentType.JSON);

        //  保存到es
        bulkRequest.add(indexRequest);
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        //  检查上传结果
        for (BulkItemResponse bulkItemResponse : bulkResponse) {
            if (bulkItemResponse.isFailed()) {
                log.error("es数据上传失败,失败原因：{}", bulkItemResponse.getFailureMessage());
            }
        }

        log.info("数据上报到ES成功,orderId:{}", orderDetailDTO.getOrderId());
    }

    @SneakyThrows
    public void deleteIndex(String indexName) {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
        deleteIndexRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
        restHighLevelClient.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
    }
}
