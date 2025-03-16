package com.games.games.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.games.games.mapper.*;
import com.games.games.pojo.*;
import com.games.games.utils.FileUtils;
import com.games.games.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TrainServiceImpl implements TrainService {
    // 用于存储子线程的映射
    private final ConcurrentHashMap<String, Thread> processMap = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();
    @Autowired
    private TrainMapper trainMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private TrainLogMapper trainLogMapper;
    @Autowired
    private ExceptionLogMapper exceptionLogMapper;
    @Autowired
    private ModelPthMapper modelPthMapper;
    @Value("${server.env}")
    private String  envName;
    private int runStatus = 1;
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
                System.out.println(line);
                // 检查是否包含 processId
                if (line.contains(processId)) {
                    System.out.println(line);
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

//    @Override
//    public Map<String, String> addTrain(MultiValueMap<String, String> data) {
//        String trainingName_pre = data.getFirst("trainingName");
//        long currentTimeMillis = System.currentTimeMillis(); // 获取当前的时间戳
//        Date currentDate = new Date(currentTimeMillis); // 将时间戳转换为Date对象
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // 定义日期格式
//        String formattedDate = dateFormat.format(currentDate); // 格式化日期
//        String trainingName = trainingName_pre + "_" + formattedDate;
//        String model = data.getFirst("model");
//        String pytorchVersion = data.getFirst("pytorchVersion");
//        String modelParams = data.getFirst("modelParams");
//        String scene = data.getFirst("scene");
//        File projectPath = new File(System.getProperty("user.dir"), "/python");
//        if (!projectPath.exists()) {
//            if (projectPath.mkdirs()) {
//                System.out.println("目录创建成功：" + projectPath.getAbsolutePath());
//            } else {
//                System.err.println("目录创建失败！");
//            }
//        }
//        File tensorboardDir = new File(projectPath.getPath(), "/logs");
//        File checkpointDir = new File(projectPath.getPath(), "/chekcpoint");
//        if (!tensorboardDir.exists()) {
//            if (tensorboardDir.mkdirs()) {
//                System.out.println("目录创建成功：" + tensorboardDir.getAbsolutePath());
//            } else {
//                System.err.println("目录创建失败！");
//            }
//        }
//        if (!checkpointDir.exists()) {
//            if (checkpointDir.mkdirs()) {
//                System.out.println("目录创建成功：" + checkpointDir.getAbsolutePath());
//            } else {
//                System.err.println("目录创建失败！");
//            }
//        }
//
//        File checkpointpathcls = new File(projectPath, "checkpoint/" + trainingName + "_" + scene + ".pth");
//        File tensorboardpathcls = new File(projectPath, "logs/" + trainingName + "_" + scene);
//        // 如果需要字符串形式的路径，可以调用 getPath()
//        String checkpointpath = checkpointpathcls.getPath();
//        String tensorboardpath = tensorboardpathcls.getPath();
//        String ip = data.getFirst("ip");
//        String port = data.getFirst("port");
//        Integer uId = Integer.parseInt(data.getFirst("uId"));
//        String userName = data.getFirst("userName");
//
//        QueryWrapper<Model> queryWrapper = new QueryWrapper<Model>();
//        Model modelTrain = modelMapper.selectOne(queryWrapper.eq("name", model));
//        String processId = UUID.randomUUID().toString();
//        String code = modelTrain.getCode();
//        // 创建训练脚本保存路径
//        File trainFile = new File(projectPath, "train.py");
//        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
//            new FileOutputStream(trainFile), StandardCharsets.UTF_8))) {
//            writer.write(code);
//        } catch (IOException e) {
//            Map<String, String> errorMap = new HashMap<>();
//            errorMap.put("error_message", "保存代码文件失败: " + e.getMessage());
//            return errorMap;
//        }
//
//        String params = data.getFirst("params");
//
//        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
//        String paramsJson = isWindows ? params.replace("\"", "\\\"") : params.replace("'", "\\'");
//        String quote = isWindows ? "\"" : "'";
//
//        Map<String, String> result = new HashMap<>();
//        Train train = new Train(null, trainingName, pytorchVersion, scene, model, modelParams, checkpointpath, 1, tensorboardpath, uId, 3, ip, port, processId, trainFile.getPath(), paramsJson);
//        trainMapper.insert(train);
//        System.out.println("Conda EvnName : " + envName);
//
//        Thread trainingThread = new Thread(() -> {
//            try {
//                String[] command;
//                if (System.getProperty("os.name").toLowerCase().contains("win")) {
//                    // Windows 平台
//                    command = new String[]{
//                            "cmd.exe", "/c",
//                            "conda activate " + envName + " && python -u " + trainFile.getPath() +
//                                    " --process_id " + processId +
//                                    " --tensorboardpath " + tensorboardpath +
//                                    " --params " + quote + paramsJson + quote
//                    };
//                } else {
//                    // Linux 平台
//                    command = new String[]{
//                            "/bin/bash", "-c",
//                            "source ~/anaconda3/etc/profile.d/conda.sh && conda activate " + envName + " && python -u " + trainFile.getPath() +
//                                    " --process_id " + processId + " --tensorboardpath " + tensorboardpath + " --params " + quote + paramsJson + quote
//                    };
//                }
//
//                ProcessBuilder processBuilder = new ProcessBuilder(command);
////                processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出
//                Process pythonProcess = processBuilder.start();
//
//                System.out.println("运行python程序 processId : " + processId);
//
//                new Thread(() -> {
//                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            System.out.println("Python 输出: " + line); // 仅用于调试
////                            System.out.println("read test ");
//                            TrainLog trainLog = new TrainLog(null, userName, trainingName, line, new Date());
//                            trainLogMapper.insert(trainLog);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }).start();
//
//                new Thread(() -> {
//                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()))) {
//                        String line;
//                        while ((line = errorReader.readLine()) != null) {
//                            System.out.println("Python 错误: " + line); // 仅用于调试
////                            System.out.println("插入数据库");
//                            try {
//                                // 插入数据库操作
//                                ExceptionLog exceptionLog = new ExceptionLog(null, userName, line, new Date());
//                                exceptionLogMapper.insert(exceptionLog);
////                                System.out.println("插入数据库成功");
//                            } catch (Exception e) {
//                                System.err.println("插入数据库失败: " + e.getMessage());
//                                e.printStackTrace(); // 打印具体的异常
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }).start();
//
//                try {
//                    // 等待 Python 进程结束
//                    int exitCode = pythonProcess.waitFor(); // 阻塞直到进程结束
//                    // 进程结束后执行数据库操作
//                    if (exitCode == 0) {
//                        // 成功执行 Python 进程
//                        System.out.println("Python 进程成功结束，开始执行数据库操作...");
//                        UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
//                        updateWrapper.eq("trainingname", trainingName).set("running", 0);
//                        boolean isUpdated = trainMapper.update(null, updateWrapper) > 0;
//                        if (isUpdated) {
//                            System.out.println("成功更新训练进程状态");
//                        } else {
//                            System.out.println("未成功更新训练进程状态");
//                        }
//                    } else {
//                        // Python 进程异常结束
//                        System.out.println("Python 进程异常结束，退出代码: " + exitCode);
//                        System.out.println("执行数据库操作");
//
//                        UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
//                        updateWrapper.eq("trainingname", trainingName).set("running", runStatus);
//                        boolean isUpdated = trainMapper.update(null, updateWrapper) > 0;
//                        if (isUpdated) {
//                            System.out.println("Exception : 成功更新训练进程状态");
//                        } else {
//                            System.out.println("Exception : 未成功更新训练进程状态");
//                        }
//                    }
//                } catch (InterruptedException e) {
//                    // 捕获线程中断异常
//                    Thread.currentThread().interrupt(); // 恢复中断状态
//                    System.out.println("进程等待被中断");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            finally {
//                processMap.remove(processId); // 从 map 中移除进程
//            }
//        });
//
//        processMap.put(processId, trainingThread);
//        trainingThread.start();
//
//        Map<String, String> response = new HashMap<>();
//
//        response.put("status", "success");
//        response.put("processId", processId);
//        response.put("trainningName", trainingName);
//        return response;
//    }
    public void saveCheckpoint(String trainingName, String scene, int trainId,  byte[] modelCheckpoint) throws IOException {
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
            System.out.println("目录创建成功：" + projectPath.getAbsolutePath());
        }

        File tensorboardDir = new File(projectPath.getPath(), "/logs");
        File checkpointDir = new File(projectPath.getPath(), "/checkpoint");
        if (!tensorboardDir.exists()) {
            if (tensorboardDir.mkdirs()) {
                System.out.println("目录创建成功：" + tensorboardDir.getAbsolutePath());
            } else {
                System.err.println("目录创建失败！");
            }
        }
        if (!checkpointDir.exists()) {
            if (checkpointDir.mkdirs()) {
                System.out.println("目录创建成功：" + checkpointDir.getAbsolutePath());
            } else {
                System.err.println("目录创建失败！");
            }
        }

        File checkpointFile = new File(projectPath, "checkpoint/" + trainingName + "_" + scene + ".pth");
        File tensorboardFile = new File(projectPath, "logs/" + trainingName + "_" + scene);
        String checkpointPath = checkpointFile.getPath();
        String tensorboardPath = tensorboardFile.getPath();

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
            return response;
        }

        // 处理参数
        String params = data.getFirst("params");
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String paramsJson = isWindows ? params.replace("\"", "\\\"") : params.replace("'", "\\'");
        String quote = isWindows ? "\"" : "'";

        System.out.println("Conda Env Name: " + envName);

        try {
            String[] command;
            if (isWindows) {
                command = new String[]{
                        "cmd.exe", "/c",
                        "conda activate " + envName + " && python -u " + trainFile.getPath() + " --checkpointpath " + checkpointPath +
                                " --tensorboardpath " + tensorboardPath + " --params " + quote + paramsJson + quote
                };
            } else {
                command = new String[]{
                        "/bin/bash", "-c",
                        "source ~/anaconda3/etc/profile.d/conda.sh && conda activate " + envName +
                                " && python -u " + trainFile.getPath() + " --checkpointpath " + checkpointPath +
                                " --tensorboardpath " + tensorboardPath + " --params " + quote + paramsJson + quote
                };
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process pythonProcess = processBuilder.start();

            System.out.println("Python 训练进程启动，等待 PID 输出...");

            final String[] processIdHolder = {null};

            // 读取 stdout 和 PID
            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Python 输出: " + line);

                        // 获取 PID
                        if (line.startsWith("PID :")) {
                            synchronized (processIdHolder) {
                                System.out.println("want get pid !!!!!");
                                processIdHolder[0] = line.replace("PID :", "").trim();
                                System.out.println("PID : " + processIdHolder[0]);
                            }
                        }

                        TrainLog trainLog = new TrainLog(null, userName, trainingName, line, new Date());
                        trainLogMapper.insert(trainLog);
                    }
                } catch (IOException e) {
                    System.err.println("读取标准输出时出错: " + e.getMessage());
                }
            });

            // 读取 stderr
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("Python 错误: " + line);
                        try {
                            ExceptionLog exceptionLog = new ExceptionLog(null, userName, line, new Date());
                            exceptionLogMapper.insert(exceptionLog);
                        } catch (Exception e) {
                            System.err.println("插入数据库失败: " + e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    System.err.println("读取错误输出时出错: " + e.getMessage());
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
            System.out.println("Python 进程 PID 获取成功: " + processId);

            // 记录到数据库
            Train train = new Train(null, trainingName, pytorchVersion, scene, model, modelParams, checkpointPath, 1, tensorboardPath, uId, 3, ip, port, processId, trainFile.getPath(), paramsJson);
            trainMapper.insert(train);

            // 监听进程状态
            Thread trainingThread = new Thread(() -> {
                try {
                    int exitCode = pythonProcess.waitFor();
                    System.out.println("Python 进程退出，代码: " + exitCode);
                    UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("trainingname", trainingName).set("running", exitCode == 0 ? 0 : runStatus);
                    if (trainMapper.update(null, updateWrapper) > 0) {
                        if (runStatus == 0 || exitCode == 0) {
                            Train train1 = trainMapper.selectOne(new QueryWrapper<Train>().eq("trainingname", trainingName));
                            byte[] modelCheckpoint = FileUtils.readFileToByteArray(checkpointPath);
                            saveCheckpoint(trainingName, scene, train1.getId(), modelCheckpoint);
                            System.out.println("保存 checkpoint 文件...");
                        }
                        System.out.println("成功更新训练进程状态");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    processMap.remove(processId);
                }
            });

            processMap.put(processId, trainingThread);
            trainingThread.start();

            Map<String, String> response = new HashMap<>();
            response.put("message", "success");
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
            System.err.println("终止进程失败: " + e.getMessage());
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
                System.out.println("Exception : 成功更新训练进程状态");
            } else {
                System.out.println("Exception : 未成功更新训练进程状态");
            }
            response.put("status", "error");
            response.put("message", "Invalid processId");
            return response;
        }


        // 获取存储的进程
        Thread thread = processMap.get(processId);
        String processPid = getPythonProcessId(processId);

        // 如果找不到 Python 进程的 PID，返回错误
        if (processPid == null) {
            // 暂停训练时终止训练
//            System.out.println("!!!!!!!!!!!!!!!!!!!!找不到对应线程");
            response.put("status", "error");
            response.put("message", "Could not find Python process");
            return response;
        }
        lock.lock();
        try {
            runStatus = 0;
            boolean killed = terminateProcess(processPid);
            if (!killed) {
                response.put("status", "error");
                response.put("message", "Failed to terminate process");
                return response;
            }
//
            // **等待 `addTrain` 里的线程执行完毕**
            System.out.println("等待训练线程执行完数据库更新...");
            thread.join();  // **等待该线程执行完毕**
            System.out.println("训练线程已结束，数据库更新完成");
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
        String processPid = getPythonProcessId(processId);

        // 如果找不到 Python 进程的 PID，返回错误
        if (processPid == null) {
            response.put("status", "error");
            response.put("message", "Could not find Python process");
            return response;
        }
        lock.lock();
        try {
            runStatus = 2;
            boolean killed = terminateProcess(processPid);
            if (!killed) {
                response.put("status", "error");
                response.put("message", "Failed to terminate process");
                return response;
            }
//
            // **等待 `addTrain` 里的线程执行完毕**
            System.out.println("等待训练线程执行完数据库更新...");
            thread.join();  // **等待该线程执行完毕**
            System.out.println("训练线程已结束，数据库更新完成");
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
        Map<String, String> response = new HashMap<>();
        String processId = data.getFirst("processId");
        String trainingName = data.getFirst("trainingName");
        String tensorboardpath = data.getFirst("tensorboardpath");
        Train train = trainMapper.selectOne(new QueryWrapper<Train>().eq("trainingname", trainingName));
        String trainPyPath = train.getTrainpypath();
        String paramsJson = train.getParams();
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String quote = isWindows ? "\"" : "'";

        try {
            String[] command;
            if (isWindows) {
                command = new String[]{
                        "cmd.exe", "/c",
                        "conda activate " + envName + " && python -u " + trainPyPath + " --checkpointpath " + train.getCheckpointpath() +
                                " --tensorboardpath " + tensorboardpath + " --params " + quote + paramsJson + quote
                };
            } else {
                command = new String[]{
                        "/bin/bash", "-c",
                        "source ~/anaconda3/etc/profile.d/conda.sh && conda activate " + envName +
                                " && python -u " + trainPyPath + " --checkpointpath " + train.getCheckpointpath() +
                                " --tensorboardpath " + tensorboardpath + " --params " + quote + paramsJson + quote
                };
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process pythonProcess = processBuilder.start();

            System.out.println("Python 训练进程启动，等待 PID 输出...");

            final String[] processIdHolder = {null};
            // 读取 stdout 和 PID
            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Python 输出: " + line);

                        // 获取 PID
                        if (line.startsWith("PID :")) {
                            synchronized (processIdHolder) {
                                processIdHolder[0] = line.replace("PID :", "").trim();
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("读取标准输出时出错: " + e.getMessage());
                }
            });

            // 读取 stderr
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("Python 错误: " + line);
                    }
                } catch (IOException e) {
                    System.err.println("读取错误输出时出错: " + e.getMessage());
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
                response = new HashMap<>();
                response.put("message", "保存代码文件失败: " + "无法获取 Python 进程的 PID");
                return response;
            }

            processId = processIdHolder[0];
            System.out.println("Python 进程 PID 获取成功: " + processId);
            UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("trainingname", trainingName).set("running", 1).set("processid", processId);
            AtomicBoolean isUpdated = new AtomicBoolean(trainMapper.update(null, updateWrapper) > 0);
            if (isUpdated.get()) {
                System.out.println("Exception : 成功更新训练进程状态");
            } else {
                System.out.println("Exception : 未成功更新训练进程状态");
            }
            // 监听进程状态
            String finalProcessId = processId;
            Thread trainingThread = new Thread(() -> {
                try {
                    int exitCode = pythonProcess.waitFor();
                    System.out.println("Python 进程退出，代码: " + exitCode);

                    updateWrapper.eq("trainingname", trainingName).set("running", exitCode == 0 ? 0 : runStatus);
                    if (trainMapper.update(null, updateWrapper) > 0) {
                        System.out.println("成功更新训练进程状态");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    processMap.remove(finalProcessId);
                }
            });

            processMap.put(processId, trainingThread);
            trainingThread.start();

            response = new HashMap<>();
            response.put("message", "success");
            response.put("processId", processId);
            response.put("trainingName", trainingName);
            return response;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            response = new HashMap<>();
            response.put("message", "启动 Python 进程失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, String> addTensorboard(MultiValueMap<String, String> data) {
        Map<String, String> result = new HashMap<>();
        String tensorboardpath = data.getFirst("tensorboardpath");
        System.out.println("tensorboardpath : " + tensorboardpath);
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
        System.out.println("tensorboard port : " + finalPort);
        new Thread(() -> {
            try {
                // 构建 TensorBoard 命令
                String[] command;
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    // Windows 下使用 cmd.exe
                    command = new String[]{
                            "cmd.exe", "/c",
                            "conda activate " + envName + " && tensorboard --logdir=" + tensorboardpath + " --port=" + finalPort
                    };
                } else {
                    // Linux 下使用 /bin/bash
                    command = new String[]{
                            "/bin/bash", "-c",
                            "source ~/anaconda3/etc/profile.d/conda.sh && conda activate " + envName + " && tensorboard --logdir=" + tensorboardpath + " --port=" + finalPort
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
        System.out.println("want to kill port : " + port);
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
                System.out.println("line is: " + line);
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
                        System.out.println("Successfully killed process with PID " + pid);
                    } else {
                        System.out.println("Failed to kill process with PID " + pid);
                    }
//                    break;
                }
            }
//            System.out.println("TensorBoard PID using port 6001 is: " + pid);

//            if (pid != null && !pid.isEmpty()) {
//                ProcessBuilder killProcessBuilder;
//                if (os.contains("win")) {
//                    // Windows 使用 taskkill 杀死进程
//                    killProcessBuilder = new ProcessBuilder("cmd.exe", "/c", "taskkill /PID " + pid + " /F");
//                } else {
//                    // Linux 使用 kill 命令
//                    killProcessBuilder = new ProcessBuilder("/bin/bash", "-c", "kill -9 " + pid);
//                }
//
//                Process killProcess = killProcessBuilder.start();
//                int exitCode = killProcess.waitFor();
//                if (exitCode == 0) {
//                    System.out.println("Successfully killed process with PID " + pid);
//                } else {
//                    System.out.println("Failed to kill process with PID " + pid);
//                }
//            } else {
//                System.out.println("No process found on port " + port);
//            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
