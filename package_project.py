#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
打包MVC项目为JAR文件的Python脚本

功能说明:
1. 自动执行 Maven clean package 命令打包MVC项目
2. 支持指定项目目录路径，默认为当前目录
3. 提供完整的错误处理和日志记录
4. 当Maven不可用时，自动检测并报告现有的JAR文件
5. 生成详细的执行日志，保存在项目的logs目录中

使用方法:
  python package_project.py [项目目录路径]

示例:
  python package_project.py              # 在当前目录打包
  python package_project.py /path/to/my_project  # Linux/macOS路径示例
  python package_project.py D:\\my_project  # Windows路径示例
"""

import os
import sys
import subprocess
import time
import logging
import datetime
import shutil

def setup_logging():
    """
    设置日志记录
    
    Returns:
        logging.Logger: 配置好的日志记录器
    """
    # 创建logs目录
    logs_dir = os.path.join(os.getcwd(), 'logs')
    os.makedirs(logs_dir, exist_ok=True)
    
    # 生成日志文件名
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    log_file = os.path.join(logs_dir, f"package_{timestamp}.log")
    
    # 配置日志记录器
    logger = logging.getLogger('package_script')
    logger.setLevel(logging.DEBUG)
    
    # 清除已有的处理器
    if logger.handlers:
        for handler in logger.handlers:
            handler.close()
        logger.handlers.clear()
    
    # 添加文件处理器
    file_handler = logging.FileHandler(log_file, encoding='utf-8')
    file_handler.setLevel(logging.DEBUG)
    file_formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    file_handler.setFormatter(file_formatter)
    logger.addHandler(file_handler)
    
    # 添加控制台处理器
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    console_formatter = logging.Formatter('%(levelname)s - %(message)s')
    console_handler.setFormatter(console_formatter)
    logger.addHandler(console_handler)
    
    return logger

def package_project(project_dir=None, logger=None):
    """
    打包MVC项目为JAR文件
    
    Args:
        project_dir: 项目目录路径，如果为None则使用当前目录
        logger: 日志记录器，如果为None则创建新的
    
    Returns:
        int: 0表示成功，非0表示失败
    """
    # 如果没有提供日志记录器，则创建一个
    if logger is None:
        logger = setup_logging()
    try:
        # 如果未提供项目目录，则使用当前目录
        if project_dir is None:
            project_dir = os.getcwd()
            logger.info(f"未提供项目目录，使用当前目录: {project_dir}")
        else:
            logger.info(f"使用指定项目目录: {project_dir}")
        
        # 验证项目目录是否存在
        if not os.path.isdir(project_dir):
            error_msg = f"项目目录 '{project_dir}' 不存在"
            logger.error(error_msg)
            print(f"错误: {error_msg}")
            return 1
        
        # 验证项目目录中是否存在pom.xml文件
        pom_file = os.path.join(project_dir, 'pom.xml')
        if not os.path.isfile(pom_file):
            error_msg = f"在 '{project_dir}' 中未找到pom.xml文件，确保这是一个Maven项目"
            logger.error(error_msg)
            print(f"错误: {error_msg}")
            return 1
        
        logger.info(f"开始打包项目: {project_dir}")
        logger.info("执行命令: mvn clean package")
        print(f"\n开始打包项目: {project_dir}")
        print("执行命令: mvn clean package")
    
        # 检查Maven是否已安装
        maven_available = False
        
        # 根据操作系统确定可能的Maven命令
        if os.name == 'nt':  # Windows系统
            possible_maven_commands = ['mvn', 'mvn.cmd', 'mvn.bat']
        else:  # Linux/macOS系统
            possible_maven_commands = ['mvn']
        maven_command = None
        
        # 首先尝试通过shutil.which查找
        for cmd in possible_maven_commands:
            try:
                maven_path = shutil.which(cmd)
                if maven_path:
                    logger.info(f"使用shutil.which找到Maven命令: {maven_path} ({cmd})")
                    maven_command = cmd
                    break
            except Exception as e:
                logger.warning(f"使用shutil.which查找命令 '{cmd}' 时出错: {str(e)}")
        
        # 如果shutil.which没找到，但PATH中有Maven目录，尝试直接执行
        if not maven_command:
            logger.warning("shutil.which未找到Maven命令，尝试直接执行'mvn'...")
            maven_command = 'mvn'  # 默认为mvn命令
        
        # 尝试执行Maven命令验证可用性
        logger.info(f"尝试执行'{maven_command} -version'验证Maven可用性...")
        try:
            # 在Windows下，使用shell=True可能会更可靠
            if os.name == 'nt':  # Windows系统
                result = subprocess.run(
                    f'{maven_command} -version', 
                    shell=True,
                    stdout=subprocess.PIPE,
                    stderr=subprocess.PIPE,
                    text=True
                )
            else:
                result = subprocess.run(
                    [maven_command, '-version'],
                    stdout=subprocess.PIPE,
                    stderr=subprocess.PIPE,
                    text=True
                )
            
            if result.returncode == 0:
                maven_available = True
                # 合并stdout和stderr来提取版本信息
                output = result.stdout + result.stderr
                version_lines = output.split('\n')
                for line in version_lines:
                    if 'Apache Maven' in line:
                        logger.info(f"Maven版本: {line.strip()}")
                        break
                logger.info("Maven验证成功，可以使用")
            else:
                logger.warning(f"Maven命令执行失败，返回码: {result.returncode}")
                logger.debug(f"Maven执行输出:\n{result.stdout}\n{result.stderr}")
        except Exception as e:
            logger.warning(f"执行Maven命令时发生异常: {str(e)}")
            # 再次尝试使用shell=True作为后备方案
            try:
                logger.warning("尝试使用shell=True再次执行Maven命令...")
                # 使用统一的Maven命令格式
                mvn_cmd = 'mvn -version'
                shell_exec = True if os.name == 'nt' else False  # Windows默认使用shell=True
                result = subprocess.run(
                    mvn_cmd, 
                    shell=shell_exec,
                    stdout=subprocess.PIPE,
                    stderr=subprocess.PIPE,
                    text=True
                )
                if result.returncode == 0:
                    maven_available = True
                    logger.info("使用shell=True执行Maven命令成功")
            except Exception as e2:
                logger.warning(f"使用shell=True执行Maven命令也失败: {str(e2)}")
        
        if not maven_available:
            error_msg = "未找到Maven命令，请确保Maven已安装并添加到系统PATH中"
            logger.error(error_msg)
            print(f"错误: {error_msg}")
            
            # 检查target目录是否已经存在jar文件
            target_dir = os.path.join(project_dir, 'target')
            if os.path.isdir(target_dir):
                try:
                    jar_files = [f for f in os.listdir(target_dir) if f.endswith('.jar') and not f.endswith('-sources.jar') and not f.endswith('-tests.jar')]
                    if jar_files:
                        logger.warning(f"虽然Maven不可用，但在target目录中发现了 {len(jar_files)} 个已存在的JAR文件")
                        print("\n注意: 虽然Maven不可用，但在target目录中发现了已存在的JAR文件:")
                        for jar_file in jar_files:
                            jar_path = os.path.join(target_dir, jar_file)
                            try:
                                size = os.path.getsize(jar_path) / (1024 * 1024)  # 转换为MB
                                mtime = datetime.datetime.fromtimestamp(os.path.getmtime(jar_path)).strftime("%Y-%m-%d %H:%M:%S")
                                print(f"- {jar_file} ({size:.2f} MB) - 最后修改时间: {mtime}")
                            except Exception:
                                print(f"- {jar_file} (无法获取详情)")
                        print("\n如果这些JAR文件是最新的，可以直接使用它们。")
                        return 2  # 特殊返回码表示Maven不可用但发现了JAR文件
                except PermissionError:
                    logger.error(f"权限不足，无法读取target目录: {target_dir}")
                    print(f"\n错误: 权限不足，无法读取target目录: {target_dir}")
            
            print("\n提示: 您可以从Apache Maven官网(https://maven.apache.org/download.cgi)下载并安装Maven，")
            print("然后将Maven的bin目录添加到系统PATH环境变量中。")
            return 1
        
        # 执行Maven打包命令
        logger.info("执行Maven打包命令...")
        start_time = time.time()
        
        # 在Windows下使用shell=True执行Maven命令
        if os.name == 'nt':  # Windows系统
            logger.info("Windows系统检测到，使用shell=True执行Maven命令")
            result = subprocess.run(
                'mvn clean package', 
                shell=True,
                cwd=project_dir,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True
            )
        else:
            result = subprocess.run(
                ['mvn', 'clean', 'package'], 
                cwd=project_dir,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True
            )
        end_time = time.time()
        
        # 记录Maven执行的标准输出和错误输出
        logger.debug("Maven标准输出:\n" + result.stdout)
        if result.stderr:
            logger.debug("Maven错误输出:\n" + result.stderr)
        
        # 输出命令执行结果
        if result.returncode == 0:
            success_msg = "打包成功!"
            logger.info(success_msg)
            logger.info(f"执行时间: {end_time - start_time:.2f} 秒")
            
            print(f"\n{success_msg}")
            print(f"执行时间: {end_time - start_time:.2f} 秒")
            
            # 查找生成的jar文件
            target_dir = os.path.join(project_dir, 'target')
            jar_paths = []
            if os.path.isdir(target_dir):
                try:
                    jar_files = [f for f in os.listdir(target_dir) if f.endswith('.jar') and not f.endswith('-sources.jar') and not f.endswith('-tests.jar')]
                    if jar_files:
                        logger.info(f"找到 {len(jar_files)} 个JAR文件")
                        print("\n生成的JAR文件:")
                        for jar_file in jar_files:
                            jar_path = os.path.join(target_dir, jar_file)
                            jar_paths.append(jar_path)
                            try:
                                size = os.path.getsize(jar_path) / (1024 * 1024)  # 转换为MB
                                logger.info(f"- {jar_file} ({size:.2f} MB)")
                                print(f"- {jar_file} ({size:.2f} MB)")
                            except Exception as e:
                                logger.error(f"获取文件大小失败: {jar_file}, 错误: {str(e)}")
                                print(f"- {jar_file} (无法获取大小)")
                    else:
                        logger.warning("在target目录中未找到JAR文件")
                        print("\n警告: 在target目录中未找到JAR文件")
                except Exception as e:
                    logger.error(f"读取target目录失败: {str(e)}")
                    print(f"\n错误: 读取target目录失败: {str(e)}")
            
            return 0
        else:
            error_msg = "打包失败!"
            logger.error(error_msg)
            logger.error(f"Maven返回码: {result.returncode}")
            
            print(f"\n{error_msg}")
            print(f"Maven返回码: {result.returncode}")
            
            # 显示错误输出的最后几行
            error_lines = result.stderr.strip().split('\n')
            recent_errors = error_lines[-10:]  # 显示最后10行
            print("\n最近的错误输出:")
            for line in recent_errors:
                print(line)
            
            return result.returncode
            
    except FileNotFoundError:
        error_msg = "执行Maven命令时出现FileNotFoundError"
        logger.error(error_msg)
        print(f"错误: {error_msg}")
        logger.info("尝试使用shell=True再次执行Maven命令作为后备方案...")
        try:
            start_time = time.time()
            # 使用与平台相关的命令执行方式
            mvn_cmd = 'mvn clean package'
            shell_exec = True if os.name == 'nt' else False
            result = subprocess.run(
                mvn_cmd, 
                shell=shell_exec,
                cwd=project_dir,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True
            )
            end_time = time.time()
            
            # 记录Maven执行的标准输出和错误输出
            logger.debug("Maven标准输出:\n" + result.stdout)
            if result.stderr:
                logger.debug("Maven错误输出:\n" + result.stderr)
            
            # 输出命令执行结果
            if result.returncode == 0:
                success_msg = "打包成功! (使用shell=True执行)"
                logger.info(success_msg)
                logger.info(f"执行时间: {end_time - start_time:.2f} 秒")
                
                print(f"\n{success_msg}")
                print(f"执行时间: {end_time - start_time:.2f} 秒")
                
                # 查找生成的jar文件
                target_dir = os.path.join(project_dir, 'target')
                jar_paths = []
                if os.path.isdir(target_dir):
                    try:
                        jar_files = [f for f in os.listdir(target_dir) if f.endswith('.jar') and not f.endswith('-sources.jar') and not f.endswith('-tests.jar')]
                        if jar_files:
                            logger.info(f"找到 {len(jar_files)} 个JAR文件")
                            print("\n生成的JAR文件:")
                            for jar_file in jar_files:
                                jar_path = os.path.join(target_dir, jar_file)
                                jar_paths.append(jar_path)
                                try:
                                    size = os.path.getsize(jar_path) / (1024 * 1024)  # 转换为MB
                                    logger.info(f"- {jar_file} ({size:.2f} MB)")
                                    print(f"- {jar_file} ({size:.2f} MB)")
                                except Exception as e:
                                    logger.error(f"获取文件大小失败: {jar_file}, 错误: {str(e)}")
                                    print(f"- {jar_file} (无法获取大小)")
                        else:
                            logger.warning("在target目录中未找到JAR文件")
                            print("\n警告: 在target目录中未找到JAR文件")
                    except Exception as e:
                        logger.error(f"读取target目录失败: {str(e)}")
                        print(f"\n错误: 读取target目录失败: {str(e)}")
                
                return 0
            else:
                error_msg = "打包失败! (使用shell=True执行也失败)"
                logger.error(error_msg)
                logger.error(f"Maven返回码: {result.returncode}")
                
                print(f"\n{error_msg}")
                print(f"Maven返回码: {result.returncode}")
                
                return result.returncode
        except Exception as e:
            logger.error(f"后备执行方案也失败: {str(e)}")
            print(f"\n后备执行方案也失败: {str(e)}")
        
        # 检查target目录是否已经存在jar文件
        target_dir = os.path.join(project_dir, 'target')
        if os.path.isdir(target_dir):
            try:
                jar_files = [f for f in os.listdir(target_dir) if f.endswith('.jar') and not f.endswith('-sources.jar') and not f.endswith('-tests.jar')]
                if jar_files:
                    logger.warning(f"虽然无法执行Maven，但在target目录中发现了 {len(jar_files)} 个已存在的JAR文件")
                    print("\n注意: 虽然无法执行Maven，但在target目录中发现了已存在的JAR文件:")
                    for jar_file in jar_files:
                        jar_path = os.path.join(target_dir, jar_file)
                        try:
                            size = os.path.getsize(jar_path) / (1024 * 1024)  # 转换为MB
                            mtime = datetime.datetime.fromtimestamp(os.path.getmtime(jar_path)).strftime("%Y-%m-%d %H:%M:%S")
                            print(f"- {jar_file} ({size:.2f} MB) - 最后修改时间: {mtime}")
                        except Exception:
                            print(f"- {jar_file} (无法获取详情)")
                    print("\n如果这些JAR文件是最新的，可以直接使用它们。")
                    return 2  # 特殊返回码表示Maven不可用但发现了JAR文件
            except PermissionError:
                logger.error(f"权限不足，无法读取target目录: {target_dir}")
                print(f"\n错误: 权限不足，无法读取target目录: {target_dir}")
        
        return 1
    except PermissionError:
        error_msg = f"权限不足，无法访问项目目录或执行命令: {str(sys.exc_info()[1])}"
        logger.error(error_msg)
        print(f"错误: {error_msg}")
        return 1
    except KeyboardInterrupt:
        logger.info("用户中断了打包过程")
        print("\n打包过程已被用户中断")
        # 使用与操作系统相关的退出码
        return 130 if os.name != 'nt' else 1  # 130是Unix中Ctrl+C的退出码，Windows使用1
    except Exception as e:
        error_msg = f"执行打包命令时发生异常: {str(e)}"
        logger.error(error_msg, exc_info=True)
        print(f"错误: {error_msg}")
        return 1

def show_help():
    """
    显示帮助信息
    """
    print("\nMVC项目打包工具 v1.0 (跨平台兼容)")
    print("============================")
    print("功能: 自动执行Maven打包命令，生成JAR文件")
    print("\n使用方法:")
    print("  python package_project.py [项目目录路径] [-h|--help]")
    print("\n参数说明:")
    print("  项目目录路径    可选，指定要打包的MVC项目目录，默认为当前目录")
    print("  -h, --help      显示此帮助信息并退出")
    print("\n路径示例:")
    print("  Linux/macOS: python package_project.py /path/to/project")
    print("  Windows:    python package_project.py D:\\path\\to\\project")
    print("\n返回码说明:")
    print("  0     成功: Maven打包成功")
    print("  1     失败: Maven不可用或执行失败")
    print("  2     警告: Maven不可用但发现了现有的JAR文件")
    print("\n日志文件:")
    print("  执行日志保存在项目根目录下的logs文件夹中")
    print()

if __name__ == "__main__":
    # 检查是否显示帮助信息
    if len(sys.argv) > 1 and (sys.argv[1] == "-h" or sys.argv[1] == "--help"):
        show_help()
        sys.exit(0)
    
    # 设置日志记录
    logger = setup_logging()
    logger.info("=== 启动MVC项目打包脚本 ===")
    
    # 检查是否提供了命令行参数作为项目目录
    project_dir = None
    if len(sys.argv) > 1:
        project_dir = sys.argv[1]
        logger.info(f"从命令行参数获取项目目录: {project_dir}")
    
    try:
        # 添加调试信息
        logger.info(f"当前工作目录: {os.getcwd()}")
        logger.info(f"系统PATH环境变量: {os.environ.get('PATH', '')}")
        logger.info(f"尝试查找Maven命令...")
        
        # 尝试直接查找Maven
        try:
            mvn_path = shutil.which('mvn')
            logger.info(f"使用shutil.which查找Maven结果: {mvn_path}")
        except Exception as e:
            logger.warning(f"使用shutil.which查找Maven时出错: {str(e)}")
        
        # 执行打包并返回结果
        exit_code = package_project(project_dir, logger)
        logger.info(f"脚本执行完成，退出码: {exit_code}")
        
        # 输出退出码说明
        if exit_code == 0:
            print("\n✅ 打包成功完成!")
        elif exit_code == 2:
            print("\n⚠️ Maven不可用，但发现了现有的JAR文件")
        else:
            print("\n❌ 打包过程遇到错误")
            print("\n提示: 详细信息请查看logs目录中的日志文件")
            print("\n调试信息已记录到日志文件，包含PATH环境变量和Maven查找结果")
        
        sys.exit(exit_code)
    except Exception as e:
        logger.critical(f"脚本执行过程中发生严重错误: {str(e)}", exc_info=True)
        print(f"\n严重错误: {str(e)}")
        print("提示: 详细信息请查看logs目录中的日志文件")
        sys.exit(1)
