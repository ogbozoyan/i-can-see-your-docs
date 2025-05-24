FROM python:3.9-alpine

WORKDIR /opt/app

RUN apk update && \
    apk add --no-cache \
    build-base \
    cmake \
    g++ \
    python3-dev \
    && rm -rf /var/cache/apk/*

COPY . ./

RUN pip3 install --no-cache-dir -r requirements.txt

CMD ["python3", "src/app.py"]
