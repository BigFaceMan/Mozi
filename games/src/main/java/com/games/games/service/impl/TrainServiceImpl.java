package com.games.games.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.games.games.mapper.*;
import com.games.games.pojo.ExceptionLog;
import com.games.games.pojo.Model;
import com.games.games.pojo.Train;
import com.games.games.pojo.TrainLog;
import com.games.games.service.TrainService;
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
    private int runStatus = 1;
    private List<Integer> allowPorts = new ArrayList<>(Collections.nCopies(10, 0));

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
    public Map<String, String> addTrain(MultiValueMap<String, String> data) {
        String trainingName_pre = data.getFirst("trainingName");
        long currentTimeMillis = System.currentTimeMillis(); // 获取当前的时间戳
        Date currentDate = new Date(currentTimeMillis); // 将时间戳转换为Date对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // 定义日期格式
        String formattedDate = dateFormat.format(currentDate); // 格式化日期
        String trainingName = trainingName_pre + "_" + formattedDate;
        String model = data.getFirst("model");
        String pytorchVersion = data.getFirst("pytorchVersion");
        String modelParams = data.getFirst("modelParams");
        String scene = data.getFirst("scene");
        File projectPath = new File(System.getProperty("user.dir"), "src/main/python");
        File checkpointpathcls = new File(projectPath, "checkpoint/" + trainingName + "_" + scene + ".pth");
        File tensorboardpathcls = new File(projectPath, "logs/" + trainingName + "_" + scene);
        // 如果需要字符串形式的路径，可以调用 getPath()
        String checkpointpath = checkpointpathcls.getPath();
        String tensorboardpath = tensorboardpathcls.getPath();
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        Integer uId = Integer.parseInt(data.getFirst("uId"));
        String userName = data.getFirst("userName");

        QueryWrapper<Model> queryWrapper = new QueryWrapper<Model>();
        Model modelTrain = modelMapper.selectOne(queryWrapper.eq("name", model));
        String processId = UUID.randomUUID().toString();
        String code = modelTrain.getCode();
        // 创建训练脚本保存路径
        File trainFile = new File(projectPath, "train.py");

        // 将代码写入 train.py 文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(trainFile))) {
            writer.write(code);
        } catch (IOException e) {
            // 处理写入文件时的异常
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error_message", "保存代码文件失败: " + e.getMessage());
            return errorMap;
        }


        Map<String, String> result = new HashMap<>();
        Train train = new Train(null, trainingName, pytorchVersion, scene, model, modelParams, checkpointpath, 1, tensorboardpath, uId, 3, ip, port, processId, trainFile.getPath());
        trainMapper.insert(train);

        Thread trainingThread = new Thread(() -> {
            try {
                String[] command = {
                    "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                    "conda activate ssp &&python " + " -u " + trainFile.getPath() +  " --process_id " + processId + " --tensorboardpath " + tensorboardpath
                };

                ProcessBuilder processBuilder = new ProcessBuilder(command);
//                processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出
                Process pythonProcess = processBuilder.start();

                System.out.println("运行python程序 processId : " + processId);

                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("Python 输出: " + line); // 仅用于调试
//                            System.out.println("read test ");
                            TrainLog trainLog = new TrainLog(null, userName, trainingName, line, new Date());
                            trainLogMapper.insert(trainLog);
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
//                                System.out.println("插入数据库成功");
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
                        UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("trainingname", trainingName).set("running", 0);
                        boolean isUpdated = trainMapper.update(null, updateWrapper) > 0;
                        if (isUpdated) {
                            System.out.println("成功更新训练进程状态");
                        } else {
                            System.out.println("未成功更新训练进程状态");
                        }
                    } else {
                        // Python 进程异常结束
                        System.out.println("Python 进程异常结束，退出代码: " + exitCode);
                        System.out.println("执行数据库操作");

                        UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("trainingname", trainingName).set("running", runStatus);
                        boolean isUpdated = trainMapper.update(null, updateWrapper) > 0;
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

        processMap.put(processId, trainingThread);
        trainingThread.start();

        Map<String, String> response = new HashMap<>();

        response.put("status", "success");
        response.put("processId", processId);
        response.put("trainningName", trainingName);
        return response;
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
    public Map<String, String> continueTrain(MultiValueMap<String, String> data) throws IOException {
        Map<String, String> response = new HashMap<>();
        String processId = data.getFirst("processId");
        String trainingName = data.getFirst("trainingName");
        String tensorboardpath = data.getFirst("tensorboardpath");
        Train train = trainMapper.selectOne(new QueryWrapper<Train>().eq("trainingname", trainingName));
        String trainPyPath = train.getTrainpypath();
//        long currentTimeMillis = System.currentTimeMillis(); // 获取当前的时间戳
//        Date currentDate = new Date(currentTimeMillis); // 将时间戳转换为Date对象
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // 定义日期格式
//        String formattedDate = dateFormat.format(currentDate); // 格式化日期
//        String model = data.getFirst("model");
//        String pytorchVersion = data.getFirst("pytorchVersion");
//        String modelParams = data.getFirst("modelParams");
//        String scene = data.getFirst("scene");
//        File projectPath = new File(System.getProperty("user.dir"), "src/main/python");
//        File checkpointpathcls = new File(projectPath, "checkpoint/" + trainingName + "_" + scene + ".pth");
//        File tensorboardpathcls = new File(projectPath, "logs/" + trainingName + "_" + scene);
//        // 如果需要字符串形式的路径，可以调用 getPath()
//        String checkpointpath = checkpointpathcls.getPath();
//        String tensorboardpath = tensorboardpathcls.getPath();
//        String ip = data.getFirst("ip");
//        String port = data.getFirst("port");
//        Integer uId = Integer.parseInt(data.getFirst("uId"));


        UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("trainingname", trainingName).set("running", 1);
        AtomicBoolean isUpdated = new AtomicBoolean(trainMapper.update(null, updateWrapper) > 0);
        if (isUpdated.get()) {
            System.out.println("Exception : 成功更新训练进程状态");
        } else {
            System.out.println("Exception : 未成功更新训练进程状态");
        }
//        Train train = new Train(null, trainingName, pytorchVersion, scene, model, modelParams, checkpointpath, 1, tensorboardpath, uId, 3, ip, port, processId);
//        trainMapper.insert(train);

        Thread trainingThread = new Thread(() -> {
            try {
                String[] command = {
                        "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                        "conda activate ssp &&python " + trainPyPath + " --process_id " + processId + " --tensorboardpath " + tensorboardpath
                };

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出
                Process pythonProcess = processBuilder.start();
                try {
                    System.out.println("运行python程序 processId : " + processId);
                    // 等待 Python 进程结束
                    int exitCode = pythonProcess.waitFor(); // 阻塞直到进程结束
                    // 进程结束后执行数据库操作
                    if (exitCode == 0) {
                        // 成功执行 Python 进程
                        System.out.println("Python 进程成功结束，开始执行数据库操作...");
                        updateWrapper.eq("trainingname", trainingName).set("running", 0);
                        isUpdated.set(trainMapper.update(null, updateWrapper) > 0);
                        if (isUpdated.get()) {
                            System.out.println("成功更新训练进程状态");
                        } else {
                            System.out.println("未成功更新训练进程状态");
                        }
                    } else {
                        // Python 进程异常结束
                        System.out.println("Python 进程异常结束，退出代码: " + exitCode);
                        System.out.println("执行数据库操作");

                        updateWrapper.eq("trainingname", trainingName).set("running", runStatus);
                        isUpdated.set(trainMapper.update(null, updateWrapper) > 0);
                        if (isUpdated.get()) {
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

        processMap.put(processId, trainingThread);
        trainingThread.start();


        response.put("status", "success");
        response.put("processId", processId);
        response.put("trainningName", trainingName);
        return response;
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
                String[] command = {
                        "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                        "conda activate ssp && tensorboard --logdir=" + tensorboardpath + " --port=" + finalPort
                };

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
        killProcessByPort(port);
        HashMap<String, String> result = new HashMap<>();
        result.put("status", "success");
        return result;
    }

    public void killProcessByPort(int port) {
        try {
            // 查找占用该端口的进程 PID
            String[] netstatCommand = {
                    "cmd.exe", "/c", "netstat -ano | findstr :" + port
            };
            ProcessBuilder netstatProcessBuilder = new ProcessBuilder(netstatCommand);
            Process netstatProcess = netstatProcessBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(netstatProcess.getInputStream()));

            String line;
            String pid = null;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("LISTENING")) {
                    continue;
                }
                // netstat 输出格式: TCP    0.0.0.0:6001             0.0.0.0:0              LISTENING       12345
                // 这里提取 PID
                System.out.println("line is : " + line);
                String[] parts = line.trim().split("\\s+");
                pid = parts[parts.length - 1];
            }
            System.out.println("pid is : " + pid);

            if (pid != null) {
                // 通过 PID 结束进程
                String[] killCommand = {"cmd.exe", "/c", "taskkill /PID " + pid + " /F"};
                ProcessBuilder killProcessBuilder = new ProcessBuilder(killCommand);
                Process killProcess = killProcessBuilder.start();
                int exitCode = killProcess.waitFor();
                if (exitCode == 0) {
                    System.out.println("Successfully killed process with PID " + pid);
                } else {
                    System.out.println("Failed to kill process with PID " + pid);
                }
            } else {
                System.out.println("No process found on port " + port);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
