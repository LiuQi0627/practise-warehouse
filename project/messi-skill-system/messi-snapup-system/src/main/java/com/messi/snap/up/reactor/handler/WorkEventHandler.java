package com.messi.snap.up.reactor.handler;

import com.lmax.disruptor.WorkHandler;
import com.messi.snap.up.reactor.event.WorkEvent;
import com.messi.snap.up.reactor.listener.EventListener;


/**
 * 自定义工作总线事件执行器
 */
public class WorkEventHandler implements WorkHandler<WorkEvent> {

    @Override
    public void onEvent(WorkEvent workEvent) throws Exception {
        try {
            //  执行事件，遍历所有的自定义监听器做回调执行
            for (EventListener listener : workEvent.eventListeners) {
                //  执行事件
                listener.onEvent(workEvent.event, workEvent.context);
            }
        } finally {
            workEvent.clear();
        }
    }
}
