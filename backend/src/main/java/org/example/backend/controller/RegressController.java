package org.example.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

@RestController
public class RegressController {
    @PostMapping("/regress/")
    public Map<String, Object> uploadFiles(
            @RequestParam("inputFile") MultipartFile inputFile,
            @RequestParam("outputFile") MultipartFile outputFile,
            @RequestParam("predictFile") MultipartFile predictFile,
            @RequestParam("model") String model) {

        Map<String, Object> response = new HashMap<>();
        System.out.println("选择的回归模型: " + model);

        try {
            // 读取输入和输出文件
            List<Double> X = readFile(inputFile);
            List<Double> Y = readFile(outputFile);
            System.out.println("Input: " + X);
            System.out.println("Output: " + Y);

            if (X.size() != Y.size()) {
                response.put("error", "输入文件和输出文件行数不匹配！");
                return response;
            }

            // 计算回归系数
            double[] coefficients = linearRegression(X, Y);
            double a = coefficients[0], b = coefficients[1];
            System.out.println("回归系数: a = " + a + ", b = " + b);

            // 读取预测数据
            List<Double> X_predict = readFile(predictFile);
            List<Double> Y_predict = new ArrayList<>();

            // 计算预测值
            for (double x : X_predict) {
                Y_predict.add(a * x + b);
            }
            System.out.println("Predict: " + Y_predict);

            // 返回结果
            response.put("message", "预测成功");
            response.put("model", model);
//            response.put("coefficients", Map.of("a", a, "b", b));
            response.put("predictions", Y_predict);

        } catch (Exception e) {
            response.put("error", "文件处理失败: " + e.getMessage());
        }

        return response;
    }

    // 读取文件内容，返回 double 列表
    private List<Double> readFile(MultipartFile file) throws Exception {
        List<Double> values = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                values.add(Double.parseDouble(line.trim()));
            }
        }
        return values;
    }

    // 计算线性回归参数 a, b
    private double[] linearRegression(List<Double> X, List<Double> Y) {
        int N = X.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < N; i++) {
            sumX += X.get(i);
            sumY += Y.get(i);
            sumXY += X.get(i) * Y.get(i);
            sumX2 += X.get(i) * X.get(i);
        }

        double a = (N * sumXY - sumX * sumY) / (N * sumX2 - sumX * sumX);
        double b = (sumY - a * sumX) / N;
        return new double[]{a, b};
    }
}
