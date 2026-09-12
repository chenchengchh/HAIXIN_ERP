#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
VAD语音活动检测模型和时间戳预测模型下载脚本
用于下载模型到指定位置
"""

import os
import sys
from pathlib import Path

try:
    from modelscope.hub.snapshot_download import snapshot_download
except ImportError:
    print("错误: 未安装modelscope库，请先安装: pip install modelscope")
    sys.exit(1)

# 模型配置
MODEL_CONFIG = {
    "vad": {
        "name": "damo/speech_fsmn_vad_zh-cn-16k-common-pytorch",
        "save_dir": "speech_fsmn_vad_zh-cn-16k-common-pytorch"
    },
    "timestamp": {
        "name": "damo/speech_timestamp_prediction-v1-16k-offline",
        "save_dir": "speech_timestamp_prediction-v1-16k-offline"
    }
}

# 下载目标根目录
target_root = Path("D:\AItest\HXCOE003260116\frontend\public\models\iic")

def download_model(model_name, save_dir):
    """下载单个模型"""
    print(f"\n=== 开始下载模型: {model_name} ===")
    print(f"保存目录: {save_dir}")
    
    try:
        # 创建保存目录
        save_dir.mkdir(parents=True, exist_ok=True)
        
        # 下载模型
        model_dir = snapshot_download(
            model_name,
            cache_dir=str(save_dir.parent),
            local_dir=str(save_dir),
            local_dir_use_symlinks=False
        )
        
        print(f"✅ 模型下载成功: {model_name}")
        print(f"✅ 模型保存到: {model_dir}")
        
        # 验证下载完整性
        verify_model(model_dir)
        
        return True
        
    except Exception as e:
        print(f"❌ 模型下载失败: {model_name}")
        print(f"❌ 错误信息: {e}")
        import traceback
        traceback.print_exc()
        return False

def verify_model(model_dir):
    """验证模型完整性"""
    print(f"\n=== 验证模型完整性: {model_dir} ===")
    
    # 检查必要文件
    model_dir = Path(model_dir)
    required_files = [".mdl", ".msc", ".mv", "am.mvn", "config.yaml", "configuration.json", "model.pt", "README.md"]
    
    all_exist = True
    for file in required_files:
        file_path = model_dir / file
        if file_path.exists():
            size_mb = os.path.getsize(file_path) / (1024 * 1024)
            print(f"✅ {file} ({size_mb:.2f} MB)")
        else:
            print(f"❌ {file}")
            all_exist = False
    
    # 检查时间戳模型特有文件
    if "timestamp" in str(model_dir).lower():
        timestamp_files = ["seg_dict", "tokens.json"]
        for file in timestamp_files:
            file_path = model_dir / file
            if file_path.exists():
                size_mb = os.path.getsize(file_path) / (1024 * 1024)
                print(f"✅ {file} ({size_mb:.2f} MB)")
            else:
                print(f"❌ {file}")
                all_exist = False
    
    if all_exist:
        print("✅ 模型完整性验证通过")
    else:
        print("❌ 模型完整性验证失败，缺少必要文件")

def main():
    """主函数"""
    print("=== VAD和时间戳预测模型下载脚本 ===")
    print(f"下载目标根目录: {target_root}")
    print(f"当前目录: {os.getcwd()}")
    
    # 检查目标目录
    if not target_root.exists():
        print(f"错误: 目标目录不存在: {target_root}")
        print("请创建目录后再运行脚本")
        sys.exit(1)
    
    # 下载VAD模型
    vad_config = MODEL_CONFIG["vad"]
    vad_save_dir = target_root / vad_config["save_dir"]
    vad_success = download_model(vad_config["name"], vad_save_dir)
    
    # 下载时间戳预测模型
    timestamp_config = MODEL_CONFIG["timestamp"]
    timestamp_save_dir = target_root / timestamp_config["save_dir"]
    timestamp_success = download_model(timestamp_config["name"], timestamp_save_dir)
    
    # 总结
    print("\n=== 下载总结 ===")
    print(f"VAD模型: {'✅ 成功' if vad_success else '❌ 失败'}")
    print(f"时间戳模型: {'✅ 成功' if timestamp_success else '❌ 失败'}")
    
    if vad_success and timestamp_success:
        print("\n🎉 所有模型下载完成!")
        sys.exit(0)
    else:
        print("\n❌ 部分模型下载失败!")
        sys.exit(1)

if __name__ == "__main__":
    main()
