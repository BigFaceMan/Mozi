'''
Author: ssp
Date: 2025-03-13 10:33:15
LastEditTime: 2025-04-13 18:28:59
'''
import time
import argparse
import sys
import json
import os
import torch
# from torch.utils.tensorboard import SummaryWriter

# 创建解析器以支持命令行参数
parser = argparse.ArgumentParser(description="Train model and record iterations using TensorBoard.")
parser.add_argument('--tensorboardpath', type=str, help="Path to save TensorBoard logs.")
parser.add_argument('--checkpointpath', type=str, help="Path to save model checkpoints.")
parser.add_argument('--params', type=str, help="Model parameters in JSON format.")
parser.add_argument('--reload', type=str, help="Model parameters in JSON format.")
args = parser.parse_args()

# 确保日志实时刷新
print("PID :", os.getpid())
time.sleep(1)
print("checkpointPaht is : ", args.checkpointpath)

# 解析参数
print(f"Get process params: {args.params}")
params = json.loads(args.params)
print("params dict:", params)
trainIters = int(params.get("trainIters", 1000))  # 默认训练迭代次数

continueTrain = int(params["reload"])
# 定义一个简单的 PyTorch 模型
class SimpleModel(torch.nn.Module):
    def __init__(self, input_size, output_size):
        super(SimpleModel, self).__init__()
        self.fc = torch.nn.Linear(input_size, output_size)

    def forward(self, x):
        return self.fc(x)

# 从参数中获取输入和输出维度，提供默认值
input_size = 10  # 默认 10
output_size = 1  # 默认 1

# 初始化模型
model = SimpleModel(input_size, output_size)

if continueTrain:
    print("Continue training from checkpoint")
    # 这里可以添加加载模型参数的逻辑
    model.load_state_dict(torch.load(args.checkpointpath))
    print("Model loaded from checkpoint")
# trainRangeL, trainRangeR
trainRangeL = int(params["trainRangeL"])
trainRangeR = int(params["trainRangeR"])

# 训练模拟过程
for i in range(trainRangeL, trainRangeR):
    print("Itera:", i)
    print("trainInfo", i, i, i, i, i, i)
    if i == 2:
        sys.stderr.write("This is an error message\n")
    # writer.add_scalar("Iteration Value", i, i)  # 记录 TensorBoard 数据
    # 每 5 轮保存一次模型
    if i % 5 == 0 or i == 99:
        print(f"Checkpoint saved: {args.checkpointpath}")
        torch.save(model.state_dict(), args.checkpointpath)
    time.sleep(3)

# writer.close()
print("Training completed.")