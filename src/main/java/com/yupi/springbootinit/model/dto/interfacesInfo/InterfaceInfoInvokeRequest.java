package com.yupi.springbootinit.model.dto.interfacesInfo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户请求参数
     */
    private String userRequestParams;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}