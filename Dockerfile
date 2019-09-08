FROM adoptopenjdk/openjdk11:latest

RUN apt-get update && \
    apt-get install -y sqlite3 && \
    apt-get install -y cron

# Set up the cron
COPY ./subscription-cron /etc/cron.d/subscription-cron
RUN chmod 0644 /etc/cron.d/subscription-cron
RUN crontab /etc/cron.d/subscription-cron
RUN touch /var/log/cron.log

COPY . /anteus
WORKDIR /anteus

EXPOSE 7000
# When the container starts: build, test and run the app.
CMD cron && ./gradlew build && ./gradlew test && ./gradlew run
