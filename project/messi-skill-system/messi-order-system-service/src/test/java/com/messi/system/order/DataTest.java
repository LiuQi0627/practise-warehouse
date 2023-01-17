package com.messi.system.order;

import com.messi.system.order.domain.dto.CheckOrderPriceDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class DataTest {

    /**
     * 比较2个map里的值是否相等，用来比较订单价格
     */
    public static void main(String[] args) {
        CheckOrderPriceDTO checkOrderPriceDTO1 = new CheckOrderPriceDTO();
        checkOrderPriceDTO1.setSkuId("1");
        checkOrderPriceDTO1.setCalculatePrice(100);

        CheckOrderPriceDTO checkOrderPriceDTO2 = new CheckOrderPriceDTO();
        checkOrderPriceDTO2.setSkuId("2");
        checkOrderPriceDTO2.setCalculatePrice(200);

        List<CheckOrderPriceDTO> list1 = new ArrayList<>();
        list1.add(checkOrderPriceDTO1);
        list1.add(checkOrderPriceDTO2);


        CheckOrderPriceDTO checkOrderPriceDTO3 = new CheckOrderPriceDTO();
        checkOrderPriceDTO3.setSkuId("1");
        checkOrderPriceDTO3.setCalculatePrice(300);

        CheckOrderPriceDTO checkOrderPriceDTO4 = new CheckOrderPriceDTO();
        checkOrderPriceDTO4.setSkuId("2");
        checkOrderPriceDTO4.setCalculatePrice(400);

        List<CheckOrderPriceDTO> list2 = new ArrayList<>();
        list2.add(checkOrderPriceDTO3);
        list2.add(checkOrderPriceDTO4);

        Map<String, Integer> frontPriceMap1 = list1.stream().collect(
                Collectors.toMap(
                        CheckOrderPriceDTO::getSkuId,
                        CheckOrderPriceDTO::getCalculatePrice)
        );
        Map<String, Integer> frontPriceMap2 = list2.stream().collect(
                Collectors.toMap(
                        CheckOrderPriceDTO::getSkuId,
                        CheckOrderPriceDTO::getCalculatePrice)
        );

        for (Map.Entry<String, Integer> entry1 : frontPriceMap1.entrySet()) {
            Integer value1 = entry1.getValue();
            Integer value2 = frontPriceMap2.get(entry1.getKey());
            if (value1.equals(value2)) {
                System.out.println(true);
            }
        }
    }
}
