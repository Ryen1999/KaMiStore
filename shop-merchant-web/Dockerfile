# shop-merchant-web/Dockerfile
FROM nginx:alpine

# 删除默认配置
RUN rm /etc/nginx/conf.d/default.conf

# 复制自定义配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制前端静态文件
COPY dist/ /usr/share/nginx/html/

# 暴露端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
