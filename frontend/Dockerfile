FROM nginx
WORKDIR /Device-Health/frontend

COPY device-health.lg-apps.com /etc/nginx/conf.d/device-health.lg-apps.com.conf

EXPOSE 80

CMD service nginx restart

