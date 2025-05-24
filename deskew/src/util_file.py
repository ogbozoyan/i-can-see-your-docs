from PIL import Image
import io
import zipfile
import os

def save_crops_to_zip(crops, zip_filename="crops.zip"):
    with zipfile.ZipFile(zip_filename, "w") as zipf:
        zip_buffer = io.BytesIO()

        with zipfile.ZipFile(zip_buffer, "a", zipfile.ZIP_DEFLATED, False) as zipf:
            for name, img_array in crops.items():
                img_pil = Image.fromarray(img_array)

                img_bytes = io.BytesIO()
                img_pil.save(img_bytes, format="PNG")
                img_bytes.seek(0)

                zipf.writestr(f"{name}.png", img_bytes.read())

        zip_buffer.seek(0)
        return zip_buffer

def save_bits_to_file(bytes: bytes, filename):
    img_dir = "img"
    os.makedirs(img_dir, exist_ok=True)

    file_path = os.path.join(img_dir, filename)
    with open(file_path, 'wb') as f:
        f.write(bytes)

    return file_path

def delete_file(file_path):
    if os.path.exists(file_path):
        os.remove(file_path)