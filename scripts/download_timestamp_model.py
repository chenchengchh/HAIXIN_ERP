import os

# 设置模型名称
TIMESTAMP_MODEL_NAME = "damo/speech_timestamp_prediction-v1-16k-offline"

print(f"开始下载时间戳预测模型: {TIMESTAMP_MODEL_NAME}")

# 直接使用snapshot_download下载模型
try:
    from modelscope.hub.snapshot_download import snapshot_download
    model_dir = snapshot_download(TIMESTAMP_MODEL_NAME)
    print(f"模型下载成功!")
    print(f"模型下载到目录: {model_dir}")
    
    # 显示目录内容
    print(f"目录内容: {os.listdir(model_dir)}")
    for file in os.listdir(model_dir):
        file_path = os.path.join(model_dir, file)
        if os.path.isfile(file_path):
            size = os.path.getsize(file_path) / (1024 * 1024)  # MB
            print(f"  - {file}: {size:.2f} MB")
        else:
            print(f"  - {file}/")
    
except Exception as e:
    print(f"模型下载失败: {e}")
    import traceback
    traceback.print_exc()
