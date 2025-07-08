'''
Author: ssp
Date: 2025-03-13 10:33:15
LastEditTime: 2025-07-07 18:51:47
'''
import time
import os

# 确保日志实时刷新
print("PID :", os.getpid())
time.sleep(1)
# 训练模拟过程
for i in range(0, 100):
    print("iter:", i)
    print("trainInfo", i, i, i, i, i, i)
    time.sleep(3)

print("Training completed.")