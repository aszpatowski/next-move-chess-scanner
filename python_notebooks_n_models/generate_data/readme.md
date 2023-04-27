## Steps
1. Run positions generator.
2. Run divine_to_train_test.py
    ```
    python3 divine_to_train_test.py --dataset_path raw_data/dataset --train_path raw_data --test_path raw_data
    ```
3. Run create_separate_fields.py
    ```
    python3 create_separate_fields.py
    ```
4. Run create_datasets.py
    ```
    python3 create_datasets.py
    ```
5. Run one by one, to train the network and create tflite models
    - Run blank_or_occupied.py
    ```
    python3 blank_or_occupied.py
    ```
    - Run black_or_white.py
    ```
    python3 black_or_white.py
    ```
    - Run what_piece.py
    ```
    python3 what_piece.py
    ```