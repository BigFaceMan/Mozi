'''
Author: ssp
Date: 2025-03-13 10:33:15
LastEditTime: 2025-04-10 20:32:43
'''
import time
import argparse
import sys
import json
import os


# 确保日志实时刷新
print("PID :", os.getpid())
time.sleep(1)
for i in range(50):
    print(i)
    print("trainInfo", i, i, i, i, i, i)
    time.sleep(2)