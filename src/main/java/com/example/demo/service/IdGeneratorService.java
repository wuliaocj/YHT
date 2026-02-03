package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.domain.ProductSpecPrice;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * ID生成服务
 * 用于按照规则生成产品规格加价表的ID
 */
@Service
public class IdGeneratorService {

    @Resource
    private ProductSpecPriceService productSpecPriceService;

    /**
     * 生成产品规格ID
     * 规则：产品ID + 规格类型编码 + 序号
     * 例如：产品ID=6，杯型编码=1，序号=01 → 6101
     * @param productId 产品ID
     * @param specType 规格类型
     * @return 生成的规格ID
     */
    public Long generateSpecId(Long productId, String specType) {
        // 1. 确定规格类型编码
        int typeCode = getTypeCode(specType);
        
        // 2. 查询该产品该类型的最大序号
        int maxSeq = getMaxSeq(productId, specType);
        
        // 3. 生成新的序号（加1）
        int newSeq = maxSeq + 1;
        
        // 4. 拼接ID：产品ID + 类型编码 + 两位序号
        String idStr = productId.toString() + typeCode + String.format("%02d", newSeq);
        
        return Long.parseLong(idStr);
    }

    /**
     * 获取规格类型编码
     * @param specType 规格类型
     * @return 类型编码
     */
    private int getTypeCode(String specType) {
        switch (specType) {
            case "cup_type":
                return 1;
            case "taste":
                return 2;
            case "temperature":
                return 3;
            case "topping":
                return 4;
            default:
                throw new IllegalArgumentException("未知的规格类型：" + specType);
        }
    }

    /**
     * 获取该产品该类型的最大序号
     * @param productId 产品ID
     * @param specType 规格类型
     * @return 最大序号
     */
    private int getMaxSeq(Long productId, String specType) {
        // 1. 查询该产品该类型的所有规格
        LambdaQueryWrapper<ProductSpecPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSpecPrice::getProductId, productId)
                .eq(ProductSpecPrice::getSpecType, specType);
        List<ProductSpecPrice> specList = productSpecPriceService.list(wrapper);
        
        // 2. 计算最大序号
        int maxSeq = 0;
        String productIdStr = productId.toString();
        int typeCode = getTypeCode(specType);
        String prefix = productIdStr + typeCode;
        
        for (ProductSpecPrice spec : specList) {
            String idStr = spec.getId().toString();
            if (idStr.startsWith(prefix) && idStr.length() == prefix.length() + 2) {
                try {
                    int seq = Integer.parseInt(idStr.substring(prefix.length()));
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的ID
                }
            }
        }
        
        return maxSeq;
    }
}
