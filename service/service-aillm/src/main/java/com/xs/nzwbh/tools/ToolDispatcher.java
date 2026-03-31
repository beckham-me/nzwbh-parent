package com.xs.nzwbh.tools;

import org.springframework.stereotype.Component;

/**
 * ToolDispatcher：统一工具调度器（已优化）
 *
 * ✔ 不再做图片识别逻辑判断
 * ✔ 不再调用 YOLO（detectTool 仅用于独立工具调用）
 * ✔ pesticide_recommend 只接收 pestName
 */
@Component
public class ToolDispatcher {

    private final PestDetectTool detectTool;      // 图片识别工具
    private final PesticideTool pesticideTool;    // 农药推荐工具

    /**
     * 构造函数注入
     */
    public ToolDispatcher(PestDetectTool detectTool,
                          PesticideTool pesticideTool) {
        this.detectTool = detectTool;
        this.pesticideTool = pesticideTool;
    }

    /**
     * 工具调度入口
     *
     * @param tool  工具名称
     *              pest_detect           → 图片识别
     *              pesticide_recommend   → 农药推荐
     *
     * @param input 输入参数：
     *              pest_detect → 图片URL
     *              pesticide_recommend → 虫害名称
     *
     * @return 工具执行结果
     */
    public String dispatch(String tool, String input) {


        if (tool == null || tool.isEmpty()) {
            return "工具不能为空";
        }

        if (input == null || input.isEmpty()) {
            return "输入参数不能为空";
        }

        // 工具分发
        switch (tool) {

            // 图片识别工具（YOLO）
            case "pest_detect":

                // 调用YOLO识别
                return detectTool.detect(input);


            // 农药推荐工具（RAG）
            case "pesticide_recommend":


                // 如果误传URL，直接拦截（防止隐式错误）
                if (input.startsWith("http")) {           // 判断是否是URL
                    return "pesticide_recommend 不支持图片URL，请先完成虫害识别";
                }

                // 调用RAG推荐（纯文本）
                return pesticideTool.recommend(input);


            default:
                return "未知工具：" + tool;
        }
    }
}