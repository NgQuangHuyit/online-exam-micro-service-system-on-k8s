FROM nginx:1.25-alpine

# Copy custom configuration files
COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# Create a basic HTML file for the root endpoint
RUN mkdir -p /usr/share/nginx/html && \
    echo "<html><head><title>Quiz System API Gateway</title></head><body><h1>Quiz System API Gateway</h1><p>Welcome to the Quiz System API Gateway</p></body></html>" > /usr/share/nginx/html/index.html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]