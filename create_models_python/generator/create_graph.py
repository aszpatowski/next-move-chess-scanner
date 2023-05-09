import os
import matplotlib.pyplot as plt

# ścieżka do folderu z folderami z obrazami
folder_path = 'C:\\next-move-chess-scanner\\create_models_python\\generator\\pieces'

# stwórz pustą listę na obrazy
images = []

# przejdź przez każdy folder
print(os.listdir(folder_path))
for i in os.listdir(folder_path):
    # stwórz ścieżkę do folderu i przejdź do niego
    folder_name = str(i)
    folder_dir = os.path.join(folder_path, folder_name)
    os.chdir(folder_dir)
    print(os.listdir(folder_dir))
    # dodaj wszystkie obrazy z folderu do listy
    for file_name in os.listdir(folder_dir):
        image_name = file_name
        image_path = os.path.join(folder_dir, image_name)
        image = plt.imread(image_path)
        images.append(image)
    
    # wróć do folderu z folderami
    os.chdir('../')

# stwórz wykres z obrazami
fig, axs = plt.subplots(nrows=21, ncols=12, figsize=(12, 16))
for i in range(21):
    for j in range(12):
        image_index = i*12 + j
        axs[i][j].imshow(images[image_index])
        axs[i][j].axis('off')
    axs[i][0].set_title(str(i+1), loc='left', x=-0.7,y=0.0)

# wyświetl wykres
plt.show()