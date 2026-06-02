FROM liquibase/liquibase:4.29

WORKDIR /liquibase
COPY changelog ./changelog

ENV LIQUIBASE_COMMAND_CHANGELOG_FILE=changelog/db.changelog-master.yaml

CMD ["update"]
