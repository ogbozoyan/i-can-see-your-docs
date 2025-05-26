import logging

import requests
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
        required: false
        description: Файл для обработки
      - name: url
        in: formData
        type: string
        required: false
        description: URL файла для обработки
    responses:
      200:
        description: ZIP архив с 14 файлами
      400:
        description: Ошибка при загрузке файла
    """
    file_content_bytes = None

    if 'file' in request.files:
        uploaded_file = request.files['file']
        if uploaded_file.filename == '':
            return 'No file selected', 400
        file_content_bytes = uploaded_file.read()

    elif 'url' in request.form:
        file_url = request.form['url']
        logger.info(f"Fetching file from URL: {file_url}")
        try:
            response = requests.get(file_url, timeout=100)
            response.raise_for_status()
            file_content_bytes = response.content
        except requests.RequestException as e:
            logger.error(f"Failed to fetch file from URL: {e}")
            return f"Failed to fetch file from URL: {str(e)}", 400
    else:
        return 'No file or URL provided', 400

    try:
        crops = parser.parser(file_content_bytes)
        zip_buffer = util_file.save_crops_to_zip(crops)

        return Response(
            zip_buffer.getvalue(),
            mimetype="application/zip",
            headers={
                "Content-Disposition": f"attachment;filename=result.zip",
                "Content-Length": len(zip_buffer.getvalue())
            }
        )
    except ValueError as ve:
        logger.error(f"Error decoding image: {str(ve)}")
        return f"Error processing file (image decoding): {str(ve)}", 400
    except Exception as e:
        logger.error(f"Error processing file: {str(e)}", exc_info=True)
        return f"Error processing file: {str(e)}", 500

if __name__ == '__main__':
    app.run(port=PORT, host=HOST) # Changed '0.0.0.0' to HOST from settings for consistency