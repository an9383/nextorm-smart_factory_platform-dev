# 158 서버에서 빌드되는 베이스 이미지 이름임.
FROM model-server-base:cudnn-12.1.0

# 로컬에서 개발할 때, 서버와 쿠다 버전이 맞지 않다면,
# 위를 주석처리하고 아래와 같이 다른 이미지를 베이스로 빌드하여 사용해야함
#FROM model-server-base:11.6.cudnn

# Set environment variables
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    PYTHONIOENCODING=utf-8

# Create necessary directories and set permissions
RUN mkdir -p /home/apache-flask-master/test \
    /home/apache-flask-master/dropout_ae \
    /home/apache-flask-master/randomforest_model \
    /var/www/apache-flask/logs \
    /home/apache-flask-master/app \
    /train_data \
    /logs && \
    chown -R www-data:www-data /var/www/ /home/ /train_data /logs

# Copy and configure Apache
COPY apache-flask.conf /etc/apache2/sites-available/apache-flask.conf
RUN a2ensite apache-flask && \
    a2enmod headers && \
    a2dissite 000-default.conf && \
    ln -sf /proc/self/fd/1 /var/log/apache2/access.log && \
    ln -sf /proc/self/fd/1 /var/log/apache2/error.log

# Expose port and define volumes
EXPOSE 80
VOLUME [ "/train_data", "/logs", "/var/www/apache-flask/app" ]

# Set working directory and command
WORKDIR /var/www/apache-flask
CMD chmod -R 777 /train_data && /usr/sbin/apache2ctl -D FOREGROUND