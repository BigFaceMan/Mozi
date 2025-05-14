package com.games.games.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.games.mapper.*;
import com.games.games.pojo.*;
import com.games.games.utils.FileUtils;
import com.games.games.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TrainServiceImpl implements TrainService {
    // 用于存储子线程的映射
    private final ConcurrentHashMap<String, Thread> processMap = new ConcurrentHashMap<>();

    // 维护多个日志队列，每个 processId 一个队列
    private static final Map<String, BlockingQueue<String>> logQueueMap = new ConcurrentHashMap<>();
    private static final Map<String, BlockingQueue<String>> infoQueueMap = new ConcurrentHashMap<>();
    private static final Map<String, BlockingQueue<String>> errorQueueMap = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TrainMapper trainMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private TrainLogMapper trainLogMapper;
    @Autowired
    private InferLogMapper inferLogMapper;
    @Autowired
    private TrainInfoMapper trainInfoMapper;
    @Autowired
    private ExceptionLogMapper exceptionLogMapper;
    @Autowired
    private ModelPthMapper modelPthMapper;
    @Value("${trainLogPrint}")
    private static int trainLogPrint;
    @Value("${server.env}")
    private String  envName;
    @Value("${enginePlatformUrl}")
    private String enginePlatformUrl;// 服务平台的URL
    @Value("${servicePlatformUrl}")
    private String serviceAddr;
    private int runStatus = 0;
    private List<Integer> allowPorts = new ArrayList<>(Collections.nCopies(10, 0));



    private static String getPythonProcessId(String processId) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String pid = null;
        String line;
        ProcessBuilder processBuilder;

        if (os.contains("win")) {
            // Windows: 使用 WMIC 查询 Python 进程
            processBuilder = new ProcessBuilder("wmic", "process", "where", "\"commandline like 'python%'\"", "get", "ProcessId,CommandLine");
        } else {
            // Linux: 使用 ps aux 查询 Python 进程
            processBuilder = new ProcessBuilder("/bin/bash", "-c", "ps aux | grep '[p]ython'");
        }

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = reader.readLine()) != null) {
                printTrainLog(line);
                // 检查是否包含 processId
                if (line.contains(processId)) {
                    printTrainLog(line);
                    // Windows: WMIC 输出格式 -> 获取最后一列的 PID
                    if (os.contains("win")) {
                        String[] columns = line.trim().split("\\s+");
                        if (columns.length > 1) {
                            pid = columns[columns.length - 1]; // 获取 ProcessId
                            break;
                        }
                    } else {
                        // Linux: `ps aux` 输出格式 -> 第二列是 PID
                        String[] columns = line.trim().split("\\s+");
                        if (columns.length > 2) {
                            pid = columns[1]; // 进程 ID 在 `ps aux` 的第二列
                            break;
                        }
                    }
                }
            }
        }
        return pid;
    }
    public void saveCheckpoint(String trainingName, String scene, int trainId,  byte[] modelCheckpoint) throws IOException {
        QueryWrapper<ModelPth> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("train_id", trainId);
        ModelPth existingCheckpoint = modelPthMapper.selectOne(objectQueryWrapper);
        if (existingCheckpoint != null) {
            // 如果存在，则更新
            existingCheckpoint.setModelPth(modelCheckpoint);
            modelPthMapper.updateById(existingCheckpoint);
            return;
        }
        ModelPth checkpoint = new ModelPth();
        checkpoint.setTrainningName(trainingName);
        checkpoint.setScene(scene);
        checkpoint.setTrainId(trainId);
        checkpoint.setModelPth(modelCheckpoint);
        checkpoint.setTimestamp(new Date());
        modelPthMapper.insert(checkpoint);
    }
    @Override
    public Map<String, String> addTrain(MultiValueMap<String, String> data) {
        int type = Integer.parseInt(data.getFirst("type"));
        String trainingNamePre = data.getFirst("trainingName");
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String formattedDate = dateFormat.format(new Date(currentTimeMillis));
        String trainingName = trainingNamePre + "_" + formattedDate;

        String model = data.getFirst("model");
        String pytorchVersion = data.getFirst("pytorchVersion");
        String modelParams = data.getFirst("modelParams");
        String scene = data.getFirst("scene");

        File projectPath = new File(System.getProperty("user.dir"), "/python");
        if (!projectPath.exists() && projectPath.mkdirs()) {
            printTrainLog("目录创建成功：" + projectPath.getAbsolutePath());
        }

        File tensorboardDir = new File(projectPath.getPath(), "/logs");
        File checkpointDir = new File(projectPath.getPath(), "/checkpoint");
        if (!tensorboardDir.exists()) {
            if (tensorboardDir.mkdirs()) {
                printTrainLog("目录创建成功：" + tensorboardDir.getAbsolutePath());
            } else {
                printTrainLog("目录创建失败！");
            }
        }
        if (!checkpointDir.exists()) {
            if (checkpointDir.mkdirs()) {
                printTrainLog("目录创建成功：" + checkpointDir.getAbsolutePath());
            } else {
                printTrainLog("目录创建失败！");
            }
        }

        File checkpointFile = new File(projectPath, "checkpoint/" + trainingName + "_" + scene + ".pth");
        File tensorboardFile = new File(projectPath, "logs/" + trainingName + "_" + scene);
        String checkpointPath = checkpointFile.getPath();
        String tensorboardPath = tensorboardFile.getPath();

        if (data.getFirst("trainId") != null) {
            int useTrainId = Integer.parseInt(data.getFirst("trainId"));
            ModelPth modelPth = modelPthMapper.selectOne(new QueryWrapper<ModelPth>().eq("train_id", useTrainId));
            if (modelPth != null) {
                byte[] modelPthData = modelPth.getModelPth();
                try (FileOutputStream fos = new FileOutputStream(checkpointFile)) {
                    fos.write(modelPthData);
                    fos.flush();
                    printTrainLog("模型文件已保存至：" + checkpointFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("保存模型失败", e);
                }
            } else {
                printTrainLog("未找到对应的模型数据");
            }
        }

        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        Integer uId = Integer.parseInt(data.getFirst("uId"));
        String userName = data.getFirst("userName");

        QueryWrapper<Model> queryWrapper = new QueryWrapper<>();
        Model modelTrain = modelMapper.selectOne(queryWrapper.eq("name", model));

        // 写入 Python 代码到 train.py
        File trainFile = new File(projectPath, "train.py");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile), StandardCharsets.UTF_8))) {
            writer.write(modelTrain.getCode());
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "保存代码文件失败: " + e.getMessage());
            response.put("code", "500");
            return response;
        }

        // 处理参数
        String params = data.getFirst("params");

        Map<String, Object> paramsMap;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            paramsMap = objectMapper.readValue(params, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String paramsJson = isWindows ? params.replace("\"", "\\\"") : params.replace("'", "\\'");
        String quote = isWindows ? "\"" : "'";
        printTrainLog("GameNode Got ParamsJson: " + paramsJson);
        printTrainLog("Conda Env Name: " + envName);
        try {
            String[] command;
            if (isWindows) {
                command = new String[]{
                        "cmd.exe", "/c",
                        "conda activate " + envName + " && python -u " + trainFile.getPath() + " --checkpointpath " + checkpointPath + " --params " + quote + paramsJson + quote
                };
            } else {
                command = new String[]{
                        "/bin/bash", "-c",
                        "source ~/anaconda3/etc/profile.d/conda.sh && conda activate " + envName +
                                " && python -u " + trainFile.getPath() + " --checkpointpath " + checkpointPath + " --params " + quote + paramsJson + quote
                };
            }

            AtomicInteger runStep = new AtomicInteger(-1);
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process pythonProcess = processBuilder.start();

            printTrainLog("Python 训练进程启动，等待 PID 输出...");

            final String[] processIdHolder = {null};

            // 读取 stdout 和 PID
            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        printTrainLog("Python 输出: " + line);
                        System.out.flush();
                        // 获取 PID
                        if (line.startsWith("PID :")) {
                            synchronized (processIdHolder) {
                                printTrainLog("want get pid !!!!!");
                                processIdHolder[0] = line.replace("PID :", "").trim();
                                printTrainLog("PID : " + processIdHolder[0]);
                            }
                        }
                        synchronized (processIdHolder) {
                            if (processIdHolder[0] != null && logQueueMap.containsKey(processIdHolder[0])) {
                                if (line.startsWith("trainInfo")) {
                                    infoQueueMap.get(processIdHolder[0]).put(line);
                                    runStep.getAndIncrement();
                                } else {
                                    logQueueMap.get(processIdHolder[0]).put(line);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    printTrainLog("读取标准输出时出错: " + e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            // 读取 stderr
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        printTrainLog("Python 错误: " + line);
                        System.out.flush();
                        synchronized (processIdHolder) {
                            if (processIdHolder[0] != null && errorQueueMap.containsKey(processIdHolder[0])) {
                                errorQueueMap.get(processIdHolder[0]).put(line);
                            }
                        }
                    }
                } catch (IOException e) {
                    printTrainLog("读取错误输出时出错: " + e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            stdoutThread.start();
            stderrThread.start();
            // 等待最多 5 秒获取 PID
            long startTime = System.currentTimeMillis();
            long timeout = 100000; // 5 秒
            while (processIdHolder[0] == null && (System.currentTimeMillis() - startTime) < timeout) {
                Thread.sleep(100);
            }

            // 确保 PID 获取成功
            if (processIdHolder[0] == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "保存代码文件失败: " + "无法获取 Python 进程的 PID");
                return response;
            }
            String processId = processIdHolder[0];
            logQueueMap.put(processId, new LinkedBlockingQueue<>());
            errorQueueMap.put(processId, new LinkedBlockingQueue<>());
            infoQueueMap.put(processId, new LinkedBlockingQueue<>());
            printTrainLog("Python 进程 PID 获取成功: " + processId);

            Thread dbThread = new Thread(() -> {
                try {
                    BlockingQueue<String> queue = logQueueMap.get(processId);
                    while (true) {
                        String log = queue.take();  // 读取该进程的日志
                        if (type == 0) {
                            TrainLog trainLog = new TrainLog(null, userName, trainingName, log, new Date());
                            trainLogMapper.insert(trainLog);
                        } else {
                            InferLog inferLog = new InferLog(null, userName, trainingName, log, new Date());
                            inferLogMapper.insert(inferLog);
                        }
                    }
                } catch (InterruptedException e) {
                    printTrainLog("数据库写入线程被中断: " + e.getMessage());
                }
            });
            dbThread.start();
// 处理错误日志
            Thread errorDbThread = new Thread(() -> {
                try {
                    BlockingQueue<String> queue = errorQueueMap.get(processId);
                    while (true) {
                        String errorLog = queue.take();
                        ExceptionLog exceptionLog = new ExceptionLog(null, userName, errorLog, new Date());
                        exceptionLogMapper.insert(exceptionLog);
                    }
                } catch (InterruptedException e) {
                    printTrainLog("数据库写入线程被中断: " + e.getMessage());
                }
            });
//            errorDbThread.setDaemon(true);
            errorDbThread.start();
            QueryWrapper<Train> trainQueryWrapper = new QueryWrapper<>();
            trainQueryWrapper.eq("scene", scene).eq("model", model);
            List<Train> trains = trainMapper.selectList(trainQueryWrapper);
            Train train;
            if (trains.isEmpty()) {
                train = new Train(null, trainingName, pytorchVersion, scene, model, modelParams, checkpointPath, 1, tensorboardPath, uId, 3, ip, port, processId, trainFile.getPath(), params, 1,  -1, type);
            } else {
                train = new Train(null, trainingName, pytorchVersion, scene, model, modelParams, checkpointPath, 1, tensorboardPath, uId, 3, ip, port, processId, trainFile.getPath(), params, 0,  -1, type);
            }
            // 记录到数据库
            trainMapper.insert(train);
            int trainId = trainMapper.selectOne(new QueryWrapper<Train>().eq("trainingname", trainingName)).getId();

//            loss... info
            Thread infoDbThread = new Thread(() -> {
                try {
                    BlockingQueue<String> queue = infoQueueMap.get(processId);
                    while (true) {
                        String log = queue.take();  // 读取该进程的日志
                        String[] parts = log.split(" ");
//                        step accuracy speed stability loss reward
                        if (parts.length < 7) {
                            continue; // 如果日志格式不正确，跳过
                        }
                        int step = Integer.parseInt(parts[1]);
                        float accuracy = Float.parseFloat(parts[2]);
                        float speed = Float.parseFloat(parts[3]);
                        float stability = Float.parseFloat(parts[4]);
                        float loss = Float.parseFloat(parts[5]);
                        float reward = Float.parseFloat(parts[6]);

                        TrainInfo trainInfo = new TrainInfo(null, trainId, step, accuracy, speed, stability, loss, reward);
                        trainInfoMapper.insert(trainInfo);
                    }
                } catch (InterruptedException e) {
                    printTrainLog("loss写入线程被中断: " + e.getMessage());
                }
            });
            infoDbThread.start();

            // 监听进程状态
            Thread trainingThread = new Thread(() -> {
                try {
                    int exitCode = pythonProcess.waitFor();
                    printTrainLog("Python 进程退出，代码: " + exitCode);
                    UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();

                    updateWrapper.eq("trainingname", trainingName).set("running", exitCode == 0 ? 0 : runStatus).set("step", runStep.get());


                    String url = serviceAddr + "/games/stopEngine/";
//                    head
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
//                    body
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("engineAddr", paramsMap.get("envAdress"));
                    requestBody.put("zzjId", paramsMap.get("envsId"));
                    printTrainLog("train want stop : " + requestBody);
//                    httpEntity
                    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
                    String respJson = restTemplate.postForObject(url, requestBody, String.class);
                    Map<String, Object> resp = objectMapper.readValue(respJson, new TypeReference<Map<String, Object>>() {});
                    String respCode = (String) resp.get("code");
                    String respMsg = (String) resp.get("msg");
                    if (respCode.equals("200")) {
                        printTrainLog("关闭中间件和引擎成功!!!");
                        printTrainLog(respMsg);
                    }
//                    update datbase
                    if (trainMapper.update(null, updateWrapper) > 0 && type == 0) {
                        Train train1 = trainMapper.selectOne(new QueryWrapper<Train>().eq("trainingname", trainingName));
                        byte[] modelCheckpoint = FileUtils.readFileToByteArray(checkpointPath);
                        saveCheckpoint(trainingName, scene, train1.getId(), modelCheckpoint);
                        printTrainLog("保存 checkpoint 文件...");
//                        }
                        printTrainLog("成功更新训练进程状态");
                        runStatus = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    while (!logQueueMap.get(processId).isEmpty() || !errorQueueMap.get(processId).isEmpty() || !infoQueueMap.get(processId).isEmpty()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    dbThread.interrupt();
                    infoDbThread.interrupt();
                    errorDbThread.interrupt();
                    logQueueMap.remove(processId);
                    errorQueueMap.remove(processId);
                    printTrainLog("删除队列成功!!!!!!!!!!!");
                    processMap.remove(processId);
                }
            });

            processMap.put(processId, trainingThread);
            trainingThread.start();

            Map<String, String> response = new HashMap<>();
            response.put("message", "success");
            response.put("code", "200");
            response.put("processId", processId);
            response.put("trainingName", trainingName);
            return response;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "启动 Python 进程失败: " + e.getMessage());
            return response;
        }
    }

    public static void printTrainLog(String msg) {
        if (trainLogPrint == 1) {
            printTrainLog(msg);
        }
    }

    public boolean terminateProcess(String pid) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process;
            if (os.contains("win")) {
                process = new ProcessBuilder("taskkill", "/F", "/T", "/PID", pid).start();
            } else {
                process = new ProcessBuilder("kill", "-9", pid).start();
            }
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            printTrainLog("终止进程失败: " + e.getMessage());
            return false;
        }
    }
    @Override
    public Map<String, String> killTrain(MultiValueMap<String, String> data) throws IOException {
        Map<String, String> response = new HashMap<>();
        String processId = data.getFirst("processId");
        String trainingName = data.getFirst("trainingName");;
        if (processId == null || !processMap.containsKey(processId)) {
            UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("trainingname", trainingName).set("running", 0);
            boolean isUpdated = trainMapper.update(null, updateWrapper) > 0;
            if (isUpdated) {
                printTrainLog("Exception : 成功更新训练进程状态");
            } else {
                printTrainLog("Exception : 未成功更新训练进程状态");
            }
            response.put("status", "error");
            response.put("message", "Invalid processId");
            return response;
        }

        printTrainLog("In Kill Train processId : " + processId);

        // 获取存储的进程
        Thread thread = processMap.get(processId);
        lock.lock();
        try {
            runStatus = 0;
            boolean killed = terminateProcess(processId);
            if (!killed) {
                response.put("status", "error");
                response.put("message", "Failed to terminate process");
                return response;
            }

            printTrainLog("等待训练线程执行完数据库更新...");
            thread.join();  // **等待该线程执行完毕**
            printTrainLog("训练线程已结束，数据库更新完成");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        response.put("status", "success");
        return response;
    }

    @Override
    public Map<String, String> stopTrain(MultiValueMap<String, String> data) throws IOException {
        Map<String, String> response = new HashMap<>();
        String processId = data.getFirst("processId");
        String trainingName = data.getFirst("trainingName");;
        if (processId == null || !processMap.containsKey(processId)) {
            response.put("status", "error");
            response.put("message", "Invalid processId");
            return response;
        }
        // 获取存储的进程
        Thread thread = processMap.get(processId);
        lock.lock();
        try {
            runStatus = 2;
            boolean killed = terminateProcess(processId);
            if (!killed) {
                response.put("status", "error");
                response.put("message", "Failed to terminate process");
                return response;
            }
            // **等待 `addTrain` 里的线程执行完毕**
            printTrainLog("等待训练线程执行完数据库更新...");
            thread.join();  // **等待该线程执行完毕**
            printTrainLog("训练线程已结束，数据库更新完成");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        response.put("status", "success");
        return response;
    }

    @Override
    public Map<String, String> continueTrain(MultiValueMap<String, String> data) throws IOException {
        int trainId = Integer.parseInt(data.getFirst("trainId"));
        Train train = trainMapper.selectById(trainId);
        String trainingName = train.getTrainingname();
        String model = train.getModel();
        String scene = train.getScene();

        File projectPath = new File(System.getProperty("user.dir"), "/python");
        if (!projectPath.exists() && projectPath.mkdirs()) {
            printTrainLog("目录创建成功：" + projectPath.getAbsolutePath());
        }

        File tensorboardDir = new File(projectPath.getPath(), "/logs");
        File checkpointDir = new File(projectPath.getPath(), "/checkpoint");
        if (!tensorboardDir.exists()) {
            if (tensorboardDir.mkdirs()) {
                printTrainLog("目录创建成功：" + tensorboardDir.getAbsolutePath());
            } else {
                printTrainLog("目录创建失败！");
            }
        }
        if (!checkpointDir.exists()) {
            if (checkpointDir.mkdirs()) {
                printTrainLog("目录创建成功：" + checkpointDir.getAbsolutePath());
            } else {
                printTrainLog("目录创建失败！");
            }
        }

        File checkpointFile = new File(projectPath, "checkpoint/" + trainingName + "_" + scene + ".pth");
        File tensorboardFile = new File(projectPath, "logs/" + trainingName + "_" + scene);
        String checkpointPath = checkpointFile.getPath();
        String tensorboardPath = tensorboardFile.getPath();

        ModelPth modelPth = modelPthMapper.selectOne(new QueryWrapper<ModelPth>().eq("train_id", trainId));
        if (modelPth != null) {
            byte[] modelPthData = modelPth.getModelPth();

            try (FileOutputStream fos = new FileOutputStream(checkpointFile)) {
                fos.write(modelPthData);
                fos.flush();
                printTrainLog("模型文件已保存至：" + checkpointFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("保存模型失败", e);
            }
        } else {
            printTrainLog("未找到对应的模型数据");
        }


        String userName = data.getFirst("userName");
        QueryWrapper<Model> queryWrapper = new QueryWrapper<>();
        Model modelTrain = modelMapper.selectOne(queryWrapper.eq("name", model));

        // 写入 Python 代码到 train.py
        File trainFile = new File(projectPath, "train.py");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile), StandardCharsets.UTF_8))) {
            writer.write(modelTrain.getCode());
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "保存代码文件失败: " + e.getMessage());
            response.put("code", "500");
            return response;
        }

        // 处理参数
        String params = data.getFirst("params");
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String paramsJson = isWindows ? params.replace("\"", "\\\"") : params.replace("'", "\\'");
        String quote = isWindows ? "\"" : "'";
        printTrainLog("GameNode Got ParamsJson: " + paramsJson);
        printTrainLog("Conda Env Name: " + envName);
        try {
            String[] command;
            if (isWindows) {
                command = new String[]{
                        "cmd.exe", "/c",
                        "conda activate " + envName + " && python -u " + trainFile.getPath() + " --checkpointpath " + checkpointPath + " --reload " + "1" +
                                " --tensorboardpath " + tensorboardPath + " --params " + quote + paramsJson + quote
                };
            } else {
                command = new String[]{
                        "/bin/bash", "-c",
                        "source ~/anaconda3/etc/profile.d/conda.sh && conda activate " + envName +
                                " && python -u " + trainFile.getPath() + " --checkpointpath " + checkpointPath + " --reload " + "1" +
                                " --tensorboardpath " + tensorboardPath + " --params " + quote + paramsJson + quote
                };
            }

            AtomicInteger runStep = new AtomicInteger(-1);
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process pythonProcess = processBuilder.start();

            printTrainLog("Python 训练进程启动，等待 PID 输出...");

            final String[] processIdHolder = {null};

            // 读取 stdout 和 PID
            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        printTrainLog("Python 输出: " + line);
                        System.out.flush();
                        // 获取 PID
                        if (line.startsWith("PID :")) {
                            synchronized (processIdHolder) {
                                printTrainLog("want get pid !!!!!");
                                processIdHolder[0] = line.replace("PID :", "").trim();
                                printTrainLog("PID : " + processIdHolder[0]);
                            }
                        }
                        synchronized (processIdHolder) {
                            if (processIdHolder[0] != null && logQueueMap.containsKey(processIdHolder[0])) {
                                if (line.startsWith("trainInfo")) {
                                    infoQueueMap.get(processIdHolder[0]).put(line);
                                    runStep.getAndIncrement();
                                } else {
                                    logQueueMap.get(processIdHolder[0]).put(trainingName + " " + line);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    printTrainLog("读取标准输出时出错: " + e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            // 读取 stderr
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        printTrainLog("Python 错误: " + line);
                        System.out.flush();
                        synchronized (processIdHolder) {
                            if (processIdHolder[0] != null && errorQueueMap.containsKey(processIdHolder[0])) {
                                errorQueueMap.get(processIdHolder[0]).put(line);
                            }
                        }
                    }
                } catch (IOException e) {
                    printTrainLog("读取错误输出时出错: " + e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            stdoutThread.start();
            stderrThread.start();
            // 等待最多 5 秒获取 PID
            long startTime = System.currentTimeMillis();
            long timeout = 10000; // 5 秒
            while (processIdHolder[0] == null && (System.currentTimeMillis() - startTime) < timeout) {
                Thread.sleep(100);
            }
            // 确保 PID 获取成功
            if (processIdHolder[0] == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "保存代码文件失败: " + "无法获取 Python 进程的 PID");
                return response;
            }
            String processId = processIdHolder[0];
            logQueueMap.put(processId, new LinkedBlockingQueue<>());
            errorQueueMap.put(processId, new LinkedBlockingQueue<>());
            infoQueueMap.put(processId, new LinkedBlockingQueue<>());
            printTrainLog("Python 进程 PID 获取成功: " + processId);

            Thread dbThread = new Thread(() -> {
                try {
                    BlockingQueue<String> queue = logQueueMap.get(processId);
                    while (true) {
                        String log = queue.take();  // 读取该进程的日志
                        TrainLog trainLog = new TrainLog(null, userName, trainingName, log, new Date());
                        trainLogMapper.insert(trainLog);
                    }
                } catch (InterruptedException e) {
                    printTrainLog("数据库写入线程被中断: " + e.getMessage());
                }
            });
//            dbThread.setDaemon(true);
            dbThread.start();
// 处理错误日志
            Thread errorDbThread = new Thread(() -> {
                try {
                    BlockingQueue<String> queue = errorQueueMap.get(processId);
                    while (true) {
                        String errorLog = queue.take();
                        ExceptionLog exceptionLog = new ExceptionLog(null, userName, errorLog, new Date());
                        exceptionLogMapper.insert(exceptionLog);
                    }
                } catch (InterruptedException e) {
                    printTrainLog("数据库写入线程被中断: " + e.getMessage());
                }
            });
//            errorDbThread.setDaemon(true);
            errorDbThread.start();
            UpdateWrapper<Train> objectUpdateWrapper = new UpdateWrapper<>();
            objectUpdateWrapper.eq("id", trainId).set("running", 1).set("processid", processId).set("params", params);
            boolean isUpdated = trainMapper.update(null, objectUpdateWrapper) > 0;
            if (isUpdated) {
                printTrainLog("Exception : 成功更新继续训练进程状态");
            } else {
                printTrainLog("Exception : 未成功继续更新训练进程状态");
            }

//            loss... info
            Thread infoDbThread = new Thread(() -> {
                try {
                    BlockingQueue<String> queue = infoQueueMap.get(processId);
                    while (true) {
                        String log = queue.take();  // 读取该进程的日志
                        String[] parts = log.split(" ");
//                        step accuracy speed stability loss reward
                        if (parts.length < 7) {
                            continue; // 如果日志格式不正确，跳过
                        }
                        int step = Integer.parseInt(parts[1]);
                        float accuracy = Float.parseFloat(parts[2]);
                        float speed = Float.parseFloat(parts[3]);
                        float stability = Float.parseFloat(parts[4]);
                        float loss = Float.parseFloat(parts[5]);
                        float reward = Float.parseFloat(parts[6]);

                        TrainInfo trainInfo = new TrainInfo(null, trainId, step, accuracy, speed, stability, loss, reward);
                        trainInfoMapper.insert(trainInfo);
                    }
                } catch (InterruptedException e) {
                    printTrainLog("loss写入线程被中断: " + e.getMessage());
                }
            });
            infoDbThread.start();

            // 监听进程状态
            Thread trainingThread = new Thread(() -> {
                try {
                    int exitCode = pythonProcess.waitFor();
                    printTrainLog("Python 进程退出，代码: " + exitCode);
                    UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("trainingname", trainingName).set("running", exitCode == 0 ? 0 : runStatus).set("step", runStep.get());
                    if (trainMapper.update(null, updateWrapper) > 0) {
//                        if (runStatus == 0 || exitCode == 0) {
                        Train train1 = trainMapper.selectOne(new QueryWrapper<Train>().eq("trainingname", trainingName));
                        byte[] modelCheckpoint = FileUtils.readFileToByteArray(checkpointPath);
                        saveCheckpoint(trainingName, scene, train1.getId(), modelCheckpoint);
                        printTrainLog("保存 checkpoint 文件...");
//                        }
                        printTrainLog("成功更新训练进程状态");
                    }
                    runStatus = 0;
                    // 训练完成后删除队列，避免占用内存
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    while (!logQueueMap.get(processId).isEmpty() || !errorQueueMap.get(processId).isEmpty() || !infoQueueMap.get(processId).isEmpty()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    dbThread.interrupt();
                    infoDbThread.interrupt();
                    errorDbThread.interrupt();

                    logQueueMap.remove(processId);
                    errorQueueMap.remove(processId);
                    printTrainLog("删除队列成功!!!!!!!!!!!");
                    processMap.remove(processId);
                }
            });

            processMap.put(processId, trainingThread);
            trainingThread.start();

            Map<String, String> response = new HashMap<>();
            response.put("message", "success");
            response.put("code", "200");
            response.put("processId", processId);
            response.put("trainingName", trainingName);
            return response;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "启动 Python 进程失败: " + e.getMessage());
            return response;
        }
//        Map<String, String> response = new HashMap<>();
//        String processId = data.getFirst("processId");
//        String trainingName = data.getFirst("trainingName");
//        String tensorboardpath = data.getFirst("tensorboardpath");
//        Train train = trainMapper.selectOne(new QueryWrapper<Train>().eq("trainingname", trainingName));
//        String trainPyPath = train.getTrainpypath();
//        String paramsJson = train.getParams();
//        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
//        String quote = isWindows ? "\"" : "'";
//
//        try {
//            String[] command;
//            if (isWindows) {
//                command = new String[]{
//                        "cmd.exe", "/c",
//                        "conda activate " + envName + " && python -u " + trainPyPath + " --checkpointpath " + train.getCheckpointpath() +
//                                " --tensorboardpath " + tensorboardpath + " --params " + quote + paramsJson + quote
//                };
//            } else {
//                command = new String[]{
//                        "/bin/bash", "-c",
//                        "source ~/anaconda3/etc/profile.d/conda.sh && conda activate " + envName +
//                                " && python -u " + trainPyPath + " --checkpointpath " + train.getCheckpointpath() +
//                                " --tensorboardpath " + tensorboardpath + " --params " + quote + paramsJson + quote
//                };
//            }
//
//            ProcessBuilder processBuilder = new ProcessBuilder(command);
//            Process pythonProcess = processBuilder.start();
//
//            printTrainLog("Python 训练进程启动，等待 PID 输出...");
//
//            final String[] processIdHolder = {null};
//            // 读取 stdout 和 PID
//            Thread stdoutThread = new Thread(() -> {
//                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        printTrainLog("Python 输出: " + line);
//
//                        // 获取 PID
//                        if (line.startsWith("PID :")) {
//                            synchronized (processIdHolder) {
//                                processIdHolder[0] = line.replace("PID :", "").trim();
//                            }
//                        }
//                    }
//                } catch (IOException e) {
//                    printTrainLog("读取标准输出时出错: " + e.getMessage());
//                }
//            });
//
//            // 读取 stderr
//            Thread stderrThread = new Thread(() -> {
//                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()))) {
//                    String line;
//                    while ((line = errorReader.readLine()) != null) {
//                        printTrainLog("Python 错误: " + line);
//                    }
//                } catch (IOException e) {
//                    printTrainLog("读取错误输出时出错: " + e.getMessage());
//                }
//            });
//
//            stdoutThread.start();
//            stderrThread.start();
//            // 等待最多 5 秒获取 PID
//            long startTime = System.currentTimeMillis();
//            long timeout = 10000; // 5 秒
//            while (processIdHolder[0] == null && (System.currentTimeMillis() - startTime) < timeout) {
//                Thread.sleep(100);
//            }
//            // 确保 PID 获取成功
//            if (processIdHolder[0] == null) {
//                response = new HashMap<>();
//                response.put("message", "保存代码文件失败: " + "无法获取 Python 进程的 PID");
//                return response;
//            }
//
//            processId = processIdHolder[0];
//            printTrainLog("Python 进程 PID 获取成功: " + processId);
//            UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
//            updateWrapper.eq("trainingname", trainingName).set("running", 1).set("processid", processId);
//            AtomicBoolean isUpdated = new AtomicBoolean(trainMapper.update(null, updateWrapper) > 0);
//            if (isUpdated.get()) {
//                printTrainLog("Exception : 成功更新训练进程状态");
//            } else {
//                printTrainLog("Exception : 未成功更新训练进程状态");
//            }
//            // 监听进程状态
//            String finalProcessId = processId;
//            Thread trainingThread = new Thread(() -> {
//                try {
//                    int exitCode = pythonProcess.waitFor();
//                    printTrainLog("Python 进程退出，代码: " + exitCode);
//
//                    updateWrapper.eq("trainingname", trainingName).set("running", exitCode == 0 ? 0 : runStatus);
//                    if (trainMapper.update(null, updateWrapper) > 0) {
//                        printTrainLog("成功更新训练进程状态");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    processMap.remove(finalProcessId);
//                }
//            });
//
//            processMap.put(processId, trainingThread);
//            trainingThread.start();
//
//            response = new HashMap<>();
//            response.put("message", "success");
//            response.put("processId", processId);
//            response.put("trainingName", trainingName);
//            return response;
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            response = new HashMap<>();
//            response.put("message", "启动 Python 进程失败: " + e.getMessage());
//            return response;
//        }
    }

    @Override
    public Map<String, String> addTensorboard(MultiValueMap<String, String> data) {
        Map<String, String> result = new HashMap<>();
        String tensorboardpath = data.getFirst("tensorboardpath");
        printTrainLog("tensorboardpath : " + tensorboardpath);
        int port = 6001;
        synchronized (this) {
            for (int i = 0; i < 10; i ++) {
                if (allowPorts.get(i) == 0) {
                    port = 6001 + i;
                    allowPorts.set(i, 1);
                    break;
                }
            }
        }

        // 启动一个线程来执行 TensorBoard 命令

        int finalPort = port;
        printTrainLog("tensorboard port : " + finalPort);
        new Thread(() -> {
            try {
                // 构建 TensorBoard 命令
                String[] command;
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    // Windows 下使用 cmd.exe
                    command = new String[]{
                            "cmd.exe", "/c",
                            "conda activate " + envName + " && python -m tensorboard.main --logdir=" + tensorboardpath + " --port=" + finalPort
                    };
                } else {
                    // Linux 下使用 /bin/bash
                    command = new String[]{
                            "/bin/bash", "-c",
                            "source ~/anaconda3/etc/profile.d/conda.sh && conda activate " + envName + " && python -m tensorboard.main --logdir=" + tensorboardpath + " --port=" + finalPort
                    };
                }
                // 使用 ProcessBuilder 启动 TensorBoard
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出

                Process tensorBoardProcess = processBuilder.start(); // 启动进程
                int exitCode = tensorBoardProcess.waitFor(); // 等待进程结束并获取退出码

                if (exitCode != 0) {
                    // 如果退出码不为 0，表示启动失败
                    result.put("error_message", "failed");
                } else {
                    // 正常启动
                    result.put("error_message", "success");
                }

            } catch (IOException e) {
                // 捕获启动时的异常
                e.printStackTrace();
                result.put("error_message", "failed: IOException occurred");
            } catch (InterruptedException e) {
                // 捕获线程中断异常
                e.printStackTrace();
                result.put("error_message", "failed: Process was interrupted");
            } finally {
                // 释放端口
                synchronized (this) {
                    int index = finalPort - 6001;
                    allowPorts.set(index, 0);
                }
            }
        }).start();
        // 让当前线程休眠 0.5 秒
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            e.printStackTrace();
        }
        // 默认返回 "success"（但启动结果可能需要异步检查）
        result.put("error_message", "success");
        result.put("tPort", String.valueOf(port));
        return result;
    }

    @Override
    public Map<String, String> deleteTensorboard(MultiValueMap<String, String> data) {
        int port = Integer.parseInt(data.getFirst("tPort"));
        printTrainLog("want to kill port : " + port);
        killProcessByPort(port);
        HashMap<String, String> result = new HashMap<>();
        result.put("status", "success");
        return result;
    }

    public void killProcessByPort(int port) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder netstatProcessBuilder;

            if (os.contains("win")) {
                // Windows 使用 netstat 获取 PID
                netstatProcessBuilder = new ProcessBuilder("cmd.exe", "/c", "netstat -ano | findstr :" + port);
            } else {
                // Linux 使用 lsof 获取 PID
                netstatProcessBuilder = new ProcessBuilder("/bin/bash", "-c", "lsof -i :" + port + " -t");
            }

            Process netstatProcess = netstatProcessBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(netstatProcess.getInputStream()));

            String line;
            String pid = null;
            while ((line = reader.readLine()) != null) {
                printTrainLog("line is: " + line);
                if (line.contains("6001")) {  // 只处理包含 6001 端口的行
                    String[] parts = line.trim().split("\\s+");
                    pid = parts[parts.length - 1]; // PID 在最后一列
                    ProcessBuilder killProcessBuilder;
                    if (os.contains("win")) {
                        // Windows 使用 taskkill 杀死进程
                        killProcessBuilder = new ProcessBuilder("cmd.exe", "/c", "taskkill /PID " + pid + " /F");
                    } else {
                        // Linux 使用 kill 命令
                        killProcessBuilder = new ProcessBuilder("/bin/bash", "-c", "kill -9 " + pid);
                    }
                    Process killProcess = killProcessBuilder.start();
                    int exitCode = killProcess.waitFor();
                    if (exitCode == 0) {
                        printTrainLog("Successfully killed process with PID " + pid);
                    } else {
                        printTrainLog("Failed to kill process with PID " + pid);
                    }
//                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
