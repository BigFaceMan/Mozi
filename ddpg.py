import time
import argparse
import sys
import json
import os
import torch
from torch.utils.tensorboard import SummaryWriter

# 创建解析器以支持命令行参数
parser = argparse.ArgumentParser(description="Train model and record iterations using TensorBoard.")
parser.add_argument('--tensorboardpath', type=str, help="Path to save TensorBoard logs.")
parser.add_argument('--checkpointpath', type=str, help="Path to save model checkpoints.")
parser.add_argument('--params', type=str, help="Model parameters in JSON format.")
args = parser.parse_args()

# 确保日志实时刷新
print("PID:", os.getpid())
sys.stdout.flush()

# 解析参数
print(f"Get process params: {args.params}")
params = json.loads(args.params)
print("params dict:", params)

# 确保 checkpoint 目录存在
if not os.path.exists(args.checkpointpath):
    os.makedirs(args.checkpointpath)

# 初始化 TensorBoard SummaryWriter
writer = SummaryWriter(log_dir=args.tensorboardpath)

# 定义一个简单的 PyTorch 模型
class SimpleModel(torch.nn.Module):
    def __init__(self, input_size, output_size):
        super(SimpleModel, self).__init__()
        self.fc = torch.nn.Linear(input_size, output_size)

    def forward(self, x):
        return self.fc(x)

# 从参数中获取输入和输出维度，提供默认值
input_size = params.get("input_size", 10)  # 默认 10
output_size = params.get("output_size", 1)  # 默认 1

# 初始化模型
model = SimpleModel(input_size, output_size)

# 训练模拟过程
for i in range(100):
    print("Itera:", i)
    sys.stdout.flush()

    if i == 2:
        sys.stderr.write("This is an error message\n")
        sys.stderr.flush()

    writer.add_scalar("Iteration Value", i, i)  # 记录 TensorBoard 数据

    # 每 50 轮保存一次模型
    if i % 50 == 0 or i == 99:
        checkpoint_file = os.path.join(args.checkpointpath, f"model_epoch_{i}.pth")
        torch.save(model.state_dict(), checkpoint_file)
        print(f"Checkpoint saved: {checkpoint_file}")
        sys.stdout.flush()
# 关闭 TensorBoard writer
writer.close()
print("Training completed.")
sys.stdout.flush()
