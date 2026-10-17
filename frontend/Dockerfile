FROM nginx:1.27-alpine
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY public/ /usr/share/nginx/html/
ENV NGINX_PORT=80 \
    BACKEND_URL=http://backend:8080
EXPOSE 80
