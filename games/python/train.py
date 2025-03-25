'''
Author: ssp
Date: 2025-03-13 10:33:15
LastEditTime: 2025-03-16 23:00:36
'''
import time
import argparse
import sys
import json
import os

# 创建解析器以支持命令行参数
parser = argparse.ArgumentParser(description="Train model and record iterations using TensorBoard.")
parser.add_argument('--tensorboardpath', type=str, help="Path to save TensorBoard logs.")
parser.add_argument('--checkpointpath', type=str, help="Path to save model checkpoints.")
parser.add_argument('--params', type=str, help="Model parameters in JSON format.")
args = parser.parse_args()

# 确保日志实时刷新
print("PID :", os.getpid())
time.sleep(1)
print("checkpointPaht is : ", args.checkpointpath)

# 解析参数
print(f"Get process params: {args.params}")
params = json.loads(args.params)
print("params dict:", params)
for i in range(10):
    print(i)
    time.sleep(3)