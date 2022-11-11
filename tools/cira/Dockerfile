FROM python:3.10

COPY . /opt/cira
RUN ls -lah /opt/cira
WORKDIR /opt/cira
RUN ./init.sh

ENV CIRA_URL 0.0.0.0
ENV CIRA_PORT 8000

RUN ./start.sh --no-server-start

ENTRYPOINT [ "bash", "-c", "./start.sh --url ${CIRA_URL} --port ${CIRA_PORT}"]