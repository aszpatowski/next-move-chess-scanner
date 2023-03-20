import os
import random
import shutil
import argparse

# parse command line arguments
parser = argparse.ArgumentParser(description="Split images in a directory into training and testing sets")
parser.add_argument("--dataset_path", help="path to the directory with jpeg images")
parser.add_argument("--train_path", help="path to the directory where train directory will be created")
parser.add_argument("--test_path", help="path to the directory where test directory will be created")
parser.add_argument("--test-size", type=int, default=20, help="percentage of images to be used for testing")
args = parser.parse_args()

# path to the directory with jpeg images
path = args.dataset_path

# path to the directory where train and test directories will be created
train_path = args.train_path
test_path = args.test_path

# percentage of images to be used for testing
test_size = args.test_size

# create train and test directories
os.makedirs(os.path.join(train_path, "train"), exist_ok=True)
os.makedirs(os.path.join(test_path, "test"), exist_ok=True)

# get list of jpeg files in the dataset directory
files = [f for f in os.listdir(path) if f.endswith(".jpeg")]

# shuffle the list of files randomly
random.shuffle(files)

# calculate number of images for testing
num_test = int(len(files) * test_size / 100)

# move images to test directory
for file in files[:num_test]:
    shutil.move(os.path.join(path, file), os.path.join(test_path, "test", file))

# move images to train directory
for file in files[num_test:]:
    shutil.move(os.path.join(path, file), os.path.join(train_path, "train", file))
