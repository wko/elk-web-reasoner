FROM hseeberger/scala-sbt


COPY . /root/myapp

WORKDIR /root/myapp

RUN sbt compile   

ENV _JAVA_OPTIONS="-Xmx1024m -XX:-UseGCOverheadLimit"

ENV ONTOLOGY_PATH="/root/data/ontologies/snomed.ofn"

RUN mkdir -p /root/data/ontologies && \
    mv /root/myapp/data/ontologies/snomed.ofn /root/data/ontologies

VOLUME ["/root/data/ontologies", "/root/myapp"]

EXPOSE 8080
CMD sbt "~;jetty:stop;jetty:start"
