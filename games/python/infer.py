'''
Author: ssp
Date: 2025-03-18 13:10:55
LastEditTime: 2025-03-18 13:40:00
'''
import argparse
import json
import torch
import os
import time

def load_model(checkpoint_path, input_size, output_size):
    class SimpleModel(torch.nn.Module):
        def __init__(self, input_size, output_size):
            super(SimpleModel, self).__init__()
            self.fc = torch.nn.Linear(input_size, output_size)

        def forward(self, x):
            return self.fc(x)

    model = SimpleModel(input_size, output_size)
    model.load_state_dict(torch.load(checkpoint_path))
    model.eval()
    return model

def infer(model, input_tensor):
    with torch.no_grad():
        output = model(input_tensor)
    return output

if __name__ == "__main__":
    print("PID :", os.getpid())
    time.sleep(1)
    parser = argparse.ArgumentParser(description="Inference using trained model.")
    parser.add_argument('--checkpointpath', type=str, required=True, help="Path to the trained model checkpoint.")
    parser.add_argument('--params', type=str, required=True, help="Model parameters in JSON format.")
    args = parser.parse_args()

    # 解析参数
    params = json.loads(args.params)
    input_size = 10  # 允许从参数中获取输入大小
    output_size = 1

    # 加载模型
    if not os.path.exists(args.checkpointpath):
        raise FileNotFoundError(f"Checkpoint not found: {args.checkpointpath}")
    model = load_model(args.checkpointpath, input_size, output_size)

    # 生成随机输入数据
    input_tensor = torch.randn(1, input_size)  # 生成 1 组随机输入
    print("Generated input tensor:", input_tensor.tolist())

    # 运行推理
    output = infer(model, input_tensor)
    print("Inference result:", output.tolist())
