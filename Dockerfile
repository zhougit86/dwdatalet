FROM openjdk:8
ADD datax /usr/bin/datax
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ADD datalet.jar /usr/bin/datalet.jar
ENTRYPOINT ["java","-jar","/usr/bin/datalet.jar"]
