package com.messi.system.product.service.impl;

import com.messi.system.constant.DistributedLockConstants;
import com.messi.system.core.ResResult;
import com.messi.system.lock.DistributedLock;
import com.messi.system.product.converter.ProductConverter;
import com.messi.system.product.dao.SkuDAO;
import com.messi.system.product.dao.SkuStockDAO;
import com.messi.system.product.domain.dto.DeductionStockProductDTO;
import com.messi.system.product.domain.dto.SkuDTO;
import com.messi.system.product.domain.entity.ProductAndSkuDO;
import com.messi.system.product.domain.entity.SkuDO;
import com.messi.system.product.domain.entity.SkuStockDO;
import com.messi.system.product.domain.request.ProductReq;
import com.messi.system.product.domain.request.CreateProductReq;
import com.messi.system.product.mapper.SkuStockMapper;
import com.messi.system.product.service.ProductService;
import com.messi.system.utils.CheckParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 商品service实现类
 */
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private SkuDAO skuDAO;

    @Autowired
    private SkuStockDAO skuStockDAO;

    @Autowired
    private ProductConverter productConverter;

    @Autowired
    private DistributedLock distributedLock;

    @Override
    @Transactional
    public void createProduct(CreateProductReq createProductReq) {
        log.info("执行createProduct,createProductReq:{}", createProductReq.toString());

        //  1、检查参数
        checkPeq(createProductReq);

        //  2、分布式锁
        String redisKey = distributedLock(createProductReq.getProductId());

        try {
            //  3、、幂等验证
            checkRepeatCreate(createProductReq);

            //  4、创建数据对象
            ProductAndSkuDO productAndSkuDO = buildProduct(createProductReq);

            //  5、保存数据库
            save(productAndSkuDO);

            //  6、完成返回
            log.info("完成createProduct,createProductReq:{}", createProductReq);

        } finally {
            distributedLock.unLock(redisKey);
        }
    }

    private String distributedLock(String productId) {
        String redisKey = DistributedLockConstants.CREATE_PRODUCT_KEY + productId;
        if (!distributedLock.tryLock(redisKey)) {
            log.warn("执行createProduct加锁失败");
            throw new RuntimeException("执行createProduct加锁失败");
        }
        return redisKey;
    }

    @Override
    public SkuDTO getSku(String productId, String skuId) {
        SkuDO sku = skuDAO.getSku(productId, skuId);
        return productConverter.skuDTO2SkuDO(sku);
    }

    private void save(ProductAndSkuDO productAndSkuDO) {

        saveProduct(productAndSkuDO.getSkuDO());

        saveSku(productAndSkuDO.getSkuStockDO());
    }

    private void saveSku(SkuStockDO skuStockDO) {
        skuStockDAO.save(skuStockDO);
    }

    private void saveProduct(SkuDO skuDO) {
        skuDAO.save(skuDO);
    }

    /**
     * 创建数据对象
     *
     * @param createProductReq 创建商品入参
     */
    private ProductAndSkuDO buildProduct(CreateProductReq createProductReq) {
        ProductAndSkuDO productAndSkuDO = new ProductAndSkuDO();

        SkuDO skuDO = buildProductDO(createProductReq);
        productAndSkuDO.setSkuDO(skuDO);

        SkuStockDO skuStockDO = buildSkuDO(createProductReq);
        productAndSkuDO.setSkuStockDO(skuStockDO);

        return productAndSkuDO;
    }

    private SkuStockDO buildSkuDO(CreateProductReq createProductReq) {
        CreateProductReq.SkuReq skuReq = createProductReq.getSkuReq();
        return SkuStockDO.builder().skuId(skuReq.getSkuId()).totalStock(skuReq.getTotalStock()).saledStock(skuReq.getSaledStock()).build();
    }

    private SkuDO buildProductDO(CreateProductReq createProductReq) {
        String productId = createProductReq.getProductId();
        String skuName = createProductReq.getSkuName();
        Integer skuPrice = createProductReq.getSkuPrice();
        String skuId = createProductReq.getSkuReq().getSkuId();

        SkuDO skuDO = new SkuDO();
        skuDO.setProductId(productId);
        skuDO.setSkuName(skuName);
        skuDO.setSkuPrice(skuPrice);
        skuDO.setSkuId(skuId);

        return skuDO;
    }

    /**
     * 接口幂等校验，防止重复创建数据
     *
     * @param createProductReq 创建商品入参
     */
    private void checkRepeatCreate(CreateProductReq createProductReq) {
        String productId = createProductReq.getProductId();
        CreateProductReq.SkuReq skuReq = createProductReq.getSkuReq();

        SkuDO daoSkuDO = skuDAO.getSku(productId, skuReq.getSkuId());

        if (daoSkuDO != null) {
            log.error("重复添加商品,productId:{},skuId:{}", productId, skuReq.getSkuId());
            throw new RuntimeException("重复添加商品");
        }
    }

    /**
     * 检查参数
     */
    private void checkPeq(CreateProductReq createProductReq) {

        CheckParamUtil.checkStringNotEmpty(createProductReq.getProductId(), "商品id不能是空");

        CheckParamUtil.checkStringNotEmpty(createProductReq.getSkuName(), "sku名称不能是空");

        CheckParamUtil.checkParamNotEmpty(createProductReq.getSkuPrice(), "sku单价不能是空");

        CheckParamUtil.checkParamNotEmpty(createProductReq.getSkuReq().getTotalStock(), "总库存不能是空");

        CheckParamUtil.checkParamNotEmpty(createProductReq.getSkuReq().getSaledStock(), "已售库存不能是空");

        CheckParamUtil.checkParamNotEmpty(createProductReq.getSkuReq().getLockedStock(), "已锁定库存不能是空");
    }

    /**
     * 检查商品
     *
     * @param productReqList 检查商品list
     * @return 检查结果
     */
    @Override
    public Boolean checkProduct(List<ProductReq> productReqList) {
        for (ProductReq productReq : productReqList) {
            String productId = productReq.getProductId();
            String skuId = productReq.getSkuId();

            SkuDO sku = skuDAO.getSku(productId, skuId);
            if (sku == null) {
                throw new RuntimeException("商品信息不存在");
            }
        }

        return true;
    }

    /**
     * 扣除sku库存
     *
     * @param productReqList productReqList
     */
    @Override
    public void deductionStock(List<ProductReq> productReqList) {
        List<SkuStockDO> skuStockList = new LinkedList<>();

        for (ProductReq productReq : productReqList) {
            SkuStockDO skuStockDB = skuStockDAO.getSkuStock(productReq.getSkuId());

            Integer saleNum = productReq.getSaleNum();
            skuStockDB.setTotalStock(skuStockDB.getTotalStock() - saleNum);
            skuStockDB.setSaledStock(skuStockDB.getSaledStock() + saleNum);

            skuStockList.add(skuStockDB);
        }

        skuStockDAO.updateBatchById(skuStockList);
        log.info("扣减库存执行成功");
    }

    /**
     * 单独扣减sku库存
     *
     * @param deductionStockProductDTO deductionStockProductDTO
     */
    @Override
    public void aloneDeductionStock(DeductionStockProductDTO deductionStockProductDTO) {
        SkuStockDO skuStockDB = skuStockDAO.getSkuStock(deductionStockProductDTO.getSkuId());

        Integer saleNum = deductionStockProductDTO.getSaleNum();
        skuStockDB.setTotalStock(skuStockDB.getTotalStock() - saleNum);
        skuStockDB.setSaledStock(skuStockDB.getSaledStock() + saleNum);

        skuStockDAO.updateById(skuStockDB);
        log.info("单独扣减库存执行成功,skuId:{}", skuStockDB.getSkuId());
    }

    /**
     * 提供给事务消息使用的，补偿库存
     *
     * @param productReqList productReqList
     */
    @Override
    public Boolean compensationStock(List<ProductReq> productReqList) {
        List<SkuStockDO> skuStockList = new LinkedList<>();

        for (ProductReq productReq : productReqList) {
            SkuStockDO skuStockDB = skuStockDAO.getSkuStock(productReq.getSkuId());

            Integer saleNum = productReq.getSaleNum();
            skuStockDB.setTotalStock(skuStockDB.getTotalStock() + saleNum);
            skuStockDB.setSaledStock(skuStockDB.getSaledStock() - saleNum);

            skuStockList.add(skuStockDB);
        }

        skuStockDAO.updateBatchById(skuStockList);
        log.info("补偿库存执行成功");

        return true;
    }
}
