package com.messi.system.data.migration.task;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.messi.system.data.migration.binlog.BinlogConsumer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 增量迁移数据的监听binlog工作任务线程
 */
@Slf4j
public class IncrementalConsumeBinlogTask implements Runnable {

    /**
     * canal操作客户端
     */
    private final CanalConnector canalConnector;

    private final BinlogConsumer binlogConsumer;

    private final boolean running;

    //  构造器注入
    public IncrementalConsumeBinlogTask(CanalConnector canalConnector, BinlogConsumer binlogConsumer) {
        this.running = true;
        this.canalConnector = canalConnector;
        this.binlogConsumer = binlogConsumer;
    }

    @Override
    public void run() {
        //  订阅数据库
        canalConnector.subscribe("messi_order_system.order_info,messi_order_system.order_item_info," +
                "messi_order_system.order_price_details");

        while (running) {
            try {
                //  每次拉取1000条数据
                Message message = canalConnector.get(1000);

                //  获取数据集合
                List<CanalEntry.Entry> entries = message.getEntries();

                //  如果数据集为空,线程休眠1000ms后再次执行拉取数据操作
                if (entries.size() <= 0) {
                    Thread.sleep(1000);
                } else {
                    //  遍历解析拉取到的数据
                    for (CanalEntry.Entry entry : entries) {
                        log.info("entry:{}",entry);
                        //  获取数据表名
                        String tableName = entry.getHeader().getTableName();

                        //  获取数据类型
                        CanalEntry.EntryType entryType = entry.getEntryType();

                        //  判断数据类型,只监听处理行数据
                        //  ROW是真实地行数据记录
                        if (!CanalEntry.EntryType.ROWDATA.equals(entryType)) {
                            continue;
                        }

                        //  反序列化二进制数据
                        ByteString storeValue = entry.getStoreValue();
                        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(storeValue);

                        //  获取数据执行的事件类型
                        CanalEntry.EventType eventType = rowChange.getEventType();

                        //  获取数据集
                        List<CanalEntry.RowData> rowDataList = rowChange.getRowDatasList();

                        //  处理数据
                        for (CanalEntry.RowData rowData : rowDataList) {
                            //  旧数据
                            JSONObject beforeData = new JSONObject();
                            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
                            for (CanalEntry.Column column : beforeColumnsList) {
                                //  字段名，字段值
                                beforeData.put(column.getName(), column.getValue());
                            }

                            JSONObject afterData = new JSONObject();
                            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
                            for (CanalEntry.Column column : afterColumnsList) {
                                //  字段名，字段值
                                afterData.put(column.getName(), column.getValue());
                            }

                            log.info("当前解析出来待消费binlog的数据,数据表:{},事件类型:{},新增前修改前的数据:{},新增后修改后的数据:{}",
                                    tableName, eventType, beforeData, afterData);

                            //  做binlog监听处理
                            binlogConsumer.consumer(tableName, eventType, beforeData, afterData);
                        }

                    }
                }
            } catch (InterruptedException | InvalidProtocolBufferException e) {
                log.error("IncrementalConsumeBinlogTask执行失败：{}", e.getLocalizedMessage());
            }
        }
    }
}
