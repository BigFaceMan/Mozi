package org.example.backend.service.impl.infer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.example.backend.mapper.InferMapper;
import org.example.backend.mapper.TrainMapper;
import org.example.backend.pojo.Infer;
import org.example.backend.pojo.Train;
import org.example.backend.pojo.User;
import org.example.backend.service.impl.utils.UserDetailsImpl;
import org.example.backend.service.infer.InferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class InferServiceImpl implements InferService {
    @Autowired
    private InferMapper inferMapper;

    private Process tensorBoardProcess = null;
    private static final ConcurrentHashMap<String, Thread> runningThreads = new ConcurrentHashMap<>();
    @Override
    public Map<String, String> addInfer(MultiValueMap<String, String> data) {
        String inferName = data.getFirst("inferName");
        String scene = data.getFirst("scene");
        String model = data.getFirst("model");
//        Integer scene = Integer.parseInt(data.getFirst("scene"));
        String projectPath = System.getProperty("user.dir") + "\\src\\main\\python\\";
        String tensorboardpath = projectPath + File.separator + "logs\\" + inferName + "_" + scene;
        Map<String, String> result = new HashMap<>();

        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();

        Infer infer = new Infer(null, inferName, scene, model, 1, tensorboardpath, user.getId());
        inferMapper.insert(infer);
        putInfer(data);
        result.put( "error_message", "success");
        return result;
    }

    @Override
    public void putInfer(MultiValueMap<String, String> data) {
        String inferName = data.getFirst("inferName");
        String scene = data.getFirst("scene");
        String model = data.getFirst("model");
        String projectPath = System.getProperty("user.dir") + "\\src\\main\\python\\";
        String tensorboardpath = projectPath + File.separator + "logs\\" + inferName + "_" + scene;
        Thread trainingThread = new Thread(() -> {
            Process trainingProcess = null;
            boolean isInterrupted = false; // 中断标志
            try {
                String[] command = {
                        "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                        "conda activate ssp && python C:\\Users\\Lenovo\\Desktop\\research\\kaifa\\202411\\backend\\src\\main\\python\\infer.py --tensorboardpath " + tensorboardpath
                };

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出
                trainingProcess = processBuilder.start();

                // 等待进程执行完毕，检查中断状态
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (trainingProcess.waitFor(1, TimeUnit.SECONDS)) {
                            break; // 进程正常结束，退出
                        }
                    } catch (InterruptedException e) {
                        // 捕获到 InterruptedException 后，退出等待并销毁进程
                        Thread.currentThread().interrupt();  // 恢复中断状态
                        trainingProcess.destroy();  // 销毁进程
                        System.out.println("线程被中断，进程已被销毁");
                        isInterrupted = true; // 设置标志为 true
                        return;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 更新数据库状态
                if (!isInterrupted) {
                    UpdateWrapper<Infer> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("infername", inferName).set("running", 0);
                    boolean isUpdated = inferMapper.update(null, updateWrapper) > 0;
                    if (isUpdated) {
                        System.out.println("成功更新推断进程状态");
                    } else {
                        System.out.println("未成功更新推断进程状态");
                    }
                } else {
                    System.out.println("线程被中断，跳过数据库更新");
                }
            }
        });
        // 保存线程引用到Map中
        runningThreads.put(inferName, trainingThread);
        // 启动线程
        trainingThread.start();
    }

    @Override
    public Map<String, String> stopInfer(MultiValueMap<String, String> data) {
        String inferName = data.getFirst("inferName");

//        System.out.println("Threads : ");
//        System.out.println(runningThreads.size());
//        System.out.println(runningThreads.keys());
//        System.out.println(inferName);
        Thread trainingThread = runningThreads.get(inferName);

//        System.out.println("infer ai here !!!!!!!!!!!!!!!!!!");
        Map<String, String> result = new HashMap<>();
        if (trainingThread != null) {
            // 需要实现线程终止的逻辑，例如通过发送中断信号
            trainingThread.interrupt();
            runningThreads.remove(inferName);
            System.out.println("训练已终止: " + inferName);
            UpdateWrapper<Infer> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("infername", inferName).set("running", 2);
            boolean isUpdated = inferMapper.update(null, updateWrapper) > 0;
            if (isUpdated) {
                System.out.println("成功更新训练进程状态");
                result.put( "error_message", "成功更新训练进程状态");
            } else {
                System.out.println("未成功更新训练进程状态");
                result.put( "error_message", "未成功更新训练进程状态");
            }
        } else {
            System.out.println("未找到训练任务: " + inferName);
        }
        return result;
    }

    @Override
    public Map<String, String> killInfer(MultiValueMap<String, String> data) {
        Map<String, String> result = new HashMap<>();
        String inferName = data.getFirst("inferName");
        UpdateWrapper<Infer> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("infername", inferName).set("running", 0);
        boolean isUpdated = inferMapper.update(null, updateWrapper) > 0;
        if (isUpdated) {
            System.out.println("成功更新训练进程状态");
            result.put( "error_message", "成功更新训练进程状态");
        } else {
            System.out.println("未成功更新训练进程状态");
            result.put( "error_message", "未成功更新训练进程状态");
        }
        return result;
    }
    @Override
    public Map<String, String> continueInfer(MultiValueMap<String, String> data) {
        Map<String, String> result = new HashMap<>();
        String inferName = data.getFirst("inferName");
        UpdateWrapper<Infer> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("infername", inferName).set("running", 1);
        boolean isUpdated = inferMapper.update(null, updateWrapper) > 0;
        if (isUpdated) {
            System.out.println("成功更新训练进程状态");
            result.put( "error_message", "成功更新训练进程状态");
        } else {
            System.out.println("未成功更新训练进程状态");
            result.put( "error_message", "未成功更新训练进程状态");
        }
        return result;
    }
    @Override
    public Map<String, String> addTensorboard(MultiValueMap<String, String> data) {
        Map<String, String> result = new HashMap<>();
        String tensorboardpath = data.getFirst("tensorboardpath");

        // 启动一个线程来执行 TensorBoard 命令
        new Thread(() -> {
            try {
                // 构建 TensorBoard 命令

                String[] command = {
                        "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                        "conda activate ssp && tensorboard --logdir=" + tensorboardpath
                };

                // 使用 ProcessBuilder 启动 TensorBoard
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);  // 合并标准输出和错误输出
                tensorBoardProcess = processBuilder.start();  // 启动进程并保存引用

                // 等待 TensorBoard 执行
                tensorBoardProcess.waitFor();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        result.put("error_message", "success");
        return result;
    }

    // killLog函数，终止 TensorBoard 进程
    @Override
    public Map<String, String> deleteTensorboard() {
        Map<String, String> result = new HashMap<>();
        if (tensorBoardProcess != null) {
            tensorBoardProcess.destroy();  // 销毁进程
            result.put("error_message", "TensorBoard process killed successfully");
        } else {
            result.put("error_message", "No running TensorBoard process found");
        }
        return result;
    }

    @Override
    public List<Infer> getList() {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();
        List<Infer> trainList;
        if (user.getUrank().equals("0")) {
            System.out.println("根据当前uid来查找对应的推理信息");
            QueryWrapper<Infer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", user.getId());  // 过滤出 uid 等于 user.getId() 的记录
            trainList = inferMapper.selectList(queryWrapper);
        } else {
            trainList = inferMapper.selectList(null);
        }
        Collections.reverse(trainList);  // 反转列表
        return trainList;
    }

    @Override
    public Map<String, String> removeInfer(Map<String, String> data) {
        Map<String, String> result = new HashMap<>();
        int id = Integer.parseInt(data.get("id"));

        int rows = inferMapper.deleteById(id);
        result.put("status", rows > 0 ? "success" : "failure");
        return result;
    }
}
