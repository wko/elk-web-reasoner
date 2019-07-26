FROM hseeberger/scala-sbt


# Install el2db on image 

RUN git clone https://github.com/wko/el2db && \ 
  cd el2db && \ 
  sbt publishLocal


COPY . /root/myapp

WORKDIR /root/myapp


RUN sbt update && \ 
  sbt compile   

ENV _JAVA_OPTIONS="-Xmx1024m -XX:-UseGCOverheadLimit"

ENV ONTOLOGY_PATH="/root/data/ontologies/snomed.ofn"

RUN mkdir -p /root/data/ontologies && \
    mv /root/myapp/data/ontologies/snomed.ofn /root/data/ontologies

VOLUME ["/root/data/ontologies"]

EXPOSE 8080
CMD sbt "~;jetty:stop;jetty:start"
