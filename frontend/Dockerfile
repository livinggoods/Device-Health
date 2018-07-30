FROM nginx:1.15.2-alpine

#COPY dist /device_health_frontend
#RUN mkdir -p /device_health_frontend

#COPY device-health.lg-apps.com /etc/nginx/conf.d/device-health.lg-apps.com.conf
#COPY nginx.conf /etc/nginx/


#WORKDIR /device_health_frontend

# COPY docker-entrypoint.sh /usr/local/bin/

# CMD ["chmod", "+x","/usr/local/bin/docker-entrypoint.sh"]

# CMD ["/usr/local/bin/docker-entrypoint.sh"]

EXPOSE 80

#CMD service nginx restart
CMD ["nginx", "-g", "daemon off;"]


