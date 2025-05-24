import logging
from flask import Flask, request, Response
from flasgger import Swagger
from dynaconf import Dynaconf
from typing import Tuple, Union
import parser_file as parser
import os
import util_file

app = Flask(__name__)
Swagger(app)

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

settings = Dynaconf(
    envvar_prefix="DYNACONF",
    settings_files=['src/resources/application.yml'],
)

PORT = int(os.environ.get("port", settings.PORT))
HOST = os.environ.get("host", settings.HOST)


@app.route('/upload', methods=['POST'])
def upload_file() -> Union[Response, Tuple[str, int]]:
    """
    Загрузка файла и возврат zip-архива с 14 файлами
    ---
    tags:
      - File Processing
    consumes:
      - multipart/form-data
    parameters:
      - name: file
        in: formData
        type: file
        required: true
        description: Файл для обработки
    responses:
      200:
        description: ZIP архив с 14 файлами
        content:
          application/zip:
            schema:
              type: string
              format: binary
      400:
        description: Файл не был загружен
    """
    if 'file' not in request.files:
        return 'No file uploaded', 400

    uploaded_file = request.files['file']

    if uploaded_file.filename == '':
        return 'No file selected', 400


    try:
        file_content: bytes = uploaded_file.read()

        file_path =  util_file.save_bits_to_file(file_content, uploaded_file.filename)
        crops = parser.parser(file_path)
        zip_buffer = util_file.save_crops_to_zip(crops)

        util_file.delete_file(file_path)
        return Response(
            zip_buffer.getvalue(),
            mimetype="application/zip",
            headers={
                "Content-Disposition": f"attachment;filename=result.zip",
                "Content-Length": len(zip_buffer.getvalue())
            }
        )

    except Exception as e:
        logger.error(f"Error processing file: {str(e)}")
        return f"Error processing file: {str(e)}", 500

if __name__ == '__main__':
    app.run(port=PORT, host='0.0.0.0')
