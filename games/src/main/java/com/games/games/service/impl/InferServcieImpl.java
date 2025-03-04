package com.games.games.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.games.games.mapper.*;
import com.games.games.pojo.*;
import com.games.games.service.InferServcie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class InferServcieImpl implements InferServcie {
    // 用于存储子线程的映射
    private final ConcurrentHashMap<String, Thread> processMap = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private TrainMapper trainMapper;
    @Autowired
    private ExceptionLogMapper exceptionLogMapper;
    @Autowired
    private InferMapper inferMapper;
    @Autowired
    private InferLogMapper inferLogMapper;
    private int runStatus = 0;
    private static String getPythonProcessId(String processId) throws IOException {
//        System.out.println("processId " + processId);
        String pid = null;
        String line;
        // 使用 wmic 获取进程命令行信息
        ProcessBuilder builder = new ProcessBuilder("wmic", "process", "where", "\"commandline like 'python%'\"", "get", "ProcessId,CommandLine");
        Process wmicProcess = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(wmicProcess.getInputStream()))) {
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                // 检查是否包含传递的 processId
                if (line.contains(processId)) {
                    System.out.println(line);
                    // 处理 wmic 输出，获取 PID
                    String[] columns = line.split("\\s+");
                    if (columns.length > 1) {
                        pid = columns[columns.length-1]; // 获取 ProcessId
                        break;
                    }
                }
            }
        }
        return pid;
    }
    @Override
    public Map<String, String> addInfer(MultiValueMap<String, String> data) {
        String inferNamePre = data.getFirst("inferName");
        long currentTimeMillis = System.currentTimeMillis(); // 获取当前的时间戳
        Date currentDate = new Date(currentTimeMillis); // 将时间戳转换为Date对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // 定义日期格式
        String formattedDate = dateFormat.format(currentDate); // 格式化日期
        String inferName = inferNamePre + "_" + formattedDate;
        String trainName = data.getFirst("model");
        String scene = data.getFirst("scene");
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        Integer uId = Integer.parseInt(data.getFirst("uId"));
        String userName = data.getFirst("userName");
        File projectPath = new File(System.getProperty("user.dir"), "src/main/python");
        File tensorboardpathcls = new File(projectPath, "logs/" + inferName + "_" + scene);

        QueryWrapper<Train> queryWrapper = new QueryWrapper<Train>();
        Train train = trainMapper.selectOne(queryWrapper.eq("trainingname", trainName));
        String checkPointPath = train.getCheckpointpath();
        String algName = train.getModel();
        QueryWrapper<Model> queryModelWrapper = new QueryWrapper<Model>();
        Model modelTrain = modelMapper.selectOne(queryModelWrapper.eq("name", algName));
        String processId = UUID.randomUUID().toString();
        String code = modelTrain.getCode();
        // 创建训练脚本保存路径
        File inferFile = new File(projectPath, "infer.py");

        // 将代码写入 train.py 文件
        File trainFile;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(inferFile))) {
            writer.write(code);
        } catch (IOException e) {
            // 处理写入文件时的异常
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error_message", "保存代码文件失败: " + e.getMessage());
            return errorMap;
        }

        Map<String, String> result = new HashMap<>();
        Infer infer = new Infer(null, inferName, scene, trainName, 1, tensorboardpathcls.getPath(), uId, ip, port, processId, inferFile.getPath(), checkPointPath);
        inferMapper.insert(infer);

        Thread inferThread = new Thread(() -> {
            try {
                String[] command = {
                        "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                        "conda activate ssp &&python " + " -u " + inferFile.getPath() +  " --process_id " + processId + " --tensorboardpath " + tensorboardpathcls.getPath() + " --checkpointpath " + checkPointPath
                };

                ProcessBuilder processBuilder = new ProcessBuilder(command);
//                processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出
                Process pythonProcess = processBuilder.start();

                System.out.println("运行python infer程序 processId : " + processId);

                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("Python Infer 输出: " + line); // 仅用于调试
//                            System.out.println("read test ");
//                            TrainLog trainLog = new TrainLog(null, userName, trainingName, line, new Date());
//                            trainLogMapper.insert(trainLog);
                            InferLog inferLog = new InferLog(null, userName, inferName, line, new Date());
                            inferLogMapper.insert(inferLog);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                new Thread(() -> {
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()))) {
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            System.out.println("Python 错误: " + line); // 仅用于调试
//                            System.out.println("插入数据库");
                            try {
                                // 插入数据库操作
                                ExceptionLog exceptionLog = new ExceptionLog(null, userName, line, new Date());
                                exceptionLogMapper.insert(exceptionLog);
                            } catch (Exception e) {
                                System.err.println("插入数据库失败: " + e.getMessage());
                                e.printStackTrace(); // 打印具体的异常
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                try {
                    // 等待 Python 进程结束
                    int exitCode = pythonProcess.waitFor(); // 阻塞直到进程结束
                    // 进程结束后执行数据库操作
                    if (exitCode == 0) {
                        // 成功执行 Python 进程
                        System.out.println("Python 进程成功结束，开始执行数据库操作...");
                        UpdateWrapper<Infer> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("infername", inferName).set("running", 0);
                        boolean isUpdated = inferMapper.update(null, updateWrapper) > 0;
                        if (isUpdated) {
                            System.out.println("成功更新训练进程状态");
                        } else {
                            System.out.println("未成功更新训练进程状态");
                        }
                    } else {
                        // Python 进程异常结束
                        System.out.println("Python 进程异常结束，退出代码: " + exitCode);
                        System.out.println("执行数据库操作");

                        UpdateWrapper<Infer> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("infername", inferName).set("running", runStatus);
                        boolean isUpdated = inferMapper.update(null, updateWrapper) > 0;
                        if (isUpdated) {
                            System.out.println("Exception : 成功更新训练进程状态");
                        } else {
                            System.out.println("Exception : 未成功更新训练进程状态");
                        }
                    }
                } catch (InterruptedException e) {
                    // 捕获线程中断异常
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    System.out.println("进程等待被中断");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                processMap.remove(processId); // 从 map 中移除进程
            }
        });

        processMap.put(processId, inferThread);
        inferThread.start();

        Map<String, String> response = new HashMap<>();

        response.put("status", "success");
        response.put("processId", processId);
        response.put("inferName", inferName);
        return response;
    }

    @Override
    public Map<String, String> killInfer(MultiValueMap<String, String> data) throws IOException {
        Map<String, String> response = new HashMap<>();
        String processId = data.getFirst("processId");
        String inferName = data.getFirst("inferName");;
        if (processId == null || !processMap.containsKey(processId)) {
            UpdateWrapper<Infer> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("infername", inferName).set("running", 0);
            boolean isUpdated = inferMapper.update(null, updateWrapper) > 0;
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
            // 在 Windows 系统上终止整个进程树
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("taskkill", "/F", "/T", "/PID", processPid).start();
            } else {
                // 在类 Unix 系统上终止整个进程树
                thread.destroy(); // 尝试优雅终止
            }

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
    public Map<String, String> stopInfer(MultiValueMap<String, String> data) throws IOException {
        Map<String, String> response = new HashMap<>();
        String processId = data.getFirst("processId");
        String inferName = data.getFirst("inferName");;
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
            // 在 Windows 系统上终止整个进程树
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("taskkill", "/F", "/T", "/PID", processPid).start();
            } else {
                // 在类 Unix 系统上终止整个进程树
                thread.destroy(); // 尝试优雅终止
            }

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
    public Map<String, String> continueInfer(MultiValueMap<String, String> data) throws IOException {
        Map<String, String> response = new HashMap<>();
        String processId = data.getFirst("processId");
        String inferName = data.getFirst("inferName");;
        String tensorBoardPath = data.getFirst("tensorboardpath");
        String checkPointPath = data.getFirst("checkpointpath");
        String userName = data.getFirst("userName");
        Infer infer = inferMapper.selectOne(new QueryWrapper<Infer>().eq("infername", inferName));
        String inferPyPath = infer.getInferpypath();

        UpdateWrapper<Infer> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("infername", inferName).set("running", 1);
        AtomicBoolean isUpdated = new AtomicBoolean(inferMapper.update(null, updateWrapper) > 0);
        if (isUpdated.get()) {
            System.out.println("Exception : 成功更新训练进程状态");
        } else {
            System.out.println("Exception : 未成功更新训练进程状态");
        }

        Thread inferThread = new Thread(() -> {
            try {
                String[] command = {
                        "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                        "conda activate ssp &&python " + inferPyPath + " --process_id " + processId + " --tensorboardpath " + tensorBoardPath + " --checkpointpath" + checkPointPath
                };

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process pythonProcess = processBuilder.start();
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("Python Infer 输出: " + line); // 仅用于调试
//                            System.out.println("read test ");
//                            TrainLog trainLog = new TrainLog(null, userName, trainingName, line, new Date());
//                            trainLogMapper.insert(trainLog);
                            InferLog inferLog = new InferLog(null, userName, inferName, line, new Date());
                            inferLogMapper.insert(inferLog);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                new Thread(() -> {
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()))) {
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            System.out.println("Python 错误: " + line); // 仅用于调试
//                            System.out.println("插入数据库");
                            try {
                                // 插入数据库操作
                                ExceptionLog exceptionLog = new ExceptionLog(null, userName, line, new Date());
                                exceptionLogMapper.insert(exceptionLog);
                            } catch (Exception e) {
                                System.err.println("插入数据库失败: " + e.getMessage());
                                e.printStackTrace(); // 打印具体的异常
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                try {
                    // 等待 Python 进程结束
                    int exitCode = pythonProcess.waitFor(); // 阻塞直到进程结束
                    // 进程结束后执行数据库操作
                    if (exitCode == 0) {
                        // 成功执行 Python 进程
                        System.out.println("Python 进程成功结束，开始执行数据库操作...");
//                        UpdateWrapper<Infer> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("infername", inferName).set("running", 0);
                        boolean isUpdatedThread = inferMapper.update(null, updateWrapper) > 0;
                        if (isUpdatedThread) {
                            System.out.println("成功更新训练进程状态");
                        } else {
                            System.out.println("未成功更新训练进程状态");
                        }
                    } else {
                        // Python 进程异常结束
                        System.out.println("Python 进程异常结束，退出代码: " + exitCode);
                        System.out.println("执行数据库操作");

//                        UpdateWrapper<Infer> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("infername", inferName).set("running", runStatus);
                        boolean isUpdatedThread = inferMapper.update(null, updateWrapper) > 0;
                        if (isUpdatedThread) {
                            System.out.println("Exception : 成功更新训练进程状态");
                        } else {
                            System.out.println("Exception : 未成功更新训练进程状态");
                        }
                    }
                } catch (InterruptedException e) {
                    // 捕获线程中断异常
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    System.out.println("进程等待被中断");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                processMap.remove(processId); // 从 map 中移除进程
            }
        });

        processMap.put(processId, inferThread);
        inferThread.start();


        response.put("status", "success");
        response.put("processId", processId);
        response.put("inferName", inferName);
        return response;
    }
}
