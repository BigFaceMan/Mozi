package org.example.backend.service.impl.model;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.example.backend.pojo.AlgParams;
import org.example.backend.mapper.ModelMapper;
import org.example.backend.mapper.AlgParamsMapper;
import org.example.backend.mapper.ModelPthMapper;
import org.example.backend.mapper.TrainMapper;
import org.example.backend.pojo.*;
import org.example.backend.service.impl.utils.UserDetailsImpl;
import org.example.backend.service.model.ModelOptService;
import org.example.backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class ModelOptServiceImpl implements ModelOptService {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ModelPthMapper modelPthMapper;
    @Autowired
    private TrainMapper trainMapper;
    @Autowired
    private AlgParamsMapper algParamsMapper;

    @Transactional
    @Override
    public Map<String, String> add(Map<String, Object> data) throws IOException {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();
        // 创建返回的错误信息 Map
        Map<String, String> map = new HashMap<>();

        // 获取数据
        String name = (String) data.get("name");
        String summary = (String) data.get("summary");
        String environment = (String) data.get("environment");
        String abilityStr = (String) data.get("ability"); // 注意这里是 String 类型，后面需要转换为 Integer
        String modelstruct = (String) data.get("modelstruct"); // 注意这里是 String 类型，后面需要转换为 Integer
        String modelselect = (String) data.get("modelselect");
        String situationselect = (String) data.get("situationselect");

        System.out.println("modelstruct : " + modelstruct);
        String structureimageBase64 = (String) data.get("structureimage"); // Base64 编码的图片
        if (structureimageBase64 != null && structureimageBase64.startsWith("data:image")) {
            // 去掉Base64前缀（如果有）
            structureimageBase64 = structureimageBase64.split(",")[1];
        }
        String code = (String) data.get("code");
//        String inferCode = (String) data.get("inferCode");

        // 检查必填字段是否为空
        if (name == null || name.trim().isEmpty()) {
            map.put("error_message", "模型名不能为空");
            return map;
        }

        if (summary == null || summary.trim().isEmpty()) {
            map.put("error_message", "模型简介不能为空");
            return map;
        }

        if (environment == null || environment.trim().isEmpty()) {
            map.put("error_message", "运行环境不能为空");
            return map;
        }

        if (abilityStr == null || abilityStr.trim().isEmpty()) {
            map.put("error_message", "模型能力不能为空");
            return map;
        }

        if (structureimageBase64 == null || structureimageBase64.trim().isEmpty()) {
            map.put("error_message", "模型结构图片不能为空");
            return map;
        }

        if (code == null) {
            System.out.println("using modelstruct generate code !!!");
            code =
                    "import torch\n" +
                            "import torch.nn as nn\n" +
                            "\n" +
                            "class Model(nn.Module):\n" +
                            "    def __init__(self, input_size, hidden_size, output_size):\n" +
                            "        super(Model, self).__init__()\n" +
                            "        self.model = nn.Sequential(\n" +
                            "            nn.Linear(input_size, hidden_size),\n" +
                            "            nn.ReLU(),\n" +
                            "            nn.Linear(hidden_size, output_size)\n" +
                            "        )\n" +
                            "\n" +
                            "    def forward(self, x):\n" +
                            "        return self.model(x)\n" +
                            "\n" +
                            "# Example instantiation\n" +
                            "# model = Model(input_size=784, hidden_size=128, output_size=10)\n";

        }

        // 转换 ability 字段为 Integer
        Integer ability = null;
        try {
            ability = Integer.parseInt(abilityStr);
        } catch (NumberFormatException e) {
            map.put("error_message", "模型能力必须是一个整数");
            return map;
        }

        // 解码 Base64 图片
        byte[] decodedImage = Base64.getDecoder().decode(structureimageBase64);

        // 生成唯一的文件名
        String fileName = UUID.randomUUID() + ".png"; // 使用 UUID 生成唯一文件名，假设图片格式为 PNG

        // 图片保存路径（假设保存到服务器的 /uploads 目录）
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator; // 你可以根据实际的文件存储路径来调整
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 如果目录不存在，创建目录
        }

        // 文件路径
        String filePath = uploadDir + fileName;

        // 将字节数组保存到文件系统
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(decodedImage);
        } catch (IOException e) {
            map.put("error_message", "图片保存失败: " + e.getMessage());
            return map;
        }

        // 将文件路径存入数据库的结构图片字段
        String structureimagePath = filePath; // 保存到数据库中的路径

        // 这里可以进行其他的逻辑处理，比如存储数据等
        // 示例代码，假设数据存储成功
        Date now = new Date();
        Model model = new Model(null, name, summary, environment, ability, structureimagePath, code, modelstruct, situationselect, modelselect, user.getId(), now, now);
        modelMapper.insert(model);

        if (data.get("modelPth") != null) {
            MultipartFile modelPth = (MultipartFile) data.get("modelPth");
            String modelPthName = name + "_loadModel";
            byte[] modelBytes = modelPth.getBytes();

            Train train = new Train(null, modelPthName,  environment, situationselect,  name,  "1", "1",  3, "1", user.getId(), 3, "1", "1", "1", "1", "1", 1, 0, 0);
            trainMapper.insert(train);
            Train trainQuery = trainMapper.selectOne(new QueryWrapper<Train>().eq("trainingname", modelPthName));
            int trainId = Math.toIntExact(trainQuery.getId());
            modelPthMapper.insert(new ModelPth(null, trainId, modelPthName, situationselect, modelBytes, new Date()));
        }

        Model newModel = modelMapper.selectOne(new QueryWrapper<Model>().eq("name", name));
        int modelId = model.getId();
        algParamsMapper.insert(new AlgParams(null, modelId, 1600, 1000, 0.01, 32));

        map.put("success_message", "success");
        return map;
    }

    @Override
    public List<Model> getList() {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();
        System.out.println("getModelList user is : " + user);
        System.out.println("user'rank : " + user.getUrank());
        List<Model> models;
        if (user.getUrank().equals("0")) {
            System.out.println("根据当前uid来查找对应的模型信息");
            QueryWrapper<Model> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", user.getId());  // 过滤出 uid 等于 user.getId() 的记录
            models = modelMapper.selectList(queryWrapper); // 假设 modelMapper 有一个方法可以查询所有模型
        } else {
            models = modelMapper.selectList(null); // 假设 modelMapper 有一个方法可以查询所有模型
        }
        // 转换图片路径为 Base64 编码
        for (Model model : models) {
            String imagePath = model.getStructureimage(); // 获取图片路径
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    File imageFile = new File(imagePath);
                    byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    model.setStructureimage("data:image/png;base64," + base64Image); // 假设图片是 PNG 格式
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return models;
    }

    @Override
    public Map<String, String> remove(Map<String, String> data) {
        Map<String, String> map = new HashMap<>();

        // 获取模型名
        String model_id = data.get("id");

        // 使用 MyBatis-Plus 查询模型
        Model model = modelMapper.selectOne(new QueryWrapper<Model>().eq("id", model_id));
        if (model == null) {
            map.put("error_message", "模型不存在");
            return map;
        }

        // 删除模型对应的文件
        String structureImagePath = model.getStructureimage();
        if (structureImagePath != null && !structureImagePath.trim().isEmpty()) {
            File file = new File(structureImagePath);
            if (file.exists()) {
                file.delete(); // 删除文件
            }
        }

        // 使用 MyBatis-Plus 删除模型
        modelMapper.delete(new QueryWrapper<Model>().eq("id", model_id)); // 使用 QueryWrapper 根据 name 删除模型

        map.put("success_message", "success");
        return map;
    }

    @Override
    public Map<String, String> update(Map<String, String> data) {
        Map<String, String> map = new HashMap<>();

        // 获取模型名
        String name = data.get("name");
        if (name == null || name.trim().isEmpty()) {
            map.put("error_message", "模型名不能为空");
            return map;
        }

        // 使用 MyBatis-Plus 查询模型对象
        Model model = modelMapper.selectOne(new QueryWrapper<Model>().eq("name", name));
        if (model == null) {
            map.put("error_message", "模型不存在");
            return map;
        }

        // 更新其他字段
        String summary = data.get("summary");
        String environment = data.get("environment");
        String abilityStr = data.get("ability");
        String structureimageBase64 = data.get("structureimage");
        String code = data.get("code");

        if (summary != null && !summary.trim().isEmpty()) {
            model.setSummary(summary);
        }

        if (environment != null && !environment.trim().isEmpty()) {
            model.setEnvironment(environment);
        }

        if (abilityStr != null && !abilityStr.trim().isEmpty()) {
            try {
                Integer ability = Integer.parseInt(abilityStr);
                model.setAbility(ability);
            } catch (NumberFormatException e) {
                map.put("error_message", "模型能力必须是一个整数");
                return map;
            }
        }

        if (code != null && !code.trim().isEmpty()) {
            model.setCode(code);
        }

        // 处理新的结构图片
        if (structureimageBase64 != null && !structureimageBase64.trim().isEmpty()) {
            if (structureimageBase64.startsWith("data:image")) {
                structureimageBase64 = structureimageBase64.split(",")[1]; // 去掉 Base64 前缀
            }

            // 删除原有的图片
            String oldImagePath = model.getStructureimage();
            if (oldImagePath != null && !oldImagePath.trim().isEmpty()) {
                File oldFile = new File(oldImagePath);
                if (oldFile.exists()) {
                    oldFile.delete(); // 删除旧图片
                }
            }

            // 解码新的图片并保存
            byte[] decodedImage = Base64.getDecoder().decode(structureimageBase64);
            String fileName = UUID.randomUUID() + ".png"; // 使用 UUID 生成文件名
            String uploadDir = "/uploads/"; // 假设路径为 /uploads
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs(); // 如果目录不存在，创建目录
            }
            String filePath = uploadDir + fileName;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(decodedImage);
            } catch (IOException e) {
                map.put("error_message", "图片保存失败: " + e.getMessage());
                return map;
            }

            model.setStructureimage(filePath); // 更新图片路径
        }

        // 使用 MyBatis-Plus 更新模型信息
        modelMapper.updateById(model); // 更新操作直接使用 updateById

        map.put("success_message", "success");
        return map;
    }

    @Override
    @Transactional
    public Result<String> rollback(Map<String, String> data) {
        String scene = data.get("scene");
        String model = data.get("model");
        Integer trainId = Integer.parseInt(data.get("trainId"));
        
        UpdateWrapper<Train> trainUpdateWrapper = new UpdateWrapper<>();
        trainUpdateWrapper.eq("scene", scene).eq("model", model).eq("mversion", 1);
        Train trainUpdate = new Train();
        trainUpdate.setMversion(0);
        trainMapper.update(trainUpdate, trainUpdateWrapper);

        Train trainNow = new Train();
        trainNow.setId(trainId);
        trainNow.setMversion(1);
        trainMapper.updateById(trainNow);

        return Result.success();
    }

    @Override
    public byte[] getModelPth(Integer trainId) {
        ModelPth modelPth = modelPthMapper.selectOne(new QueryWrapper<ModelPth>().eq("train_id", trainId));
        if (modelPth == null) {
            return null;
        }
        return modelPth.getModelPth();
    }

    @Override
    public List<AlgParams> getModelParamsAll() {
        return algParamsMapper.selectList(null);
    }
}
