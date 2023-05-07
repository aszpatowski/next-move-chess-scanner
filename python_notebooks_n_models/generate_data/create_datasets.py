import argparse
import os
import random
import shutil
from timeit import default_timer as timer

class DatasetsCreator:
    def __init__(self, path):
        self.black_fields = "black_fields"
        self.white_fields = "white_fields"
        self.pieces = ["king", "queen", "knight", "bishop", "rook", "pawn"]
        self.colors = ["black", "white"]
        self.blank = "blank"

    def create_occupied_data(self):
        name = "blank_or_occupied_data"
        self.__create_base_structure(name)
        print("START ", name)
        for color_field in [self.black_fields, self.white_fields]:
            occupied_path = os.path.join(name, color_field, "occupied")
            blank_path = os.path.join(name, color_field, "blank")
            os.makedirs(occupied_path)
            os.makedirs(blank_path)
            for color in self.colors:
                for piece in self.pieces:
                    curr_dir = os.path.join(path, color_field,piece+"_"+color)
                    print("Start: ", curr_dir)
                    for file_name in os.scandir(curr_dir):
                        shutil.copy2(os.path.join(curr_dir,file_name.name), occupied_path)
            curr_dir = os.path.join(path, color_field, self.blank)
            print("Start: ", curr_dir)
            for file_name in os.scandir(curr_dir):
                shutil.copy2(os.path.join(curr_dir,file_name.name), blank_path)
            self.__equalize_directory_sizes(os.path.join(name, color_field))
                
                

    def create_color_data(self):
        name = "black_or_white_data"
        self.__create_base_structure(name)
        print("START ", name)
        for color_field in [self.black_fields, self.white_fields]:
            for color in self.colors:
                color_path = os.path.join(name, color_field, color)
                print("Start: ", color_path)
                os.makedirs(color_path)
                for piece in self.pieces:
                    curr_dir = os.path.join(path, color_field,piece+"_"+color)
                    for file_name in os.scandir(curr_dir):
                        shutil.copy2(os.path.join(curr_dir,file_name.name), color_path)
            self.__equalize_directory_sizes(os.path.join(name, color_field))

    def create_pieces_data(self):
        name = "pieces_data"
        self.__create_base_structure(name)
        print("START ", name)
        for color_field in [self.black_fields, self.white_fields]:
            for color in self.colors:
                for piece in self.pieces:
                    piece_path = os.path.join(name, color_field,color,piece)
                    print("Start: ", piece_path)
                    os.makedirs(piece_path)
                    curr_dir = os.path.join(path,color_field,piece+"_"+color)
                    for file_name in os.scandir(curr_dir):
                        shutil.copy2(os.path.join(curr_dir,file_name.name), piece_path)
                self.__equalize_directory_sizes(os.path.join(name, color_field, color))

    def __create_base_structure(self, name):
        for color_field in [self.black_fields, self.white_fields]:
            os.makedirs(os.path.join(name, color_field), exist_ok=True)

    def __equalize_directory_sizes(self, directory_path):
        """
        Equalizes the number of files in each directory in the specified directory.
        Deletes random files to make the number of files in each directory equal to the 
        number of files in the directory with the least number of files.
        :param directory_path: The path of the directory to process.
        """
        # Get a list of all directories in the directory
        directories = [d for d in os.listdir(directory_path) if os.path.isdir(os.path.join(directory_path, d))]

        # Get the number of files in each directory and store them in a dictionary
        num_files = {}
        for directory in directories:
            files = os.listdir(os.path.join(directory_path, directory))
            num_files[directory] = len(files)

        # Calculate the number of files in the directory with the fewest files
        min_files = min(num_files.values())

        # Equalize the number of files in each directory
        for directory in directories:
            files = os.listdir(os.path.join(directory_path, directory))
            while len(files) > min_files:
                # Delete a random file from the directory
                file_to_remove = random.choice(files) 
                os.remove(os.path.join(directory_path, directory, file_to_remove))
                files.remove(file_to_remove)

if __name__ == "__main__":
    start = timer()
    # parse command line arguments
    parser = argparse.ArgumentParser()
    parser.add_argument("--dataset_path")
    args = parser.parse_args()

    # path to the directory with png images
    path = args.dataset_path
    datasets_creator = DatasetsCreator(path)
    datasets_creator.create_occupied_data()
    datasets_creator.create_color_data()
    datasets_creator.create_pieces_data()
    end = timer()
    print(end - start, "s")