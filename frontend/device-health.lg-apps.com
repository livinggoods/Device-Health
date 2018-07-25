 server {
              listen 80;
              server_name  localhost;
              root /device_health_frontend;
              index index.html;

        }