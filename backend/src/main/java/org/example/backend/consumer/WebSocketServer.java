package org.example.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import org.example.backend.consumer.utils.JwtAuthentication;
import org.example.backend.mapper.UserMapper;
import org.example.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {

    final public static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();
    private User user;
    private Session session = null;

    public static UserMapper userMapper;

    public static RestTemplate restTemplate;

    private final static String addTrainUrl = "http://127.0.0.1:3000/train/add/";
    private final static String getListTrainUrl = "http://127.0.0.1:3000/train/getlist/";
    private final static String removeTrainUrl = "http://127.0.0.1:3000/train/remove/";
    private final static String addTrainLogUrl = "http://127.0.0.1:3000/trainlog/add/";
    private final static String getListTrainLogUrl = "http://127.0.0.1:3000/trainlog/getist/";


    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        this.session = session;
        System.out.println("connected!");
//        System.out.println("token : " + token);
        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);

        if (this.user != null) {
            users.put(userId, this);
        } else {
            this.session.close();
        }

        System.out.println(users);
    }

    @OnClose
    public void onClose() {
        System.out.println("disconnected!");
        if (this.user != null) {
            users.remove(this.user.getId());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {  // 当做路由
        System.out.println("receive message!");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        String trainingName = data.getString("trainingName");
        String model = data.getString("model");
        String pytorchVersion = data.getString("pytorchVersion");
        String modelParams = data.getString("modelParams");
//        System.out.println("event : " + event);
//        System.out.println("trainingName : " + trainingName);
//        System.out.println("model : " + model);
//        System.out.println("pytorchVersion : " + pytorchVersion);
//        System.out.println("modelParams : " + modelParams);


        if (event.equals("start-training")) {
            System.out.println("in start-training");
            startTraining(message);
        }
        else if (event.equals("stop-training")) {
            System.out.println("stop-training");
            stopTraining();
        }
    }
    // 启动训练线程


//    private void startTraining(String message) {
//        JSONObject data_web = JSONObject.parseObject(message);
//        String trainingName_pre = data_web.getString("trainingName");
//        long currentTimeMillis = System.currentTimeMillis(); // 获取当前的时间戳
//        Date currentDate = new Date(currentTimeMillis); // 将时间戳转换为Date对象
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // 定义日期格式
//        String formattedDate = dateFormat.format(currentDate); // 格式化日期
//        String trainingName = trainingName_pre + "_" + formattedDate;
//        String model = data_web.getString("model");
//        String pytorchVersion = data_web.getString("pytorchVersion");
//        String modelParams = data_web.getString("modelParams");
//        String token = data_web.getString("token");
////        Map<String, String> paramsTrain = new HashMap<>();
////        paramsTrain.put("trainingName", trainingName);
////        paramsTrain.put("model", model);
////        paramsTrain.put("pytorchVersion", pytorchVersion);
////        paramsTrain.put("modelParams", modelParams);
//
//        MultiValueMap<String, String> paramsTrain = new LinkedMultiValueMap<>();
//        paramsTrain.add("trainingName", trainingName);
//        paramsTrain.add("model", model);
//        paramsTrain.add("pytorchVersion", pytorchVersion);
//        paramsTrain.add("modelParams", modelParams);
//        // 调用后端接口添加训练任务
////        System.out.println("token : " + token);
////
////        System.out.println("paramsTrain : " );
////        System.out.println( paramsTrain);
//
//        restTemplate.postForObject(addTrainUrl, paramsTrain, String.class);
//
////        System.out.println("成功addTrain");
//        isRunning = true; // 启动训练时将标志位设为 true
//        new Thread(() -> {
//            try {
//                for (int i = 0; i <= 100 && isRunning; i += 10) { // 检查 isRunning 以停止循环
//                    // 模拟数据，生成 JSON 格式的字符串
//                    JSONObject data = new JSONObject();
//                    System.out.println("itera : ");
//                    System.out.println(i);
//                    data.put("progress", i);
//                    data.put("loss", String.format("%.4f", 1.0 / (i + 1)));
//                    data.put("cpu_usage", i);
//                    data.put("gpu_usage", i * 0.5);
//                    data.put("memory_usage", i * 0.8);
//                    data.put("vram_usage", i * 0.7);
//                    data.put("network_bandwidth", i * 0.3);
//                    data.put("error_message", "running");
//
//                    // 将 JSON 数据发送到 WebSocket 客户端
//                    sendMessage(data.toJSONString());
//
//                    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//                    params.add("trainingname", trainingName); // 例如使用用户名作为 trainingname
//                    params.add("progress", String.valueOf(i));
//                    params.add("loss", String.valueOf(1.0 / (i + 1)));
//                    params.add("cpu_usage", String.valueOf(i));
//                    params.add("gpu_usage", String.valueOf(i * 0.5));
//                    params.add("memory_usage", String.valueOf(i * 0.8));
//                    params.add("vram_usage", String.valueOf(i * 0.7));
//                    params.add("network_bandwidth", String.valueOf(i * 0.3));
//
//                    // 发送 POST 请求到数据库更新接口
//                    restTemplate.postForObject(addTrainLogUrl, params, String.class);
//                    // 模拟2秒延迟
//                    Thread.sleep(2000);
//                }
//                // 如果正常完成循环，发送完成消息
//                if (isRunning) {
//                    JSONObject data = new JSONObject();
//                    data.put("error_message", "complete");
//                    sendMessage(data.toJSONString());
//                }
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } finally {
//                isRunning = false; // 保证线程结束时将标志位设为 false
//            }
//        }).start();
//    }

    private Process trainingProcess = null; // 用于保存 Python 进程
    private boolean isRunning = false;     // 标志训练状态
    // 停止训练方法，将 isRunning 设置为 false
    private void stopTraining() {
        isRunning = false; // 设置状态为停止
        if (trainingProcess != null) {
            trainingProcess.destroy(); // 终止 Python 进程
            System.out.println("Python 训练进程已终止");
            trainingProcess = null;
        }
    }
    private void startTraining(String message) {
        JSONObject data_web = JSONObject.parseObject(message);
        String trainingName_pre = data_web.getString("trainingName");
        long currentTimeMillis = System.currentTimeMillis(); // 获取当前的时间戳
        Date currentDate = new Date(currentTimeMillis); // 将时间戳转换为Date对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // 定义日期格式
        String formattedDate = dateFormat.format(currentDate); // 格式化日期
        String trainingName = trainingName_pre + "_" + formattedDate;
        String model = data_web.getString("model");
        String pytorchVersion = data_web.getString("pytorchVersion");
        String modelParams = data_web.getString("modelParams");
        String token = data_web.getString("token");
        String scene = data_web.getString("scene");
        String checkpointpath = "./" + trainingName + "_" + scene + ".pth";
//        Map<String, String> paramsTrain = new HashMap<>();
//        paramsTrain.put("trainingName", trainingName);
//        paramsTrain.put("model", model);
//        paramsTrain.put("pytorchVersion", pytorchVersion);
//        paramsTrain.put("modelParams", modelParams);

        MultiValueMap<String, String> paramsTrain = new LinkedMultiValueMap<>();
        paramsTrain.add("trainingName", trainingName);
        paramsTrain.add("model", model);
        paramsTrain.add("pytorchVersion", pytorchVersion);
        paramsTrain.add("modelParams", modelParams);
        paramsTrain.add("scene", scene);
        paramsTrain.add("checkpointpath", checkpointpath);
        // 调用后端接口添加训练任务
//        System.out.println("token : " + token);
//
//        System.out.println("paramsTrain : " );
//        System.out.println( paramsTrain);

        restTemplate.postForObject(addTrainUrl, paramsTrain, String.class);

//        System.out.println("成功addTrain");
        isRunning = true; // 启动训练时将标志位设为 true

        new Thread(() -> {
            try {
                String[] command = {
                        "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                        "conda activate ssp && python C:\\Users\\Lenovo\\Desktop\\research\\kaifa\\202411\\backend\\src\\main\\python\\test.py"
                };

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出
                trainingProcess = processBuilder.start();

                // 读取 Python 输出
                BufferedReader reader = new BufferedReader(new InputStreamReader(trainingProcess.getInputStream()));

                System.out.println("准备运行cmd 指令");

                String line;
                while ((line = reader.readLine()) != null) {
                    // 假设 Python 输出是 JSON 格式
                    System.out.println("python train data : " + line);

                    JSONObject data = new JSONObject();
                    data.put("output", line);

                    // 将 JSON 数据发送到 WebSocket 客户端
                    sendMessage(data.toJSONString());

                    // 模拟 2 秒的延迟，符合需求
                    Thread.sleep(1000);
                }

                JSONObject data = new JSONObject();
                data.put("error_message", "complete");
                sendMessage(data.toJSONString());

                reader.close();
                trainingProcess.waitFor();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) {
        synchronized (this.session) {
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}