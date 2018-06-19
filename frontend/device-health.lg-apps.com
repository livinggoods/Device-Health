 server {
              listen 80;
              server_name  localhost;
              root /Device-Health/frontend/dist;
              index index.html;

        }