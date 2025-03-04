import time
import argparse
import sys
from torch.utils.tensorboard import SummaryWriter

# 创建解析器以支持命令行参数
parser = argparse.ArgumentParser(description="Record iterations using TensorBoard.")
parser.add_argument('--process_id', type=str, required=True, help="Path to save TensorBoard logs.")
parser.add_argument('--tensorboardpath', type=str, required=True, help="Path to save TensorBoard logs.")
parser.add_argument('--checkpointpath', type=str, help="Path to save TensorBoard logs.")
args = parser.parse_args()
print(f"Running process with ID: {args.process_id}")

# 初始化 TensorBoard SummaryWriter
writer = SummaryWriter(log_dir=args.tensorboardpath)

# 模拟迭代过程
for i in range(100):
    print("itera:", i)
    if i == 2:
        sys.stderr.write("This is an error message\n")

    writer.add_scalar("Iteration Value", i, i)  # 将每次迭代的值记录到 TensorBoard
    time.sleep(5)  # 每 5 秒输出一次

# 关闭 TensorBoard writer
writer.close()
