import logging
from flask import Flask, request, Response
from flasgger import Swagger
from dynaconf import Dynaconf
from typing import Tuple, Union
import parser_file as parser
import os
import util_file # util_file is still needed for save_crops_to_zip
from werkzeug.exceptions import RequestEntityTooLarge

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 1024*1024*1024*1024

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

@app.before_request
def log_request_info():
    logger.info(
        f"Request: {request.method} {request.path} | Headers: {dict(request.headers)} | Content-Length: {request.headers.get('Content-Length')}"
    )

@app.after_request
def log_response_info(response):
    logger.info(
        f"Response: {request.method} {request.path} -> {response.status} | Content-Length: {response.headers.get('Content-Length')}"
    )
    return response

@app.errorhandler(RequestEntityTooLarge)
def handle_413(e):
    logger.error(
        f"413 Request Entity Too Large: {request.method} {request.path} | "
        f"Headers: {dict(request.headers)} | Content-Length: {request.headers.get('Content-Length')}"
    )
    return "File too large", 413


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
        description: Файл не был загружен или выбран
    """
    if 'file' not in request.files:
        logger.warning("No file part in the request.")
        return 'No file part in the request', 400

    uploaded_file = request.files['file']

    if uploaded_file.filename == '':
        logger.warning("No file selected for uploading.")
        return 'No file selected', 400

    try:
        file_content_bytes: bytes = uploaded_file.read()
        # filename = uploaded_file.filename # Keep if needed for logging or other purposes

        # Instead of saving to file, pass bytes directly to the parser
        # file_path =  util_file.save_bits_to_file(file_content, uploaded_file.filename) # REMOVE
        crops = parser.parser(file_content_bytes) # MODIFIED: Pass bytes
        zip_buffer = util_file.save_crops_to_zip(crops) # This function already works with in-memory data

        # util_file.delete_file(file_path) # REMOVE: No file to delete

        return Response(
            zip_buffer.getvalue(), # getvalue() is correct for BytesIO
            mimetype="application/zip",
            headers={
                "Content-Disposition": f"attachment;filename=result.zip", # Ensure result.zip is a desired filename
                "Content-Length": len(zip_buffer.getvalue())
            }
        )

    except ValueError as ve: # Catch specific error from parser if image decoding fails
        logger.error(f"Error decoding image: {str(ve)}")
        return f"Error processing file (image decoding): {str(ve)}", 400 # Potentially a 400 if bad image data
    except Exception as e:
        logger.error(f"Error processing file: {str(e)}", exc_info=True) # Add exc_info for better traceback in logs
        return f"Error processing file: {str(e)}", 500

if __name__ == '__main__':
    app.run(port=PORT, host=HOST) # Changed '0.0.0.0' to HOST from settings for consistency