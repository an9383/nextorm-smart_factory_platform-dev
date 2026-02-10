import os
import configparser

current_dir = os.path.dirname(os.path.abspath(__file__))
config_path = os.path.abspath(os.path.join(current_dir, ".."))

config = configparser.ConfigParser()
config.read(config_path + os.path.sep + 'config.ini', encoding='UTF-8')

def get_train_data_root_directory():
    return str(config['FILE_PATH_CONFIG']['train_data_root_directory'])