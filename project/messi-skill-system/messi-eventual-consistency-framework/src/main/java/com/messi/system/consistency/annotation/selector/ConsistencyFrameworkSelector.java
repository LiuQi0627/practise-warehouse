package com.messi.system.consistency.annotation.selector;

import com.messi.system.consistency.annotation.config.ComponentScanConfig;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 导入最终一致性框架注解的选择器
 */
public class ConsistencyFrameworkSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        //  把自定义扫描器的实体提供给选择器
        return new String[]{ComponentScanConfig.class.getName()};
    }
}
