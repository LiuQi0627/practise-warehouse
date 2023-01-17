package com.messi.system;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

/**
 * 代码自动生成器
 */
public class CodeGenerator {

    public static void main(String[] args) {
        //  代码自动生成器 对象
        AutoGenerator autoGenerator = new AutoGenerator();
        String quote = "/messi-generator";

        //  1、全局配置
        GlobalConfig gc = new GlobalConfig();
        String oPath = System.getProperty("user.dir");  // 得到当前项目的路径
        gc.setOutputDir(oPath + quote + "/src/main/java");   // 生成文件输出根目录
        gc.setIdType(IdType.ASSIGN_ID);
        gc.setOpen(false);  // 生成完成后不弹出文件框
        gc.setFileOverride(true);  // 文件覆盖
        gc.setEnableCache(false);
        gc.setMapperName("%s" + "Mapper");
        gc.setAuthor(null);
        autoGenerator.setGlobalConfig(gc);

        //  2、数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setDbType(DbType.MYSQL);   //设置数据库类型
        dsc.setUrl("jdbc:mysql://localhost:3306/messi_market_system?useSSL=false&useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8");  //指定数据库
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("123456");
        autoGenerator.setDataSource(dsc);

        //  3、包的配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.messi.system");
        pc.setModuleName("generator");
        pc.setEntity("domain");
        pc.setMapper("mapper");
        autoGenerator.setPackageInfo(pc);
        autoGenerator.setCfg(null);

        // 4、策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setInclude("coupon_info"); // 设置要映射的表名
//        strategy.setInclude("order_info", "order_item_info","order_status_record"); // 多张表用“,”分隔
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityLombokModel(true);    // 自动lombok
        strategy.setControllerMappingHyphenStyle(true);
        autoGenerator.setStrategy(strategy);

        //  5、自定义不生成controller和service模板
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setController(null);
        templateConfig.setService(null);
        templateConfig.setServiceImpl(null);
        autoGenerator.setTemplate(templateConfig);

        //  6、自定义配置项
        InjectionConfig cfg = new InjectionConfig() {

            @Override
            public void initMap() {

            }

            @Override
            public void initTableMap(TableInfo tableInfo) {
                super.initTableMap(tableInfo);
                for (TableField field : tableInfo.getFields()) {
                    field.setConvert(true);
                    if (field.getName().equals("create_time")) {
                        field.setFill(FieldFill.INSERT.name());
                    } else if (field.getName().equals("modified_time")) {
                        field.setFill(FieldFill.INSERT_UPDATE.name());
                    } else {
                        field.setFill(FieldFill.DEFAULT.name());
                    }
                }
            }
        };
        autoGenerator.setCfg(cfg);

        //  7、执行生成
        autoGenerator.execute();
    }

}
