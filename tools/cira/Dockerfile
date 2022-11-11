FROM python:3.10

COPY . /opt/cira
WORKDIR /opt/cira

RUN chmod +x init.sh
RUN ./init.sh

RUN chmod +x start.sh
RUN ./start.sh --no-server-start

ENV CIRA_URL 0.0.0.0
ENV CIRA_PORT 8000

ENTRYPOINT [ "bash", "-c", "./start.sh --url ${CIRA_URL} --port ${CIRA_PORT}"]
