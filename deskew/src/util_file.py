from PIL import Image
import io
import zipfile

def save_crops_to_zip(crops): # zip_filename parameter is not actually used to write to disk
    """
    Saves multiple image crops (NumPy arrays) into an in-memory ZIP buffer.
    """
    zip_buffer = io.BytesIO()

    # It's more efficient to create the ZipFile object for zip_buffer once
    with zipfile.ZipFile(zip_buffer, "a", zipfile.ZIP_DEFLATED, False) as zipf:
        for name, img_array in crops.items():
            img_pil = Image.fromarray(img_array)

            img_bytes_buffer = io.BytesIO() # Buffer for individual image
            img_pil.save(img_bytes_buffer, format="PNG")
            img_bytes_buffer.seek(0)

            zipf.writestr(f"{name}.png", img_bytes_buffer.read())

    zip_buffer.seek(0) # Rewind buffer to the beginning before reading
    return zip_buffer