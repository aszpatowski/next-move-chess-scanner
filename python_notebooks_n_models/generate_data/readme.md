## Steps
1. Run positions generator.
2. Run divine_to_train_test.py
    ```
    python3 divine_to_train_test.py --dataset_path raw_data/dataset --train_path raw_data --test_path raw_data
    ```
3. Run create_separate_fields.py
    ```
    python create_separate_fields.py --start_train 0 --end_train 16000 --start_test 0 --end_test 4000
    ```
4. Run create_datasets.py to create seperate datasets for every neural network models
    ```
    python3 create_datasets.py --dataset_path data
    ```
5. Run one by one, to train the network and create tflite models
    - Run blank_or_occupied.py
    ```
    python blank_or_occupied.py
    ```
    - Run black_or_white.py
    ```
    python black_or_white.py
    ```
    - Run what_piece.py
    ```
    python what_piece.py
    ```
6. 